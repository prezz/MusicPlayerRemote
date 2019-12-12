package net.prezz.mpr.mpd.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.LibraryEntity.Builder;
import net.prezz.mpr.model.LibraryEntity.Tag;
import net.prezz.mpr.model.UriEntity.FileType;
import net.prezz.mpr.model.UriEntity.UriType;
import net.prezz.mpr.model.SearchResult;
import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;
import android.database.Cursor;

public class MpdSearchLibraryCommand extends MpdDatabaseCommand<MpdSearchLibraryCommand.Param, SearchResult>{

    protected static final class Param {
        public final String query;
        public final boolean searchUri;
        public final SortedSet<String> uriFilter;

        public Param(String query, boolean searchUri, SortedSet<String> uriFilter) {
            this.query = query;
            this.searchUri = searchUri;
            this.uriFilter = (uriFilter != null) ? Collections.unmodifiableSortedSet(uriFilter) : null;
        }
    }

    public MpdSearchLibraryCommand(String queury, boolean searchUri, SortedSet<String> uriFilter) {
        super(new Param(queury, searchUri, uriFilter));
    }

    @Override
    protected SearchResult doExecute(MpdLibraryDatabaseHelper databaseHelper, Param param)    throws Exception {
        SortedSet<String> uriFilter = param.uriFilter;

        Builder entityBuilder = LibraryEntity.createBuilder();

        List<LibraryEntity> libraryEntities = new ArrayList<LibraryEntity>();

        Cursor c = databaseHelper.findArtists(param.query, uriFilter);
        try {
            if (c.moveToFirst()) {
                do {
                    String artist = c.getString(0);
                    String metaArtist = c.getString(1);
                    Integer metaCount = c.getInt(2);
                    libraryEntities.add(entityBuilder.clear().setTag(Tag.ARTIST).setArtist(artist). setMetaArtist(metaArtist).setMetaCount(metaCount).setUriFilter(uriFilter).build());
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        c = databaseHelper.findAlbumArtists(param.query, uriFilter);
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

        c = databaseHelper.findComposers(param.query, uriFilter);
        try {
            if (c.moveToFirst()) {
                do {
                    String composers = c.getString(0);
                    Integer metaCount = c.getInt(1);
                    libraryEntities.add(entityBuilder.clear().setTag(Tag.COMPOSER).setComposer(composers).setMetaCount(metaCount).setUriFilter(uriFilter).build());
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        c = databaseHelper.findAlbums(param.query, uriFilter);
        try {
            if (c.moveToFirst()) {
                do {
                    String album = c.getString(0);
                    String metaAlbum = c.getString(1);
                    String artist = c.getString(2);
                    String metaArtist = c.isNull(3) ? "" : c.getString(3);
                    libraryEntities.add(entityBuilder.clear().setTag(Tag.ALBUM).setAlbum(album).setMetaAlbum(metaAlbum).setMetaArtist(metaArtist).setLookupArtist(artist).setUriFilter(uriFilter).build());
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        c = databaseHelper.findTitles(param.query, uriFilter);
        try {
            if (c.moveToFirst()) {
                do {
                    String title = c.getString(0);
                    String artist = c.getString(1);
                    String album = c.getString(2);
                    libraryEntities.add(entityBuilder.clear().setTag(Tag.TITLE).setTitle(title).setArtist(artist).setAlbum(album).setUriFilter(uriFilter).build());
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        TreeSet<UriEntity> uriEntities = new TreeSet<UriEntity>(MpdCommandHelper.getUriComparator());

        if (param.searchUri) {
            c = databaseHelper.findUri(param.query, uriFilter);
            try {
                if (c.moveToFirst()) {
                    String lowerCaseQuery = param.query.toLowerCase(Locale.US);
                    do {
                        String uri = c.getString(0);
                        String[] split = uri.split(UriEntity.DIR_SEPERATOR);

                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < split.length; i++) {
                            String section = split[i];
                            if (section.toLowerCase(Locale.US).contains(lowerCaseQuery)) {
                                if (i == split.length - 1) {
                                    uriEntities.add(new UriEntity(UriType.FILE, FileType.MUSIC, sb.toString(), section));
                                } else {
                                    uriEntities.add(new UriEntity(UriType.DIRECTORY, FileType.NA, sb.toString(), section));
                                }
                            }

                            sb.append(section);
                            sb.append(UriEntity.DIR_SEPERATOR);
                        }

                    } while (c.moveToNext());
                }
            } finally {
                c.close();
            }
        }

        return new SearchResult(libraryEntities.toArray(new LibraryEntity[libraryEntities.size()]), uriEntities.toArray(new UriEntity[uriEntities.size()]));
    }

    @Override
    protected SearchResult onError() {
        return new SearchResult(new LibraryEntity[0], new UriEntity[0]);
    }
}
