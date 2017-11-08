/*
 * @Title DownloadTask.java
 * @Copyright Copyright 2010-2015 Yann Software Co,.Ltd All Rights Reserved.
 * @Description??
 * @author Yann
 * @date 2015-8-7 ????10:11:05
 * @version 1.0
 */
package com.ljz.circlebar.service;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;


import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.ljz.circlebar.activity.MainActivity;
import com.ljz.circlebar.db.ThreadDAO;
import com.ljz.circlebar.db.ThreadDAOImpl;
import com.ljz.circlebar.entities.FileInfo;
import com.ljz.circlebar.entities.ThreadInfo;


/** 
 * ??????????
 * @author Yann
 * @date 2015-8-7 ????10:11:05
 */
public class DownloadTask
{
	private Context mContext = null;
	private FileInfo mFileInfo = null;
	private ThreadDAO mDao = null;
	private int mFinised = 0;
	public boolean isPause = false;
	
	/** 
	 *@param mContext
	 *@param mFileInfo
	 */
	public DownloadTask(Context mContext, FileInfo mFileInfo)
	{
		this.mContext = mContext;
		this.mFileInfo = mFileInfo;
		mDao = new ThreadDAOImpl(mContext);
	}
	
	public void downLoad()
	{
		// ???????????????
		List<ThreadInfo> threads = mDao.getThreads(mFileInfo.getUrl());
		ThreadInfo threadInfo = null;
		
		if (0 == threads.size())
		{
			// ???????????????
			threadInfo = new ThreadInfo(0, mFileInfo.getUrl(),
					0, mFileInfo.getLength(), 0);
		}
		else
		{
			threadInfo = threads.get(0);
		}
		
		// ????????????????
		new DownloadThread(threadInfo).start();
	}
	
	/** 
	 * ???????
	 * @author Yann
	 * @date 2015-8-8 ????11:18:55
	 */ 
	private class DownloadThread extends Thread
	{
		private ThreadInfo mThreadInfo = null;

		/** 
		 *@param mInfo
		 */
		public DownloadThread(ThreadInfo mInfo)
		{
			this.mThreadInfo = mInfo;
		}
		
		/**
		 * @see Thread#run()
		 */
		@Override
		public void run()
		{
			// ????????????????
			if (!mDao.isExists(mThreadInfo.getUrl(), mThreadInfo.getId()))
			{
				mDao.insertThread(mThreadInfo);
			}
			
			HttpURLConnection connection = null;
			RandomAccessFile raf = null;
			InputStream inputStream = null;
			
			try
			{
				URL url = new URL(mThreadInfo.getUrl());
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(5000);
				connection.setRequestMethod("GET");
				// ????????λ??
				int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
				connection.setRequestProperty("Range",
						"bytes=" + start + "-" + mThreadInfo.getEnd());
				// ???????д??λ??
				File file = new File(DownloadService.DOWNLOAD_PATH,
						mFileInfo.getFileName());
				raf = new RandomAccessFile(file, "rwd");
				raf.seek(start);
				Intent intent = new Intent();
				intent.setAction(DownloadService.ACTION_UPDATE);
				mFinised += mThreadInfo.getFinished();
				// ???????
				if (connection.getResponseCode() == 206)
				{
					// ???????
					inputStream = connection.getInputStream();
					byte buf[] = new byte[1024 << 2];
					int len = -1;
					long time = System.currentTimeMillis();
					while ((len = inputStream.read(buf)) != -1)
					{
						// д?????
						raf.write(buf, 0, len);
						// ???????????????Activity
						mFinised += len;
						if (System.currentTimeMillis() - time > 500)
						{
							time = System.currentTimeMillis();
							intent.putExtra("finished", mFinised * 100 / mThreadInfo.getEnd());
							mContext.sendBroadcast(intent);
						}
						
						// ???????????????????????
						if (isPause)
						{
							mDao.updateThread(mThreadInfo.getUrl(),	mThreadInfo.getId(), mFinised);
							return;
						}
					}
					
					// ?????????
					mDao.deleteThread(mThreadInfo.getUrl(),	mThreadInfo.getId());
					Log.i("DownloadTask", "???????");
					MainActivity.mMainActivity.handler.sendEmptyMessage(0);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					if (connection != null)
					{
						connection.disconnect();
					}
					if (raf != null)
					{
						raf.close();
					}
					if (inputStream != null)
					{
						inputStream.close();
					}
				}
				catch (Exception e2)
				{
					e2.printStackTrace();
				}
			}
		}
	}
}
