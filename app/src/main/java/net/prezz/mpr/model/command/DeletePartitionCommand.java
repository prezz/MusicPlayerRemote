package net.prezz.mpr.model.command;

public class DeletePartitionCommand implements Command {

    private String name;

    public DeletePartitionCommand(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
