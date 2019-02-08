package net.prezz.mpr.model.command;

public class VolumeUpCommand implements Command {

	private final int amount;

	public VolumeUpCommand(int amount) {
		this.amount = amount;
	}

	public int getAmount() {
		return amount;
	}
}
