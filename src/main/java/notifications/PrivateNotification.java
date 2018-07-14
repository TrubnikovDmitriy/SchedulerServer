package notifications;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.internal.NonNull;
import tools.DatabaseHelper;
import com.google.firebase.database.DataSnapshot;
import models.TokenNotification;
import parallelworking.TasksExecutor;
import parallelworking.executors.PrivateParseExecutor;
import parallelworking.executors.PrivateSendExecutor;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


public class PrivateNotification extends Notification {

	private final ExecutorService executor;

	public PrivateNotification(long millisecDelay) {
		super(millisecDelay);

		final ThreadFactory threadFactory = new ThreadFactoryBuilder()
				.setNameFormat("private-task-%d")
				.build();
		final int maxThread = 2 * Runtime.getRuntime().availableProcessors();

		executor = Executors.newFixedThreadPool(maxThread, threadFactory);
	}

	@Override
	protected Queue<TasksExecutor> getExecutors(@NonNull DataSnapshot inputData) {

		final ConcurrentLinkedQueue<TokenNotification> notifications = new ConcurrentLinkedQueue<>();

		final TasksExecutor parseExecutor = new PrivateParseExecutor(executor, inputData, notifications, this);
		final TasksExecutor sendExecutor = new PrivateSendExecutor(executor, notifications);

		final LinkedList<TasksExecutor> list = new LinkedList<>();
		list.add(parseExecutor);
		list.add(sendExecutor);
		return list;
	}

	@Override
	protected DatabaseReference getReference() {
		return DatabaseHelper.getPrivateUserList();
	}
}
