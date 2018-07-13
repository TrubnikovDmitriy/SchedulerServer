package parallelworking.executors;

import models.TokenNotification;
import models.TopicNotification;
import parallelworking.Task;
import parallelworking.TasksExecutor;
import parallelworking.tasks.PrivateSendTask;
import parallelworking.tasks.PublicSendTask;

import java.util.Queue;
import java.util.concurrent.ExecutorService;


public class PublicSendExecutor extends TasksExecutor<TopicNotification> {

	private final Queue<TopicNotification> notifications;

	public PublicSendExecutor(ExecutorService service,
	                          Queue<TopicNotification> inputData) {
		super(service);
		this.notifications = inputData;
	}

	@Override
	protected long getSize() {
		return notifications.size();
	}

	@Override
	protected Iterable<TopicNotification> getIterable() {
		return notifications;
	}

	@Override
	protected Task<TopicNotification> getTask(TopicNotification notification) {
		return new PublicSendTask(notification);
	}
}
