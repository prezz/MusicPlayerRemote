package net.prezz.mpr.mpd.command;

import android.database.Cursor;

import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.LibraryEntity.Builder;
import net.prezz.mpr.model.LibraryEntity.Tag;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MpdGetAllArtistsCommand extends MpdDatabaseCommand<Set<String>, LibraryEntity[]> {

    public MpdGetAllArtistsCommand(Set<String> uriFilter) {
        super(uriFilter);
    }

    @Override
    protected LibraryEntity[] doExecute(MpdLibraryDatabaseHelper databaseHelper, Set<String> uriFilter) throws Exception {
        Builder entityBuilder = LibraryEntity.createBuilder();

        List<LibraryEntity> libraryEntities = new ArrayList<LibraryEntity>();

        Cursor c = databaseHelper.selectAllArtists(uriFilter);
        try {
            if (c.moveToFirst()) {
                do {
                    String artist = c.getString(0);
                    String metaArtist = c.getString(1);
                    Integer metaCount = c.getInt(2);
                    libraryEntities.add(entityBuilder.clear().setTag(Tag.ARTIST).setArtist(artist).setMetaArtist(metaArtist).setMetaCount(metaCount).setUriFilter(uriFilter).build());
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        c = databaseHelper.selectAllAlbumArtists(uriFilter);
        try {
            if (c.moveToFirst()) {
                do {
                    String albumArtist = c.getString(0);
                    String metaAlbumArtist = c.getString(1);
                    Integer metaCount = c.getInt(2);
                    libraryEntities.add(entityBuilder.clear().setTag(Tag.ALBUM_ARTIST).setAlbumArtist(albumArtist).setMetaAlbumArtist(metaAlbumArtist).setMetaCount(metaCount).setUriFilter(uriFilter).build());
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        c = databaseHelper.selectAllComposers(uriFilter);
        try {
            if (c.moveToFirst()) {
                do {
                    String composer = c.getString(0);
                    Integer metaCount = c.getInt(1);
                    libraryEntities.add(entityBuilder.clear().setTag(Tag.COMPOSER).setComposer(composer).setMetaCount(metaCount).setUriFilter(uriFilter).build());
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
