package net.prezz.mpr.model.command;

public class PauseCommand implements Command {

	private boolean resume;

	public PauseCommand(boolean resume) {
		this.resume = resume;
	}
	
	public boolean getResume() {
		return resume;
	}
}
