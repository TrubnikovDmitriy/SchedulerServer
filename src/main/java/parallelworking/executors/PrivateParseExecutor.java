package parallelworking.executors;

import com.google.firebase.database.DataSnapshot;
import models.TokenNotification;
import parallelworking.Task;
import parallelworking.TasksExecutor;
import parallelworking.tasks.PrivateParseTask;

import java.util.Collection;
import java.util.concurrent.ExecutorService;


public class PrivateParseExecutor extends TasksExecutor<DataSnapshot> {

	private final DataSnapshot userNodes;
	private final Collection<TokenNotification> notifications;

	public PrivateParseExecutor(ExecutorService service,
	                            DataSnapshot inputData,
	                            Collection<TokenNotification> outputData) {
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
	protected Task<DataSnapshot> getTask(DataSnapshot userNode) {
		return new PrivateParseTask(userNode, notifications);
	}
}
