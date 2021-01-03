package net.prezz.mpr.model.command;

import net.prezz.mpr.model.AudioOutput;

public class MoveOutputToPartitionCommand implements Command {

    private AudioOutput output;
    private String partition;

    public MoveOutputToPartitionCommand(AudioOutput output, String partition) {
        this.output = output;
        this.partition = partition;
    }

    public AudioOutput getAudioOutput() {
        return output;
    }

    public String getPartition() {
        return partition;
    }
}
