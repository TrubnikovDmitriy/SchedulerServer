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


	public static DatabaseReference getPrivate() {
		return FirebaseDatabase
				.getInstance()
				.getReference(PRIVATE);
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
