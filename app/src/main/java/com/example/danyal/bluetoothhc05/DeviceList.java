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
//                    pairedDevicesList();
//                    Toast.makeText(DeviceList.this, "Looking for available devices 1", Toast.LENGTH_SHORT).show();
//                    Log.d("BT: " , "Looking for available devices 1");
//
//
//                    final ArrayList<String> devices = new ArrayList<>();
//                    final ArrayAdapter<String> theAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, devices);
//                    visibleDevices.setAdapter(theAdapter);
//                    blueReceiver = new BroadcastReceiver() {
//                        @Override
//                        public void onReceive(Context context, Intent intent) {
//                            String action = intent.getAction();
//                            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
//                                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
//                                if (state == BluetoothAdapter.STATE_ON) {
//                                    bluetoothAdapter.startDiscovery();
//                                } else if (state == BluetoothAdapter.STATE_OFF) {
//                                    devices.clear();
//                                    theAdapter.notifyDataSetChanged();
//                                }
//                            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                                Boolean repeated = false;
//                                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                                String deviceInfo = device.getName() + "\n" + device.getAddress();
//                                for (String x : devices) {
//                                    if (x.equals(deviceInfo)) {
//                                        repeated = true;
//                                        break;
//                                    }
//                                }
//                                if (!repeated)
//                                    devices.add(deviceInfo);
//                                theAdapter.notifyDataSetChanged();
//                            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
//                                devices.clear();
//                        }
//                    };
                }
            });

//            IntentFilter filter = new IntentFilter();
//            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
//            filter.addAction(BluetoothDevice.ACTION_FOUND);
//            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
//            registerReceiver(blueReceiver, filter);
        }
    }


    private void pairedDevicesList () {
//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(mReceiver, filter);

        pairedDevices = bluetoothAdapter.getBondedDevices();
        ArrayList list = new ArrayList();

        if ( pairedDevices.size() > 0 ) {
            for ( BluetoothDevice bt : pairedDevices ) {
                list.add(bt.getName().toString() + "\n" + bt.getAddress().toString());
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
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
