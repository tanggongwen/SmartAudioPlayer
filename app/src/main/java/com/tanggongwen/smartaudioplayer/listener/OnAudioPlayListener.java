package com.tanggongwen.smartaudioplayer.listener;

public interface OnAudioPlayListener {
    void onAudioPlayReady(int duration);

    void onAudioPlaying(int current, int duration);

    void onAudioPlayEnd();

    void OnAudioPlayError();
}
