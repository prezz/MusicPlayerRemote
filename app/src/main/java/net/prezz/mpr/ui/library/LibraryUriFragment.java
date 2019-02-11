package net.prezz.mpr.ui.library;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.StoredPlaylistEntity;
import net.prezz.mpr.model.TaskHandle;
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
import net.prezz.mpr.ui.library.filtered.FilteredUriActivity;
import net.prezz.mpr.R;
import net.prezz.mpr.ui.view.DataFragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

public class LibraryUriFragment extends Fragment implements LibraryCommonsFragment, OnItemClickListener, OnMenuItemClickListener {

    private static final int FRAGMENT_POSITION = 3;
    private static final String ENTITIES_SAVED_INSTANCE_STATE = "UriEntities";

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

        boolean uriFilterChanged = ((LibraryActivity)getActivity()).attachFragment(this, FRAGMENT_POSITION);
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

        ((LibraryActivity)getActivity()).detachFragment(FRAGMENT_POSITION);
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
                AddToStoredPlaylistHelper.addUriToStoredPlaylist(getActivity(), displayText, uriEntity);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,    long id) {
        AdapterEntity adapterEntity = adapterEntities[position];

        if (adapterEntity instanceof UriAdapterEntity) {
            UriEntity entity = ((UriAdapterEntity) adapterEntity).getEntity();
            if (entity.getUriType() == UriType.DIRECTORY) {
                Intent intent = new Intent(getActivity(), FilteredUriActivity.class);
                Bundle args = new Bundle();
                args.putString(FilteredUriActivity.TITLE_ARGUMENT_KEY, entity.getFullUriPath(false));
                args.putSerializable(FilteredUriActivity.ENTITY_ARGUMENT_KEY, entity);
                intent.putExtras(args);
                startActivity(intent);
            }
        }
    }

    public void onChoiceMenuClick(View view) {
        final String[] items = getResources().getStringArray(R.array.library_uri_root_menu);
           final CharSequence title = getActivity().getTitle();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         builder.setTitle(title);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (adapterEntities != null) {
                    List<Command> commandList = new ArrayList<Command>();
                    String displayText = getString(R.string.library_added_to_playlist_toast, title);
                    switch(item) {
                    case 0:
                        commandList.add(new ClearPlaylistCommand());
                        commandList = addAll(commandList);
                        sendControlCommands(displayText, commandList);
                        break;
                    case 1:
                        commandList.add(new ClearPlaylistCommand());
                        commandList = addAll(commandList);
                        commandList.add(new PlayCommand());
                        sendControlCommands(displayText, commandList);
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

    private List<Command> addAll(List<Command> commandList) {
        List<UriEntity> entities = new ArrayList<UriEntity>(adapterEntities.length);
        for (AdapterEntity adapterEntity : adapterEntities) {
            if (adapterEntity instanceof UriAdapterEntity) {
                entities.add(((UriAdapterEntity)adapterEntity).getEntity());
            }
        }
        commandList.add(new AddUriToPlaylistCommand(entities.toArray(new UriEntity[entities.size()])));
        return commandList;
    }

    private void sendControlCommands(CharSequence displayText, List<Command> commands) {
        MusicPlayerControl.sendControlCommands(commands);
        Boast.makeText(getActivity(), displayText).show();
    }

    private void updateEntities() {
        if (adapterEntities != null) {
            createEntityAdapter(adapterEntities);
        } else if (!updating) {
            showUpdatingIndicator();
            Set<String> hiddenUriFolders = ((LibraryActivity) getActivity()).getUriFilter();
            getFromLibraryHandle.cancelTask();
            getFromLibraryHandle = MusicPlayerControl.getUriFromLibrary(null, hiddenUriFolders, new ResponseReceiver<UriEntity[]>() {
                @Override
                public void receiveResponse(UriEntity[] response) {
                    adapterEntities = createAdapterEntities(response);
                    createEntityAdapter(adapterEntities);
                    hideUpdatingIndicator();
                }
            });
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

    private LibraryArrayAdapter createAdapter(AdapterEntity[] adapterEntities) {
        return new LibraryArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, adapterEntities, SectionSortedAdapterIndexStrategy.INSTANCE, false);
    }

    private ListView findListView() {
        View view = getView();
        //if swiping fast left and right the view might actually be destroyed when the response returns
        return (view != null) ? (ListView)view.findViewById(R.id.library_list_view_browse) : null;
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
