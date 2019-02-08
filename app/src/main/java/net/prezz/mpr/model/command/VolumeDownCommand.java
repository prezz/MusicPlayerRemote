package net.prezz.mpr.model.command;

public class VolumeDownCommand implements Command {

	private final int amount;

	public VolumeDownCommand(int amount) {
		this.amount = amount;
	}

	public int getAmount() {
		return amount;
	}
}
