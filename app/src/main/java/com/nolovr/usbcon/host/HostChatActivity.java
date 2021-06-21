package com.nolovr.usbcon.host;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.nolovr.usbcon.UsbUtils.Constants;
import com.nolovr.usbcon.UsbUtils.HostAccessoryUtils;
import com.nolovr.usbcon.UsbUtils.LogUtils;
import com.nolovr.usbcon.main.BaseChatActivity;


public class HostChatActivity extends BaseChatActivity {

    private final AtomicBoolean keepThreadAlive = new AtomicBoolean(true);
    private UsbDevice device;
    private HostAccessoryUtils hostAccessoryUtils;
    
    private Context mContext;

    @Override
    public void sendString(final String string) {
        byte[] bytes = new byte[Constants.BUFFER_SIZE_IN_BYTES];
        bytes = string.getBytes();
        if(!hostAccessoryUtils.sendData(bytes))
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
        if(devices.size() > 0) {
        	LogUtils.d("getDevices:"+devices.toString());
        	device= devices.get(0);
        	if(!hostAccessoryUtils.connect(device)) {
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
        if (hostAccessoryUtils!=null){
            hostAccessoryUtils.release();
        }
    }

    private class CommunicationRunnable implements Runnable {

        @Override
        public void run() {
             while (keepThreadAlive.get()) {
                 byte[] bytes = hostAccessoryUtils.receiveData();
                 // TODO: 2021/6/21 计算数据传输量

                 if (bytes != null && bytes.length< 1024) {
                    printLineToUI("device> host:"+new String(bytes));
                 }
            }
        }
    }
    
    
//    private class FileRunnable implements Runnable {
//
//		@Override
//		public void run() {
//			// TODO Auto-generated method stub
//			FileInputStream is = null;
//			try {
//				is= mContext.openFileInput("sun");
//	    		byte[] bytes = new byte[Constants.BUFFER_SIZE_IN_BYTES];
//	    		int offset = 0;
//	    		int len;
//	    		LogUtils.d("transfer begin");
//	    		while((len =is.read(bytes)) != -1) {
//	    			hostAccessoryUtils.sendData(bytes);
//	    			LogUtils.d("len:"+len);
//	    			offset = offset+len;
//	    			LogUtils.d("offset:"+offset);
//	    		}
//	    	} catch(Exception e) {
//	    		LogUtils.e("error:"+e.toString());
//	    	} finally {
//	    		LogUtils.d("send over");
//	    		try {
//					if(is != null)
//						is.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//	    		String endCmd = "lijh";
//	    		hostAccessoryUtils.sendData(endCmd.getBytes());
//	    	}
//		}    	
//    }   
}
