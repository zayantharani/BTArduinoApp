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
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import pl.pawelkleczkowski.customgauge.CustomGauge;

public class ledControl extends AppCompatActivity {

    Button autoButton, manualButton, zeroButton, oneButton, twoButton, sendSignalButton;
    String address = null;
    TextView tempTextView;
    CustomGauge cg1;
    ImageView btnInc, btnDec;
    TextView tv_temp;
    int initial_temp_val = 24;
    EditText deltaTEditText;
    TextView setTempTextView;
    ImageView bluetoothImageView;
    static Context context;
    static char oppMode;
    static char wingDirection;

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

        autoButton = (Button) findViewById(R.id.btn_auto);
        manualButton = (Button) findViewById(R.id.btn_manual);
        zeroButton = (Button) findViewById(R.id.zeroButton);
        oneButton = (Button) findViewById(R.id.oneButton);
        twoButton= (Button) findViewById(R.id.twoButton);
        cg1 = findViewById(R.id.gauge1);
        btnInc = findViewById(R.id.btnIncrease);
        btnDec = findViewById(R.id.btnDecrease);

        sendSignalButton = (Button) findViewById(R.id.sendSignalButton);
        tempTextView = (TextView) findViewById(R.id.tv_currentTemp); //Receiving
//        deltaTEditText = (EditText) findViewById(R.id.deltaTEditText);
        bluetoothImageView = findViewById(R.id.bluetoothImageview);
        onOffSwitch = (Switch) findViewById(R.id.onOffSwitch);
        setTempTextView =  findViewById(R.id.tv_ac_temp); //Sending

        new ConnectBT().execute();


        cg1.setPointSize(0);
        cg1.setSweepAngle(270);

        cg1.setPointStartColor(Color.parseColor("#00FF2B"));
        cg1.setPointEndColor(Color.parseColor("#FF0000"));
        cg1.setPointSize((initial_temp_val-15) * 18);
        setTempTextView.setText(initial_temp_val + "");

        cg1.setVisibility(View.INVISIBLE);
        cg1.setVisibility(View.VISIBLE);


        btnInc.setClickable(true);
        btnDec.setClickable(true);

        btnInc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int c_temp_val = Integer.parseInt(setTempTextView.getText().toString().trim());
                c_temp_val++;
                if(c_temp_val<=30){
                    setTempTextView.setText(c_temp_val+"");
                    int x = cg1.getPointSize() + 18;
                    cg1.setPointSize(x);
                    cg1.setPointStartColor(Color.parseColor("#00FF2B"));
                    cg1.setPointEndColor(Color.parseColor("#FF0000"));
                    cg1.setVisibility(View.INVISIBLE);
                    cg1.setVisibility(View.VISIBLE);
                }
            }
        });
        btnDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int c_temp_val = Integer.parseInt(setTempTextView.getText().toString().trim());
                c_temp_val--;
                if(c_temp_val>=15) {
                    setTempTextView.setText(c_temp_val +"");
                    int x = cg1.getPointSize() - 18;
                    cg1.setPointSize(x);
                    cg1.setPointStartColor(Color.parseColor("#00FF2B"));
                    cg1.setPointEndColor(Color.parseColor("#FF0000"));

                    cg1.setVisibility(View.INVISIBLE);
                    cg1.setVisibility(View.VISIBLE);
                }
            }
        });

        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    switchIsChecked = true;
                    onOffSwitch.setText("On");
                    char b[] = {'<','1', '-', '-', '-','-','-','>'};
                    sendSignal(b);
                }
                else
                {
                    char b[] = {'<','0', '-', '-', '-','-','-','>'};
                    sendSignal(b);
                    switchIsChecked = false;
                    onOffSwitch.setText("Off");
                }
            }
        });

        //Asking for temp from device
        Timer timerObj = new Timer();
        TimerTask timerTaskObj = new TimerTask() {
            public void run() {
                if (switchIsChecked)
                {
                    char b[] = {'<','1', oppMode, '?', '?',wingDirection,'-','>'};
                    sendSignal(b);
                    msg("Signal Sent");
                }
            }
        };
        timerObj.schedule(timerTaskObj, 0, 15000);

        autoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if (switchIsChecked){
                    String tempEditText= setTempTextView.getText().toString();
                    oppMode = '0';
                    manualButton.setEnabled(true);
                    deltaTEditText.setVisibility(View.VISIBLE);
                    autoButton.setBackgroundColor(Color.GRAY);
                    manualButton.setBackgroundResource(android.R.drawable.btn_default);
                    zeroButton.setVisibility(View.INVISIBLE);
                    oneButton.setVisibility(View.INVISIBLE);
                    twoButton.setVisibility(View.INVISIBLE);
                    tempTextView.setText("");
                    wingDirection = '2';


                }
                else
                    Toast.makeText(ledControl.this, "Please turn on the device", Toast.LENGTH_SHORT).show();
            }
        });

        manualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if (switchIsChecked){
                    manualButton.setEnabled(false);
                    autoButton.setEnabled(true);
                    oppMode = '1';
                    manualButton.setBackgroundColor(Color.GRAY);
                    autoButton.setBackgroundResource(android.R.drawable.btn_default);
                    deltaTEditText.setVisibility(View.INVISIBLE);
                    zeroButton.setVisibility(View.VISIBLE);
                    oneButton.setVisibility(View.VISIBLE);
                    twoButton.setVisibility(View.VISIBLE);
                    tempTextView.setText("");



                }
                else
                    Toast.makeText(ledControl.this, "Please turn on the device", Toast.LENGTH_SHORT).show();

            }
        });

        zeroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if (switchIsChecked)
                    wingDirection = '0';
                else
                    Toast.makeText(ledControl.this, "Please turn on the device", Toast.LENGTH_SHORT).show();

            }
        });

        oneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if (switchIsChecked)
                    wingDirection = '1';
                else
                    Toast.makeText(ledControl.this, "Please turn on the device", Toast.LENGTH_SHORT).show();
            }
        });

        twoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {

                if (switchIsChecked)
                    wingDirection = '1';
                else
                    Toast.makeText(ledControl.this, "Please turn on the device", Toast.LENGTH_SHORT).show();

            }
        });

        sendSignalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (switchIsChecked){

                    String tempEditText= setTempTextView.getText().toString();
                    if(tempEditText.length()>1){
                        char b[] = {'<','1', oppMode, tempEditText.charAt(0), tempEditText.charAt(1),wingDirection,'-','>'};
                        sendSignal(b);
                        msg("Signal Sent");


                        sendSignalButton.setEnabled(false);

                        Timer buttonTimer = new Timer();
                        buttonTimer.schedule(new TimerTask() {

                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        sendSignalButton.setEnabled(true);
                                    }
                                });
                            }
                        }, 5000);
                    }
                    else{
                        Toast.makeText(ledControl.this, "Please Set A Temperature", Toast.LENGTH_SHORT).show();
                    }
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
                byte[] buffer = new byte[1];
                int bytes;
                String mData="";
                while(true) {

                    try {
                        bytes = inputStream.read(buffer);
                        String data = new String(buffer);

//                        Log.d("tester", String.valueOf(bytes));
//                        Log.d("tester2", getString(btSocket.getInputStream()));
                        Log.d("tester3", "" + data);
                        if(data.equals(">")){
                            final String filterData=mData;
                            Log.d("tester4", mData);

                            if(mData.charAt(6)=='1'){
                                //Acknoledgement
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ledControl.this, "Acknowledgement Received", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else {
                                String rtp = Character.toString(mData.charAt(3)) + Character.toString(mData.charAt(4));
                                initial_temp_val = Integer.parseInt(rtp);
                                try{
                                    int roomTemp = Integer.parseInt(rtp);
                                    Log.d("RTP: ", Integer.toString(roomTemp));
                                    Log.d("Comparison", Character.compare(mData.charAt(2),'0') + "");
                                    if (Character.compare(mData.charAt(2),'0') == 0) {



                                        if (setTempTextView.getText().toString().length() > 1 && deltaTEditText.getText().toString().length() >= 1) {

                                            int setTemp = Integer.parseInt(setTempTextView.getText().toString());
                                            Log.d("Set Temp: ", Integer.toString(setTemp));
                                            int deltaTemp = Integer.parseInt(deltaTEditText.getText().toString());
                                            Log.d("delta Temp: ", Integer.toString(deltaTemp));

                                            if ((roomTemp - setTemp) > deltaTemp)
                                                wingDirection = '2';

                                            else if ((roomTemp - setTemp) == 0)
                                                wingDirection = '0';

                                            else if ((roomTemp - setTemp) <= deltaTemp)
                                                wingDirection = '1';

                                        }
                                    }
                                }
                                catch (Exception e){
                                    Log.e("Unable to convert", e.getMessage());
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tempTextView.setText("" + filterData.charAt(3) + filterData.charAt(4) + "°C");
                                    }
                                });
                            }

                            mData="";


                        }else{
                            mData+=data;
                        }


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

    private void sendSignal (char [] number) {
        if ( btSocket != null ) {
            try {
                for(int i=0;i<number.length;i++)
                outputStream.write(number[i]);
            } catch (IOException e) {
                msg("Error");
            }

        }

        }

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
