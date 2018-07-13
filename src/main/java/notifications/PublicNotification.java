package notifications;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.internal.NonNull;
import models.TopicNotification;
import parallelworking.TasksExecutor;
import parallelworking.executors.PublicParseExecutor;
import parallelworking.executors.PublicSendExecutor;
import tools.DatabaseHelper;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PublicNotification extends Notification {

	private final int maxThread = 2 * Runtime.getRuntime().availableProcessors();
	private final ExecutorService executor = Executors.newFixedThreadPool(maxThread);


	public PublicNotification(long millisecDelay) {
		super(millisecDelay);
	}

	@Override
	protected void processData(@NonNull DataSnapshot eventNodes) {

		final ConcurrentLinkedQueue<TopicNotification> notifications = new ConcurrentLinkedQueue<>();

		final TasksExecutor parseExecutor = new PublicParseExecutor(executor, eventNodes, notifications, this);
		final TasksExecutor sendExecutor = new PublicSendExecutor(executor, notifications);

		try {
			parseExecutor.parallelExecute();
			sendExecutor.parallelExecute();

		} catch (InterruptedException e) {
			logger.error("Public data processing was interrupted", e);
			return;
		}

		logger.info("End retrieve public data from Firebase RD");
		oldTime = newTime;
	}

	@Override
	protected DatabaseReference getReference() {
		return DatabaseHelper.getPublicEventList();
	}
}
