package com.domain.user.etherarduino;

import android.app.AlertDialog;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.usb.*;
import android.view.View;
import android.widget.EditText;
import android.app.PendingIntent;
import android.content.*;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.app.PendingIntent.getActivity;

public class Transaction extends AppCompatActivity {
    UsbManager mUsbManager;
    UsbDevice device;
    private static final String ACTION_USB_PERMISSION = "com.domain.user.etherarduino.USB_PERMISSION";
    UsbInterface intf;
    UsbEndpoint endpoint;
    UsbDeviceConnection connection;
//    final int interfaceNo = 0, endpointNo_in = 1, endpointNo_out = 0;
    final int interfaceNo = 1, endpointNo_in = 0, endpointNo_out = 1;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            // Set up device communication
                            // It should be done in another to prevent blocking of UI
                            intf = device.getInterface(interfaceNo);
                            connection = mUsbManager.openDevice(device);
                            new Thread(new Runnable() {
                                public void run() {
                                    sendToArduino();
                                }
                            }).start();
                        }
                    } else {
                        Log.d("ERROR", "permission denied for device " + device);
                    }
                }
            }
        }
    };

    void sendToArduino()
    {
        // Show all interfaces & endpoints
//        Log.d("MYDEBUG", "Device Name: " + device.getProductName());
//        Log.d("MYDEBUG", "interface count: " + device.getInterfaceCount());
//        for (int i = 0; i < device.getInterfaceCount(); i++) {
//            UsbInterface intf = device.getInterface(i);
//            Log.d("MYDEBUG", "interface #" + i + ": " + intf.getName());
//            for (int j = 0; j < intf.getEndpointCount(); j++) {
//                UsbEndpoint endpoint = intf.getEndpoint(j);
//                Log.d("MYDEBUG", "endpoint #" + j + " describeContents: " + endpoint.describeContents());
//                Log.d("MYDEBUG", "endpoint #" + j + " getType: " + endpoint.getType());
//                Log.d("MYDEBUG", "endpoint #" + j + " getDirection: " + endpoint.getDirection());
//                Log.d("MYDEBUG", "endpoint #" + j + " toString: " + endpoint.toString());
//            }
//        }
        endpoint = intf.getEndpoint(endpointNo_in);
        Log.d("MYDEBUG", "endpoint type: " + endpoint.getType());

        // Send TX data to arduino
        byte[] data = {111, 111, 111};
        connection.claimInterface(intf, true);
        connection.bulkTransfer(endpoint, data, data.length, 3);

        Log.d("MYDEBUG", "done");

        receiveFromArduino();
    }

    void receiveFromArduino()
    {
        endpoint = intf.getEndpoint(endpointNo_out);
        Log.d("MYDEBUG", "endpoint type: " + endpoint.getType());

        // Send TX data to arduino
        byte[] data = new byte[1];
        connection.claimInterface(intf, true);
        connection.bulkTransfer(endpoint, data, data.length, 3);

        Log.d("MYDEBUG", "Received: " + data[0]);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transection);
    }

    public void SendTransaction(View view) {
        mUsbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        if (!deviceList.isEmpty()) {
            device = deviceList.get(deviceList.keySet().toArray()[0]);
            Log.d("MYDEBUG", "Device vendor id" + device.getVendorId());
        }
        if (device == null || device.getVendorId() != 0x403) {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("No arduino connected.")
                    .show();
            return;
        }
        try {
            // Request for permission
            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            this.registerReceiver(mUsbReceiver, filter);
            mUsbManager.requestPermission(device, mPermissionIntent);
        } catch (Exception ex) {
            Log.d("ERROR", ex.getClass().getName() + " : " + ex.getMessage());
        }
    }
}
