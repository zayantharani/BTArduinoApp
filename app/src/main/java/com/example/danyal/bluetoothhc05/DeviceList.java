package com.example.danyal.bluetoothhc05;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
    //    final BroadcastReceiver mReceiver =new MyBroadcastReceiver();
    TinyDB tinydb;
    private BroadcastReceiver blueReceiver;

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // R.menu.mymenu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mybutton) {
            // do something here
            Intent intent = new Intent(DeviceList.this, SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        //startActivity(new Intent(this, ledControl.class));
        btnPaired = (Button) findViewById(R.id.button);
        visibleDevices = (ListView) findViewById(R.id.listView);
        tinydb = new TinyDB(DeviceList.this);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
//        bluetoothAdapter.enable();
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.startDiscovery();
            }

            btnPaired.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(bAdapter == null)
                    {
                        Toast.makeText(getApplicationContext(),"Bluetooth Not Supported",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if(!bAdapter.isEnabled()){
                            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),1);
                            Toast.makeText(getApplicationContext(),"Bluetooth Turned ON",Toast.LENGTH_SHORT).show();
                        }
                    }


                    pairedDevicesList();
                }
            });


        }
    }


    private void pairedDevicesList() {

        pairedDevices = bluetoothAdapter.getBondedDevices();
        ArrayList list = new ArrayList();
        ArrayList<Object> registeredDevicesList;
        registeredDevicesList = tinydb.getListObject("PairedDevices",Device.class);
        if (registeredDevicesList != null) {
            if (registeredDevicesList.size() == 0) {
                Intent intent = new Intent(DeviceList.this, SettingsActivity.class);
                startActivity(intent);

                Toast.makeText(this, "Please register a device first", Toast.LENGTH_LONG).show();
            } else {
                if (pairedDevices.size() > 0) {
                    int i=0;
                    for (BluetoothDevice bt : pairedDevices) {
                        Log.i("BTNAME","BT Name: " + bt.getName());
                        if(i<registeredDevicesList.size()) {
//                            Log.i("BTNAME", "Device Name: " + (Device)registeredDevicesList.get(i++).customDeviceName);
                        }
                        for (Object o:
                             registeredDevicesList) {
                            Device device= (Device) o;
                            if (device.deviceName.toLowerCase().equals(bt.getName().toLowerCase()))
                                list.add(device.deviceName.toUpperCase() + "\n" + bt.getAddress().toString());
                        }

                    }
                } else {
                    Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Intent intent = new Intent(DeviceList.this, SettingsActivity.class);
            startActivity(intent);
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, R.layout.visible_devices_row, R.id.tv_visible_device_name, list);
        visibleDevices.setAdapter(adapter);
        visibleDevices.setOnItemClickListener(myListClickListener);
        visibleDevices.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String nameAndAddress=((TextView)view.findViewById(R.id.tv_visible_device_name)).getText().toString();
                Toast.makeText(DeviceList.this, nameAndAddress, Toast.LENGTH_SHORT).show();
                String[] nameAndAddressArr=nameAndAddress.split("\n");
//                tinydb.putString("");





                //Chaping

                AlertDialog.Builder builder = new AlertDialog.Builder(DeviceList.this);
                builder.setTitle("Title");

                // Set up the input
                final EditText input = new EditText(DeviceList.this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String m_Text = "";

                        m_Text = input.getText().toString();
                        Toast.makeText(DeviceList.this, "YOu text : "+m_Text, Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();



                //chaping end
                return true;
            }
        });
    }


    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            String info = ((TextView) view.findViewById(R.id.tv_visible_device_name)).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent i = new Intent(DeviceList.this, ledControl.class);
            i.putExtra(EXTRA_ADDRESS, address);
            startActivity(i);
            finish();
        }
    };
}
