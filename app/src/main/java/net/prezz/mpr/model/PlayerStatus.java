package net.prezz.mpr.model;

import java.io.Serializable;

public class PlayerStatus implements Serializable {

    private static final long serialVersionUID = -8096344131114099250L;

    private boolean connected;
    private long timestamp;
    private int playlistVersion;
    private boolean consume;
    private boolean random;
    private boolean repeat;
    private PlayerState state;
    private int currentSong;
    private int nextSong;
    private int volume;
    private int elapsedTime;
    private int totalTime;
    private String partition;
    private AudioOutput[] audioOutputs;


    public PlayerStatus(boolean connected) {
        this.connected = connected;
        this.timestamp = System.currentTimeMillis();
        this.playlistVersion = -1;
        this.consume = false;
        this.random = false;
        this.repeat = false;
        this.state = PlayerState.STOP;
        this.currentSong = -1;
        this.nextSong = -1;
        this.volume = 0;
        this.elapsedTime = 0;
        this.totalTime = 0;
        this.partition = "";
        this.audioOutputs = new AudioOutput[0];
    }

    public boolean isConnected() {
        return connected;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getPlaylistVersion() {
        return playlistVersion;
    }

    public void setPlaylistVersion(int playlistVersion) {
        this.playlistVersion = playlistVersion;
    }

    public boolean getConsume() {
        return consume;
    }

    public void setConsume(boolean consume) {
        this.consume = consume;
    }

    public boolean getRandom() {
        return random;
    }

    public void setRandom(boolean random) {
        this.random = random;
    }

    public boolean getRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public PlayerState getState() {
        return state;
    }

    public void setState(PlayerState state) {
        this.state = state;
    }

    public int getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(int currentSong) {
        this.currentSong = currentSong;
    }

    public int getNextSong() {
        return nextSong;
    }

    public void setNextSong(int nextSong) {
        this.nextSong = nextSong;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int time) {
        this.elapsedTime = time;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

    public AudioOutput[] getAudioOutputs() {
        return audioOutputs;
    }

    public void setAudioOutputs(AudioOutput[] audioOutputs) {
        this.audioOutputs = audioOutputs;
    }
}
