package net.prezz.mpr.ui.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import net.prezz.mpr.R;
import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.model.servers.ServerConfigurationService;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

public class UriFilterHelper {

    public interface UriFilterChangedListener {
        void entityFilterChanged();
    }

    private static final String PREFERENCE_LIBRARY_VISIBLE_FOLDERS = "library_visible_folders";

    private final Activity activity;
    private final UriFilterChangedListener uriFilterChangedListener;
    private TaskHandle getAllUriFoldersHandle;

    public UriFilterHelper(Activity owner, UriFilterChangedListener uriFilterChangedListener) {
        this.activity = owner;
        this.uriFilterChangedListener = uriFilterChangedListener;
        this.getAllUriFoldersHandle = TaskHandle.NULL_HANDLE;
    }

    public void setUriFilter() {

        getAllUriFoldersHandle.cancelTask();
        getAllUriFoldersHandle = MusicPlayerControl.getHideableUriFolders(new ResponseReceiver<String[]>() {
            @Override
            public void receiveResponse(final String[] items) {

                SortedSet<String> visible = getUriFilter();
                Arrays.sort(items, new SortComparator());

                final boolean[] checked = new boolean[items.length];
                for (int i = 0; i < items.length; i++) {
                    checked[i] = visible.contains(items[i]);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.library_visibility_folder_header);
                builder.setMultiChoiceItems(items, checked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checked[which] = isChecked;
                    }
                });
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SortedSet<String> values = new TreeSet<String>();
                        for (int i = 0; i < items.length; i++) {
                            if (checked[i]) {
                                values.add(items[i]);
                            }
                        }
                        saveUriFilter(values);
                        uriFilterChangedListener.entityFilterChanged();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    public SortedSet<String> getUriFilter() {
        String host = ServerConfigurationService.getSelectedServerConfiguration().getHost();

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        return new TreeSet<String>(sharedPreferences.getStringSet(PREFERENCE_LIBRARY_VISIBLE_FOLDERS + host, Collections.<String>emptySet()));
    }

    private void saveUriFilter(SortedSet<String> values) {
        String host = ServerConfigurationService.getSelectedServerConfiguration().getHost();

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(PREFERENCE_LIBRARY_VISIBLE_FOLDERS + host, values);
        editor.commit();
    }

    private static class SortComparator implements Comparator<String> {

        @Override
        public int compare(String s1, String s2) {
            return s1.compareToIgnoreCase(s2);
        }
    }

    public static void removeUriFilter(Context context, String host) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PREFERENCE_LIBRARY_VISIBLE_FOLDERS + host);
        editor.commit();
    }
}
