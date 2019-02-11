package net.prezz.mpr.model.command;

import net.prezz.mpr.model.UriEntity;

public class AddUriToPlaylistCommand implements Command {

    private UriEntity[] entity;

    public AddUriToPlaylistCommand(UriEntity entity) {
        this.entity = new UriEntity[] { entity };
    }

    public AddUriToPlaylistCommand(UriEntity[] entities) {
        this.entity = entities;
    }

    public UriEntity[] getEntities() {
        return entity;
    }
}
