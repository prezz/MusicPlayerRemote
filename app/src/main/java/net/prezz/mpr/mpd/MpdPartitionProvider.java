package net.prezz.mpr.mpd;

public interface MpdPartitionProvider {

    String DEFAULT_PARTITION = "default";

    String getPartition();

    void onInvalidPartition();
}
