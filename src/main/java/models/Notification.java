package models;

import java.util.LinkedList;
import java.util.List;


public class Notification {

	private final String userUID;
	private final String token;
	private final List<EventModel> notification;


	public Notification(String userUID, String token) {
		this.userUID = userUID;
		this.token = token;
		notification = new LinkedList<>();
	}

	public void addNotification(final EventModel eventModel) {
		notification.add(eventModel);
	}

	public String getUserUID() {
		return userUID;
	}

	public String getToken() {
		return token;
	}

	public List<EventModel> getNotification() {
		return notification;
	}
}
