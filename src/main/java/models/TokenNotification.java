package models;

import java.util.LinkedList;
import java.util.List;


public class TokenNotification {

	private final String userUID;
	private final String token;
	private final List<Event> notification;


	public TokenNotification(String userUID, String token) {
		this.userUID = userUID;
		this.token = token;
		notification = new LinkedList<>();
	}

	public void addNotification(final Event event) {
		notification.add(event);
	}

	public String getUserUID() {
		return userUID;
	}

	public String getToken() {
		return token;
	}

	public List<Event> getNotification() {
		return notification;
	}
}
