package com.example.danyal.bluetoothhc05;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ledControl extends AppCompatActivity {

    Button autoButton, manualButton, zeroButton, oneButton, twoButton;
    String address = null;
    TextView tempTextView;
    EditText deltaTEditText;
    ImageView bluetoothImageView;
    static Context context;

    private ProgressDialog progress;
    Switch onOffSwitch;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    InputStream inputStream;
    OutputStream outputStream;
    static boolean switchIsChecked;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS);

        setContentView(R.layout.activity_led_control);

        autoButton = (Button) findViewById(R.id.autoButton);
        manualButton = (Button) findViewById(R.id.manualButton);
        zeroButton = (Button) findViewById(R.id.zeroButton);
        oneButton = (Button) findViewById(R.id.oneButton);
        twoButton= (Button) findViewById(R.id.twoButton);
        tempTextView = (TextView) findViewById(R.id.tempTextView);
        deltaTEditText = (EditText) findViewById(R.id.deltaTEditText);
        bluetoothImageView = (ImageView) findViewById(R.id.bluetoothImageView);
        onOffSwitch = (Switch) findViewById(R.id.onOffSwitch);
        context = this.getApplicationContext();

        new ConnectBT().execute();

        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    switchIsChecked = true;
                    onOffSwitch.setText("On");
                }
                else
                {
                    switchIsChecked = false;
                    onOffSwitch.setText("Off");
                }
            }
        });

        autoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if (switchIsChecked){
                    deltaTEditText.setVisibility(View.VISIBLE);
                    autoButton.setBackgroundColor(Color.GRAY);
                    manualButton.setBackgroundResource(android.R.drawable.btn_default);
                    zeroButton.setVisibility(View.INVISIBLE);
                    oneButton.setVisibility(View.INVISIBLE);
                    twoButton.setVisibility(View.INVISIBLE);
                    tempTextView.setText("");

                    byte b[] = {'<',1, 2, 3, 4,'>'};
                    sendSignal(b);
                }
                else
                    Toast.makeText(ledControl.this, "Please turn on the device", Toast.LENGTH_SHORT).show();
            }
        });

        manualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if (switchIsChecked){
                    manualButton.setBackgroundColor(Color.GRAY);
                    autoButton.setBackgroundResource(android.R.drawable.btn_default);
                    deltaTEditText.setVisibility(View.INVISIBLE);
                    zeroButton.setVisibility(View.VISIBLE);
                    oneButton.setVisibility(View.VISIBLE);
                    twoButton.setVisibility(View.VISIBLE);
                    tempTextView.setText("");

                    sendSignal("2>");
                }
                else
                    Toast.makeText(ledControl.this, "Please turn on the device", Toast.LENGTH_SHORT).show();

            }
        });

        zeroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if (switchIsChecked){
                    sendSignal("0");
                }
                else
                    Toast.makeText(ledControl.this, "Please turn on the device", Toast.LENGTH_SHORT).show();

            }
        });

        oneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if (switchIsChecked){
                    sendSignal("1");
                }
                else
                    Toast.makeText(ledControl.this, "Please turn on the device", Toast.LENGTH_SHORT).show();
            }
        });

        twoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if (switchIsChecked){
                    sendSignal("2");
                }
                else
                    Toast.makeText(ledControl.this, "Please turn on the device", Toast.LENGTH_SHORT).show();
            }
        });



        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!isBtConnected)
                        Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                byte[] buffer = new byte[1024];
                int bytes;
                while(true) {

                    try {
                        bytes = inputStream.read(buffer);
                        String data = new String(buffer);
                        final String filterData;
                        Log.d("tester", String.valueOf(bytes));
//                        Log.d("tester2", getString(btSocket.getInputStream()));
                        Log.d("tester3", "" + data);
                        filterData = data.substring(0,2);
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    tempTextView.setText(filterData);
                                }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }
    // Function to convert an Input Stream to String in Java
//    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getString(InputStream in) throws IOException
    {
        Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(reader);

        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append('\n');
        }
        br.close();

        return sb.toString();
    }
    private void sendSignal ( String number ) {
        if ( btSocket != null ) {
            try {
                outputStream.write(number.toString().getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void sendSignal (byte [] number) {
        if ( btSocket != null ) {
            try {
                outputStream.write(number);
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

//    private void receiveSignal ( String number ) {
//        if ( btSocket != null ) {
//            try {
//                btSocket.getOutputStream().write(number.toString().getBytes());
//            } catch (IOException e) {
//                msg("Error");
//            }
//        }
//    }
    private void Disconnect () {
        if ( btSocket!=null ) {
            try {
                btSocket.close();
            } catch(IOException e) {
                msg("Error");
            }
        }

        finish();
    }

    private void msg (String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected  void onPreExecute () {
            try{
                progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please Wait!!!");
            }
            catch (Exception e)
            {
                Log.e("Exception", e.getMessage());
            }
        }

        @Override
        protected Void doInBackground (Void... devices) {
            try {
                if ( btSocket==null || !isBtConnected ) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                    inputStream=btSocket.getInputStream();
                    outputStream=btSocket.getOutputStream();

                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute (Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected");
                isBtConnected = true;
                bluetoothImageView.setImageResource(R.drawable.ic_bluetooth_connected);
            }

            progress.dismiss();
        }
    }
}
