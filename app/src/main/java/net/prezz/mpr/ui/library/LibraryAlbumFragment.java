package net.prezz.mpr.ui.library;

import net.prezz.mpr.R;
import net.prezz.mpr.Utils;
import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.model.LibraryEntity.Tag;
import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.ui.adapter.AdapterEntity;
import net.prezz.mpr.ui.adapter.AdapterIndexStrategy;
import net.prezz.mpr.ui.adapter.AlbumAdapterEntity;
import net.prezz.mpr.ui.adapter.LibraryAdapterEntity;
import net.prezz.mpr.ui.adapter.LibraryArrayAdapter;
import net.prezz.mpr.ui.adapter.SectionAdapterEntity;
import net.prezz.mpr.ui.adapter.SectionSortedAdapterIndexStrategy;
import net.prezz.mpr.ui.adapter.SortedAdapterIndexStrategy;
import net.prezz.mpr.ui.library.filtered.FilteredActivity;
import net.prezz.mpr.ui.library.filtered.FilteredAlbumAndTitleActivity;
import net.prezz.mpr.ui.library.filtered.FilteredTrackAndTitleActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class LibraryAlbumFragment extends LibraryFragment {

    private static final int FRAGMENT_POSITION = 1;

    private boolean sortByArtist;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sortByArtist = sortByArtist();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        AdapterEntity adapterEntity = getAdapterEntity(position);
        if (adapterEntity instanceof AlbumAdapterEntity) {
            LibraryEntity entity = ((AlbumAdapterEntity)adapterEntity).getEntity();

            Intent intent = new Intent(getActivity(), FilteredTrackAndTitleActivity.class);
            Bundle args = new Bundle();
            args.putString(FilteredActivity.TITLE_ARGUMENT_KEY, entity.getAlbum());
            args.putSerializable(FilteredActivity.ENTITY_ARGUMENT_KEY, entity);
            intent.putExtras(args);
            startActivity(intent);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (super.onMenuItemClick(item)) {
            return true;
        } else if (item.getItemId() == 5) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            AdapterEntity adapterEntity = getAdapterEntity(info.position);
            if (adapterEntity instanceof LibraryAdapterEntity) {
                LibraryEntity entity = ((LibraryAdapterEntity)adapterEntity).getEntity();
                LibraryEntity artistEntity = LibraryEntity.createBuilder().setTag(Tag.ARTIST).setArtist(entity.getLookupArtist()).setUriEntity(entity.getUriEntity()).setUriFilter(entity.getUriFilter()).build();

                Intent intent = new Intent(getActivity(), FilteredAlbumAndTitleActivity.class);
                Bundle args = new Bundle();
                args.putString(FilteredActivity.TITLE_ARGUMENT_KEY, artistEntity.getArtist());
                args.putSerializable(FilteredActivity.ENTITY_ARGUMENT_KEY, artistEntity);
                intent.putExtras(args);
                startActivity(intent);
                return true;
            }
        }

        return false;
    }

    @Override
    protected String[] getContextMenuItems(LibraryEntity entity) {
        if (entity.getMetaCompilation() == Boolean.FALSE) {
            return getResources().getStringArray(R.array.library_album_selected_menu);
        } else {
            return getResources().getStringArray(R.array.library_selected_menu);
        }
    }

    @Override
    protected int getFragmentPosition() {
        return FRAGMENT_POSITION;
    }

    @Override
    protected TaskHandle getEntities(ResponseReceiver<LibraryEntity[]> responseReceiver) {
        LibraryActivity libraryActivity = (LibraryActivity) getActivity();
        UriEntity uriEntityFilter = libraryActivity.getUriEntityFilter();
        Set<String> hiddenUriFolders = (uriEntityFilter == null) ? libraryActivity.getUriFilter() : Collections.<String>emptySet();
        return MusicPlayerControl.getAllAlbumsFromLibrary(sortByArtist, uriEntityFilter, hiddenUriFolders, responseReceiver);
    }

    @Override
    protected AdapterEntity[] createAdapterEntities(LibraryEntity[] entities) {
        boolean sections = entities.length > 0 && Utils.nullOrEmpty(entities[0].getAlbum());
        boolean tracksSection = sections;
        boolean albumsSection = sections;


        ArrayList<AdapterEntity> result = new ArrayList<AdapterEntity>(entities.length + 2);
        for (int i = 0; i < entities.length; i++) {
            if (tracksSection) {
                result.add(new SectionAdapterEntity(getString(R.string.library_titles_section)));
                tracksSection = false;
            } else if (albumsSection && !Utils.nullOrEmpty(entities[i].getAlbum())) {
                result.add(new SectionAdapterEntity(getString(R.string.library_albums_section)));
                albumsSection = false;
            }

            result.add(new AlbumAdapterEntity(entities[i], sortByArtist));
        }

        return result.toArray(new AdapterEntity[result.size()]);
    }

    @Override
    protected LibraryArrayAdapter createAdapter(AdapterEntity[] adapterEntities) {
        AdapterIndexStrategy indexStrategy = (adapterEntities.length > 0 && adapterEntities[0] instanceof SectionAdapterEntity) ? SectionSortedAdapterIndexStrategy.INSTANCE : SortedAdapterIndexStrategy.INSTANCE;
        return new LibraryArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, adapterEntities, indexStrategy, showCovers());
    }

    private boolean sortByArtist() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Resources resources = getActivity().getResources();
        return sharedPreferences.getBoolean(resources.getString(R.string.settings_library_sort_album_by_artist_key), true);
    }

    private boolean showCovers() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Resources resources = getActivity().getResources();
        return sharedPreferences.getBoolean(resources.getString(R.string.settings_library_show_covers_for_all_albums_key), true);
    }
}
