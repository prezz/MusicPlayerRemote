package net.prezz.mpr.ui.library.filtered;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.StoredPlaylistEntity;
import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.model.UriEntity.FileType;
import net.prezz.mpr.model.UriEntity.UriType;
import net.prezz.mpr.model.command.AddUriToPlaylistCommand;
import net.prezz.mpr.model.command.ClearPlaylistCommand;
import net.prezz.mpr.model.command.Command;
import net.prezz.mpr.model.command.LoadStoredPlaylistCommand;
import net.prezz.mpr.model.command.PlayCommand;
import net.prezz.mpr.model.command.PrioritizeUriCommand;
import net.prezz.mpr.model.command.UpdatePrioritiesCommand;
import net.prezz.mpr.ui.adapter.AdapterEntity;
import net.prezz.mpr.ui.adapter.LibraryArrayAdapter;
import net.prezz.mpr.ui.adapter.SectionAdapterEntity;
import net.prezz.mpr.ui.adapter.SectionSortedAdapterIndexStrategy;
import net.prezz.mpr.ui.adapter.UriAdapterEntity;
import net.prezz.mpr.ui.helpers.AddToStoredPlaylistHelper;
import net.prezz.mpr.ui.helpers.Boast;
import net.prezz.mpr.ui.helpers.MiniControlHelper;
import net.prezz.mpr.R;
import net.prezz.mpr.ui.helpers.ThemeHelper;
import net.prezz.mpr.ui.helpers.VolumeButtonsHelper;
import net.prezz.mpr.ui.view.DataFragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

public class FilteredUriActivity extends Activity implements OnItemClickListener, OnMenuItemClickListener {

	public static final String TITLE_ARGUMENT_KEY = "title";
	public static final String ENTITY_ARGUMENT_KEY = "entity";

	private static final String ENTITIES_SAVED_INSTANCE_STATE = "adapterEntities";


	private AdapterEntity[] adapterEntities = null;
	private boolean updating = false;
	private MiniControlHelper controlHelper;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ThemeHelper.applyTheme(this);
		setContentView(R.layout.activity_filtered);
		// Show the Up button in the action bar.
		setupActionBar();
        setupLollipop();
		
		String title = this.getIntent().getExtras().getString(TITLE_ARGUMENT_KEY);
		setTitle(title);

        DataFragment dataFragment = DataFragment.getRestoreFragment(this, getClass());
		if (dataFragment != null) {
			//restore entities if loaded into memory again (or after rotation)
			Object[] objectEntities = (Object[]) dataFragment.getData(ENTITIES_SAVED_INSTANCE_STATE, null);
			if (objectEntities != null) {
				adapterEntities = Arrays.copyOf(objectEntities, objectEntities.length, AdapterEntity[].class);
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
		if (v.getId() == R.id.filtered_list_view_browse) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			AdapterEntity entity = adapterEntities[info.position];
			if (entity instanceof UriAdapterEntity) {
                FileType fileType = ((UriAdapterEntity) entity).getEntity().getFileType();
				menu.setHeaderTitle(entity.getText());
                String[] menuItems = getResources().getStringArray((fileType == FileType.PLAYLIST) ? R.array.library_playlist_selected_menu : R.array.library_selected_menu);
				for (int i = 0; i < menuItems.length; i++) {
					MenuItem menuItem = menu.add(Menu.NONE, i, i, menuItems[i]);
					menuItem.setOnMenuItemClickListener(this);
				}
			}
		}
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		List<Command> commandList = new ArrayList<Command>();
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		AdapterEntity entity = adapterEntities[info.position];
		if (entity instanceof UriAdapterEntity) {
			UriEntity uriEntity = ((UriAdapterEntity) entity).getEntity();
			String displayText = getString(R.string.library_added_to_playlist_toast, entity.getText());
            int itemId = item.getItemId();
            if (uriEntity.getFileType() == FileType.PLAYLIST && itemId > 0) {
                itemId += 1;
            }
			switch (itemId) {
			case 0:
				if (uriEntity.getFileType() == UriEntity.FileType.PLAYLIST) {
					commandList.add(new LoadStoredPlaylistCommand(new StoredPlaylistEntity(uriEntity.getFullUriPath(false))));
				} else {
					commandList.add(new AddUriToPlaylistCommand(uriEntity));
				}
				commandList.add(new UpdatePrioritiesCommand());
				sendControlCommands(displayText, commandList);
				return true;
			case 1:
				commandList.add(new PrioritizeUriCommand(uriEntity));
				sendControlCommands(displayText, commandList);
				return true;
			case 2:
				commandList.add(new ClearPlaylistCommand());
                if (uriEntity.getFileType() == UriEntity.FileType.PLAYLIST) {
                    commandList.add(new LoadStoredPlaylistCommand(new StoredPlaylistEntity(uriEntity.getFullUriPath(false))));
                } else {
                    commandList.add(new AddUriToPlaylistCommand(uriEntity));
                }
				sendControlCommands(displayText, commandList);
				return true;
			case 3:
				commandList.add(new ClearPlaylistCommand());
                if (uriEntity.getFileType() == UriEntity.FileType.PLAYLIST) {
                    commandList.add(new LoadStoredPlaylistCommand(new StoredPlaylistEntity(uriEntity.getFullUriPath(false))));
                } else {
                    commandList.add(new AddUriToPlaylistCommand(uriEntity));
                }
				commandList.add(new PlayCommand());
				sendControlCommands(displayText, commandList);
				return true;
	        case 4:
	        	AddToStoredPlaylistHelper.addUriToStoredPlaylist(this, displayText, uriEntity);
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
		AdapterEntity adapterEntity = adapterEntities[position];
		
		if (adapterEntity instanceof UriAdapterEntity) {
			UriEntity entity = ((UriAdapterEntity) adapterEntity).getEntity();
			if (entity.getUriType() == UriType.DIRECTORY) {
		    	Intent intent = new Intent(this, FilteredUriActivity.class);
				Bundle args = new Bundle();
				args.putString(FilteredUriActivity.TITLE_ARGUMENT_KEY, entity.getFullUriPath(false));
				args.putSerializable(FilteredUriActivity.ENTITY_ARGUMENT_KEY, entity);
				intent.putExtras(args);
		    	startActivity(intent);
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if (VolumeButtonsHelper.handleKeyDown(this, keyCode, event)) {
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	public void onChoiceMenuClick(View view) {
	   	final CharSequence title = getTitle();
	   	final String[] items = getResources().getStringArray(R.array.library_selected_menu);
	   	for (int i = 0; i < items.length; i++) {
	   		items[i] = String.format(items[i], title);
	   	}
		
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(title);
    	builder.setItems(items, new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int item) {
    	    	if (adapterEntities != null) {
		    		List<Command> commandList = new ArrayList<Command>();
	    	        String displayText = getString(R.string.library_added_to_playlist_toast, title);
					switch(item) {
	    	        case 0:
	    	        	commandList.add(new AddUriToPlaylistCommand(getEntityArgument()));
	    				commandList.add(new UpdatePrioritiesCommand());
	    				sendControlCommands(displayText, commandList);
	    	        	break;
	    	        case 1:
	    				commandList.add(new PrioritizeUriCommand(getEntityArgument()));
	    				sendControlCommands(displayText, commandList);
	    	        	break;
	    	        case 2:
	    				commandList.add(new ClearPlaylistCommand());
	    	        	commandList.add(new AddUriToPlaylistCommand(getEntityArgument()));
	    				sendControlCommands(displayText, commandList);
	    	        	break;
	    	        case 3:
	    				commandList.add(new ClearPlaylistCommand());
	    	        	commandList.add(new AddUriToPlaylistCommand(getEntityArgument()));
	    				commandList.add(new PlayCommand());
	    				sendControlCommands(displayText, commandList);
	    	        	break;
	    	        case 4:
	    	        	AddToStoredPlaylistHelper.addUriToStoredPlaylist(FilteredUriActivity.this, displayText, getEntityArgument());
	    	        	break;
	    	        }
    	    	}
    	    }
    	});
    	AlertDialog alert = builder.create();
    	alert.show();
	}
	
    public void onControlMenuClick(View view) {
    	controlHelper.toggleVisibility();
    }
	
	@Override
	protected void onPause() {
		super.onPause();
		
		controlHelper.hideVisibility();
	}
    
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void setupLollipop() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			View choiceBarSeperator = findViewById(R.id.filtered_choice_bar_seperator);
			choiceBarSeperator.setVisibility(View.GONE);
        	
			View choiceBar = findViewById(R.id.filtered_choice_bar);
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
			final UriEntity entity = getEntityArgument();
			if (entity != null) {
				showUpdatingIndicator();
				MusicPlayerControl.getUriFromLibrary(entity, null, new ResponseReceiver<UriEntity[]>() {
					@Override
					public void receiveResponse(UriEntity[] response) {
						adapterEntities = createAdapterEntities(response);
						createEntityAdapter(adapterEntities);
						hideUpdatingIndicator();
					}
				});
			}
		}
	}
	
	private AdapterEntity[] createAdapterEntities(UriEntity[] entities) {
		boolean addDirSection = true;
		boolean addFileSection = true;
		ArrayList<AdapterEntity> result = new ArrayList<AdapterEntity>(entities.length + 2);
		
		for (int i = 0; i < entities.length; i++) {
			if (entities[i].getUriType() == UriType.DIRECTORY) {
				if (addDirSection) {
					result.add(new SectionAdapterEntity(getString(R.string.library_directories_section)));
				}
				addDirSection = false;
				result.add(new UriAdapterEntity(entities[i]));
			}
			if (entities[i].getUriType() == UriType.FILE) {
				if (addFileSection) {
					result.add(new SectionAdapterEntity(getString(R.string.library_files_section)));
				}
				addFileSection = false;
				result.add(new UriAdapterEntity(entities[i]));
			}
		}
		
		return result.toArray(new AdapterEntity[result.size()]);
	}
	
	private void createEntityAdapter(AdapterEntity[] adapterEntities) {
		ListView listView = findListView();
		if (listView != null) {
			ListAdapter adapter = createAdapter(adapterEntities);
			listView.setAdapter(adapter);
		}
	}
	
	private ListAdapter createAdapter(AdapterEntity[] adapterEntities) {
		return new LibraryArrayAdapter(this, android.R.layout.simple_list_item_1, adapterEntities, SectionSortedAdapterIndexStrategy.INSTANCE, false);
	}
	
	private UriEntity getEntityArgument() {
		return (UriEntity)this.getIntent().getExtras().getSerializable(ENTITY_ARGUMENT_KEY);
	}
			
	private ListView findListView() {
		return (ListView)this.findViewById(R.id.filtered_list_view_browse);
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
		return (ProgressBar)this.findViewById(R.id.filtered_progress_bar_load);
	}
}
