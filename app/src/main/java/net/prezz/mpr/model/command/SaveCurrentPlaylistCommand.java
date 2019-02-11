package net.prezz.mpr.model.command;


public class SaveCurrentPlaylistCommand implements Command {

    private String name;

    public SaveCurrentPlaylistCommand(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
