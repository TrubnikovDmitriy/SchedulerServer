package notifications;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public final class DatabaseHelper {

	static final String PRIVATE = "private";
	static final String PUBLIC = "public";
	static final String DASHBOARDS = "dashboards";
	static final String EVENTS = "events";
	static final String INFO = "info";
	static final String WATCHERS = "watchers";
	static final String TOKEN = "token";

	public static final String TITLE = "title";


	static DatabaseReference getPrivate() {
		return FirebaseDatabase.getInstance().getReference(PRIVATE);
	}

	
	public static DatabaseReference getPublicInfoList() {
		return FirebaseDatabase.getInstance().getReference()
				.child(PUBLIC)
				.child(DASHBOARDS)
				.child(INFO);
	}

	public static DatabaseReference getPublicInfo(final String dashID) {
		return FirebaseDatabase.getInstance().getReference()
				.child(PUBLIC)
				.child(DASHBOARDS)
				.child(INFO)
				.child(dashID);
	}

	public static DatabaseReference getPublicWatchers(final String dashID) {
		return FirebaseDatabase.getInstance().getReference()
				.child(PUBLIC)
				.child(DASHBOARDS)
				.child(WATCHERS)
				.child(dashID);
	}

	public static DatabaseReference getPublicEvents(final String dashID) {
		return FirebaseDatabase.getInstance().getReference()
				.child(PUBLIC)
				.child(DASHBOARDS)
				.child(EVENTS)
				.child(dashID);
	}



	public static DatabaseReference getPrivateInfoList(final String userUID) {
		return FirebaseDatabase.getInstance().getReference()
				.child(PRIVATE)
				.child(userUID)
				.child(DASHBOARDS)
				.child(INFO);
	}


	public static DatabaseReference getPrivateInfo(final String userUID,
	                                               final String dashID) {
		return FirebaseDatabase.getInstance().getReference()
				.child(PRIVATE)
				.child(userUID)
				.child(DASHBOARDS)
				.child(INFO)
				.child(dashID);

	}

	public static DatabaseReference getPrivateEvents(final String userUID,
	                                                 final String dashID) {
		return FirebaseDatabase.getInstance().getReference()
				.child(PRIVATE)
				.child(userUID)
				.child(DASHBOARDS)
				.child(EVENTS)
				.child(dashID);
	}



	public static String getPathToPrivateEvents(final String userUID,
	                                            final String dashID) {
		return PRIVATE + '/' +
				userUID + '/' +
				DASHBOARDS + '/' +
				EVENTS + '/' +
				dashID;
	}

	public static String getPathToPublicEvents(final String dashID) {
		return PUBLIC
				.concat("/").concat(DASHBOARDS)
				.concat("/").concat(EVENTS)
				.concat("/").concat(dashID);
	}
}
