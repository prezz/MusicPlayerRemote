package net.prezz.mpr.model;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

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

    public static TaskHandle getAllArtistsFromLibrary(Set<String> uriFilter, ResponseReceiver<LibraryEntity[]> responseReceiver) {
        return musicPlayer.getAllArtistsFromLibrary(uriFilter, responseReceiver);
    }

    public static TaskHandle getAllAlbumsFromLibrary(boolean sortByArtist, Set<String> uriFilter, ResponseReceiver<LibraryEntity[]> responseReceiver) {
        return musicPlayer.getAllAlbumsFromLibrary(sortByArtist, uriFilter, responseReceiver);
    }

    public static TaskHandle getAllGenresFromLibrary(Set<String> uriFilter, ResponseReceiver<LibraryEntity[]> responseReceiver) {
        return musicPlayer.getAllGenresFromLibrary(uriFilter, responseReceiver);
    }

    public static TaskHandle getAllUriPathsFromLibrary(Set<String> uriFilter, ResponseReceiver<LibraryEntity[]> responseReceiver) {
        return musicPlayer.getAllUriPathsFromLibrary(uriFilter, responseReceiver);
    }

    public static TaskHandle getFilteredAlbumsAndTitlesFromLibrary(LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver) {
        return musicPlayer.getFilteredAlbumsAndTitlesFromLibrary(entity, responseReceiver);
    }

    public static TaskHandle getFilteredArtistsFromLibrary(LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver) {
        return musicPlayer.getFilteredArtistsFromLibrary(entity, responseReceiver);
    }

    public static TaskHandle getFilteredAlbumsFromLibrary(boolean sortByArtist, LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver) {
        return musicPlayer.getFilteredAlbumsFromLibrary(sortByArtist, entity, responseReceiver);
    }

    public static TaskHandle getFilteredTracksAndTitlesFromLibrary(LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver) {
        return musicPlayer.getFilteredTracksAndTitlesFromLibrary(entity, responseReceiver);
    }

    public static TaskHandle getUriFromLibrary(UriEntity uriEntity, Set<String> uriFilter, ResponseReceiver<UriEntity[]> responseReceiver) {
        return musicPlayer.getUriFromLibrary(uriEntity, uriFilter, responseReceiver);
    }

    public static TaskHandle searchLibrary(String query, boolean searchUri, Set<String> uriFilter, ResponseReceiver<SearchResult> responseReceiver) {
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

    public static TaskHandle getOutputs(ResponseReceiver<AudioOutput[]> responseReceiver) {
        return musicPlayer.getOutputs(responseReceiver);
    }

    public static TaskHandle getStatistics(ResponseReceiver<Statistics> responseReceiver) {
        return musicPlayer.getStatistics(responseReceiver);
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
