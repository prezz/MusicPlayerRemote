package net.prezz.mpr.model.command;

import net.prezz.mpr.model.LibraryEntity;

public class AddToNewStoredPlaylistCommand implements Command {

    private String playlistName;
    private LibraryEntity[] entity;


    public AddToNewStoredPlaylistCommand(String playlistName, LibraryEntity entity) {
        this(playlistName, new LibraryEntity[] { entity });
    }

    public AddToNewStoredPlaylistCommand(String playlistName, LibraryEntity[] entities) {
        this.playlistName = playlistName;
        this.entity = entities;
    }

    public LibraryEntity[] getEntities() {
        return entity;
    }

    public String getPlaylistName() {
        return playlistName;
    }
}
