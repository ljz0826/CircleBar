package com.ljz.circlebar.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ljz.circlebar.R;
import com.ljz.circlebar.entities.FileInfo;
import com.ljz.circlebar.service.DownloadService;
import com.ljz.circlebar.util.LinearGradientUtil;
import com.ljz.circlebar.view.CircleBarView;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private CircleBarView circleBarView;
    private TextView textView, mFileName;
    private Button bt_start, bt_stop;
    public static MainActivity mMainActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        circleBarView = findViewById(R.id.circlebarview);
        textView = findViewById(R.id.text_progress);
        mFileName = findViewById(R.id.tv_name);
        bt_start = findViewById(R.id.start);
        bt_stop = findViewById(R.id.stop);
//        circleBarView.setProgressNum(3000);
        circleBarView.setProgressNum(50, 3000);
        circleBarView.setTextView(textView);
        circleBarView.setOnAnimationListener(new CircleBarView.OnAnimationListener() {
            @Override
            public String howToChangeText(float interpolatedTime, float progressNum, float maxNum) {
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                Log.i("zhou", "interpolatedTime" + interpolatedTime);
                String s = decimalFormat.format(interpolatedTime * progressNum / maxNum * 100) + "%";
                textView.setText(s);
                return s;
            }

            @Override
            public void howTiChangeProgressColor(Paint paint, float interpolatedTime, float progressNum, float maxNum) {
                LinearGradientUtil linearGradientUtil = new LinearGradientUtil(Color.YELLOW, Color.RED);
                paint.setColor(linearGradientUtil.getColor(interpolatedTime));
            }
        });

        final FileInfo  fileInfo = new FileInfo(0, "http://www.imooc.com/mobile/imooc.apk", "imooc.apk", 0, 0);
        bt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_START);
                intent.putExtra("fileInfo", fileInfo);
                startService(intent);
            }
        });
        bt_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_STOP);
                intent.putExtra("fileInfo", fileInfo);
                startService(intent);
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        registerReceiver(mReceiver, filter);
        mMainActivity = this;
    }

    /**
     * 更新UI的广播接收器
     */
    BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (DownloadService.ACTION_UPDATE.equals(intent.getAction()))
            {
                int finised = intent.getIntExtra("finished", 0);
                Log.i("zhou",finised+"");

//                mProgressBar.setProgress(finised);
            }
        }
    };

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }


    public Handler handler = new Handler()
    {
        @Override
        public void handleMessage(android.os.Message msg) {
            Toast.makeText(mMainActivity, "下载完毕", Toast.LENGTH_SHORT).show();
        }
    };


}
