package com.nolovr.usbcon.main;

import com.nolovr.usbcon.R;
import com.nolovr.usbcon.UsbUtils.Constants;
import com.nolovr.usbcon.UsbUtils.LogUtils;
import com.nolovr.usbcon.device.DeviceChatActivity;
import com.nolovr.usbcon.host.HostChatActivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class BaseChatActivity extends Activity {

    private static final String TAG = "BaseChatActivity";

    private TextView contentTextView;
    private EditText input;
    private Button   sendBt;

    Context mContext = this;
    byte[]  datas    = new byte[Constants.TEST_BUFFER_LENGTH];

    public abstract void sendString(final String string);

    protected abstract boolean sendByte(byte[] datas);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //USB插入监听 添加广播过滤意图
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        filter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
        filter.addAction("android.hardware.usb.action.USB_ACCESSORY_ATTACHED");
        filter.addAction("android.hardware.usb.action.USB_ACCESSORY_DETACHED");
        filter.addAction("android.hardware.usb.action.USB_STATE");
        //registerReceiver(mUsbBroadcastReceiver, filter);
        fillDatas();

        contentTextView = (TextView) findViewById(R.id.content_text);
        input = (EditText) findViewById(R.id.input_edittext);
        sendBt = (Button) findViewById(R.id.send_button);
        sendBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String inputString = input.getText().toString();
                if (inputString.length() == 0) {
                    return;
                }
                sendString(inputString);
                printLineToUI(getString(R.string.local_prompt) + inputString);
                input.setText("");
            }
        });
    }

    public void setBtText(String text) {
        if (sendBt != null)
            sendBt.setText(text);
    }

    public void printLineToUI(final String line) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LogUtils.d("printLineToUI" + line);
                contentTextView.setText(contentTextView.getText() + "\n" + line);
            }
        });
    }


    //BOOLEAN为成员变量，自己随便起一个就行
    BroadcastReceiver mUsbBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.d("--->" + action);
            //判断外设
            if (action.equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {//外设已经连接
                startActivity(new Intent(mContext, HostChatActivity.class));
            } else if (action.equals("android.hardware.usb.action.USB_DEVICE_DETACHED")) {

            } else if (action.equals("android.hardware.usb.action.USB_ACCESSORY_ATTACHED")) {
                startActivity(new Intent(mContext, DeviceChatActivity.class));
            } else if (action.equals("android.hardware.usb.action.USB_ACCESSORY_DETACHED")) {

            }
            //判断存储usb
            if (action.equals("android.hardware.usb.action.USB_STATE")) {
                boolean connected = intent.getExtras().getBoolean("connected");
                if (connected) {//USB 已经连接

                } else {


                }
            }
            //判断耳机
            if (action.equals("android.intent.action.HEADSET_PLUG")) {
                int intExtra = intent.getIntExtra("state", 0);
                // state --- 0代表拔出，1代表插入
                // name--- 字符串，代表headset的类型。
                // microphone -- 1代表这个headset有麦克风，0则没有
                // int i=intent.getIntExtra("",0);
                if (intExtra == 0) {

                }
                if (intExtra == 1) {
                    //耳机插入
                    int intType = intent.getIntExtra("microphone", 0);
                    if (intType == 0) {

                        //没有麦克风
                    }
                    if (intType == 1) {

                        //有话筒
                    }
                }

            }
        }

    };

    private void fillDatas() {
        for (int i = 0; i < datas.length; i++) {
            datas[i] = (byte) i;
        }
    }

    DataLoopThread dataLoopThread;
    boolean        running = false;
    int            count   = 0;

    public void onDataLoopStart(View view) {

        //boolean ret = sendString(inputString);
        // 开启循环，
        running = true;
        if (dataLoopThread == null) {
            dataLoopThread = new DataLoopThread();
        }
        if (!dataLoopThread.isAlive()) {
            dataLoopThread.start();
        }

    }

    public void onDataLoopStop(View view) {
        running = false;
        count = 0;
        if (dataLoopThread != null) {
            dataLoopThread.interrupt();
            try {
                dataLoopThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            dataLoopThread = null;
        }
    }

    long lastMillos;
    long sendDatas = 0;

    private class DataLoopThread extends Thread {
        @Override
        public void run() {
            while (running) {
                long    startTime = System.currentTimeMillis();
                boolean ret       = sendByte(datas);
                count++;
                long endTime = System.currentTimeMillis();
                Log.d(TAG, ret + "  run: count=" + count + "use time =" + (endTime - startTime));
                if (ret) {
                    sendDatas = sendDatas + datas.length;
                }
                long chazhie = endTime - lastMillos;
                if (chazhie > 1000) {
                    Log.d(TAG, "run: chazhie=" + chazhie + "发送数据为：" + sendDatas);
                    sendDatas = 0;
                    lastMillos = endTime;
                }
            }
        }
    }

}
