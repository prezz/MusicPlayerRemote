package net.prezz.mpr.ui.player;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import net.prezz.mpr.R;

class PlayerPagerAdapter extends FragmentStateAdapter {

    private Context context;

    public PlayerPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);

        this.context = fragmentActivity;
    }

    public String getTitle(int position) {
        switch (position) {
        case 0:
            return context.getString(R.string.player_playlist);
        case 1:
            return context.getString(R.string.player_remote_control);
        }

        return "";
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PlayerPlaylistFragment();
            case 1:
                return new PlayerControlFragment();
        }

        return null;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
