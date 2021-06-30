package com.nolovr.usbcon.host;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.nolovr.usbcon.UsbUtils.Constants;
import com.nolovr.usbcon.UsbUtils.HostAccessoryUtils;
import com.nolovr.usbcon.UsbUtils.LogUtils;
import com.nolovr.usbcon.main.BaseChatActivity;


public class HostChatActivity extends BaseChatActivity {

    private final AtomicBoolean      keepThreadAlive = new AtomicBoolean(true);
    private       UsbDevice          device;
    private       HostAccessoryUtils hostAccessoryUtils;

    private Context mContext;

    @Override
    public void sendString(final String string) {
        //byte[] bytes = new byte[Constants.BUFFER_SIZE_IN_BYTES];
        byte[] bytes = string.getBytes();
        if (!hostAccessoryUtils.sendData(bytes))
            printLineToUI("send error!");
    }

    @Override
    protected boolean sendByte(byte[] datas) {
        return hostAccessoryUtils.sendData(datas);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setBtText("host_send to devices");
        hostAccessoryUtils = new HostAccessoryUtils(this);
        LogUtils.d("onCreate");
        List<UsbDevice> devices = hostAccessoryUtils.searchForUsbAccessory();
        if (devices.size() > 0) {
            LogUtils.d("getDevices:" + devices.toString());
            device = devices.get(0);
            if (!hostAccessoryUtils.connect(device)) {
                LogUtils.e("connect:error");
                finish();
            } else {
                printLineToUI("connected - ready to communicate");
                mContext = this;
                new Thread(new CommunicationRunnable()).start();
            }
        } else {
            LogUtils.e("no devices");
            finish();
        }
    }

    @Override
    protected void onStop() {
        hostAccessoryUtils.disconnect();
        keepThreadAlive.set(false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hostAccessoryUtils != null) {
            hostAccessoryUtils.release();
        }
    }

    private class CommunicationRunnable implements Runnable {

        @Override
        public void run() {
            while (keepThreadAlive.get()) {
                byte[] bytes = hostAccessoryUtils.receiveData();
                // TODO: 2021/6/21 计算数据传输量

                if (bytes != null && bytes.length < 1024) {
                    printLineToUI("device> host:" + new String(bytes));
                }
            }
        }
    }



}
