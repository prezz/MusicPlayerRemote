package net.prezz.mpr.mpd.command;

import net.prezz.mpr.model.FutureTaskHandleImpl;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.mpd.connection.MpdConnection;
import net.prezz.mpr.mpd.database.MpdDatabaseBuilder;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;
import android.util.Log;

public abstract class MpdDatabaseCommand<Param, Result> extends MpdCommand {

    public interface MpdDatabaseCommandReceiver<Result> {
        void build();
        void receive(Result result);
    }

    private Param param;
    private boolean rebuild;

    public MpdDatabaseCommand(Param param) {
        this.param = param;
        this.rebuild = true;
    }

    public MpdDatabaseCommand(Param param, boolean rebuild) {
        this.param = param;
        this.rebuild = rebuild;
    }

    public final TaskHandle execute(final MpdLibraryDatabaseHelper databaseHelper, final MpdConnection connection, final MpdDatabaseCommandReceiver<Result> commandReceiver) {

        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (lock) {
                        if (rebuild && databaseHelper.getRowCount() == 0) {
                            postBuild(commandReceiver);
                            try {
                                connection.connect();
                                MpdDatabaseBuilder.buildDatabase(connection, databaseHelper);
                            } finally {
                                connection.disconnect();
                            }
                        }
                    }

                    Result result = doExecute(databaseHelper, param);
                    postResult(result, commandReceiver);
                } catch (Exception ex) {
                    Log.e(MpdDatabaseCommand.class.getName(), "error executing command", ex);
                    Result result = onError();
                    postResult(result, commandReceiver);
                } finally {
                    databaseHelper.close();
                }
            }
        };

        return new FutureTaskHandleImpl(executor.submit(task, null));
    }

    protected abstract Result doExecute(MpdLibraryDatabaseHelper databaseHelper, Param param) throws Exception;

    protected abstract Result onError();

    private void postBuild(final MpdDatabaseCommandReceiver<Result> commandReceiver) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                commandReceiver.build();
            }
        });
    }

    private void postResult(final Result result, final MpdDatabaseCommandReceiver<Result> commandReceiver) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                commandReceiver.receive(result);
            }
        });
    }
}
