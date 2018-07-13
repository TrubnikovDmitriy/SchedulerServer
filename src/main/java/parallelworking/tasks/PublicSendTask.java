package parallelworking.tasks;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import models.Event;
import models.TokenNotification;
import models.TopicNotification;
import org.apache.log4j.Logger;
import parallelworking.Task;

public class PublicSendTask extends Task<TopicNotification> {

	private final TopicNotification notification;

	public PublicSendTask(TopicNotification notification) {
		this.notification = notification;
	}

	@Override
	protected void work() {
		final Logger logger = Logger.getLogger(Thread.currentThread().getName());
		logger.debug("Sending [" + notification.getNotification().size() +
				" message(s)] for topic " + notification.getTopic());

		for (final Event event : notification.getNotification()) {
			// Build message
			final Message message = Message.builder()
					.setTopic(notification.getTopic())
					.putAllData(event.toMap())
					.build();

			// Send message
			try {
				final String response = FirebaseMessaging.getInstance().send(message);
				logger.debug("Successfully sent message to topic: " + response);

			} catch (FirebaseMessagingException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
