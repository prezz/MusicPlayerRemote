package net.prezz.mpr.model.servers;

import net.prezz.mpr.Utils;
import net.prezz.mpr.R;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

public class ServerConfigurationDatabaseHelper extends SQLiteOpenHelper {

    private Context context;


    public ServerConfigurationDatabaseHelper(Context context) {
        super(context, "Servers.db", null, 3);

        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE server (id INTEGER PRIMARY KEY, name TEXT, host TEXT, port TEXT, password TEXT, output TEXT, streaming TEXT)");

        //get the existing configuration for versions prior to 15 (1.1.5) an add to database
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String host = sharedPreferences.getString(context.getResources().getString(R.string.settings_connection_mpd_host_key), "");
        String port = sharedPreferences.getString(context.getResources().getString(R.string.settings_connection_mpd_port_key), "6600");
        String password = sharedPreferences.getString(context.getResources().getString(R.string.settings_connection_mpd_password_key), "");
        if (!Utils.nullOrEmpty(host) && !Utils.nullOrEmpty(port)) {
            //Don't call addServer method as getting DB recursively will throw an exception
            ContentValues values = new ContentValues();
            values.put("id", 1);
            values.put("name", host);
            values.put("host", host);
            values.put("port", port);
            values.put("password", password);
            values.put("output", "");
            values.put("streaming", "");
            db.insert("server", null, values);

            Editor editor = sharedPreferences.edit();
            editor.putInt(ServerConfigurationService.SELECTED_SERVER_CONFIGURATION_KEY, 1);
            editor.commit();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE server ADD COLUMN output TEXT DEFAULT \"\"");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE server ADD COLUMN streaming TEXT DEFAULT \"\"");
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

    public void addServer(String name, String host, String port, String password, String output, String streaming) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("host", host);
        values.put("port", port);
        values.put("password", password);
        values.put("output", output);
        values.put("streaming", streaming);

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert("server", null, values);
    }

    public void updateServer(Integer id, String name, String host, String port,    String password, String output, String streaming) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("host", host);
        values.put("port", port);
        values.put("password", password);
        values.put("output", output);
        values.put("streaming", streaming);

        SQLiteDatabase db = this.getWritableDatabase();
        db.update("server", values, String.format("id=%s", id), null);
    }

    public void deleteServer(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(String.format("DELETE FROM server WHERE id=%s", id));
    }
}