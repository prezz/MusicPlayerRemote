package net.prezz.mpr.ui.library;

import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.mpd.database.MpdDatabaseBuilder;
import net.prezz.mpr.mpd.database.MpdDatabaseBuilder.UpdateDatabaseResult;
import net.prezz.mpr.ui.helpers.MiniControlHelper;
import net.prezz.mpr.R;
import net.prezz.mpr.ui.helpers.ThemeHelper;
import net.prezz.mpr.ui.helpers.UriFilterHelper;
import net.prezz.mpr.ui.helpers.VolumeButtonsHelper;
import net.prezz.mpr.ui.view.DataFragment;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.SortedSet;

public class LibraryActivity extends FragmentActivity implements UriFilterHelper.UriFilterChangedListener {

    private static final String ENTITIES_CHANGED = "entities_changed";
    private static final String URI_ENTITY_FILTER = "uri_entity_filter";

    private static final String PREFERENCE_FRAGMENT_POSITION_KEY = "fragment_position_key";
    private static final String PREFERENCE_SHOW_SWIPE_HINT_KEY = "library_show_swipe_hint";

    private LibraryCommonsFragment[] attachedFragments = new LibraryCommonsFragment[LibraryPagerAdapter.FRAGMENT_COUNT];
    private boolean[] entitiesChanged = new boolean[attachedFragments.length];
    private UriEntity uriEntityFilter = null;
    private int fragmentPosition;
    private MiniControlHelper controlHelper;
    private UriFilterHelper uriFilterHelper;

    private AlertDialog buildDatabaseErrorDialog;
    private AlertDialog swipeHintDialog;

    private TaskHandle getUrientitiesFilterHandle = TaskHandle.NULL_HANDLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeHelper.applyTheme(this);
        setContentView(R.layout.activity_library);
        // Show the Up button in the action bar.
        setupActionBar();
        setupLollipop();

        buildDatabaseErrorDialog = null;
        swipeHintDialog = null;

        //setup swipe between fragments
        final LibraryPagerAdapter pageAdapter = new LibraryPagerAdapter(getSupportFragmentManager(), this);
        ViewPager viewPager = (ViewPager)findViewById(R.id.library_view_pager_swipe);
        viewPager.setAdapter(pageAdapter);
        
        fragmentPosition = Math.min(readFragmentPosition(), pageAdapter.getCount());
        if (fragmentPosition > 0) {
            viewPager.setCurrentItem(fragmentPosition);
        }
        
        setTitle(pageAdapter.getTitle(fragmentPosition));
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                fragmentPosition = position;
                setTitle(pageAdapter.getTitle(position));
            }
        });

        controlHelper = new MiniControlHelper(this);
        uriFilterHelper = new UriFilterHelper(this, this);

        DataFragment dataFragment = DataFragment.getRestoreFragment(this, getClass());
        if (dataFragment != null) {
            entitiesChanged = (boolean[]) dataFragment.getData(ENTITIES_CHANGED, entitiesChanged);
            uriEntityFilter = (UriEntity) dataFragment.getData(URI_ENTITY_FILTER, uriEntityFilter);
        }
    }

    public void verifyBuildDatabase() {
        if (buildDatabaseErrorDialog != null && buildDatabaseErrorDialog.isShowing()) {
            return;
        }

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
                    buildDatabaseErrorDialog = null;
                }
            });
            buildDatabaseErrorDialog = builder.create();
            buildDatabaseErrorDialog.show();
        } else {
            showSwipeHint();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        controlHelper.hideVisibility();
    }

    @Override
    protected void onStop() {
        super.onStop();

        storeFragmentPosition(fragmentPosition);
    }

    @Override
    public void onDestroy() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.library_view_pager_swipe);
        viewPager.clearOnPageChangeListeners();

        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        DataFragment dataFragment = DataFragment.getSaveFragment(this, getClass());
        if (dataFragment != null) {
            dataFragment.setData(ENTITIES_CHANGED, entitiesChanged);
            dataFragment.setData(URI_ENTITY_FILTER, uriEntityFilter);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.library, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            case R.id.library_action_visible_folders: {
                uriFilterHelper.setUriFilter();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupLollipop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View choiceBarSeperator = findViewById(R.id.library_choice_bar_seperator);
            choiceBarSeperator.setVisibility(View.GONE);

            View choiceBar = findViewById(R.id.library_choice_bar);
            choiceBar.setElevation(getResources().getDimension(R.dimen.choice_bar_elevation));

            View controlSeperator = findViewById(R.id.control_layout_seperator);
            controlSeperator.setVisibility(View.GONE);

            View controlLayout = findViewById(R.id.control_layout_mini_control);
            controlLayout.setElevation(getResources().getDimension(R.dimen.choice_bar_elevation));
        }
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
        for (int i = 0; i < attachedFragments.length; i++) {
            entitiesChanged[i] = true;
            if (attachedFragments[i] != null) {
                attachedFragments[i].entitiesChanged();
                entitiesChanged[i] = false;
            }
        }
    }

    public LibraryEntity getLibraryEntityFilter() {
        return LibraryEntity.createBuilder()
                .setUriEntity(getUriEntityFilter())
                .setUriFilter(getUriFilter())
                .build();
    }

    public UriEntity getUriEntityFilter() {
        return uriEntityFilter;
    }

    public SortedSet<String> getUriFilter() {
        return uriFilterHelper.getUriFilter();
    }

    public boolean attachFragment(LibraryCommonsFragment fragment, int pos) {
        attachedFragments[pos] = fragment;

        boolean changed = entitiesChanged[pos];
        entitiesChanged[pos] = false;
        return changed;
    }
    
    public void detachFragment(int pos) {
        attachedFragments[pos] = null;
    }

    public void onFilterMenuClick(View view) {

        getUrientitiesFilterHandle.cancelTask();
        getUrientitiesFilterHandle = MusicPlayerControl.getUriFromLibrary(null, getUriFilter(), new ResponseReceiver<UriEntity[]>() {
            @Override
            public void receiveResponse(final UriEntity[] entities) {

                final UriEntity[] selections = new UriEntity[entities.length+1];
                System.arraycopy(entities, 0, selections, 1, entities.length);

                String[] items = new String[selections.length];
                for (int i = 0; i < selections.length; i++) {
                    items[i] = (selections[i] != null) ? selections[i].getFullUriPath(true) : LibraryActivity.this.getResources().getString(R.string.library_selected_folder_none);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(LibraryActivity.this);
                builder.setTitle(R.string.library_selected_folder_header);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        uriEntityFilter = selections[which];
                        entityFilterChanged();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    public void onChoiceMenuClick(View view) {
        attachedFragments[fragmentPosition].onChoiceMenuClick(view);
    }
    
    public void onControlMenuClick(View view) {
        controlHelper.toggleVisibility();
    }
    
    private int readFragmentPosition() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int position = sharedPreferences.getInt(PREFERENCE_FRAGMENT_POSITION_KEY, 0);
        return position;
    }

    private void storeFragmentPosition(int position) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = sharedPreferences.edit();
        editor.putInt(PREFERENCE_FRAGMENT_POSITION_KEY, position);
        editor.commit();
    }

    private void showSwipeHint() {
        if (swipeHintDialog != null && swipeHintDialog.isShowing()) {
            return;
        }

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean show = sharedPreferences.getBoolean(PREFERENCE_SHOW_SWIPE_HINT_KEY, true);

        if (show) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LibraryActivity.this);
            builder.setCancelable(false);
            builder.setTitle(R.string.library_swipe_hint_header);
            builder.setMessage(R.string.library_swipe_hint_message);
            builder.setPositiveButton(R.string.library_swipe_hint_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(PREFERENCE_SHOW_SWIPE_HINT_KEY, false);
                    editor.commit();
                }
            });
            swipeHintDialog = builder.create();
            swipeHintDialog.show();
        }
    }
}
