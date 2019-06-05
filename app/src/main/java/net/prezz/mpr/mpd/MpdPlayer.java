package net.prezz.mpr.mpd;

import java.util.List;
import java.util.Set;

import net.prezz.mpr.model.AudioOutput;
import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.MusicPlayer;
import net.prezz.mpr.model.PlaylistEntity;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.ResponseResult;
import net.prezz.mpr.model.SearchResult;
import net.prezz.mpr.model.Statistics;
import net.prezz.mpr.model.StatusListener;
import net.prezz.mpr.model.StoredPlaylistEntity;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.model.command.Command;
import net.prezz.mpr.mpd.command.MpdConnectionCommand.MpdConnectionCommandReceiver;
import net.prezz.mpr.mpd.command.MpdDatabaseCommand.MpdDatabaseCommandReceiver;
import net.prezz.mpr.mpd.command.MpdDeleteLocalLibraryDatabaseCommand;
import net.prezz.mpr.mpd.command.MpdGetAllAlbumsCommand;
import net.prezz.mpr.mpd.command.MpdGetAllArtistsCommand;
import net.prezz.mpr.mpd.command.MpdGetAllGenresCommand;
import net.prezz.mpr.mpd.command.MpdGetHideableUriFolders;
import net.prezz.mpr.mpd.command.MpdGetFilteredAlbumsAndTitlesCommand;
import net.prezz.mpr.mpd.command.MpdGetFilteredArtistsCommand;
import net.prezz.mpr.mpd.command.MpdGetFilteredTracksAndTitlesCommand;
import net.prezz.mpr.mpd.command.MpdGetOutputsCommand;
import net.prezz.mpr.mpd.command.MpdGetPlaylistCommand;
import net.prezz.mpr.mpd.command.MpdGetPlaylistDetailsCommand;
import net.prezz.mpr.mpd.command.MpdGetPlaylistEntityCommand;
import net.prezz.mpr.mpd.command.MpdGetStatisticsCommand;
import net.prezz.mpr.mpd.command.MpdGetStoredPlaylistsCommand;
import net.prezz.mpr.mpd.command.MpdGetUriCommand;
import net.prezz.mpr.mpd.command.MpdSearchLibraryCommand;
import net.prezz.mpr.mpd.command.MpdSendControlCommands;
import net.prezz.mpr.mpd.connection.MpdConnection;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;
import net.prezz.mpr.ui.ApplicationActivator;

public class MpdPlayer implements MusicPlayer {

    private MpdConnection connection;
    private MpdStatusMonitor monitor;
    private MpdLibraryDatabaseHelper databaseHelper;


    public MpdPlayer(MpdSettings settings) {
        this.connection = new MpdConnection(settings);
        this.monitor = new MpdStatusMonitor(settings);
        this.databaseHelper = new MpdLibraryDatabaseHelper(ApplicationActivator.getContext(), settings.getMpdHost());
    }

    @Override
    public void dispose() {
        connection = null;

        if (monitor != null) {
            monitor.setStatusListener(null);
            monitor = null;
        }

        if (databaseHelper != null) {
            databaseHelper.close();
            databaseHelper = null;
        }
    }

    @Override
    public void setStatusListener(StatusListener listener) {
        monitor.setStatusListener(listener);
    }

    @Override
    public TaskHandle deleteLocalLibraryDatabase(final ResponseReceiver<Boolean> responseReceiver) {
        MpdDeleteLocalLibraryDatabaseCommand command = new MpdDeleteLocalLibraryDatabaseCommand(databaseHelper);
        return command.execute(connection, new MpdConnectionCommandReceiver<Boolean>() {
            @Override
            public void receive(Boolean result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle getHideableUriFolders(final ResponseReceiver<String[]> responseReceiver) {
        MpdGetHideableUriFolders command = new MpdGetHideableUriFolders();
        return command.execute(databaseHelper, connection, new MpdDatabaseCommandReceiver<String[]>() {
            @Override
            public void build() {
                responseReceiver.buildingDatabase();
            }

            @Override
            public void receive(String[] result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle getAllArtistsFromLibrary(Set<String> uriFilter, final ResponseReceiver<LibraryEntity[]> responseReceiver) {
        MpdGetAllArtistsCommand command = new MpdGetAllArtistsCommand(uriFilter);
        return command.execute(databaseHelper, connection, new MpdDatabaseCommandReceiver<LibraryEntity[]>() {
            @Override
            public void build() {
                responseReceiver.buildingDatabase();
            }

            @Override
            public void receive(LibraryEntity[] result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle getAllAlbumsFromLibrary(boolean sortByArtist, Set<String> uriFilter, final ResponseReceiver<LibraryEntity[]> responseReceiver) {
        MpdGetAllAlbumsCommand command = new MpdGetAllAlbumsCommand(sortByArtist, uriFilter);
        return command.execute(databaseHelper, connection, new MpdDatabaseCommandReceiver<LibraryEntity[]>() {
            @Override
            public void build() {
                responseReceiver.buildingDatabase();
            }

            @Override
            public void receive(LibraryEntity[] result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle getAllGenresFromLibrary(Set<String> uriFilter, final ResponseReceiver<LibraryEntity[]> responseReceiver) {
        MpdGetAllGenresCommand command = new MpdGetAllGenresCommand(uriFilter);
        return command.execute(databaseHelper, connection, new MpdDatabaseCommandReceiver<LibraryEntity[]>() {
            @Override
            public void build() {
                responseReceiver.buildingDatabase();
            }

            @Override
            public void receive(LibraryEntity[] result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle getFilteredAlbumsAndTitlesFromLibrary(final LibraryEntity entity, final ResponseReceiver<LibraryEntity[]> responseReceiver) {
        MpdGetFilteredAlbumsAndTitlesCommand command = new MpdGetFilteredAlbumsAndTitlesCommand(entity);
        return command.execute(databaseHelper, connection, new MpdDatabaseCommandReceiver<LibraryEntity[]>() {
            @Override
            public void build() {
                responseReceiver.buildingDatabase();
            }

            @Override
            public void receive(LibraryEntity[] result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle getFilteredArtistsFromLibrary(LibraryEntity entity, final ResponseReceiver<LibraryEntity[]> responseReceiver) {
        MpdGetFilteredArtistsCommand command = new MpdGetFilteredArtistsCommand(entity);
        return command.execute(databaseHelper, connection, new MpdDatabaseCommandReceiver<LibraryEntity[]>() {
            @Override
            public void build() {
                responseReceiver.buildingDatabase();
            }

            @Override
            public void receive(LibraryEntity[] result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle getFilteredTracksAndTitlesFromLibrary(LibraryEntity entity, final ResponseReceiver<LibraryEntity[]> responseReceiver) {
        MpdGetFilteredTracksAndTitlesCommand command = new MpdGetFilteredTracksAndTitlesCommand(entity);
        return command.execute(databaseHelper, connection, new MpdDatabaseCommandReceiver<LibraryEntity[]>() {
            @Override
            public void build() {
                responseReceiver.buildingDatabase();
            }

            @Override
            public void receive(LibraryEntity[] result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle getUriFromLibrary(UriEntity uriEntity, Set<String> uriFilter, final ResponseReceiver<UriEntity[]> responseReceiver) {
        MpdGetUriCommand command = new MpdGetUriCommand(uriEntity, uriFilter);
        return command.execute(databaseHelper, connection, new MpdDatabaseCommandReceiver<UriEntity[]>() {
            @Override
            public void build() {
                responseReceiver.buildingDatabase();
            }

            @Override
            public void receive(UriEntity[] result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle searchLibrary(String query, boolean searchUri, Set<String> uriFilter, final ResponseReceiver<SearchResult> responseReceiver) {
        MpdSearchLibraryCommand command = new MpdSearchLibraryCommand(query.trim(), searchUri, uriFilter);
        return command.execute(databaseHelper, connection, new MpdDatabaseCommandReceiver<SearchResult>() {
            @Override
            public void build() {
                responseReceiver.buildingDatabase();
            }

            @Override
            public void receive(SearchResult result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle getPlaylist(final ResponseReceiver<PlaylistEntity[]> responseReceiver) {
        MpdGetPlaylistCommand command = new MpdGetPlaylistCommand();
        return command.execute(connection, new MpdConnectionCommandReceiver<PlaylistEntity[]>() {
            @Override
            public void receive(PlaylistEntity[] result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle getPlaylistEntity(int position, final ResponseReceiver<PlaylistEntity> responseReceiver) {
        MpdGetPlaylistEntityCommand command = new MpdGetPlaylistEntityCommand(position);
        return command.execute(connection, new MpdConnectionCommandReceiver<PlaylistEntity>() {
            @Override
            public void receive(PlaylistEntity result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle getStoredPlaylists(final ResponseReceiver<StoredPlaylistEntity[]> responseReceiver) {
        MpdGetStoredPlaylistsCommand command = new MpdGetStoredPlaylistsCommand();
        return command.execute(connection, new MpdConnectionCommandReceiver<StoredPlaylistEntity[]>() {
            @Override
            public void receive(StoredPlaylistEntity[] result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle getPlaylistDetails(StoredPlaylistEntity storedPlaylist, final ResponseReceiver<PlaylistEntity[]> responseReceiver) {
        MpdGetPlaylistDetailsCommand command = new MpdGetPlaylistDetailsCommand(storedPlaylist);
        return command.execute(connection, new MpdConnectionCommandReceiver<PlaylistEntity[]>() {
            @Override
            public void receive(PlaylistEntity[] result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle getOutputs(final ResponseReceiver<AudioOutput[]> responseReceiver) {
        MpdGetOutputsCommand command = new MpdGetOutputsCommand();
        return command.execute(connection, new MpdConnectionCommandReceiver<AudioOutput[]>() {
            @Override
            public void receive(AudioOutput[] result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle getStatistics(final ResponseReceiver<Statistics> responseReceiver) {
        MpdGetStatisticsCommand command = new MpdGetStatisticsCommand();
        return command.execute(connection, new MpdConnectionCommandReceiver<Statistics>() {
            @Override
            public void receive(Statistics result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle sendControlCommands(List<Command> commands, final ResponseReceiver<ResponseResult> responseReceiver) {
        MpdSendControlCommands command = new MpdSendControlCommands(commands);
        return command.execute(connection, new MpdConnectionCommandReceiver<ResponseResult>() {
            @Override
            public void receive(ResponseResult result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }
}
