package com.ljz.circlebar.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ljz.circlebar.entities.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lenovo on 2017/11/6.
 */

public class DownloadService extends Service
{
    public static final String DOWNLOAD_PATH =
            Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/downloads/";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final int MSG_INIT = 0;
    private DownloadTask mTask = null;
    private String TAG = "DownloadService";

    /**
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        // 获得Activity传过来的参数
        if (ACTION_START.equals(intent.getAction()))
        {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            // 启动初始化线程
            new InitThread(fileInfo).start();
        }
        else if (ACTION_STOP.equals(intent.getAction()))
        {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");

            if (mTask != null)
            {
                mTask.isPause = true;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what)
            {
                case MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    Log.i(TAG, "Init:" + fileInfo);
                    // 启动下载任务
                    mTask = new DownloadTask(DownloadService.this, fileInfo);
                    mTask.downLoad();
                    break;

                default:
                    break;
            }
        };
    };

    private class InitThread extends Thread
    {
        private FileInfo mFileInfo = null;

        public InitThread(FileInfo mFileInfo)
        {
            this.mFileInfo = mFileInfo;
        }

        /**
         * @see java.lang.Thread#run()
         */
        @Override
        public void run()
        {
            HttpURLConnection connection = null;
            RandomAccessFile raf = null;

            try
            {
                // 连接网络文件
                URL url = new URL(mFileInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("GET");
                int length = -1;

                if (connection.getResponseCode() == 200)
                {
                    // 获得文件的长度
                    length = connection.getContentLength();
                }

                if (length <= 0)
                {
                    return;
                }

                File dir = new File(DOWNLOAD_PATH);
                if (!dir.exists())
                {
                    dir.mkdir();
                }

                // 在本地创建文件
                File file = new File(dir, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                // 设置文件长度
                raf.setLength(length);
                mFileInfo.setLength(length);
                mHandler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (connection != null)
                {
                    connection.disconnect();
                }
                if (raf != null)
                {
                    try
                    {
                        raf.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

}