package net.prezz.mpr.ui.player;

import net.prezz.mpr.R;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class PlayerPagerAdapter extends FragmentPagerAdapter {

    private Context context;

    public PlayerPagerAdapter(FragmentManager fm, Context context) {
        super(fm);

        this.context = context;
    }

    public String getTitle(int i) {
        switch (i) {
        case 0:
            return context.getString(R.string.player_playlist);
        case 1:
            return context.getString(R.string.player_remote_control);
        }

        return "";
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
        case 0:
            return new PlayerPlaylistFragment();
        case 1:
            return new PlayerControlFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
