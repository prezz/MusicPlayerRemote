package net.prezz.mpr.mpd;

public interface MpdPartitionProvider {

    String getPartition();

    void onValidPartition();

    void onInvalidPartition();
}
