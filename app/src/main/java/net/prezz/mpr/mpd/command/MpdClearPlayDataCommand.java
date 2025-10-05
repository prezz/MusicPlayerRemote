package net.prezz.mpr.mpd.command;

import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;

public class MpdClearPlayDataCommand extends MpdDatabaseCommand<Void, Boolean> {

    public MpdClearPlayDataCommand() {
        super(null);
    }

    @Override
    protected Boolean doExecute(MpdLibraryDatabaseHelper databaseHelper, Void param) throws Exception {

        databaseHelper.beginTransaction();
        try {
            databaseHelper.clearPlayData();
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
