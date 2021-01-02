package net.prezz.mpr.mpd.command;

import net.prezz.mpr.model.PlaylistEntity;
import net.prezz.mpr.model.PlaylistEntity.Builder;
import net.prezz.mpr.mpd.MpdPartitionProvider;
import net.prezz.mpr.mpd.connection.MpdConnection;


public class MpdGetPlaylistEntityCommand extends MpdConnectionCommand<Integer, PlaylistEntity>{

    public MpdGetPlaylistEntityCommand(MpdPartitionProvider partitionProvider, int position) {
        super(partitionProvider, Integer.valueOf(position));
    }

    @Override
    protected PlaylistEntity doExecute(MpdConnection connection, Integer position) throws Exception {
        Builder builder = PlaylistEntity.createBuilder();

        boolean exist = false;
        String[] lines = connection.writeResponseCommand(String.format("playlistinfo %s\n", position));
        for (String line : lines) {
            if (line.startsWith("file: ")) {
                builder.setUri(line.substring(6));
                exist = true;
            }
            if (line.startsWith("Id: ")) {
                builder.setId(Integer.decode(line.substring(4)));
            }
            if (line.startsWith("Pos: ")) {
                Integer pos = Integer.decode(line.substring(5));
                builder.setPosition(pos);
            }
            if (line.startsWith("Prio: ")) {
                builder.setPriority(Integer.decode(line.substring(6)));
            }
            if (line.startsWith("Artist: ")) {
                builder.setArtist(line.substring(8));
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

        if (exist) {
            return builder.build();
        }

        return onError();
    }

    @Override
    protected PlaylistEntity onError() {
        return null;
    }
}
