package net.prezz.mpr.ui;

import net.prezz.mpr.R;
import net.prezz.mpr.ui.helpers.LyngdorfHelper;
import net.prezz.mpr.ui.helpers.ThemeHelper;
import net.prezz.mpr.ui.helpers.VolumeButtonsHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

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

    public void onHiddenLyngdorfClick(View view) {
        showHiddenLyngdorfInput(this, "Secret Lyngdorf setting", LyngdorfHelper.getLyngdorfIp(this));
    }

    private void showHiddenLyngdorfInput(Activity activity, String titleText, String value) {

        final EditText editTextView = new EditText(activity);
        editTextView.setSingleLine();
        editTextView.setText(value);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(titleText);
        builder.setView(editTextView);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String inputText = editTextView.getText().toString();
                LyngdorfHelper.setLyngdorfIp(AboutActivity.this, inputText);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        final AlertDialog dialog = builder.create();
        editTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        dialog.show();
    }
}
