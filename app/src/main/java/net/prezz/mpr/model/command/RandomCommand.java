package net.prezz.mpr.model.command;

public class RandomCommand implements Command {

	private boolean random;

	public RandomCommand(boolean random) {
		this.random = random;
	}
	
	public boolean getRandom() {
		return random;
	}
}
