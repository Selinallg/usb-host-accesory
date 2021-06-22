package com.nolovr.usbcon.device;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import com.nolovr.usbcon.UsbUtils.AccessoryUtils;
import com.nolovr.usbcon.UsbUtils.LogUtils;

public abstract class AccessoryCommunicator implements AccessoryUtils.AccessoryAttouchListener {

    private       Context       context;
    private final AtomicBoolean running = new AtomicBoolean(true);
    CommunicationThread thread;

    private AccessoryUtils accessoryUtils;

    private static final String TAG = "AccessoryCommunicator";

    public AccessoryCommunicator(final Context mContext) {
        this.context = mContext;
        accessoryUtils = new AccessoryUtils(context);
        accessoryUtils.setAccessoryAttouchListener(this);
        thread = new CommunicationThread();
        doOpenAccessory();

    }

    private void doOpenAccessory() {
        if (accessoryUtils.openAccessory(accessoryUtils.searchAccessory())) {
            onConnected();
            if (thread != null) {
                thread.start();
            }
        } else {
            onError("cann't connect,\nplease exit host apk and device apk and then open it again");
            onDisconnected();
        }
    }

    public boolean send(byte[] payload) {
        return accessoryUtils.sendData(payload);
    }

    public abstract void onReceive(final byte[] payload, final int length);

    public abstract void onError(String msg);

    public abstract void onConnected();

    public abstract void onDisconnected();

    public void stop() {
        running.set(false);
        accessoryUtils.closeAccessory();
        LogUtils.d("stop");
    }

    @Override
    public void onAttouch() {
        doOpenAccessory();
    }

    long lastTimeMillis;
    long revDataLenght;
    int count =0;

    private class CommunicationThread extends Thread {
        @Override
        public void run() {
            while (running.get()) {
                byte[] bytes = accessoryUtils.receiveData();
                if (bytes != null) {
                    long currentTimeMillis = System.currentTimeMillis();
                    long chazhi            = currentTimeMillis - lastTimeMillis;
                    count ++;
                    revDataLenght = revDataLenght + bytes.length;
                    if (chazhi > 1000) {
                        Log.d(TAG, "run: CommunicationThread rev data =" + revDataLenght  + "time =" + chazhi + "|-->"+count);
                        lastTimeMillis = currentTimeMillis;
                        revDataLenght = 0;
                        count = 0;
                    }
                    onReceive(bytes, bytes.length);
                }
            }
        }
    }


}