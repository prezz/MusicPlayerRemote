package net.prezz.mpr.ui.adapter;

import java.io.Serializable;

import net.prezz.mpr.model.StoredPlaylistEntity;

public class StoredPlaylistAdapterEntity implements Serializable {

	private static final long serialVersionUID = -2314273911954285555L;

	private StoredPlaylistEntity entity;
	
	
	public StoredPlaylistAdapterEntity(StoredPlaylistEntity entity) {
		this.entity = entity;
	}

	public StoredPlaylistEntity getEntity() {
		return entity;
	}
	
	@Override
	public String toString() {
		return entity.getPlaylistName();
	}
}
