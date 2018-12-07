package com.tanggongwen.smartaudioplayer.core;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.tanggongwen.simpleaudioplayer.listener.OnAudioPlayListener;

public class AudioPlayService extends Service {
    private MediaPlayer audioPlayer;
    private AudioBinder audioBinder;
    private boolean isSetData;
    private OnAudioPlayListener onAudioPlayListener;
    private static final int AUDIO_ON_PLAYING = 60001;
    private static final int AUDIO_ON_STOP = 60002;
    private boolean isEnd = true;

    @Override
    public void onCreate() {
        super.onCreate();

        //初始化数据
        isSetData = false;
        audioPlayer = new MediaPlayer();
        audioBinder = new AudioBinder();
    }

    private void playMusic(String path) {
        try {
            //设置资源
            isEnd = true;
            audioPlayer.reset();
            audioPlayer.setDataSource(path);
            isSetData = true;

            //异步缓冲准备及监听
            audioPlayer.prepareAsync();
            audioPlayer.setOnPreparedListener(mp -> {
                onAudioPlayListener.onAudioPlayReady(mp.getDuration());
                audioPlayer.start();
                isEnd = false;
                handler.sendEmptyMessageDelayed(AUDIO_ON_PLAYING, 500);
            });

            //播放结束监听
            audioPlayer.setOnCompletionListener(mp -> {
                if (null == onAudioPlayListener) {
                    return;
                }
                isEnd = true;
                onAudioPlayListener.onAudioPlayEnd();
            });

            audioPlayer.setOnErrorListener((mp, what, extra) -> {
                if (null == onAudioPlayListener) {
                    return false;
                }
                isEnd = true;
                onAudioPlayListener.OnAudioPlayError();
                return false;
            });
        } catch (Exception e) {
            e.printStackTrace();
            isSetData = false;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (audioPlayer != null) {
            audioPlayer.stop();
            audioPlayer.release();
        }
        isSetData = false;
    }

    class AudioBinder extends Binder {

        //开始播放
        void start(String songUrl) {
            playMusic(songUrl);
        }

        //获取资源状态
        boolean isSetData() {
            return isSetData;
        }

        //获取当前播放状态
        boolean isPlaying() {
            return audioPlayer.isPlaying();
        }

        //继续播放
        boolean play() {
            if (isSetData) {
                if (!audioPlayer.isPlaying()) {
                    audioPlayer.start();
                }
            }
            return audioPlayer.isPlaying();
        }

        //暂停播放
        boolean pause() {
            if (isSetData) {
                if (audioPlayer.isPlaying()) {
                    audioPlayer.pause();
                }
            }
            return audioPlayer.isPlaying();
        }

        /**
         * 获取歌曲当前时长位置
         * 如果返回-1，则mediaplayer没有缓冲歌曲
         *
         * @return
         */
        int getCurrent() {
            if (isSetData) {
                return audioPlayer.getCurrentPosition();
            } else {
                return -1;
            }
        }


        void seekTo(int progress) {
            if (isSetData) {
                audioPlayer.seekTo(progress);
            }
        }

        /**
         * 获取歌曲总时长
         * 如果返回-1，则mediaplayer没有缓冲歌曲
         *
         * @return
         */
        int getDuration() {
            if (isSetData) {
                return audioPlayer.getDuration();
            } else {
                return -1;
            }
        }

        void setAudioListener(OnAudioPlayListener playListener) {
            onAudioPlayListener = playListener;
        }


    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (null == onAudioPlayListener) {
                return;
            }
            if (msg.what == AUDIO_ON_PLAYING && !isEnd) {
                onAudioPlayListener.onAudioPlaying(audioPlayer.getCurrentPosition(), audioPlayer.getDuration());
                handler.sendEmptyMessageDelayed(AUDIO_ON_PLAYING, 500);
            }else if (isEnd){
                onAudioPlayListener.onAudioPlayEnd();
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return audioBinder;
    }


}
