package net.prezz.mpr.ui.partitions;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.core.app.NavUtils;

import net.prezz.mpr.R;
import net.prezz.mpr.Utils;
import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.PartitionEntity;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.ResponseResult;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.model.command.CreatePartitionCommand;
import net.prezz.mpr.model.command.DeletePartitionCommand;
import net.prezz.mpr.service.PlaybackService;
import net.prezz.mpr.ui.adapter.PartitionAdapterEntity;
import net.prezz.mpr.ui.adapter.PartitionArrayAdapter;
import net.prezz.mpr.ui.helpers.Boast;
import net.prezz.mpr.ui.helpers.PartitionHelper;
import net.prezz.mpr.ui.helpers.ThemeHelper;
import net.prezz.mpr.ui.helpers.VolumeButtonsHelper;
import net.prezz.mpr.ui.view.DataFragment;

import java.util.ArrayList;
import java.util.Arrays;

public class PartitionsActivity extends Activity implements OnItemClickListener, OnMenuItemClickListener {

    private static final String PARTITIONS_SAVED_INSTANCE_STATE = "partitions";

    private final RefreshEntitiesResponseReceiver refreshResponseReceiver = new RefreshEntitiesResponseReceiver();

    private PartitionAdapterEntity[] adapterEntities = null;
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
                adapterEntities = Arrays.copyOf(objectEntities, objectEntities.length, PartitionAdapterEntity[].class);
            }
        }

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
            dataFragment.setData(PARTITIONS_SAVED_INSTANCE_STATE, adapterEntities);
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
        if (v.getId() == R.id.partitions_list_view_browse) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            PartitionAdapterEntity entity = adapterEntities[info.position];
            menu.setHeaderTitle(entity.getText());
            String[] menuItems = getResources().getStringArray(R.array.partitions_context_menu);
            for (int i = 0; i < menuItems.length; i++) {
                MenuItem menuItem = menu.add(Menu.NONE, i, i, menuItems[i]);
                menuItem.setOnMenuItemClickListener(this);
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        PartitionAdapterEntity entity = adapterEntities[info.position];
        PartitionEntity partitionEntity = entity.getEntity();
        switch (item.getItemId()) {
            case 0:
                // unable to delete if it is current client partition and f it has outputs
                if (Utils.equals("default", entity.getText())) {
                    Boast.makeText(PartitionsActivity.this, R.string.partitions_delete_default_toast).show();
                } else {
                    MusicPlayerControl.sendControlCommand(new DeletePartitionCommand(partitionEntity.getPartitionName()), refreshResponseReceiver);
                }
                return true;
        }

        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PartitionAdapterEntity entity = adapterEntities[position];
        PartitionEntity partitionEntity = entity.getEntity();
        PartitionHelper.setClientPartition(this, partitionEntity.getPartitionName());

        if (!partitionEntity.isClientPartition()) {
            PlaybackService.stop();
            MusicPlayerControl.switchPartition(partitionEntity.getPartitionName(), new ResponseReceiver<PartitionEntity[]>() {
                @Override
                public void receiveResponse(PartitionEntity[] partitionEntities) {
                    refreshEntities(partitionEntities);
                }
            });
        }
    }

    public void onCreatePartitionClick(View view) {
        if (adapterEntities != null) {
            createPartition();
        }
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

    private void updateEntities() {
        if (adapterEntities != null) {
            createEntityAdapter(adapterEntities);
        } else if (!updating) {
            showUpdatingIndicator();
            updatingPartitionsHandle.cancelTask();
            updatingPartitionsHandle = MusicPlayerControl.getPartitions(new ResponseReceiver<PartitionEntity[]>() {
                @Override
                public void receiveResponse(PartitionEntity[] response) {
                    adapterEntities = createAdapterEntities(response);
                    createEntityAdapter(adapterEntities);
                    hideUpdatingIndicator();
                }
            });
        }
    }

    private PartitionAdapterEntity[] createAdapterEntities(PartitionEntity[] entities) {
        ArrayList<PartitionAdapterEntity> result = new ArrayList<PartitionAdapterEntity>(entities.length);
        for (int i = 0; i < entities.length; i++) {
            result.add(new PartitionAdapterEntity(entities[i]));
        }
        return result.toArray(new PartitionAdapterEntity[result.size()]);
    }

    private void createEntityAdapter(PartitionAdapterEntity[] adapterEntities) {
        ListView listView = findListView();
        if (listView != null) {
            ListAdapter adapter = createAdapter(adapterEntities);
            listView.setAdapter(adapter);
        }
    }

    private ListAdapter createAdapter(PartitionAdapterEntity[] adapterEntities) {
        return new PartitionArrayAdapter(this, android.R.layout.simple_list_item_2, new ArrayList<PartitionAdapterEntity>(Arrays.asList(adapterEntities)));
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

    private void createPartition() {

        final EditText editTextView = new EditText(PartitionsActivity.this);
        editTextView.setSingleLine();

        AlertDialog.Builder builder = new AlertDialog.Builder(PartitionsActivity.this);
        builder.setTitle(R.string.partitions_create_button);
        builder.setView(editTextView);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final String partitionName = editTextView.getText().toString();
                if (partitionName.isEmpty() || Arrays.asList(getPartitionNames(adapterEntities)).contains(partitionName)) {
                    Boast.makeText(PartitionsActivity.this, R.string.partitions_invalid_name_toast).show();
                } else {
                    MusicPlayerControl.sendControlCommand(new CreatePartitionCommand(partitionName), refreshResponseReceiver);
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        final AlertDialog dialog = builder.create();
        editTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        dialog.show();
    }

    private String[] getPartitionNames(PartitionAdapterEntity[] adapterEntities) {
        String[] result = new String[adapterEntities.length];
        for (int i = 0; i < adapterEntities.length; i++) {
            result[i] = adapterEntities[i].getEntity().getPartitionName();
        }
        return result;
    }

    private void refreshEntities(PartitionEntity[] entities) {
        adapterEntities = createAdapterEntities(entities);
        ListView listView = findListView();
        if (listView != null) {
            ArrayAdapter<PartitionAdapterEntity> arrayAdapter = Utils.cast(listView.getAdapter());
            arrayAdapter.setNotifyOnChange(false);
            arrayAdapter.clear();
            arrayAdapter.addAll(adapterEntities);
            arrayAdapter.notifyDataSetChanged();
        }
    }

    private final class RefreshEntitiesResponseReceiver extends ResponseReceiver<ResponseResult> {

        @Override
        public void receiveResponse(ResponseResult response) {
            if (!response.isSuccess()) {
                Boast.makeText(PartitionsActivity.this, R.string.partitions_server_error_toast).show();
            }

            updatingPartitionsHandle.cancelTask();
            updatingPartitionsHandle = MusicPlayerControl.getPartitions(new ResponseReceiver<PartitionEntity[]>() {
                @Override
                public void receiveResponse(PartitionEntity[] response) {
                    refreshEntities(response);
                }
            });
        }
    }
}
