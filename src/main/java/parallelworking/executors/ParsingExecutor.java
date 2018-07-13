package parallelworking.executors;

import com.google.firebase.database.DataSnapshot;
import models.Notification;
import parallelworking.Task;
import parallelworking.TasksExecutor;
import parallelworking.tasks.ParsingTask;

import java.util.Collection;
import java.util.concurrent.ExecutorService;


public class ParsingExecutor extends TasksExecutor<DataSnapshot> {

	private final DataSnapshot userNodes;
	private final Collection<Notification> notifications;

	public ParsingExecutor(ExecutorService service,
	                       DataSnapshot inputData,
	                       Collection<Notification> outputData) {
		super(service);
		this.userNodes = inputData;
		this.notifications = outputData;
	}

	@Override
	protected long getIterableSize() {
		return userNodes.getChildrenCount();
	}

	@Override
	protected Iterable<DataSnapshot> getIterable() {
		return userNodes.getChildren();
	}

	@Override
	protected Task getTask(DataSnapshot userNode) {
		return new ParsingTask(userNode, notifications);
	}
}
