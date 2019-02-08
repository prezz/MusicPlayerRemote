package net.prezz.mpr.ui.helpers;

import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.ResponseResult;
import net.prezz.mpr.model.StoredPlaylistEntity;
import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.model.command.AddToNewStoredPlaylistCommand;
import net.prezz.mpr.model.command.AddToStoredPlaylistCommand;
import net.prezz.mpr.model.command.AddUriToNewStoredPlaylistCommand;
import net.prezz.mpr.model.command.AddUriToStoredPlaylistCommand;
import net.prezz.mpr.model.command.Command;
import net.prezz.mpr.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.EditText;

public class AddToStoredPlaylistHelper {
	
	public static void addToStoredPlaylist(final Activity activity, final String displayText, final LibraryEntity entity) {
		addToStoredPlaylist(activity, displayText, new LibraryEntityCommandFactory(entity));
	}	
	
	public static void addUriToStoredPlaylist(final Activity activity, final String displayText, final UriEntity entity) {
		addToStoredPlaylist(activity, displayText, new UriEntityCommandFactory(entity));
	}	

	private static void addToStoredPlaylist(final Activity activity, final String displayText, final CommandFactory commandFactory) {
    	MusicPlayerControl.getStoredPlaylists(new ResponseReceiver<StoredPlaylistEntity[]>() {
			@Override
			public void receiveResponse(final StoredPlaylistEntity[] response) {
				String[] items = new String[response.length + 1];
				items[0] = activity.getString(R.string.library_new_stored_playlist);
				for (int i = 0; i < response.length; i++) {
					items[i+1] = response[i].getPlaylistName();
				}
				
		    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		    	builder.setTitle(R.string.library_add_to_stored_playlist);
		    	builder.setItems(items, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0) {
							addToNewStoredPlaylist(activity, displayText, commandFactory);
						} else {
							MusicPlayerControl.sendControlCommand(commandFactory.createAddToCommand(response[which-1]));
							Boast.makeText(activity, displayText).show();
						}

					}
				});
	        	AlertDialog alert = builder.create();
	        	alert.show();
			}
		});
	}
	
	private static void addToNewStoredPlaylist(final Activity activity, final String displayText, final CommandFactory commandFactory) {
		final EditText editTextView = new EditText(activity);
		editTextView.setSingleLine();
		
		Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.library_new_stored_playlist);
		builder.setView(editTextView);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
		        final String saveName = editTextView.getText().toString();
		        if (!saveName.isEmpty()) {
					MusicPlayerControl.sendControlCommand(commandFactory.createAddToNewCommand(saveName), new ResponseReceiver<ResponseResult>() {
						@Override
						public void receiveResponse(ResponseResult response) {
				    		if (!response.isSuccess()) {
								Boast.makeText(activity, R.string.library_new_stores_playlist_server_error).show();
				    		}
						}
					});
					Boast.makeText(activity, displayText).show();
		        }
		    }
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
		    }
		});
		
		final AlertDialog dialog = builder.create();
		editTextView.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
		        if (hasFocus) {
		            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		        }
			}
		});
		dialog.show();
	}
	
	private interface CommandFactory {
		Command createAddToCommand(StoredPlaylistEntity storedPlaylist);
		Command createAddToNewCommand(String playlistName);
	}
	
	private static final class LibraryEntityCommandFactory implements CommandFactory {

		private final LibraryEntity entity;

		public LibraryEntityCommandFactory(LibraryEntity entity) {
			this.entity = entity;
		}
		
		@Override
		public Command createAddToCommand(StoredPlaylistEntity storedPlaylist) {
			return new AddToStoredPlaylistCommand(storedPlaylist, entity);
		}

		@Override
		public Command createAddToNewCommand(String playlistName) {
			return new AddToNewStoredPlaylistCommand(playlistName, entity);
		}
	}
	
	private static final class UriEntityCommandFactory implements CommandFactory {

		private final UriEntity entity;

		public UriEntityCommandFactory(UriEntity entity) {
			this.entity = entity;
		}
		
		@Override
		public Command createAddToCommand(StoredPlaylistEntity storedPlaylist) {
			return new AddUriToStoredPlaylistCommand(storedPlaylist, entity);
		}

		@Override
		public Command createAddToNewCommand(String playlistName) {
			return new AddUriToNewStoredPlaylistCommand(playlistName, entity);
		}
	}
}
