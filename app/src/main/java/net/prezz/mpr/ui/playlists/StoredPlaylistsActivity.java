package net.prezz.mpr.ui.playlists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.prezz.mpr.Utils;
import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.ResponseResult;
import net.prezz.mpr.model.StoredPlaylistEntity;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.model.command.ClearPlaylistCommand;
import net.prezz.mpr.model.command.Command;
import net.prezz.mpr.model.command.DeleteStoredPlaylistCommand;
import net.prezz.mpr.model.command.LoadStoredPlaylistCommand;
import net.prezz.mpr.model.command.PlayCommand;
import net.prezz.mpr.model.command.SaveCurrentPlaylistCommand;
import net.prezz.mpr.model.command.UpdatePrioritiesCommand;
import net.prezz.mpr.ui.adapter.StoredPlaylistAdapterEntity;
import net.prezz.mpr.ui.helpers.Boast;
import net.prezz.mpr.ui.helpers.MiniControlHelper;
import net.prezz.mpr.R;
import net.prezz.mpr.ui.helpers.ThemeHelper;
import net.prezz.mpr.ui.helpers.VolumeButtonsHelper;
import net.prezz.mpr.ui.view.DataFragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

public class StoredPlaylistsActivity extends Activity implements OnItemClickListener, OnMenuItemClickListener {

	private static final String ENTITIES_SAVED_INSTANCE_STATE = "storedPlaylists";

	private final RefreshEntitiesResponseReceiver refreshResponseReceiver = new RefreshEntitiesResponseReceiver();
	
	private StoredPlaylistAdapterEntity[] adapterEntities = null;
	private boolean updating = false;
	private TaskHandle updatingPlaylistsHandle;
	private MiniControlHelper controlHelper;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ThemeHelper.applyTheme(this);
		setContentView(R.layout.activity_stored_playlists);
		// Show the Up button in the action bar.
		setupActionBar();
        setupLollipop();
		
		updatingPlaylistsHandle = TaskHandle.NULL_HANDLE;

		DataFragment dataFragment = DataFragment.getRestoreFragment(this, getClass());
		if (dataFragment != null) {
			//restore entities if loaded into memory again (or after rotation)
			Object[] objectEntities = (Object[]) dataFragment.getData(ENTITIES_SAVED_INSTANCE_STATE, null);
			if (objectEntities != null) {
				adapterEntities = Arrays.copyOf(objectEntities, objectEntities.length, StoredPlaylistAdapterEntity[].class);
			}
		}

		controlHelper = new MiniControlHelper(this);

		updateEntities();
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		ListView listView = findListView();
		listView.setOnItemClickListener(this);
		registerForContextMenu(listView);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		controlHelper.hideVisibility();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		updatingPlaylistsHandle.cancelTask();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		DataFragment dataFragment = DataFragment.getSaveFragment(this, getClass());
		if (dataFragment != null) {
			dataFragment.setData(ENTITIES_SAVED_INSTANCE_STATE, adapterEntities);
		}

		super.onSaveInstanceState(outState);
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
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.stored_playlists_list_view_browse) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			StoredPlaylistAdapterEntity entity = adapterEntities[info.position];
			menu.setHeaderTitle(entity.toString());
			String[] menuItems = getResources().getStringArray(R.array.stored_playlists_selected_menu);
			for (int i = 0; i < menuItems.length; i++) {
				MenuItem menuItem = menu.add(Menu.NONE, i, i, menuItems[i]);
				menuItem.setOnMenuItemClickListener(this);
			}
		}
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		List<Command> commandList = new ArrayList<Command>();
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		StoredPlaylistAdapterEntity adapterEntity = adapterEntities[info.position];
		StoredPlaylistEntity playlistEntity = adapterEntity.getEntity();
		String displayText = getString(R.string.stored_playlists_loaded_playlist_toast, adapterEntity.toString());
		switch (item.getItemId()) {
		case 0:
			commandList.add(new LoadStoredPlaylistCommand(playlistEntity));
			commandList.add(new UpdatePrioritiesCommand());
			sendControlCommands(displayText, commandList);
			return true;
		case 1:
			commandList.add(new ClearPlaylistCommand());
			commandList.add(new LoadStoredPlaylistCommand(playlistEntity));
			sendControlCommands(displayText, commandList);
			return true;
		case 2:
			commandList.add(new ClearPlaylistCommand());
			commandList.add(new LoadStoredPlaylistCommand(playlistEntity));
			commandList.add(new PlayCommand());
			sendControlCommands(displayText, commandList);
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
		StoredPlaylistAdapterEntity storedPlaylist = adapterEntities[position];
    	Intent intent = new Intent(this, PlaylistDetailsActivity.class);
		Bundle args = new Bundle();
		args.putSerializable(PlaylistDetailsActivity.PLAYLIST_ARGUMENT_KEY, storedPlaylist.getEntity());
		intent.putExtras(args);
    	startActivity(intent);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if (VolumeButtonsHelper.handleKeyDown(this, keyCode, event)) {
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	public void onChoiceMenuClick(View view) {
		if (adapterEntities != null) {
		   	final String[] items = getResources().getStringArray(R.array.stored_playlists_choice_menu);
			
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	 		builder.setTitle(getTitle());
	    	builder.setItems(items, new DialogInterface.OnClickListener() {
	    	    public void onClick(DialogInterface dialog, int item) {
					switch(item) {
	    	        case 0:
	    	            savePlaylist(items[0]);
	    	        	break;
	    	        case 1:
	    	        	deletePlaylists(items[1]);
	    	        	break;
	    	        }
	    	    }
	    	});
	    	AlertDialog alert = builder.create();
	    	alert.show();
		}
	}
	
    public void onControlMenuClick(View view) {
    	controlHelper.toggleVisibility();
    }
	
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void setupLollipop() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			View choiceBarSeperator = findViewById(R.id.stored_playlists_choice_bar_seperator);
			choiceBarSeperator.setVisibility(View.GONE);
        	
			View choiceBar = findViewById(R.id.stored_playlists_choice_bar);
			choiceBar.setElevation(getResources().getDimension(R.dimen.choice_bar_elevation));
			
			View controlSeperator = findViewById(R.id.control_layout_seperator);
			controlSeperator.setVisibility(View.GONE);
			
			View controlLayout = findViewById(R.id.control_layout_mini_control);
			controlLayout.setElevation(getResources().getDimension(R.dimen.choice_bar_elevation));
        }
	}

	private void sendControlCommands(CharSequence displayText, List<Command> commands) {
		MusicPlayerControl.sendControlCommands(commands);
		Boast.makeText(this, displayText).show();
	}
	
	private void updateEntities() {
		if (adapterEntities != null) {
			createEntityAdapter(adapterEntities);
		} else if (!updating) {
			showUpdatingIndicator();
			updatingPlaylistsHandle.cancelTask();
			updatingPlaylistsHandle = MusicPlayerControl.getStoredPlaylists(new ResponseReceiver<StoredPlaylistEntity[]>() {
				@Override
				public void receiveResponse(StoredPlaylistEntity[] response) {
					adapterEntities = createAdapterEntities(response);
					createEntityAdapter(adapterEntities);
					hideUpdatingIndicator();
				}
			});
		}
	}
		
	private StoredPlaylistAdapterEntity[] createAdapterEntities(StoredPlaylistEntity[] entities) {
		ArrayList<StoredPlaylistAdapterEntity> result = new ArrayList<StoredPlaylistAdapterEntity>(entities.length);
		for (int i = 0; i < entities.length; i++) {
			result.add(new StoredPlaylistAdapterEntity(entities[i]));
		}
		return result.toArray(new StoredPlaylistAdapterEntity[result.size()]);
	}
	
	private void createEntityAdapter(StoredPlaylistAdapterEntity[] adapterEntities) {
		ListView listView = findListView();
		if (listView != null) {
			ListAdapter adapter = createAdapter(adapterEntities);
			listView.setAdapter(adapter);
		}
	}
	
	private ListAdapter createAdapter(StoredPlaylistAdapterEntity[] adapterEntities) {
		return new ArrayAdapter<StoredPlaylistAdapterEntity>(this, android.R.layout.simple_list_item_1, new ArrayList<StoredPlaylistAdapterEntity>(Arrays.asList(adapterEntities)));
	}
		
	private ListView findListView() {
		return (ListView)this.findViewById(R.id.stored_playlists_list_view_browse);
	}
	
	private void showUpdatingIndicator() {
		updating = true;
		ProgressBar progressBar = findProgressBar();
		progressBar.setVisibility(View.VISIBLE);
	}
	
	private void hideUpdatingIndicator() {
		updating = false;
		ProgressBar progressBar = findProgressBar();
		if (progressBar != null) {
			progressBar.setVisibility(View.GONE);
		}
	}
	
	private ProgressBar findProgressBar() {
		return (ProgressBar)this.findViewById(R.id.stored_playlists_progress_bar_load);
	}
	
	private void savePlaylist(String header) {
		final EditText editTextView = new EditText(StoredPlaylistsActivity.this);
		editTextView.setSingleLine();
		
		Builder builder = new AlertDialog.Builder(StoredPlaylistsActivity.this);
		builder.setTitle(header);
		builder.setView(editTextView);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
		        final String saveName = editTextView.getText().toString();
		        if (!saveName.isEmpty()) {
			        final StoredPlaylistEntity existing = getExistingPlaylistEntity(saveName);
			        if (existing != null) {
			    		Builder alertBuilder = new AlertDialog.Builder(StoredPlaylistsActivity.this);
			    		alertBuilder.setTitle(R.string.stored_playlist_file_exist_title);
			    		alertBuilder.setMessage(getString(R.string.stored_playlist_file_exist_message, saveName));
			    		alertBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			    		    public void onClick(DialogInterface dialog, int whichButton) {
					        	updatingPlaylistsHandle.cancelTask();
					        	List<Command> commandList = new ArrayList<Command>(Arrays.asList(new DeleteStoredPlaylistCommand(existing), new SaveCurrentPlaylistCommand(saveName)));
					        	updatingPlaylistsHandle = MusicPlayerControl.sendControlCommands(commandList, refreshResponseReceiver);
			    		    }
			    		});
			    		alertBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			    		    public void onClick(DialogInterface dialog, int whichButton) {
			    		    }
			    		});
			    		alertBuilder.show();
			        } else {
			        	updatingPlaylistsHandle.cancelTask();
						updatingPlaylistsHandle = MusicPlayerControl.sendControlCommand(new SaveCurrentPlaylistCommand(saveName), refreshResponseReceiver);
			        }
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
	
    private void deletePlaylists(String header) {
    	if (adapterEntities.length == 0) {
			Boast.makeText(this, R.string.stored_playlist_nothing_to_delete_toast).show();
    	} else {
			String[] items = new String[adapterEntities.length];
			final boolean[] checkedItems = new boolean[adapterEntities.length];
			for (int i = 0; i < adapterEntities.length; i++) {
				items[i] = adapterEntities[i].getEntity().getPlaylistName();
				checkedItems[i] = false;
			}
			
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setTitle(header);
	    	builder.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					checkedItems[which] = isChecked;
				}
			});
	    	builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					List<Command> commands = new ArrayList<Command>();
					for (int i = 0; i < adapterEntities.length; i++) {
						if (checkedItems[i]) {
							StoredPlaylistEntity entity = adapterEntities[i].getEntity();
							commands.add(new DeleteStoredPlaylistCommand(entity));
						}
					}
					if (!commands.isEmpty()) {
						updatingPlaylistsHandle.cancelTask();
						updatingPlaylistsHandle = MusicPlayerControl.sendControlCommands(commands, refreshResponseReceiver);
					}
				}
			});
	    	AlertDialog alert = builder.create();
	    	alert.show();
    	}
	}
    
    private StoredPlaylistEntity getExistingPlaylistEntity(String name) {
		for (int i = 0; i < adapterEntities.length; i++) {
			StoredPlaylistEntity entity = adapterEntities[i].getEntity();
			if (name.equals(entity.getPlaylistName())) {
				return entity;
			}
		}
		return null;
    }

    private final class RefreshEntitiesResponseReceiver extends ResponseReceiver<ResponseResult> {

		@Override
		public void receiveResponse(ResponseResult response) {
    		if (!response.isSuccess()) {
				Boast.makeText(StoredPlaylistsActivity.this, R.string.stored_playlist_server_error_toast).show();
    		}
    		
			updatingPlaylistsHandle.cancelTask();
			updatingPlaylistsHandle = MusicPlayerControl.getStoredPlaylists(new ResponseReceiver<StoredPlaylistEntity[]>() {
				@Override
				public void receiveResponse(StoredPlaylistEntity[] response) {
					adapterEntities = createAdapterEntities(response);
			    	ListView listView = findListView();
				    if (listView != null) {
				    	ArrayAdapter<StoredPlaylistAdapterEntity> arrayAdapter = Utils.cast(listView.getAdapter());
				    	arrayAdapter.setNotifyOnChange(false);
				    	arrayAdapter.clear();
				        arrayAdapter.addAll(adapterEntities);
				        arrayAdapter.notifyDataSetChanged();
				    }
				}
			});
		}
    }
}
