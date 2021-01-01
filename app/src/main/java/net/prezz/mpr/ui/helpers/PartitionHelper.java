package net.prezz.mpr.ui.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PartitionHelper {

    private static final String PREFERENCE_CURRENT_CLIENT_PARTITION_KEY = "current_client_partition";

    private PartitionHelper() {
        //prevent instantiation
    }

    public static void setClientPartition(Context context, String partition) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREFERENCE_CURRENT_CLIENT_PARTITION_KEY, partition);
        editor.commit();
    }

    public static String getClientPartition(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(PREFERENCE_CURRENT_CLIENT_PARTITION_KEY, "default");
    }
}
