package net.prezz.mpr.mpd.command;

import android.database.Cursor;

import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;

import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

public class MpdGetHideableUriFolders extends MpdDatabaseCommand<Void, String[]> {

    public MpdGetHideableUriFolders() {
        super(null);
    }

    @Override
    protected String[] doExecute(MpdLibraryDatabaseHelper databaseHelper, Void param) throws Exception {

        TreeSet<String> uriSet = new TreeSet<String>(new UriComparator());

        Cursor c = databaseHelper.selectAllLibraryUris(Collections.<String>emptySet());
        try {
            addToSet(uriSet, c);
        } finally {
            c.close();
        }

        c = databaseHelper.selectAllPlaylistUris(Collections.<String>emptySet());
        try {
            addToSet(uriSet, c);
        } finally {
            c.close();
        }

        return uriSet.toArray(new String[uriSet.size()]);
    }

    @Override
    protected String[] onError() {
        return new String[0];
    }

    private void addToSet(TreeSet<String> uriSet, Cursor c) {
        if (c.moveToFirst()) {
            do {
                String uri = c.getString(0);

                if (uri.contains(UriEntity.DIR_SEPERATOR)) {
                    uri = uri.substring(0, uri.indexOf(UriEntity.DIR_SEPERATOR)+1);
                    uriSet.add(uri);
                }
            } while (c.moveToNext());
        }
    }

    private static final class UriComparator implements Comparator<String> {

        @Override
        public int compare(String lhs, String rhs) {
            return lhs.compareToIgnoreCase(rhs);
        }
    }
}
