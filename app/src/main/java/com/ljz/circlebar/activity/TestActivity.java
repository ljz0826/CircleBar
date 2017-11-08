package com.ljz.circlebar.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.util.zip.Inflater;

/**
 * Created by lenovo on 2017/11/7.
 */

public class TestActivity extends Activity {

    private MyBroadCast myBroadCast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("aaa");
        registerReceiver(myBroadCast,intentFilter);

        sendBroadcast(new Intent("aaa"));
//        sendOrderedBroadcast(new Intent("aaa"));
//        LocalBroadcastManager.getInstance(this).registerReceiver();
//        sendStickyBroadcast
    }

    class MyBroadCast extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadCast);
    }
}
