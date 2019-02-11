package net.prezz.mpr.mpd.command;

import net.prezz.mpr.model.Statistics;
import net.prezz.mpr.mpd.connection.MpdConnection;


public class MpdGetStatisticsCommand extends MpdConnectionCommand<Void, Statistics>{

    public MpdGetStatisticsCommand() {
        super(null);
    }

    @Override
    protected Statistics doExecute(MpdConnection connection, Void param) throws Exception {
        String[][] lines = connection.writeResponseCommandList(new String[] {"stats\n", "status\n" });

        Statistics result = new Statistics();

        for (String line : lines[0]) {
            if (line.startsWith("artists: ")) {
                result.setArtists(Integer.decode(line.substring(9)));
            }
            if (line.startsWith("albums: ")) {
                result.setAlbums(Integer.decode(line.substring(8)));
            }
            if (line.startsWith("songs: ")) {
                result.setSongs(Integer.decode(line.substring(7)));
            }
        }

        for (String line : lines[1]) {
            if (line.startsWith("updating_db: ")) {
                result.setUpdatingJob(Integer.decode(line.substring(13)));
            }
        }

        return result;
    }

    @Override
    protected Statistics onError() {
        return null;
    }
}
