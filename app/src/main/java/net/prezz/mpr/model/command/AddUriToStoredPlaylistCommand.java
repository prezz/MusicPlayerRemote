package net.prezz.mpr.model.command;

import net.prezz.mpr.model.StoredPlaylistEntity;
import net.prezz.mpr.model.UriEntity;

public class AddUriToStoredPlaylistCommand implements Command {

	private StoredPlaylistEntity storedPlaylist;
	private UriEntity[] entity;

	
	public AddUriToStoredPlaylistCommand(StoredPlaylistEntity storedPlaylist, UriEntity entity) {
		this(storedPlaylist, new UriEntity[] { entity });
	}
	
	public AddUriToStoredPlaylistCommand(StoredPlaylistEntity storedPlaylist, UriEntity[] entities) {
		this.storedPlaylist = storedPlaylist;
		this.entity = entities;
	}

	public UriEntity[] getEntities() {
		return entity;
	}

	public StoredPlaylistEntity getStoredPlaylist() {
		return storedPlaylist;
	}
}
