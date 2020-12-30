package net.prezz.mpr.mpd.command;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import net.prezz.mpr.Utils;
import net.prezz.mpr.model.PlaylistEntity;
import net.prezz.mpr.model.PlaylistEntity.Builder;
import net.prezz.mpr.model.StoredPlaylistEntity;
import net.prezz.mpr.mpd.connection.MpdConnection;


public class MpdGetPlaylistDetailsCommand extends MpdConnectionCommand<StoredPlaylistEntity, PlaylistEntity[]>{

    public MpdGetPlaylistDetailsCommand(StoredPlaylistEntity storedPlaylist) {
        super(storedPlaylist);
    }

    @Override
    protected PlaylistEntity[] doExecute(MpdConnection connection, StoredPlaylistEntity storedPlaylist) throws Exception {
        Builder builder = PlaylistEntity.createBuilder();

        List<PlaylistEntity> result = new LinkedList<PlaylistEntity>();

        connection.writeCommand(String.format("listplaylistinfo \"%s\"\n", storedPlaylist.getPlaylistName()));
        int position = -1;
        boolean sort = false;
        boolean add = false;
        String artist = null;
        String line = null;
        while ((line = connection.readLine()) != null) {
            if (line.startsWith(MpdConnection.OK)) {
                break;
            }
            if (line.startsWith(MpdConnection.ACK)) {
                throw new IOException("Error reading MPD response: " + line);
            }

            if (line.startsWith("file: ")) {
                if (add) {
                    builder.setPosition(Integer.valueOf(position));
                    result.add(builder.build());
                }
                position++;
                add = true;
                artist = null;
                builder.clear();
                builder.setUri(line.substring(6));
            }
            if (line.startsWith("Artist: ")) {
                artist = line.substring(8);
                if (!Utils.nullOrEmpty(artist)) {
                    builder.setArtist(artist);
                }
            }
            if (line.startsWith("AlbumArtist: ")) {
                if (Utils.nullOrEmpty(artist)) {
                    builder.setArtist(line.substring(13));
                }
            }
            if (line.startsWith("Album: ")) {
                builder.setAlbum(line.substring(7));
            }
            if (line.startsWith("Disc: ")) {
                builder.setDisc(MpdCommandHelper.getDecimalNumber(line.substring(6)));
            }
            if (line.startsWith("Track: ")) {
                builder.setTrack(MpdCommandHelper.getDecimalNumber(line.substring(7)));
            }
            if (line.startsWith("Title: ")) {
                builder.setTitle(line.substring(7));
            }
            if (line.startsWith("duration: ") && connection.isMinimumVersion(0, 22, 0)) {
                builder.setTime(Math.round(Float.parseFloat(line.substring(10))));
            }
            if (line.startsWith("Time: ") && !connection.isMinimumVersion(0, 22, 0)) { // deprecated
                builder.setTime(Integer.decode(line.substring(6)));
            }
            if (line.startsWith("Name: ")) {
                builder.setName(line.substring(6));
            }
        }
        if (add) {
            result.add(builder.build());
        }

        if (sort) {
            Collections.sort(result, new Comparator<PlaylistEntity>() {
                @Override
                public int compare(PlaylistEntity lhs, PlaylistEntity rhs) {
                    return lhs.getPosition().compareTo(rhs.getPosition());
                }
            });
        }

        return result.toArray(new PlaylistEntity[result.size()]);
    }

    @Override
    protected PlaylistEntity[] onError() {
        return new PlaylistEntity[0];
    }
}
