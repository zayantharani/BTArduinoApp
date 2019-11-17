package com.example.danyal.bluetoothhc05;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class DeviceList extends AppCompatActivity {

    Button btnPaired;
    ListView visibleDevices;

    private BluetoothAdapter bluetoothAdapter = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";
    final BroadcastReceiver mReceiver =new MyBroadcastReceiver();

    private BroadcastReceiver blueReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        btnPaired = (Button) findViewById(R.id.button);
        visibleDevices = (ListView) findViewById(R.id.listView);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        bluetoothAdapter.enable();
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.startDiscovery();
            }

            btnPaired.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View v) {
                    pairedDevicesList();
                }
            });


        }
    }


    private void pairedDevicesList () {

        pairedDevices = bluetoothAdapter.getBondedDevices();
        ArrayList list = new ArrayList();

        if (SettingsActivity.deviceList.size() == 0){
            Intent intent = new Intent(DeviceList.this, SettingsActivity.class);
            startActivity(intent);

            Toast.makeText(this, "Please register a device first", Toast.LENGTH_LONG).show();
        }

        else{
            if ( pairedDevices.size() > 0 ) {
                for ( BluetoothDevice bt : pairedDevices ) {
                    if (SettingsActivity.deviceList.equals(bt.getName().toLowerCase()))
                        list.add(bt.getName().toString() + "\n" + bt.getAddress().toString());
                }
            } else {
                Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
            }
        }


        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        visibleDevices.setAdapter(adapter);
        visibleDevices.setOnItemClickListener(myListClickListener);
    }


    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length()-17);

            Intent i = new Intent(DeviceList.this, ledControl.class);
            i.putExtra(EXTRA_ADDRESS, address);
            startActivity(i);
            finish();
        }
    };
}
