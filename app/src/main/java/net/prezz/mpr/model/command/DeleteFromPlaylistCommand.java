package net.prezz.mpr.model.command;

public class DeleteFromPlaylistCommand implements Command {

    private int pos;

    public DeleteFromPlaylistCommand(int pos) {
        this.pos = pos;
    }

    public int getPos() {
        return pos;
    }
}
