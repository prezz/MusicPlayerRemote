package net.prezz.mpr.mpd.database;

import net.prezz.mpr.Utils;
import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.UriEntity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Iterator;
import java.util.SortedSet;

public class MpdLibraryDatabaseHelper extends SQLiteOpenHelper {

    public static final String LIBRARY_FILE_DB_POSTFIX = "_library.db";


    public MpdLibraryDatabaseHelper(Context context, String host) {
        super(context, host + LIBRARY_FILE_DB_POSTFIX, null, 8);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        doCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS music_entities");
        db.execSQL("DROP TABLE IF EXISTS playlist_entities");
        doCreate(db);
    }

    public void cleanDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS music_entities");
        db.execSQL("DROP TABLE IF EXISTS playlist_entities");
        doCreate(db);
    }

    public void beginTransaction() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
    }

    public void setTransactionSuccessful() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.setTransactionSuccessful();
    }

    public void endTransaction() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.endTransaction();
    }

    public void addMusicEntity(MusicLibraryRecord record) {
        ContentValues values = new ContentValues();
        values.put("artist", record.getArtist());
        values.put("meta_artist", record.getMetaArtist());
        values.put("album_artist", record.getAlbumArtist());
        values.put("meta_album_artist", record.getMetaAlbumArtist());
        values.put("composer", record.getComposer());
        values.put("album", record.getAlbum());
        values.put("meta_album", record.getMetaAlbum());
        values.put("title", record.getTitle());
        values.put("disc", record.getDisc());
        values.put("track", record.getTrack());
        values.put("genre", record.getGenre());
        values.put("year", record.getYear());
        values.put("length", record.getLength());
        values.put("uri", record.getUri());

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert("music_entities", null, values);
    }

    public void addPlaylistEntity(String uri) {
        ContentValues values = new ContentValues();
        values.put("uri", uri);

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert("playlist_entities", null, values);
    }

    public int getRowCount() {
        int result = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT count(*) FROM music_entities", null);
        try {
            if (c.moveToFirst()) {
                result += c.isNull(0) ? 0 : c.getInt(0);
            }
        } finally {
            c.close();
        }

        c = db.rawQuery("SELECT count(*) FROM playlist_entities", null);
        try {
            if (c.moveToFirst()) {
                result += c.isNull(0) ? 0 : c.getInt(0);
            }
        } finally {
            c.close();
        }

        return result;
    }

    public Cursor selectAlbums(boolean orderByArtist, LibraryEntity entity) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (orderByArtist) {
            return db.rawQuery(String.format("SELECT album, meta_album, group_concat(a, ', '), group_concat(ma, ', ') g, count(a) FROM (SELECT DISTINCT album, meta_album, artist a, meta_artist ma FROM music_entities %s ORDER BY track, meta_artist, title) GROUP BY album ORDER BY g COLLATE NOCASE asc, album COLLATE NOCASE asc", buildFilter(null, entity)), null);
        } else {
            return db.rawQuery(String.format("SELECT album, meta_album, group_concat(a, ', '), group_concat(ma, ', '), count(a) FROM (SELECT DISTINCT album, meta_album, artist a, meta_artist ma FROM music_entities %s ORDER BY track, meta_artist, title) GROUP BY meta_album ORDER BY meta_album COLLATE NOCASE asc", buildFilter(null, entity)), null);
        }
    }

    public Cursor selectArtists(LibraryEntity entity) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(String.format("SELECT artist, meta_artist, count(title) FROM music_entities %s GROUP BY meta_artist ORDER BY meta_artist COLLATE NOCASE asc", buildFilter(null, entity)), null);
    }

    public Cursor selectAlbumArtists(LibraryEntity entity) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(String.format("SELECT album_artist, meta_album_artist, count(title) FROM music_entities %s GROUP BY meta_album_artist ORDER BY meta_album_artist COLLATE NOCASE asc", buildFilter("WHERE album_artist IS NOT NULL", entity)), null);
    }

    public Cursor selectComposers(LibraryEntity entity) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(String.format("SELECT composer, count(title) FROM music_entities %s GROUP BY composer ORDER BY composer COLLATE NOCASE asc", buildFilter("WHERE composer IS NOT NULL", entity)), null);
    }

    public Cursor selectGenres(LibraryEntity entity) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(String.format("SELECT genre, count(a), sum(aa), sum(c) FROM (SELECT genre, artist a, count(DISTINCT album_artist) aa, count(DISTINCT composer) c FROM music_entities %s GROUP BY genre, artist) GROUP BY genre ORDER BY genre COLLATE NOCASE asc", buildFilter(null, entity)), null);
    }

    public Cursor selectFilteredAlbumsWithStatistics(LibraryEntity entity) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(String.format("SELECT album, meta_album, group_concat(a, ', '), count(a), sum(t), sum(l) FROM (SELECT DISTINCT album, meta_album, artist a, count(title) t, sum(length) l FROM music_entities %s GROUP BY album, meta_album, artist) GROUP BY meta_album ORDER BY meta_album COLLATE NOCASE asc", buildFilter(null, entity)), null);
    }

    public Cursor selectFilteredArtistTitles(LibraryEntity entity) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(String.format("SELECT title, album, length, year, genre FROM music_entities %s ORDER BY title COLLATE NOCASE asc", buildFilter(null, entity)), null);
    }

    public Cursor selectFilteredArtists(LibraryEntity entity) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(String.format("SELECT artist, meta_artist, count(t) FROM (SELECT artist, meta_artist, title t FROM music_entities %s) GROUP BY meta_artist ORDER BY meta_artist COLLATE NOCASE asc", buildFilter(null, entity)), null);
    }

    public Cursor selectFilteredAlbumArtists(LibraryEntity entity) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(String.format("SELECT album_artist, meta_album_artist, count(t) FROM (SELECT album_artist, meta_album_artist, title t FROM music_entities %s AND album_artist NOT NULL) GROUP BY meta_album_artist ORDER BY meta_album_artist COLLATE NOCASE asc", buildFilter(null, entity)), null);
    }

    public Cursor selectFilteredComposers(LibraryEntity entity) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(String.format("SELECT composer, count(t) FROM (SELECT composer, title t FROM music_entities %s AND composer NOT NULL) GROUP BY composer ORDER BY composer COLLATE NOCASE asc", buildFilter(null, entity)), null);
    }

    public Cursor selectFilteredAlbumTitles(LibraryEntity entity) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(String.format("SELECT disc, track, title, artist, meta_artist, album_artist, meta_album_artist, composer, length, year, genre FROM music_entities %s ORDER BY disc, track, meta_artist COLLATE NOCASE asc, title COLLATE NOCASE asc", buildFilter(null, entity)), null);
    }

    public Cursor selectMusicEntitiesRootUri(SortedSet<String> uriFilter) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(String.format("SELECT uri FROM music_entities %s", buildFilter(null, uriFilter)), null);
    }

    public Cursor selectMusicEntitiesUri(String uriPath) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(String.format("SELECT SUBSTR(uri, %s) FROM music_entities WHERE uri LIKE '%s%%'", Integer.valueOf(uriPath.length() + 1), Utils.fixDatabaseQuery(uriPath)), null);
    }

    public Cursor selectPlaylistEntitiesRootUri(SortedSet<String> uriFilter) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(String.format("SELECT uri FROM playlist_entities %s", buildFilter(null, uriFilter)), null);
    }

    public Cursor selectPlaylistEntitiesUri(String uriPath) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(String.format("SELECT SUBSTR(uri, %s) FROM playlist_entities WHERE uri LIKE '%s%%'", Integer.valueOf(uriPath.length() + 1), Utils.fixDatabaseQuery(uriPath)), null);
    }

    public Cursor findArtists(String query, SortedSet<String> uriFilter) {
        SQLiteDatabase db = this.getReadableDatabase();
        String filter = buildFilter(String.format("WHERE artist LIKE '%%%s%%'", Utils.fixDatabaseQuery(query)), uriFilter);
        return db.rawQuery(String.format("SELECT artist, meta_artist, count(title) c FROM music_entities %s GROUP BY meta_artist ORDER BY meta_artist COLLATE NOCASE asc", filter), null);
    }

    public Cursor findAlbumArtists(String query, SortedSet<String> uriFilter) {
        SQLiteDatabase db = this.getReadableDatabase();
        String filter = buildFilter(String.format("WHERE album_artist IS NOT NULL AND album_artist LIKE '%%%s%%'", Utils.fixDatabaseQuery(query)), uriFilter);
        return db.rawQuery(String.format("SELECT album_artist, meta_album_artist, count(title) c FROM music_entities %s GROUP BY meta_album_artist ORDER BY meta_album_artist COLLATE NOCASE asc", filter), null);
    }

    public Cursor findComposers(String query, SortedSet<String> uriFilter) {
        SQLiteDatabase db = this.getReadableDatabase();
        String filter = buildFilter(String.format("WHERE composer IS NOT NULL AND composer LIKE '%%%s%%'", Utils.fixDatabaseQuery(query)), uriFilter);
        return db.rawQuery(String.format("SELECT composer, count(title) c FROM music_entities %s GROUP BY composer ORDER BY composer COLLATE NOCASE asc", filter), null);
    }

    public Cursor findAlbums(String query, SortedSet<String> uriFilter) {
        SQLiteDatabase db = this.getReadableDatabase();
        String filter = buildFilter(String.format("WHERE album LIKE '%%%s%%'", Utils.fixDatabaseQuery(query)), uriFilter);
        return db.rawQuery(String.format("SELECT album, meta_album, group_concat(a, ', '), group_concat(ma, ', ') FROM (SELECT DISTINCT album, meta_album, artist a, meta_artist ma FROM music_entities %s) GROUP BY meta_album ORDER BY meta_album COLLATE NOCASE asc", filter), null);
    }

    public Cursor findTitles(String query, SortedSet<String> uriFilter) {
        SQLiteDatabase db = this.getReadableDatabase();
        String filter = buildFilter(String.format("WHERE title LIKE '%%%s%%'", Utils.fixDatabaseQuery(query)), uriFilter);
        return db.rawQuery(String.format("SELECT title, artist, album FROM music_entities %s ORDER BY title COLLATE NOCASE asc", filter), null);
    }

    public Cursor findUri(String query, SortedSet<String> uriFilter) {
        SQLiteDatabase db = this.getReadableDatabase();
        String filter = buildFilter(String.format("WHERE uri LIKE '%%%s%%'", Utils.fixDatabaseQuery(query)), uriFilter);
        return db.rawQuery(String.format("SELECT uri FROM music_entities %s ORDER BY uri COLLATE NOCASE asc", filter), null);
    }

    public Cursor findUris(String artist, String album) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (Utils.nullOrEmpty(artist)) {
            return db.rawQuery(String.format("SELECT uri FROM music_entities WHERE album='%s'", Utils.fixDatabaseQuery(album)), null);
        } else {
            return db.rawQuery(String.format("SELECT uri FROM music_entities WHERE artist='%s' AND album='%s'", Utils.fixDatabaseQuery(artist), Utils.fixDatabaseQuery(album)), null);
        }
    }

    private String buildFilter(String prefix, LibraryEntity entity) {

        StringBuilder stringBuilder = new StringBuilder();

        if (!Utils.nullOrEmpty(prefix)) {
            stringBuilder.append(prefix);
        }

        if (entity == null) {
            return stringBuilder.toString();
        }

        if (entity.getArtist() != null) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("WHERE");
            } else {
                stringBuilder.append(" AND");
            }

            stringBuilder.append(" artist='");
            stringBuilder.append(Utils.fixDatabaseQuery(entity.getArtist()));
            stringBuilder.append("'");
        }

        if (entity.getAlbumArtist() != null) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("WHERE");
            } else {
                stringBuilder.append(" AND");
            }

            stringBuilder.append(" album_artist='");
            stringBuilder.append(Utils.fixDatabaseQuery(entity.getAlbumArtist()));
            stringBuilder.append("'");
        }

        if (entity.getComposer() != null) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("WHERE");
            } else {
                stringBuilder.append(" AND");
            }

            stringBuilder.append(" composer='");
            stringBuilder.append(Utils.fixDatabaseQuery(entity.getComposer()));
            stringBuilder.append("'");
        }

        if (entity.getAlbum() != null) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("WHERE");
            } else {
                stringBuilder.append(" AND");
            }

            stringBuilder.append(" album='");
            stringBuilder.append(Utils.fixDatabaseQuery(entity.getAlbum()));
            stringBuilder.append("'");
        }

        if (entity.getGenre() != null) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("WHERE");
            } else {
                stringBuilder.append(" AND");
            }

            stringBuilder.append(" genre='");
            stringBuilder.append(Utils.fixDatabaseQuery(entity.getGenre()));
            stringBuilder.append("'");
        }

        if (entity.getTitle() != null) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("WHERE");
            } else {
                stringBuilder.append(" AND");
            }

            stringBuilder.append(" title='");
            stringBuilder.append(Utils.fixDatabaseQuery(entity.getTitle()));
            stringBuilder.append("'");
        }

        if (entity.getUriEntity() != null) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append("WHERE");
            } else {
                stringBuilder.append(" AND");
            }

            UriEntity uriEntity = entity.getUriEntity();
            stringBuilder.append(String.format(" uri LIKE '%s%%'", Utils.fixDatabaseQuery(uriEntity.getFullUriPath(true))));
        }

        return buildFilter(stringBuilder.toString(), entity.getUriFilter());
    }

    private String buildFilter(String prefix, SortedSet<String> uriFilter) {
        StringBuilder stringBuilder = new StringBuilder();

        if (!Utils.nullOrEmpty(prefix)) {
            stringBuilder.append(prefix);
        }

        if (uriFilter != null && uriFilter.size() > 0) {
            Iterator<String> it = uriFilter.iterator();

            if (it.hasNext()) {
                if (stringBuilder.length() == 0) {
                    stringBuilder.append("WHERE (");
                } else {
                    stringBuilder.append(" AND (");
                }

                stringBuilder.append("uri LIKE '");
                stringBuilder.append(Utils.fixDatabaseQuery(it.next()));
                stringBuilder.append("%'");
            }

            while (it.hasNext()) {
                stringBuilder.append(" OR uri LIKE '");
                stringBuilder.append(Utils.fixDatabaseQuery(it.next()));
                stringBuilder.append("%'");
            }

            stringBuilder.append(")");
        }

        return stringBuilder.toString();
    }

    private void doCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE music_entities (id INTEGER PRIMARY KEY, artist TEXT, meta_artist TEXT, album_artist TEXT, meta_album_artist TEXT, composer TEXT, album TEXT, meta_album TEXT, title TEXT, disc INTEGER, track INTEGER, genre TEXT, year INTEGER, length INTEGER, uri TEXT)");
        db.execSQL("CREATE INDEX idx_artist ON music_entities (artist);");
        db.execSQL("CREATE INDEX idx_album_artist ON music_entities (album_artist);");
        db.execSQL("CREATE INDEX idx_composer ON music_entities (composer);");
        db.execSQL("CREATE INDEX idx_album ON music_entities (album);");
        db.execSQL("CREATE INDEX idx_genre ON music_entities (genre);");
        db.execSQL("CREATE INDEX idx_title ON music_entities (title);");
        db.execSQL("CREATE INDEX idx_uri ON music_entities (uri);");

        db.execSQL("CREATE TABLE playlist_entities (id INTEGER PRIMARY KEY, uri TEXT)");
    }
}
