package net.prezz.mpr.mpd.command;

import java.util.ArrayList;
import java.util.List;

import net.prezz.mpr.Utils;
import net.prezz.mpr.model.AudioOutput;
import net.prezz.mpr.mpd.MpdPartitionProvider;
import net.prezz.mpr.mpd.connection.MpdConnection;


public class MpdGetOutputsCommand extends MpdConnectionCommand<Boolean, AudioOutput[]>{

    public MpdGetOutputsCommand(MpdPartitionProvider partitionProvider, boolean all) {
        super(partitionProvider, Boolean.valueOf(all));
    }

    @Override
    protected AudioOutput[] doExecute(MpdConnection connection, Boolean all) throws Exception {
        String[] lines = connection.writeResponseCommand("outputs\n");

        List<AudioOutput> result = new ArrayList<AudioOutput>();

        String outputId = null;
        String outputName = null;
        String plugin = null;
        Boolean outputEnabled = null;
        for (String line : lines) {
            if (line.startsWith("outputid: ")) {
                outputId = line.substring(10);
            }

            if (line.startsWith("outputname: ")) {
                outputName = line.substring(12);
            }

            if (line.startsWith("plugin: ")) {
                plugin = line.substring(8);
            }

            if (line.startsWith("outputenabled: ")) {
                String s = line.substring(15);
                outputEnabled = Boolean.valueOf("1".equals(s));
            }

            if (outputId != null && outputName != null && plugin != null && outputEnabled != null) {
                if (all == Boolean.TRUE || !Utils.equals(plugin, "dummy")) {
                    result.add(new AudioOutput(outputId, outputName, plugin, outputEnabled));
                }
                outputId = null;
                outputName = null;
                plugin = null;
                outputEnabled = null;
            }
        }

        return result.toArray(new AudioOutput[result.size()]);
    }

    @Override
    protected AudioOutput[] onError() {
        return new AudioOutput[0];
    }
}
