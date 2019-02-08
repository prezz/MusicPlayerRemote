package net.prezz.mpr.model;

import java.io.Serializable;

import net.prezz.mpr.model.UriEntity.UriType;

public class PlaylistEntity implements Serializable {

    private static final long serialVersionUID = 1363338950259562011L;

    private Integer id;
    private Integer position;
    private Integer priority;
    private String artist;
    private String album;
    private Integer track;
    private String title;
    private Integer time;
    private String name;
    private UriEntity uriEntity;


    private PlaylistEntity(Integer id, Integer position, Integer priority, String artist, String album, Integer track, String title, Integer time, String name, UriEntity uriEntity) {
        this.id = id;
        this.position = position;
        this.priority = priority;
        this.artist = artist;
        this.album = album;
        this.track = track;
        this.title = title;
        this.time = time;
        this.name = name;
        this.uriEntity = uriEntity;
    }

    public Integer getId() {
        return id;
    }

    public Integer getPosition() {
        return position;
    }

    public Integer getPriority() {
        return priority;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public Integer getTrack() {
        return track;
    }

    public String getTitle() {
        return title;
    }

    public Integer getTime() {
        return time;
    }

    public String getName() {
        return name;
    }

    public UriEntity getUriEntity() {
        return uriEntity;
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s, %s, %s, %s %s %s %s %s", id, position, priority, artist, album, track, title, time, name, uriEntity);
    }

    public static Builder createBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer id;
        private Integer position;
        private Integer priority;
        private String artist;
        private String album;
        private Integer track;
        private String title;
        private Integer time;
        private String name;
        private String uri;

        public Builder clear() {
            id = null;
            position = null;
            priority = null;
            artist = null;
            album = null;
            track = null;
            title = null;
            time = null;
            uri = null;
            return this;
        }

        public Builder setId(Integer id) {
            this.id = id;
            return this;
        }

        public Builder setPosition(Integer position) {
            this.position = position;
            return this;
        }

        public Builder setPriority(Integer priority) {
            this.priority = priority;
            return this;
        }

        public Builder setArtist(String artist) {
            this.artist = artist;
            return this;
        }

        public Builder setAlbum(String album) {
            this.album = album;
            return this;
        }

        public Builder setTrack(Integer track) {
            this.track = track;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setTime(Integer time) {
            this.time = time;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setUri(String uri) {
            this.uri = uri;
            return this;
        }

        public PlaylistEntity build() {
            UriEntity uriEntity = new UriEntity(UriType.FILE, UriEntity.FileType.PLAYLIST, "", uri);
            return new PlaylistEntity(id, position, priority, artist, album, track, title, time, name, uriEntity);
        }
    }
}
