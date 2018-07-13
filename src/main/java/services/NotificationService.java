package services;

import notifications.PrivateNotification;
import notifications.PublicNotification;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class NotificationService {

	public static final long DELAY_MILLISECONDS = 30_000;

	public static void start() {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

		scheduler.scheduleWithFixedDelay(
				new PrivateNotification(DELAY_MILLISECONDS),
				0,
				DELAY_MILLISECONDS,
				TimeUnit.MILLISECONDS
		);

		scheduler.scheduleWithFixedDelay(
				new PublicNotification(DELAY_MILLISECONDS),
				DELAY_MILLISECONDS / 2,
				DELAY_MILLISECONDS,
				TimeUnit.MILLISECONDS
		);
	}
}
