package net.prezz.mpr.mpd.command;

import net.prezz.mpr.mpd.connection.MpdConnection;

import java.util.ArrayList;
import java.util.List;


public class MpdGetPartitionsCommand extends MpdConnectionCommand<Void, String[]>{

    public MpdGetPartitionsCommand() {
        super(null);
    }

    @Override
    protected String[] doExecute(MpdConnection connection, Void param) throws Exception {

        if (connection.isMinimumVersion(0, 22, 0)) {

            String[] lines = connection.writeResponseCommand("listpartitions\n");

            List<String> result = new ArrayList<String>();

            for (String line : lines) {
                if (line.startsWith("partition: ")) {
                    result.add(line.substring(11));
                }
            }

            return result.toArray(new String[result.size()]);
        } else {
            return new String[0];
        }
    }

    @Override
    protected String[] onError() {
        return new String[0];
    }
}
