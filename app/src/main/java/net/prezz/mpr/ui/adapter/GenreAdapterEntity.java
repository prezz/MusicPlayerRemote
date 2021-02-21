package net.prezz.mpr.ui.adapter;

import net.prezz.mpr.R;
import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.ui.ApplicationActivator;

public class GenreAdapterEntity extends LibraryAdapterEntity {

    private static final long serialVersionUID = 2475536371192907039L;


    public GenreAdapterEntity(LibraryEntity entity) {
        super(entity);
    }

    @Override
    public String getText() {
        return getEntity().getGenre();
    }

    @Override
    public String getSubText() {
        StringBuilder sb = new StringBuilder();

        Integer metaCount = getEntity().getMetaCount();
        if (metaCount != null) {
            sb.append(metaCount);
            sb.append(" ");
            if (metaCount != null && metaCount.intValue() == 1) {
                sb.append(ApplicationActivator.getContext().getString(R.string.library_album_count));
            } else {
                sb.append(ApplicationActivator.getContext().getString(R.string.library_albums_count));
            }
        }

        return sb.toString();
    }

    @Override
    public String getTime() {
        return null;
    }
}
