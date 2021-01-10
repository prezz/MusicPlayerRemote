package net.prezz.mpr.model;

import java.util.concurrent.Future;

public class FutureTaskHandleImpl implements TaskHandle {

    private Future<?> future;

    public FutureTaskHandleImpl(Future<?> future) {
        this.future = future;
    }

    @Override
    public void cancelTask() {
        future.cancel(false);
    }
}
