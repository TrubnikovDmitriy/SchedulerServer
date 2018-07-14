package notifications;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.internal.NonNull;
import models.Event;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import parallelworking.DateChecker;
import parallelworking.TasksExecutor;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;


public abstract class Notification implements Runnable, ValueEventListener, DateChecker {

	private final AtomicBoolean isActive = new AtomicBoolean(false);

	private long retrieveTime = 0L;
	private long newTime = 0L;
	private long oldTime = DateTime.now().getMillis();

	protected final Logger logger = Logger.getLogger(getClass());
	protected final long millisecDelay;


	public Notification(long millisecDelay) {
		this.millisecDelay = millisecDelay;
	}

	@Override
	public final void run() {
		// If the previous tasks are not completed
		if (!isActive.compareAndSet(false, true)) {
			logger.error("The previous task is still running");
			return;
		}

		// Start the cycle
		retrieveTime = DateTime.now().getMillis();
		logger.info("Start retrieve private data from Firebase RD");
		// Try to get data from Firebase
		getReference().addListenerForSingleValueEvent(this);
	}

	@Override
	public final void onDataChange(DataSnapshot dataSnapshot) {
		newTime = DateTime.now().getMillis();
		logger.info("Received data snapshot for [" + (newTime - retrieveTime) + " ms]");
		logger.info("Start searching the events between " + oldTime + " and " + newTime);

		if (dataSnapshot != null) {
			processData(dataSnapshot);
		} else {
			logger.error("Recieved data is null");
		}
		isActive.set(false);
	}

	@Override
	public final void onCancelled(DatabaseError databaseError) {
		logger.error(databaseError.getMessage(), databaseError.toException());
		isActive.set(false);
	}

	private void processData(@NonNull DataSnapshot snapshot) {

		final Queue<TasksExecutor> executors = getExecutors(snapshot);

		try {
			for (final TasksExecutor executor : executors) {
				executor.parallelExecute();
			}

		} catch (InterruptedException e) {
			logger.error("Data processing was interrupted", e);
			return;
		}

		logger.info("End retrieve private data from Firebase RD");
		oldTime = newTime;
	}


	protected abstract Queue<TasksExecutor> getExecutors(@NonNull DataSnapshot inputData);

	protected abstract DatabaseReference getReference();


	@Override
	@SuppressWarnings("OverlyComplexMethod")
	public boolean checkDate(final long time, Event.EventType type) {

		final Logger debugLogger = Logger.getLogger(Thread.currentThread().getName());
		debugLogger.debug("Time: " + time + ", type: " + type.name());

		final DateTime dateTime = new DateTime(time);
		final long millisOfDay = dateTime.getMillisOfDay();

		final boolean isItOutOfTimeRange =
				millisOfDay <= new DateTime(oldTime).getMillisOfDay() ||
						new DateTime(newTime).getMillisOfDay() < millisOfDay;

		// When in any case, the time is out of range
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
