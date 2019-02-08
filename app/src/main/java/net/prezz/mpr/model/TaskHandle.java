package net.prezz.mpr.model;

public interface TaskHandle {

	public static final TaskHandle NULL_HANDLE = new TaskHandle() {
		@Override
		public void cancelTask() {
		}
	};
	
	void cancelTask();
}
