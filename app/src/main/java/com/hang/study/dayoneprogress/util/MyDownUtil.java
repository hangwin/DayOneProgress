package com.hang.study.dayoneprogress.util;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;

import com.hang.study.dayoneprogress.activity.BActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;


/**
 * Created by hang on 16/8/3.
 */
public class MyDownUtil {
    public Handler handler;
    public static  boolean isDown=false;
    public static String url;
    public long totalLength;
    public Context context;
    public static String path= Environment.getExternalStorageDirectory().getAbsolutePath()+"/DayOneProgress";
    public File dir;
    public MyDownUtil(Context context, Handler mHandler) {
        this.handler=mHandler;
        this.context=context;
        dir=new File(path);
        if(!dir.exists()||dir.isFile()) {
            dir.mkdir();
        }
    }

    public void download(String url) {
        this.url = url;
        try {
            URL requestUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            totalLength = connection.getContentLength();
            if (connection.getResponseCode() == 200) {
                handler.sendEmptyMessage(BActivity.SHOWNOTIFY);
                System.out.println("文件总长度：" + totalLength);
                File file = new File(dir,getFileNameFromUrl());
                RandomAccessFile raf = new RandomAccessFile(file, "rw");
                raf.setLength(totalLength);
                new downloadThread(0,totalLength-1).start();

            } else {
                System.out.println("请求失败");
            }
        }catch (ConnectException e) {
            System.out.println("请求失败");
            handler.sendEmptyMessage(BActivity.CONNECT_ERROR);
        }catch (UnknownHostException e) {
            handler.sendEmptyMessage(BActivity.UNKOWNHOST);
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFileNameFromUrl() {
        int index = url.lastIndexOf("/");
        String fileName = url.substring(index + 1);
        return fileName;
    }

    class downloadThread extends Thread {
        private long start, end, curLength;

        public downloadThread(long start, long end) {
            this.start = start;
            this.end = end;
        }


        @Override
        public void run() {
            super.run();
            InputStream is=null;
            RandomAccessFile raf=null;
            File file = new File(dir,getFileNameFromUrl() + "_position.txt");
            try {
                if (file.exists() && file.length() > 0) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                    String curLen=reader.readLine();
                    if(!TextUtils.isEmpty(curLen)) {
                        this.curLength=Integer.parseInt(curLen);
                        start+=this.curLength;
                    }
                }else {
                    this.curLength=0;
                }
                URL url=new URL(MyDownUtil.url);
                HttpURLConnection con= (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.setRequestProperty("Range", "bytes=" + start + "-" + end);
                con.connect();
               // long contentLength=con.getContentLength();
                if(con.getResponseCode()/100==2) {
                    File downloadFile=new File(dir,getFileNameFromUrl());
                    raf=new RandomAccessFile(downloadFile,"rw");
                    raf.seek(start);
                    byte[] buf=new byte[1024*1024];
                    is=con.getInputStream();
                    int len=0;
                    File position=new File(dir,getFileNameFromUrl()+"_position.txt");
                    long time=System.currentTimeMillis();
                    while((len=is.read(buf))!=-1) {
                         if(!isDown) {
                             System.out.println("暂停下载");
                             Intent intent = new Intent();
                             intent.setAction(BActivity.STOP);
                             intent.putExtra("cur", (int) (curLength *100/ totalLength ));
                             context.sendBroadcast(intent);
                             return;
                         }
                         RandomAccessFile r=new RandomAccessFile(position,"rwd");
                         raf.write(buf,0,len);
                         this.curLength+=len;
                        /* Message message=new Message();
                         Bundle data=new Bundle();
                         data.putInt("cur", (int) this.curLength);
                         data.putInt("total", (int) totalLength);
                         message.setData(data);
                         handler.sendMessage(message);
                         */
                         if(System.currentTimeMillis()-time>=500) {
                             Intent intent = new Intent();
                             intent.setAction(BActivity.UPDATE);
                             intent.putExtra("cur", (int) (curLength *100/ totalLength ));
                             context.sendBroadcast(intent);
                             time=System.currentTimeMillis();
                         }
                         r.write(String.valueOf(this.curLength).getBytes());
                         r.close();

                    }

                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (raf != null)
                        raf.close();
                    if (is != null)
                        is.close();
                }catch (Exception e) {
                    e.printStackTrace();
                }

            }
            File position=new File(dir,getFileNameFromUrl()+"_position.txt");
            if(position.exists()) {
                position.delete();
            }
        }
    }
}
