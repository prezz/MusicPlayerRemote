package net.prezz.mpr.model.servers;

import java.io.File;

import net.prezz.mpr.Utils;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;
import net.prezz.mpr.ui.ApplicationActivator;
import net.prezz.mpr.ui.helpers.UriFilterHelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.preference.PreferenceManager;

public class ServerConfigurationService {

    public static final String SELECTED_SERVER_CONFIGURATION_KEY = "selectedServerKey";
    private static ServerConfigurationDatabaseHelper databaseHelper = new ServerConfigurationDatabaseHelper(ApplicationActivator.getContext());


    private ServerConfigurationService() {
        //prevent instantiation
    }

    public static ServerConfiguration getSelectedServerConfiguration() {
        Context context = ApplicationActivator.getContext();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int selectedServerId = sharedPreferences.getInt(SELECTED_SERVER_CONFIGURATION_KEY, 0);

        ServerConfiguration[] serverConfigurations = getServerConfigurations();
        if (serverConfigurations.length > 0) {
            for (ServerConfiguration configuration : serverConfigurations) {
                if (configuration.getId() == selectedServerId) {
                    return configuration;
                }
            }

            return serverConfigurations[0];
        }

        return new ServerConfiguration("", "", "6600", "", "");
    }

    public static void setSelectedServerConfiguration(ServerConfiguration configuration) {
        Context context = ApplicationActivator.getContext();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        editor.putInt(SELECTED_SERVER_CONFIGURATION_KEY, configuration.getId());
        editor.commit();
    }

    public static ServerConfiguration[] getServerConfigurations() {
        Cursor c = databaseHelper.getServers();

        try {
            int i = 0;
            ServerConfiguration[] result = new ServerConfiguration[c.getCount()];
            if (c.moveToFirst()) {
                do {
                    int id = Integer.valueOf(c.getInt(0));
                    String name = c.getString(1);
                    String host = c.getString(2);
                    String port = c.getString(3);
                    String password = c.getString(4);
                    String unused = c.getString(5);
                    String streaming = c.getString(6);
                    result[i++] = new ServerConfiguration(id, name, host, port, password, streaming);
                } while (c.moveToNext());
            }
            return result;
        } finally {
            c.close();
            databaseHelper.close();
        }
    }

    public static void addServerConfiguration(ServerConfiguration configuration) {
        databaseHelper.addServer(configuration.getName(), configuration.getHost(), configuration.getPort(), configuration.getPassword(), "", configuration.getStreaming());
        databaseHelper.close();

        ServerConfiguration[] serverConfigurations = getServerConfigurations();
        if (serverConfigurations.length == 1) {
            setSelectedServerConfiguration(serverConfigurations[0]);
        }
    }

    public static void updateServerConfiguration(ServerConfiguration configuration) {
        String oldHost = getHost(configuration.getId());
        databaseHelper.updateServer(configuration.getId(), configuration.getName(), configuration.getHost(), configuration.getPort(), configuration.getPassword(), "", configuration.getStreaming());
        databaseHelper.close();
        if (!Utils.equals(oldHost, configuration.getHost())) {
            deleteOrphanLibraryDatabase(oldHost);
        }
    }

    public static void deleteServerConfiguration(ServerConfiguration configuration) {
        Context context = ApplicationActivator.getContext();

        String oldHost = getHost(configuration.getId());
        databaseHelper.deleteServer(configuration.getId());
        databaseHelper.close();
        deleteOrphanLibraryDatabase(oldHost);
        UriFilterHelper.removeUriFilter(context, oldHost);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int selectedServerId = sharedPreferences.getInt(SELECTED_SERVER_CONFIGURATION_KEY, 0);
        if (selectedServerId == configuration.getId()) {
            ServerConfiguration[] serverConfigurations = getServerConfigurations();
            if (serverConfigurations.length > 0) {
                setSelectedServerConfiguration(serverConfigurations[0]);
            }
        }
    }

    private static String getHost(int id) {
        String host = null;

        try {
            Cursor c = databaseHelper.getServerHost(id);
            try {
                if (c.moveToFirst()) {
                    host = c.getString(0);
                }
            } finally {
                c.close();
                //don't close databaseHelper here, this will be done by update and delete
            }
        } catch (Exception ex) {
        }

        return host;
    }

    private static void deleteOrphanLibraryDatabase(String host) {
        if (host != null) {
            try {
                Context context = ApplicationActivator.getContext();
                File dbToDelete = context.getDatabasePath(host + MpdLibraryDatabaseHelper.LIBRARY_FILE_DB_POSTFIX);
                if (dbToDelete != null && dbToDelete.exists()) {
                    context.deleteDatabase(dbToDelete.getAbsolutePath());
                }
            } catch (Exception ex) {
            }
        }
    }
}
