package tools;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public final class DatabaseHelper {

	public static final String PRIVATE = "private";
	public static final String PUBLIC = "public";
	public static final String DASHBOARDS = "dashboards";
	public static final String EVENTS = "events";
	public static final String INFO = "info";
	public static final String WATCHERS = "watchers";
	public static final String TOKEN = "token";

	public static final String TIME = "timestamp";
	public static final String TITLE = "title";
	public static final String TYPE = "type";
	public static final String TEXT = "text";


	public static DatabaseReference getPrivateUserList() {
		return FirebaseDatabase
				.getInstance()
				.getReference(PRIVATE);
	}

	public static DatabaseReference getPublicEventList() {
		return FirebaseDatabase
				.getInstance()
				.getReference()
				.child(PUBLIC)
				.child(DASHBOARDS)
				.child(EVENTS);
	}
}
