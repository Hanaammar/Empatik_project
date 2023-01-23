package com.empatik.models;

public class SongModel {
    private String songName;

    public SongModel() {
    }

    public SongModel(String songName) {
        this.songName = songName;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }
}
