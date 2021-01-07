package net.prezz.mpr.mpd;

public interface MpdPartitionProvider {

    public static final String DEFAULT_PARTITION = "default";

    String getPartition();

    void onInvalidPartition();
}
