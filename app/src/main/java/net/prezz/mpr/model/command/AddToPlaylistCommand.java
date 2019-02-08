package net.prezz.mpr.model.command;

import net.prezz.mpr.model.LibraryEntity;

public class AddToPlaylistCommand implements Command {

	private LibraryEntity[] entity;

	public AddToPlaylistCommand(LibraryEntity entity) {
		this.entity = new LibraryEntity[] { entity };
	}
	
	public AddToPlaylistCommand(LibraryEntity[] entities) {
		this.entity = entities;
	}

	public LibraryEntity[] getEntities() {
		return entity;
	}
}
