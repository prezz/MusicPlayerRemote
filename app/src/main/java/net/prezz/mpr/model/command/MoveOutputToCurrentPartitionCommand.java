package net.prezz.mpr.model.command;

import net.prezz.mpr.model.AudioOutput;

public class MoveOutputToCurrentPartitionCommand implements Command {

    private AudioOutput output;

    public MoveOutputToCurrentPartitionCommand(AudioOutput output) {
        this.output = output;
    }

    public AudioOutput getAudioOutput() {
        return output;
    }
}
