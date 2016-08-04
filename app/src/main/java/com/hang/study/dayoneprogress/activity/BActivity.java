package com.hang.study.dayoneprogress.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.hang.study.dayoneprogress.R;
import com.hang.study.dayoneprogress.util.MyDownUtil;
import com.hang.study.dayoneprogress.util.SPUtil;

/**
 * Created by hang on 16/8/3.
 */
public class BActivity extends Activity implements View.OnClickListener {
    public Button start;
    public EditText url_input;
    public ProgressBar progressBar;
    public TextView showProgress;
    public myReceiver receiver;
    public Button stop;
    public MyDownUtil downUtil;
    public NotificationManager notify;
    public Notification notification;
    public static final String UPDATE = "PROGRESS_UPDATE";
    public static final String STOP = "PROGRESS_STOP";
    public static final int CONNECT_ERROR=1;
    public static final int UNKOWNHOST=2;
    public static final int SHOWNOTIFY=3;

    public Handler mHandler=new Handler() {
          @Override
          public void handleMessage(Message msg) {
              super.handleMessage(msg);
              switch (msg.what){
                  case CONNECT_ERROR:
                      Toast.makeText(BActivity.this,"请求失败，请检查地址是否正确",Toast.LENGTH_SHORT).show();
                      break;
                  case SHOWNOTIFY:
                      notify = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                      initNotify();
                      notify.notify(0, notification);
                      break;
                  case UNKOWNHOST:
                      Toast.makeText(BActivity.this,"请求失败，请检查网络连接是否正常",Toast.LENGTH_SHORT).show();
                      break;
              }

          }
      };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.b_layout);
        start = (Button) findViewById(R.id.start_download);
        stop = (Button) findViewById(R.id.stop_download);
        url_input = (EditText) findViewById(R.id.url_input);
        progressBar = (ProgressBar) findViewById(R.id.pb);
        progressBar.setMax(100);
        downUtil = new MyDownUtil(this,mHandler);
        showProgress = (TextView) findViewById(R.id.show_progress);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        IntentFilter filter = new IntentFilter(UPDATE);
        receiver = new myReceiver();
        registerReceiver(receiver, filter);

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

    @Override
    protected void onPause() {
        super.onPause();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver=null;
        }
        MyDownUtil.isDown = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    protected void onStart() {
        super.onStart();
        int curP = SPUtil.getInt(this, "curprogress");
        //显示上次的进度
        if (curP != -1) {
            progressBar.setVisibility(View.VISIBLE);
            showProgress.setVisibility(View.VISIBLE);
            progressBar.setProgress(curP);
            showProgress.setText("下载中：" + curP + "%");
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.start_download:
                startDownload();
                break;
            case R.id.stop_download:
                stopDownload();
                break;
        }

    }

    //开始下载
    private void startDownload() {
        MyDownUtil.isDown = true;
        progressBar.setVisibility(View.VISIBLE);
        final String url = url_input.getText().toString();
        if (!TextUtils.isEmpty(url)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    downUtil.download(url);
                }
            }).start();
        } else
            Toast.makeText(this, "下载地址不能为空", Toast.LENGTH_SHORT).show();
    }

    private void stopDownload() {
        MyDownUtil.isDown = false;
    }


    //接收广播更新进度条
    class myReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UPDATE)) {
                int cur = intent.getIntExtra("cur", -1);
                System.out.println("cur:" + cur);
                if (cur != -1) {
                    progressBar.setProgress(cur);
                    showProgress.setVisibility(View.VISIBLE);
                    updateNotifyProgress(cur);
                    SPUtil.setInt(BActivity.this, "curprogress", cur);
                    if (cur == 100) {
                        showProgress.setText("下载完成");
                        SPUtil.setInt(BActivity.this, "curprogress", -1);
                    } else
                        showProgress.setText("下载中:" + cur + "%");
                }
            }
        }
    }
}
