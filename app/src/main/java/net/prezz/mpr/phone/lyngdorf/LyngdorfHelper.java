package net.prezz.mpr.phone.lyngdorf;

import android.util.Log;

import net.prezz.mpr.ui.player.PlayerControlFragment;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LyngdorfHelper {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void volumeDown() {

        sendLyngdorfCommand("!VOLDN\n");
    }

    public static void volumeUp() {

        sendLyngdorfCommand("!VOLUP\n");
    }

    private static void sendLyngdorfCommand(String command) {

        Runnable task = new Runnable() {
            @Override
            public void run() {

                try (Socket socket = new Socket();) {
                    socket.connect(new InetSocketAddress("192.168.1.20", 84), 10000);
                    socket.setSoTimeout(10000);

                    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));) {
                        writer.write(command);
                        writer.flush();
                    }
                } catch (IOException ex) {
                    Log.e(LyngdorfHelper.class.getName(), "error writing to Lyngdorf", ex);
                }
            }
        };

        executor.submit(task);
    }
}
