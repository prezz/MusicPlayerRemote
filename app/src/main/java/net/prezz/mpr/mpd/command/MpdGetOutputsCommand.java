package net.prezz.mpr.mpd.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.prezz.mpr.model.AudioOutput;
import net.prezz.mpr.mpd.MpdPartitionProvider;
import net.prezz.mpr.mpd.connection.MpdConnection;


public class MpdGetOutputsCommand extends MpdConnectionCommand<Void, AudioOutput[]>{

    public MpdGetOutputsCommand(MpdPartitionProvider partitionProvider) {
        super(null, partitionProvider);
    }

    @Override
    protected AudioOutput[] doExecute(MpdConnection connection, Void param) throws Exception {
        return getOutputs(connection);
    }

    @Override
    protected AudioOutput[] onError() {
        return new AudioOutput[0];
    }

    protected static AudioOutput[] getOutputs(MpdConnection connection) throws Exception {
        String[] lines = connection.writeResponseCommand("outputs\n");

        List<AudioOutput> result = new ArrayList<AudioOutput>();

        boolean add = false;
        String outputId = null;
        String outputName = null;
        String plugin = "";
        Boolean outputEnabled = null;
        for (String line : lines) {
            if (line.startsWith(MpdConnection.OK)) {
                break;
            }
            if (line.startsWith(MpdConnection.ACK)) {
                throw new IOException("Error reading MPD response: " + line);
            }

            if (line.startsWith("outputid: ")) {
                if (add) {
                    result.add(new AudioOutput(outputId, outputName, plugin, outputEnabled));
                }
                add = false;
                outputId = line.substring(10);
                outputName = null;
                plugin = "";
                outputEnabled = null;
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
                add = true;
            }
        }
        if (add) {
            result.add(new AudioOutput(outputId, outputName, plugin, outputEnabled));
        }

        return result.toArray(new AudioOutput[result.size()]);
    }
}
