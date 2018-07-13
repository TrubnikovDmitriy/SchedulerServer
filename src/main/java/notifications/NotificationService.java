package notifications;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class NotificationService {


	public static void start() {
		final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

		scheduler.scheduleWithFixedDelay(
				new PrivateNotification(),
				0,
				PrivateNotification.DELAY_MILLISECONDS,
				TimeUnit.MILLISECONDS
		);
	}
}
