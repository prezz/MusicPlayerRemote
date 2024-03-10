package net.prezz.mpr.mpd.command;

import android.database.Cursor;

import net.prezz.mpr.model.PlaylistEntity;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MpdUpdatePlayDataCommand extends MpdDatabaseCommand<List<PlaylistEntity>, Boolean> {

    public MpdUpdatePlayDataCommand(List<PlaylistEntity> param) {
        super(param);
    }

    @Override
    protected Boolean doExecute(MpdLibraryDatabaseHelper databaseHelper, List<PlaylistEntity> param) throws Exception {

        Boolean result = Boolean.FALSE;
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        databaseHelper.beginTransaction();
        try {
            for (PlaylistEntity entity : param) {
                String artist = entity.getArtist();
                String album = entity.getAlbum();
                String title = entity.getTitle();

                Cursor c = databaseHelper.getPlayData(artist, album, title);
                if (c.moveToFirst()) {
                    String date = c.getString(0);
                    Integer count = c.getInt(1);
                    if (!today.equals(date)) {
                        databaseHelper.upsertPlayData(artist, album, title, today, count + 1);
                        result = Boolean.TRUE;
                    }
                } else {
                    databaseHelper.upsertPlayData(artist, album, title, today, 1);
                    result = Boolean.TRUE;
                }
            }

            databaseHelper.setTransactionSuccessful();
        } finally {
            databaseHelper.endTransaction();
        }

        return result;
    }

    @Override
    protected Boolean onError() {
        return Boolean.FALSE;
    }
}
