package net.prezz.mpr.mpd.command;

import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;

public class MpdDeleteLocalLibraryDatabaseCommand extends MpdDatabaseCommand<Void, Boolean> {

    public MpdDeleteLocalLibraryDatabaseCommand() {
        super(null, false);
    }

    @Override
    protected Boolean doExecute(MpdLibraryDatabaseHelper databaseHelper, Void param) throws Exception {
        databaseHelper.cleanDatabase();
        return Boolean.TRUE;
    }

    @Override
    protected Boolean onError() {
        return Boolean.FALSE;
    }
}
