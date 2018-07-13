package parallelworking.tasks;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.internal.NonNull;
import models.Event;
import models.TopicNotification;
import notifications.Notification;
import notifications.PrivateNotification;
import org.apache.log4j.Logger;
import parallelworking.Task;

import java.util.Collection;


public class PublicParseTask extends Task<DataSnapshot> {

	private final Collection<TopicNotification> queue;
	private final DataSnapshot eventsNode;

	public PublicParseTask(@NonNull DataSnapshot eventsNode,
	                       Collection<TopicNotification> queue) {
		this.queue = queue;
		this.eventsNode = eventsNode;
	}

	@Override
	protected void work() {
		final Logger logger = Logger.getLogger(Thread.currentThread().getName());
		logger.debug("Start parse event's node");

		final String topic = eventsNode.getKey();
		final TopicNotification notification = new TopicNotification(topic);

		logger.debug("Public dashboard <" + topic + "> has " + eventsNode.getChildrenCount() + " events");
		for (DataSnapshot eventData : eventsNode.getChildren()) {

			final String eventID = eventData.getKey();
			final Event event = eventData.getValue(Event.class);
			if (event == null || event.isInvalid()) {
				logger.error("Event <" + eventID + "> in dashboard <" + topic + "> is invalid");
				continue;
			}

			logger.debug("Check event <" + eventID + '>');
			if (!Notification.isItTime(event.getTimestamp(), event.getType())) {
				continue;
			}
			logger.debug("The time of event <" + eventID + "> has come");

			event.setDashID(topic);
			event.setEventID(eventID);

			notification.addNotification(event);
		}

		if (!notification.getNotification().isEmpty()) {
			logger.debug("Add non-empty public notification to queue");
			queue.add(notification);
		}
		logger.debug("End parse event's node");
	}
}
