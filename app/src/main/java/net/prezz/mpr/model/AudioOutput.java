package net.prezz.mpr.model;

public class AudioOutput {

    private String outputId;
    private String outputName;
    private boolean enabled;

    public AudioOutput(String outputId, String outputName, boolean enabled) {
        this.outputId = outputId;
        this.outputName = outputName;
        this.enabled = enabled;
    }

    public String getOutputId() {
        return outputId;
    }

    public String getOutputName() {
        return outputName;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
