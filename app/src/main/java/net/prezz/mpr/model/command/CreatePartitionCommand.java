package net.prezz.mpr.model.command;

public class CreatePartitionCommand implements Command {

    private String name;

    public CreatePartitionCommand(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
