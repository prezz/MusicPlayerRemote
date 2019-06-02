package net.prezz.mpr.ui.library;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.ui.adapter.AdapterEntity;
import net.prezz.mpr.ui.adapter.FolderAdapterEntity;
import net.prezz.mpr.ui.adapter.LibraryAdapterEntity;
import net.prezz.mpr.ui.adapter.LibraryArrayAdapter;
import net.prezz.mpr.ui.adapter.SortedAdapterIndexStrategy;
import net.prezz.mpr.ui.library.filtered.FilteredActivity;
import net.prezz.mpr.ui.library.filtered.FilteredAlbumActivity;

import java.util.Set;

public class LibraryFolderFragment extends LibraryFragment {

    private static final int FRAGMENT_POSITION = 3;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LibraryAdapterEntity adapterEntity = (LibraryAdapterEntity)getAdapterEntity(position);
        LibraryEntity entity = adapterEntity.getEntity();

        Intent intent = new Intent(getActivity(), FilteredAlbumActivity.class);
        Bundle args = new Bundle();
        args.putString(FilteredActivity.TITLE_ARGUMENT_KEY, adapterEntity.getText());
        args.putSerializable(FilteredActivity.ENTITY_ARGUMENT_KEY, entity);
        intent.putExtras(args);
        startActivity(intent);
    }

    @Override
    protected int getFragmentPosition() {
        return FRAGMENT_POSITION;
    }

    @Override
    protected TaskHandle getEntities(ResponseReceiver<LibraryEntity[]> responseReceiver) {
        Set<String> hiddenUriFolders = ((LibraryActivity) getActivity()).getUriFilter();
        return MusicPlayerControl.getAllUriPathsFromLibrary(hiddenUriFolders, responseReceiver);
    }

    @Override
    protected AdapterEntity[] createAdapterEntities(LibraryEntity[] entities) {
        AdapterEntity[] result = new AdapterEntity[entities.length];
        for (int i = 0; i < entities.length; i++) {
            result[i] = new FolderAdapterEntity(entities[i]);
        }

        return result;
    }

    @Override
    protected LibraryArrayAdapter createAdapter(AdapterEntity[] adapterEntities) {
        return new LibraryArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, adapterEntities, SortedAdapterIndexStrategy.INSTANCE, false);
    }
}
