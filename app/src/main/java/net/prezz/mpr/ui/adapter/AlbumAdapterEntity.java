package net.prezz.mpr.ui.adapter;

import android.annotation.SuppressLint;

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

    @SuppressLint("DefaultLocale")
    @Override
    public String getTime() {
        Integer metaLength = getEntity().getMetaLength();
        return (metaLength != null) ? String.format("%d:%02d", metaLength.intValue() / 60, metaLength.intValue() % 60) : "";
    }

    @Override
    public String getData() {
        Integer playedDaysAgo = getEntity().getPlayedDaysAgo();
        Integer playedCount = getEntity().getPlayedCount();
        return (playedDaysAgo != null && playedCount != null) ? String.format("%d days | %d", playedDaysAgo, playedCount) : "0";
    }
}
