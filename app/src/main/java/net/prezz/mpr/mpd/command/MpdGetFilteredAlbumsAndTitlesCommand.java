package net.prezz.mpr.mpd.command;

import java.util.ArrayList;
import java.util.List;

import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.LibraryEntity.Builder;
import net.prezz.mpr.model.LibraryEntity.Tag;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;
import android.database.Cursor;

public class MpdGetFilteredAlbumsAndTitlesCommand extends MpdDatabaseCommand<LibraryEntity, LibraryEntity[]> {

    public MpdGetFilteredAlbumsAndTitlesCommand(LibraryEntity entity) {
        super(entity);
    }

    @Override
    protected LibraryEntity[] doExecute(MpdLibraryDatabaseHelper databaseHelper, LibraryEntity entity) throws Exception {
        Builder entityBuilder = LibraryEntity.createBuilder();

        List<LibraryEntity> result = new ArrayList<LibraryEntity>();

        Cursor c = databaseHelper.selectFilteredAlbums(entity);
        try {
            if (c.moveToFirst()) {
                do {
                    String album = c.getString(0);
                    String metaAlbum = c.getString(1);
                    String artist = c.getString(2);
                    Boolean metaCompilation = c.getInt(3) > 1 ? Boolean.TRUE : Boolean.FALSE;
                    Integer metaCount = c.getInt(4);
                    Integer metaLength = c.getInt(5);
                    result.add(entityBuilder.clear().setTag(Tag.ALBUM).setGenre(entity.getGenre()).setArtist(entity.getArtist()).setAlbumArtist(entity.getAlbumArtist()).setComposer(entity.getComposer())
                            .setAlbum(album).setUriPath(entity.getUriPath()).setMetaAlbum(metaAlbum).setMetaArtist(artist).setLookupArtist(artist).setLookupAlbum(album).setMetaCompilation(metaCompilation).setMetaCount(metaCount)
                            .setMetaLength(metaLength).setUriFilter(entity.getUriFilter()).build());
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        c = databaseHelper.selectFilteredArtistTitles(entity);
        try {
            if (c.moveToFirst()) {
                do {
                    String title = c.getString(0);
                    String album = c.getString(1);
                    Integer metaLength = c.getInt(2);
                    Integer metaYear = c.isNull(3) ? null : c.getInt(3);
                    String metaGenre = c.getString(4);
                    result.add(entityBuilder.clear().setTag(Tag.TITLE).setGenre(entity.getGenre()).setArtist(entity.getArtist()).setAlbumArtist(entity.getAlbumArtist()).setComposer(entity.getComposer())
                            .setTitle(title).setAlbum(album).setMetaLength(metaLength).setMetaYear(metaYear).setMetaGenre(metaGenre).setUriFilter(entity.getUriFilter()).build());
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        return result.toArray(new LibraryEntity[result.size()]);
    }

    @Override
    protected LibraryEntity[] onError() {
        return new LibraryEntity[0];
    }
}
