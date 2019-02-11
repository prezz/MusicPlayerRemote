package net.prezz.mpr.ui.adapter;

import net.prezz.mpr.model.LibraryEntity;
import android.annotation.SuppressLint;

public class SearchTitleAdapterEntity extends LibraryAdapterEntity {

    private static final long serialVersionUID = 5519388736500542177L;


    public SearchTitleAdapterEntity(LibraryEntity entity) {
        super(entity);
    }

    @Override
    public String getSectionIndexText() {
        StringBuilder sb = new StringBuilder();

        String title = getEntity().getTitle();
        if (title != null) {
            sb.append(title);
        }

        String artist = getEntity().getArtist();
        if (artist != null) {
            sb.append(artist);
        }

        return sb.toString();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String getText() {
        return getEntity().getTitle();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String getSubText() {
        String artist = getEntity().getArtist();
        return (artist != null) ? artist : "";
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String getTime() {
        return "";
    }
}
