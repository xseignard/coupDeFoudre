package org.stereolux.cordova.serial;

import org.apache.cordova.CallbackContext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class UsbBroadcastReceiver extends BroadcastReceiver {

	// usb permission tag name
    public static final String USB_PERMISSION ="org.stereolux.cordova.serial.USB_PERMISSION";
	
	private CallbackContext callbackContext;
	private String tag;
	
	public UsbBroadcastReceiver(CallbackContext callbackContext, String tag) {
		this.callbackContext = callbackContext;
		this.tag = tag;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
        if (USB_PERMISSION.equals(action)) {
            synchronized (this) {
                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if(device != null){
                    	//permitted = true;
                        Log.d(tag, "Permission to connect to the device was accepted!");
                        callbackContext.success("Permission to connect to the device was accepted!");
                   }
                } 
                else {
                	Log.d(tag, "Permission to connect to the device was denied!");
                    callbackContext.error("Permission to connect to the device was denied!");
                }
            }
        }
	}	
}
