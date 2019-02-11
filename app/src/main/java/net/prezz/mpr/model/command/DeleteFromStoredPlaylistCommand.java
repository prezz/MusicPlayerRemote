package net.prezz.mpr.model.command;

import net.prezz.mpr.model.StoredPlaylistEntity;

public class DeleteFromStoredPlaylistCommand implements Command {

    private StoredPlaylistEntity storedPlaylist;
    private int pos;

    public DeleteFromStoredPlaylistCommand(StoredPlaylistEntity storedPlaylist, int pos) {
        this.storedPlaylist = storedPlaylist;
        this.pos = pos;
    }

    public StoredPlaylistEntity getStoredPlaylist() {
        return storedPlaylist;
    }

    public int getPos() {
        return pos;
    }
}
