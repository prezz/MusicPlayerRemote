package net.prezz.mpr.model.command;

public class PlayCommand implements Command {

	private Integer id;
	
	public PlayCommand() {
		this.id = null;
	}

	public PlayCommand(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}
}
