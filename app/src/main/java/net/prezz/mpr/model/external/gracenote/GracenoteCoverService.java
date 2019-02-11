package net.prezz.mpr.model.external.gracenote;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;

import net.prezz.mpr.R;
import net.prezz.mpr.Utils;
import net.prezz.mpr.model.external.CoverService;
import net.prezz.mpr.ui.ApplicationActivator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GracenoteCoverService implements CoverService {

    private static final String VARIOUS_ARTISTS = "Various Artists";
    private static final String URL = "https://c%s.web.cddbp.net/webapi/xml/1.0/";


    public List<String> getCoverUrls(String artist, String album) {
        try {
            String clientId = getClientId();
            if (!Utils.nullOrEmpty(clientId)) {
                String url = getUrl(clientId);
                String userId = getUserId(url, clientId);

                String body = createCoverRequestBody(clientId, userId, artist, album);
                if (body == null) {
                    return Collections.emptyList();
                }

                HttpURLConnection connection = createHttpConnection(url, body);
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
            }
        } catch (Exception ex) {
            Log.e(GracenoteCoverService.class.getName(), "Error getting covers from Gracenote", ex);
        }

        return Collections.emptyList();
    }

    private String getClientId() {
        Context context = ApplicationActivator.getContext();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Resources resources = context.getResources();
        String clientId = sharedPreferences.getString(resources.getString(R.string.settings_covers_gracenote_client_id_key), "");
        return clientId;
    }

    private String getUrl(String clientId) {
        String digits = clientId.substring(0, clientId.indexOf("-"));
        return String.format(URL, digits);
    }

    private String getUserId(String url, String clientId) throws XmlPullParserException, IOException {
        String userId = loadUserId();

        if (Utils.nullOrEmpty(userId)) {
            String body = "<QUERIES>"
                            + "<QUERY CMD=\"REGISTER\">"
                                + "<CLIENT>" + clientId + "</CLIENT>"
                            + "</QUERY>"
                        + "</QUERIES>";

            HttpURLConnection connection = createHttpConnection(url, body);
            try {
                InputStream inputStream = connection.getInputStream();
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    parser.setInput(inputStream, "UTF-8");

                    String tagName = null;
                    int eventType = parser.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_TAG:
                                tagName = parser.getName();
                                if ("USER".equals(tagName)) {
                                    userId = parser.nextText();
                                }
                                break;
                        }
                        eventType = parser.next();
                    }
                } finally {
                    inputStream.close();
                }
            } finally {
                connection.disconnect();
            }

            if (!Utils.nullOrEmpty(userId)) {
                saveUserId(userId);
            }
        }

        return userId;
    }

    private String createCoverRequestBody(String clientId, String userId, String artist, String album) {
        if (album != null) {
            artist = (Utils.nullOrEmpty(artist)) ? VARIOUS_ARTISTS : artist;

            String body = "<QUERIES>"
                            + "<AUTH>"
                                + "<CLIENT>" + clientId + "</CLIENT>"
                                + "<USER>" + userId + "</USER>"
                            + "</AUTH>"
                            + "<QUERY CMD=\"ALBUM_SEARCH\">"
                                + "<MODE>SINGLE_BEST_COVER</MODE>"
                                + "<TEXT TYPE=\"ARTIST\">" + artist + "</TEXT>"
                                + "<TEXT TYPE=\"ALBUM_TITLE\">" + album + "</TEXT>"
                                + "<OPTION>"
                                    + "<PARAMETER>SELECT_EXTENDED</PARAMETER>"
                                    + "<VALUE>COVER,ARTIST_IMAGE</VALUE>"
                                + "</OPTION>"
                                + "<OPTION>"
                                    + "<PARAMETER>COVER_SIZE</PARAMETER>"
                                    + "<VALUE>MEDIUM</VALUE>"
                                + "</OPTION>"
                            + "</QUERY>"
                        + "</QUERIES>";
            return body;
        }

        return null;
    }

    private List<String> parseCoverResponse(InputStream inputStream) throws XmlPullParserException, IOException {
        List<String> result = new ArrayList<String>();

        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(inputStream, "UTF-8");

        String status = null;
        String tagName = null;
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    tagName = parser.getName();
                    if ("RESPONSE".equals(tagName)) {
                        status = parser.getAttributeValue(null, "STATUS");
                    }

                    if ("URL".equals(tagName)) {
                        String type = parser.getAttributeValue(null, "TYPE");
                        if ("COVERART".equals(type)) {
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

        if ("ERROR".equals(status)) {
            saveUserId("");
        }

        return result;
    }

    private HttpURLConnection createHttpConnection(String requestUrl, String body) throws IOException {
        URL url = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/plain");
        connection.setRequestProperty("Charset", "utf-8");
        connection.setRequestProperty("Content-Length", Integer.toString(body.getBytes().length));
        connection.setUseCaches(false);

        BufferedWriter outputWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
        try {
            outputWriter.write(body);
            outputWriter.flush();
        } finally {
            outputWriter.close();
        }

        return connection;
    }

    private String loadUserId() {
        Context context = ApplicationActivator.getContext();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Resources resources = context.getResources();
        String userId = sharedPreferences.getString(resources.getString(R.string.settings_covers_gracenote_user_id_key), "");
        return userId;
    }

    private void saveUserId(String userId) {
        Context context = ApplicationActivator.getContext();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        Resources resources = context.getResources();
        editor.putString(resources.getString(R.string.settings_covers_gracenote_user_id_key), userId);
        editor.commit();
    }
}
