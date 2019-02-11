package net.prezz.mpr.ui.player;

import android.view.View;
import net.prezz.mpr.model.PlayerStatus;
import net.prezz.mpr.model.PlaylistEntity;

public interface PlayerFragment {

    void statusUpdated(PlayerStatus status);

    void playlistUpdated(PlaylistEntity[] playlistEntities);

    void onChoiceMenuClick(View view);
}
