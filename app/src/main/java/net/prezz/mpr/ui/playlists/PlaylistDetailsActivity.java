package net.prezz.mpr.ui.playlists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.prezz.mpr.Utils;
import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.PlayerState;
import net.prezz.mpr.model.PlaylistEntity;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.ResponseResult;
import net.prezz.mpr.model.StoredPlaylistEntity;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.model.UriEntity.FileType;
import net.prezz.mpr.model.UriEntity.UriType;
import net.prezz.mpr.model.command.AddUriToPlaylistCommand;
import net.prezz.mpr.model.command.AddUriToStoredPlaylistCommand;
import net.prezz.mpr.model.command.ClearPlaylistCommand;
import net.prezz.mpr.model.command.Command;
import net.prezz.mpr.model.command.DeleteFromStoredPlaylistCommand;
import net.prezz.mpr.model.command.LoadStoredPlaylistCommand;
import net.prezz.mpr.model.command.MoveInStoredPlaylistCommand;
import net.prezz.mpr.model.command.PlayCommand;
import net.prezz.mpr.model.command.PrioritizeUriCommand;
import net.prezz.mpr.model.command.UpdatePrioritiesCommand;
import net.prezz.mpr.ui.adapter.PlaylistAdapterEntity;
import net.prezz.mpr.ui.adapter.PlaylistArrayAdapter;
import net.prezz.mpr.ui.helpers.Boast;
import net.prezz.mpr.ui.helpers.MiniControlHelper;
import net.prezz.mpr.ui.helpers.ThemeHelper;
import net.prezz.mpr.ui.helpers.VolumeButtonsHelper;
import net.prezz.mpr.ui.view.DataFragment;
import net.prezz.mpr.ui.view.DragListView;
import net.prezz.mpr.ui.view.DragListView.DropListener;
import net.prezz.mpr.ui.view.DragListView.RemoveListener;
import net.prezz.mpr.R;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

public class PlaylistDetailsActivity extends Activity implements OnMenuItemClickListener {

    public static final String PLAYLIST_ARGUMENT_KEY = "playlistArgument";

    private static final String ENTITIES_SAVED_INSTANCE_STATE = "storedPlaylistEntities";

    private PlaylistAdapterEntity[] adapterEntities = null;
    private boolean updating = false;
    private TaskHandle updatingPlaylistsHandle;
    private MiniControlHelper controlHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeHelper.applyTheme(this);
        setContentView(R.layout.activity_playlist_details);
        // Show the Up button in the action bar.
        setupActionBar();
        setupLollipop();

        updatingPlaylistsHandle = TaskHandle.NULL_HANDLE;

        String title = getPlaylistArgument().getPlaylistName();
        setTitle(title);

        DataFragment dataFragment = DataFragment.getRestoreFragment(this, getClass());
        if (dataFragment != null) {
            //restore entities if loaded into memory again (or after rotation)
            Object[] objectEntities = (Object[]) dataFragment.getData(ENTITIES_SAVED_INSTANCE_STATE, null);
            if (objectEntities != null) {
                adapterEntities = Arrays.copyOf(objectEntities, objectEntities.length, PlaylistAdapterEntity[].class);
            }
        }

        controlHelper = new MiniControlHelper(this);

        updateEntities();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        DragListView listView = findListView();
        registerForContextMenu(listView);

        listView.setDropListener(new EntityDropListener());
        listView.setRemoveListener(new EntityRemoveListener());
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
        if (v.getId() == R.id.playlist_details_list_view_browse) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            PlaylistAdapterEntity adapterEntity = adapterEntities[info.position];
            menu.setHeaderTitle(adapterEntity.getText());
            String[] menuItems = getResources().getStringArray(R.array.playlist_details_selected_menu);
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
        PlaylistAdapterEntity adapterEntity = adapterEntities[info.position];
        PlaylistEntity playlistEntity = adapterEntity.getEntity();
        String displayText = getString(R.string.playlist_details_added_to_playlist_toast, adapterEntity.getText());
        switch (item.getItemId()) {
        case 0:
            commandList.add(new AddUriToPlaylistCommand(playlistEntity.getUriEntity()));
            commandList.add(new UpdatePrioritiesCommand());
            sendControlCommands(displayText, commandList);
            return true;
        case 1:
            commandList.add(new PrioritizeUriCommand(playlistEntity.getUriEntity()));
            sendControlCommands(displayText, commandList);
            return true;
        case 2:
            commandList.add(new ClearPlaylistCommand());
            commandList.add(new AddUriToPlaylistCommand(playlistEntity.getUriEntity()));
            sendControlCommands(displayText, commandList);
            return true;
        case 3:
            commandList.add(new ClearPlaylistCommand());
            commandList.add(new AddUriToPlaylistCommand(playlistEntity.getUriEntity()));
            commandList.add(new PlayCommand());
            sendControlCommands(displayText, commandList);
            return true;
        case 4:
            removeTrack(info.position);
            return true;
        case 5:
            removeAlbum(info.position);
            return true;
        }

        return false;
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
               final String[] items = getResources().getStringArray(R.array.playlist_details_choice_menu);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setTitle(getTitle());
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    StoredPlaylistEntity playlistEntity = getPlaylistArgument();
                    String displayText = getString(R.string.playlist_details_loaded_playlist_toast, getTitle());

                    List<Command> commandList = new ArrayList<Command>();
                    switch(item) {
                    case 0:
                        commandList.add(new LoadStoredPlaylistCommand(playlistEntity));
                        commandList.add(new UpdatePrioritiesCommand());
                        sendControlCommands(displayText, commandList);
                        break;
                    case 1:
                        commandList.add(new ClearPlaylistCommand());
                        commandList.add(new LoadStoredPlaylistCommand(playlistEntity));
                        sendControlCommands(displayText, commandList);
                        break;
                    case 2:
                        commandList.add(new ClearPlaylistCommand());
                        commandList.add(new LoadStoredPlaylistCommand(playlistEntity));
                        commandList.add(new PlayCommand());
                        sendControlCommands(displayText, commandList);
                        break;
                    case 3:
                        removeDuplicateTracks();
                        break;
                    case 4:
                        addUrlToPlaylist();
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
            View choiceBarSeperator = findViewById(R.id.playlist_details_choice_bar_seperator);
            choiceBarSeperator.setVisibility(View.GONE);

            View choiceBar = findViewById(R.id.playlist_details_choice_bar);
            choiceBar.setElevation(getResources().getDimension(R.dimen.choice_bar_elevation));

            View controlSeperator = findViewById(R.id.control_layout_seperator);
            controlSeperator.setVisibility(View.GONE);

            View controlLayout = findViewById(R.id.control_layout_mini_control);
            controlLayout.setElevation(getResources().getDimension(R.dimen.choice_bar_elevation));
        }
    }

    private void updateEntities() {
        if (adapterEntities != null) {
            createEntityAdapter(adapterEntities);
            setActivityTitle();
        } else if (!updating) {
            showUpdatingIndicator();
            updatingPlaylistsHandle.cancelTask();
            updatingPlaylistsHandle = MusicPlayerControl.getPlaylistDetails(getPlaylistArgument(), new ResponseReceiver<PlaylistEntity[]>() {
                @Override
                public void receiveResponse(PlaylistEntity[] response) {
                    adapterEntities = createAdapterEntities(response);
                    createEntityAdapter(adapterEntities);
                    hideUpdatingIndicator();
                    setActivityTitle();
                }
            });
        }
    }

    private PlaylistAdapterEntity[] createAdapterEntities(PlaylistEntity[] entities) {
        ArrayList<PlaylistAdapterEntity> result = new ArrayList<PlaylistAdapterEntity>(entities.length);
        for (int i = 0; i < entities.length; i++) {
            result.add(new PlaylistAdapterEntity(entities[i], false));
        }
        return result.toArray(new PlaylistAdapterEntity[result.size()]);
    }

    private void createEntityAdapter(PlaylistAdapterEntity[] adapterEntities) {
        ListView listView = findListView();
        if (listView != null) {
            ListAdapter adapter = createAdapter(adapterEntities);
            listView.setAdapter(adapter);
        }
    }

    private ListAdapter createAdapter(PlaylistAdapterEntity[] adapterEntities) {
        return new PlaylistArrayAdapter(this, android.R.layout.simple_list_item_2, new ArrayList<PlaylistAdapterEntity>(Arrays.asList(adapterEntities)));
    }

    private void sendControlCommands(CharSequence displayText, List<Command> commands) {
        MusicPlayerControl.sendControlCommands(commands);
        Boast.makeText(this, displayText).show();
    }

    private DragListView findListView() {
        return (DragListView)this.findViewById(R.id.playlist_details_list_view_browse);
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
        return (ProgressBar)this.findViewById(R.id.playlist_details_progress_bar_load);
    }

    private StoredPlaylistEntity getPlaylistArgument() {
        return (StoredPlaylistEntity)this.getIntent().getExtras().getSerializable(PLAYLIST_ARGUMENT_KEY);
    }

    private void refreshEntities(boolean refreshLocalEntities) {
        DragListView listView = findListView();
        if (listView != null) {
            if (refreshLocalEntities) {
                //first update the list view with what we assume is the result of the move
                PlaylistArrayAdapter adapter = (PlaylistArrayAdapter)listView.getAdapter();
                adapter.setData(adapterEntities, -1, PlayerState.STOP);
            }

            //the re-query the playlist from server just in case
            updatingPlaylistsHandle.cancelTask();
            updatingPlaylistsHandle = MusicPlayerControl.getPlaylistDetails(getPlaylistArgument(), new ResponseReceiver<PlaylistEntity[]>() {
                @Override
                public void receiveResponse(PlaylistEntity[] response) {
                    adapterEntities = createAdapterEntities(response);
                    DragListView listView = findListView();
                    if (listView != null) {
                        PlaylistArrayAdapter adapter = (PlaylistArrayAdapter)listView.getAdapter();
                        adapter.setData(adapterEntities, -1, PlayerState.STOP);
                    }
                    setActivityTitle();
                }
            });
        }
    }

    private void removeDuplicateTracks() {
        if (adapterEntities != null) {
            List<Integer> toDelete = new ArrayList<Integer>();

            Set<String> uriSet = new HashSet<String>();
            for (int i = 0; i < adapterEntities.length; i++) {
                PlaylistEntity entity = adapterEntities[i].getEntity();
                String uri = entity.getUriEntity().getFullUriPath(false);
                if (uriSet.contains(uri)) {
                    toDelete.add(Integer.valueOf(i));
                } else {
                    uriSet.add(uri);
                }
            }
            uriSet = null;

            if (!toDelete.isEmpty()) {
                List<Command> commands = new ArrayList<Command>(toDelete.size());

                StoredPlaylistEntity playlist = getPlaylistArgument();
                for (int i = toDelete.size() - 1; i >= 0; i--) {
                    Integer pos = toDelete.get(i);
                    commands.add(new DeleteFromStoredPlaylistCommand(playlist, pos.intValue()));
                }

                MusicPlayerControl.sendControlCommands(commands);
                refreshEntities(false);
            }

            Boast.makeText(this, getString(R.string.playlist_details_duplicates_removed_toast, toDelete.size())).show();
        }
    }

    private void addUrlToPlaylist() {
        final EditText editTextView = new EditText(this);

        Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.playlist_details_add_url_title);
        builder.setView(editTextView);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String uri = editTextView.getText().toString();
                if (!Utils.nullOrEmpty(uri)) {
                    AddUriToStoredPlaylistCommand command = new AddUriToStoredPlaylistCommand(getPlaylistArgument(), new UriEntity(UriType.FILE, FileType.MUSIC, "", uri));
                    MusicPlayerControl.sendControlCommand(command, new ResponseReceiver<ResponseResult>() {
                        @Override
                        public void receiveResponse(ResponseResult response) {
                            refreshEntities(false);
                            if (!response.isSuccess()) {
                                Boast.makeText(PlaylistDetailsActivity.this, R.string.playlist_details_added_error_url_toast).show();
                            }
                        }
                    });
                    Boast.makeText(PlaylistDetailsActivity.this, getString(R.string.playlist_details_added_url_toast, uri)).show();
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

    private void setActivityTitle() {
        String title = getPlaylistArgument().getPlaylistName();
        title += getPlaylistTime();
        setTitle(title);
    }

    private String getPlaylistTime() {
        int totalTime = 0;

        if (adapterEntities != null) {
            for (int i = 0; i < adapterEntities.length; i++) {
                Integer time = adapterEntities[i].getEntity().getTime();
                if (time != null) {
                    totalTime += time;
                }
            }
        }

        int hours = totalTime / 3600;
        int remaining = totalTime % 3600;
        int minutes = remaining / 60;
        int seconds = remaining % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(String.format(" (%d:%02d:%02d)", hours, minutes, seconds));
        } else {
            sb.append(String.format(" (%d:%02d)", minutes, seconds));
        }

        return sb.toString();
    }

    private void removeTrack(int which) {
        PlaylistAdapterEntity[] newEntities = new PlaylistAdapterEntity[adapterEntities.length - 1];
        System.arraycopy(adapterEntities, 0, newEntities, 0, which);
        System.arraycopy(adapterEntities, which + 1, newEntities, which, newEntities.length - which);
        MusicPlayerControl.sendControlCommand(new DeleteFromStoredPlaylistCommand(getPlaylistArgument(), which));
        adapterEntities = newEntities;
        refreshEntities(true);
    }

    private void removeAlbum(int which) {
        List<Integer> toDelete = new ArrayList<Integer>();

        String album = adapterEntities[which].getEntity().getAlbum();

        for (int i = 0; i < adapterEntities.length; i++) {
            PlaylistEntity entity = adapterEntities[i].getEntity();
            if (album.equals(entity.getAlbum())) {
                toDelete.add(Integer.valueOf(i));
            }
        }

        if (!toDelete.isEmpty()) {
            List<Command> commands = new ArrayList<Command>(toDelete.size());

            StoredPlaylistEntity playlist = getPlaylistArgument();
            for (int i = toDelete.size() - 1; i >= 0; i--) {
                Integer pos = toDelete.get(i);
                commands.add(new DeleteFromStoredPlaylistCommand(playlist, pos.intValue()));
            }

            MusicPlayerControl.sendControlCommands(commands);
            refreshEntities(false);
        }
    }

    private final class EntityDropListener implements DropListener {

        @Override
        public void drop(int from, int to) {
            PlaylistAdapterEntity movingEntity = adapterEntities[from];
            if (from > to) { //moving up in list
                System.arraycopy(adapterEntities, to, adapterEntities, to + 1, from - to);
                adapterEntities[to] = movingEntity;
                MusicPlayerControl.sendControlCommand(new MoveInStoredPlaylistCommand(getPlaylistArgument(), from, to));
                refreshEntities(true);
            } else if (from < to) { //moving down in list
                System.arraycopy(adapterEntities, from + 1, adapterEntities, from, to - from);
                adapterEntities[to] = movingEntity;
                MusicPlayerControl.sendControlCommand(new MoveInStoredPlaylistCommand(getPlaylistArgument(), from, to));
                refreshEntities(true);
            }
        }
    }

    private final class EntityRemoveListener implements RemoveListener {

        @Override
        public void remove(int which) {
            removeTrack(which);
        }
    }
}
