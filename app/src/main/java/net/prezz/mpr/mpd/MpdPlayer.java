package net.prezz.mpr.mpd;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import net.prezz.mpr.model.AudioOutput;
import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.MusicPlayer;
import net.prezz.mpr.model.PartitionEntity;
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
import net.prezz.mpr.mpd.command.MpdGetAlbumsCommand;
import net.prezz.mpr.mpd.command.MpdGetArtistsCommand;
import net.prezz.mpr.mpd.command.MpdGetGenresCommand;
import net.prezz.mpr.mpd.command.MpdGetHideableUriFolders;
import net.prezz.mpr.mpd.command.MpdGetFilteredAlbumsAndTitlesCommand;
import net.prezz.mpr.mpd.command.MpdGetFilteredTracksAndTitlesCommand;
import net.prezz.mpr.mpd.command.MpdGetOutputsCommand;
import net.prezz.mpr.mpd.command.MpdGetPartitionsCommand;
import net.prezz.mpr.mpd.command.MpdGetPlaylistCommand;
import net.prezz.mpr.mpd.command.MpdGetPlaylistDetailsCommand;
import net.prezz.mpr.mpd.command.MpdGetPlaylistEntityCommand;
import net.prezz.mpr.mpd.command.MpdGetStatisticsCommand;
import net.prezz.mpr.mpd.command.MpdGetStoredPlaylistsCommand;
import net.prezz.mpr.mpd.command.MpdGetUriCommand;
import net.prezz.mpr.mpd.command.MpdSearchLibraryCommand;
import net.prezz.mpr.mpd.command.MpdSendControlCommands;
import net.prezz.mpr.mpd.command.MpdUpdatePlayDataCommand;
import net.prezz.mpr.mpd.connection.MpdConnection;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;
import net.prezz.mpr.ui.ApplicationActivator;

public class MpdPlayer implements MusicPlayer {

    private MpdConnection connection;
    private MpdStatusMonitor monitor;
    private MpdPartitionStore partitionStore;
    private MpdLibraryDatabaseHelper databaseHelper;


    public MpdPlayer(MpdSettings settings) {
        this.connection = new MpdConnection(settings);
        this.monitor = new MpdStatusMonitor(settings);
        this.partitionStore = new MpdPartitionStore(ApplicationActivator.getContext(), settings);
        this.databaseHelper = new MpdLibraryDatabaseHelper(ApplicationActivator.getContext(), settings.getName());
    }

    @Override
    public void dispose() {
        connection = null;
        partitionStore = null;

        if (monitor != null) {
            monitor.setStatusListener(null, null);
            monitor = null;
        }

        if (databaseHelper != null) {
            databaseHelper.close();
            databaseHelper = null;
        }
    }

    @Override
    public void setStatusListener(StatusListener listener) {
        monitor.setStatusListener(listener, partitionStore);
    }

    @Override
    public TaskHandle deleteLocalLibraryDatabase(final ResponseReceiver<Boolean> responseReceiver) {
        MpdDeleteLocalLibraryDatabaseCommand command = new MpdDeleteLocalLibraryDatabaseCommand();
        return command.execute(databaseHelper, connection, new MpdDatabaseCommandReceiver<Boolean>() {
            @Override
            public void build() {
                // the delete local library command should not rebuild the database
            }

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
    public TaskHandle getArtistsFromLibrary(LibraryEntity entity, final ResponseReceiver<LibraryEntity[]> responseReceiver) {
        MpdGetArtistsCommand command = new MpdGetArtistsCommand(entity);
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
    public TaskHandle getAlbumsFromLibrary(boolean sortByArtist, LibraryEntity entity, final ResponseReceiver<LibraryEntity[]> responseReceiver) {
        MpdGetAlbumsCommand command = new MpdGetAlbumsCommand(sortByArtist, entity);
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
    public TaskHandle getGenresFromLibrary(LibraryEntity entity, final ResponseReceiver<LibraryEntity[]> responseReceiver) {
        MpdGetGenresCommand command = new MpdGetGenresCommand(entity);
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
    public TaskHandle getUriFromLibrary(UriEntity uriEntity, SortedSet<String> uriFilter, final ResponseReceiver<UriEntity[]> responseReceiver) {
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
    public TaskHandle searchLibrary(String query, boolean searchUri, SortedSet<String> uriFilter, final ResponseReceiver<SearchResult> responseReceiver) {
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
        MpdGetPlaylistCommand command = new MpdGetPlaylistCommand(partitionStore);
        return command.execute(connection, new MpdConnectionCommandReceiver<PlaylistEntity[]>() {
            @Override
            public void receive(PlaylistEntity[] result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle getPlaylistEntity(int position, final ResponseReceiver<PlaylistEntity> responseReceiver) {
        MpdGetPlaylistEntityCommand command = new MpdGetPlaylistEntityCommand(position, partitionStore);
        return command.execute(connection, new MpdConnectionCommandReceiver<PlaylistEntity>() {
            @Override
            public void receive(PlaylistEntity result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle getStoredPlaylists(final ResponseReceiver<StoredPlaylistEntity[]> responseReceiver) {
        MpdGetStoredPlaylistsCommand command = new MpdGetStoredPlaylistsCommand(partitionStore);
        return command.execute(connection, new MpdConnectionCommandReceiver<StoredPlaylistEntity[]>() {
            @Override
            public void receive(StoredPlaylistEntity[] result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle getPlaylistDetails(StoredPlaylistEntity storedPlaylist, final ResponseReceiver<PlaylistEntity[]> responseReceiver) {
        MpdGetPlaylistDetailsCommand command = new MpdGetPlaylistDetailsCommand(storedPlaylist, partitionStore);
        return command.execute(connection, new MpdConnectionCommandReceiver<PlaylistEntity[]>() {
            @Override
            public void receive(PlaylistEntity[] result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle getOutputs(boolean defaultPartition, final ResponseReceiver<AudioOutput[]> responseReceiver) {
        MpdGetOutputsCommand command = new MpdGetOutputsCommand(defaultPartition ? new SpecificPartitionProvider(MpdPartitionProvider.DEFAULT_PARTITION) : partitionStore);
        return command.execute(connection, new MpdConnectionCommandReceiver<AudioOutput[]>() {
            @Override
            public void receive(AudioOutput[] result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle getStatistics(final ResponseReceiver<Statistics> responseReceiver) {
        MpdGetStatisticsCommand command = new MpdGetStatisticsCommand(partitionStore);
        return command.execute(connection, new MpdConnectionCommandReceiver<Statistics>() {
            @Override
            public void receive(Statistics result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle getPartitions(final ResponseReceiver<PartitionEntity[]> responseReceiver) {
        MpdGetPartitionsCommand command = new MpdGetPartitionsCommand(partitionStore);
        return command.execute(connection, new MpdConnectionCommandReceiver<PartitionEntity[]>() {
            @Override
            public void receive(PartitionEntity[] result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle switchPartition(final String partition, final ResponseReceiver<PartitionEntity[]> responseReceiver) {
        MpdGetPartitionsCommand command = new MpdGetPartitionsCommand(new SpecificPartitionProvider(partition));
        return command.execute(connection, new MpdConnectionCommandReceiver<PartitionEntity[]>() {
            @Override
            public void receive(PartitionEntity[] result) {
                Set<String> partitions = new HashSet<String>(result.length);
                for (PartitionEntity entity : result) {
                    partitions.add(entity.getPartitionName());
                }

                if (partitions.contains(partition)) {
                    partitionStore.putPartition(partition);
                    monitor.switchPartition(partitionStore);
                }

                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle updatePlayData(List<PlaylistEntity> entities, ResponseReceiver<Boolean> responseReceiver) {
        MpdUpdatePlayDataCommand command = new MpdUpdatePlayDataCommand(entities);
        return command.execute(databaseHelper, connection, new MpdDatabaseCommandReceiver<Boolean>() {
            @Override
            public void build() {
                responseReceiver.buildingDatabase();
            }

            @Override
            public void receive(Boolean result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    @Override
    public TaskHandle sendControlCommands(List<Command> commands, final ResponseReceiver<ResponseResult> responseReceiver) {
        MpdSendControlCommands command = new MpdSendControlCommands(commands, partitionStore);
        return command.execute(connection, new MpdConnectionCommandReceiver<ResponseResult>() {
            @Override
            public void receive(ResponseResult result) {
                responseReceiver.receiveResponse(result);
            }
        });
    }

    private static class SpecificPartitionProvider implements MpdPartitionProvider {

        private String partition;

        public SpecificPartitionProvider(String partition) {
            this.partition = partition;
        }

        @Override
        public String getPartition() {
            return partition;
        }

        @Override
        public void onInvalidPartition() {
            this.partition = DEFAULT_PARTITION;
        }
    }
}
