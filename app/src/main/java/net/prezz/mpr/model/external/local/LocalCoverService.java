package net.prezz.mpr.model.external.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import net.prezz.mpr.R;
import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.model.external.CoverService;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;
import net.prezz.mpr.ui.ApplicationActivator;
import net.prezz.mpr.ui.mpd.MpdPlayerSettings;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocalCoverService implements CoverService {

    @Override
    public List<String> getCoverUrls(String artist, String album) {
        try {
            Context context = ApplicationActivator.getContext();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            Resources resources = context.getResources();
            boolean enabled = sharedPreferences.getBoolean(resources.getString(R.string.settings_covers_local_enabled_key), false);

            if (enabled && album != null) {
                MpdPlayerSettings mpdSettings = MpdPlayerSettings.create(context);
                String mpdHost = mpdSettings.getMpdHost();

                Set<String> directories = getDirectories(context, mpdHost, artist, album);
                List<URI> uris = getUris(mpdHost, directories, sharedPreferences, resources);
                List<String> result = getValidUrls(uris);
                return result;
            }
        } catch (Exception ex) {
            Log.e(LocalCoverService.class.getName(), "Error getting covers from Local cover service", ex);
        }

        return Collections.emptyList();
    }

    private static Set<String> getDirectories(Context context, String mpdHost, String artist, String album) {
        Set<String> result = new HashSet<String>();

        MpdLibraryDatabaseHelper databaseHelper = new MpdLibraryDatabaseHelper(context, mpdHost);
        try {
            Cursor c = databaseHelper.findUris(artist, album);
            try {
                if (c.moveToFirst()) {
                    do {
                        String uri = c.getString(0);
                        String dir = uri.substring(0, uri.lastIndexOf(UriEntity.DIR_SEPERATOR));
                        result.add(dir);
                    } while (c.moveToNext());
                }
            } finally {
                c.close();
            }
        } finally {
            databaseHelper.close();
        }

        return result;
    }

    private static List<URI> getUris(String mpdHost, Set<String> directories, SharedPreferences sharedPreferences, Resources resources) {
        List<URI> result = new ArrayList<URI>();

        String port = sharedPreferences.getString(resources.getString(R.string.settings_covers_local_port_key), "80");
        String path = sharedPreferences.getString(resources.getString(R.string.settings_covers_local_path_key), "/");
        String filename = sharedPreferences.getString(resources.getString(R.string.settings_covers_local_file_key), "Folder.jpg");

        path = path.replaceAll("^/", "").replaceAll("/$", "");

        for (String dir : directories) {
            try {
                Uri.Builder builder = new Uri.Builder();
                String fullPath = builder.scheme("http")
                        .encodedAuthority(mpdHost + ":" + port)
                        .appendEncodedPath(path)
                        .appendEncodedPath(dir)
                        .appendEncodedPath(filename)
                        .build().toString();

                URL url = new URL(fullPath);
                URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                result.add(uri);
            } catch (Exception ex) {
                Log.e(LocalCoverService.class.getName(), "Error encoding url", ex);
            }
        }

        return result;
    }

    private static List<String> getValidUrls(List<URI> uris) {
        List<String> result = new ArrayList<String>();

        for (URI uri : uris) {
            if (uriExists(uri)) {
                result.add(uri.toASCIIString());
            }
        }

        return result;
    }

    private static boolean uriExists(URI uri) {
        try {
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("HEAD");
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                if (connection.getContentLength() > 0) {
                    return true;
                }
            }
        } catch (Exception ex) {
            Log.e(LocalCoverService.class.getName(), "Error checking if url exists", ex);
        }
        return false;
    }
}
