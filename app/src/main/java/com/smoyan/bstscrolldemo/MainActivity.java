package com.smoyan.bstscrolldemo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import protocol.sdk.shbst.com.singlechipsdk.services.ProtocolCallBack;
import protocol.sdk.shbst.com.singlechipsdk.services.ProtocolService;

public class MainActivity extends Activity implements ProtocolCallBack {

    private ProtocolService mService;
    private ServiceConnection mServiceConnection;
    private TranslateAnimation animation1, animation2;

    private RelativeLayout bg;
    private TextView text1, text2;
    private LinearLayout scroll;
    private long  mStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        bindAndStartService();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long cur = System.currentTimeMillis();
                if ((cur - mStartTime)/1000>2){
                    changeText("",true);
                }

            }
        },100,100);



    }

    private void initView() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        bg = findViewById(R.id.bg);
        text1 = findViewById(R.id.text1);
        text2 = findViewById(R.id.text2);
        scroll = findViewById(R.id.scroll);
        animation1 = new TranslateAnimation(0.0f, 0.0f, outMetrics.heightPixels, -1000);
        animation1.setInterpolator(new LinearInterpolator());
        animation1.setDuration(10000);
        animation1.setRepeatCount(-1);
        scroll.startAnimation(animation1);
//        Log.v("szm", "" + outMetrics.heightPixels + "  ");
//        animation2 = new TranslateAnimation(0.0f, 0.0f, outMetrics.heightPixels, -400);
//        animation2.setInterpolator(new LinearInterpolator());
//        animation2.setDuration(10000);
//        animation2.setStartOffset(5000);
//        animation2.setRepeatCount(-1);
//        text2.startAnimation(animation2);

        changeText("",true);
    }

    /**
     * 初始化协议SDK
     */
    private void bindAndStartService() {
        Intent bindIntent = new Intent(this, ProtocolService.class);
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {

            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = ((ProtocolService.ServiceBinder) service).getInstance();
                mService.bindCallBack(MainActivity.this);
                try {
                    mService.startReceiveService("/dev/ttymxc2", 9600, 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        bindService(bindIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onReceiveData(final byte[] data) {

        Log.d("test","receive dagta "+data[1]+ " ,"+data[2]);
        if (data[0] ==0x01){
            if((data[1]& 0x01) == 0x01){
                changeText(ConstantStr.NORMAL,true);
            }
            if((data[1]& 0x02)==0x02){
                changeText(ConstantStr.CHECK,false);
            }
            if((data[1]& 0x04) == 0x04){
                changeText(ConstantStr.OUTOFSERVERSI,false);
            }
            if((data[1]& 0x08) == 0x08){
                changeText(ConstantStr.NOENTRY,false);
            }

            mStartTime = System.currentTimeMillis();
            return;
        }else{
            return;
        }

        /*
        if (data[0] != 0x00) {
            return;
        }

        int index = 0;
        int[] states = new int[21];
        for (char c = 0; c < 21; c++) {
            if ((data[5 + c / 7] & (1 << (c % 7))) != 0) {
                states[index++] = c + 1;
            }
        }
        for (int i = 0; i < index; i++) {
            switch (states[i]) {
                case 7://故障
                    changeText(ConstantStr.NOENTRY,false);
                    break;
                case 9://停止运行
                    changeText(ConstantStr.OUTOFSERVERSI,false);
                    break;
                case 6://检修
                    changeText(ConstantStr.CHECK,false);
                    break;
                default: //其余皆为正常
                    changeText(ConstantStr.NORMAL,true);
                    break;
            }
        }
        */
    }


    private String mStatusString = null;
    private void changeText(final String str ,final boolean isOK) {


        if (str.equals(mStatusString)){
            return;
        }

        mStatusString = str;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (TextUtils.isEmpty(mStatusString)){
                    bg.setBackgroundColor(Color.BLACK);
                    animation1.cancel();
                    return ;
                }
                if(isOK) {
                    bg.setBackgroundColor(getResources().getColor(R.color.green));
                }else {
                    bg.setBackgroundColor(Color.RED);
                }
                text1.setText(str);
                text2.setText(str);

                animation1.cancel();
                scroll.startAnimation(animation1);

            }
        });
    }

    @Override
    public void onUpdateProcess(int i) {

    }

    @Override
    public void onReady(boolean b) {

    }

    @Override
    protected void onDestroy() {
        if (null != mService) {
            mService.stopProtocolService();
            unbindService(mServiceConnection);
        }

        super.onDestroy();
    }
}
