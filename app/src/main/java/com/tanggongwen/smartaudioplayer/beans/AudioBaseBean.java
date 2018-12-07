package com.tanggongwen.smartaudioplayer.beans;

import java.io.Serializable;

public class AudioBaseBean {
    private String audioTitle;
    private String audioDes;
    private String audioUrl;
    private String audioId;
    private Serializable otherData;

    public String getAudioTitle() {
        return audioTitle;
    }

    public void setAudioTitle(String title) {
        this.audioTitle = title;
    }

    public String getAudioDes() {
        return audioDes;
    }

    public void setAudioDes(String audioDes) {
        this.audioDes = audioDes;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getAudioId() {
        return audioId;
    }

    public void setAudioId(String audioId) {
        this.audioId = audioId;
    }

    public Serializable getOtherData() {
        return otherData;
    }

    public void setOtherData(Serializable otherData) {
        this.otherData = otherData;
    }
}
