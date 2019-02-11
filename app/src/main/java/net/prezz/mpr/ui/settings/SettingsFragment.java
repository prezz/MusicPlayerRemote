package net.prezz.mpr.ui.settings;

import java.util.Arrays;

import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.service.PlaybackService;
import net.prezz.mpr.ui.AboutActivity;
import net.prezz.mpr.ui.ApplicationActivator;
import net.prezz.mpr.ui.helpers.Boast;
import net.prezz.mpr.ui.settings.servers.ServersActivity;
import net.prezz.mpr.R;

import android.Manifest;
import android.annotation.TargetApi;
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
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.InputType;

import static android.content.Context.NOTIFICATION_SERVICE;


public class SettingsFragment extends PreferenceFragment {

    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 3003;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_PHONE_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CheckBoxPreference pauseOnPhonePreference = (CheckBoxPreference) findPreference(getString(R.string.settings_behavior_pause_on_phonecall_key));
                    pauseOnPhonePreference.setChecked(true);
                }
                break;
        }
    }

    private void setupSimplePreferencesScreen() {
        addPreferencesFromResource(R.xml.settings_screen);

        setupServersPreferences();
        setupThemePreferences();
        setupProperSortingPreferences();
        setupGracenoteCoverPreferences();
        setupPauseOnPhoneCallPreferences();
        setupNotificationPreferences();
        setupAboutPreferences();

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.settings_default_player_fragment_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.settings_volume_control_amount_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.settings_streaming_player_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.settings_covers_local_path_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.settings_covers_local_port_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.settings_covers_local_file_key)));

    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(bindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's current value.
        bindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    private static Preference.OnPreferenceChangeListener bindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            } else {
                boolean password = false;
                if (preference instanceof EditTextPreference) {
                    int inputMask = ((EditTextPreference)preference).getEditText().getInputType();
                    password = ((inputMask & InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0);
                }
                preference.setSummary(password ? createString(stringValue.length(), '*') : stringValue);
            }
            return true;
        }
    };

    private static String createString(int length, char fillChar) {
        if (length > 0) {
            char[] array = new char[length];
            Arrays.fill(array, fillChar);
            return new String(array);
        }
        return "";
    }

    private void setupServersPreferences() {
        Preference serversPreference = findPreference(getString(R.string.settings_servers_key));
        serversPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), ServersActivity.class);
                startActivity(intent);
                return true;
            }
        });
     }

    private void setupThemePreferences() {
        Preference themePreference = findPreference(getString(R.string.settings_interface_dark_theme_key));
        themePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                getActivity().recreate();
                return true;
            }
        });
    }

    private void setupProperSortingPreferences() {
        Preference notificationPreference = findPreference(getString(R.string.settings_library_proper_sort_key));
        notificationPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
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

    private void setupGracenoteCoverPreferences() {
        Preference gracenotePreference = findPreference(getString(R.string.settings_covers_gracenote_client_id_key));
        gracenotePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Context context = ApplicationActivator.getContext();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                Resources resources = context.getResources();
                editor.putString(resources.getString(R.string.settings_covers_gracenote_user_id_key), "");
                editor.commit();

                return true;
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void setupPauseOnPhoneCallPreferences() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Preference pauseOnPhonePreference = findPreference(getString(R.string.settings_behavior_pause_on_phonecall_key));
            pauseOnPhonePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
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
    }

    private void setupNotificationPreferences() {
        Preference notificationPreference = findPreference(getString(R.string.settings_behavior_show_notification_key));
        notificationPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
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

                return true;
            }
        });
    }

    private void setupAboutPreferences() {
        String version = getVersion();
        String buildTime = ""; //" (" + getBuildTime() + ")";

        Preference aboutPreference = findPreference(getString(R.string.settings_about_key));
        aboutPreference.setSummary(getString(R.string.settings_about_summary) + " " + version + buildTime);

         aboutPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                startActivity(intent);
                 return true;
             }
         });
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

//    private String getBuildTime() {
//        try {
//            Activity activity = getActivity();
//            PackageInfo info = activity.getPackageManager().getPackageInfo(activity.getApplicationContext().getPackageName(), 0);
//            Date date = new Date(info.lastUpdateTime);
//            return date.toString();
//        } catch (Exception e) {
//        }
//
//        return "-";
//    }
}
