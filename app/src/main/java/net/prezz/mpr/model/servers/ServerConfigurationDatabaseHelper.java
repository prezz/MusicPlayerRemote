package net.prezz.mpr.model.servers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ServerConfigurationDatabaseHelper extends SQLiteOpenHelper {


    public ServerConfigurationDatabaseHelper(Context context) {
        super(context, "Servers.db", null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE server (id INTEGER PRIMARY KEY, name TEXT, host TEXT, port TEXT, password TEXT, streaming TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE server ADD COLUMN output TEXT DEFAULT \"\"");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE server ADD COLUMN streaming TEXT DEFAULT \"\"");
        }
        if (oldVersion < 4) {
            db.execSQL("CREATE TABLE server_migrate (id INTEGER PRIMARY KEY, name TEXT, host TEXT, port TEXT, password TEXT, streaming TEXT)");
            db.execSQL("INSERT INTO server_migrate (id, name, host, port, password, streaming) SELECT id, name, host, port, password, streaming FROM server");
            db.execSQL("DROP TABLE server");
            db.execSQL("ALTER TABLE server_migrate RENAME TO server");
        }
    }

    public Cursor getServerHost(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(String.format("SELECT host FROM server WHERE id=%s", id), null);
    }

    public Cursor getServers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM server ORDER BY name COLLATE NOCASE asc", null);
    }

    public void addServer(String name, String host, String port, String password, String streaming) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("host", host);
        values.put("port", port);
        values.put("password", password);
        values.put("streaming", streaming);

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert("server", null, values);
    }

    public void updateServer(Integer id, String name, String host, String port, String password, String streaming) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("host", host);
        values.put("port", port);
        values.put("password", password);
        values.put("streaming", streaming);

        SQLiteDatabase db = this.getWritableDatabase();
        db.update("server", values, String.format("id=%s", id), null);
    }

    public void deleteServer(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(String.format("DELETE FROM server WHERE id=%s", id));
    }
}