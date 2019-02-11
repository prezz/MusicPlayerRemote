package net.prezz.mpr.mpd.command;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.model.UriEntity.FileType;
import net.prezz.mpr.model.UriEntity.UriType;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;
import android.database.Cursor;

public class MpdGetUriCommand extends MpdDatabaseCommand<MpdGetUriCommand.Param, UriEntity[]> {

    protected static final class Param {
        public final UriEntity uriEntity;
        public final Set<String> uriFilter;

        public Param(UriEntity uriEntity, Set<String> uriFilter) {
            this.uriEntity = uriEntity;
            this.uriFilter = (uriFilter != null) ? Collections.unmodifiableSet(uriFilter) : null;
        }
    }

    public MpdGetUriCommand(UriEntity uriEntity, Set<String> uriFilter) {
        super(new Param(uriEntity, uriFilter));
    }

    @Override
    protected UriEntity[] doExecute(MpdLibraryDatabaseHelper databaseHelper, Param param) throws Exception {
        UriEntity uriEntity = param.uriEntity;
        Set<String> uriFilter = param.uriFilter;

        TreeSet<UriEntity> uriSet = new TreeSet<UriEntity>(MpdCommandHelper.getUriComparator());

        String uriPath = (uriEntity != null) ? uriEntity.getFullUriPath(true) : "";

        Cursor c = uriPath.isEmpty() ? databaseHelper.selectMusicEntitiesRootUri(uriFilter) : databaseHelper.selectMusicEntitiesUri(uriPath);
        try {
            addToSet(uriSet, c, uriPath, FileType.MUSIC);
        } finally {
            c.close();
        }

        c = uriPath.isEmpty() ? databaseHelper.selectPlaylistEntitiesRootUri(uriFilter) : databaseHelper.selectPlaylistEntitiesUri(uriPath);
        try {
            addToSet(uriSet, c, uriPath, FileType.PLAYLIST);
        } finally {
            c.close();
        }

        return uriSet.toArray(new UriEntity[uriSet.size()]);
    }

    @Override
    protected UriEntity[] onError() {
        return new UriEntity[0];
    }

    private void addToSet(TreeSet<UriEntity> uriSet, Cursor c, String uriPath, FileType fileType) {
        if (c.moveToFirst()) {
            do {
                String uri = c.getString(0);

                if (uri.contains(UriEntity.DIR_SEPERATOR)) {
                    String dir = uri.substring(0, uri.indexOf(UriEntity.DIR_SEPERATOR));
                    uriSet.add(new UriEntity(UriType.DIRECTORY, FileType.NA, uriPath, dir));
                } else {
                    uriSet.add(new UriEntity(UriType.FILE, fileType, uriPath, uri));
                }
            } while (c.moveToNext());
        }
    }
}
