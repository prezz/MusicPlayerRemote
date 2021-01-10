package net.prezz.mpr.mpd.command;

import net.prezz.mpr.model.FutureTaskHandleImpl;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.mpd.MpdPartitionProvider;
import net.prezz.mpr.mpd.connection.MpdConnection;

import android.util.Log;

public abstract class MpdConnectionCommand<Param, Result> extends MpdCommand {

    public interface MpdConnectionCommandReceiver<Result> {
        void receive(Result result);
    }

    private Param param;
    private MpdPartitionProvider partitionProvider;

    public MpdConnectionCommand(Param param, MpdPartitionProvider partitionProvider) {
        this.param = param;
        this.partitionProvider = partitionProvider;
    }

    public final TaskHandle execute(final MpdConnection connection, final MpdConnectionCommandReceiver<Result> commandReceiver) {

        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (lock) {
                        try {
                            connection.connect();
                            if (!connection.setPartition(partitionProvider.getPartition())) {
                                partitionProvider.onInvalidPartition();
                            }

                            Result result = doExecute(connection, param);
                            postResult(result, commandReceiver);
                        } finally {
                            connection.disconnect();
                        }
                    }
                } catch (Exception ex) {
                    Log.e(MpdConnectionCommand.class.getName(), "error executing command", ex);
                    Result result = onError();
                    postResult(result, commandReceiver);
                }
            }
        };

        return new FutureTaskHandleImpl(executor.submit(task, null));
    }

    protected String getPartition() {
        return partitionProvider.getPartition();
    }

    protected abstract Result doExecute(MpdConnection connection, Param param) throws Exception;

    protected abstract Result onError();

    private void postResult(final Result result, final MpdConnectionCommandReceiver<Result> commandReceiver) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                commandReceiver.receive(result);
            }
        });
    }
}
