package net.prezz.mpr.mpd.command;

import net.prezz.mpr.Utils;

import java.util.concurrent.Executor;

public abstract class MpdCommand {

    protected static final Executor executor = Utils.createExecutor();
    protected static final Object lock = new Object();

}
