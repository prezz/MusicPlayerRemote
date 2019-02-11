package net.prezz.mpr.model.command;

import net.prezz.mpr.model.StoredPlaylistEntity;

public class LoadStoredPlaylistCommand implements Command {

    private StoredPlaylistEntity entity;

    public LoadStoredPlaylistCommand(StoredPlaylistEntity entity) {
        this.entity = entity;
    }

    public StoredPlaylistEntity getEntity() {
        return entity;
    }
}
