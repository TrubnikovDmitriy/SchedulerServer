package parallelworking.tasks;

import com.google.firebase.database.DataSnapshot;
import models.EventModel;
import models.Notification;
import tools.DatabaseHelper;
import notifications.PrivateNotification;
import org.apache.log4j.Logger;
import parallelworking.Task;

import java.util.Collection;


public class ParsingTask extends Task {

	private final Collection<Notification> queue;
	private final DataSnapshot userNode;

	public ParsingTask(DataSnapshot userNode, Collection<Notification> queue) {
		this.queue = queue;
		this.userNode = userNode;
	}

	@Override
	protected void work() {
		final Logger logger = Logger.getLogger(Thread.currentThread().getName());
		logger.debug("Start parse user's node");

		final String userUID = userNode
				.getKey();

		final String token = userNode
				.child(DatabaseHelper.TOKEN)
				.getValue(String.class);

		if (token == null) {
			logger.error("Token of user <" + userUID + "> is not defined");
			return;
		}

		final DataSnapshot events = userNode
				.child(DatabaseHelper.DASHBOARDS)
				.child(DatabaseHelper.EVENTS);

		if (events == null) {
			logger.debug("User hasn't dashboards");
			return;
		}

		final Notification notification = new Notification(userUID, token);

		logger.debug("User <" + userUID + "> has " + events.getChildrenCount() + " dashboards");
		for (DataSnapshot dashboardNode : events.getChildren()) {

			logger.debug("Dashboard <" + dashboardNode.getKey() + "> has " + dashboardNode.getChildrenCount() + " events");
			for (DataSnapshot eventNode : dashboardNode.getChildren()) {

				final EventModel event = eventNode.getValue(EventModel.class);
				if (event == null || event.isInvalid()) {
					logger.error("Event <" + eventNode.getKey() + "> in dashboard <" + dashboardNode.getKey() + "> is invalid");
					continue;
				}

				logger.debug("Check event <" + eventNode.getKey() + '>');

				if (!PrivateNotification.isItTime(event.getTimestamp(), event.getType())) {
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
	}
}
