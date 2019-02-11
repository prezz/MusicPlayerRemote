package net.prezz.mpr.model.external;

import java.util.List;

public interface CoverService {

    List<String> getCoverUrls(String artist, String album);
}
