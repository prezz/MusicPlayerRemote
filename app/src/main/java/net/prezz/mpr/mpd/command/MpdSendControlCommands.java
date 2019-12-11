package net.prezz.mpr.mpd.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.PlayerState;
import net.prezz.mpr.model.ResponseResult;
import net.prezz.mpr.model.StoredPlaylistEntity;
import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.model.UriEntity.UriType;
import net.prezz.mpr.model.command.AddToNewStoredPlaylistCommand;
import net.prezz.mpr.model.command.AddToPlaylistCommand;
import net.prezz.mpr.model.command.AddToStoredPlaylistCommand;
import net.prezz.mpr.model.command.AddUriToNewStoredPlaylistCommand;
import net.prezz.mpr.model.command.AddUriToPlaylistCommand;
import net.prezz.mpr.model.command.AddUriToStoredPlaylistCommand;
import net.prezz.mpr.model.command.ClearPlaylistCommand;
import net.prezz.mpr.model.command.Command;
import net.prezz.mpr.model.command.ConsumeCommand;
import net.prezz.mpr.model.command.DeleteFromPlaylistCommand;
import net.prezz.mpr.model.command.DeleteFromStoredPlaylistCommand;
import net.prezz.mpr.model.command.DeleteMultipleFromPlaylistCommand;
import net.prezz.mpr.model.command.DeleteStoredPlaylistCommand;
import net.prezz.mpr.model.command.LoadStoredPlaylistCommand;
import net.prezz.mpr.model.command.MoveInPlaylistCommand;
import net.prezz.mpr.model.command.MoveInStoredPlaylistCommand;
import net.prezz.mpr.model.command.NextCommand;
import net.prezz.mpr.model.command.PauseCommand;
import net.prezz.mpr.model.command.PlayCommand;
import net.prezz.mpr.model.command.PlayPauseCommand;
import net.prezz.mpr.model.command.PreviousCommand;
import net.prezz.mpr.model.command.PrioritizeCommand;
import net.prezz.mpr.model.command.PrioritizeUriCommand;
import net.prezz.mpr.model.command.RandomCommand;
import net.prezz.mpr.model.command.RepeatCommand;
import net.prezz.mpr.model.command.SaveCurrentPlaylistCommand;
import net.prezz.mpr.model.command.SeekCommand;
import net.prezz.mpr.model.command.ShuffleCommand;
import net.prezz.mpr.model.command.StopCommand;
import net.prezz.mpr.model.command.ToggleOutputCommand;
import net.prezz.mpr.model.command.UnprioritizeCommand;
import net.prezz.mpr.model.command.UpdateLibraryCommand;
import net.prezz.mpr.model.command.UpdatePrioritiesCommand;
import net.prezz.mpr.model.command.VolumeDownCommand;
import net.prezz.mpr.model.command.VolumeUpCommand;
import net.prezz.mpr.mpd.connection.Filter;
import net.prezz.mpr.mpd.connection.MpdConnection;
import net.prezz.mpr.mpd.connection.RejectAllFilter;

public class MpdSendControlCommands extends MpdConnectionCommand<List<Command>, ResponseResult>{

    private static final Filter FILE_FILTER = new Filter() {
        @Override
        public boolean accepts(String line) {
            return line.startsWith("file: ");
        }
    };

    private static final Filter ID_FILTER = new Filter() {
        @Override
        public boolean accepts(String line) {
            return line.startsWith("Id: ");
        }
    };

    public MpdSendControlCommands(List<Command> commands) {
        super(commands);
    }

    @Override
    protected ResponseResult doExecute(MpdConnection connection, List<Command> commands) throws Exception {
        if (commands.isEmpty()) {
            return new ResponseResult(true);
        }

        Integer volume = null;
        PlayerState playerState = null;
        for (Command command : commands) {
            if (command instanceof AddToNewStoredPlaylistCommand) {
                String playlistName = ((AddToNewStoredPlaylistCommand) command).getPlaylistName();
                LibraryEntity[] entities = ((AddToNewStoredPlaylistCommand) command).getEntities();
                addToStoredPlaylist(connection, playlistName, entities);
            }
            if (command instanceof AddToPlaylistCommand) {
                LibraryEntity[] entities = ((AddToPlaylistCommand) command).getEntities();
                addToPlaylist(connection, entities);
            }
            if (command instanceof AddToStoredPlaylistCommand) {
                StoredPlaylistEntity storedPlaylist = ((AddToStoredPlaylistCommand) command).getStoredPlaylist();
                LibraryEntity[] entities = ((AddToStoredPlaylistCommand) command).getEntities();
                addToStoredPlaylist(connection, storedPlaylist.getPlaylistName(), entities);
            }
            if (command instanceof AddUriToNewStoredPlaylistCommand) {
                String playlistName = ((AddUriToNewStoredPlaylistCommand) command).getPlaylistName();
                UriEntity[] entities = ((AddUriToNewStoredPlaylistCommand) command).getEntities();
                addToStoredPlaylist(connection, playlistName, entities);
            }
            if (command instanceof AddUriToPlaylistCommand) {
                UriEntity[] entities = ((AddUriToPlaylistCommand) command).getEntities();
                addToPlaylist(connection, entities);
            }
            if (command instanceof AddUriToStoredPlaylistCommand) {
                StoredPlaylistEntity storedPlaylist = ((AddUriToStoredPlaylistCommand) command).getStoredPlaylist();
                UriEntity[] entities = ((AddUriToStoredPlaylistCommand) command).getEntities();
                addToStoredPlaylist(connection, storedPlaylist.getPlaylistName(), entities);
            }
            if (command instanceof ClearPlaylistCommand) {
                connection.writeResponseCommand("clear\n", RejectAllFilter.INSTANCE);
            }
            if (command instanceof ConsumeCommand) {
                boolean consume = ((ConsumeCommand) command).getConsume();
                connection.writeResponseCommand(String.format("consume %s\n", consume ? "1" : "0"), RejectAllFilter.INSTANCE);
            }
            if (command instanceof DeleteFromPlaylistCommand) {
                int pos = ((DeleteFromPlaylistCommand) command).getPos();
                connection.writeResponseCommand(String.format("delete %s\n", pos), RejectAllFilter.INSTANCE);
            }
            if (command instanceof DeleteFromStoredPlaylistCommand) {
                String name = ((DeleteFromStoredPlaylistCommand) command).getStoredPlaylist().getPlaylistName();
                int pos = ((DeleteFromStoredPlaylistCommand) command).getPos();
                connection.writeResponseCommand(String.format("playlistdelete \"%s\" %s\n", name, pos), RejectAllFilter.INSTANCE);
            }
            if (command instanceof DeleteMultipleFromPlaylistCommand) {
                List<String> commandList = new ArrayList<String>();
                for (Integer id : ((DeleteMultipleFromPlaylistCommand) command).getIdentifiers()) {
                    commandList.add(String.format("deleteid %s\n", id));
                }
                connection.writeResponseCommandList(commandList.toArray(new String[commandList.size()]), RejectAllFilter.INSTANCE);
            }
            if (command instanceof DeleteStoredPlaylistCommand) {
                StoredPlaylistEntity entity = ((DeleteStoredPlaylistCommand) command).getEntity();
                connection.writeResponseCommand(String.format("rm \"%s\"\n", entity.getPlaylistName()), RejectAllFilter.INSTANCE);
            }
            if (command instanceof LoadStoredPlaylistCommand) {
                StoredPlaylistEntity entity = ((LoadStoredPlaylistCommand) command).getEntity();
                connection.writeResponseCommand(String.format("load \"%s\"\n", entity.getPlaylistName()), RejectAllFilter.INSTANCE);
            }
            if (command instanceof MoveInPlaylistCommand) {
                int id = ((MoveInPlaylistCommand) command).getId();
                int to = ((MoveInPlaylistCommand) command).getTo();
                connection.writeResponseCommand(String.format("moveid %s %s\n", id, to), RejectAllFilter.INSTANCE);
            }
            if (command instanceof MoveInStoredPlaylistCommand) {
                String name = ((MoveInStoredPlaylistCommand) command).getStoredPlaylist().getPlaylistName();
                int from = ((MoveInStoredPlaylistCommand) command).getFrom();
                int to = ((MoveInStoredPlaylistCommand) command).getTo();
                connection.writeResponseCommand(String.format("playlistmove \"%s\" %s %s\n", name, from, to), RejectAllFilter.INSTANCE);
            }
            if (command instanceof NextCommand) {
                connection.writeResponseCommand("next\n", RejectAllFilter.INSTANCE);
            }
            if (command instanceof PauseCommand) {
                boolean resume = ((PauseCommand) command).getResume();
                connection.writeResponseCommand(String.format("pause %s\n", resume ? "0" : "1"), RejectAllFilter.INSTANCE);
            }
            if (command instanceof PlayCommand) {
                Integer id = ((PlayCommand) command).getId();
                if (id != null) {
                    connection.writeResponseCommand(String.format("playid %s\n", id), RejectAllFilter.INSTANCE);
                } else {
                    connection.writeResponseCommand("play\n", RejectAllFilter.INSTANCE);
                }
            }
            if (command instanceof PlayPauseCommand) {
                String state = getState(connection);
                if ("play".equals(state)) {
                    playerState = PlayerState.PAUSE;
                    connection.writeResponseCommand("pause 1\n", RejectAllFilter.INSTANCE);
                } else if ("stop".equals(state)) {
                    playerState = PlayerState.PLAY;
                    connection.writeResponseCommand("play\n", RejectAllFilter.INSTANCE);
                } else if ("pause".equals(state)) {
                    playerState = PlayerState.PLAY;
                    connection.writeResponseCommand("pause 0\n", RejectAllFilter.INSTANCE);
                }
            }
            if (command instanceof PreviousCommand) {
                connection.writeResponseCommand("previous\n", RejectAllFilter.INSTANCE);
            }
            if (command instanceof PrioritizeCommand) {
                LibraryEntity[] entities = ((PrioritizeCommand) command).getEntities();
                prioritize(connection, entities);
            }
            if (command instanceof PrioritizeUriCommand) {
                UriEntity[] entities = ((PrioritizeUriCommand) command).getEntities();
                prioritize(connection, entities);
            }
            if (command instanceof RandomCommand) {
                boolean random = ((RandomCommand) command).getRandom();
                connection.writeResponseCommand(String.format("random %s\n", random ? "1" : "0"), RejectAllFilter.INSTANCE);
            }
            if (command instanceof RepeatCommand) {
                boolean repeat = ((RepeatCommand) command).getRepeat();
                connection.writeResponseCommand(String.format("repeat %s\n", repeat ? "1" : "0"), RejectAllFilter.INSTANCE);
            }
            if (command instanceof UpdateLibraryCommand) {
                connection.writeResponseCommand("update\n", RejectAllFilter.INSTANCE);
            }
            if (command instanceof SaveCurrentPlaylistCommand) {
                String name = ((SaveCurrentPlaylistCommand) command).getName();
                connection.writeResponseCommand(String.format("save \"%s\"\n", name), RejectAllFilter.INSTANCE);
            }
            if (command instanceof SeekCommand) {
                int id = ((SeekCommand) command).getId();
                int pos = ((SeekCommand) command).getPosition();
                connection.writeResponseCommand(String.format("seekid %s %s\n", id, pos), RejectAllFilter.INSTANCE);
            }
            if (command instanceof ShuffleCommand) {
                connection.writeResponseCommand("shuffle\n", RejectAllFilter.INSTANCE);
            }
            if (command instanceof StopCommand) {
                connection.writeResponseCommand("stop\n", RejectAllFilter.INSTANCE);
            }
            if (command instanceof ToggleOutputCommand) {
                String outputId = ((ToggleOutputCommand) command).getOutputId();
                boolean enable = ((ToggleOutputCommand) command).getEnabled();
                String cmd = enable ? "enableoutput %s\n" : "disableoutput %s\n";
                connection.writeResponseCommand(String.format(cmd, outputId), RejectAllFilter.INSTANCE);
            }
            if (command instanceof UnprioritizeCommand) {
                UnprioritizeCommand cmd = (UnprioritizeCommand)command;
                connection.writeResponseCommand(String.format("prio 0 %s:%s\n", cmd.getFrom(), cmd.getTo()));
            }
            if (command instanceof UpdatePrioritiesCommand) {
                updatePriorities(connection);
            }
            if (command instanceof VolumeDownCommand) {
                int amount = ((VolumeDownCommand) command).getAmount();
                Integer currentVolume = getVolume(connection);
                if (currentVolume != null) {
                    if (currentVolume.intValue() == -1) {
                        volume = currentVolume;
                    } else {
                        volume = Math.max(0, currentVolume - amount);
                        if (currentVolume > 0) {
                            connection.writeResponseCommand(String.format("setvol %s\n", volume), RejectAllFilter.INSTANCE);
                        }
                    }
                }
            }
            if (command instanceof VolumeUpCommand) {
                int amount = ((VolumeUpCommand) command).getAmount();
                Integer currentVolume = getVolume(connection);
                if (currentVolume != null) {
                    if (currentVolume.intValue() == -1) {
                        volume = currentVolume;
                    } else {
                        volume = Math.min(100, currentVolume + amount);
                        if (currentVolume < 100) {
                            connection.writeResponseCommand(String.format("setvol %s\n", volume), RejectAllFilter.INSTANCE);
                        }
                    }
                }
            }
        }

        return new ResponseResult(true)
                .putResponseValue(ResponseResult.ValueType.VOLUME, volume)
                .putResponseValue(ResponseResult.ValueType.PLAYER_STATE, playerState);

    }

    @Override
    protected ResponseResult onError() {
        return new ResponseResult(false);
    }

    private String getState(MpdConnection connection) throws IOException {
        String[] response = connection.writeResponseCommand("status\n");
        for (String line : response) {
            if (line.startsWith("state: ")) {
                return line.substring(7);
            }
        }
        return null;
    }

    private Integer getVolume(MpdConnection connection) throws IOException {
        String[] response = connection.writeResponseCommand("status\n");
        for (String line : response) {
            if (line.startsWith("volume: ")) {
                return Integer.decode(line.substring(8));
            }
        }
        return null;
    }

    private void addToStoredPlaylist(MpdConnection connection, String playlistName, LibraryEntity[] entities) throws IOException {
        List<String> findCommands = new ArrayList<String>();
        for (int i = 0; i < entities.length; i++) {
            findCommands.addAll(MpdCommandHelper.createQuery("find", entities[i]));
        }

        connection.writeCommandList(findCommands.toArray(new String[findCommands.size()]));
        String[] files = connection.readResponse(FILE_FILTER);

        List<String> commands = new ArrayList<String>();
        for (String file : files) {
            commands.add(String.format("playlistadd \"%s\" \"%s\"\n", playlistName, file.substring(6)));
        }

        connection.writeResponseCommandList(commands.toArray(new String[commands.size()]), RejectAllFilter.INSTANCE);
    }

    private void addToStoredPlaylist(MpdConnection connection, String playlistName, UriEntity[] entities) throws IOException {
        List<String> commands = new ArrayList<String>();
        for (UriEntity uri : entities) {
            commands.add(String.format("playlistadd \"%s\" \"%s\"\n", playlistName, uri.getFullUriPath(false)));
        }

        connection.writeResponseCommandList(commands.toArray(new String[commands.size()]), RejectAllFilter.INSTANCE);
    }

    private void addToPlaylist(MpdConnection connection, LibraryEntity[] entities) throws IOException {
        if (connection.isMinimumVersion(0, 16, 0)) {
            List<String> commands = new ArrayList<String>();

            for (int i = 0; i < entities.length; i++) {
                commands.addAll(MpdCommandHelper.createQuery("findadd", entities[i]));
            }

            connection.writeResponseCommandList(commands.toArray(new String[commands.size()]), RejectAllFilter.INSTANCE);
        } else {
            List<String> findCommands = new ArrayList<String>();
            for (int i = 0; i < entities.length; i++) {
                findCommands.addAll(MpdCommandHelper.createQuery("find", entities[i]));
            }

            connection.writeCommandList(findCommands.toArray(new String[findCommands.size()]));
            String[] files = connection.readResponse(FILE_FILTER);

            List<String> commands = new ArrayList<String>();
            for (String file : files) {
                commands.add(String.format("add \"%s\"\n", file.substring(6)));
            }

            connection.writeResponseCommandList(commands.toArray(new String[commands.size()]), RejectAllFilter.INSTANCE);
        }
    }

    private void addToPlaylist(MpdConnection connection, UriEntity[] entities) throws IOException {
        List<String> commands = new ArrayList<String>();
        for (UriEntity uri : entities) {
            commands.add(String.format("add \"%s\"\n", uri.getFullUriPath(false)));
        }

        connection.writeResponseCommandList(commands.toArray(new String[commands.size()]), RejectAllFilter.INSTANCE);
    }

    private void prioritize(MpdConnection connection, LibraryEntity[] entities) throws IOException {
        SearchResult result = searchPlaylist(connection);
        int addPosition = result.destinationPosition;

        String[] files = findFiles(connection, entities);
        List<Integer> addedToPlaylist = addFilesToPlaylistWithId(connection, files, addPosition);

        setPriorities(connection, result, addedToPlaylist);
    }

    private void prioritize(MpdConnection connection, UriEntity[] entities) throws IOException {
        SearchResult result = searchPlaylist(connection);
        int addPosition = result.destinationPosition;

        String[] files = findFiles(connection, entities);
        List<Integer> addedToPlaylist = addFilesToPlaylistWithId(connection, files, addPosition);

        setPriorities(connection, result, addedToPlaylist);
    }

    private void updatePriorities(MpdConnection connection) throws IOException {
        if (connection.isMinimumVersion(0, 17, 0)) {
            SearchResult result = searchPlaylist(connection);
            List<String> commands = new ArrayList<String>();
            reprioritize(result, commands);
            connection.writeResponseCommandList(commands.toArray(new String[commands.size()]), RejectAllFilter.INSTANCE);
        }
    }

    private SearchResult searchPlaylist(MpdConnection connection) throws IOException {
        int playing = -1;

        String[] statusLines = connection.writeResponseCommand("status\n");
        for (String line : statusLines) {
            if (line.startsWith("song: ")) {
                playing = Integer.parseInt(line.substring(6));
            }
        }

        connection.writeCommand(String.format("playlistinfo\n"));

        TreeMap<Integer, Integer> unprioritizeMap = new TreeMap<Integer, Integer>();
        TreeMap<Integer, Integer> reprioritizeMap = new TreeMap<Integer, Integer>();
        Integer position = null;
        Integer id = null;
        boolean prioritized = false;
        boolean reprioritize = true;
        String line = null;
        while ((line = connection.readLine()) != null) {
            if (line.startsWith(MpdConnection.OK)) {
                break;
            }
            if (line.startsWith(MpdConnection.ACK)) {
                throw new IOException("Error reading MPD response: " + line);
            }

            if (line.startsWith("file: ")) {
                if (prioritized) {
                    unprioritizeMap.put(position, id);
                }

                if (position != null) {
                    int pos = position.intValue();
                    if (pos > playing && !prioritized) {
                        reprioritize = false;
                    }
                    if (pos >= playing && prioritized && reprioritize) {
                        reprioritizeMap.put(position, id);
                    }
                }
                id = null;
                position = null;
                prioritized = false;
            }
            if (line.startsWith("Id: ")) {
                id = Integer.decode(line.substring(4));
            }
            if (line.startsWith("Pos: ")) {
                position = Integer.decode(line.substring(5));
            }
            if (line.startsWith("Prio: ")) {
                prioritized = true;
            }
        }
        if (prioritized) {
            unprioritizeMap.put(position, id);
        }
        if (position != null) {
            int pos = position.intValue();
            if (pos > playing && !prioritized) {
                reprioritize = false;
            }
            if (pos > playing && prioritized && reprioritize) {
                reprioritizeMap.put(position, id);
            }
        }

        int end = playing;
        if (!reprioritizeMap.isEmpty()) {
            end = reprioritizeMap.lastKey().intValue();
        }

        SearchResult searchResult = new SearchResult();
        searchResult.destinationPosition = end + 1;
        searchResult.unprioritizeMap = unprioritizeMap;
        searchResult.reprioritizeMap = reprioritizeMap;
        return searchResult;
    }

    private String[] findFiles(MpdConnection connection, LibraryEntity[] entities) throws IOException {
        List<String> findCommands = new ArrayList<String>();
        for (int i = 0; i < entities.length; i++) {
            findCommands.addAll(MpdCommandHelper.createQuery("find", entities[i]));
        }
        connection.writeCommandList(findCommands.toArray(new String[findCommands.size()]));
        String[] files = connection.readResponse(FILE_FILTER);
        for (int i = 0; i < files.length; i++) {
            files[i] = files[i].substring(6);
        }
        return files;
    }

    private String[] findFiles(MpdConnection connection, UriEntity[] entities) throws IOException {
        List<String> result = new ArrayList<String>();

        for (int i = 0; i < entities.length; i++) {
            if (entities[i].getUriType() == UriType.FILE) {
                result.add(entities[i].getFullUriPath(false));
            } else {
                String listCommand = String.format("listall \"%s\"\n", entities[i].getFullUriPath(false));
                String[] files = connection.writeResponseCommand(listCommand, FILE_FILTER);
                for (int j = 0; j < files.length; j++) {
                    result.add(files[j].substring(6));
                }
            }
        }

        return result.toArray(new String[result.size()]);
    }

    private List<Integer> addFilesToPlaylistWithId(MpdConnection connection, String[] files, int position) throws IOException {
        String[] addCommands = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            addCommands[i] = String.format("addid \"%s\" %s\n", files[i], position++);
        }
        connection.writeCommandList(addCommands);
        String[] idLines = connection.readResponse(ID_FILTER);

        List<Integer> result = new ArrayList<Integer>(idLines.length);
        for (String line : idLines) {
            String id = line.substring(4);
            result.add(Integer.decode(id));
        }
        return result;
    }

    private void setPriorities(MpdConnection connection, SearchResult result, List<Integer> addedToPlaylist) throws IOException {
        if (connection.isMinimumVersion(0, 17, 0)) {
            List<String> priorityCommands = new ArrayList<String>();
            int priority = reprioritize(result, priorityCommands);
            for (Integer id : addedToPlaylist) {
                if (priority > 0) {
                    priorityCommands.add(String.format("prioid %s %s\n", priority--, id));
                }
            }
            connection.writeResponseCommandList(priorityCommands.toArray(new String[priorityCommands.size()]), RejectAllFilter.INSTANCE);
        }
    }

    private int reprioritize(SearchResult searchResult, List<String> priorityCommands) throws IOException {
        for (Integer id : searchResult.unprioritizeMap.values()) {
            priorityCommands.add(String.format("prioid 0 %s\n", id));
        }

        int priority = 255;
        for (Integer id : searchResult.reprioritizeMap.values()) {
            priorityCommands.add(String.format("prioid %s %s\n", priority--, id));
        }
        return priority;
    }

    private static final class SearchResult {
        public int destinationPosition;
        public TreeMap<Integer, Integer> unprioritizeMap;
        public TreeMap<Integer, Integer> reprioritizeMap;
    }
}
