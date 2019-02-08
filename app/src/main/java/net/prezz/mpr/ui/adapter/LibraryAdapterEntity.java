package net.prezz.mpr.ui.adapter;

import net.prezz.mpr.model.LibraryEntity;

public abstract class LibraryAdapterEntity implements AdapterEntity {

	private static final long serialVersionUID = -2010311452515165465L;

	private LibraryEntity entity;

	
	@Override
	public String getSectionIndexText() {
		return getText();
	}
	
	public LibraryAdapterEntity(LibraryEntity entity) {
		this.entity = entity;
	}
	
	public LibraryEntity getEntity() {
		return entity;
	}
	
	public abstract String getSubText();
	
	public abstract String getTime();
}
