package parallelworking;

import models.Event;

public interface DateChecker {
	boolean checkDate(final long time, Event.EventType type);
}
