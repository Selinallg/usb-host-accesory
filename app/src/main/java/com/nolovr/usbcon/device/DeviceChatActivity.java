package com.nolovr.usbcon.device;

import com.nolovr.usbcon.main.BaseChatActivity;

import android.os.Bundle;
import android.util.Log;

public class DeviceChatActivity extends BaseChatActivity {

    private static final String TAG = "DeviceChatActivity";

    private AccessoryCommunicator communicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setBtText("devices_Send to Host");
        communicator = new AccessoryCommunicator(this) {

            @Override
            public void onReceive(byte[] payload, int length) {
               // Log.d(TAG, "onReceive: ="+length);
                if (length >= 1024) {
                    //
                } else {
                    printLineToUI("host> " + new String(payload, 0, length));
                }

            }

            @Override
            public void onError(String msg) {
                printLineToUI("error:" + msg);
            }

            @Override
            public void onConnected() {
                printLineToUI("connected");
            }

            @Override
            public void onDisconnected() {
                printLineToUI("disconnected");
            }
        };
    }

    @Override
    public void sendString(String string) {
        communicator.send(string.getBytes());
    }

    @Override
    protected boolean sendByte(byte[] datas) {
        return communicator.send(datas);
    }

    @Override
    public void onDestroy() {
        communicator.stop();
        super.onDestroy();
    }
}
