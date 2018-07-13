package notifications;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.internal.NonNull;
import models.Event;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;


import java.util.concurrent.atomic.AtomicBoolean;


public abstract class Notification implements Runnable, ValueEventListener {

	private static final AtomicBoolean IS_ACTIVE = new AtomicBoolean(false);

	protected static long retrieveTime = 0L;
	protected static long newTime = 0L;
	protected static long oldTime = DateTime.now().getMillis();

	protected final Logger logger = Logger.getLogger(getClass());
	protected final long millisecDelay;


	public Notification(long millisecDelay) {
		this.millisecDelay = millisecDelay;
	}

	@Override
	public final void run() {
		// If the previous tasks are not completed
		if (!IS_ACTIVE.compareAndSet(false, true)) {
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
		logger.debug("Start searching the events between " + oldTime + " and " + newTime);

		if (dataSnapshot != null) {
			processData(dataSnapshot);
		} else {
			logger.error("Recieved data is null");
		}
		IS_ACTIVE.set(false);
	}

	@Override
	public final void onCancelled(DatabaseError databaseError) {
		logger.error(databaseError.getMessage(), databaseError.toException());
		IS_ACTIVE.set(false);
	}


	protected abstract void processData(@NonNull DataSnapshot snapshot);

	protected abstract DatabaseReference getReference();


	@SuppressWarnings("OverlyComplexMethod")
	public static boolean isItTime(final long time, Event.EventType type) {

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
