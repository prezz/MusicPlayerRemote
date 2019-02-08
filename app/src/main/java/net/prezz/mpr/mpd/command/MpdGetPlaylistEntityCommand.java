package net.prezz.mpr.mpd.command;

import net.prezz.mpr.model.PlaylistEntity;
import net.prezz.mpr.model.PlaylistEntity.Builder;
import net.prezz.mpr.mpd.connection.MpdConnection;


public class MpdGetPlaylistEntityCommand extends MpdConnectionCommand<Integer, PlaylistEntity>{

    public MpdGetPlaylistEntityCommand(int position) {
        super(Integer.valueOf(position));
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
            if (line.startsWith("Track: ")) {
                builder.setTrack(MpdCommandHelper.getTrack(line.substring(7)));
            }
            if (line.startsWith("Title: ")) {
                builder.setTitle(line.substring(7));
            }
            if (line.startsWith("Time: ")) {
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
