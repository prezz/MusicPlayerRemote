package net.prezz.mpr.mpd.command;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class MpdCommand {

    protected static final ExecutorService executor = Executors.newSingleThreadExecutor();
    protected static final Handler handler = new Handler(Looper.getMainLooper());
    protected static final Object lock = new Object();

}
