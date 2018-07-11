package models;

import java.util.HashMap;
import java.util.Map;


public class EventModel {

	private String eventID;
	private String dashID;
	private String text;
	private Long timestamp;
	private String title;
	private EventType type;
	private Priority priority;

	public enum EventType {
		ONE_TIME,
		EVERY_DAY,
		EVERY_WEEK,
		EVERY_MONTH,
		EVERY_YEAR,
		EVERY_CENTURY
	}

	public enum Priority {
		LOW,
		MEDIUM,
		HIGH,
		ULTRA_HIGH
	}


	public EventModel() { }

	public void setEventID(String eventID) {
		this.eventID = eventID;
	}

	public void setDashID(String dashID) {
		this.dashID = dashID;
	}

	public String getEventID() {
		return eventID;
	}

	public String getDashID() {
		return dashID;
	}

	public String getText() {
		return text;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public String getTitle() {
		return title;
	}

	public EventType getType() {
		return type;
	}

	public Priority getPriority() {
		return priority;
	}

	public boolean isInvalid() {
		return (timestamp == null || type == null || title == null);
	}

	public Map<String, String> toMap() {
		final HashMap<String, String> firebaseModel = new HashMap<>();
		firebaseModel.put("text", text);
		firebaseModel.put("title", title);
		firebaseModel.put("timestamp", timestamp.toString());
		firebaseModel.put("type", type.name());
		firebaseModel.put("priority", priority.name());
		firebaseModel.put("eventID", eventID);
		firebaseModel.put("dashID", dashID);
		return firebaseModel;
	}
}
