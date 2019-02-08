package net.prezz.mpr.ui.library;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.model.command.AddToPlaylistCommand;
import net.prezz.mpr.model.command.ClearPlaylistCommand;
import net.prezz.mpr.model.command.Command;
import net.prezz.mpr.model.command.PlayCommand;
import net.prezz.mpr.model.command.PrioritizeCommand;
import net.prezz.mpr.model.command.UpdatePrioritiesCommand;
import net.prezz.mpr.ui.adapter.AdapterEntity;
import net.prezz.mpr.ui.adapter.LibraryAdapterEntity;
import net.prezz.mpr.ui.adapter.LibraryArrayAdapter;
import net.prezz.mpr.ui.helpers.AddToStoredPlaylistHelper;
import net.prezz.mpr.R;
import net.prezz.mpr.ui.helpers.Boast;
import net.prezz.mpr.ui.view.DataFragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

public abstract class LibraryFragment extends Fragment implements LibraryCommonsFragment, OnItemClickListener, OnMenuItemClickListener {
	
	private static final String ENTITIES_SAVED_INSTANCE_STATE = "libraryEntities";
	
	private AdapterEntity[] adapterEntities = null;
	private boolean updating = false;
	private TaskHandle getFromLibraryHandle;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getFromLibraryHandle = TaskHandle.NULL_HANDLE;

        DataFragment dataFragment = DataFragment.getRestoreFragment(getActivity(), getClass());
		if (dataFragment != null) {
			Object[] objectEntities = (Object[]) dataFragment.getData(ENTITIES_SAVED_INSTANCE_STATE, null);
			if (objectEntities != null) {
				adapterEntities = Arrays.copyOf(objectEntities, objectEntities.length, AdapterEntity[].class);
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_library, container, false);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		boolean uriFilterChanged = ((LibraryActivity) getActivity()).attachFragment(this, getFragmentPosition());
		if (uriFilterChanged) {
		    adapterEntities = null;
        }
		
		ListView listView = findListView();
		listView.setOnItemClickListener(this);
		registerForContextMenu(listView);

		//ensure to call before onViewStateRestored (i think) as the scroll position then will be restored.
		//if calling it after, the scroll position will be reset as the list is re-populated after the scroll is restored
		updateEntities();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		if (updating) {
			showUpdatingIndicator();
		}
	}

	@Override
	public void onDestroyView() {
		getFromLibraryHandle.cancelTask();
		
		((LibraryActivity)getActivity()).detachFragment(getFragmentPosition());
		super.onDestroyView();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
        DataFragment dataFragment = DataFragment.getSaveFragment(getActivity(), getClass());
		if (dataFragment != null) {
			dataFragment.setData(ENTITIES_SAVED_INSTANCE_STATE, adapterEntities);
		}

		super.onSaveInstanceState(outState);
	}
		
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.library_list_view_browse) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			AdapterEntity entity = adapterEntities[info.position];
			if (entity instanceof LibraryAdapterEntity) {
				menu.setHeaderTitle(entity.getText());
				String[] menuItems = getContextMenuItems(((LibraryAdapterEntity) entity).getEntity());
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
		if (entity instanceof LibraryAdapterEntity) {
			LibraryEntity libraryEntity = ((LibraryAdapterEntity) entity).getEntity();
			String displayText = getString(R.string.library_added_to_playlist_toast, entity.getText());
			switch (item.getItemId()) {
			case 0:
				commandList.add(new AddToPlaylistCommand(libraryEntity));
				commandList.add(new UpdatePrioritiesCommand());
				sendControlCommands(displayText, commandList);
				return true;
			case 1:
				commandList.add(new PrioritizeCommand(libraryEntity));
				sendControlCommands(displayText, commandList);
				return true;
			case 2:
				commandList.add(new ClearPlaylistCommand());
				commandList.add(new AddToPlaylistCommand(libraryEntity));
				sendControlCommands(displayText, commandList);
				return true;
			case 3:
				commandList.add(new ClearPlaylistCommand());
				commandList.add(new AddToPlaylistCommand(libraryEntity));
				commandList.add(new PlayCommand());
				sendControlCommands(displayText, commandList);
				return true;
			case 4:
				AddToStoredPlaylistHelper.addToStoredPlaylist(getActivity(), displayText, libraryEntity);
				return true;
			}
		}
		
		return false;
	}
	
	public void onChoiceMenuClick(View view) {
		final String[] items = getResources().getStringArray(R.array.library_root_menu);
	   	final CharSequence title = getActivity().getTitle();
		
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		builder.setTitle(title);
    	builder.setItems(items, new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int item) {
    	    	if (adapterEntities != null) {
		    		List<Command> commandList = new ArrayList<Command>();
	    	        switch(item) {
	    	        case 0:
                        List<LibraryAdapterEntity> libraryAdapterEntities = new ArrayList<>(adapterEntities.length);
                        for (AdapterEntity adapterEntity : adapterEntities) {
                            if (adapterEntity instanceof LibraryAdapterEntity) {
                                libraryAdapterEntities.add((LibraryAdapterEntity) adapterEntity);
                            }
                        }

	    	        	if (libraryAdapterEntities.size() > 0) {
		    	        	Random rand = new Random();
                            int idx = rand.nextInt(libraryAdapterEntities.size());
		    	        	LibraryAdapterEntity libraryEntity = libraryAdapterEntities.get(idx);
		    	        	String displayText = getString(R.string.library_added_to_playlist_toast, libraryEntity.getText());
		    				commandList.add(new ClearPlaylistCommand());
		    				commandList.add(new AddToPlaylistCommand(libraryEntity.getEntity()));
		    				sendControlCommands(displayText, commandList);
	    	        	}
	    	        	break;
	    	        case 1: {
			    			String displayText = getString(R.string.library_added_to_playlist_toast, title);
		    				commandList.add(new ClearPlaylistCommand());
		    				commandList = addAll(commandList);
		    				sendControlCommands(displayText, commandList);
		    	        }
	    	        	break;
	    	        case 2: {
	    	        		String displayText = getString(R.string.library_added_to_playlist_toast, title);
		    				commandList.add(new ClearPlaylistCommand());
		    				commandList = addAll(commandList);
		    				commandList.add(new PlayCommand());
		    				sendControlCommands(displayText, commandList);
		    	        }
	    	        	break;
	    	        }
    	    	}
    	    }
    	});
    	AlertDialog alert = builder.create();
    	alert.show();
	}

	@Override
	public void uriFilterChanged() {
        adapterEntities = null;
        updateEntities();
    }
	
	protected AdapterEntity getAdapterEntity(int pos) {
		return adapterEntities[pos];
	}
	
	protected List<Command> addAll(List<Command> commandList) {
		List<LibraryEntity> entities = new ArrayList<LibraryEntity>(adapterEntities.length);
		for (AdapterEntity adapterEntity : adapterEntities) {
			if (adapterEntity instanceof LibraryAdapterEntity) {
				entities.add(((LibraryAdapterEntity)adapterEntity).getEntity());
			}
		}
		commandList.add(new AddToPlaylistCommand(entities.toArray(new LibraryEntity[entities.size()])));
		return commandList;
	}

	protected void sendControlCommands(CharSequence displayText, List<Command> commands) {
		MusicPlayerControl.sendControlCommands(commands);
		Boast.makeText(getActivity(), displayText).show();
	}
	
	protected String[] getContextMenuItems(LibraryEntity entity) {
		return getResources().getStringArray(R.array.library_selected_menu);
	}
	
	protected abstract int getFragmentPosition();
	
	protected abstract TaskHandle getEntities(ResponseReceiver<LibraryEntity[]> responseReceiver);
	
	protected abstract AdapterEntity[] createAdapterEntities(LibraryEntity[] entities);
	
	protected abstract LibraryArrayAdapter createAdapter(AdapterEntity[] adapterEntities);
			
	private ListView findListView() {
		View view = getView();
		//if swiping fast left and right the view might actually be destroyed when the response returns
		return (view != null) ? (ListView)view.findViewById(R.id.library_list_view_browse) : null;
	}
	
	private void updateEntities() {
		if (adapterEntities != null) {
			createEntityAdapter(adapterEntities);
		} else if (!updating) {
			showUpdatingIndicator();
			getFromLibraryHandle.cancelTask();
			getFromLibraryHandle = getEntities(new ResponseReceiver<LibraryEntity[]>() {
				@Override
				public void buildingDatabase() {
					Boast.makeText(getActivity(), R.string.database_build_building_database_toast).show();
				}

				@Override
				public void receiveResponse(LibraryEntity[] response) {
					adapterEntities = createAdapterEntities(response);
					createEntityAdapter(adapterEntities);
					hideUpdatingIndicator();
					
					((LibraryActivity)getActivity()).verifyBuildDatabase();
				}
			});
		}
	}
	
	private void createEntityAdapter(AdapterEntity[] adapterEntities) {
		ListView listView = findListView();
		//if swiping fast left and right the view might actually be destroyed when the response returns
		if (listView != null) {
		    boolean notify = (listView.getAdapter() != null);
            LibraryArrayAdapter adapter = createAdapter(adapterEntities);
            listView.setAdapter(adapter);
            if (notify) {
                adapter.notifyDataSetChanged();
            }
		}
	}
	
	private void showUpdatingIndicator() {
		updating = true;
		ProgressBar progressBar = findProgressBar();
		if (progressBar != null) {
			progressBar.setVisibility(View.VISIBLE);
		}
	}
	
	private void hideUpdatingIndicator() {
		updating = false;
		ProgressBar progressBar = findProgressBar();
		if (progressBar != null) {
			progressBar.setVisibility(View.GONE);
		}
	}
	
	private ProgressBar findProgressBar() {
		View view = getView();
		return (view != null) ? (ProgressBar)view.findViewById(R.id.library_progress_bar_load) : null;
	}
}
