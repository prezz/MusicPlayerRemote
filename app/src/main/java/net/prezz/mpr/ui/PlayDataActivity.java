package net.prezz.mpr.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import net.prezz.mpr.R;
import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.ui.helpers.Boast;
import net.prezz.mpr.ui.helpers.ThemeHelper;
import net.prezz.mpr.ui.helpers.VolumeButtonsHelper;

import java.util.Optional;

public class PlayDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeHelper.applyTheme(this);
        setContentView(R.layout.activity_play_data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.play_data, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            case R.id.play_data_action_clear: {
                clearPlayData();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (VolumeButtonsHelper.handleKeyDown(this, keyCode, event)) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void onImportClick(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(R.string.play_data_import_header);
        builder.setMessage(R.string.play_data_import_message);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText editTextCsv = (EditText) findViewById(R.id.play_data_csv_text);
                MusicPlayerControl.importPlayData(editTextCsv.getText().toString(), new ResponseReceiver<Boolean>() {
                    @Override
                    public void receiveResponse(Boolean response) {
                        if (response == Boolean.TRUE) {
                            Boast.makeText(PlayDataActivity.this, R.string.play_data_import_success_toast).show();
                        } else {
                            Boast.makeText(PlayDataActivity.this, R.string.play_data_import_failed_toast).show();
                        }
                    }
                });
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onExportClick(View view) {

        final int defaultLimit = 10000;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 10, 40, 10);

        EditText pageText = new EditText(this);
        pageText.setHint(R.string.play_data_export_page_hint);
        pageText.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(pageText);

        EditText limitText = new EditText(this);
        limitText.setHint(getString(R.string.play_data_export_limit_hint, defaultLimit));
        limitText.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(limitText);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.play_data_export_button);
        builder.setView(layout);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String pageString = pageText.getText().toString();
                String limitString = limitText.getText().toString();
                int page = pageString.isBlank() ? 0 : Integer.parseInt(pageString);
                int limit = limitString.isBlank() ? defaultLimit : Integer.parseInt(limitString);
                int offset = page * limit;

                MusicPlayerControl.exportPlayData(offset, limit, new ResponseReceiver<String>() {
                    @Override
                    public void receiveResponse(String response) {
                        if (response != null) {
                            EditText editTextCsv = (EditText) findViewById(R.id.play_data_csv_text);
                            editTextCsv.setText(response);

                            long lines = response.lines().count() - 1;
                            Boast.makeText(PlayDataActivity.this, getString(R.string.play_data_export_count_toast, lines)).show();
                        }
                    }
                });
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void clearPlayData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(R.string.play_data_clear_header);
        builder.setMessage(R.string.play_data_clear_message);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MusicPlayerControl.clearPlayData(new ResponseReceiver<Boolean>() {
                    @Override
                    public void receiveResponse(Boolean response) {
                        if (response == Boolean.TRUE) {
                            Boast.makeText(PlayDataActivity.this, R.string.play_data_clear_success_toast).show();
                        } else {
                            Boast.makeText(PlayDataActivity.this, R.string.play_data_clear_failed_toast).show();
                        }
                    }
                });
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
