package net.prezz.mpr.mpd.command;

import android.database.Cursor;

import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.LibraryEntity.Builder;
import net.prezz.mpr.model.LibraryEntity.Tag;
import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class MpdGetAllFoldersCommand extends MpdDatabaseCommand<Set<String>, LibraryEntity[]> {

    public MpdGetAllFoldersCommand(Set<String> uriFilter) {
        super(uriFilter);
    }

    @Override
    protected LibraryEntity[] doExecute(MpdLibraryDatabaseHelper databaseHelper, Set<String> uriFilter) throws Exception {
        TreeSet<String> dirSet = new TreeSet<String>(new UriComparator());

        Cursor c = databaseHelper.selectMusicEntitiesRootUri(uriFilter);
        try {
            if (c.moveToFirst()) {
                do {
                    String uri = c.getString(0);

                    if (uri.contains(UriEntity.DIR_SEPERATOR)) {
                        uri = uri.substring(0, uri.indexOf(UriEntity.DIR_SEPERATOR));
                        dirSet.add(uri);
                    }
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        Builder entityBuilder = LibraryEntity.createBuilder();
        LibraryEntity[] result = new LibraryEntity[dirSet.size()];
        int i = 0;
        for (String dir : dirSet) {
            Integer albumCount = getAlbumCount(databaseHelper, dir);
            UriEntity uriEntity = new UriEntity(UriEntity.UriType.DIRECTORY, UriEntity.FileType.NA, "", dir);
            result[i++] = entityBuilder.clear().setTag(Tag.URI).setUriEntity(uriEntity).setMetaCount(albumCount).setUriFilter(uriFilter).build();
        }

        return result;
    }

    @Override
    protected LibraryEntity[] onError() {
        return new LibraryEntity[0];
    }

    private Integer getAlbumCount(MpdLibraryDatabaseHelper databaseHelper, String uriPath) {
        Integer count = 0;

        Cursor c = databaseHelper.selectAlbumCountInUri(uriPath);
        try {
            if (c.moveToFirst()) {
                count = c.getInt(0);
            }
        } finally {
            c.close();
        }

        return count;
    }

    private static final class UriComparator implements Comparator<String> {

        @Override
        public int compare(String lhs, String rhs) {
            return lhs.compareToIgnoreCase(rhs);
        }
    }

}
