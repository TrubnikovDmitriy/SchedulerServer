package notifications;

import tools.DatabaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import models.EventModel;
import models.Notification;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import parallelworking.TasksExecutor;
import parallelworking.executors.ParsingExecutor;
import parallelworking.executors.SendingExecutor;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


public class PrivateNotification implements Runnable, ValueEventListener {

	public static final long DELAY_MILLISECONDS = 30_000L;
	private static final int THREAD_COUNT = 10 * Runtime.getRuntime().availableProcessors();

	private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(THREAD_COUNT);
	private static final AtomicBoolean IS_ACTIVE = new AtomicBoolean(false);

	private static long retrieveTime = 0L;
	private static long newTime = 0L;
	private static long oldTime = DateTime.now().getMillis() - DELAY_MILLISECONDS;

	private final Logger logger = Logger.getLogger(getClass());


	@Override
	public void run() {
		// If prevous task is not complete
		if (!IS_ACTIVE.compareAndSet(false, true)) {
			logger.error("The previous task is still running");
			return;
		}

		// Start the cycle
		retrieveTime = DateTime.now().getMillis();
		logger.info("Start retrieve private data from Firebase RD");
		// Try to get data from Firebase
		DatabaseHelper.getPrivate().addListenerForSingleValueEvent(this);
	}

	@Override
	public void onDataChange(DataSnapshot snapshot) {
		newTime = DateTime.now().getMillis();
		logger.info("Received data snapshot for [" + (newTime - retrieveTime) + " ms]");
		logger.debug("Start searching the events between " + oldTime + " and " + newTime);

		processData(snapshot);
		IS_ACTIVE.set(false);
	}

	@Override
	public void onCancelled(DatabaseError databaseError) {
		logger.error(databaseError.getMessage(), databaseError.toException());
		IS_ACTIVE.set(false);
	}


	private void processData(DataSnapshot userNodes) {

		if (userNodes == null) {
			logger.error("Recieved data is null");
			return;
		}


		final ConcurrentLinkedQueue<Notification> notifications = new ConcurrentLinkedQueue<>();

		final TasksExecutor parseExecutor = new ParsingExecutor(EXECUTOR, userNodes, notifications);
		final TasksExecutor sendExecutor = new SendingExecutor(EXECUTOR, notifications);

		try {
			parseExecutor.parallelExecute();
			sendExecutor.parallelExecute();

		} catch (InterruptedException e) {
			logger.error("Data processing was interrupted", e);
			return;
		}

		logger.info("End retrieve private data from Firebase RD");
		oldTime = newTime;
	}

	@SuppressWarnings("OverlyComplexMethod")
	public static boolean isItTime(final long time, EventModel.EventType type) {

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
}
