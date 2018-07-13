package parallelworking;

public abstract class Task<T> implements Runnable {

	protected OnTaskComplete completeListener;

	@Override
	public final void run() {
		work();
		if (completeListener != null) {
			completeListener.complete();
		}
	}

	protected abstract void work();

	void setCompleteListener(OnTaskComplete completeListener) {
		this.completeListener = completeListener;
	}
}
