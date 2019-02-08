package net.prezz.mpr.mpd.command;

import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.model.TaskHandleImpl;
import net.prezz.mpr.mpd.connection.MpdConnection;
import net.prezz.mpr.mpd.database.MpdDatabaseBuilder;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;
import android.os.AsyncTask;
import android.util.Log;

public abstract class MpdDatabaseCommand<Param, Result> extends MpdCommand {

	public interface MpdDatabaseCommandReceiver<Result> {
		void build();
		void receive(Result result);
	}

	private Param param;
	
	public MpdDatabaseCommand(Param param) {
		this.param = param;
	}
	
	@SuppressWarnings("unchecked")
	public final TaskHandle execute(MpdLibraryDatabaseHelper databaseHelper, MpdConnection connection, final MpdDatabaseCommandReceiver<Result> commandReceiver) {
		
		AsyncTask<Object, Void, Result> task = new AsyncTask<Object, Void, Result>() {
			@Override
			protected Result doInBackground(Object... params) {
				MpdLibraryDatabaseHelper databaseHelperParam = (MpdLibraryDatabaseHelper) params[0];
				try {
					synchronized (lock) {
						if (databaseHelperParam.getRowCount() == 0) {
							publishProgress();
							MpdConnection connectionParam = (MpdConnection) params[1];
							try {
								connectionParam.connect();
								MpdDatabaseBuilder.buildDatabase(connectionParam, databaseHelperParam);
							} finally {
								connectionParam.disconnect();
							}
						}
					}
					
					return doExecute(databaseHelperParam, (Param) params[2]);
				} catch (Exception ex) {
					Log.e(MpdDatabaseCommand.class.getName(), "error executing command", ex);
					return onError();
				} finally {
					databaseHelperParam.close();
				}
			}
			
			@Override
		    protected void onProgressUpdate(Void... values) {
				commandReceiver.build();
		    }
			
			@Override
			protected void onPostExecute(Result result) {
				commandReceiver.receive(result);
			}
		};
		
		return new TaskHandleImpl<Object, Void, Result>(task.executeOnExecutor(executor, databaseHelper, connection, param));
	}
	
	protected abstract Result doExecute(MpdLibraryDatabaseHelper databaseHelper, Param param) throws Exception;

	protected abstract Result onError();
}
