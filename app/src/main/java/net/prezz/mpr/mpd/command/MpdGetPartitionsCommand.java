package net.prezz.mpr.mpd.command;

import net.prezz.mpr.Utils;
import net.prezz.mpr.model.AudioOutput;
import net.prezz.mpr.model.PartitionEntity;
import net.prezz.mpr.mpd.MpdPartitionProvider;
import net.prezz.mpr.mpd.connection.MpdConnection;

import java.util.ArrayList;
import java.util.List;


public class MpdGetPartitionsCommand extends MpdConnectionCommand<Void, PartitionEntity[]>{

    public MpdGetPartitionsCommand(MpdPartitionProvider partitionProvider) {
        super(partitionProvider, null);
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

            try {
                MpdGetOutputsCommand getOutputsCommand = new MpdGetOutputsCommand(null, false);
                for (String partition : partitions) {
                    if (connection.setPartition(partition)) {
                        boolean isClientPartition = Utils.equals(super.getPartition(), partition);
                        AudioOutput[] audioOutputs = getOutputsCommand.doExecute(connection, Boolean.FALSE);
                        result.add(new PartitionEntity(isClientPartition, partition, audioOutputs));
                    }
                }
            } finally {
                connection.setPartition(super.getPartition());
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
