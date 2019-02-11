package net.prezz.mpr.model.command;

import net.prezz.mpr.model.UriEntity;

public class PrioritizeUriCommand implements Command {

    private UriEntity[] entity;

    public PrioritizeUriCommand(UriEntity entity) {
        this.entity = new UriEntity[] { entity };
    }

    public PrioritizeUriCommand(UriEntity[] entities) {
        this.entity = entities;
    }

    public UriEntity[] getEntities() {
        return entity;
    }
}
