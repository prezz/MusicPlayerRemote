package net.prezz.mpr.model.command;

public class ConsumeCommand implements Command {

    private boolean consume;

    public ConsumeCommand(boolean consume) {
        this.consume = consume;
    }

    public boolean getConsume() {
        return consume;
    }
}
