package net.prezz.mpr.mpd.database;

import net.prezz.mpr.Utils;


public class MusicLibraryRecord { 

	private String artist;
    private String metaArtist;
	private String albumArtist;
    private String metaAlbumArtist;
	private String composer;
	private String album;
    private String metaAlbum;
	private String title;
	private Integer track;
	private String genre;
	private Integer year;
	private Integer length;
	private String uri;
	
	
	public MusicLibraryRecord() {
		clear();
	}
	
	public void clear() {
		this.artist = "";
        this.metaArtist = "";
		this.albumArtist = null;
        this.metaAlbumArtist = null;
		this.composer = null;
		this.album = "";
        this.metaAlbum = "";
		this.title = "";
		this.track = null;
		this.genre = "";
		this.year = null;
		this.length = null;
		this.uri = "";
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

    public String getMetaArtist() {
        return metaArtist;
    }

    public void setMetaArtist(String metaArtist) {
        this.metaArtist = metaArtist;
    }

	public String getAlbumArtist() {
		return Utils.equals(albumArtist, artist) ? null : albumArtist;
	}

	public void setAlbumArtist(String albumArtist) {
		this.albumArtist = albumArtist;
	}

    public String getMetaAlbumArtist() {
        return Utils.equals(metaAlbumArtist, metaArtist) ? null : metaAlbumArtist;
    }

    public void setMetaAlbumArtist(String metaAlbumArtist) {
        this.metaAlbumArtist = metaAlbumArtist;
    }

	public String getComposer() {
		return (Utils.equals(composer, artist) || Utils.equals(composer, albumArtist)) ? null : composer;
	}

	public void setComposer(String composer) {
		this.composer = composer;
	}
	
	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

    public String getMetaAlbum() {
        return metaAlbum;
    }

    public void setMetaAlbum(String metaAlbum) {
        this.metaAlbum = metaAlbum;
    }

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getTrack() {
		return track;
	}

	public void setTrack(Integer track) {
		this.track = track;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public String getUri() {
		return uri;
	}
	
	public void setUri(String uri) {
		this.uri = uri;
	}
}
