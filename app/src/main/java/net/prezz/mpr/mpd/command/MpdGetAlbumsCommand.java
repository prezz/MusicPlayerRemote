package net.prezz.mpr.mpd.command;

import static java.time.temporal.ChronoUnit.DAYS;

import net.prezz.mpr.Utils;
import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.LibraryEntity.Builder;
import net.prezz.mpr.model.LibraryEntity.Tag;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;
import android.database.Cursor;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MpdGetAlbumsCommand extends MpdDatabaseCommand<MpdGetAlbumsCommand.Param, LibraryEntity[]> {

    protected static final class Param {
        public final Boolean sortByArtist;
        public final LibraryEntity entity;

        public Param(Boolean sortByArtist, LibraryEntity entity) {
            this.sortByArtist = sortByArtist;
            this.entity = entity;
        }
    }

    private static final String VARIOUS = "Various";

    public MpdGetAlbumsCommand(boolean sortByArtist, LibraryEntity entity) {
        super(new Param(Boolean.valueOf(sortByArtist), entity));
    }

    @Override
    protected LibraryEntity[] doExecute(MpdLibraryDatabaseHelper databaseHelper, Param param) throws Exception {

        LocalDate today = LocalDate.now();
        Map<String, PlayData> playDataMap = new HashMap<>();
        Cursor c = databaseHelper.selectAllAlbumPlayData();
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

        Builder entityBuilder = LibraryEntity.createBuilder();

        Boolean sortByArtist = param.sortByArtist;
        LibraryEntity entity = param.entity;

        c = databaseHelper.selectAlbums(sortByArtist.booleanValue(), entity);
        try {
            int compilationIndex = 0;
            List<LibraryEntity> tracks = new ArrayList<LibraryEntity>();
            List<LibraryEntity> compilations = new ArrayList<LibraryEntity>();
            List<LibraryEntity> result = new ArrayList<LibraryEntity>(c.getCount());
            if (c.moveToFirst()) {
                do {
                    String album = c.getString(0);
                    String metaAlbum = c.getString(1);
                    String artist = c.getString(2);
                    String metaArtist = c.isNull(3) ? "" : c.getString(3);
                    Integer metaLength =  c.getInt(4);
                    Boolean metaCompilation = c.getInt(5) > 1 ? Boolean.TRUE : Boolean.FALSE;

                    if (Utils.nullOrEmpty(album)) {
                        LibraryEntity.Builder b = entityBuilder.clear().setTag(Tag.ALBUM).setGenre(entity.getGenre()).setAlbum(album).setUriEntity(entity.getUriEntity()).setMetaAlbum(metaAlbum).setMetaArtist(metaArtist)
                                .setLookupArtist(artist).setLookupAlbum(album).setMetaCompilation(metaCompilation).setMetaLength(metaLength).setUriFilter(entity.getUriFilter());

                        PlayData playData = playDataMap.getOrDefault(album, new PlayData(null, null));
                        b.setPlayedDaysAgo(playData.daysAgo);
                        b.setPlayedCount(playData.playCount);

                        tracks.add(b.build());
                    } else if (sortByArtist == Boolean.TRUE && metaCompilation == Boolean.TRUE) {
                        LibraryEntity.Builder b = entityBuilder.clear().setTag(Tag.ALBUM).setGenre(entity.getGenre()).setAlbum(album).setUriEntity(entity.getUriEntity()).setMetaAlbum(metaAlbum).setMetaArtist(VARIOUS)
                                .setLookupArtist(VARIOUS).setLookupAlbum(album).setMetaCompilation(metaCompilation).setMetaLength(metaLength).setUriFilter(entity.getUriFilter());

                        PlayData playData = playDataMap.getOrDefault(album, new PlayData(null, null));
                        b.setPlayedDaysAgo(playData.daysAgo);
                        b.setPlayedCount(playData.playCount);

                        compilations.add(b.build());
                    } else {
                        LibraryEntity.Builder b = entityBuilder.clear().setTag(Tag.ALBUM).setGenre(entity.getGenre()).setAlbum(album).setUriEntity(entity.getUriEntity()).setMetaAlbum(metaAlbum).setMetaArtist(metaArtist)
                                .setLookupArtist(artist).setLookupAlbum(album).setMetaCompilation(metaCompilation).setMetaLength(metaLength).setUriFilter(entity.getUriFilter());

                        PlayData playData = playDataMap.getOrDefault(album, new PlayData(null, null));
                        b.setPlayedDaysAgo(playData.daysAgo);
                        b.setPlayedCount(playData.playCount);

                        result.add(b.build());
                        if (sortByArtist == Boolean.TRUE && VARIOUS.compareToIgnoreCase(metaArtist) > 0) {
                            compilationIndex++;
                        }
                    }
                } while (c.moveToNext());
            }

            if (sortByArtist == Boolean.TRUE) {
                Collections.sort(compilations, new Comparator<LibraryEntity>() {
                    @Override
                    public int compare(LibraryEntity lhs, LibraryEntity rhs) {
                        return lhs.getAlbum().compareToIgnoreCase(rhs.getAlbum());
                    }
                });

                result.addAll(compilationIndex, compilations);
            }

            result.addAll(0, tracks);

            return result.toArray(new LibraryEntity[result.size()]);
        } finally {
            c.close();
        }
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
