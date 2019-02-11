package net.prezz.mpr.model;

import java.io.Serializable;
import java.util.Date;

public class StoredPlaylistEntity implements Serializable {

    private static final long serialVersionUID = -3601721304916558267L;

    private String playlistName;

    public StoredPlaylistEntity(String playlistName) {
        this.playlistName = playlistName;
    }

    public String getPlaylistName() {
        return playlistName;
    }
}
