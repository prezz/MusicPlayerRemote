package net.prezz.mpr.mpd.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import java.io.IOException;

import net.prezz.mpr.R;
import net.prezz.mpr.Utils;
import net.prezz.mpr.mpd.command.MpdCommandHelper;
import net.prezz.mpr.mpd.connection.MpdConnection;
import net.prezz.mpr.ui.ApplicationActivator;

public class MpdDatabaseBuilder {

	public enum UpdateDatabaseResult { NO_ERROR, UPDATE_RUNNING_ERROR, TRACK_COUNT_MISMATCH_ERROR }
	
	private static UpdateDatabaseResult lastDatabaseResult = UpdateDatabaseResult.NO_ERROR;
	
	
	private MpdDatabaseBuilder() {
	}
	
	public static UpdateDatabaseResult getLastDatabaseResult() {
		return lastDatabaseResult;
	}
	
	public static void buildDatabase(MpdConnection connection, MpdLibraryDatabaseHelper libraryDatabaseHelper) throws IOException {
		lastDatabaseResult = UpdateDatabaseResult.NO_ERROR;
		
		int expectedTrackCount = 0;
		int updatingJobId = 0;
		
		String[][] lines = connection.writeResponseCommandList(new String[] {"status\n", "stats\n"});
		for (String line : lines[0]) {
			if (line.startsWith("updating_db: ")) {
				updatingJobId = Integer.decode(line.substring(13));
			}
		}
		
		if (updatingJobId != 0) {
			lastDatabaseResult = UpdateDatabaseResult.UPDATE_RUNNING_ERROR;
			return;
		}

		for (String line : lines[1]) {
			if (line.startsWith("songs: ")) {
				expectedTrackCount = Integer.decode(line.substring(7));
			}
		}

        boolean properSorting = properSorting();

		int actualTrackCount = 0;
		libraryDatabaseHelper.beginTransaction();
		try {
			libraryDatabaseHelper.cleanDatabase();

			connection.writeCommand("listallinfo\n");
			MusicLibraryRecord record = new MusicLibraryRecord();
			boolean add = false;
			String line = null;
			while ((line = connection.readLine()) != null) {
				if (line.startsWith(MpdConnection.OK)) {
					break;
				}
				if (line.startsWith(MpdConnection.ACK)) {
					throw new IOException("Error reading MPD response: " + line);
				}
				
				if (line.startsWith("file: ")) {
					if (add) {
						libraryDatabaseHelper.addMusicEntity(record);
						actualTrackCount++;
					}
					record.clear();
					add = true;
					record.setUri(line.substring(6));
				}
				if (line.startsWith("Artist: ")) {
                    String artist = line.substring(8);
					record.setArtist(artist);

                    String metaArtist = (properSorting) ? Utils.moveInsignificantWordsLast(artist) : artist;
                    record.setMetaArtist(metaArtist);
				}
				if (line.startsWith("AlbumArtist: ")) {
                    String albumArtist = line.substring(13);
					record.setAlbumArtist(albumArtist);

                    String metaAlbumArtist = (properSorting) ? Utils.moveInsignificantWordsLast(albumArtist) : albumArtist;
                    record.setMetaAlbumArtist(metaAlbumArtist);
				}
				if (line.startsWith("Composer: ")) {
					record.setComposer(line.substring(10));
				}
				if (line.startsWith("Artist: ")) {
					record.setArtist(line.substring(8));
				}
				if (line.startsWith("Album: ")) {
                    String album = line.substring(7);
					record.setAlbum(line.substring(7));

                    String metaAlbum = (properSorting) ? Utils.moveInsignificantWordsLast(album) : album;
                    record.setMetaAlbum(metaAlbum);
				}
				if (line.startsWith("Title: ")) {
					record.setTitle(line.substring(7));
				}
				if (line.startsWith("Track: ")) {
					record.setTrack(MpdCommandHelper.getTrack(line.substring(7)));
				}
				if (line.startsWith("Genre: ")) {
					record.setGenre(line.substring(7));
				}
				if (line.startsWith("Date: ")) {
					record.setYear(parseInteger(line.substring(6)));
				}
				if (line.startsWith("Time: ")) {
					record.setLength(parseInteger(line.substring(6)));
				}

				if (line.startsWith("playlist: ")) {
                    String uri = line.substring(10);
                    libraryDatabaseHelper.addPlaylistEntity(uri);
                }
			}
			
			if (add) {
				libraryDatabaseHelper.addMusicEntity(record);
				actualTrackCount++;
			}
			
			libraryDatabaseHelper.setTransactionSuccessful();
		} finally {
			libraryDatabaseHelper.endTransaction();
		}
		
		if (expectedTrackCount != actualTrackCount) {
			lastDatabaseResult = UpdateDatabaseResult.TRACK_COUNT_MISMATCH_ERROR;
		}
	}
	
	private static Integer parseInteger(String integer) {
		try {
			return Integer.parseInt(integer);
		} catch (Exception ex) {
		}
		return null;
	}

    private static boolean properSorting() {
        Context context = ApplicationActivator.getContext();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Resources resources = context.getResources();
        return sharedPreferences.getBoolean(resources.getString(R.string.settings_library_proper_sort_key), false);
    }
}
