package net.prezz.mpr.model.command;

public class MoveInPlaylistCommand implements Command {

	private int id;
	private int to;

	public MoveInPlaylistCommand(int id, int to) {
		this.id = id;
		this.to = to;
	}

	public int getId() {
		return id;
	}

	public int getTo() {
		return to;
	}
}
