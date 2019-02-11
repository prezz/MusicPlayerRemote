package net.prezz.mpr.model.external.lastfm;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.prezz.mpr.Utils;
import net.prezz.mpr.model.external.CoverService;
import net.prezz.mpr.model.external.InfoService;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.net.Uri;
import android.net.Uri.Builder;
import android.util.Log;
import android.util.Xml;

public class LastFmCoverAndInfoService implements CoverService, InfoService {

    private static final String VARIOUS_ARTISTS = "Various Artists";
    private static final String URL = "http://ws.audioscrobbler.com/2.0/";
    private static final String API_KEY = "debdc3f32ba3f7b5b4b915da3ce04852";


    @Override
    public List<String> getCoverUrls(String artist, String album) {
        try {
            String request = createCoverRequest(API_KEY, artist, album);
            if (request == null) {
                return Collections.emptyList();
            }

            HttpURLConnection connection = createHttpConnection(request);
            try {
                InputStream inputStream = connection.getInputStream();
                try {
                    return parseCoverResponse(inputStream);
                } finally {
                    inputStream.close();
                }
            } finally {
                connection.disconnect();
            }
        } catch (Exception ex) {
            Log.e(LastFmCoverAndInfoService.class.getName(), "Error getting covers from Last.FM", ex);
        }

        return Collections.emptyList();
    }

    @Override
    public List<String> getArtistInfoUrls(String artist) {
        try {
            String request = createArtistInfoRequest(API_KEY, artist);
            if (request == null) {
                return null;
            }

            HttpURLConnection connection = createHttpConnection(request);
            try {
                InputStream inputStream = connection.getInputStream();
                try {
                    return parseArtistAndAlbumInfoResponse(artist, null, inputStream);
                } finally {
                    inputStream.close();
                }
            } finally {
                connection.disconnect();
            }
        } catch (Exception ex) {
            Log.e(LastFmCoverAndInfoService.class.getName(), "Error searching Last.fm for artist", ex);
        }

        return Collections.emptyList();
    }

    @Override
    public List<String> getAlbumInfoUrls(String artist, String album) {
        try {
            String request = createAlbumInfoRequest(API_KEY, artist, album);
            if (request == null) {
                return null;
            }

            HttpURLConnection connection = createHttpConnection(request);
            try {
                InputStream inputStream = connection.getInputStream();
                try {
                    return parseArtistAndAlbumInfoResponse(album, artist, inputStream);
                } finally {
                    inputStream.close();
                }
            } finally {
                connection.disconnect();
            }
        } catch (Exception ex) {
            Log.e(LastFmCoverAndInfoService.class.getName(), "Error searching Last.fm for album", ex);
        }

        return Collections.emptyList();
    }

    private String createCoverRequest(String apiKey, String artist, String album) {
        if (album != null) {
            Builder builder = Uri.parse(URL).buildUpon();
            builder.appendQueryParameter("method", "album.getinfo");
            builder.appendQueryParameter("api_key", apiKey);
            builder.appendQueryParameter("artist", (Utils.nullOrEmpty(artist)) ? VARIOUS_ARTISTS : artist);
            builder.appendQueryParameter("album", album);
            return builder.build().toString();
        }

        return null;
    }

    private String createArtistInfoRequest(String apiKey, String artist) {
        if (artist != null) {
            Builder builder = Uri.parse(URL).buildUpon();
            builder.appendQueryParameter("method", "artist.search");
            builder.appendQueryParameter("api_key", apiKey);
            builder.appendQueryParameter("artist", artist);
            return builder.build().toString();
        }

        return null;
    }

    private String createAlbumInfoRequest(String apiKey, String artist, String album) {
        if (artist != null && album != null) {
            Builder builder = Uri.parse(URL).buildUpon();
            builder.appendQueryParameter("method", "album.search");
            builder.appendQueryParameter("api_key", apiKey);
            builder.appendQueryParameter("artist", artist);
            builder.appendQueryParameter("album", album);
            return builder.build().toString();
        }

        return null;
    }

    private HttpURLConnection createHttpConnection(String request) throws IOException {
        URL url = new URL(request);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        return connection;
    }

    private List<String> parseCoverResponse(InputStream inputStream) throws XmlPullParserException, IOException {
        List<String> result = new ArrayList<String>();

        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(inputStream, "UTF-8");
        
        String tagName = null;
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
            case XmlPullParser.START_TAG:
                tagName = parser.getName();
                if ("image".equals(tagName)) {
                    String size = parser.getAttributeValue(null, "size");
                    if ("extralarge".equals(size)) {
                         String coverUrl = parser.nextText();
                         if (!Utils.nullOrEmpty(coverUrl) && !result.contains(coverUrl)) {
                             result.add(coverUrl);
                         }
                    }
                }
                break;
            }
            eventType = parser.next();
        }

        return result;
    }

    private List<String> parseArtistAndAlbumInfoResponse(String queryName, String queryArtist, InputStream inputStream) throws XmlPullParserException, IOException {
        LinkedList<String> result = new LinkedList<String>();

        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(inputStream, "UTF-8");
        
        String tagName = null;
        boolean nameMatch = false;
        boolean artistMatch = (queryArtist == null);
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
            case XmlPullParser.START_TAG:
                tagName = parser.getName();

                if ("name".equals(tagName)) {
                     String text = parser.nextText();
                     nameMatch = text.equalsIgnoreCase(queryName);
                }

                if (queryArtist != null && "artist".equals(tagName)) {
                     String text = parser.nextText();
                     artistMatch = text.equalsIgnoreCase(queryArtist);
                }

                if ("url".equals(tagName)) {
                     String text = parser.nextText();
                     if (!Utils.nullOrEmpty(text) && !result.contains(text)) {
                         if (nameMatch && artistMatch) {
                             result.addFirst(text);
                         } else {
                             result.add(text);
                         }
                         nameMatch = false;
                         artistMatch = (queryArtist == null);
                    }
                }
                break;
            }
            eventType = parser.next();
        }

        return result;
    }
}
