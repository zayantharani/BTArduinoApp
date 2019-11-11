package com.example.danyal.bluetoothhc05;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;

public class MyBroadcastReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        ArrayList list = new ArrayList();
        Log.d("BT: " , "Looking for available devices");
        Toast.makeText(context, "Looking for available devices", Toast.LENGTH_SHORT).show();


        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            // Discovery has found a device. Get the BluetoothDevice
            // object and its info from the Intent.
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceName = device.getName();
            String deviceHardwareAddress = device.getAddress(); // MAC address
            list.add(deviceName + "\n" + deviceHardwareAddress );
//
//            if ( pairedDevices.size() > 0 ) {
//                for ( BluetoothDevice bt : pairedDevices ) {
//
//                }
//            } else {
//                Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
//            }

            final ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, list);
//            visibleDevices.setAdapter(adapter);
//            visibleDevices.setOnItemClickListener(myListClickListener);
        }
    }
}
