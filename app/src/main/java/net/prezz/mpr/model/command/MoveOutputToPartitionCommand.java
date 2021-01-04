package net.prezz.mpr.model.command;

public class MoveOutputToPartitionCommand implements Command {

    private String outputName;
    private String partition;

    public MoveOutputToPartitionCommand(String outputName, String partition) {
        this.outputName = outputName;
        this.partition = partition;
    }

    public String getOutputName() {
        return outputName;
    }

    public String getPartition() {
        return partition;
    }
}
