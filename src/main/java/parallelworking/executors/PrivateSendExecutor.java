package parallelworking.executors;

import models.TokenNotification;
import parallelworking.Task;
import parallelworking.TasksExecutor;
import parallelworking.tasks.PrivateSendTask;

import java.util.Queue;
import java.util.concurrent.ExecutorService;


public class PrivateSendExecutor extends TasksExecutor<TokenNotification> {

	private final Queue<TokenNotification> notifications;

	public PrivateSendExecutor(ExecutorService service,
	                           Queue<TokenNotification> inputData) {
		super(service);
		this.notifications = inputData;
	}

	@Override
	protected long getSize() {
		return notifications.size();
	}

	@Override
	protected Iterable<TokenNotification> getIterable() {
		return notifications;
	}

	@Override
	protected Task<TokenNotification> getTask(TokenNotification notification) {
		return new PrivateSendTask(notification);
	}
}
