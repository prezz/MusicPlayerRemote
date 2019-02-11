package net.prezz.mpr.mpd.command;

import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.LibraryEntity.Builder;
import net.prezz.mpr.model.LibraryEntity.Tag;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;
import android.database.Cursor;

public class MpdGetFilteredTracksAndTitlesCommand extends MpdDatabaseCommand<LibraryEntity, LibraryEntity[]> {

    public MpdGetFilteredTracksAndTitlesCommand(LibraryEntity entity) {
        super(entity);
    }

    @Override
    protected LibraryEntity[] doExecute(MpdLibraryDatabaseHelper databaseHelper, LibraryEntity entity) throws Exception {
        Builder entityBuilder = LibraryEntity.createBuilder();
        Cursor c = databaseHelper.selectFilteredAlbumTitles(entity);

        try {
            int i = 0;
            LibraryEntity[] result = new LibraryEntity[c.getCount()];
            if (c.moveToFirst()) {
                do {
                    Integer track = c.isNull(0) ? null : c.getInt(0);
                    String title = c.getString(1);
                    String artist = c.getString(2);
                    String metaArtist = (track == null) ? c.getString(3) : artist;
                    String albumArtist = c.getString(4);
                    String metaAlbumArtist = (track == null) ? c.getString(5) : albumArtist;
                    String composer = c.getString(6);
                    Integer metaLength = c.getInt(7);
                    Integer metaYear = c.isNull(8) ? null : c.getInt(8);
                    String metaGenre = c.getString(9);
                    result[i++] = entityBuilder.clear().setTag(Tag.TITLE).setGenre(entity.getGenre()).setAlbum(entity.getAlbum()).setArtist(artist)
                            .setMetaArtist(metaArtist).setAlbumArtist(albumArtist).setMetaAlbumArtist(metaAlbumArtist).setComposer(composer).setTitle(title)
                            .setMetaTrack(track).setMetaLength(metaLength).setMetaYear(metaYear).setMetaGenre(metaGenre).setUriFilter(entity.getUriFilter()).build();
                } while (c.moveToNext());
            }
            return result;
        } finally {
            c.close();
        }
    }

    @Override
    protected LibraryEntity[] onError() {
        return new LibraryEntity[0];
    }
}
