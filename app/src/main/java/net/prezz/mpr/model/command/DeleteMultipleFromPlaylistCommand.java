package net.prezz.mpr.model.command;

public class DeleteMultipleFromPlaylistCommand implements Command {

    private Integer[] identifiers;

    public DeleteMultipleFromPlaylistCommand(Integer[] identifiers) {
        this.identifiers = identifiers;
    }

    public Integer[] getIdentifiers() {
        return identifiers;
    }
}
