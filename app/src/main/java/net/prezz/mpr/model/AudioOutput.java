package net.prezz.mpr.model;

import net.prezz.mpr.Utils;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof AudioOutput) {
            AudioOutput other = (AudioOutput) obj;

            if (!Utils.equals(this.outputId, other.outputId)) {
                return false;
            }

            if (!Utils.equals(this.outputName, other.outputName)) {
                return false;
            }

            if (this.enabled != other.enabled) {
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = 31 * hash + Utils.hashCode(outputId);
        hash = 31 * hash + Utils.hashCode(outputName);
        hash = 31 * hash + Utils.hashCode(enabled);

        return hash;
    }
}
