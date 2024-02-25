package net.prezz.mpr.mpd;

import net.prezz.mpr.Utils;
import net.prezz.mpr.model.AudioOutput;
import net.prezz.mpr.model.PlayerState;
import net.prezz.mpr.model.PlayerStatus;
import net.prezz.mpr.model.StatusListener;
import net.prezz.mpr.mpd.connection.MpdConnection;
import net.prezz.mpr.mpd.connection.RejectAllFilter;
import net.prezz.mpr.service.PlaybackService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class MpdStatusMonitor extends Handler {

    private static final int STATUS_EVENT = 10001;
    private static final int INVALID_PARTITION_EVENT = 10002;
    private static final String STATUS_BUNDLE_KEY = "status";

    private MpdSettings settings;
    private ExecutorService executor;
    private Monitor monitor;
    private StatusListener statusListener;
    private MpdPartitionProvider partitionProvider;

    public MpdStatusMonitor(MpdSettings settings) {
        super(Looper.getMainLooper());

        this.settings = settings;
        this.executor = Executors.newCachedThreadPool(new DaemonThreadFactory());
        this.monitor = null;
        this.statusListener = null;
        this.partitionProvider = null;
    }

    public void switchPartition(MpdPartitionProvider partitionProvider) {
        setStatusListener(statusListener, partitionProvider);
    }

    public void setStatusListener(StatusListener listener, MpdPartitionProvider partitionProvider) {
        if (monitor != null) {
            monitor.abort();
            monitor = null;
        }

        this.statusListener = listener;
        this.partitionProvider = partitionProvider;

        if (statusListener != null && partitionProvider != null) {
            monitor = new Monitor(settings, partitionProvider.getPartition());
            executor.execute(monitor);
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case STATUS_EVENT:
                Bundle bundle = msg.getData();
                PlayerStatus status = bundle.getSerializable(STATUS_BUNDLE_KEY, PlayerStatus.class);
                if (status != null && statusListener != null) {
                    statusListener.statusUpdated(status);
                    if (status.isConnected() && status.getState() != PlayerState.STOP) {
                        PlaybackService.start();
                    }
                }
                break;
            case INVALID_PARTITION_EVENT:
                partitionProvider.onInvalidPartition();

                if (statusListener != null && partitionProvider != null) {
                    monitor = new Monitor(settings, partitionProvider.getPartition());
                    executor.execute(monitor);
                }
                break;
        }
    }

    private final class Monitor implements Runnable {

        private static final int MAX_ERROR_COUNT = 10;

        private final Object lock = new Object();

        private MpdConnection connection;
        private String partition;
        private boolean running;
        private boolean connected;
        private int errorCount;
        private int connectionHash;

        public Monitor(MpdSettings settings, String partition) {
            this.connection = new MpdConnection(settings, 60000 * 5);
            this.partition = partition;
            this.running = true;
            this.connected = false;
            this.errorCount = 0;
            this.connectionHash = Utils.shortHashCode(settings.getMpdHost(), settings.getMpdPort(), partition);
        }

        public void abort() {
            synchronized (lock) {
                running = false;
                try {
                    executor.execute(new Abort(connection, partition));
                } catch (Exception ex) {
                    Log.e(MpdStatusMonitor.class.getName(), "error sending noidle command", ex);
                }

                removeMessages(STATUS_EVENT);
            }
        }

        @Override
        public void run() {
            Log.d(Monitor.class.getName(), "starting monitor thread");

            PlayerStatus status = getPlayerStatus(true);
            if (status == null && running) {
                status = new PlayerStatus(false);
            }
            dispatchStatus(status);

            while (running) {
                try {
                    if (!connected) {
                        status = getPlayerStatus(false);
                        dispatchStatus(status);
                    }

                    connection.connect();
                    if (!connection.setPartition(partition)) {
                        dispatchInvalidPartition();
                        running = false;
                    }
                    synchronized (lock) {
                        if (!running) {
                            break;
                        }
                        connection.writeCommand("idle playlist options player mixer output\n");
                    }

                    connection.readResponse(RejectAllFilter.INSTANCE);

                    if (running) {
                        status = getPlayerStatus(false);
                        dispatchStatus(status);
                    }
                } catch (Exception ex) {
                    Log.e(MpdStatusMonitor.class.getName(), "error in monitor thread", ex);

                    if (running) {
                        connection.disconnect();
                        if (connected && ++errorCount > MAX_ERROR_COUNT) {
                            connected = false;
                            dispatchStatus(new PlayerStatus(false));
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex1) {
                            throw new RuntimeException(ex1);
                        }
                    }
                }
            }

            connection.disconnect();
            Log.d(Monitor.class.getName(), "exiting monitor thread");
        }

        private PlayerStatus getPlayerStatus(boolean returnOnError) {
            while (running) {
                try {
                    connection.connect();
                    if (!connection.setPartition(partition)) {
                        return new PlayerStatus(false);
                    }
                    PlayerStatus status = new PlayerStatus(true);

                    String[] statusLines = connection.writeResponseCommand("status\n");
                    for (String line : statusLines) {
                        if (line.startsWith("consume: ")) {
                            String s = line.substring(9);
                            status.setConsume("1".equals(s));
                        }
                        if (line.startsWith("playlist: ")) {
                            String s = line.substring(10);
                            status.setPlaylistVersion(connectionHash + Integer.parseInt(s));
                        }
                        if (line.startsWith("random: ")) {
                            String s = line.substring(8);
                            status.setRandom("1".equals(s));
                        }
                        if (line.startsWith("repeat: ")) {
                            String s = line.substring(8);
                            status.setRepeat("1".equals(s));
                        }
                        if (line.startsWith("song: ")) {
                            String s = line.substring(6);
                            status.setCurrentSong(Integer.parseInt(s));
                        }
                        if (line.startsWith("nextsong: ")) {
                            String s = line.substring(10);
                            status.setNextSong(Integer.parseInt(s));
                        }
                        if (line.startsWith("state: ")) {
                            String s = line.substring(7);
                            if ("play".equals(s)) {
                                status.setState(PlayerState.PLAY);
                            }
                            if ("stop".equals(s)) {
                                status.setState(PlayerState.STOP);
                            }
                            if ("pause".equals(s)) {
                                status.setState(PlayerState.PAUSE);
                            }
                        }
                        if (line.startsWith("elapsed: ") && connection.isMinimumVersion(0, 22, 0)) {
                            String s = line.substring(9);
                            status.setElapsedTime(Math.round(Float.parseFloat(s)));
                        }
                        if (line.startsWith("duration: ") && connection.isMinimumVersion(0, 22, 0)) {
                            String s = line.substring(10);
                            status.setTotalTime(Math.round(Float.parseFloat(s)));
                        }
                        if (line.startsWith("time: ") && !connection.isMinimumVersion(0, 22, 0)) { // deprecated
                            String s = line.substring(6);
                            String[] split = s.split(":");
                            status.setElapsedTime(Integer.parseInt(split[0]));
                            status.setTotalTime(Integer.parseInt(split[1]));
                        }
                        if (line.startsWith("volume: ")) {
                            String s = line.substring(8);
                            status.setVolume(Integer.parseInt(s));
                        }
                        if (line.startsWith("partition: ")) {
                            String s = line.substring(11);
                            status.setPartition(s);
                        }
                    }

                    String[] outputLines = connection.writeResponseCommand("outputs\n");
                    List<AudioOutput> audioOutputs = new ArrayList<AudioOutput>();
                    String outputId = null;
                    String outputName = null;
                    String plugin = null;
                    Boolean outputEnabled = null;
                    for (String line : outputLines) {
                        if (line.startsWith("outputid: ")) {
                            outputId = line.substring(10);
                        }

                        if (line.startsWith("outputname: ")) {
                            outputName = line.substring(12);
                        }

                        if (line.startsWith("plugin: ")) {
                            plugin = line.substring(8);
                        }

                        if (line.startsWith("outputenabled: ")) {
                            String s = line.substring(15);
                            outputEnabled = Boolean.valueOf("1".equals(s));
                        }

                        if (outputId != null && outputName != null && plugin != null && outputEnabled != null) {
                            if (outputEnabled) {
                                audioOutputs.add(new AudioOutput(outputId, outputName, plugin, outputEnabled));
                            }
                            outputId = null;
                            outputName = null;
                            plugin = null;
                            outputEnabled = null;
                        }
                    }
                    status.setAudioOutputs(audioOutputs.toArray(new AudioOutput[audioOutputs.size()]));

                    connected = true;
                    errorCount = 0;
                    return status;
                } catch (Exception ex) {
                    Log.e(MpdStatusMonitor.class.getName(), "Error in monitor thread. Failed getting initial player status", ex);

                    if (returnOnError) {
                        connection.disconnect();
                        break;
                    }
                    if (running) {
                        connection.disconnect();
                        if (connected && ++errorCount > MAX_ERROR_COUNT) {
                            connected = false;
                            return new PlayerStatus(false);
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex1) {
                            throw new RuntimeException(ex1);
                        }
                    }
                }
            }

            return null;
        }

        private void dispatchStatus(PlayerStatus status) {
            synchronized (lock) {
                if (status != null && running) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(STATUS_BUNDLE_KEY, status);

                    Message message = obtainMessage(STATUS_EVENT);
                    message.setData(bundle);
                    sendMessage(message);
                }
            }
        }

        private void dispatchInvalidPartition() {
            synchronized (lock) {
                if (running) {
                    Message message = obtainMessage(INVALID_PARTITION_EVENT);
                    sendMessage(message);
                }
            }
        }
    }

    private static final class Abort implements Runnable {

        private MpdConnection connection;
        private String partition;

        public Abort(MpdConnection connection, String partition) {
            this.connection = connection;
            this.partition = partition;
        }

        @Override
        public void run() {
            try {
                connection.connect();
                //connection.setPartition(partition);
                connection.writeCommand("noidle\n");
            } catch (Exception ex) {
                Log.e(MpdStatusMonitor.class.getName(), "error sending noidle command", ex);
            }
        }
    }

    private static final class DaemonThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setDaemon(true);
            return thread;
        }
    }
}
