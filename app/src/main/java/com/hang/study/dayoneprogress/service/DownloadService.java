package com.hang.study.dayoneprogress.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.hang.study.dayoneprogress.R;
import com.hang.study.dayoneprogress.activity.BActivity;
import com.hang.study.dayoneprogress.util.MyDownUtil;

/**
 * 下载服务
 * Created by hang on 16/8/5.
 */
public class DownloadService extends Service {
    public int lastProcess;
    public Notification notification;
    public NotificationManager notify;
    public myReceiver receiver=null;
    public static final int CONNECT_ERROR=1;
    public static final int UNKOWNHOST=2;
    public static final int SHOWNOTIFY=3;
    public Handler mHandler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case CONNECT_ERROR:
                   // Toast.makeText(BActivity.this, "请求失败，请检查地址是否正确", Toast.LENGTH_SHORT).show();
                    System.out.println("请求失败，请检查地址是否正确");
                    break;
                case SHOWNOTIFY:
                    notify = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    initNotify();
                    notify.notify(0, notification);
                    break;
                case UNKOWNHOST:
                  //  Toast.makeText(BActivity.this,"请求失败，请检查网络连接是否正常",Toast.LENGTH_SHORT).show();
                    System.out.println("请求失败，请检查网络连接是否正常");
                    break;
            }

        }
    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url=intent.getStringExtra("url");
        lastProcess=intent.getIntExtra("lastProcess",0);
        System.out.println("服务开启,url:"+url);
        receiver=new myReceiver();
        IntentFilter filter=new IntentFilter(BActivity.UPDATE);
        registerReceiver(receiver,filter);
        if(!TextUtils.isEmpty(url)) {
            startDown(url);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startDown(final String url) {
        final String murl=url;
        new Thread(new Runnable() {
            @Override
            public void run() {
                new MyDownUtil(DownloadService.this,mHandler,lastProcess).download(murl);
            }
        }).start();
    }


    @Override
    public void onDestroy() {
        System.out.println("服务停止");
        MyDownUtil.isDown = false;
        if(receiver!=null) {
            unregisterReceiver(receiver);
            receiver=null;
        }
        super.onDestroy();
    }

    //初始化通知
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initNotify() {
        long when = System.currentTimeMillis();
        Notification.Builder builder = new Notification.Builder(this);
        builder.setTicker("下载中");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setWhen(when);
        builder.setAutoCancel(true);
        RemoteViews notify = new RemoteViews(getPackageName(), R.layout.notify_layout);
        notify.setTextViewText(R.id.notification_update_progress_text, "0%");
        builder.setContent(notify);
        Intent intent = new Intent(this, BActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[]{intent}, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        notification = builder.build();
    }

    //更新通知栏进度条
    public void updateNotifyProgress(int progress) {
        notification.contentView.setProgressBar(R.id.notification_update_progress_bar, 100, progress, false);
        notification.contentView.setTextViewText(R.id.notification_update_progress_text, progress + "%");
        notify.notify(0, notification);
    }

    //接收广播更新进度条
    class myReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BActivity.UPDATE)) {
                int cur = intent.getIntExtra("cur", -1);
                System.out.println("cur:" + cur);
                if (cur != -1) {
                    updateNotifyProgress(cur);
                }
            }
        }
    }
}
