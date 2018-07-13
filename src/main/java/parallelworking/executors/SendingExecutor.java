package parallelworking.executors;

import models.Notification;
import parallelworking.Task;
import parallelworking.TasksExecutor;
import parallelworking.tasks.SendingTask;

import java.util.Queue;
import java.util.concurrent.ExecutorService;


public class SendingExecutor extends TasksExecutor<Notification> {

	private final Queue<Notification> notifications;

	public SendingExecutor(ExecutorService service,
	                       Queue<Notification> inputData) {
		super(service);
		this.notifications = inputData;
	}

	@Override
	protected long getIterableSize() {
		return notifications.size();
	}

	@Override
	protected Iterable<Notification> getIterable() {
		return notifications;
	}

	@Override
	protected Task getTask(Notification notification) {
		return new SendingTask(notification);
	}
}
