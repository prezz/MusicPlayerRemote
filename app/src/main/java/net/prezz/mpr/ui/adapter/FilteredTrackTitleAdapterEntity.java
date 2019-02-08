package net.prezz.mpr.ui.adapter;

import net.prezz.mpr.model.LibraryEntity;
import android.annotation.SuppressLint;

public class FilteredTrackTitleAdapterEntity extends LibraryAdapterEntity {

	private static final long serialVersionUID = 7762682442042782036L;

	
	public FilteredTrackTitleAdapterEntity(LibraryEntity entity) {
		super(entity);
	}
	
	@Override
	public String getSectionIndexText() {
		StringBuilder sb = new StringBuilder();

		Integer metaTrack = getEntity().getMetaTrack(); 
		if (metaTrack != null) {
			sb.append(metaTrack);
		}
		
		String artist = getEntity().getMetaArtist();
		if (artist != null) {
			sb.append(artist);
		}

		String title = getEntity().getTitle(); 
		if (title != null) {
			sb.append(title);
		}

		return sb.toString();
	}
	
	@SuppressLint("DefaultLocale")
	@Override
	public String getText() {
		Integer metaTrack = getEntity().getMetaTrack();
		return (metaTrack != null) ? String.format("%02d - %s", metaTrack, getEntity().getTitle()) : getEntity().getTitle();
	}

	@SuppressLint("DefaultLocale")
	@Override
	public String getSubText() {
		String artist = getEntity().getMetaArtist();
		return (artist != null) ? artist : "";
	}
	
	@SuppressLint("DefaultLocale")
	@Override
	public String getTime() {
		Integer metaLength = getEntity().getMetaLength();
		return (metaLength != null) ? String.format("%d:%02d", metaLength.intValue() / 60, metaLength.intValue() % 60) : "";
	}
}
