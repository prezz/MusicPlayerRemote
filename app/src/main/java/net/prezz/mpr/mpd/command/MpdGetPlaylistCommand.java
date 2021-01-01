package net.prezz.mpr.mpd.command;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import net.prezz.mpr.Utils;
import net.prezz.mpr.model.PlaylistEntity;
import net.prezz.mpr.model.PlaylistEntity.Builder;
import net.prezz.mpr.mpd.connection.MpdConnection;


public class MpdGetPlaylistCommand extends MpdConnectionCommand<Void, PlaylistEntity[]>{

    public MpdGetPlaylistCommand(String partition) {
        super(partition, null);
    }

    @Override
    protected PlaylistEntity[] doExecute(MpdConnection connection, Void param) throws Exception {
        Builder builder = PlaylistEntity.createBuilder();

        List<PlaylistEntity> result = new LinkedList<PlaylistEntity>();

        connection.writeCommand("playlistinfo\n");
        int lastPos = 0;
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
                    result.add(builder.build());
                }
                add = false;
                artist = null;
                builder.clear();
                builder.setUri(line.substring(6));
            }
            if (line.startsWith("Id: ")) {
                builder.setId(Integer.decode(line.substring(4)));
                add = true;
            }
            if (line.startsWith("Pos: ")) {
                Integer pos = Integer.decode(line.substring(5));
                if (pos.intValue() < lastPos) {
                    sort = true;
                }
                lastPos = pos.intValue();
                builder.setPosition(pos);
            }
            if (line.startsWith("Prio: ")) {
                builder.setPriority(Integer.decode(line.substring(6)));
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
