package notifications;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.internal.NonNull;
import models.TopicNotification;
import parallelworking.TasksExecutor;
import parallelworking.executors.PublicParseExecutor;
import parallelworking.executors.PublicSendExecutor;
import tools.DatabaseHelper;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


public class PublicNotification extends Notification {

	private final ExecutorService executor;

	public PublicNotification(long millisecDelay) {
		super(millisecDelay);

		final ThreadFactory threadFactory = new ThreadFactoryBuilder()
				.setNameFormat("public-task-%d")
				.build();
		final int maxThread = 2 * Runtime.getRuntime().availableProcessors();

		executor = Executors.newFixedThreadPool(maxThread, threadFactory);
	}

	@Override
	protected Queue<TasksExecutor> getExecutors(@NonNull DataSnapshot inputData) {

		final ConcurrentLinkedQueue<TopicNotification> notifications = new ConcurrentLinkedQueue<>();

		final TasksExecutor parseExecutor = new PublicParseExecutor(executor, inputData, notifications, this);
		final TasksExecutor sendExecutor = new PublicSendExecutor(executor, notifications);

		final LinkedList<TasksExecutor> list = new LinkedList<>();
		list.add(parseExecutor);
		list.add(sendExecutor);
		return list;
	}

	@Override
	protected DatabaseReference getReference() {
		return DatabaseHelper.getPublicEventList();
	}
}
