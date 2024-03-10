package net.prezz.mpr.ui.library;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import net.prezz.mpr.R;

class LibraryPagerAdapter extends FragmentStateAdapter {

    public static final int FRAGMENT_COUNT = 4;

    private Context context;

    public LibraryPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);

        this.context = fragmentActivity;
    }

    public String getTitle(int position) {
        switch (position) {
        case 0:
            return context.getString(R.string.library_albums);
        case 1:
            return context.getString(R.string.library_genres);
        case 2:
            return context.getString(R.string.library_musicians);
        case 3:
            return context.getString(R.string.library_files);
        }

        return "";
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
        case 0:
            return new LibraryAlbumFragment();
        case 1:
            return new LibraryGenreFragment();
        case 2:
            return new LibraryArtistFragment();
        case 3:
            return new LibraryUriFragment();
        }

        return null;
    }

    @Override
    public int getItemCount() {
        return FRAGMENT_COUNT;
    }
}
