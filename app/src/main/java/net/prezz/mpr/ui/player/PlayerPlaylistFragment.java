package net.prezz.mpr.ui.player;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import net.prezz.mpr.R;
import net.prezz.mpr.Utils;
import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.PlayerState;
import net.prezz.mpr.model.PlayerStatus;
import net.prezz.mpr.model.PlaylistEntity;
import net.prezz.mpr.model.command.ClearPlaylistCommand;
import net.prezz.mpr.model.command.DeleteFromPlaylistCommand;
import net.prezz.mpr.model.command.DeleteMultipleFromPlaylistCommand;
import net.prezz.mpr.model.command.MoveInPlaylistCommand;
import net.prezz.mpr.model.command.PauseCommand;
import net.prezz.mpr.model.command.PlayCommand;
import net.prezz.mpr.model.command.PrioritizeUriCommand;
import net.prezz.mpr.model.command.ShuffleCommand;
import net.prezz.mpr.model.command.UnprioritizeCommand;
import net.prezz.mpr.model.command.UpdatePrioritiesCommand;
import net.prezz.mpr.ui.adapter.PlaylistAdapterEntity;
import net.prezz.mpr.ui.adapter.PlaylistArrayAdapter;
import net.prezz.mpr.ui.helpers.Boast;
import net.prezz.mpr.ui.helpers.UriFilterHelper;
import net.prezz.mpr.ui.library.filtered.FilteredActivity;
import net.prezz.mpr.ui.library.filtered.FilteredAlbumAndTitleActivity;
import net.prezz.mpr.ui.library.filtered.FilteredTrackAndTitleActivity;
import net.prezz.mpr.ui.view.DragListView;
import net.prezz.mpr.ui.view.DragListView.DragListener;
import net.prezz.mpr.ui.view.DragListView.DropListener;
import net.prezz.mpr.ui.view.DragListView.RemoveListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerPlaylistFragment extends Fragment implements PlayerFragment, OnItemClickListener, OnMenuItemClickListener {

    public static final int FRAGMENT_POSITION = 0;

    private PlayerStatus playerStatus = new PlayerStatus(false);
    private PlaylistAdapterEntity[] adapterEntities;

    private UriFilterHelper uriFilterHelper;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_playlist, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        createEntityAdapter();

        DragListView listView = findListView();

        listView.setOnItemClickListener(this);

        listView.setDragListener(new EntityDragListener());
        listView.setDropListener(new EntityDropListener());
        listView.setRemoveListener(new EntityRemoveListener());

        registerForContextMenu(listView);

        showUpdatingIndicator();

        uriFilterHelper = new UriFilterHelper(getActivity(), new UriFilterHelper.UriFilterChangedListener() {
            @Override
            public void entityFilterChanged() {
            }
        });

        ((PlayerActivity)getActivity()).attachFragment(this, FRAGMENT_POSITION);
    }

    @Override
    public void onDestroyView() {
        ((PlayerActivity)getActivity()).detachFragment(FRAGMENT_POSITION);
        super.onDestroyView();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,    long id) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Resources resources = getActivity().getResources();
        boolean enabled = sharedPreferences.getBoolean(resources.getString(R.string.settings_playlist_track_click_behaviour_key), true);

        if (enabled) {
            doPlay((int)id);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.player_list_view_playlist) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            PlaylistAdapterEntity entity = adapterEntities[info.position];
            menu.setHeaderTitle(entity.getText());
            String[] menuItems = getResources().getStringArray(R.array.player_playlist_context_menu);
            for (int i = 0; i < menuItems.length; i++) {
                MenuItem menuItem = menu.add(Menu.NONE, i, i, menuItems[i]);
                menuItem.setOnMenuItemClickListener(this);
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        PlaylistAdapterEntity entity = adapterEntities[info.position];
        PlaylistEntity playlistEntity = entity.getEntity();
        switch (item.getItemId()) {
        case 0:
            doPlay(info.position);
            return true;
        case 1: {
                if (info.position != playerStatus.getCurrentSong()) {
                    MusicPlayerControl.sendControlCommands(Arrays.asList(new DeleteFromPlaylistCommand(info.position), new PrioritizeUriCommand(playlistEntity.getUriEntity())));
                }
                return true;
            }
        case 2:
                MusicPlayerControl.sendControlCommands(Arrays.asList(new DeleteFromPlaylistCommand(info.position), new UpdatePrioritiesCommand()));
                return true;
        case 3: {
                List<Integer> identifiers = new ArrayList<Integer>();
                String album = playlistEntity.getAlbum();
                for (PlaylistAdapterEntity adapterEntity : adapterEntities) {
                    PlaylistEntity e = adapterEntity.getEntity();
                    if (Utils.equals(album, e.getAlbum())) {
                        identifiers.add(e.getId());
                    }
                }
                if (!identifiers.isEmpty()) {
                    MusicPlayerControl.sendControlCommands(Arrays.asList(new DeleteMultipleFromPlaylistCommand(identifiers.toArray(new Integer[identifiers.size()])), new UpdatePrioritiesCommand()));
                }
                return true;
            }
        case 4: {
                goTo(playlistEntity);
                return true;
            }
        }

        return false;
    }

    @Override
    public void statusUpdated(PlayerStatus status) {
        boolean refresh = playerStatus.getPlaylistVersion() == status.getPlaylistVersion() && (playerStatus.getCurrentSong() != status.getCurrentSong() || playerStatus.getState() != status.getState());
        playerStatus = status;
        if (refresh) {
            refreshEntities();
        }
    }

    @Override
    public void playlistUpdated(PlaylistEntity[] playlistEntities) {
        if (playerStatus.getPlaylistVersion() > -1) {
            hideUpdatingIndicator();
            scrollToPlayingSong();
        }
        adapterEntities = createAdapterEntities(playlistEntities);
        refreshEntities();
    }

    @Override
    public void onChoiceMenuClick(View view) {
        final String[] items = getResources().getStringArray(R.array.player_playlist_choice_menu);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.player_playlist));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch(item) {
                case 0:
                    scrollToPlayingSong();
                    break;
                case 1:
                    shufflePlaylist();
                    break;
                case 2:
                    if (adapterEntities != null && adapterEntities.length > 0) {
                        MusicPlayerControl.sendControlCommand(new UnprioritizeCommand(0, adapterEntities.length));
                    }
                    break;
                case 3:
                    MusicPlayerControl.sendControlCommand(new ClearPlaylistCommand());
                    break;
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void scrollToPlayingSong() {
        findListView().setSelectionFromTop(Math.max(0, playerStatus.getCurrentSong() - 1), 0);
    }

    private void shufflePlaylist() {
        MusicPlayerControl.sendControlCommands(Arrays.asList(new UnprioritizeCommand(0, adapterEntities.length), new ShuffleCommand()));
    }

    private void createEntityAdapter() {
        ListView listView = findListView();
        if (listView != null) {
            ListAdapter adapter = new PlaylistArrayAdapter(getActivity(), android.R.layout.simple_list_item_2, new ArrayList<PlaylistAdapterEntity>());
            listView.setAdapter(adapter);
        }
    }

    protected PlaylistAdapterEntity[] createAdapterEntities(PlaylistEntity[] entities) {
        PlaylistAdapterEntity[] result = new PlaylistAdapterEntity[entities.length];

        for (int i = 0; i < entities.length; i++) {
            result[i] = new PlaylistAdapterEntity(entities[i], showPriorities());
        }

        return result;
    }

    private void doPlay(int id) {
        if (id == playerStatus.getCurrentSong()) {
            if (playerStatus.getState() == PlayerState.PLAY) {
                MusicPlayerControl.sendControlCommand(new PauseCommand(false));
                return;
            }

            if (playerStatus.getState() == PlayerState.PAUSE) {
                MusicPlayerControl.sendControlCommand(new PauseCommand(true));
                return;
            }
        }
        PlaylistAdapterEntity entity = adapterEntities[id];
        PlaylistEntity playlistEntity = entity.getEntity();
        MusicPlayerControl.sendControlCommands(Arrays.asList(new PlayCommand(playlistEntity.getId()), new UpdatePrioritiesCommand()));
    }

    private DragListView findListView() {
        return (DragListView)getView().findViewById(R.id.player_list_view_playlist);
    }

    private void showUpdatingIndicator() {
        ProgressBar progressBar = findProgressBar();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideUpdatingIndicator() {
        ProgressBar progressBar = findProgressBar();
        progressBar.setVisibility(View.GONE);
    }

    private ProgressBar findProgressBar() {
        return (ProgressBar)getView().findViewById(R.id.player_progress_bar_load);
    }

    private void refreshEntities() {
        refreshEntities(playerStatus.getCurrentSong(), playerStatus.getState());
    }

    private void refreshEntities(int currentSong, PlayerState playerState) {
        DragListView listView = findListView();
        if (listView != null) {
            PlaylistArrayAdapter adapter = (PlaylistArrayAdapter)listView.getAdapter();
            adapter.setData(adapterEntities, currentSong, playerState);
        }
    }

    private void setSwipeEnabled(boolean enabled) {
        FragmentActivity activity = getActivity();
        ViewPager2 viewPager = (ViewPager2)activity.findViewById(R.id.player_view_pager_swipe);
        viewPager.setUserInputEnabled(enabled);
        //viewPager.requestDisallowInterceptTouchEvent(enabled);
    }

    private boolean showPriorities() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Resources resources = getActivity().getResources();
        return sharedPreferences.getBoolean(resources.getString(R.string.settings_playlist_show_priority_in_playlist_key), false);
    }

    private void goTo(final PlaylistEntity playlistEntity) {
        String[] items = getResources().getStringArray(R.array.player_context_goto);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.player_goto_header);
        builder.setItems(items, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                case 0: {
                        String artist = playlistEntity.getArtist();
                        if (!Utils.nullOrEmpty(artist)) {
                            Intent intent = new Intent(getActivity(), FilteredAlbumAndTitleActivity.class);
                            Bundle args = new Bundle();
                            args.putString(FilteredActivity.TITLE_ARGUMENT_KEY, artist);
                            args.putSerializable(FilteredActivity.ENTITY_ARGUMENT_KEY, LibraryEntity.createBuilder().setArtist(artist).setUriFilter(uriFilterHelper.getUriFilter()).build());
                            intent.putExtras(args);
                            startActivity(intent);
                        } else {
                            Boast.makeText(getActivity(), R.string.player_not_possible).show();
                        }
                    }
                    break;
                case 1: {
                        String album = playlistEntity.getAlbum();
                        if (!Utils.nullOrEmpty(album)) {
                            Intent intent = new Intent(getActivity(), FilteredTrackAndTitleActivity.class);
                            Bundle args = new Bundle();
                            args.putString(FilteredActivity.TITLE_ARGUMENT_KEY, album);
                            args.putSerializable(FilteredActivity.ENTITY_ARGUMENT_KEY, LibraryEntity.createBuilder().setAlbum(album).setUriFilter(uriFilterHelper.getUriFilter()).build());
                            intent.putExtras(args);
                            startActivity(intent);
                        } else {
                            Boast.makeText(getActivity(), R.string.player_not_possible).show();
                        }
                    }
                    break;
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private final class EntityDragListener implements DragListener {
        @Override
        public void drag(int from, int to) {
            setSwipeEnabled(false);
        }
    }

    private final class EntityDropListener implements DropListener {

        @Override
        public void drop(int from, int to) {
            setSwipeEnabled(true);

            PlaylistAdapterEntity movingEntity = adapterEntities[from];
            if (from > to) { //moving up in list
                System.arraycopy(adapterEntities, to, adapterEntities, to + 1, from - to);
                adapterEntities[to] = movingEntity;
                MusicPlayerControl.sendControlCommands(Arrays.asList(new MoveInPlaylistCommand(movingEntity.getEntity().getId(), to), new UpdatePrioritiesCommand()));
                if (from > playerStatus.getCurrentSong() && to <= playerStatus.getCurrentSong()) {
                    refreshEntities(playerStatus.getCurrentSong() + 1, playerStatus.getState());
                } else if (from == playerStatus.getCurrentSong()) {
                    refreshEntities(to, playerStatus.getState());
                } else {
                    refreshEntities();
                }
            } else if (from < to) { //moving down in list
                System.arraycopy(adapterEntities, from + 1, adapterEntities, from, to - from);
                adapterEntities[to] = movingEntity;
                MusicPlayerControl.sendControlCommands(Arrays.asList(new MoveInPlaylistCommand(movingEntity.getEntity().getId(), to), new UpdatePrioritiesCommand()));
                if (from < playerStatus.getCurrentSong() && to >= playerStatus.getCurrentSong()) {
                    refreshEntities(playerStatus.getCurrentSong() - 1, playerStatus.getState());
                } else if (from == playerStatus.getCurrentSong()) {
                    refreshEntities(to, playerStatus.getState());
                } else {
                    refreshEntities();
                }
            }
        }
    }

    private final class EntityRemoveListener implements RemoveListener {

        @Override
        public void remove(int which) {
            setSwipeEnabled(true);

            PlaylistAdapterEntity[] newEntities = new PlaylistAdapterEntity[adapterEntities.length - 1];
            System.arraycopy(adapterEntities, 0, newEntities, 0, which);
            System.arraycopy(adapterEntities, which + 1, newEntities, which, newEntities.length - which);
            MusicPlayerControl.sendControlCommands(Arrays.asList(new DeleteFromPlaylistCommand(which), new UpdatePrioritiesCommand()));
            adapterEntities = newEntities;

            if (which < playerStatus.getCurrentSong()) {
                refreshEntities(playerStatus.getCurrentSong() - 1, playerStatus.getState());
            } else if (which == playerStatus.getCurrentSong()) {
                refreshEntities(-1, playerStatus.getState());
            } else {
                refreshEntities();
            }
        }
    }
}
