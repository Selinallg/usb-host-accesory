package com.example.usbcon.main;

import com.example.usbcon.R;
import com.example.usbcon.UsbUtils.HostAccessoryUtils;
import com.example.usbcon.device.DeviceChatActivity;
import com.example.usbcon.host.HostChatActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class InfoActivity extends Activity {
    private static final String TAG = "InfoActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        final TextView textView = (TextView) findViewById(R.id.infotext);
       
        UsbManager usbManager =(UsbManager)getSystemService(Context.USB_SERVICE);      
        
        HostAccessoryUtils usbAccessoryUtils = new HostAccessoryUtils(this);
        if(usbManager.getAccessoryList()!=null) {
            Log.d(TAG, "onCreate: DeviceChatActivity");
        	startActivity(new Intent(this,DeviceChatActivity.class));
        } else {
            Log.d(TAG, "onCreate: HostChatActivity");
        	startActivity(new Intent(this,HostChatActivity.class));
        } 
           
    } 
}
