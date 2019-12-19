package com.example.danyal.bluetoothhc05;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    RadioGroup rgTemperatureUnit;
    RadioGroup rgTemperatureMode;

    RadioButton rbSelectedRadio;
    EditText etNewDeviceName;
    EditText etNewDeviceNameCustom;
    ImageView btnAddNewDevice;
    RecyclerView rvRegisteredDevices;
    static String deltaT;
    static ArrayList<Device> deviceList;
    MyListAdapter adapter;
    int tempUnit = 0;
    int tempMode = 0;

    TinyDB tinydb;
    EditText deltaTEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        tinydb = new TinyDB(SettingsActivity.this);
        deltaTEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                tinydb.putString("deltaTEditText",charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        rgTemperatureUnit = findViewById(R.id.rg_temp_units);
        tempUnit=tinydb.getInt("TempType");

        rgTemperatureUnit.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.rb_celcius) {
                    tempUnit = 0;
                }
                else {
                    tempUnit = 1;
                }
                tinydb.putInt("TempType",tempUnit);
            }
        });


        rgTemperatureMode = findViewById(R.id.rg_temp_mode);
        tempMode=tinydb.getInt("TempMode");

        rgTemperatureMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.rb_cold) {
                    tempMode = 0;
                }
                else {
                    tempMode = 1;
                }
                tinydb.putInt("TempMode",tempMode);
            }
        });

        if(tempUnit==0)rgTemperatureUnit.check(R.id.rb_celcius);
        else rgTemperatureUnit.check(R.id.rb_fahrenheit);
        rbSelectedRadio = findViewById(rgTemperatureUnit.getCheckedRadioButtonId());
        rbSelectedRadio.setChecked(true);
//        rb.selec
        //do your operations with "rbSelectedRadio" now
        etNewDeviceName = findViewById(R.id.et_new_device_name);
        btnAddNewDevice = findViewById(R.id.iv_add_new_device);
        rvRegisteredDevices = findViewById(R.id.rv_registered_devices);


        deviceList= new ArrayList<>();
        ArrayList<Object> pairedDevicesList=tinydb.getListObject("PairedDevices",Device.class);
//                tinydb.getListString("PairedDevices");
        for(Object Objdevice:pairedDevicesList){
            Device device= (Device) Objdevice;
            deviceList.add(new Device(device.deviceName,device.customDeviceName));
        }
        adapter = new MyListAdapter(deviceList);
        rvRegisteredDevices.setHasFixedSize(true);
        rvRegisteredDevices.setLayoutManager(new LinearLayoutManager(this));
        rvRegisteredDevices.setAdapter(adapter);

        btnAddNewDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //update the list view here
                String customDeviceName=etNewDeviceNameCustom.getText().toString().toLowerCase();
                String devicename = etNewDeviceName.getText().toString().toLowerCase();
                if(!devicename.isEmpty() && !customDeviceName.isEmpty()) {
                    Device newDevice=new Device(devicename,customDeviceName);
                    deviceList.add(newDevice);
                    ArrayList<Object> pairedDevicesList=tinydb.getListObject("PairedDevices",Device.class);
                    pairedDevicesList.add(newDevice);
//                    tinydb.putListString("PairedDevices",pairedDevicesList );
                    tinydb.putListObject("PairedDevices",pairedDevicesList);
                    adapter.notifyChange();
                    etNewDeviceName.setText("");
                    etNewDeviceNameCustom.setText("");
                    Toast.makeText(SettingsActivity.this, customDeviceName + " Added.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {
        ArrayList<Device> devices;
        MyListAdapter(ArrayList<Device> devices) {
            this.devices = devices;
        }
        void notifyChange(){
            notifyDataSetChanged();
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem = layoutInflater.inflate(R.layout.registered_divices_row, parent, false);
            return new ViewHolder(listItem);
        }
        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final Device device = devices.get(position);
            holder.textViewDeviceName.setText(device.customDeviceName);
            holder.imageViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(SettingsActivity.this, deviceList.get(position).customDeviceName + " Deleted.", Toast.LENGTH_SHORT).show();
                    ArrayList<Object> pairedDevicesList=tinydb.getListObject("PairedDevices",Device.class);
//                    ArrayList<Device> pairedDevicesList=n
                    Log.d("POPOL", "onClick: "+pairedDevicesList.size());
                    Log.d("POPOL", "onClick: "+deviceList.get(position).deviceName);
//                    for (Object x:pairedDevicesList
//                         ) {
//
//                    }

                    pairedDevicesList.remove(position);
                    Log.d("POPOL2", "onClick: "+pairedDevicesList.size());

//                    tinydb.putListString("PairedDevices",pairedDevicesList );
                    tinydb.putListObject("PairedDevices",pairedDevicesList);
                    deviceList.remove(position);
                    notifyChange();

                }
            });
        }

        @Override
        public int getItemCount() {
            if(devices!=null) {
                return devices.size();
            }else{
                return 0;
            }
        }
        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView textViewDeviceName;
            ImageView imageViewDelete;
            ConstraintLayout cl;
            ViewHolder(View itemView) {
                super(itemView);
                this.textViewDeviceName =  itemView.findViewById(R.id.tv_device_name);
                this.imageViewDelete = itemView.findViewById(R.id.iv_delete_device);
                this.cl = itemView.findViewById(R.id.constraint_layout_rv);
            }
        }
    }
}
