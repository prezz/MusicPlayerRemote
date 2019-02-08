package net.prezz.mpr.model.command;

import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.StoredPlaylistEntity;

public class AddToStoredPlaylistCommand implements Command {

	private StoredPlaylistEntity storedPlaylist;
	private LibraryEntity[] entity;

	
	public AddToStoredPlaylistCommand(StoredPlaylistEntity storedPlaylist, LibraryEntity entity) {
		this(storedPlaylist, new LibraryEntity[] { entity });
	}
	
	public AddToStoredPlaylistCommand(StoredPlaylistEntity storedPlaylist, LibraryEntity[] entities) {
		this.storedPlaylist = storedPlaylist;
		this.entity = entities;
	}

	public LibraryEntity[] getEntities() {
		return entity;
	}

	public StoredPlaylistEntity getStoredPlaylist() {
		return storedPlaylist;
	}
}
