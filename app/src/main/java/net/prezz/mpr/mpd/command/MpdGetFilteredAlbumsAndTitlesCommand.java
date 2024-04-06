package net.prezz.mpr.mpd.command;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Map<String, PlayData> playDataMap = new HashMap<>();
        if (entity.getArtist() != null) {
            LocalDate today = LocalDate.now();
            Cursor c = databaseHelper.selectAlbumPlayData(entity.getArtist());
            try {
                if (c.moveToFirst()) {
                    do {
                        String album = c.getString(0);
                        String playDate = c.getString(1);
                        Integer playCount = c.getInt(2);

                        int daysAgo = (int) DAYS.between(LocalDate.parse(playDate, DateTimeFormatter.ISO_DATE), today);
                        playDataMap.put(album, new PlayData(daysAgo, playCount));
                    } while (c.moveToNext());
                }
            } finally {
                c.close();
            }
        }

        Builder entityBuilder = LibraryEntity.createBuilder();
        List<LibraryEntity> result = new ArrayList<LibraryEntity>();
        Cursor c = databaseHelper.selectFilteredAlbumsWithStatistics(entity);
        try {
            if (c.moveToFirst()) {
                do {
                    String album = c.getString(0);
                    String metaAlbum = c.getString(1);
                    String artist = c.getString(2);
                    Boolean metaCompilation = c.getInt(3) > 1 ? Boolean.TRUE : Boolean.FALSE;
                    Integer metaCount = c.getInt(4);
                    Integer metaLength = c.getInt(5);
                    LibraryEntity.Builder b = entityBuilder.clear().setTag(Tag.ALBUM).setGenre(entity.getGenre()).setArtist(entity.getArtist()).setAlbumArtist(entity.getAlbumArtist()).setComposer(entity.getComposer())
                            .setAlbum(album).setUriEntity(entity.getUriEntity()).setMetaAlbum(metaAlbum).setMetaArtist(artist).setLookupArtist(artist).setLookupAlbum(album).setMetaCompilation(metaCompilation).setMetaCount(metaCount)
                            .setMetaLength(metaLength).setUriFilter(entity.getUriFilter());

                    PlayData playData = playDataMap.getOrDefault(album, new PlayData(null, null));
                    b.setPlayedDaysAgo(playData.daysAgo);
                    b.setPlayedCount(playData.playCount);

                    result.add(b.build());
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
                            .setTitle(title).setAlbum(album).setUriEntity(entity.getUriEntity()).setMetaLength(metaLength).setMetaYear(metaYear).setMetaGenre(metaGenre).setUriFilter(entity.getUriFilter()).build());
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

    private static class PlayData {

        Integer daysAgo;
        Integer playCount;

        public PlayData(Integer daysAgo, Integer playCount) {
            this.daysAgo = daysAgo;
            this.playCount = playCount;
        }
    }
}
