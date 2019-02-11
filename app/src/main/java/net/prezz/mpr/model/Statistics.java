package net.prezz.mpr.model;

public class Statistics {

    private Integer artists;
    private Integer albums;
    private Integer songs;
    private Integer updatingJobId;

    public Integer getArtists() {
        return artists;
    }

    public void setArtists(Integer artists) {
        this.artists = artists;
    }

    public Integer getAlbums() {
        return albums;
    }

    public void setAlbums(Integer albums) {
        this.albums = albums;
    }

    public Integer getSongs() {
        return songs;
    }

    public void setSongs(Integer songs) {
        this.songs = songs;
    }

    public void setUpdatingJob(Integer updatingJobId) {
        this.updatingJobId = updatingJobId;
    }

    public Integer getUpdatingJob() {
        return updatingJobId;
    }
}
