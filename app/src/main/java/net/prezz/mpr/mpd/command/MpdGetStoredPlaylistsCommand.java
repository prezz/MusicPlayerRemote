package net.prezz.mpr.mpd.command;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.util.Log;

import net.prezz.mpr.model.StoredPlaylistEntity;
import net.prezz.mpr.mpd.MpdPartitionProvider;
import net.prezz.mpr.mpd.connection.MpdConnection;


public class MpdGetStoredPlaylistsCommand extends MpdConnectionCommand<Void, StoredPlaylistEntity[]>{

    public MpdGetStoredPlaylistsCommand(MpdPartitionProvider partitionProvider) {
        super(partitionProvider, null);
    }

    @Override
    protected StoredPlaylistEntity[] doExecute(MpdConnection connection, Void param) throws Exception {
        String[] lines = connection.writeResponseCommand("listplaylists\n");

        List<StoredPlaylistEntity> result = new ArrayList<StoredPlaylistEntity>();

        for (String line : lines) {
            if (line.startsWith("playlist: ")) {
                String playlistName = line.substring(10);
                result.add(new StoredPlaylistEntity(playlistName));
            }
        }

        Collections.sort(result, new Comparator<StoredPlaylistEntity>() {
            @Override
            public int compare(StoredPlaylistEntity lhs, StoredPlaylistEntity rhs) {
                return lhs.getPlaylistName().compareToIgnoreCase(rhs.getPlaylistName());
            }
        });

        return result.toArray(new StoredPlaylistEntity[result.size()]);
    }

    @Override
    protected StoredPlaylistEntity[] onError() {
        return new StoredPlaylistEntity[0];
    }

}
