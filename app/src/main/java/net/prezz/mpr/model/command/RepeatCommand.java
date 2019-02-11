package net.prezz.mpr.model.command;

public class RepeatCommand implements Command {

    private boolean repeat;

    public RepeatCommand(boolean random) {
        this.repeat = random;
    }

    public boolean getRepeat() {
        return repeat;
    }
}
