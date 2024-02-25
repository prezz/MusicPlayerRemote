package net.prezz.mpr.ui.adapter;

import net.prezz.mpr.R;
import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.ui.ApplicationActivator;

public class ArtistAdapterEntity extends LibraryAdapterEntity {

    private static final long serialVersionUID = 8835914650261723361L;


    public ArtistAdapterEntity(LibraryEntity entity) {
        super(entity);
    }

    @Override
    public String getText() {
        LibraryEntity entity = getEntity();
        switch (entity.getTag()) {
        case ARTIST:
            return entity.getMetaArtist();
        case ALBUM_ARTIST:
            return entity.getMetaAlbumArtist();
        case COMPOSER:
            return entity.getComposer();
        default:
            return "";
        }
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

    @Override
    public String getTime() {
        return "";
    }

    @Override
    public String getData() {
        return "";
    }

    public String getEntityText() {
        LibraryEntity entity = getEntity();
        switch (entity.getTag()) {
            case ARTIST:
                return entity.getArtist();
            case ALBUM_ARTIST:
                return entity.getAlbumArtist();
            case COMPOSER:
                return entity.getComposer();
            default:
                return "";
        }
    }
}
