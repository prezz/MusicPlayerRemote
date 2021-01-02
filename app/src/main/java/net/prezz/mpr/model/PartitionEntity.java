package net.prezz.mpr.model;

import net.prezz.mpr.Utils;

import java.util.Arrays;

public class PartitionEntity {

    private boolean clientPartition;
    private String partitionName;
    private AudioOutput[] partitionOutputs;

    public PartitionEntity(boolean clientPartition, String partitionName, AudioOutput[] partitionOutputs) {
        this.clientPartition = clientPartition;
        this.partitionName = partitionName;
        this.partitionOutputs = partitionOutputs;
    }

    public boolean isClientPartition() {
        return clientPartition;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public AudioOutput[] getPartitionOutputs() {
        return partitionOutputs;
    }

    @Override
    public String toString() {
        return partitionName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof PartitionEntity) {
            PartitionEntity other = (PartitionEntity) obj;

            if (this.clientPartition != other.clientPartition) {
                return false;
            }

            if (!Utils.equals(this.partitionName, other.partitionName)) {
                return false;
            }

            if (!Arrays.equals(this.partitionOutputs, other.partitionOutputs)) {
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = 31 * hash + Utils.hashCode(clientPartition);
        hash = 31 * hash + Utils.hashCode(partitionName);
        hash = 31 * hash + Arrays.hashCode(partitionOutputs);

        return hash;
    }
}
