package net.prezz.mpr.mpd.command;

import android.database.Cursor;

import net.prezz.mpr.Utils;
import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.LibraryEntity.Builder;
import net.prezz.mpr.model.LibraryEntity.Tag;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MpdGetFilteredAlbumsCommand extends MpdDatabaseCommand<MpdGetFilteredAlbumsCommand.Param, LibraryEntity[]> {

    protected static final class Param {
        public final Boolean sortByArtist;
        public final LibraryEntity entity;

        public Param(Boolean sortByArtist, LibraryEntity entity) {
            this.sortByArtist = sortByArtist;
            this.entity = entity;
        }
    }

    private static final String VARIOUS = "Various";

    public MpdGetFilteredAlbumsCommand(boolean sortByArtist, LibraryEntity entity) {
        super(new Param(Boolean.valueOf(sortByArtist), entity));
    }

    @Override
    protected LibraryEntity[] doExecute(MpdLibraryDatabaseHelper databaseHelper, Param param) throws Exception {
        Builder entityBuilder = LibraryEntity.createBuilder();

        Boolean sortByArtist = param.sortByArtist;
        LibraryEntity paramEntity = param.entity;

        Cursor c = databaseHelper.selectFilteredAlbums(paramEntity, sortByArtist);

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
                    Boolean metaCompilation = c.getInt(4) > 1 ? Boolean.TRUE : Boolean.FALSE;

                    if (Utils.nullOrEmpty(album)) {
                        LibraryEntity entity = entityBuilder.clear().setTag(Tag.ALBUM).setAlbum(album).setUriPath(paramEntity.getUriPath())
                                .setMetaAlbum(metaAlbum).setMetaArtist(metaArtist).setLookupArtist(artist).setLookupAlbum(album)
                                .setMetaCompilation(metaCompilation).setUriFilter(paramEntity.getUriFilter()).build();
                        tracks.add(entity);
                    } else if (sortByArtist == Boolean.TRUE && metaCompilation == Boolean.TRUE) {
                        LibraryEntity entity = entityBuilder.clear().setTag(Tag.ALBUM).setAlbum(album).setUriPath(paramEntity.getUriPath())
                                .setMetaAlbum(metaAlbum).setMetaArtist(VARIOUS).setLookupArtist(VARIOUS).setLookupAlbum(album)
                                .setMetaCompilation(metaCompilation).setUriFilter(paramEntity.getUriFilter()).build();
                        compilations.add(entity);
                    } else {
                        LibraryEntity entity = entityBuilder.clear().setTag(Tag.ALBUM).setAlbum(album).setUriPath(paramEntity.getUriPath())
                                .setMetaAlbum(metaAlbum).setMetaArtist(metaArtist).setLookupArtist(artist).setLookupAlbum(album)
                                .setMetaCompilation(metaCompilation).setUriFilter(paramEntity.getUriFilter()).build();
                        result.add(entity);
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
}
