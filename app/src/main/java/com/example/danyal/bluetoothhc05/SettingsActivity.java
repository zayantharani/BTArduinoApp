package com.example.danyal.bluetoothhc05;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
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
    RadioButton rbSelectedRadio;
    EditText etNewDeviceName;
    ImageView btnAddNewDevice;
    RecyclerView rvRegisteredDevices;
    static String deltaT;
    static ArrayList<Device> deviceList;
    MyListAdapter adapter;
    int tempUnit = 0;
    TinyDB tinydb;
    EditText deltaTEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        tinydb = new TinyDB(SettingsActivity.this);
        deltaTEditText = findViewById(R.id.deltaTEditText);
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
        ArrayList<String> pairedDevicesList=tinydb.getListString("PairedDevices");
        for(String device:pairedDevicesList){
            deviceList.add(new Device(device));
        }
        adapter = new MyListAdapter(deviceList);
        rvRegisteredDevices.setHasFixedSize(true);
        rvRegisteredDevices.setLayoutManager(new LinearLayoutManager(this));
        rvRegisteredDevices.setAdapter(adapter);

        btnAddNewDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //update the list view here

                String devicename = etNewDeviceName.getText().toString().toLowerCase();
                if(!devicename.isEmpty()) {
                    deviceList.add(new Device(devicename));
                    ArrayList pairedDevicesList=tinydb.getListString("PairedDevices");
                    pairedDevicesList.add(devicename);
                    tinydb.putListString("PairedDevices",pairedDevicesList );

                    adapter.notifyChange();
                    etNewDeviceName.setText("");
                    Toast.makeText(SettingsActivity.this, devicename + " Added.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


//    @Override
//    public void onBackPressed() {
//        //TODO: Dekh lou yaar isseh bhy
//    }
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
            holder.textViewDeviceName.setText(device.deviceName);
            holder.imageViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(SettingsActivity.this, deviceList.get(position).deviceName + " Deleted.", Toast.LENGTH_SHORT).show();
                    ArrayList pairedDevicesList=tinydb.getListString("PairedDevices");
                    pairedDevicesList.remove(deviceList.get(position).deviceName);
                    tinydb.putListString("PairedDevices",pairedDevicesList );
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
