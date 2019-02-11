package net.prezz.mpr.model.external.local;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.model.external.CoverService;
import net.prezz.mpr.mpd.connection.MpdConnection;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;
import net.prezz.mpr.ui.ApplicationActivator;
import net.prezz.mpr.ui.mpd.MpdPlayerSettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MpdCoverService implements CoverService {

    @Override
    public List<String> getCoverUrls(String artist, String album) {
        try {
            Context context = ApplicationActivator.getContext();

            if (album != null) {
                MpdPlayerSettings mpdSettings = MpdPlayerSettings.create(context);
                String mpdHost = mpdSettings.getMpdHost();

                Set<String> files = getFileFromEachDirectories(context, mpdHost, artist, album);
                List<String> result = getValidUrls(mpdSettings, files);
                return result;
            }
        } catch (Exception ex) {
            Log.e(MpdCoverService.class.getName(), "Error getting covers from Local cover service", ex);
        }

        return Collections.emptyList();
    }

    private static Set<String> getFileFromEachDirectories(Context context, String mpdHost, String artist, String album) {
        Map<String, String> result = new HashMap<String, String>();

        MpdLibraryDatabaseHelper databaseHelper = new MpdLibraryDatabaseHelper(context, mpdHost);
        try {
            Cursor c = databaseHelper.findUris(artist, album);
            try {
                if (c.moveToFirst()) {
                    do {
                        String uri = c.getString(0);
                        String dir = uri.substring(0, uri.lastIndexOf(UriEntity.DIR_SEPERATOR));
                        result.put(dir, uri);
                    } while (c.moveToNext());
                }
            } finally {
                c.close();
            }
        } finally {
            databaseHelper.close();
        }

        return new HashSet<>(result.values());
    }

    private static List<String> getValidUrls(MpdPlayerSettings mpdSettings, Collection<String> uris) {
        List<String> result = new ArrayList<String>();

        for (String uri : uris) {
            if (uriExists(mpdSettings, uri)) {
                result.add("mpd://" + uri);
            }
        }

        return result;
    }

    private static boolean uriExists(MpdPlayerSettings mpdSettings, String uri) {
        boolean result = false;

        MpdConnection connection = new MpdConnection(mpdSettings);

        try {
            connection.connect();
            if (connection.isMinimumVersion(0, 21, 0)) {

                connection.writeCommand("albumart \"" + uri + "\" 0\n");

                boolean exists = false;
                String line = null;
                while ((line = connection.readLine()) != null) {
                    if (line.startsWith(MpdConnection.OK)) {
                        result = exists;
                        break;
                    }
                    if (line.startsWith(MpdConnection.ACK)) {
                        break;
                    }
                    if (line.startsWith("size: ")) {
                        int size = Integer.parseInt(line.substring(6));
                        exists = (size <= 2097152); // 2MB
                    }
                    if (line.startsWith("binary: ")) {
                        int length = Integer.parseInt(line.substring(8));
                        byte[] buffer = new byte[length];
                        connection.readBinary(buffer, 0, length);
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(MpdCoverService.class.getName(), "Error checking if mpd cover exists", ex);
        } finally {
            connection.disconnect();
        }

        return result;
    }
}
