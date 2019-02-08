package net.prezz.mpr.model.command;

public class UnprioritizeCommand implements Command {

	private int from;
	private int to;
	
	public UnprioritizeCommand(int from, int to) {
		this.from = from;
		this.to = to;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}
}
