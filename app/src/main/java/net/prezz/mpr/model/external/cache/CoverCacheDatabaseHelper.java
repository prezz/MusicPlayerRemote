package net.prezz.mpr.model.external.cache;

import net.prezz.mpr.Utils;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CoverCacheDatabaseHelper extends SQLiteOpenHelper {

	
	public CoverCacheDatabaseHelper(Context context) {
		super(context, "CoverCache.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE album_cover_url (artist TEXT, album TEXT, url TEXT, PRIMARY KEY(artist, album))");
		db.execSQL("CREATE TABLE album_cover_file (url TEXT, filename TEXT, PRIMARY KEY(url))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
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

	public Cursor getCoverUrl(String artist, String album) {
		SQLiteDatabase db = this.getReadableDatabase();
		return db.rawQuery(String.format("SELECT url FROM album_cover_url WHERE artist='%s' AND album ='%s'", Utils.fixDatabaseQuery(artist), Utils.fixDatabaseQuery(album)), null);
	}
	
	public void insertCoverUrl(String artist, String album, String url) {
		ContentValues url_values = new ContentValues();
		url_values.put("artist", artist);
		url_values.put("album", album);
		url_values.put("url", url);
		
		SQLiteDatabase db = this.getWritableDatabase();
		db.insert("album_cover_url", null, url_values);
	}
	
	public void deleteCoverUrl(String artist, String album) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL(String.format("DELETE FROM album_cover_url WHERE artist='%s' AND album ='%s'", Utils.fixDatabaseQuery(artist), Utils.fixDatabaseQuery(album)));
	}

	public Cursor getUrlUseCount(String url) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(String.format("SELECT count(*) FROM album_cover_url WHERE url='%s'", Utils.fixDatabaseQuery(url)), null);
    }
	
	public Cursor getCoverFile(String url) {
		SQLiteDatabase db = this.getReadableDatabase();
		return db.rawQuery(String.format("SELECT filename FROM album_cover_file WHERE url='%s'", Utils.fixDatabaseQuery(url)), null);
	}

	public void insertCoverFile(String coverUrl, String coverFile) {
		ContentValues file_values = new ContentValues();
		file_values.put("url", coverUrl);
		file_values.put("filename", coverFile);

		SQLiteDatabase db = this.getWritableDatabase();
		db.insert("album_cover_file", null, file_values);
	}

	public void deleteCoverFile(String url) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL(String.format("DELETE FROM album_cover_file WHERE url='%s'", Utils.fixDatabaseQuery(url)));
	}
}
