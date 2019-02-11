package net.prezz.mpr.ui.adapter;

import android.annotation.SuppressLint;
import net.prezz.mpr.R;
import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.ui.ApplicationActivator;

public class FilteredAlbumAdapterEntity extends LibraryAdapterEntity {

    private static final long serialVersionUID = 3344502606040445346L;


    public FilteredAlbumAdapterEntity(LibraryEntity entity) {
        super(entity);
    }

    @Override
    public String getText() {
        return getEntity().getMetaAlbum();
    }

    @Override
    public String getSubText() {
        StringBuilder sb = new StringBuilder();

        Integer metaCount = getEntity().getMetaCount();
        if (metaCount != null) {
            sb.append(metaCount);
            sb.append(" ");
            if (metaCount != null && metaCount.intValue() == 1) {
                sb.append(ApplicationActivator.getContext().getString(R.string.library_track_count));
            } else {
                sb.append(ApplicationActivator.getContext().getString(R.string.library_tracks_count));
            }
        }

        return sb.toString();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String getTime() {
        Integer metaLength = getEntity().getMetaLength();
        return (metaLength != null) ? String.format("%d:%02d", metaLength.intValue() / 60, metaLength.intValue() % 60) : "";
    }
}
