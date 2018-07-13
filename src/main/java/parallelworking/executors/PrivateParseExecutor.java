package parallelworking.executors;

import com.google.firebase.database.DataSnapshot;
import models.TokenNotification;
import parallelworking.DateChecker;
import parallelworking.Task;
import parallelworking.TasksExecutor;
import parallelworking.tasks.PrivateParseTask;

import java.util.Collection;
import java.util.concurrent.ExecutorService;


public class PrivateParseExecutor extends TasksExecutor<DataSnapshot> {

	private final DataSnapshot userNodes;
	private final Collection<TokenNotification> notifications;
	private final DateChecker dateChecker;

	public PrivateParseExecutor(ExecutorService service,
	                            DataSnapshot inputData,
	                            Collection<TokenNotification> outputData,
	                            DateChecker dateChecker) {
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
	protected Task<DataSnapshot> getTask(DataSnapshot userNode) {
		return new PrivateParseTask(userNode, notifications, dateChecker);
	}
}
