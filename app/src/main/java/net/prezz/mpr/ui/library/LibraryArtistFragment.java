package net.prezz.mpr.ui.library;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.LibraryEntity.Tag;
import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.ui.adapter.AdapterEntity;
import net.prezz.mpr.ui.adapter.ArtistAdapterEntity;
import net.prezz.mpr.ui.adapter.LibraryArrayAdapter;
import net.prezz.mpr.ui.adapter.SectionAdapterEntity;
import net.prezz.mpr.ui.adapter.SectionSortedAdapterIndexStrategy;
import net.prezz.mpr.ui.library.filtered.FilteredActivity;
import net.prezz.mpr.ui.library.filtered.FilteredAlbumAndTitleActivity;
import net.prezz.mpr.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

public class LibraryArtistFragment extends LibraryFragment {

    private static final int FRAGMENT_POSITION = 0;

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        AdapterEntity adapterEntity = getAdapterEntity(position);
        if (adapterEntity instanceof ArtistAdapterEntity) {
            ArtistAdapterEntity libraryAdapterEntity = (ArtistAdapterEntity)adapterEntity;
            LibraryEntity entity = libraryAdapterEntity.getEntity();

            Intent intent = new Intent(getActivity(), FilteredAlbumAndTitleActivity.class);
            Bundle args = new Bundle();
            args.putString(FilteredActivity.TITLE_ARGUMENT_KEY, libraryAdapterEntity.getEntityText());
            args.putSerializable(FilteredActivity.ENTITY_ARGUMENT_KEY, entity);
            intent.putExtras(args);
            startActivity(intent);
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
        return MusicPlayerControl.getAllArtistsFromLibrary(uriEntityFilter, hiddenUriFolders, responseReceiver);
    }

    @Override
    protected AdapterEntity[] createAdapterEntities(LibraryEntity[] entities) {
        ArrayList<AdapterEntity> result = new ArrayList<AdapterEntity>(entities.length + 3);

        boolean addArtistSection = true;
        boolean addAlbumArtistSection = true;
        boolean addComposerSection = true;
        for (int i = 0; i < entities.length; i++) {
            if (entities[i].getTag() == Tag.ARTIST) {
                if (addArtistSection) {
                    result.add(new SectionAdapterEntity(getString(R.string.library_artists)));
                    addArtistSection = false;
                }
                result.add(new ArtistAdapterEntity(entities[i]));
            }
            if (entities[i].getTag() == Tag.ALBUM_ARTIST) {
                if (addAlbumArtistSection) {
                    result.add(new SectionAdapterEntity(getString(R.string.library_album_artists)));
                    addAlbumArtistSection = false;
                }
                result.add(new ArtistAdapterEntity(entities[i]));
            }
            if (entities[i].getTag() == Tag.COMPOSER) {
                if (addComposerSection) {
                    result.add(new SectionAdapterEntity(getString(R.string.library_composers)));
                    addComposerSection = false;
                }
                result.add(new ArtistAdapterEntity(entities[i]));
            }
        }

        return result.toArray(new AdapterEntity[result.size()]);
    }

    @Override
    protected LibraryArrayAdapter createAdapter(AdapterEntity[] adapterEntities) {
        return new LibraryArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, adapterEntities, SectionSortedAdapterIndexStrategy.INSTANCE, false);
    }
}
