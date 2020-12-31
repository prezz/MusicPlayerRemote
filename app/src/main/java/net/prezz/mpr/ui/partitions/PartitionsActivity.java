package net.prezz.mpr.ui.partitions;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

import net.prezz.mpr.R;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.ui.adapter.StoredPlaylistAdapterEntity;
import net.prezz.mpr.ui.helpers.ThemeHelper;
import net.prezz.mpr.ui.helpers.VolumeButtonsHelper;
import net.prezz.mpr.ui.view.DataFragment;

import java.util.Arrays;

public class PartitionsActivity extends Activity implements OnItemClickListener, OnMenuItemClickListener {

    private static final String PARTITIONS_SAVED_INSTANCE_STATE = "partitions";

    private String[] partitions = null;
    private boolean updating = false;
    private TaskHandle updatingPartitionsHandle = TaskHandle.NULL_HANDLE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeHelper.applyTheme(this);
        setContentView(R.layout.activity_partitions);

        setupActionBar();
        setupLollipop();

        DataFragment dataFragment = DataFragment.getRestoreFragment(this, getClass());
        if (dataFragment != null) {
            //restore entities if loaded into memory again (or after rotation)
            Object[] objectEntities = (Object[]) dataFragment.getData(PARTITIONS_SAVED_INSTANCE_STATE, null);
            if (objectEntities != null) {
                partitions = Arrays.copyOf(objectEntities, objectEntities.length, String[].class);
            }
        }
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        ListView listView = findListView();
        listView.setOnItemClickListener(this);
        registerForContextMenu(listView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,    long id) {
    }

    public void onAddPartitionClick(View view) {
    }

    @Override 
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (VolumeButtonsHelper.handleKeyDown(this, keyCode, event)) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void setupActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupLollipop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View choiceBarSeperator = findViewById(R.id.partitions_choice_bar_seperator);
            choiceBarSeperator.setVisibility(View.GONE);

            View choiceBar = findViewById(R.id.partitions_choice_bar);
            choiceBar.setElevation(getResources().getDimension(R.dimen.choice_bar_elevation));
        }
    }

    private ListView findListView() {
        return (ListView)this.findViewById(R.id.partitions_list_view_browse);
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
        return (ProgressBar)this.findViewById(R.id.partitions_progress_bar_load);
    }
}
