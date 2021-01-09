package net.prezz.mpr.ui.settings;

import net.prezz.mpr.R;
import net.prezz.mpr.ui.helpers.ThemeHelper;
import net.prezz.mpr.ui.helpers.VolumeButtonsHelper;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.MenuItem;


public class SettingsActivity extends AppCompatActivity {

    private static final String FRAGMENT_TAG = "settings_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeHelper.applyTheme(this);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
            if (fragment == null) {
                fragment = new SettingsFragment();
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_fragment_container, fragment, FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (VolumeButtonsHelper.handleKeyDown(this, keyCode, event)) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
