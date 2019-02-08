package net.prezz.mpr.mpd.command;

import net.prezz.mpr.mpd.connection.MpdConnection;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;

public class MpdDeleteLocalLibraryDatabaseCommand extends MpdConnectionCommand<MpdLibraryDatabaseHelper, Boolean> {

	public MpdDeleteLocalLibraryDatabaseCommand(MpdLibraryDatabaseHelper libraryDatabaseHelper) {
		super(libraryDatabaseHelper);
	}

	@Override
	protected Boolean doExecute(MpdConnection connection, MpdLibraryDatabaseHelper libraryDatabaseHelper) throws Exception {
		libraryDatabaseHelper.cleanDatabase();
		return Boolean.TRUE;
	}

	@Override
	protected Boolean onError() {
		return Boolean.FALSE;
	}
}
