package com.tanggongwen.smartaudioplayer.core;

import android.content.Context;

import com.bakclass.web.audio.bean.AudioBaseBean;
import com.bakclass.web.util.SharedPreManager;
import com.tanggongwen.smartaudioplayer.beans.AudioBaseBean;
import com.tanggongwen.smartaudioplayer.listener.OnAudioPlayListener;

import net.sourceforge.simcpux.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public enum AudioHelper {
    INSTANCE;
    private Context context;
    private AudioEngine audioEngine;
    private AudioBaseBean currentAudio;
    private List<? extends AudioBaseBean> audioList = new ArrayList<>();
    private int currentAudioPos;
    private AudioPlayMode playMode;
    private AudioNotificationEngine notificationEngine;
    private List<OnAudioPlayListener> audioPlayListenerList = new ArrayList<>();


    public void init(Context context) {
        this.context = context;
        audioEngine = new AudioEngine(context, onAudioPlayListener);
        notificationEngine = new AudioNotificationEngine(context);
        playMode = getPlayMode();
    }


    /**
     * 从设置获的当前播放模式
     *
     * @return
     */
    private AudioPlayMode getPlayMode() {
        AudioPlayMode mode;
        int modeInt = SharedPreManager.getInt(Constants.AUDIO_PLAY_MODE);
        if (modeInt == AudioPlayMode.PLAY_MODE_RANDOM.ordinal()) {
            mode = AudioPlayMode.PLAY_MODE_RANDOM;
        } else if (modeInt == AudioPlayMode.PLAY_MODE_SINGLE_CYCLE.ordinal()) {
            mode = AudioPlayMode.PLAY_MODE_SINGLE_CYCLE;
        } else {
            mode = AudioPlayMode.PLAY_MODE_LIST_CYCLE;
        }
        return mode;
    }


    /**
     * 设置播放列表
     *
     * @param audioList
     */
    public void setAudioList(List<? extends AudioBaseBean> audioList) {
        currentAudio = null;
        currentAudioPos = 0;
        this.audioList = audioList;
    }

    /**
     * 播放下一首
     */
    public void playNext() {
        if (null == audioList || audioList.isEmpty()) {
            return;
        }
        currentAudio = changePlayAudio(true);
        audioEngine.playAudio(currentAudio);
        notificationEngine.updateAudioNotification(currentAudio.getAudioTitle(), currentAudio.getAudioDes(), audioEngine.isPlaying());
    }


    public void setPauseByUser(boolean pauseByUser) {
        audioEngine.setPauseByUser(pauseByUser);
    }

    /**
     * 播放上一首
     */
    public void playLast() {
        if (null == audioList || audioList.isEmpty()) {
            return;
        }
        currentAudio = changePlayAudio(false);
        audioEngine.playAudio(currentAudio);
        notificationEngine.updateAudioNotification(currentAudio.getAudioTitle(), currentAudio.getAudioDes(), audioEngine.isPlaying());
    }


    public void playStart(AudioBaseBean audio) {
        currentAudio = audio;
        audioEngine.playAudio(currentAudio);
        notificationEngine.updateAudioNotification(currentAudio.getAudioTitle(), currentAudio.getAudioDes(), audioEngine.isPlaying());
    }


    public void playStart(int pos) {
        currentAudioPos = pos;
        currentAudio = audioList.get(pos);
        audioEngine.playAudio(currentAudio);
        notificationEngine.updateAudioNotification(currentAudio.getAudioTitle(), currentAudio.getAudioDes(), audioEngine.isPlaying());
    }


    public void playResume() {
        audioEngine.resumeAudio();
        notificationEngine.updateAudioNotification(currentAudio.getAudioTitle(), currentAudio.getAudioDes(), audioEngine.isPlaying());
    }

    public void playPaues() {
        audioEngine.pauseAudio();
        notificationEngine.updateAudioNotification(currentAudio.getAudioTitle(), currentAudio.getAudioDes(), audioEngine.isPlaying());
    }

    /**
     * 切换播放源
     *
     * @return
     */
    private AudioBaseBean changePlayAudio(boolean next) {
        AudioBaseBean audio;
        if (next) {
            audio = getNextAudio();
        } else {
            audio = getLastAudio();
        }
        return audio;
    }

    /**
     * 获得下一个播放地址
     */
    private AudioBaseBean getNextAudio() {
        AudioBaseBean nextAudio;
        if (playMode == AudioPlayMode.PLAY_MODE_RANDOM) {
            nextAudio = getRandomAudio();
        } else if (playMode == AudioPlayMode.PLAY_MODE_SINGLE_CYCLE) {
            nextAudio = currentAudio;
        } else {
            if (currentAudioPos == audioList.size() - 1) {
                currentAudioPos = 0;
            } else {
                currentAudioPos = currentAudioPos + 1;
            }
            nextAudio = audioList.get(currentAudioPos);
        }
        this.currentAudio = nextAudio;
        return nextAudio;
    }


    /**
     * 获得上一首播放地址
     *
     * @return
     */
    private AudioBaseBean getLastAudio() {
        AudioBaseBean lastAudio;
        if (playMode == AudioPlayMode.PLAY_MODE_RANDOM) {
            lastAudio = getRandomAudio();
        } else if (playMode == AudioPlayMode.PLAY_MODE_SINGLE_CYCLE) {
            lastAudio = currentAudio;
        } else {
            if (currentAudioPos == 0) {
                currentAudioPos = audioList.size() - 1;
            } else {
                currentAudioPos = currentAudioPos - 1;
            }
            lastAudio = audioList.get(currentAudioPos);
        }
        this.currentAudio = lastAudio;
        return lastAudio;
    }


    /**
     * 获取随机播放地址
     *
     * @return
     */
    private AudioBaseBean getRandomAudio() {
        AudioBaseBean randomAudio;
        int randomPos;
        Random random = new Random();
        randomPos = random.nextInt(audioList.size());
        this.currentAudioPos = randomPos;
        randomAudio = audioList.get(randomPos);
        return randomAudio;
    }

    /**
     * 设置播放模式
     *
     * @param mode
     */
    public void setPlayMode(AudioPlayMode mode) {
        SharedPreManager.putInt(Constants.AUDIO_PLAY_MODE, mode.ordinal());
    }


    public boolean isPlaying() {
        return audioEngine.isPlaying();
    }

    public void seekToProgress(int progress) {
        audioEngine.seekTo(progress);
    }

    /**
     * 取消通知栏
     */
    public void cancelNotification() {
        notificationEngine.cancelAudioNotification();
    }

    public void cancel() {
        audioEngine.stopEngine();
    }

    /**
     * 添加监听
     *
     * @param audioPlayListener
     */
    public void addAudioPlayListener(OnAudioPlayListener audioPlayListener) {
        if (null != audioPlayListener) {
            audioPlayListenerList.add(audioPlayListener);
        }
    }

    /**
     * 移除监听
     *
     * @param audioPlayListener
     */
    public void removeAudioPlayListener(OnAudioPlayListener audioPlayListener) {
        if (null != audioPlayListener) {
            audioPlayListenerList.remove(audioPlayListener);
        }
    }


    public int getCurrentAudioPos() {
        return currentAudioPos;
    }

    public AudioBaseBean getCurrentAudio() {
        return currentAudio;
    }

    private OnAudioPlayListener onAudioPlayListener = new OnAudioPlayListener() {
        @Override
        public void onAudioPlayReady(int duration) {
            for (OnAudioPlayListener listener : audioPlayListenerList) {
                listener.onAudioPlayReady(duration);
            }
        }

        @Override
        public void onAudioPlaying(int current, int duration) {
            for (OnAudioPlayListener listener : audioPlayListenerList) {
                listener.onAudioPlaying(current, duration);
            }
        }

        @Override
        public void onAudioPlayEnd() {
            for (OnAudioPlayListener listener : audioPlayListenerList) {
                listener.onAudioPlayEnd();
            }
            playNext();
        }

        @Override
        public void OnAudioPlayError() {
            for (OnAudioPlayListener listener : audioPlayListenerList) {
                listener.OnAudioPlayError();
            }
        }
    };

    public enum PlayMode{

    }


}
