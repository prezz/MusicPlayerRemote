package net.prezz.mpr.ui.settings.servers;

import java.util.ArrayList;

import net.prezz.mpr.Utils;
import net.prezz.mpr.model.servers.ServerConfiguration;
import net.prezz.mpr.model.servers.ServerConfigurationService;
import net.prezz.mpr.R;
import net.prezz.mpr.ui.helpers.ThemeHelper;
import net.prezz.mpr.ui.helpers.VolumeButtonsHelper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ServersActivity extends Activity implements OnItemClickListener, OnMenuItemClickListener {

	private static final int ADD_EDIT_SERVER_ACTIVITY_RESULT = 3002;
	private ServerConfiguration[] serverConfigurations;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ThemeHelper.applyTheme(this);
		setContentView(R.layout.activity_servers);
		
        setupLollipop();
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		ListView listView = findListView();
		listView.setOnItemClickListener(this);
		registerForContextMenu(listView);

		ArrayAdapter<ServerConfiguration> adapter = new ArrayAdapter<ServerConfiguration>(this, android.R.layout.simple_list_item_1, new ArrayList<ServerConfiguration>());
		listView.setAdapter(adapter);
		
		updateListView();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.servers_list_view_browse) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			ServerConfiguration configuration = serverConfigurations[info.position];
			menu.setHeaderTitle(configuration.toString());
			String[] menuItems = getResources().getStringArray(R.array.servers_context_menu);
			for (int i = 0; i < menuItems.length; i++) {
				MenuItem menuItem = menu.add(Menu.NONE, i, i, menuItems[i]);
				menuItem.setOnMenuItemClickListener(this);
			}
		}
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		ServerConfiguration configuration = serverConfigurations[info.position];
		switch (item.getItemId()) {
		case 0:
			ServerConfigurationService.deleteServerConfiguration(configuration);
			updateListView();
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
		ServerConfiguration configuration = serverConfigurations[position];
    	Intent intent = new Intent(this, AddEditServerActivity.class);
		Bundle args = new Bundle();
		args.putSerializable(AddEditServerActivity.CONFIGURATION_ARGUMENT_KEY, configuration);
		intent.putExtras(args);
    	startActivityForResult(intent, ADD_EDIT_SERVER_ACTIVITY_RESULT);
	}
	
	public void onAddServerClick(View view) {
		if (serverConfigurations != null) {
	    	Intent intent = new Intent(this, AddEditServerActivity.class);
			Bundle args = new Bundle();
			args.putSerializable(AddEditServerActivity.CONFIGURATION_ARGUMENT_KEY, null);
			intent.putExtras(args);
	    	
	    	startActivityForResult(intent, ADD_EDIT_SERVER_ACTIVITY_RESULT);
		}
	}
	
    @Override 
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ADD_EDIT_SERVER_ACTIVITY_RESULT) {
			if (resultCode == RESULT_OK) {
				updateListView();
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

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void setupLollipop() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			View choiceBarSeperator = findViewById(R.id.servers_choice_bar_seperator);
			choiceBarSeperator.setVisibility(View.GONE);
        	
			View choiceBar = findViewById(R.id.servers_choice_bar);
			choiceBar.setElevation(getResources().getDimension(R.dimen.choice_bar_elevation));
        }
	}

	private void updateListView() {
		serverConfigurations = ServerConfigurationService.getServerConfigurations();
		
    	ListView listView = findListView();
	    if (listView != null) {
	    	ArrayAdapter<ServerConfiguration> arrayAdapter = Utils.cast(listView.getAdapter());
	    	arrayAdapter.setNotifyOnChange(false);
	    	arrayAdapter.clear();
	        arrayAdapter.addAll(serverConfigurations);
	        arrayAdapter.notifyDataSetChanged();
	    }
	}
    
	private ListView findListView() {
		return (ListView)this.findViewById(R.id.servers_list_view_browse);
	}
}
