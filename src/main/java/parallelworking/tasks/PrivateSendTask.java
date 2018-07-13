package parallelworking.tasks;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.gson.Gson;
import models.Event;
import models.TokenNotification;
import org.apache.log4j.Logger;
import parallelworking.Task;
import tools.DatabaseHelper;

public class PrivateSendTask extends Task<TokenNotification> {

	private final TokenNotification notification;

	public PrivateSendTask(TokenNotification notification) {
		this.notification = notification;
	}

	@Override
	protected void work() {
		final Logger logger = Logger.getLogger(Thread.currentThread().getName());
		logger.debug("Sending [" + notification.getNotification().size() +
				" message(s)] for user " + notification.getUserUID());

		final Gson gson = new Gson();

		for (final Event event : notification.getNotification()) {
			// Build message
			final Message message = Message.builder()
					.setToken(notification.getToken())
					.putData(DatabaseHelper.PRIVATE, gson.toJson(event))
					.putData(DatabaseHelper.USER_UID, notification.getUserUID())
					.build();

			// Send message
			try {
				final String response = FirebaseMessaging.getInstance().send(message);
				logger.debug("Successfully sent message to user: " + response);

			} catch (FirebaseMessagingException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
