package net.prezz.mpr.ui.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.prezz.mpr.Utils;
import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.LibraryEntity.Tag;
import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.SearchResult;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.model.UriEntity.UriType;
import net.prezz.mpr.model.command.AddToPlaylistCommand;
import net.prezz.mpr.model.command.AddUriToPlaylistCommand;
import net.prezz.mpr.model.command.ClearPlaylistCommand;
import net.prezz.mpr.model.command.Command;
import net.prezz.mpr.model.command.PlayCommand;
import net.prezz.mpr.model.command.PrioritizeCommand;
import net.prezz.mpr.model.command.PrioritizeUriCommand;
import net.prezz.mpr.model.command.UpdatePrioritiesCommand;
import net.prezz.mpr.mpd.database.MpdDatabaseBuilder;
import net.prezz.mpr.mpd.database.MpdDatabaseBuilder.UpdateDatabaseResult;
import net.prezz.mpr.ui.adapter.AdapterEntity;
import net.prezz.mpr.ui.adapter.AlbumAdapterEntity;
import net.prezz.mpr.ui.adapter.ArtistAdapterEntity;
import net.prezz.mpr.ui.adapter.LibraryAdapterEntity;
import net.prezz.mpr.ui.adapter.LibraryArrayAdapter;
import net.prezz.mpr.ui.adapter.SearchTitleAdapterEntity;
import net.prezz.mpr.ui.adapter.SectionAdapterEntity;
import net.prezz.mpr.ui.adapter.SectionSortedAdapterIndexStrategy;
import net.prezz.mpr.ui.adapter.UriAdapterEntity;
import net.prezz.mpr.ui.helpers.Boast;
import net.prezz.mpr.ui.helpers.MiniControlHelper;
import net.prezz.mpr.ui.helpers.ThemeHelper;
import net.prezz.mpr.ui.helpers.UriFilterHelper;
import net.prezz.mpr.ui.helpers.VolumeButtonsHelper;
import net.prezz.mpr.ui.library.filtered.FilteredAlbumAndTitleActivity;
import net.prezz.mpr.ui.library.filtered.FilteredTrackAndTitleActivity;
import net.prezz.mpr.ui.library.filtered.FilteredUriActivity;
import net.prezz.mpr.R;
import net.prezz.mpr.ui.view.DataFragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.NavUtils;
import androidx.preference.PreferenceManager;

import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;

public class SearchActivity extends Activity implements OnItemClickListener, OnMenuItemClickListener, SearchView.OnQueryTextListener, UriFilterHelper.UriFilterChangedListener {

    private static final String ACTIVITY_TITLE_SAVED_INSTANCE_STATE = "activityTitle";
    private static final String FOCUS_SEARCH_SAVED_INSTANCE_STATE = "focusSearch";
    private static final String ENTITIES_SAVED_INSTANCE_STATE = "adapterEntities";

    private String activityTitle;
    private Boolean setSearchFocus = Boolean.TRUE;
    private AdapterEntity[] adapterEntities = null;
    private TaskHandle searchLibraryHandle;
    private MiniControlHelper controlHelper;
    private UriFilterHelper uriFilterHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeHelper.applyTheme(this);
        setContentView(R.layout.activity_search);
        setupActionBar();
        setupLollipop();

        searchLibraryHandle = TaskHandle.NULL_HANDLE;

        DataFragment dataFragment = DataFragment.getRestoreFragment(this, getClass());
        if (dataFragment != null) {
            //restore entities if loaded into memory again (or after rotation)
            activityTitle = (String) dataFragment.getData(ACTIVITY_TITLE_SAVED_INSTANCE_STATE, null);
            setSearchFocus = (Boolean) dataFragment.getData(FOCUS_SEARCH_SAVED_INSTANCE_STATE, Boolean.FALSE);

            Object[] objectEntities = (Object[]) dataFragment.getData(ENTITIES_SAVED_INSTANCE_STATE, null);
            if (objectEntities != null) {
                adapterEntities = Arrays.copyOf(objectEntities, objectEntities.length, AdapterEntity[].class);
            }
        }

        if (activityTitle != null) {
            setTitle(activityTitle);
        }

        if (adapterEntities != null) {
            createEntityAdapter(adapterEntities);
        }

        controlHelper = new MiniControlHelper(this);
        uriFilterHelper = new UriFilterHelper(this, this);

        //since search is only possible from within this activity the handling if the intent is actually not necessary
        //we just keep it anyway if we later want to search from other activities.
        handleSearch(getIntent());
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

        searchLibraryHandle.cancelTask();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        DataFragment dataFragment = DataFragment.getSaveFragment(this, getClass());
        if (dataFragment != null) {
            dataFragment.setData(ACTIVITY_TITLE_SAVED_INSTANCE_STATE, getTitle());

            SearchView searchView = (SearchView) this.findViewById(R.id.search_action_search);
            if (searchView != null) {
                dataFragment.setData(FOCUS_SEARCH_SAVED_INSTANCE_STATE, Boolean.valueOf(searchView.hasFocus()));
            }

            dataFragment.setData(ENTITIES_SAVED_INSTANCE_STATE, adapterEntities);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setFocusable(true);
        searchView.setOnQueryTextListener(this);
        if (setSearchFocus == Boolean.TRUE) {
            searchView.setIconified(false);
            searchView.requestFocus();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            case R.id.search_action_search: {
                onSearchRequested();
                return true;
            }
            case R.id.search_action_hide_folders: {
                uriFilterHelper.setUriFilter();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.search_list_view_browse) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            AdapterEntity entity = adapterEntities[info.position];
            if (entity instanceof LibraryAdapterEntity || entity instanceof UriAdapterEntity) {
                menu.setHeaderTitle(entity.getText());
                String[] menuItems = getResources().getStringArray(R.array.search_selected_menu);
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
            String displayText = getString(R.string.search_added_to_playlist_toast, entity.getText());
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
            }
        } else if (entity instanceof UriAdapterEntity) {
            UriEntity uriEntity = ((UriAdapterEntity) entity).getEntity();
            String displayText = getString(R.string.search_added_to_playlist_toast, entity.getText());
            switch (item.getItemId()) {
            case 0:
                commandList.add(new AddUriToPlaylistCommand(uriEntity));
                commandList.add(new UpdatePrioritiesCommand());
                sendControlCommands(displayText, commandList);
                return true;
            case 1:
                commandList.add(new PrioritizeUriCommand(uriEntity));
                sendControlCommands(displayText, commandList);
                return true;
            case 2:
                commandList.add(new ClearPlaylistCommand());
                commandList.add(new AddUriToPlaylistCommand(uriEntity));
                sendControlCommands(displayText, commandList);
                return true;
            case 3:
                commandList.add(new ClearPlaylistCommand());
                commandList.add(new AddUriToPlaylistCommand(uriEntity));
                commandList.add(new PlayCommand());
                sendControlCommands(displayText, commandList);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,    long id) {
        AdapterEntity adapterEntity = adapterEntities[position];
        if (adapterEntity instanceof ArtistAdapterEntity) {
            LibraryEntity entity = ((ArtistAdapterEntity)adapterEntity).getEntity();
            Intent intent = new Intent(this, FilteredAlbumAndTitleActivity.class);
            Bundle args = new Bundle();
            args.putString(FilteredAlbumAndTitleActivity.TITLE_ARGUMENT_KEY, adapterEntity.getText());
            args.putSerializable(FilteredAlbumAndTitleActivity.ENTITY_ARGUMENT_KEY, entity);
            intent.putExtras(args);
            startActivity(intent);
        } else if (adapterEntity instanceof AlbumAdapterEntity) {
            LibraryEntity entity = ((AlbumAdapterEntity)adapterEntity).getEntity();
            Intent intent = new Intent(this, FilteredTrackAndTitleActivity.class);
            Bundle args = new Bundle();
            args.putString(FilteredTrackAndTitleActivity.TITLE_ARGUMENT_KEY, adapterEntity.getText());
            args.putSerializable(FilteredTrackAndTitleActivity.ENTITY_ARGUMENT_KEY, entity);
            intent.putExtras(args);
            startActivity(intent);
        } else if (adapterEntity instanceof UriAdapterEntity) {
            UriEntity uriEntity = ((UriAdapterEntity) adapterEntity).getEntity();
            if (uriEntity.getUriType() == UriType.DIRECTORY) {
                Intent intent = new Intent(this, FilteredUriActivity.class);
                Bundle args = new Bundle();
                args.putString(FilteredUriActivity.TITLE_ARGUMENT_KEY, uriEntity.getFullUriPath(false));
                args.putSerializable(FilteredUriActivity.ENTITY_ARGUMENT_KEY, uriEntity);
                intent.putExtras(args);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleSearch(intent);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String text) {
        SearchView searchView = (SearchView) this.findViewById(R.id.search_action_search);
        if (searchView != null && searchView.hasFocus()) {
            if (Utils.nullOrEmpty(text)) {
                adapterEntities = new AdapterEntity[0];
                createEntityAdapter(adapterEntities);
            } else {
                search(text);
            }
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

    @Override
    public void entityFilterChanged() {
        SearchView searchView = (SearchView) this.findViewById(R.id.search_action_search);
        if (searchView != null) {
            String text = searchView.getQuery().toString();
            if (Utils.nullOrEmpty(text)) {
                adapterEntities = new AdapterEntity[0];
                createEntityAdapter(adapterEntities);
            } else {
                search(text);
            }
        }
    }

    public void onChoiceMenuClick(View view) {
        SearchView searchView = (SearchView) this.findViewById(R.id.search_action_search);
        searchView.setIconified(false);
        searchView.requestFocus();
    }
    
    public void onControlMenuClick(View view) {
        controlHelper.toggleVisibility();
    }

    private void handleSearch(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            search(query);

            SearchView searchView = (SearchView) this.findViewById(R.id.search_action_search);
            if (searchView != null) {
                searchView.clearFocus();
                searchView.setQuery("", false);
                searchView.setIconified(true);
            }
        }
    }    

    private void search(String query) {
        setTitle(query);
        showSearchIndicator();
        searchLibraryHandle.cancelTask();
        searchLibraryHandle = MusicPlayerControl.searchLibrary(query, searchUri(), uriFilterHelper.getUriFilter(), new ResponseReceiver<SearchResult>() {
            @Override
            public void buildingDatabase() {
                Boast.makeText(SearchActivity.this, R.string.database_build_building_database_toast).show();
            }

            @Override
            public void receiveResponse(SearchResult response) {
                adapterEntities = createAdapterEntities(response.getLibraryEntities(), response.getUriEntities());
                createEntityAdapter(adapterEntities);
                hideSearchIndicator();
                verifyBuildDatabase();
            }
        });
    }

    private void verifyBuildDatabase() {
        UpdateDatabaseResult lastDatabaseResult = MpdDatabaseBuilder.getLastDatabaseResult();

        if (lastDatabaseResult != UpdateDatabaseResult.NO_ERROR) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            switch (lastDatabaseResult) {
            case UPDATE_RUNNING_ERROR:
                builder.setTitle(R.string.database_build_error_updating_title);
                builder.setMessage(R.string.database_build_error_updating_message);
                break;
            case TRACK_COUNT_MISMATCH_ERROR:
                builder.setTitle(R.string.database_build_error_buffer_title);
                builder.setMessage(R.string.database_build_error_buffer_message);
                break;
            case NO_ERROR:
                break;
            }
            builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog buildDatabaseErrorDialog = builder.create();
            buildDatabaseErrorDialog.show();
        }
    }
    
    private void setupActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupLollipop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View choiceBarSeperator = findViewById(R.id.search_choice_bar_seperator);
            choiceBarSeperator.setVisibility(View.GONE);

            View choiceBar = findViewById(R.id.search_choice_bar);
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

    private AdapterEntity[] createAdapterEntities(LibraryEntity[] libraryEntities, UriEntity[] uriEntities) {
        ArrayList<AdapterEntity> result = new ArrayList<AdapterEntity>(libraryEntities.length + uriEntities.length + 5);

        boolean addArtistSection = true;
        boolean addAlbumArtistSection = true;
        boolean addComposerSection = true;
        boolean addAlbumSection = true;
        boolean addTitleSection = true;
        for (int i = 0; i < libraryEntities.length; i++) {
            if (libraryEntities[i].getTag() == Tag.ARTIST) {
                if (addArtistSection) {
                    result.add(new SectionAdapterEntity(getString(R.string.search_artist_section)));
                    addArtistSection = false;
                }
                result.add(new ArtistAdapterEntity(libraryEntities[i]));
            }
            if (libraryEntities[i].getTag() == Tag.ALBUM_ARTIST) {
                if (addAlbumArtistSection) {
                    result.add(new SectionAdapterEntity(getString(R.string.search_album_artist_section)));
                    addAlbumArtistSection = false;
                }
                result.add(new ArtistAdapterEntity(libraryEntities[i]));
            }
            if (libraryEntities[i].getTag() == Tag.COMPOSER) {
                if (addComposerSection) {
                    result.add(new SectionAdapterEntity(getString(R.string.search_composer_section)));
                    addComposerSection = false;
                }
                result.add(new ArtistAdapterEntity(libraryEntities[i]));
            }
            if (libraryEntities[i].getTag() == Tag.ALBUM) {
                if (addAlbumSection) {
                    result.add(new SectionAdapterEntity(getString(R.string.search_albums_section)));
                    addAlbumSection = false;
                }
                result.add(new AlbumAdapterEntity(libraryEntities[i], false));
            }
            if (libraryEntities[i].getTag() == Tag.TITLE) {
                if (addTitleSection) {
                    result.add(new SectionAdapterEntity(getString(R.string.search_titles_section)));
                    addTitleSection = false;
                }
                result.add(new SearchTitleAdapterEntity(libraryEntities[i]));
            }
        }

        boolean addDirSection = true;
        boolean addFileSection = true;
        for (int i = 0; i < uriEntities.length; i++) {
            if (uriEntities[i].getUriType() == UriType.DIRECTORY) {
                if (addDirSection) {
                    result.add(new SectionAdapterEntity(getString(R.string.library_directories_section)));
                    addDirSection = false;
                }
                result.add(new UriAdapterEntity(uriEntities[i]));
            }
            if (uriEntities[i].getUriType() == UriType.FILE) {
                if (addFileSection) {
                    result.add(new SectionAdapterEntity(getString(R.string.library_files_section)));
                    addFileSection = false;
                }
                result.add(new UriAdapterEntity(uriEntities[i]));
            }
        }

        return result.toArray(new AdapterEntity[result.size()]);
    }

    private void createEntityAdapter(AdapterEntity[] adapterEntities) {
        ListView listView = findListView();
        if (listView != null) {
            listView.setFastScrollEnabled(false); //To ensure the section indexes are recalculated
            ArrayAdapter adapter = createAdapter(adapterEntities);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged(); //also to ensure the section indexes are recalculated
            listView.setFastScrollEnabled(true);
        }
    }

    private ListView findListView() {
        return (ListView)this.findViewById(R.id.search_list_view_browse);
    }

    private ArrayAdapter createAdapter(AdapterEntity[] adapterEntities) {
        return new LibraryArrayAdapter(this, android.R.layout.simple_list_item_1, adapterEntities, SectionSortedAdapterIndexStrategy.INSTANCE, false);
    }

    private void showSearchIndicator() {
        ProgressBar progressBar = findProgressBar();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideSearchIndicator() {
        ProgressBar progressBar = findProgressBar();
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private ProgressBar findProgressBar() {
        return (ProgressBar)this.findViewById(R.id.search_progress_bar_search);
    }

    private boolean searchUri() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Resources resources = getResources();
        return sharedPreferences.getBoolean(resources.getString(R.string.settings_library_search_uri_key), false);
    }
}
