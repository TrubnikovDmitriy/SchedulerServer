package parallelworking.executors;

import com.google.firebase.database.DataSnapshot;
import models.TokenNotification;
import models.TopicNotification;
import parallelworking.Task;
import parallelworking.TasksExecutor;
import parallelworking.tasks.PrivateParseTask;
import parallelworking.tasks.PublicParseTask;

import java.util.Collection;
import java.util.concurrent.ExecutorService;


public class PublicParseExecutor extends TasksExecutor<DataSnapshot> {

	private final DataSnapshot userNodes;
	private final Collection<TopicNotification> notifications;

	public PublicParseExecutor(ExecutorService service,
	                           DataSnapshot inputData,
	                           Collection<TopicNotification> outputData) {
		super(service);
		this.userNodes = inputData;
		this.notifications = outputData;
	}

	@Override
	protected long getSize() {
		return userNodes.getChildrenCount();
	}

	@Override
	protected Iterable<DataSnapshot> getIterable() {
		return userNodes.getChildren();
	}

	@Override
	protected Task<DataSnapshot> getTask(DataSnapshot dashboardNode) {
		return new PublicParseTask(dashboardNode, notifications);
	}
}
