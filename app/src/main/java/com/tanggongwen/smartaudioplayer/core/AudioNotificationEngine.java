package com.tanggongwen.smartaudioplayer.core;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import com.bakclass.web.R;
import com.bakclass.web.study.HomeActivity;

import net.sourceforge.simcpux.Constants;

public class AudioNotificationEngine extends Notification {
    private Notification audioNotification = null;

    private Context context;
    private final int REQUEST_CODE = 30000;
    int flags = 10001;
    private NotificationManager manager = null;
    private Builder builder = null;

    private RemoteViews remoteViews;
    private Intent play, next, last, close;
    private PendingIntent musicPendIntent = null;

    public void setContext(Context context) {
        this.context = context;
    }

    public void setManager(NotificationManager manager) {
        this.manager = manager;
    }

    public AudioNotificationEngine(Context context) {
        this.context = context;
        // 初始化操作
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.activity_audio_notification);
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new Builder(context);
        play = new Intent();
        play.setAction(Constants.AUDIO_NOTIFICATION_ACTION_PLAY);
        next = new Intent();
        next.setAction(Constants.AUDIO_NOTIFICATION_ACTION_NEXT);
        last = new Intent();
        last.setAction(Constants.AUDIO_NOTIFICATION_ACTION_LAST);
    }


    private void onCreateAudioNotifi() {

        // 1.注册控制点击事件

        PendingIntent playIntent = PendingIntent.getBroadcast(context, REQUEST_CODE,
                play, 0);
        remoteViews.setOnClickPendingIntent(R.id.img_audio_play,
                playIntent);

        // 2.注册下一首点击事件

        PendingIntent nextIntent = PendingIntent.getBroadcast(context, REQUEST_CODE,
                next, 0);
        remoteViews.setOnClickPendingIntent(R.id.img_audio_next,
                nextIntent);

        // 3.注册上一首点击事件

        PendingIntent lastIntent = PendingIntent.getBroadcast(context, REQUEST_CODE,
                last, 0);
        remoteViews.setOnClickPendingIntent(R.id.img_audio_last,
                lastIntent);
        //4.设置点击事件（挑战到播放界面）
        Intent intent = new Intent(context, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        builder.setContent(remoteViews).setWhen(System.currentTimeMillis())
                // 通知产生的时间，会在通知信息里显示
//              .setPriority(Notification.PRIORITY_DEFAULT)
                // 设置该通知优先级
                .setContentIntent(pendingIntent)
                .setOngoing(true).setTicker("播放新的一首歌")
                .setSmallIcon(R.drawable.bak_logo);

        // 兼容性实现

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            audioNotification = builder.getNotification();
        } else {
            audioNotification = builder.build();
        }
        audioNotification.flags = Notification.FLAG_ONGOING_EVENT;
        manager.notify(Constants.AUDIO_ACTION_BROADCAST_ID, audioNotification);
//        manager.cancel(0);
    }

    public void updateAudioNotification(String title, String note, boolean isplay) {
        // 设置添加内容
        if (title == null) {
            remoteViews.setTextViewText(R.id.audio_notifi_title, "未知");
        } else {
            remoteViews.setTextViewText(R.id.audio_notifi_title, title);
        }
        if (null == note) {
            remoteViews.setTextViewText(R.id.audio_notifi_note_name, "未知");
        } else {
            remoteViews.setTextViewText(R.id.audio_notifi_note_name, note);
        }
        if (isplay) {
            remoteViews.setImageViewResource(R.id.img_audio_play,
                    R.drawable.audio_btn_play);
        } else {
            remoteViews.setImageViewResource(R.id.img_audio_play,
                    R.drawable.audio_btn_play);
        }

        onCreateAudioNotifi(); //每一次改变都要重新创建，所以直接写这里
    }


    /**
     * 取消通知栏
     */
    public void cancelAudioNotification() {
        manager.cancel(Constants.AUDIO_ACTION_BROADCAST_ID);
    }


}
