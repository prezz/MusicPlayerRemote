package net.prezz.mpr.mpd;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MpdPartitionStore implements MpdPartitionProvider {

    private static final String PREFERENCE_PARTITION_POSTFIX = "_client_partition";

    private final Object lock = new Object();
    private final Context context;
    private final String preferenceKey;
    private String partition;

    public MpdPartitionStore(Context context, MpdSettings mpdSettings) {
        this.context = context;
        this.preferenceKey = mpdSettings.getMpdHost() + mpdSettings.getMpdPort() + PREFERENCE_PARTITION_POSTFIX;
        this.partition = null;
    }

    public void putPartition(String partition) {
        synchronized (lock) {
            this.partition = null; // force load in get method

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (partition == null) {
                editor.remove(preferenceKey);
            } else {
                editor.putString(preferenceKey, partition);
            }
            editor.commit();
        }
    }

    @Override
    public String getPartition() {
        synchronized (lock) {
            if (partition == null) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                partition = sharedPreferences.getString(preferenceKey, DEFAULT_PARTITION);
            }
        }

        return partition;
    }

    @Override
    public void onInvalidPartition() {
        putPartition(null);
    }
}
