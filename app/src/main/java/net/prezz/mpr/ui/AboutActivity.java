package net.prezz.mpr.ui;

import net.prezz.mpr.R;
import net.prezz.mpr.ui.helpers.ThemeHelper;
import net.prezz.mpr.ui.helpers.VolumeButtonsHelper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeHelper.applyTheme(this);
        setContentView(R.layout.activity_about);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (VolumeButtonsHelper.handleKeyDown(this, keyCode, event)) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void onPrivacyPolicyClick(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://mpr-privacy-policy.prezz.net"));
        startActivity(browserIntent);
    }

    public void onLastfmClick(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.last.fm"));
        startActivity(browserIntent);
    }

    public void onSourceCodeClick(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/prezz/MusicPlayerRemote"));
        startActivity(browserIntent);
    }
}
