package net.prezz.mpr.mpd.command;

import net.prezz.mpr.Utils;
import net.prezz.mpr.model.AudioOutput;
import net.prezz.mpr.model.PartitionEntity;
import net.prezz.mpr.mpd.connection.MpdConnection;

import java.util.ArrayList;
import java.util.List;


public class MpdGetPartitionsCommand extends MpdConnectionCommand<Void, PartitionEntity[]>{

    private String clientPartition;

    public MpdGetPartitionsCommand(String partition) {
        super(partition, null);
        this.clientPartition = partition;
    }

    @Override
    protected PartitionEntity[] doExecute(MpdConnection connection, Void param) throws Exception {

        if (connection.isMinimumVersion(0, 22, 0)) {

            String[] lines = connection.writeResponseCommand("listpartitions\n");

            List<String> partitions = new ArrayList<String>();

            for (String line : lines) {
                if (line.startsWith("partition: ")) {
                    partitions.add(line.substring(11));
                }
            }

            List<PartitionEntity> result = new ArrayList<PartitionEntity>();

            MpdGetOutputsCommand getOutputsCommand = new MpdGetOutputsCommand(null);
            for (String partition : partitions) {
                connection.setPartition(partition);
                AudioOutput[] audioOutputs = getOutputsCommand.doExecute(connection, null);
                result.add(new PartitionEntity(Utils.equals(clientPartition, partition), partition, audioOutputs));
            }

            return result.toArray(new PartitionEntity[result.size()]);
        } else {
            return new PartitionEntity[0];
        }
    }

    @Override
    protected PartitionEntity[] onError() {
        return new PartitionEntity[0];
    }
}
