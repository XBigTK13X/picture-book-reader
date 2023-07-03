package com.simplepathstudios.pbr.audio;

import com.simplepathstudios.pbr.api.model.MusicFile;

interface IAudioPlayer {
    void play(MusicFile musicFile, int seekPosition);
    void stop();
    void pause();
    void seek(int position);
    void resume(int position);
    Integer getCurrentPosition();
    Integer getSongDuration();
    void destroy();
    boolean isPlaying();
    void setVolume(double volume);
}
