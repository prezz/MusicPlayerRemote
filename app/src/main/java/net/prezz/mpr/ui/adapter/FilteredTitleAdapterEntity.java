package net.prezz.mpr.ui.adapter;

import net.prezz.mpr.model.LibraryEntity;
import android.annotation.SuppressLint;

public class FilteredTitleAdapterEntity extends LibraryAdapterEntity {

    private static final long serialVersionUID = -8867175839235949379L;


    public FilteredTitleAdapterEntity(LibraryEntity entity) {
        super(entity);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String getText() {
        return getEntity().getTitle();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String getSubText() {
        StringBuilder sb = new StringBuilder();

        String album = getEntity().getAlbum();
        if (album != null && !album.isEmpty()) {
            sb.append(album);
        }

        Integer year = getEntity().getMetaYear();
        if (year != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(year);
        }

        String genre = getEntity().getMetaGenre();
        if (genre != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(genre);
        }

        return sb.toString();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String getTime() {
        Integer metaLength = getEntity().getMetaLength();
        return (metaLength != null) ? String.format("%d:%02d", metaLength.intValue() / 60, metaLength.intValue() % 60) : "";
    }

    @Override
    public String getData() {
        return "";
    }
}
