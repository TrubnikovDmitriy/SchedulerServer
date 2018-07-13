package parallelworking.executors;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.internal.NonNull;
import models.TopicNotification;
import parallelworking.Task;
import parallelworking.TasksExecutor;
import parallelworking.DateChecker;
import parallelworking.tasks.PublicParseTask;

import java.util.Collection;
import java.util.concurrent.ExecutorService;


public class PublicParseExecutor extends TasksExecutor<DataSnapshot> {

	private final DataSnapshot userNodes;
	private final Collection<TopicNotification> notifications;
	private final DateChecker dateChecker;

	public PublicParseExecutor(@NonNull ExecutorService service,
	                           @NonNull DataSnapshot inputData,
	                           @NonNull Collection<TopicNotification> outputData,
	                           @NonNull DateChecker dateChecker) {
		super(service);
		this.userNodes = inputData;
		this.notifications = outputData;
		this.dateChecker = dateChecker;
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
		return new PublicParseTask(dashboardNode, notifications, dateChecker);
	}
}
