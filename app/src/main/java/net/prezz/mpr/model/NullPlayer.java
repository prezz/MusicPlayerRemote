package net.prezz.mpr.model;

import java.util.List;
import java.util.SortedSet;

import net.prezz.mpr.model.command.Command;


/**
 * Music player with no functionality to be used in {@link MusicPlayerControl}
 * to avoid null checks in every call if a music player isn't set.
 *
 */
class NullPlayer implements MusicPlayer {

    @Override
    public void dispose() {
    }

    @Override
    public void setStatusListener(StatusListener listener) {
    }

    @Override
    public TaskHandle deleteLocalLibraryDatabase(ResponseReceiver<Boolean> responseReceiver) {
        return createNullHandle();
    }

    @Override
    public TaskHandle getHideableUriFolders(ResponseReceiver<String[]> responseReceiver) {
        return createNullHandle();
    }

    @Override
    public TaskHandle getAllArtistsFromLibrary(LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver) {
        return createNullHandle();
    }

    @Override
    public TaskHandle getAllAlbumsFromLibrary(boolean sortByArtist, LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver) {
        return createNullHandle();
    }

    @Override
    public TaskHandle getAllGenresFromLibrary(LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver) {
        return createNullHandle();
    }

    @Override
    public TaskHandle getFilteredAlbumsAndTitlesFromLibrary(LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver) {
        return createNullHandle();
    }

    @Override
    public TaskHandle getFilteredArtistsFromLibrary(LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver) {
        return createNullHandle();
    }

    @Override
    public TaskHandle getFilteredTracksAndTitlesFromLibrary(LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver) {
        return createNullHandle();
    }

    @Override
    public TaskHandle getUriFromLibrary(UriEntity uriEntity, SortedSet<String> uriFilter, ResponseReceiver<UriEntity[]> responseReceiver) {
        return createNullHandle();
    }

    @Override
    public TaskHandle searchLibrary(String query, boolean searchUri, SortedSet<String> uriFilter, ResponseReceiver<SearchResult> responseReceiver) {
        return createNullHandle();
    }

    @Override
    public TaskHandle getPlaylist(ResponseReceiver<PlaylistEntity[]> responseReceiver) {
        return createNullHandle();
    }

    @Override
    public TaskHandle getPlaylistEntity(int position, ResponseReceiver<PlaylistEntity> responseReceiver) {
        return createNullHandle();
    }

    @Override
    public TaskHandle getStoredPlaylists(ResponseReceiver<StoredPlaylistEntity[]> responseReceiver) {
        return createNullHandle();
    }

    @Override
    public TaskHandle getPlaylistDetails(StoredPlaylistEntity storedPlaylist, ResponseReceiver<PlaylistEntity[]> responseReceiver) {
        return createNullHandle();
    }

    @Override
    public TaskHandle getOutputs(ResponseReceiver<AudioOutput[]> responseReceiver) {
        return createNullHandle();
    }

    @Override
    public TaskHandle getStatistics(ResponseReceiver<Statistics> responseReceiver) {
        return createNullHandle();
    }

    @Override
    public TaskHandle getPartitions(final ResponseReceiver<String[]> responseReceiver) {
        return createNullHandle();
    }

    @Override
    public TaskHandle sendControlCommands(List<Command> commands, ResponseReceiver<ResponseResult> responseReceiver) {
        return createNullHandle();
    }

    private TaskHandle createNullHandle() {
        return TaskHandle.NULL_HANDLE;
    }
}
