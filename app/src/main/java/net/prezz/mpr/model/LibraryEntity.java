package net.prezz.mpr.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.SortedSet;

import net.prezz.mpr.Utils;

public class LibraryEntity implements Serializable { 

    private static final long serialVersionUID = -3383200228964358924L;

    //order is important as it impacts the priority used in sorting
    public enum Tag { ARTIST, ALBUM_ARTIST, COMPOSER, ALBUM, TITLE, GENRE };

    private Tag tag;
    private String artist;
    private String albumArtist;
    private String composer;
    private String album;
    private String title;
    private String genre;
    private UriEntity uriEntity;
    private Integer metaDisc;
    private Integer metaTrack;
    private String metaArtist;
    private String metaAlbumArtist;
    private String metaAlbum;
    private String metaGenre;
    private Integer metaCount;
    private Integer metaYear;
    private Integer metaLength;
    private Boolean metaCompilation;
    private String lookupArtist;
    private String lookupAlbum;
    private SortedSet<String> uriFilter;


    private LibraryEntity(Tag tag, String artist, String albumArtist, String composer, String album, String title, String genre, UriEntity uriEntity, Integer metaDisc, Integer metaTrack, String metaArtist, String metaAlbumArtist,
                          String metaAlbum, String metaGenre, Integer metaCount, Integer metaYear, Integer metaLength, Boolean metaCompilation, String lookupArtist, String lookupAlbum, SortedSet<String> uriFilter) {
        this.tag = tag;
        this.artist = artist;
        this.albumArtist = albumArtist;
        this.composer = composer;
        this.album = album;
        this.genre = genre;
        this.title = title;
        this.uriEntity = uriEntity;
        this.metaDisc = metaDisc;
        this.metaTrack = metaTrack;
        this.metaArtist = metaArtist;
        this.metaAlbumArtist = metaAlbumArtist;
        this.metaAlbum = metaAlbum;
        this.metaGenre = metaGenre;
        this.metaCount = metaCount;
        this.metaYear = metaYear;
        this.metaLength = metaLength;
        this.metaCompilation = metaCompilation;
        this.lookupArtist = lookupArtist;
        this.lookupAlbum = lookupAlbum;
        this.uriFilter = uriFilter;
    }

    public Tag getTag() {
        return tag;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbumArtist() {
        return albumArtist;
    }

    public String getComposer() {
        return composer;
    }

    public String getAlbum() {
        return album;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public UriEntity getUriEntity() {
        return uriEntity;
    }

    public Integer getMetaDisc() {
        return metaDisc;
    }

    public Integer getMetaTrack() {
        return metaTrack;
    }

    public String getMetaArtist() {
        return metaArtist;
    }

    public String getMetaAlbumArtist() {
        return metaAlbumArtist;
    }

    public String getMetaAlbum() {
        return metaAlbum;
    }

    public String getMetaGenre() {
        return metaGenre;
    }

    public Integer getMetaCount() {
        return metaCount;
    }

    public Integer getMetaYear() {
        return metaYear;
    }

    public Integer getMetaLength() {
        return metaLength;
    }

    public Boolean getMetaCompilation() {
        return metaCompilation;
    }

    public String getLookupArtist() {
        return lookupArtist;
    }

    public String getLookupAlbum() {
        return lookupAlbum;
    }

    public SortedSet<String> getUriFilter() {
        return (uriFilter != null) ? Collections.unmodifiableSortedSet(uriFilter) : null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof LibraryEntity) {
            LibraryEntity other = (LibraryEntity) obj;

            if (!Utils.equals(this.tag, other.tag)) {
                return false;
            }
            if (!Utils.equals(this.artist, other.artist)) {
                return false;
            }
            if (!Utils.equals(this.albumArtist, other.albumArtist)) {
                return false;
            }
            if (!Utils.equals(this.composer, other.composer)) {
                return false;
            }
            if (!Utils.equals(this.album, other.album)) {
                return false;
            }
            if (!Utils.equals(this.title, other.title)) {
                return false;
            }
            if (!Utils.equals(this.genre, other.genre)) {
                return false;
            }
            if (!Utils.equals(this.uriEntity, other.uriEntity)) {
                return false;
            }
            if (!Utils.equals(this.metaDisc, other.metaDisc)) {
                return false;
            }
            if (!Utils.equals(this.metaTrack, other.metaTrack)) {
                return false;
            }
            if (!Utils.equals(this.metaArtist, other.metaArtist)) {
                return false;
            }
            if (!Utils.equals(this.metaAlbumArtist, other.metaAlbumArtist)) {
                return false;
            }
            if (!Utils.equals(this.metaAlbum, other.metaAlbum)) {
                return false;
            }
            if (!Utils.equals(this.metaGenre, other.metaGenre)) {
                return false;
            }
            if (!Utils.equals(this.metaCount, other.metaCount)) {
                return false;
            }
            if (!Utils.equals(this.metaYear, other.metaYear)) {
                return false;
            }
            if (!Utils.equals(this.metaLength, other.metaLength)) {
                return false;
            }
            if (!Utils.equals(this.metaCompilation, other.metaCompilation)) {
                return false;
            }
            if (!Utils.equals(this.lookupArtist, other.lookupArtist)) {
                return false;
            }
            if (!Utils.equals(this.lookupAlbum, other.lookupAlbum)) {
                return false;
            }
            if (!Utils.equals(this.uriFilter, other.uriFilter)) {
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = 31 * hash + Utils.hashCode(tag);
        hash = 31 * hash + Utils.hashCode(artist);
        hash = 31 * hash + Utils.hashCode(albumArtist);
        hash = 31 * hash + Utils.hashCode(composer);
        hash = 31 * hash + Utils.hashCode(album);
        hash = 31 * hash + Utils.hashCode(title);
        hash = 31 * hash + Utils.hashCode(genre);
        hash = 31 * hash + Utils.hashCode(uriEntity);
        hash = 31 * hash + Utils.hashCode(metaDisc);
        hash = 31 * hash + Utils.hashCode(metaTrack);
        hash = 31 * hash + Utils.hashCode(metaArtist);
        hash = 31 * hash + Utils.hashCode(metaAlbumArtist);
        hash = 31 * hash + Utils.hashCode(metaAlbum);
        hash = 31 * hash + Utils.hashCode(metaGenre);
        hash = 31 * hash + Utils.hashCode(metaCount);
        hash = 31 * hash + Utils.hashCode(metaYear);
        hash = 31 * hash + Utils.hashCode(metaLength);
        hash = 31 * hash + Utils.hashCode(metaCompilation);
        hash = 31 * hash + Utils.hashCode(lookupArtist);
        hash = 31 * hash + Utils.hashCode(lookupAlbum);
        hash = 31 * hash + Utils.hashCode(uriFilter);

        return hash;
    }

    @Override
    public String toString() {
        return String.format("%s %s, %s, %s, %s, %s, %s", tag, artist, album, title, genre, metaDisc, metaTrack);
    }

    public static Builder createBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private static final int MAX_META_SIZE = 200;

        private Tag tag;
        private String artist;
        private String albumArtist;
        private String composer;
        private String album;
        private String title;
        private String genre;
        private UriEntity uriEntity;
        private Integer metaDisc;
        private Integer metaTrack;
        private String metaArtist;
        private String metaAlbumArtist;
        private String metaAlbum;
        private String metaGenre;
        private Integer metaCount;
        private Integer metaYear;
        private Integer metaLength;
        private Boolean metaCompilation;
        private String lookupArtist;
        private String lookupAlbum;
        private SortedSet<String> uriFilter;

        public Builder clear() {
            tag = null;
            artist = null;
            albumArtist = null;
            composer = null;
            album = null;
            title = null;
            genre = null;
            uriEntity = null;
            metaDisc = null;
            metaTrack = null;
            metaArtist = null;
            metaAlbumArtist = null;
            metaAlbum = null;
            metaGenre = null;
            metaCount = null;
            metaYear = null;
            metaLength = null;
            metaCompilation = null;
            lookupArtist = null;
            lookupAlbum = null;
            uriFilter = null;
            return this;
        }

        public Builder setTag(Tag tag) {
            this.tag = tag;
            return this;
        }

        public Builder setArtist(String artist) {
            this.artist = artist;
            return this;
        }

        public Builder setAlbumArtist(String albumArtist) {
            this.albumArtist = albumArtist;
            return this;
        }

        public Builder setComposer(String composer) {
            this.composer = composer;
            return this;
        }

        public Builder setAlbum(String album) {
            this.album = album;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setGenre(String genre) {
            this.genre = genre;
            return this;
        }

        public Builder setUriEntity(UriEntity uriEntity) {
            this.uriEntity = uriEntity;
            return this;
        }

        public Builder setMetaDisc(Integer metaDisc) {
            this.metaDisc = metaDisc;
            return this;
        }

        public Builder setMetaTrack(Integer metaTrack) {
            this.metaTrack = metaTrack;
            return this;
        }

        public Builder setMetaArtist(String metaArtist) {
            this.metaArtist = (metaArtist != null && metaArtist.length() > MAX_META_SIZE) ? metaArtist.substring(0, MAX_META_SIZE) : metaArtist;
            return this;
        }

        public Builder setMetaAlbumArtist(String metaAlbumArtist) {
            this.metaAlbumArtist = (metaAlbumArtist != null && metaAlbumArtist.length() > MAX_META_SIZE) ? metaAlbumArtist.substring(0, MAX_META_SIZE) : metaAlbumArtist;
            return this;
        }

        public Builder setMetaAlbum(String metaAlbum) {
            this.metaAlbum = (metaAlbum != null && metaAlbum.length() > MAX_META_SIZE) ? metaAlbum.substring(0, MAX_META_SIZE) : metaAlbum;
            return this;
        }

        public Builder setMetaGenre(String metaGenre) {
            this.metaGenre = (metaGenre != null && metaGenre.length() > MAX_META_SIZE) ? metaGenre.substring(0, MAX_META_SIZE) : metaGenre;
            return this;
        }

        public Builder setMetaCount(Integer metaCount) {
            this.metaCount = metaCount;
            return this;
        }

        public Builder setMetaYear(Integer metaYear) {
            this.metaYear = metaYear;
            return this;
        }

        public Builder setMetaLength(Integer metaLength) {
            this.metaLength = metaLength;
            return this;
        }

        public Builder setMetaCompilation(Boolean metaCompilation) {
            this.metaCompilation = metaCompilation;
            return this;
        }

        public Builder setLookupArtist(String lookupArtist) {
            this.lookupArtist = lookupArtist;
            return this;
        }

        public Builder setLookupAlbum(String lookupAlbum) {
            this.lookupAlbum = lookupAlbum;
            return this;
        }

        public Builder setUriFilter(SortedSet<String> uriFilter) {
            this.uriFilter = uriFilter;
            return this;
        }

        public LibraryEntity build() {
            return new LibraryEntity(tag, artist, albumArtist, composer, album, title, genre, uriEntity, metaDisc, metaTrack,
                    metaArtist, metaAlbumArtist, metaAlbum, metaGenre, metaCount, metaYear, metaLength, metaCompilation,
                    lookupArtist, lookupAlbum, uriFilter);
        }
    }
}
