package org.stereolux.cordova.serial;

import java.io.IOException;
import java.util.List;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

/**
 * Cordova plugin to communicate with the android serial port
 * @author Xavier Seignard <xavier.seignard@gmail.com>
 * @version 0.0.1
 */
public class Serial extends CordovaPlugin {
    // logging tag
    private final String TAG = Serial.class.getSimpleName();
    // actions definitions
    private static final String ACTION_REQUEST_PERMISSION = "requestPermission";
    private static final String ACTION_OPEN = "openSerial";
    private static final String ACTION_READ = "readSerial";
    private static final String ACTION_WRITE = "writeSerial";
    private static final String ACTION_CLOSE = "closeSerial";
    // flag to check if the user has permitted to use the USB/serial port
    private boolean permitted = false;
    // UsbManager instance to deal with permission and opening
    private UsbManager manager;
    // The current driver that handle the serial port
    private UsbSerialDriver driver;
    // The serial port that will be used in this plugin
    private UsbSerialPort port;
    
    /**
     * Overridden execute method
     * @param action the string representation of the action to execute
     * @param args
     * @param callbackContext the cordova {@link CallbackContext}
     * @return true if the action exists, false otherwise
     * @throws JSONException if the args parsing fails
     */
    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, "Action: " + action);
        JSONObject arg_object = args.optJSONObject(0);
        // request permission
        if (ACTION_REQUEST_PERMISSION.equals(action)) {
            requestPermission(callbackContext);
            return true;
        }
        // open serial port
        else if (ACTION_OPEN.equals(action)) {
            JSONObject opts = arg_object.has("opts")? arg_object.getJSONObject("opts") : new JSONObject();
            openSerial(opts, callbackContext);
            return true;
        }
        // write to the serial port
        else if (ACTION_WRITE.equals(action)) {
            String data = arg_object.getString("data");
            writeSerial(data, callbackContext);
            return true;
        }
        // read on the serial port
        else if (ACTION_READ.equals(action)) {
            readSerial(callbackContext);
            return true;
        }
        // close the serial port
        else if (ACTION_CLOSE.equals(action)) {
            closeSerial(callbackContext);
            return true;
        }
        // the action doesn't exist
        return false;
    }

    /**
     * Request permission the the user for the app to use the USB/serial port
     * @param callbackContext the cordova {@link CallbackContext}
     */
    private void requestPermission(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                // get UsbManager from Android
                manager = (UsbManager) cordova.getActivity().getSystemService(Context.USB_SERVICE);
                // find all available drivers from attached devices.
                List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
                if (!availableDrivers.isEmpty()) {
                    // get the first one as there is a high chance that there is no more than one usb device attached to your android
                    driver = availableDrivers.get(0);
                    UsbDevice device = driver.getDevice();
                    // create the intent that will be used to get the permission
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(cordova.getActivity(), 0, new Intent(UsbBroadcastReceiver.USB_PERMISSION), 0);
                    // and a filter on the permission we ask
                    IntentFilter filter = new IntentFilter(UsbBroadcastReceiver.USB_PERMISSION);
                    // this broadcast receiver will handle the permission results
                    final UsbBroadcastReceiver usbReceiver = new UsbBroadcastReceiver(callbackContext, TAG);
                    cordova.getActivity().registerReceiver(usbReceiver, filter);
                    // finally ask for the permission
                    manager.requestPermission(device, pendingIntent);
                }
                else {
                    // no available drivers
                    Log.d(TAG, "No device found!");
                    callbackContext.error("No device found!");
                }
            }
        });
    }

    /**
     * Open the serial port
     * @param opts a {@link JSONObject} containing the connection paramters
     * @param callbackContext the cordova {@link CallbackContext}
     */
    private void openSerial(final JSONObject opts, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                if (permitted) {
                    UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
                    if (connection != null) {
                        // get first port and open it
                        port = driver.getPorts().get(0);
                        try {
                            port.open(connection);
                            // get connection params or the default values
                            int baudRate = opts.has("baudRate") ? opts.getInt("baudRate") : 9600;
                            int dataBits = opts.has("dataBits") ? opts.getInt("dataBits") : UsbSerialPort.DATABITS_8;
                            int stopBits = opts.has("stopBits") ? opts.getInt("stopBits") : UsbSerialPort.STOPBITS_1;
                            int parity = opts.has("parity") ? opts.getInt("parity") : UsbSerialPort.PARITY_NONE;
                            port.setParameters(baudRate, dataBits, stopBits, parity);
                        }
                        catch (IOException  e) {
                            // deal with error
                            Log.d(TAG, e.getMessage());
                            callbackContext.error(e.getMessage());
                        }
                        catch (JSONException e) {
                            // deal with error
                            Log.d(TAG, e.getMessage());
                            callbackContext.error(e.getMessage());
                        }
                        Log.d(TAG, "Serial port opened!");
                        callbackContext.success("Serial port opened!");
                    }
                    else {
                        Log.d(TAG, "Cannot connect to the device!");
                        callbackContext.error("Cannot connect to the device!");
                    }
                }
                else {
                    Log.d(TAG, "Permission to connect to the device was denied!");
                    callbackContext.error("Permission to connect to the device was denied!");
                }
            }
        });
    }

    /**
     * Write on the serial port
     * @param data the {@link String} representation of the data to be written on the port
     * @param callbackContext the cordova {@link CallbackContext}
     */
    private void writeSerial(final String data, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                Log.d(TAG, data);
                try {
                    byte[] buffer = data.getBytes();
                    port.write(buffer, 1000);
                    callbackContext.success();
                } catch (IOException e) {
                    // deal with error
                    Log.d(TAG, e.getMessage());
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    /**
     * Read on the serial port
     * @param callbackContext the {@link CallbackContext}
     */
    private void readSerial(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    byte buffer[] = new byte[16];
                    int numBytesRead = port.read(buffer, 1000);
                    callbackContext.success(numBytesRead);
                }
                catch (IOException e) {
                    // deal with error
                    Log.d(TAG, e.getMessage());
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    /**
     * Close the serial port
     * @param callbackContext the cordova {@link CallbackContext}
     */
    private void closeSerial(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    port.close();
                    callbackContext.success();
                } catch (IOException e) {
                    // deal with error
                    Log.d(TAG, e.getMessage());
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }
}