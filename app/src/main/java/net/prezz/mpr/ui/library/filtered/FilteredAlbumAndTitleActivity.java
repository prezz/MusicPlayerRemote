package net.prezz.mpr.ui.library.filtered;

import java.util.ArrayList;

import net.prezz.mpr.R;
import net.prezz.mpr.Utils;
import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.LibraryEntity.Tag;
import net.prezz.mpr.model.external.ExternalInformationService;
import net.prezz.mpr.model.external.UrlReceiver;
import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.ui.adapter.AdapterEntity;
import net.prezz.mpr.ui.adapter.FilteredAlbumAdapterEntity;
import net.prezz.mpr.ui.adapter.FilteredTitleAdapterEntity;
import net.prezz.mpr.ui.adapter.LibraryArrayAdapter;
import net.prezz.mpr.ui.adapter.SectionAdapterEntity;
import net.prezz.mpr.ui.adapter.SectionSortedAdapterIndexStrategy;
import net.prezz.mpr.ui.helpers.Boast;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import androidx.preference.PreferenceManager;

public class FilteredAlbumAndTitleActivity extends FilteredActivity {

    private TaskHandle lastFmHandle = TaskHandle.NULL_HANDLE;


    @Override
    public void onStop() {
        super.onStop();

        lastFmHandle.cancelTask();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_filtered;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.artist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.filtered_lastfm_artist:
            goToLastFm();
            return true;
         default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,    long id) {
        AdapterEntity adapterEntity = getAdapterEntity(position);
        if (adapterEntity instanceof FilteredAlbumAdapterEntity) {
            LibraryEntity entity = ((FilteredAlbumAdapterEntity)adapterEntity).getEntity();

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
        return MusicPlayerControl.getFilteredAlbumsAndTitlesFromLibrary(entity, responseReceiver);
    }

    @Override
    protected AdapterEntity[] createAdapterEntities(LibraryEntity[] entities) {
        boolean addAlbums = hasAlbum(entities);
        boolean addAlbumSection = addAlbums;
        boolean addTitleSection = true;
        ArrayList<AdapterEntity> result = new ArrayList<AdapterEntity>(entities.length + 2);

        for (int i = 0; i < entities.length; i++) {
            if (addAlbums && entities[i].getTag() == Tag.ALBUM) {
                if (addAlbumSection) {
                    result.add(new SectionAdapterEntity(getString(R.string.library_albums_section)));
                    addAlbumSection = false;
                }
                result.add(new FilteredAlbumAdapterEntity(entities[i]));
            }
            if (entities[i].getTag() == Tag.TITLE) {
                if (addTitleSection) {
                    result.add(new SectionAdapterEntity(getString(R.string.library_titles_section)));
                    addTitleSection = false;
                }
                result.add(new FilteredTitleAdapterEntity(entities[i]));
            }
        }

        return result.toArray(new AdapterEntity[result.size()]);
    }

    @Override
    protected ListAdapter createAdapter(AdapterEntity[] adapterEntities) {
        return new LibraryArrayAdapter(this, android.R.layout.simple_list_item_1, adapterEntities, SectionSortedAdapterIndexStrategy.INSTANCE, showCovers());
    }

    private void goToLastFm() {
        String artist = getEntityArgument().getArtist();

        if (Utils.nullOrEmpty(artist)) {
            Boast.makeText(this, R.string.library_action_not_possible).show();
            return;
        }

        lastFmHandle .cancelTask();
        lastFmHandle = ExternalInformationService.getArtistInfoUrls(artist, new UrlReceiver() {
            @Override
            public void receiveUrls(String[] urls) {
                if (urls != null && urls.length > 0) {
                    String url = urls[0];
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } else {
                    Boast.makeText(FilteredAlbumAndTitleActivity.this, R.string.library_action_not_possible).show();
                }
            }
        });
    }

    private boolean hasAlbum(LibraryEntity[] entities) {
        for (int i = 0; i < entities.length; i++) {
            if (entities[i].getTag() == Tag.ALBUM && !entities[i].getAlbum().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private boolean showCovers() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Resources resources = getResources();
        return sharedPreferences.getBoolean(resources.getString(R.string.settings_library_show_covers_for_all_albums_key), true);
    }
}
