package net.prezz.mpr.ui.settings;

import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.service.PlaybackService;
import net.prezz.mpr.ui.AboutActivity;
import net.prezz.mpr.ui.ApplicationActivator;
import net.prezz.mpr.ui.helpers.Boast;
import net.prezz.mpr.ui.settings.servers.ServersActivity;
import net.prezz.mpr.R;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import static android.content.Context.NOTIFICATION_SERVICE;


public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 3003;
    private static final int PERMISSIONS_REQUEST_POST_NOTIFICATIONS = 3004;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setupSimplePreferencesScreen(rootKey);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_PHONE_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CheckBoxPreference pauseOnPhonePreference = (CheckBoxPreference) findPreference(getString(R.string.settings_behavior_pause_on_phonecall_key));
                    pauseOnPhonePreference.setChecked(true);
                }
                break;
            case PERMISSIONS_REQUEST_POST_NOTIFICATIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CheckBoxPreference showNotificationPreference = (CheckBoxPreference) findPreference(getString(R.string.settings_behavior_show_notification_key));
                    showNotificationPreference.setChecked(true);
                    handleNotificationPreference(Boolean.TRUE);
                }
                break;
        }
    }

    private void setupSimplePreferencesScreen(String rootKey) {
        setPreferencesFromResource(R.xml.settings_screen, rootKey);

        setupServersPreferences();
        setupThemePreferences();
        setupProperSortingPreferences();
        setupPauseOnPhoneCallPreferences();
        setupNotificationPreferences();
        setupAboutPreferences();

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.settings_default_player_fragment_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.settings_volume_control_amount_key)));

    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(bindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's current value.
        bindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    private static Preference.OnPreferenceChangeListener bindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            if (preference instanceof ListPreference) {
                String stringValue = value.toString();
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            }
            return true;
        }
    };

    private void setupServersPreferences() {
        Preference serversPreference = findPreference(getString(R.string.settings_servers_key));
        serversPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), ServersActivity.class);
                startActivity(intent);
                return true;
            }
        });
     }

    private void setupThemePreferences() {
        Preference themePreference = findPreference(getString(R.string.settings_interface_dark_theme_key));
        themePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                getActivity().recreate();
                return true;
            }
        });
    }

    private void setupProperSortingPreferences() {
        Preference notificationPreference = findPreference(getString(R.string.settings_library_proper_sort_key));
        notificationPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MusicPlayerControl.deleteLocalLibraryDatabase(new ResponseReceiver<Boolean>() {
                    @Override
                    public void receiveResponse(Boolean response) {
                        if (response == Boolean.TRUE) {
                            Boast.makeText(getActivity(), R.string.settings_delete_library_database_toast).show();
                        }
                    }
                });
                return true;
            }
        });
    }

    private void setupPauseOnPhoneCallPreferences() {
        Preference pauseOnPhonePreference = findPreference(getString(R.string.settings_behavior_pause_on_phonecall_key));
        pauseOnPhonePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (Boolean.TRUE == newValue) {
                    Context context = getContext();
                    if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSIONS_REQUEST_READ_PHONE_STATE);
                        return false;
                    }
                }
                return true;
            }
        });
    }

    private void setupNotificationPreferences() {
        Preference notificationPreference = findPreference(getString(R.string.settings_behavior_show_notification_key));
        notificationPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (Boolean.TRUE == newValue) {
                    Context context = getContext();
                    if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSIONS_REQUEST_POST_NOTIFICATIONS);
                        return false;
                    }
                }

                handleNotificationPreference(newValue);
                return true;
            }
        });
    }

    private void setupAboutPreferences() {
        String version = getVersion();

        Preference aboutPreference = findPreference(getString(R.string.settings_about_key));
        aboutPreference.setSummary(getString(R.string.settings_about_summary) + " " + version);

         aboutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                startActivity(intent);
                 return true;
             }
         });
     }

    private void handleNotificationPreference(Object newValue) {

        if (Boolean.FALSE.equals(newValue)) {
            PlaybackService.stop();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
            String channelId = getString(R.string.notification_media_player_channel_id);

            if (Boolean.TRUE.equals(newValue)) {
                NotificationChannel channel = new NotificationChannel(channelId, getString(R.string.notification_media_player_channel_name), NotificationManager.IMPORTANCE_LOW);
                notificationManager.createNotificationChannel(channel);
            } else {
                notificationManager.deleteNotificationChannel(channelId);
            }
        }
    }

    private String getVersion() {
         try {
            Activity activity = getActivity();
            PackageInfo info = activity.getPackageManager().getPackageInfo(activity.getApplicationContext().getPackageName(), 0);
            return info.versionName ;
         } catch (Exception ex) {
         }

         return "-";
     }
}
