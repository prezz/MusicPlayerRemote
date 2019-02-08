package net.prezz.mpr.model.command;

import net.prezz.mpr.model.StoredPlaylistEntity;

public class MoveInStoredPlaylistCommand implements Command {

	private StoredPlaylistEntity storedPlaylist;
	private int from;
	private int to;

	public MoveInStoredPlaylistCommand(StoredPlaylistEntity storedPlaylist, int from, int to) {
		this.storedPlaylist = storedPlaylist;
		this.from = from;
		this.to = to;
	}

	public StoredPlaylistEntity getStoredPlaylist() {
		return storedPlaylist;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}
}
