package net.prezz.mpr.ui.library;

import net.prezz.mpr.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class LibraryPagerAdapter extends FragmentPagerAdapter {

    private Context context;

    public LibraryPagerAdapter(FragmentManager fm, Context context) {
        super(fm);

        this.context = context;
    }

    public String getTitle(int i) {
        switch (i) {
        case 0:
            return context.getString(R.string.library_musicians);
        case 1:
            return context.getString(R.string.library_albums);
        case 2:
            return context.getString(R.string.library_genres);
        case 3:
            return context.getString(R.string.library_grouped);
        case 4:
            return context.getString(R.string.library_files);
        }

        return "";
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
        case 0:
            return new LibraryArtistFragment();
        case 1:
            return new LibraryAlbumFragment();
        case 2:
            return new LibraryGenreFragment();
        case 3:
            return new LibraryGroupedFragment();
        case 4:
            return new LibraryUriFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        return (browseUri()) ? 5 : 4;
    }

    private boolean browseUri() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Resources resources = context.getResources();
        return sharedPreferences.getBoolean(resources.getString(R.string.settings_library_browse_uri_key), true);
    }
}
