package com.tanggongwen.smartaudioplayer.core;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Build;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.tanggongwen.simpleaudioplayer.beans.AudioBaseBean;
import com.tanggongwen.simpleaudioplayer.listener.OnAudioPlayListener;
import com.tanggongwen.smartaudioplayer.beans.AudioBaseBean;
import com.tanggongwen.smartaudioplayer.listener.OnAudioPlayListener;

public class AudioEngine {
    Context context;
    private AudioManager audioManager;
    private int audioFocusResult;
    //    private TXVodPlayer txVodPlayer;
//    private TXCloudVideoView txCloudVideoView;
    private OnAudioPlayListener audioPlayListener;
    private AudioFocusRequest audioFocusRequest;
    private AudioConnection connection;
    private AudioPlayService.AudioBinder audioBinder;
    private boolean pauseByUser = false;

    @SuppressLint("ServiceCast")
    public AudioEngine(Context context, OnAudioPlayListener listener) {
        this.context = context;
        this.audioPlayListener = listener;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        connection = new AudioConnection();
        Intent intent = new Intent(context, AudioPlayService.class);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        initAudioRequest(onAudioFocusChangeListener);

    }


    /**
     * 播放音频
     *
     * @param audio
     */
    public void playAudio(AudioBaseBean audio) {
        if (requestAudioFocus()) {
            audioBinder.start(audio.getAudioUrl());
        }
    }


    /**
     * 暂停播放
     */
    public void pauseAudio() {
        audioBinder.pause();
    }

    public void seekTo(int progress) {
        audioBinder.seekTo(progress);
    }

    /**
     * 继续播放
     */
    public void resumeAudio() {
        if (requestAudioFocus()) {
            audioBinder.play();
            pauseByUser = false;
        }
    }


    public boolean isPlaying() {
        return audioBinder.isPlaying();
    }

    /**
     * 请求音频焦点
     */
    private boolean requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusResult = audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            audioFocusResult = audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
        return audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }


    private void initAudioRequest(OnAudioFocusChangeListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())

                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(listener)
                    .build();
        }

    }


    public void stopEngine() {
        if (phoneStateListener == null) {
            return;
        }
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        abandonAudioFocus();
    }

    private void abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
        } else {
            audioManager.abandonAudioFocus(onAudioFocusChangeListener);
        }
    }

    public void setPauseByUser(boolean pauseByUser) {
        this.pauseByUser = pauseByUser;
    }


    public class AudioConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                audioBinder = (AudioPlayService.AudioBinder) service;
            }catch (Exception e){

            }finally {
                audioBinder.setAudioListener(audioPlayListener);
            }


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            audioBinder = null;
        }
    }

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    if (pauseByUser) {
                        return;
                    }
                    resumeAudio();
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                case TelephonyManager.CALL_STATE_RINGING:
                    if (audioBinder.isPlaying()) {
                        pauseAudio();
                    }

                    break;
            }
        }
    };

    /**
     * 对音频焦点的监听
     */
    private OnAudioFocusChangeListener onAudioFocusChangeListener = focusChange -> {
        switch (focusChange) {

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pauseAudio();
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                if (pauseByUser) {
                    return;
                }
                resumeAudio();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                pauseAudio();
                break;
        }
    };

}
