package net.prezz.mpr.mpd;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MpdPartitionStore {

    private static final String PREFERENCE_PARTITION_POSTFIX = "_client_partition";

    private final Context context;
    private final String preferenceKey;
    private String partition;

    public MpdPartitionStore(Context context, MpdSettings mpdSettings) {
        this.context = context;
        this.preferenceKey = mpdSettings.getMpdHost() + mpdSettings.getMpdPort() + PREFERENCE_PARTITION_POSTFIX;
        this.partition = null;
    }

    public void savePartition(String partition) {
        this.partition = null;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferenceKey, partition);
        editor.commit();
    }

    public String getPartition() {
        if (partition == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            partition = sharedPreferences.getString(preferenceKey, "default");
        }

        return partition;
    }
}
