package net.prezz.mpr.mpd.command;

import android.database.Cursor;

import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.LibraryEntity.Builder;
import net.prezz.mpr.model.LibraryEntity.Tag;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class MpdGetAllArtistsCommand extends MpdDatabaseCommand<LibraryEntity, LibraryEntity[]> {

    public MpdGetAllArtistsCommand(LibraryEntity entity) {
        super(entity);
    }

    @Override
    protected LibraryEntity[] doExecute(MpdLibraryDatabaseHelper databaseHelper, LibraryEntity entity) throws Exception {
        Builder entityBuilder = LibraryEntity.createBuilder();

        List<LibraryEntity> libraryEntities = new ArrayList<LibraryEntity>();

        Cursor c = databaseHelper.selectAllArtists(entity);
        try {
            if (c.moveToFirst()) {
                do {
                    String artist = c.getString(0);
                    String metaArtist = c.getString(1);
                    Integer metaCount = c.getInt(2);
                    libraryEntities.add(entityBuilder.clear().setTag(Tag.ARTIST).setArtist(artist).setUriEntity(entity.getUriEntity()).setMetaArtist(metaArtist).setMetaCount(metaCount).setUriFilter(entity.getUriFilter()).build());
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        c = databaseHelper.selectAllAlbumArtists(entity);
        try {
            if (c.moveToFirst()) {
                do {
                    String albumArtist = c.getString(0);
                    String metaAlbumArtist = c.getString(1);
                    Integer metaCount = c.getInt(2);
                    libraryEntities.add(entityBuilder.clear().setTag(Tag.ALBUM_ARTIST).setAlbumArtist(albumArtist).setUriEntity(entity.getUriEntity()).setMetaAlbumArtist(metaAlbumArtist).setMetaCount(metaCount).setUriFilter(entity.getUriFilter()).build());
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        c = databaseHelper.selectAllComposers(entity);
        try {
            if (c.moveToFirst()) {
                do {
                    String composer = c.getString(0);
                    Integer metaCount = c.getInt(1);
                    libraryEntities.add(entityBuilder.clear().setTag(Tag.COMPOSER).setComposer(composer).setUriEntity(entity.getUriEntity()).setMetaCount(metaCount).setUriFilter(entity.getUriFilter()).build());
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
