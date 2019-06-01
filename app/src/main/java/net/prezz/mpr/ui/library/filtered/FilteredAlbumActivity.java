package net.prezz.mpr.ui.library.filtered;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import net.prezz.mpr.R;
import net.prezz.mpr.Utils;
import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.ui.adapter.AdapterEntity;
import net.prezz.mpr.ui.adapter.AdapterIndexStrategy;
import net.prezz.mpr.ui.adapter.AlbumAdapterEntity;
import net.prezz.mpr.ui.adapter.LibraryArrayAdapter;
import net.prezz.mpr.ui.adapter.SectionAdapterEntity;
import net.prezz.mpr.ui.adapter.SectionSortedAdapterIndexStrategy;
import net.prezz.mpr.ui.adapter.SortedAdapterIndexStrategy;

import java.util.ArrayList;
import java.util.Collections;

public class FilteredAlbumActivity extends FilteredActivity {

    private boolean sortByArtist;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        sortByArtist = sortByArtist();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_filtered;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,    long id) {
        AdapterEntity adapterEntity = getAdapterEntity(position);
        if (adapterEntity instanceof AlbumAdapterEntity) {
            AlbumAdapterEntity libraryAdapterEntity = (AlbumAdapterEntity)adapterEntity;
            LibraryEntity entity = libraryAdapterEntity.getEntity();

            Intent intent = new Intent(this, FilteredTrackAndTitleActivity.class);
            Bundle args = new Bundle();
            args.putString(FilteredActivity.TITLE_ARGUMENT_KEY, entity.getAlbum());
            args.putSerializable(FilteredActivity.ENTITY_ARGUMENT_KEY, entity);
            intent.putExtras(args);
            startActivity(intent);
        }
    }

    @Override
    protected TaskHandle getEntities(LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver) {
        return MusicPlayerControl.getAllAlbumsFromLibrary(sortByArtist, Collections.<String>emptySet(), responseReceiver);
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
    protected ListAdapter createAdapter(AdapterEntity[] adapterEntities) {
        AdapterIndexStrategy indexStrategy = (adapterEntities.length > 0 && adapterEntities[0] instanceof SectionAdapterEntity) ? SectionSortedAdapterIndexStrategy.INSTANCE : SortedAdapterIndexStrategy.INSTANCE;
        return new LibraryArrayAdapter(this, android.R.layout.simple_list_item_1, adapterEntities, indexStrategy, showCovers());
    }

    private boolean sortByArtist() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Resources resources = this.getResources();
        return sharedPreferences.getBoolean(resources.getString(R.string.settings_library_sort_album_by_artist_key), true);
    }

    private boolean showCovers() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Resources resources = this.getResources();
        return sharedPreferences.getBoolean(resources.getString(R.string.settings_library_show_covers_for_all_albums_key), true);
    }
}
