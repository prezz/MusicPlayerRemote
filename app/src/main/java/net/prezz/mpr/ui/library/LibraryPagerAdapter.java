package net.prezz.mpr.ui.library;

import net.prezz.mpr.R;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class LibraryPagerAdapter extends FragmentPagerAdapter {

    public static final int FRAGMENT_COUNT = 4;

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
            return new LibraryUriFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        return FRAGMENT_COUNT;
    }
}
