package net.prezz.mpr.model;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

import net.prezz.mpr.model.command.Command;

public class MusicPlayerControl {

    private static MusicPlayer musicPlayer = new NullPlayer();


    private MusicPlayerControl() {
        //prevent instantiation
    }

    public static void setMusicPlayer(MusicPlayer musicPlayer) {
        MusicPlayerControl.musicPlayer.dispose();
        MusicPlayerControl.musicPlayer = (musicPlayer == null) ? new NullPlayer() : musicPlayer;
    }

    public static void setStatusListener(StatusListener listener) {
        musicPlayer.setStatusListener(listener);
    }

    public static TaskHandle deleteLocalLibraryDatabase(ResponseReceiver<Boolean> responseReceiver) {
        return musicPlayer.deleteLocalLibraryDatabase(responseReceiver);
    }

    public static TaskHandle getHideableUriFolders(ResponseReceiver<String[]> responseReceiver) {
        return musicPlayer.getHideableUriFolders(responseReceiver);
    }

    public static TaskHandle getArtistsFromLibrary(LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver) {
        return musicPlayer.getArtistsFromLibrary(entity, responseReceiver);
    }

    public static TaskHandle getAlbumsFromLibrary(boolean sortByArtist, LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver) {
        return musicPlayer.getAlbumsFromLibrary(sortByArtist, entity, responseReceiver);
    }

    public static TaskHandle getGenresFromLibrary(LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver) {
        return musicPlayer.getGenresFromLibrary(entity, responseReceiver);
    }

    public static TaskHandle getFilteredAlbumsAndTitlesFromLibrary(LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver) {
        return musicPlayer.getFilteredAlbumsAndTitlesFromLibrary(entity, responseReceiver);
    }

    public static TaskHandle getFilteredTracksAndTitlesFromLibrary(LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver) {
        return musicPlayer.getFilteredTracksAndTitlesFromLibrary(entity, responseReceiver);
    }

    public static TaskHandle getUriFromLibrary(UriEntity uriEntity, SortedSet<String> uriFilter, ResponseReceiver<UriEntity[]> responseReceiver) {
        return musicPlayer.getUriFromLibrary(uriEntity, uriFilter, responseReceiver);
    }

    public static TaskHandle searchLibrary(String query, boolean searchUri, SortedSet<String> uriFilter, ResponseReceiver<SearchResult> responseReceiver) {
        return musicPlayer.searchLibrary(query, searchUri, uriFilter, responseReceiver);
    }

    public static TaskHandle getPlaylist(ResponseReceiver<PlaylistEntity[]> responseReceiver) {
        return musicPlayer.getPlaylist(responseReceiver);
    }

    public static TaskHandle getPlaylistEntity(int position, ResponseReceiver<PlaylistEntity> responseReceiver) {
        return musicPlayer.getPlaylistEntity(position, responseReceiver);
    }

    public static TaskHandle getStoredPlaylists(ResponseReceiver<StoredPlaylistEntity[]> responseReceiver) {
        return musicPlayer.getStoredPlaylists(responseReceiver);
    }

    public static TaskHandle getPlaylistDetails(StoredPlaylistEntity storedPlaylist, ResponseReceiver<PlaylistEntity[]> responseReceiver) {
        return musicPlayer.getPlaylistDetails(storedPlaylist, responseReceiver);
    }

    public static TaskHandle getOutputs(boolean defaultPartition, ResponseReceiver<AudioOutput[]> responseReceiver) {
        return musicPlayer.getOutputs(defaultPartition, responseReceiver);
    }

    public static TaskHandle getStatistics(ResponseReceiver<Statistics> responseReceiver) {
        return musicPlayer.getStatistics(responseReceiver);
    }

    public static TaskHandle getPartitions(ResponseReceiver<PartitionEntity[]> responseReceiver) {
        return musicPlayer.getPartitions(responseReceiver);
    }

    public static TaskHandle switchPartition(String partition, ResponseReceiver<PartitionEntity[]> responseReceiver) {
        return musicPlayer.switchPartition(partition, responseReceiver);
    }

    public static TaskHandle updatePlayData(List<String> uris, ResponseReceiver<Boolean> responseReceiver) {
        return musicPlayer.updatePlayData(uris, responseReceiver);
    }

    public static void sendControlCommand(Command command) {
        sendControlCommands(Arrays.asList(command), new ResponseReceiver<ResponseResult>() {
            @Override
            public void receiveResponse(ResponseResult response) {
            }
        });
    }

    public static TaskHandle sendControlCommand(Command command, ResponseReceiver<ResponseResult> responseReceiver) {
        return sendControlCommands(Arrays.asList(command), responseReceiver);
    }

    public static void sendControlCommands(List<Command> commands) {
        sendControlCommands(commands, new ResponseReceiver<ResponseResult>() {
            @Override
            public void receiveResponse(ResponseResult response) {
            }
        });
    }

    public static TaskHandle sendControlCommands(List<Command> commands, ResponseReceiver<ResponseResult> responseReceiver) {
        return musicPlayer.sendControlCommands(commands, responseReceiver);
    }
}
