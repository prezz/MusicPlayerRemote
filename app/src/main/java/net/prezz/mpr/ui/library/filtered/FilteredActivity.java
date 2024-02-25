package net.prezz.mpr.ui.library.filtered;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import net.prezz.mpr.ui.helpers.AddToStoredPlaylistHelper;
import net.prezz.mpr.ui.helpers.Boast;
import net.prezz.mpr.ui.helpers.MiniControlHelper;
import net.prezz.mpr.R;
import net.prezz.mpr.ui.helpers.ThemeHelper;
import net.prezz.mpr.ui.helpers.VolumeButtonsHelper;
import net.prezz.mpr.ui.view.DataFragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
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

public abstract class FilteredActivity extends AppCompatActivity implements OnItemClickListener, OnMenuItemClickListener {

    public static final String TITLE_ARGUMENT_KEY = "title";
    public static final String ENTITY_ARGUMENT_KEY = "entity";

    private static final String ENTITIES_SAVED_INSTANCE_STATE = "adapterEntities";

    protected AdapterEntity[] adapterEntities = null;
    private boolean updating = false;
    private TaskHandle getFromLibraryHandle;
    private MiniControlHelper controlHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeHelper.applyTheme(this);
        setContentView(getLayout());

        getFromLibraryHandle = TaskHandle.NULL_HANDLE;

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
    protected void onPause() {
        super.onPause();

        controlHelper.hideVisibility();
    }

    @Override
    public void onStop() {
        super.onStop();

        getFromLibraryHandle.cancelTask();
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
                AddToStoredPlaylistHelper.addToStoredPlaylist(this, displayText, libraryEntity);
                return true;
            }
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
                        commandList.add(new AddToPlaylistCommand(getEntityArgument()));
                        commandList.add(new UpdatePrioritiesCommand());
                        sendControlCommands(displayText, commandList);
                        break;
                    case 1:
                        commandList.add(new PrioritizeCommand(getEntityArgument()));
                        sendControlCommands(displayText, commandList);
                        break;
                    case 2:
                        commandList.add(new ClearPlaylistCommand());
                        commandList.add(new AddToPlaylistCommand(getEntityArgument()));
                        sendControlCommands(displayText, commandList);
                        break;
                    case 3:
                        commandList.add(new ClearPlaylistCommand());
                        commandList.add(new AddToPlaylistCommand(getEntityArgument()));
                        commandList.add(new PlayCommand());
                        sendControlCommands(displayText, commandList);
                        break;
                    case 4:
                        AddToStoredPlaylistHelper.addToStoredPlaylist(FilteredActivity.this, displayText, getEntityArgument());
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

    protected String[] getContextMenuItems(LibraryEntity entity) {
            return getResources().getStringArray(R.array.library_selected_menu);
    }

    protected AdapterEntity getAdapterEntity(int pos) {
        return adapterEntities[pos];
    }

    protected LibraryEntity getEntityArgument() {
        return this.getIntent().getExtras().getSerializable(ENTITY_ARGUMENT_KEY, LibraryEntity.class);
    }

    protected abstract int getLayout();

    protected abstract TaskHandle getEntities(LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver);

    protected abstract AdapterEntity[] createAdapterEntities(LibraryEntity[] entities);

    protected abstract ListAdapter createAdapter(AdapterEntity[] adapterEntities);

    private void sendControlCommands(CharSequence displayText, List<Command> commands) {
        MusicPlayerControl.sendControlCommands(commands);
        Boast.makeText(this, displayText).show();
    }

    private void updateEntities() {
        if (adapterEntities != null) {
            createEntityAdapter(adapterEntities);
        } else if (!updating) {
            final LibraryEntity entity = getEntityArgument();
            if (entity != null) {
                showUpdatingIndicator();
                getFromLibraryHandle.cancelTask();
                getFromLibraryHandle = getEntities(entity, new ResponseReceiver<LibraryEntity[]>() {
                    @Override
                    public void receiveResponse(LibraryEntity[] response) {
                        adapterEntities = createAdapterEntities(response);
                        createEntityAdapter(adapterEntities);
                        hideUpdatingIndicator();
                    }
                });
            }
        }
    }

    private void createEntityAdapter(AdapterEntity[] adapterEntities) {
        ListView listView = findListView();
        if (listView != null) {
            ListAdapter adapter = createAdapter(adapterEntities);
            listView.setAdapter(adapter);
        }
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
