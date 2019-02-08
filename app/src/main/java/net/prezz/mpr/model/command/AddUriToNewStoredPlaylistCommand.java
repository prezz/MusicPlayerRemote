package net.prezz.mpr.model.command;

import net.prezz.mpr.model.UriEntity;

public class AddUriToNewStoredPlaylistCommand implements Command {

	private String playlistName;
	private UriEntity[] entity;

	
	public AddUriToNewStoredPlaylistCommand(String playlistName, UriEntity entity) {
		this(playlistName, new UriEntity[] { entity });
	}
	
	public AddUriToNewStoredPlaylistCommand(String playlistName, UriEntity[] entities) {
		this.playlistName = playlistName;
		this.entity = entities;
	}

	public UriEntity[] getEntities() {
		return entity;
	}

	public String getPlaylistName() {
		return playlistName;
	}
}
