package notifications;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.apache.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class NotificationService {

	public static void start() {
		final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(new TestSendMessage(), 0, 30, TimeUnit.SECONDS);
	}

	private static class TestSendMessage implements Runnable {
		@Override
		public void run() {
			final Logger logger = Logger.getLogger(Thread.currentThread().getName());
			final String testToken = "dLgkDO_-UcI:APA91bGhBLZqosJ96ztwMp1QjrcOCejWVgbLfDoYu0LWp2ZYG81SMfb0ANxhObtgsJkhNkkpz9BiF2IMbxwf9tVdFCz9LXtZ7om2Yh-jUODv3-g9yQttltePpT9oIOFZDtDTqdR5lug0YdCcON-O2GIi2Vc_oOwvrg";

			logger.info("Start sending test message");
			final Message message = Message.builder()
					.putData("Test", "data")
					.setToken(testToken)
					.build();

			try {
				final String response = FirebaseMessaging.getInstance().send(message);
				logger.info("Successfully sent message: " + response);

			} catch (FirebaseMessagingException e) {
				logger.error("Failed to send message", e);
			}
		}
	}
}
