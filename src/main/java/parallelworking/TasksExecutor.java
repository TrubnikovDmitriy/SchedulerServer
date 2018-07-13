package parallelworking;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

public abstract class TasksExecutor<T> {

	private final ExecutorService service;
	private final AtomicLong threadCounter = new AtomicLong();
	protected final Logger logger = Logger.getLogger(this.getClass());


	public TasksExecutor(ExecutorService service) {
		this.service = service;
	}


	public final void parallelExecute() throws InterruptedException {

		if (getSize() == 0) {
			logger.info("No tasks");
			return;
		}

		synchronized (threadCounter) {

			final long timeStart = DateTime.now().getMillis();
			logger.debug("Start execution tasks");


			threadCounter.set(getSize());
			final OnTaskComplete listener = () -> {
				final long remainTasks = threadCounter.decrementAndGet();
				// If all tasks are completed, then notify the main thread
				if (remainTasks == 0) {
					synchronized (threadCounter) {
						threadCounter.notify();
					}
				}
			};

			getIterable().forEach(e -> {
				final Task task = getTask(e);
				task.setCompleteListener(listener);
				service.execute(task);
			});

			// Wait until other threads finish working
			threadCounter.wait();


			final long executionTime = DateTime.now().getMillis() - timeStart;
			logger.info("Execution takes [" + executionTime + " ms] for [" + getSize() + " tasks]");
		}
	}


	protected abstract long getSize();

	protected abstract Iterable<T> getIterable();

	protected abstract Task<T> getTask(T entity);
}
