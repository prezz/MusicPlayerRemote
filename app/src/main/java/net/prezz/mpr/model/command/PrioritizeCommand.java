package net.prezz.mpr.model.command;

import net.prezz.mpr.model.LibraryEntity;

public class PrioritizeCommand implements Command {

	private LibraryEntity[] entity;

	public PrioritizeCommand(LibraryEntity entity) {
		this.entity = new LibraryEntity[] { entity };
	}
	
	public PrioritizeCommand(LibraryEntity[] entities) {
		this.entity = entities;
	}

	public LibraryEntity[] getEntities() {
		return entity;
	}
}
