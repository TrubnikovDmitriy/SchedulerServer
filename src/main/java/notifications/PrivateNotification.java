package notifications;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.internal.NonNull;
import tools.DatabaseHelper;
import com.google.firebase.database.DataSnapshot;
import models.TokenNotification;
import parallelworking.TasksExecutor;
import parallelworking.executors.PrivateParseExecutor;
import parallelworking.executors.PrivateSendExecutor;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PrivateNotification extends Notification {

	private static final int THREAD_COUNT = 2 * Runtime.getRuntime().availableProcessors();
	private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(THREAD_COUNT);

	public PrivateNotification(long millisecDelay) {
		super(millisecDelay);
	}

	@Override
	protected void processData(@NonNull DataSnapshot userNodes) {

		final ConcurrentLinkedQueue<TokenNotification> notifications = new ConcurrentLinkedQueue<>();

		final TasksExecutor parseExecutor = new PrivateParseExecutor(EXECUTOR, userNodes, notifications);
		final TasksExecutor sendExecutor = new PrivateSendExecutor(EXECUTOR, notifications);

		try {
			parseExecutor.parallelExecute();
			sendExecutor.parallelExecute();

		} catch (InterruptedException e) {
			logger.error("Private data processing was interrupted", e);
			return;
		}

		logger.info("End retrieve private data from Firebase RD");
		oldTime = newTime;
	}

	@Override
	protected DatabaseReference getReference() {
		return DatabaseHelper.getPrivateUserList();
	}
}