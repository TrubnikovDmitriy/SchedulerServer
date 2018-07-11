package notifications;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.*;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class NotificationService {

	public static final long DELAY_BETWEEN_PRIVATE_RETRIEVE = 10_000L; // millisec

	public static final AtomicBoolean isPrivateDataProcess = new AtomicBoolean(false);

	private static Long timeRetrievePrivate = 0L;
	private static long timeRetrievePublic = 0;


	public static void start() {
		final ScheduledExecutorService privateScheduler = Executors.newSingleThreadScheduledExecutor();
		privateScheduler.scheduleWithFixedDelay(
				new RetrievePrivateData(),
				0,
				DELAY_BETWEEN_PRIVATE_RETRIEVE,
				TimeUnit.MILLISECONDS
		);
	}


	private static class RetrievePrivateData implements Runnable {

		@Override
		public void run() {

			final Logger logger = Logger.getLogger("Private Snapshot");
			logger.info("START retrieve private data from Firebase RD");

			final Long startRetrieve = getTimeNow();

			// Java Firebase SDK hasn't synchronous data retrieving,
			// so we have to do blocking reading artificially.
			synchronized (startRetrieve) {

				final DatabaseReference privateRef = DatabaseHelper.getPrivate();
				privateRef.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(DataSnapshot dataSnapshot) {
						logger.info("Get dataSnapshot");
						synchronized (startRetrieve) {
							logger.info("Notify scheduler's thread");
							startRetrieve.notify();
						}
					}

					@Override
					public void onCancelled(DatabaseError databaseError) {
						logger.error(databaseError.getMessage(), databaseError.toException());
						synchronized (startRetrieve) {
							logger.info("Notify scheduler's thread");
							startRetrieve.notify();
						}
					}
				});

				// This is necassary to avoid duplicate notification
				// and to avoid launch several task simultaneously.
				try {
					startRetrieve.wait();
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				} finally {
					logger.info("END retrieve private data from Firebase RD");

					final Long retrieveDuration = getTimeNow() - startRetrieve;
					if (retrieveDuration < DELAY_BETWEEN_PRIVATE_RETRIEVE) {
						logger.info("Blind zone for about " + retrieveDuration + " ms");
					} else {
						logger.error("Too long retrieve: " + retrieveDuration + " ms");
					}
				}
			}
		}
	}

	private static long getTimeNow() {
		return Calendar.getInstance().getTimeInMillis();
	}
}
