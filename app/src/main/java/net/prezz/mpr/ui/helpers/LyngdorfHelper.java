package net.prezz.mpr.ui.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LyngdorfHelper {

    private static final String LYNGDORF_IP_KEY = "lyngdorfIpKey";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    private static String lyngdorfIp = null;

    public static void setLyngdorfIp(Context context, String ip) {
        lyngdorfIp = null;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (ip == null || ip.isEmpty()) {
            editor.remove(LYNGDORF_IP_KEY);
        } else {
            editor.putString(LYNGDORF_IP_KEY, ip);
        }
        editor.commit();
    }

    public static String getLyngdorfIp(Context context) {
        if (lyngdorfIp != null) {
            return lyngdorfIp;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        lyngdorfIp = sharedPreferences.getString(LYNGDORF_IP_KEY, "");
        return lyngdorfIp;
    }

    public static boolean volumeDown(Context context) {
        if (getLyngdorfIp(context).isEmpty()) {
            return false;
        }

        sendLyngdorfCommand(context, "!VOLDN\n", new Callback(context));
        return true;
    }

    public static boolean volumeUp(Context context) {
        if (getLyngdorfIp(context).isEmpty()) {
            return false;
        }

        sendLyngdorfCommand(context, "!VOLUP\n", new Callback(context));
        return true;
    }

    private static void sendLyngdorfCommand(Context context, String command, Callback callback) {

        Runnable task = new Runnable() {
            @Override
            public void run() {

                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(lyngdorfIp, 84), 1000);
                    socket.setSoTimeout(1000);

                    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"))) {
                        writer.write(command);
                        writer.flush();
                    }
                    doCallback(true, callback);
                } catch (IOException ex) {
                    Log.e(LyngdorfHelper.class.getName(), "error writing to Lyngdorf", ex);
                    doCallback(false, callback);
                    //Boast.makeText(context, "Error writing to Lyngdorf").show();
                }
            }
        };

        executor.submit(task);
    }

    private static void doCallback(final boolean result, final Callback callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.call(result);
            }
        });
    }

    private static class Callback {

        private Context context;

        Callback(Context context) {
            this.context = context;
        }

        void call(boolean success) {
            if (!success) {
                Boast.makeText(context, "Error writing to Lyngdorf").show();
            }
        }
    }
}
