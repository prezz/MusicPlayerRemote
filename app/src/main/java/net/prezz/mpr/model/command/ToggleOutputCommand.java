package net.prezz.mpr.model.command;

public class ToggleOutputCommand implements Command {

    private String outputId;
    private boolean enabled;

    public ToggleOutputCommand(String outputId, boolean enabled) {
        this.outputId = outputId;
        this.enabled = enabled;
    }

    public String getOutputId() {
        return outputId;
    }

    public boolean getEnabled() {
        return enabled;
    }
}
