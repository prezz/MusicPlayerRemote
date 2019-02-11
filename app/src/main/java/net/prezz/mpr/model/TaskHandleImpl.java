package net.prezz.mpr.model;

import android.os.AsyncTask;
import net.prezz.mpr.model.TaskHandle;

public class TaskHandleImpl<Params, Progress, Result> implements TaskHandle {

    private AsyncTask<Params, Progress, Result> asyncTask;

    public TaskHandleImpl(AsyncTask<Params, Progress, Result> asyncTask) {
        this.asyncTask = asyncTask;
    }

    @Override
    public void cancelTask() {
        asyncTask.cancel(false);
    }
}
