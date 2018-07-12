package notifications;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import models.EventModel;
import models.Notification;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


class PrivateDataHandler implements Runnable, ValueEventListener {

	private static long oldTime = DateTime.now().getMillis() - NotificationService.DELAY_BETWEEN_PRIVATE_RETRIEVE;
	private static long newTime = 0L;

	private static final int THREAD_COUNT = 10 * Runtime.getRuntime().availableProcessors();
	private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(THREAD_COUNT);
	private static final AtomicBoolean IS_RUNNING = new AtomicBoolean(false);

	private final AtomicLong threadCounter = new AtomicLong();
	private final Logger logger = Logger.getLogger(getClass());
	

	@Override
	public void run() {
		// If prevous task is not complete
		if (!IS_RUNNING.compareAndSet(false, true)) {
			logger.error("The previous task is still running");
			return;
		}
		// Start the cycle
		logger.info("Start retrieve private data from Firebase RD");
		// Try to get data from Firebase
		DatabaseHelper.getPrivate().addListenerForSingleValueEvent(this);
	}

	@Override
	public void onDataChange(DataSnapshot dataSnapshot) {

		newTime = DateTime.now().getMillis();
		logger.info("Get data snapshot");
		logger.debug("Start searching the events between " + oldTime + " and " + newTime);

		if (dataSnapshot == null) {
			logger.error("Recieved data is null");
			IS_RUNNING.set(false);
			return;
		}


		synchronized (threadCounter) {

			final ConcurrentLinkedQueue<Notification> queue = new ConcurrentLinkedQueue<>();
			final OnTaskComplete onTaskComplete = () -> {
				final long remainTasks = threadCounter.decrementAndGet();
				// If all tasks are completed, then notify the main thread
				if (remainTasks == 0) {
					synchronized (threadCounter) {
						threadCounter.notify();
					}
				}
			};

			// Start parsing users' nodes in parallel threads
			if (dataSnapshot.getChildrenCount() != 0) {
				threadCounter.set(dataSnapshot.getChildrenCount());
				for (final DataSnapshot userNode : dataSnapshot.getChildren()) {
					EXECUTOR.execute(new ParseUserNode(userNode, queue, onTaskComplete));
				}
				try {
					final long startParseTime = DateTime.now().getMillis();
					logger.debug("Start to wait parsing");

					// Wait until other threads finish working
					threadCounter.wait();

					final long parseTime = DateTime.now().getMillis() - startParseTime;
					logger.info("Parsing take [" + parseTime + " ms] for [" +
							dataSnapshot.getChildrenCount() + " tasks]");

				} catch (InterruptedException e) {
					logger.error("The wait of parsing is interrupted", e);
					IS_RUNNING.set(false);
					return;
				}
			}


			// Start sending messages to users
			if (!queue.isEmpty()) {
				threadCounter.set(queue.size());
				for (final Notification notification : queue) {
					EXECUTOR.execute(new SendNotification(notification, onTaskComplete));
				}
				try {
					final long startSendTime = DateTime.now().getMillis();
					logger.debug("Start to wait sending");

					// Wait until other threads finish working
					threadCounter.wait();

					final long sendTime = DateTime.now().getMillis() - startSendTime;
					logger.info("Sending take [" + sendTime + " ms] for [" + queue.size() + " tasks]");

				} catch (InterruptedException e) {
					logger.error("The wait of sending is interrupted", e);
					IS_RUNNING.set(false);
					return;
				}
			}
		}

		logger.info("End retrieve private data from Firebase RD");
		oldTime = newTime;
		IS_RUNNING.set(false);
	}

	@Override
	public void onCancelled(DatabaseError databaseError) {
		logger.error(databaseError.getMessage(), databaseError.toException());
		IS_RUNNING.set(false);
	}


	private static class ParseUserNode implements Runnable {

		private final ConcurrentLinkedQueue<Notification> queue;
		private final OnTaskComplete onTaskComplete;
		private final DataSnapshot userNode;

		ParseUserNode(DataSnapshot userNode,
		              ConcurrentLinkedQueue<Notification> queue,
		              OnTaskComplete onTaskComplete) {
			this.queue = queue;
			this.userNode = userNode;
			this.onTaskComplete = onTaskComplete;
		}

		@Override
		public void run() {
			final Logger logger = Logger.getLogger(Thread.currentThread().getName());
			logger.debug("Start parse user's node");

			final String userUID = userNode
					.getKey();

			final String token = userNode
					.child(DatabaseHelper.TOKEN)
					.getValue(String.class);

			if (token == null) {
				logger.error("Token of user <" + userUID + "> is not defined");
				onTaskComplete.complete();
				return;
			}

			final DataSnapshot events = userNode
					.child(DatabaseHelper.DASHBOARDS)
					.child(DatabaseHelper.EVENTS);

			if (events == null) {
				logger.debug("User hasn't dashboards");
				onTaskComplete.complete();
				return;
			}

			final Notification notification = new Notification(userUID, token);

			logger.debug("User <" + userUID + "> have " + events.getChildrenCount() + " dashboards");
			for (DataSnapshot dashboardNode : events.getChildren()) {

				logger.debug("Dashboard <" + dashboardNode.getKey() + "> have " + dashboardNode.getChildrenCount() + " events");
				for (DataSnapshot eventNode : dashboardNode.getChildren()) {

					final EventModel event = eventNode.getValue(EventModel.class);
					if (event == null || event.isInvalid()) {
						logger.error("Event <" + eventNode.getKey() + "> in dashboard <" + dashboardNode.getKey() + "> is invalid");
						continue;
					}

					logger.debug("Check event <" + eventNode.getKey() + '>');
					if (!isItTime(event.getTimestamp(), event.getType())) {
						continue;
					}
					logger.debug("The time of event <" + eventNode.getKey() + "> has come");

					event.setDashID(dashboardNode.getKey());
					event.setEventID(eventNode.getKey());

					notification.addNotification(event);
				}
			}

			if (!notification.getNotification().isEmpty()) {
				logger.debug("Add non-empty notification to queue");
				queue.add(notification);
			}
			logger.debug("End parse user's node");
			onTaskComplete.complete();
		}
	}

	private static class SendNotification implements Runnable {

		private final OnTaskComplete onTaskComplete;
		private final Notification notification;

		SendNotification(Notification notification, OnTaskComplete onTaskComplete) {
			this.notification = notification;
			this.onTaskComplete = onTaskComplete;
		}

		@Override
		public void run() {
			final Logger logger = Logger.getLogger(Thread.currentThread().getName());
			logger.debug("Sending message(s) for user " + notification.getUserUID());

			for (final EventModel event : notification.getNotification()) {
				// Build message
				final Message message = Message.builder()
						.setToken(notification.getToken())
						.putAllData(event.toMap())
						.build();

				// Send message
				try {
					final String response = FirebaseMessaging.getInstance().send(message);
					logger.debug("Successfully sent message: " + response);

				} catch (FirebaseMessagingException e) {
					logger.error(e.getMessage(), e);
				}
			}

			onTaskComplete.complete();
		}
	}


	private static boolean isItTime(final long time, EventModel.EventType type) {

		final Logger debugLogger = Logger.getLogger(Thread.currentThread().getName());
		debugLogger.debug("Time: " + time + ", type: " + type.name());

		final DateTime dateTime = new DateTime(time);
		final long millisOfDay = dateTime.getMillisOfDay();

		final boolean isItOutOfTimeRange =
				millisOfDay <= new DateTime(oldTime).getMillisOfDay() ||
				new DateTime(newTime).getMillisOfDay() < millisOfDay;

		// In any case, the time is out of range
		if (isItOutOfTimeRange) {
			debugLogger.debug("It is not now");
			return false;
		}

		switch (type) {
			case EVERY_CENTURY:
				return dateTime.getCenturyOfEra() == DateTime.now().getCenturyOfEra();

			case EVERY_YEAR:
				// Double 'if' is necessary to avoid problem of leap years
				return (dateTime.getMonthOfYear() == DateTime.now().getMonthOfYear() &&
						dateTime.getDayOfMonth() == DateTime.now().getDayOfMonth());

			case EVERY_MONTH:
				return dateTime.getDayOfMonth() == DateTime.now().getDayOfMonth();

			case EVERY_WEEK:
				return dateTime.getDayOfWeek() == DateTime.now().getDayOfWeek();

			case EVERY_DAY:
				return true;

			case ONE_TIME:
				return (oldTime < time && time <= newTime);

			default:
				return false;
		}
	}

	public interface OnTaskComplete {
		void complete();
	}
}
