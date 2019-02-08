package net.prezz.mpr.ui.adapter;

import net.prezz.mpr.model.LibraryEntity;

public class AlbumAdapterEntity extends LibraryAdapterEntity {

	private static final long serialVersionUID = -8801955200935156385L;
	
	private boolean sortByArtist;

	
	public AlbumAdapterEntity(LibraryEntity entity, boolean sortByArtist) {
		super(entity);
		this.sortByArtist = sortByArtist;
	}
	
	@Override
	public String getSectionIndexText() {
		return (sortByArtist) ? getSubText() : getText();
	}
	
	@Override
	public String getText() {
        String album = (sortByArtist) ? getEntity().getAlbum() : getEntity().getMetaAlbum();
        return (album != null) ? album : "";
	}

	@Override
	public String getSubText() {
		String artist = (sortByArtist) ? getEntity().getMetaArtist() : getEntity().getLookupArtist();
		return (artist != null) ? artist : "";
	}

	@Override
	public String getTime() {
		return null;
	}
}
