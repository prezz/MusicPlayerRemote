package net.prezz.mpr.model.external;

import java.util.List;


public interface InfoService {

	List<String> getArtistInfoUrls(String artist);

	List<String> getAlbumInfoUrls(String artist, String album);
}
