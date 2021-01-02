package net.prezz.mpr.model;

import java.util.List;
import java.util.SortedSet;

import net.prezz.mpr.model.command.Command;

public interface MusicPlayer {

    void dispose();

    void setStatusListener(StatusListener listener);

    TaskHandle deleteLocalLibraryDatabase(ResponseReceiver<Boolean> responseReceiver);

    TaskHandle getHideableUriFolders(ResponseReceiver<String[]> responseReceiver);

    TaskHandle getAllArtistsFromLibrary(LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver);

    TaskHandle getAllAlbumsFromLibrary(boolean sortByArtist, LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver);

    TaskHandle getAllGenresFromLibrary(LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver);

    TaskHandle getFilteredAlbumsAndTitlesFromLibrary(LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver);

    TaskHandle getFilteredArtistsFromLibrary(LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver);

    TaskHandle getFilteredTracksAndTitlesFromLibrary(LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver);

    TaskHandle getUriFromLibrary(UriEntity uriEntity, SortedSet<String> uriFilter, ResponseReceiver<UriEntity[]> responseReceiver);

    TaskHandle searchLibrary(String query, boolean searchUri, SortedSet<String> uriFilter, ResponseReceiver<SearchResult> responseReceiver);

    TaskHandle getPlaylist(ResponseReceiver<PlaylistEntity[]> responseReceiver);

    TaskHandle getPlaylistEntity(int position, ResponseReceiver<PlaylistEntity> responseReceiver);

    TaskHandle getStoredPlaylists(ResponseReceiver<StoredPlaylistEntity[]> responseReceiver);

    TaskHandle getPlaylistDetails(StoredPlaylistEntity storedPlaylist, ResponseReceiver<PlaylistEntity[]> responseReceiver);

    TaskHandle getOutputs(ResponseReceiver<AudioOutput[]> responseReceiver);

    TaskHandle getStatistics(ResponseReceiver<Statistics> responseReceiver);

    TaskHandle getPartitions(ResponseReceiver<PartitionEntity[]> responseReceiver);

    TaskHandle switchPartition(String partition, ResponseReceiver<PartitionEntity[]> responseReceiver);

    TaskHandle sendControlCommands(List<Command> commands, ResponseReceiver<ResponseResult> responseReceiver);
}
