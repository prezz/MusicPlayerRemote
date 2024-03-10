package net.prezz.mpr.ui.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import net.prezz.mpr.R;
import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.PlaylistEntity;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.ui.adapter.PlaylistAdapterEntity;

import java.util.ArrayList;
import java.util.List;

public class UpdatePlayDataHelper {

    public static void updatePlayData(final Activity activity, PlaylistAdapterEntity[] adapterEntities) {

        PlaylistEntity[] playlistEntity = new PlaylistEntity[adapterEntities.length];
        for (int i = 0; i < adapterEntities.length; i++) {
            playlistEntity[i] = adapterEntities[i].getEntity();
        }
        updatePlayData(activity, playlistEntity);
    }

    public static void updatePlayData(final Activity activity, PlaylistEntity[] playlistEntities) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(true);
        builder.setTitle(R.string.player_mark_played_header);
        builder.setMessage(R.string.player_mark_played_message);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (playlistEntities != null && playlistEntities.length > 0) {
                    List<PlaylistEntity> entities = new ArrayList<>();
                    for (PlaylistEntity entity : playlistEntities) {
                        entities.add(entity);
                    }
                    MusicPlayerControl.updatePlayData(entities, new ResponseReceiver<Boolean>() {
                        @Override
                        public void receiveResponse(Boolean response) {
                            if (response == Boolean.TRUE) {
                                Boast.makeText(activity, R.string.player_play_data_updated).show();
                            }
                        }
                    });
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
