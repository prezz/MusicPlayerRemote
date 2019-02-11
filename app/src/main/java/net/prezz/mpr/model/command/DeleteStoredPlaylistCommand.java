package net.prezz.mpr.model.command;

import net.prezz.mpr.model.StoredPlaylistEntity;

public class DeleteStoredPlaylistCommand implements Command {

    private StoredPlaylistEntity entity;

    public DeleteStoredPlaylistCommand(StoredPlaylistEntity entity) {
        this.entity = entity;
    }

    public StoredPlaylistEntity getEntity() {
        return entity;
    }
}
