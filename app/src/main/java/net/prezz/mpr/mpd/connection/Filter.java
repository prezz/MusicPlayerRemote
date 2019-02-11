package net.prezz.mpr.mpd.connection;

public interface Filter {

    boolean accepts(String line);
}
