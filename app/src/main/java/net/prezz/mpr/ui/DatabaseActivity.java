package net.prezz.mpr.ui;

import net.prezz.mpr.R;
import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.Statistics;
import net.prezz.mpr.model.command.UpdateLibraryCommand;
import net.prezz.mpr.ui.helpers.Boast;
import net.prezz.mpr.ui.helpers.ThemeHelper;
import net.prezz.mpr.ui.helpers.VolumeButtonsHelper;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class DatabaseActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ThemeHelper.applyTheme(this);
		setContentView(R.layout.activity_database);
		
		refresh();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if (VolumeButtonsHelper.handleKeyDown(this, keyCode, event)) {
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	public void onUpdateClick(View view) {
		MusicPlayerControl.sendControlCommand(new UpdateLibraryCommand());
		
		TextView textViewRunning = (TextView) findViewById(R.id.database_text_running);
		textViewRunning.setText(getString(R.string.database_refresh_text_running));
	}
	
	public void onRefreshClick(View view) {
		refresh();
	}

	public void onDeleteClick(View view) {
    	MusicPlayerControl.deleteLocalLibraryDatabase(new ResponseReceiver<Boolean>() {
			@Override
			public void receiveResponse(Boolean response) {
				if (response == Boolean.TRUE) {
					Boast.makeText(DatabaseActivity.this, R.string.database_delete_toast).show();
				}
			}
		});
	}
	
	public void refresh() {
		MusicPlayerControl.getStatistics(new ResponseReceiver<Statistics>() {
			@Override
			public void receiveResponse(Statistics response) {
				if (response != null) {
					TextView textViewStats = (TextView) findViewById(R.id.database_text_statistics);
					textViewStats.setText(getString(R.string.database_refresh_text_statistics, response.getArtists(), response.getAlbums(), response.getSongs()));
					
					TextView textViewRunning = (TextView) findViewById(R.id.database_text_running);
					if (response.getUpdatingJob() != null) {
						textViewRunning.setText(getString(R.string.database_refresh_text_running));
					} else {
						textViewRunning.setText("");
					}
				}
			}
		});
	}
}
