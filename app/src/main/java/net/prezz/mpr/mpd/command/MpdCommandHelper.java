package net.prezz.mpr.mpd.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

import net.prezz.mpr.Utils;
import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.model.UriEntity.UriType;

public class MpdCommandHelper {

    private static final UriComparator URI_COMPARATOR = new UriComparator();


    private MpdCommandHelper() {
        //prevent instantiation
    }

    public static Integer getDecimalNumber(String value) {
        if (!value.isEmpty()) {
            try {
                int idx = value.indexOf('/');
                return (idx != -1) ? Integer.decode(value.substring(0, idx)) : Integer.decode(value);
            } catch (NumberFormatException ex) {
            }
        }

        return null;
    }

    public static List<String> createQuery(String prefix, LibraryEntity entity) {

        if (entity.getUriEntity() != null) {
            UriEntity uriEntity = entity.getUriEntity();
            return Collections.singletonList(createQuery(prefix, fixQuery(uriEntity.getFullUriPath(false)), entity));
        }

        SortedSet<String> uriFilter = entity.getUriFilter();
        if (uriFilter != null && uriFilter.size() > 0) {

            List<String> result = new ArrayList<>();
            for (String filter : uriFilter) {
                String uri = filter;
                if (!Utils.nullOrEmpty(uri) && uri.endsWith(UriEntity.DIR_SEPERATOR)) {
                    uri = uri.substring(0, uri.length() - 1);
                }
                result.add(createQuery(prefix, uri, entity));
            }
            return Collections.unmodifiableList(result);
        }

        return Collections.singletonList(createQuery(prefix, null, entity));
    }

    public static String createQuery(String prefix, String uriPath, LibraryEntity entity) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(prefix);

        if (uriPath != null) {
            stringBuilder.append(" base \"");
            stringBuilder.append(uriPath);
            stringBuilder.append("\"");
        }

        if (entity.getArtist() != null) {
            stringBuilder.append(" Artist \"");
            stringBuilder.append(fixQuery(entity.getArtist()));
            stringBuilder.append("\"");
        }

        if (entity.getAlbumArtist() != null) {
            stringBuilder.append(" AlbumArtist \"");
            stringBuilder.append(fixQuery(entity.getAlbumArtist()));
            stringBuilder.append("\"");
        }

        if (entity.getComposer() != null) {
            stringBuilder.append(" Composer \"");
            stringBuilder.append(fixQuery(entity.getComposer()));
            stringBuilder.append("\"");
        }

        if (entity.getAlbum() != null) {
            stringBuilder.append(" Album \"");
            stringBuilder.append(fixQuery(entity.getAlbum()));
            stringBuilder.append("\"");
        }

        if (entity.getGenre() != null) {
            stringBuilder.append(" Genre \"");
            stringBuilder.append(fixQuery(entity.getGenre()));
            stringBuilder.append("\"");
        }

        if (entity.getTitle() != null) {
            stringBuilder.append(" Title \"");
            stringBuilder.append(fixQuery(entity.getTitle()));
            stringBuilder.append("\"");
        }

        stringBuilder.append("\n");

        return stringBuilder.toString();
    }

    public static String fixQuery(String input) {
        if (input.contains("\"")) {
            StringBuilder sb = new StringBuilder();
            for (char c : input.toCharArray()) {
                if ('"' == c) {
                    sb.append("\\");
                }
                sb.append(c);
            }

            return sb.toString();
        }
        return input;
    }

    public static Comparator<UriEntity> getUriComparator() {
        return URI_COMPARATOR;
    }

    private static final class UriComparator implements Comparator<UriEntity> {

        @Override
        public int compare(UriEntity lhs, UriEntity rhs) {
            if (lhs.getUriType() == UriType.DIRECTORY && rhs.getUriType() != UriType.DIRECTORY) {
                return -1;
            }
            if (lhs.getUriType() != UriType.DIRECTORY && rhs.getUriType() == UriType.DIRECTORY) {
                return 1;
            }

            return lhs.getUriPath().compareToIgnoreCase(rhs.getUriPath());
        }
    }
}
