package net.prezz.mpr.mpd.command;

import android.database.Cursor;

import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.LibraryEntity.Builder;
import net.prezz.mpr.model.LibraryEntity.Tag;
import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;

import java.util.Collections;
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

        Cursor c = databaseHelper.selectMusicEntitiesRootUri(Collections.<String>emptySet()); // use uriFilter?
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

        int i = 0;
        LibraryEntity[] result = new LibraryEntity[dirSet.size()+1];

        UriEntity allUriEntity = new UriEntity(UriEntity.UriType.DIRECTORY, UriEntity.FileType.NA, "", "");
        result[i++] = entityBuilder.clear().setTag(Tag.URI).setUriEntity(allUriEntity).setUriFilter(uriFilter).build();
        for (String dir : dirSet) {
            UriEntity uriEntity = new UriEntity(UriEntity.UriType.DIRECTORY, UriEntity.FileType.NA, "", dir);
            result[i++] = entityBuilder.clear().setTag(Tag.URI).setUriEntity(uriEntity).build();
        }

        return result;
    }

    @Override
    protected LibraryEntity[] onError() {
        return new LibraryEntity[0];
    }

    private static final class UriComparator implements Comparator<String> {

        @Override
        public int compare(String lhs, String rhs) {
            return lhs.compareToIgnoreCase(rhs);
        }
    }

}
