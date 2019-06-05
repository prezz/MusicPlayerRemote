package net.prezz.mpr.mpd.command;

import android.database.Cursor;

import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.LibraryEntity.Builder;
import net.prezz.mpr.model.LibraryEntity.Tag;
import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MpdGetAllArtistsCommand extends MpdDatabaseCommand<MpdGetAllArtistsCommand.Param, LibraryEntity[]> {

    protected static final class Param {
        public final UriEntity uriEntity;
        public final Set<String> uriFilter;

        public Param(UriEntity uriEntity, Set<String> uriFilter) {
            this.uriEntity = uriEntity;
            this.uriFilter = (uriFilter != null) ? Collections.unmodifiableSet(uriFilter) : null;
        }
    }

    public MpdGetAllArtistsCommand(UriEntity uriEntity, Set<String> uriFilter) {
        super(new Param(uriEntity, uriFilter));
    }

    @Override
    protected LibraryEntity[] doExecute(MpdLibraryDatabaseHelper databaseHelper, Param param) throws Exception {
        Builder entityBuilder = LibraryEntity.createBuilder();

        UriEntity uriEntity = param.uriEntity;
        Set<String> uriFilter = param.uriFilter;

        List<LibraryEntity> libraryEntities = new ArrayList<LibraryEntity>();

        Cursor c = databaseHelper.selectAllArtists(uriEntity, uriFilter);
        try {
            if (c.moveToFirst()) {
                do {
                    String artist = c.getString(0);
                    String metaArtist = c.getString(1);
                    Integer metaCount = c.getInt(2);
                    libraryEntities.add(entityBuilder.clear().setTag(Tag.ARTIST).setArtist(artist).setUriEntity(uriEntity).setMetaArtist(metaArtist).setMetaCount(metaCount).setUriFilter(uriFilter).build());
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        c = databaseHelper.selectAllAlbumArtists(uriEntity, uriFilter);
        try {
            if (c.moveToFirst()) {
                do {
                    String albumArtist = c.getString(0);
                    String metaAlbumArtist = c.getString(1);
                    Integer metaCount = c.getInt(2);
                    libraryEntities.add(entityBuilder.clear().setTag(Tag.ALBUM_ARTIST).setAlbumArtist(albumArtist).setUriEntity(uriEntity).setMetaAlbumArtist(metaAlbumArtist).setMetaCount(metaCount).setUriFilter(uriFilter).build());
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        c = databaseHelper.selectAllComposers(uriEntity, uriFilter);
        try {
            if (c.moveToFirst()) {
                do {
                    String composer = c.getString(0);
                    Integer metaCount = c.getInt(1);
                    libraryEntities.add(entityBuilder.clear().setTag(Tag.COMPOSER).setComposer(composer).setUriEntity(uriEntity).setMetaCount(metaCount).setUriFilter(uriFilter).build());
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        return libraryEntities.toArray(new LibraryEntity[libraryEntities.size()]);
    }

    @Override
    protected LibraryEntity[] onError() {
        return new LibraryEntity[0];
    }
}
