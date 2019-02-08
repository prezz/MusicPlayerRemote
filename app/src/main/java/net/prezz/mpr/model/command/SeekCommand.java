package net.prezz.mpr.model.command;

public class SeekCommand implements Command {

	private int id;
	private int position;

	
	public SeekCommand(int id, int position) {
		this.id = id;
		this.position = position;
	}

	public int getId() {
		return id;
	}
	
	public int getPosition() {
		return position;
	}
}
