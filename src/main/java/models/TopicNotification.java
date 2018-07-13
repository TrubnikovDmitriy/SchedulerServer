package models;

import java.util.LinkedList;
import java.util.List;


public class TopicNotification {

	private final String topic;
	private final List<Event> notification;


	public TopicNotification(String topic) {
		this.topic = topic;
		this.notification = new LinkedList<>();
	}

	public void addNotification(final Event event) {
		notification.add(event);
	}

	public String getTopic() {
		return topic;
	}

	public List<Event> getNotification() {
		return notification;
	}
}
