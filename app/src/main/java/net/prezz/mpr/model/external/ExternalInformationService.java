package net.prezz.mpr.model.external;

import java.io.DataInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import net.prezz.mpr.Utils;
import net.prezz.mpr.model.FutureTaskHandleImpl;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.model.external.cache.CoverCache;
import net.prezz.mpr.model.external.gracenote.GracenoteCoverService;
import net.prezz.mpr.model.external.lastfm.LastFmCoverAndInfoService;
import net.prezz.mpr.model.external.local.MpdCoverService;
import net.prezz.mpr.mpd.connection.MpdConnection;
import net.prezz.mpr.ui.ApplicationActivator;
import net.prezz.mpr.R;
import net.prezz.mpr.ui.mpd.MpdPlayerSettings;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class ExternalInformationService {

    public static final String NULL_URL = "";
    private static final int MAX_HEIGHT = 1024;
    private static final Object lock = new Object();

    private static final String ASCII =
              "AaEeIiOoUu"    // grave
            + "AaEeIiOoUuYy"  // acute
            + "AaEeIiOoUuYy"  // circumflex
            + "AaOoNn"        // tilde
            + "AaEeIiOoUuYy"  // umlaut
            + "Aa"            // ring
            + "Cc"            // cedilla
            + "OoUu"          // double acute
            ;

        private static final String UNICODE =
              "\u00C0\u00E0\u00C8\u00E8\u00CC\u00EC\u00D2\u00F2\u00D9\u00F9"
            + "\u00C1\u00E1\u00C9\u00E9\u00CD\u00ED\u00D3\u00F3\u00DA\u00FA\u00DD\u00FD"
            + "\u00C2\u00E2\u00CA\u00EA\u00CE\u00EE\u00D4\u00F4\u00DB\u00FB\u0176\u0177"
            + "\u00C3\u00E3\u00D5\u00F5\u00D1\u00F1"
            + "\u00C4\u00E4\u00CB\u00EB\u00CF\u00EF\u00D6\u00F6\u00DC\u00FC\u0178\u00FF"
            + "\u00C5\u00E5"
            + "\u00C7\u00E7"
            + "\u0150\u0151\u0170\u0171"
            ;

    private static final CoverCache coverCache = new CoverCache();
    private static final MpdCoverService mpdCoverService = new MpdCoverService();
    private static final LastFmCoverAndInfoService lastFmCoverAndInfoService = new LastFmCoverAndInfoService();
    private static final GracenoteCoverService gracenoteCoverService = new GracenoteCoverService();
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    private ExternalInformationService() {
    }

    public static TaskHandle getCover(final String artist, final String album, final Integer maxHeight, final CoverReceiver coverReceiver) {

        Runnable task = new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    try {
                        String artistParam = (artist != null) ? artist : "";
                        String albumParam = (album != null) ? album : "";

                        byte[] coverData = null;

                        String coverUrl = coverCache.getCoverUrl(artistParam, albumParam);
                        if (coverUrl == null) {
                            coverUrl = getCoverUrlInternal(artistParam, albumParam);

                            if (!NULL_URL.equals(coverUrl)) {
                                coverData = downloadCoverImage(coverUrl);
                                if (coverData != null) {
                                    coverCache.insertCoverImage(coverUrl, coverData);
                                } else {
                                    coverUrl = NULL_URL;
                                }
                            }

                            coverCache.insertCoverUrl(artistParam, albumParam, coverUrl);
                        }

                        if (NULL_URL.equals(coverUrl)) {
                            Bitmap cover = getNoCoverImage(maxHeight);
                            postResult(cover, coverReceiver);
                            return;
                        }

                        if (coverData == null) {
                            coverData = coverCache.getCoverImage(coverUrl);
                        }

                        if (coverData != null) {
                            try {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(coverData, 0, coverData.length);
                                Bitmap cover = scaleImage(maxHeight, bitmap);
                                postResult(cover, coverReceiver);
                                return;
                            } catch (Exception ex) {
                                Log.e(ExternalInformationService.class.getName(), "Error decoding byte array to bitmap", ex);
                                coverData = null;
                            }
                        }

                        coverCache.deleteCoverImageIfLastUsage(coverUrl);
                        coverCache.deleteCoverUrl(artistParam, albumParam);

                        coverUrl = getCoverUrlInternal(artistParam, albumParam);

                        if (!NULL_URL.equals(coverUrl)) {
                            coverData = downloadCoverImage(coverUrl);
                            if (coverData != null) {
                                coverCache.insertCoverImage(coverUrl, coverData);
                            } else {
                                coverUrl = NULL_URL;
                            }
                        }

                        coverCache.insertCoverUrl(artistParam, albumParam, coverUrl);

                        if (NULL_URL.equals(coverUrl)) {
                            Bitmap cover = getNoCoverImage(maxHeight);
                            postResult(cover, coverReceiver);
                            return;
                        }

                        if (coverData != null) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(coverData, 0, coverData.length);
                            Bitmap cover = scaleImage(maxHeight, bitmap);
                            postResult(cover, coverReceiver);
                            return;
                        }

                        Bitmap cover = getNoCoverImage(maxHeight);
                        postResult(cover, coverReceiver);
                        return;
                    } catch (Exception ex) {
                        Log.e(ExternalInformationService.class.getName(), "Error fetching cover", ex);
                    }
                }
            }
        };

        try {
            return new FutureTaskHandleImpl(executor.submit(task));
        } catch (RejectedExecutionException ex) {
            Log.e(ExternalInformationService.class.getName(), "Unable to load cover. Exection rejected", ex);
            return TaskHandle.NULL_HANDLE;
        }
    }

    private static String getCoverUrlInternal(String artistParam, String albumParam) {

        List<String> coverUrlList = new ArrayList<>();

        coverUrlList.addAll(mpdCoverService.getCoverUrls(artistParam, albumParam));
        if (!coverUrlList.isEmpty()) {
            return coverUrlList.get(0);
        }

        List<String> artists = createQueryStrings(artistParam, false);
        List<String> albums = createQueryStrings(albumParam, true);
        artists.add(null);

        for (String artist : artists) {
            for (String album : albums) {
                coverUrlList = lastFmCoverAndInfoService.getCoverUrls(artist, album);
                if (!coverUrlList.isEmpty()) {
                    return coverUrlList.get(0);
                }
                coverUrlList = gracenoteCoverService.getCoverUrls(artist, album);
                if (!coverUrlList.isEmpty()) {
                    return coverUrlList.get(0);
                }
            }
        }

        return NULL_URL;
    }

    public static TaskHandle getCover(final String url, final CoverReceiver coverReceiver) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    try {
                        if (Utils.nullOrEmpty(url)) {
                            Bitmap cover = getNoCoverImage(null);
                            postResult(cover, coverReceiver);
                            return;
                        }

                        byte[] coverData = downloadCoverImage(url);
                        if (coverData == null) {
                            Bitmap cover = getNoCoverImage(null);
                            postResult(cover, coverReceiver);
                            return;
                        }

                        Bitmap bitmap = BitmapFactory.decodeByteArray(coverData, 0, coverData.length);
                        Bitmap cover = scaleImage(null, bitmap);
                        postResult(cover, coverReceiver);
                        return;
                    } catch (Exception ex) {
                        Log.e(ExternalInformationService.class.getName(), "Error fetching cover", ex);
                    }
                }
            }
        };

        try {
            return new FutureTaskHandleImpl(executor.submit(task));
        } catch (RejectedExecutionException ex) {
            Log.e(ExternalInformationService.class.getName(), "Unable to load cover. Exection rejected", ex);
            return TaskHandle.NULL_HANDLE;
        }
    }

    public static TaskHandle getCoverUrls(final String artist, final String album, final UrlReceiver urlReceiver) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    Set<String> result = new LinkedHashSet<String>();

                    try {
                        String artistParam = (artist != null) ? artist : "";
                        String albumParam = (album != null) ? album : "";

                        List<String> mpdUrlList = mpdCoverService.getCoverUrls(artistParam, albumParam);
                        result.addAll(mpdUrlList);

                        List<String> artists = createQueryStrings(artistParam, false);
                        List<String> albums = createQueryStrings(albumParam, true);
                        artists.add(null);

                        for (String artist : artists) {
                            for (String album : albums) {
                                List<String> lastfmUrlList = lastFmCoverAndInfoService.getCoverUrls(artist, album);
                                result.addAll(lastfmUrlList);

                                List<String> gracenoteUrlList = gracenoteCoverService.getCoverUrls(artist, album);
                                result.addAll(gracenoteUrlList);
                            }
                        }
                    } catch (Exception ex) {
                        Log.e(ExternalInformationService.class.getName(), "Error fetching cover", ex);
                    }

                    String[] urls = result.toArray(new String[result.size()]);
                    postResult(urls, urlReceiver);
                    return;
                }
            }
        };

        try {
            return new FutureTaskHandleImpl(executor.submit(task));
        } catch (RejectedExecutionException ex) {
            Log.e(ExternalInformationService.class.getName(), "Unable to load cover. Exection rejected", ex);
            return TaskHandle.NULL_HANDLE;
        }
    }

    public static TaskHandle setCoverUrl(final String artist, final String album, final String url, final Integer maxHeight, final CoverReceiver coverReceiver) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    try {
                        String artistParam = (artist != null) ? artist : "";
                        String albumParam = (album != null) ? album : "";
                        String urlParam = (url != null) ? url : NULL_URL;

                        String existingUrl = coverCache.getCoverUrl(artistParam, albumParam);
                        if (existingUrl != null) {
                            coverCache.deleteCoverImageIfLastUsage(existingUrl);
                            coverCache.deleteCoverUrl(artistParam, albumParam);
                        }
                        coverCache.insertCoverUrl(artistParam, albumParam, urlParam);

                        if (coverReceiver != null) {
                            if (NULL_URL.equals(urlParam)) {
                                Bitmap cover = getNoCoverImage(maxHeight);
                                postResult(cover, coverReceiver);
                                return;
                            }

                            byte[] coverData = downloadCoverImage(urlParam);
                            if (coverData == null) {
                                Bitmap cover = getNoCoverImage(maxHeight);
                                postResult(cover, coverReceiver);
                                return;
                            }
                            coverCache.insertCoverImage(urlParam, coverData);

                            Bitmap bitmap = BitmapFactory.decodeByteArray(coverData, 0, coverData.length);
                            Bitmap cover = scaleImage(maxHeight, bitmap);
                            postResult(cover, coverReceiver);
                            return;
                        }
                    } catch (Exception ex) {
                        Log.e(ExternalInformationService.class.getName(), "Error fetching cover", ex);
                    }
                }
            }
        };

        try {
            return new FutureTaskHandleImpl(executor.submit(task));
        } catch (RejectedExecutionException ex) {
            Log.e(ExternalInformationService.class.getName(), "Unable to load cover. Exection rejected", ex);
            return TaskHandle.NULL_HANDLE;
        }
    }

    public static TaskHandle getArtistInfoUrls(final String artist, final UrlReceiver urlReceiver) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    List<String> result = new ArrayList<String>();
                    try {
                        List<String> artists = createQueryStrings(artist, false);

                        for (String artist : artists) {
                            List<String> infoUrlList = lastFmCoverAndInfoService.getArtistInfoUrls(artist);
                            result.addAll(infoUrlList);
                        }
                    } catch (Exception ex) {
                        Log.e(ExternalInformationService.class.getName(), "Error getting artist info", ex);
                    }

                    String[] urls = result.toArray(new String[result.size()]);
                    postResult(urls, urlReceiver);
                    return;
                }
            }
        };

        try {
            return new FutureTaskHandleImpl(executor.submit(task));
        } catch (RejectedExecutionException ex) {
            Log.e(ExternalInformationService.class.getName(), "Unable to artist info. Exection rejected", ex);
            return TaskHandle.NULL_HANDLE;
        }
    }

    public static TaskHandle getAlbumInfoUrls(final String artist, final String album, final UrlReceiver urlReceiver) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    List<String> result = new ArrayList<String>();
                    try {
                        List<String> artists = createQueryStrings(artist, false);
                        List<String> albums = createQueryStrings(album, true);

                        for (String artist : artists) {
                            for (String album : albums) {
                                List<String> infoUrlList = lastFmCoverAndInfoService.getAlbumInfoUrls(artist, album);
                                result.addAll(infoUrlList);
                            }
                        }
                    } catch (Exception ex) {
                        Log.e(ExternalInformationService.class.getName(), "Error getting album info", ex);
                    }

                    String[] urls = result.toArray(new String[result.size()]);
                    postResult(urls, urlReceiver);
                    return;
                }
            }
        };

        try {
            return new FutureTaskHandleImpl(executor.submit(task));
        } catch (RejectedExecutionException ex) {
            Log.e(ExternalInformationService.class.getName(), "Unable to album info. Exection rejected", ex);
            return TaskHandle.NULL_HANDLE;
        }
    }

    private static void postResult(final Bitmap bitmap, final CoverReceiver coverReceiver) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                coverReceiver.receiveCover(bitmap);
            }
        });
    }

    private static void postResult(final String[] urls, final UrlReceiver urlReceiver) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                urlReceiver.receiveUrls(urls);
            }
        });
    }

    private static Bitmap getNoCoverImage(Integer maxHeight) {
        Bitmap bitmap = BitmapFactory.decodeResource(ApplicationActivator.getContext().getResources(), R.drawable.no_cover);
        return scaleImage(maxHeight, bitmap);
    }

    private static Bitmap scaleImage(Integer scaledHeight, Bitmap coverImage) {
        try {
            int height = (scaledHeight != null) ? Math.min(((Integer)scaledHeight).intValue(), MAX_HEIGHT) : MAX_HEIGHT;
            int width = (int)(((float)height / (float)coverImage.getHeight()) * coverImage.getWidth());
            return Bitmap.createScaledBitmap(coverImage, width, height, true);
        } catch (Exception ex) {
            Log.e(ExternalInformationService.class.getName(), "Error scaling cover", ex);
        }

        return coverImage;
    }

    private static List<String> createQueryStrings(String input, boolean stripParentheseSuffix) {
        List<String> result = new ArrayList<String>();

        if (!Utils.nullOrEmpty(input)) {
            String ascii = convertNonAscii(input);
            if (ascii != null) {
                if (stripParentheseSuffix) {
                    String strippedAsciiParenthese = stripParentheseSuffix(ascii);
                    if (strippedAsciiParenthese != null) {
                        result.add(strippedAsciiParenthese);
                    }
                    String strippedAsciiBracket = stripBracketSuffix(ascii);
                    if (strippedAsciiBracket != null) {
                        result.add(strippedAsciiBracket);
                    }
                }
                result.add(ascii);
            }

            if (stripParentheseSuffix) {
                String strippedParenthese = stripParentheseSuffix(input);
                if (strippedParenthese != null) {
                    result.add(strippedParenthese);
                }
                String strippedBracket = stripBracketSuffix(input);
                if (strippedBracket != null) {
                    result.add(strippedBracket);
                }
            }
            result.add(input);
        }

        return result;
    }

    private static String convertNonAscii(String s) {
        boolean converted = false;

        StringBuilder sb = new StringBuilder();
        int l = s.length();
        for (int i = 0; i < l; i++) {
            char c = s.charAt(i);
            int pos = UNICODE.indexOf(c);
            if (pos > -1) {
                converted = true;
                sb.append(ASCII.charAt(pos));
            } else {
                sb.append(c);
            }
        }

        return (converted) ? sb.toString() : null;
    }

    private static String stripParentheseSuffix(String input) {
        if (input.endsWith(")")) {
             int end = input.lastIndexOf("(");
             if (end > 0) {
                 String stripped = input.substring(0, end);
                 return stripped.trim();
             }
        }

        return null;
    }

    private static String stripBracketSuffix(String input) {
        if (input.endsWith("]")) {
             int end = input.lastIndexOf("[");
             if (end > 0) {
                 String stripped = input.substring(0, end);
                 return stripped.trim();
             }
        }

        return null;
    }

    private static byte[] downloadCoverImage(String coverUrl) {
        if (coverUrl == null) {
            return null;
        }

        if (coverUrl.startsWith("mpd://")) {
            return downloadCoverImageFromMpd(coverUrl.substring(6));
        }

        if (coverUrl.startsWith("http://") || coverUrl.startsWith("https://")) {
            return downloadCoverImageFromHttp(coverUrl);
        }

        return null;
    }

    private static byte[] downloadCoverImageFromMpd(String coverUrl) {
        Context context = ApplicationActivator.getContext();
        MpdPlayerSettings mpdSettings = MpdPlayerSettings.create(context);
        MpdConnection connection = new MpdConnection(mpdSettings);

        try {
            connection.connect();
            if (connection.isMinimumVersion(0, 21, 0)) {
                byte[] buffer = null;
                int size = 0;
                int offset = 0;

                do {
                    connection.writeCommand("albumart \"" + coverUrl + "\" " + offset + "\n");

                    String line = null;
                    while ((line = connection.readLine()) != null) {
                        if (line.startsWith(MpdConnection.OK)) {
                            break;
                        }
                        if (line.startsWith(MpdConnection.ACK)) {
                            break;
                        }
                        if (line.startsWith("size: ") && buffer == null) {
                            size = Integer.parseInt(line.substring(6));
                            buffer = new byte[size];
                        }
                        if (line.startsWith("binary: ") && buffer != null) {
                            int length = Integer.parseInt(line.substring(8));
                            int read = connection.readBinary(buffer, offset, length);
                            offset += read;

                            if (read == 0) {
                                // if we fail to read anything set offset to size to break the loop
                                offset = size;
                            }
                        }
                    }
                } while (offset < size);

                return buffer;
            }
        } catch (Exception ex) {
            Log.e(MpdCoverService.class.getName(), "Error checking if mpd cover exists", ex);
        } finally {
            connection.disconnect();
        }

        return null;
    }

    private static byte[] downloadCoverImageFromHttp(String coverUrl) {
        try {
            URL url = new URL(coverUrl);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            int contentLength = connection.getContentLength();

            DataInputStream stream = new DataInputStream(url.openStream());
            try {
                byte[] buffer = new byte[contentLength];
                stream.readFully(buffer);
                return buffer;
            } finally {
                stream.close();
            }
        } catch (Exception ex) {
            Log.e(ExternalInformationService.class.getName(), "error downloading cover", ex);
        }

        return null;
    }
}
