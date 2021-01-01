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
import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.model.command.Command;
import net.prezz.mpr.model.command.CreatePartitionCommand;
import net.prezz.mpr.model.command.DeletePartitionCommand;
import net.prezz.mpr.ui.helpers.Boast;
import net.prezz.mpr.ui.helpers.ThemeHelper;
import net.prezz.mpr.ui.helpers.VolumeButtonsHelper;
import net.prezz.mpr.ui.view.DataFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            dataFragment.setData(PARTITIONS_SAVED_INSTANCE_STATE, partitions);
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
            String partition = partitions[info.position];
            menu.setHeaderTitle(partition);
            String[] menuItems = getResources().getStringArray(R.array.partitions_context_menu);
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
        String partition = partitions[info.position];
        String displayText = getString(R.string.partitions_deleted_toast, partition);
        switch (item.getItemId()) {
            case 0:
                commandList.add(new DeletePartitionCommand(partition));
                sendControlCommands(displayText, commandList);
                return true;
        }

        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,    long id) {
    }

    public void onCreatePartitionClick(View view) {
        if (partitions != null) {
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

    private void sendControlCommands(CharSequence displayText, List<Command> commands) {
        MusicPlayerControl.sendControlCommands(commands);
        Boast.makeText(this, displayText).show();
    }

    private void updateEntities() {
        if (partitions != null) {
            createEntityAdapter(partitions);
        } else if (!updating) {
            showUpdatingIndicator();
            updatingPartitionsHandle.cancelTask();
            updatingPartitionsHandle = MusicPlayerControl.getPartitions(new ResponseReceiver<String[]>() {
                @Override
                public void receiveResponse(String[] response) {
                    partitions = response;
                    createEntityAdapter(partitions);
                    hideUpdatingIndicator();
                }
            });
        }
    }

    private void createEntityAdapter(String[] entities) {
        ListView listView = findListView();
        if (listView != null) {
            ListAdapter adapter = createAdapter(entities);
            listView.setAdapter(adapter);
        }
    }

    private ListAdapter createAdapter(String[] entities) {
        return new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>(Arrays.asList(entities)));
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
                if (partitionName.isEmpty() || Arrays.asList(partitions).contains(partitionName)) {
                    Boast.makeText(PartitionsActivity.this, R.string.partitions_invalid_name_toast).show();
                } else {
                    MusicPlayerControl.sendControlCommand(new CreatePartitionCommand(partitionName));
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
}
