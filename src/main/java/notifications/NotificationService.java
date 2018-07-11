package notifications;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class NotificationService {

	public static final long DELAY_BETWEEN_PRIVATE_RETRIEVE = 30_000L; // TimeUnit.MILLISECONDS

	public static void start() {
		final ScheduledExecutorService privateScheduler = Executors.newSingleThreadScheduledExecutor();
		privateScheduler.scheduleWithFixedDelay(
				new PrivateDataHandler(),
				0,
				DELAY_BETWEEN_PRIVATE_RETRIEVE,
				TimeUnit.MILLISECONDS
		);
	}
}
