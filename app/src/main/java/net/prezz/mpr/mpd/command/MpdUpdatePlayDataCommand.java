package net.prezz.mpr.mpd.command;

import android.database.Cursor;

import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MpdUpdatePlayDataCommand extends MpdDatabaseCommand<List<String>, Boolean> {

    public MpdUpdatePlayDataCommand(List<String> param) {
        super(param);
    }

    @Override
    protected Boolean doExecute(MpdLibraryDatabaseHelper databaseHelper, List<String> param) throws Exception {

        String today = LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE);

        databaseHelper.beginTransaction();
        try {
            for (String uri : param) {
                Cursor c = databaseHelper.getPlayData(uri);
                if (c.moveToFirst()) {
                    String date = c.getString(0);
                    Integer count = c.getInt(1);
                    if (!today.equals(date)) {
                        databaseHelper.upsertPlayData(uri, today, count + 1);
                    }
                } else {
                    databaseHelper.upsertPlayData(uri, today, 1);
                }
            }

            databaseHelper.setTransactionSuccessful();
        } finally {
            databaseHelper.endTransaction();
        }

        return Boolean.TRUE;
    }

    @Override
    protected Boolean onError() {
        return Boolean.FALSE;
    }
}
