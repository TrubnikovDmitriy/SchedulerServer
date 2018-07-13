package parallelworking.tasks;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import models.EventModel;
import models.Notification;
import org.apache.log4j.Logger;
import parallelworking.Task;

public class SendingTask extends Task {

	private final Notification notification;

	public SendingTask(Notification notification) {
		this.notification = notification;
	}

	@Override
	protected void work() {
		final Logger logger = Logger.getLogger(Thread.currentThread().getName());
		logger.debug("Sending [" + notification.getNotification().size() +
				" message(s)] for user " + notification.getUserUID());

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
	}
}
