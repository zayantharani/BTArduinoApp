package com.example.danyal.bluetoothhc05;
//TODO:SetPoint Farhenheight wala chakkar

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
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

    Button autoButton, manualButton, zeroButton, twoButton, sendSignalButton;
    String address = null;
    static TextView tempTextView;
    CustomGauge cg1;
    ImageView btnInc, btnDec;
    TextView tv_temp;
    int initial_temp_val;
    TextView setTempTextView;
    ImageView bluetoothImageView, settingsImageView;
    static Context context;
    static char oppMode = 0;
    static char wingDirection = 2;
    int roomTemp;

    private ProgressDialog progress;
    Switch onOffSwitch;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    InputStream inputStream;
    OutputStream outputStream;
    String deltaT;
    TinyDB tinydb;
    static boolean switchIsChecked;

    int currentTempType = 0;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ConstraintLayout ledControlActivity = findViewById(R.id.ledControlActivity);

//        ledControlActivity.setBackgroundColor(getResources().getColor(R.color.backgroundColour));;

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS);
        tinydb = new TinyDB(ledControl.this);

        currentTempType = tinydb.getInt("TempType");
        if (currentTempType == 0) {
            initial_temp_val = 40;
        } else if (currentTempType == 1) {
            initial_temp_val = 104;
        }
        setContentView(R.layout.activity_led_control);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        deltaT = tinydb.getString("deltaTEditText");
        Log.d("deltaT", deltaT);

        autoButton = (Button) findViewById(R.id.btn_auto);
        manualButton = (Button) findViewById(R.id.btn_manual);
        zeroButton = (Button) findViewById(R.id.zeroButton);
//        oneButton = (Button) findViewById(R.id.oneButton);
        twoButton = (Button) findViewById(R.id.twoButton);
        cg1 = findViewById(R.id.gauge1);
        btnInc = findViewById(R.id.btnIncrease);
        btnDec = findViewById(R.id.btnDecrease);
        settingsImageView = findViewById(R.id.settingsImageView);

        sendSignalButton = (Button) findViewById(R.id.sendSignalButton);
        setTempTextView = (TextView) findViewById(R.id.tv_currentTemp); //Sending
//        deltaTEditText = (EditText) findViewById(R.id.deltaTEditText);
        bluetoothImageView = findViewById(R.id.bluetoothImageview);
        onOffSwitch = (Switch) findViewById(R.id.onOffSwitch);
        tempTextView = findViewById(R.id.tv_ac_temp);//Receiving

        new ConnectBT().execute();


        cg1.setPointSize(0);
        cg1.setSweepAngle(270);

        cg1.setPointStartColor(Color.parseColor("#00FF2B"));
        cg1.setPointEndColor(Color.parseColor("#FF0000"));


        if (tinydb.getInt("TempType") == 0) {
            roomTemp = Integer.parseInt(tempTextView.getText().toString().substring(0, tempTextView.getText().toString().length() - 2));
//            tempTextView.setText(roomTemp + "°C");
            cg1.setPointSize((int) (roomTemp * 3.857));
            cg1.setVisibility(View.INVISIBLE);
            cg1.setVisibility(View.VISIBLE);
        } else {
//            tempTextView.setText(roomTemp + "°F");
            roomTemp = Integer.parseInt(tempTextView.getText().toString().substring(0, tempTextView.getText().toString().length() - 2));
//            roomTemp=Integer.parseInt(tempTextView.getText().toString());
            cg1.setPointSize((int) ((roomTemp - 32) * 2.1428));
            cg1.setVisibility(View.INVISIBLE);
            cg1.setVisibility(View.VISIBLE);

        }
        if (currentTempType == 0) {// Celcius


//            cg1.setPointSize((int)(initial_temp_val * 3.857));

            setTempTextView.setText(initial_temp_val + "°C");

//            cg1.setVisibility(View.INVISIBLE);
//            cg1.setVisibility(View.VISIBLE);


            btnInc.setClickable(true);
            btnDec.setClickable(true);

            bluetoothImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(ledControl.this, DeviceList.class);
                    finish();
                    startActivity(intent);
                    resetConnection();

                }
            });

            btnInc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = setTempTextView.getText().toString().indexOf("°");
                    int c_temp_val = Integer.parseInt(setTempTextView.getText().toString().substring(0, index).trim());
                    c_temp_val++;
                    if (c_temp_val <= 70) {

                        setTempTextView.setText(c_temp_val + "°C");

//                        int x = (int)Math.ceil(cg1.getPointSize() + 3.857);
//                        if(x>270)x=270;
//                        cg1.setPointSize(x);
//                        cg1.setPointStartColor(Color.parseColor("#00FF2B"));
//                        cg1.setPointEndColor(Color.parseColor("#FF0000"));
//                        cg1.setVisibility(View.INVISIBLE);
//                        cg1.setVisibility(View.VISIBLE);
                    }
                }
            });
            btnDec.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = setTempTextView.getText().toString().indexOf("°");
                    int c_temp_val = Integer.parseInt(setTempTextView.getText().toString().substring(0, index).trim());
                    c_temp_val--;

                    if (c_temp_val >= 0) {

                        setTempTextView.setText(c_temp_val + "°C");
                        int x = (int) Math.floor(cg1.getPointSize() - 3.857);
                        if (x < 0) x = 0;

//                        cg1.setPointSize(x);
//                        cg1.setPointStartColor(Color.parseColor("#00FF2B"));
//                        cg1.setPointEndColor(Color.parseColor("#FF0000"));
//
//                        cg1.setVisibility(View.INVISIBLE);
//                        cg1.setVisibility(View.VISIBLE);
                    }
                }
            });
        } else if (currentTempType == 1) {
//            cg1.setPointSize((int)((initial_temp_val - 32) * 2.1428));

//            initial_temp_val = (initial_temp_val * 9 / 5) + 32;
            setTempTextView.setText(initial_temp_val + "°F");


//            cg1.setVisibility(View.INVISIBLE);
//            cg1.setVisibility(View.VISIBLE);


            btnInc.setClickable(true);
            btnDec.setClickable(true);

            btnInc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = setTempTextView.getText().toString().indexOf("°");
                    int c_temp_val = Integer.parseInt(setTempTextView.getText().toString().substring(0, index).trim());
                    c_temp_val++;
                    if (c_temp_val <= 158) {

                        setTempTextView.setText(c_temp_val + "°F");

//                        int x = (int)Math.ceil(cg1.getPointSize() + 2.1428);
//                        if(x>270)x=270;
//                        cg1.setPointSize(x);
//                        cg1.setPointStartColor(Color.parseColor("#00FF2B"));
//                        cg1.setPointEndColor(Color.parseColor("#FF0000"));
//                        cg1.setVisibility(View.INVISIBLE);
//                        cg1.setVisibility(View.VISIBLE);
                    }
                }
            });
            btnDec.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = setTempTextView.getText().toString().indexOf("°");
                    int c_temp_val = Integer.parseInt(setTempTextView.getText().toString().substring(0, index).trim());
                    c_temp_val--;

                    if (c_temp_val >= 32) {

                        setTempTextView.setText(c_temp_val + "°F");
//                        int x = (int)Math.floor(cg1.getPointSize() - 2.1428);
//                        if(x<0)x=0;
//                        cg1.setPointSize(x);
//                        cg1.setPointStartColor(Color.parseColor("#00FF2B"));
//                        cg1.setPointEndColor(Color.parseColor("#FF0000"));
//
//                        cg1.setVisibility(View.INVISIBLE);
//                        cg1.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        //////////////////////////////////////////
        settingsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ledControl.this, SettingsActivity.class);
                startActivity(intent);

            }
        });

        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    switchIsChecked = true;
                    onOffSwitch.setText("On");
                    char b[] = {'<', '1', '-', '-', '-', '-', '-', '>'};
                    sendSignal(b);
                } else {
                    char b[] = {'<', '0', '-', '-', '-', '-', '-', '>'};
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
                if (switchIsChecked) {
                    char b[] = {'<', '1', oppMode, '?', '?', wingDirection, '-', '>'};
                    sendSignal(b);
                    msg("Signal Sent");
                }
            }
        };
        timerObj.schedule(timerTaskObj, 0, 5000);

        autoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchIsChecked) {
                    String tempEditText = setTempTextView.getText().toString();
                    oppMode = '0';
                    manualButton.setEnabled(true);
                    autoButton.setEnabled(false);
                    manualButton.setBackgroundColor(manualButton.getContext().getResources().getColor(R.color.buttonColourDisabled));
                    autoButton.setBackgroundColor(autoButton.getContext().getResources().getColor(R.color.buttonColourEnabled));
//                    deltaTEditText.setVisibility(View.VISIBLE);
                    zeroButton.setVisibility(View.INVISIBLE);
//                    oneButton.setVisibility(View.INVISIBLE);
                    twoButton.setVisibility(View.INVISIBLE);
                    wingDirection = '2';


                } else
                    Toast.makeText(ledControl.this, "Please turn on the device", Toast.LENGTH_SHORT).show();
            }
        });

        manualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchIsChecked) {
                    manualButton.setEnabled(false);
                    autoButton.setEnabled(true);
                    autoButton.setBackgroundColor(autoButton.getContext().getResources().getColor(R.color.buttonColourDisabled));
                    manualButton.setBackgroundColor(manualButton.getContext().getResources().getColor(R.color.buttonColourEnabled));

                    oppMode = '1';

//                    deltaTEditText.setVisibility(View.INVISIBLE);
                    zeroButton.setVisibility(View.VISIBLE);
//                    oneButton.setVisibility(View.VISIBLE);
                    twoButton.setVisibility(View.VISIBLE);


                } else
                    Toast.makeText(ledControl.this, "Please turn on the device", Toast.LENGTH_SHORT).show();

            }
        });

        zeroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchIsChecked) {
//                    oneButton.setBackgroundColor(oneButton.getContext().getResources().getColor(R.color.buttonColourDisabled));
                    twoButton.setBackgroundColor(twoButton.getContext().getResources().getColor(R.color.buttonColourDisabled));
                    zeroButton.setBackgroundColor(zeroButton.getContext().getResources().getColor(R.color.buttonColourEnabled));
                    wingDirection = '0';

                } else
                    Toast.makeText(ledControl.this, "Please turn on the device", Toast.LENGTH_SHORT).show();

            }
        });

//        oneButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick (View v) {
//                if (switchIsChecked)
//                {
//                    zeroButton.setBackgroundColor(zeroButton.getContext().getResources().getColor(R.color.buttonColourDisabled));
//                    twoButton.setBackgroundColor(twoButton.getContext().getResources().getColor(R.color.buttonColourDisabled));
//                    oneButton.setBackgroundColor(oneButton.getContext().getResources().getColor(R.color.buttonColourEnabled));
//                    wingDirection = '1';
//
//                }
//                else
//                    Toast.makeText(ledControl.this, "Please turn on the device", Toast.LENGTH_SHORT).show();
//            }
//        });

        twoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (switchIsChecked) {
                    zeroButton.setBackgroundColor(zeroButton.getContext().getResources().getColor(R.color.buttonColourDisabled));
//                    oneButton.setBackgroundColor(oneButton.getContext().getResources().getColor(R.color.buttonColourDisabled));
                    twoButton.setBackgroundColor(twoButton.getContext().getResources().getColor(R.color.buttonColourEnabled));
                    wingDirection = '2';

                } else
                    Toast.makeText(ledControl.this, "Please turn on the device", Toast.LENGTH_SHORT).show();

            }
        });

        sendSignalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (switchIsChecked) {

                    String setTempText = setTempTextView.getText().toString().substring(0, setTempTextView.getText().toString().indexOf('°'));
                    if (setTempText.length() > 1) {
                        //Converting to centigrade
                        if (tinydb.getInt("TempType") == 1) {
                            msg("Converting to Centigrade");

                            Integer setTempInt = Integer.parseInt(setTempText);
                            setTempInt = (setTempInt - 32) * 5 / 9;
                            setTempText = setTempInt.toString();
                        }

                        char b[] = {'<', '1', oppMode, setTempText.charAt(0), setTempText.charAt(1), wingDirection, '-', '>'};
                        sendSignal(b);
                        msg("Signal Sent");


                        sendSignalButton.setEnabled(false);
                        sendSignalButton.setBackgroundColor(sendSignalButton.getContext().getResources().getColor(R.color.buttonColourDisabled));

                        Timer buttonTimer = new Timer();
                        buttonTimer.schedule(new TimerTask() {

                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        sendSignalButton.setEnabled(true);
                                        sendSignalButton.setBackgroundColor(sendSignalButton.getContext().getResources().getColor(R.color.buttonColourEnabled));
                                    }
                                });
                            }
                        }, 5000);
                    } else {
                        Toast.makeText(ledControl.this, "Please Set A Temperature", Toast.LENGTH_SHORT).show();
                    }
                } else
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
                String mData = "";
                boolean wingOpenFlag = false;
                while (isBtConnected) {

                    if (btSocket.isConnected()) {
                        try {
                            bytes = inputStream.read(buffer);
                            String data = new String(buffer);

                            Log.d("tester3", "" + data);
                            if (data.equals(">")) {
                                final String filterData = mData;
                                Log.d("tester4", mData);
                                if (mData.length() > 6) {
                                    if (mData.charAt(6) == '1') {
                                        //Acknoledgement
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(ledControl.this, "Acknowledgement Received", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        String rtp = Character.toString(mData.charAt(3)) + Character.toString(mData.charAt(4));
                                        try {
                                            initial_temp_val = Integer.parseInt(rtp);

                                            roomTemp = Integer.parseInt(rtp);

                                            if (tinydb.getInt("TempType") == 1)
                                                roomTemp = (roomTemp * 9 / 5) + 32;

                                            Log.d("RTP: ", Integer.toString(roomTemp));
                                            if (oppMode == 0) {

//                                        Log.d("DeltaT out",deltaT );
//                                        if (deltaT.length() == 0)
//                                        {
//                                            Toast.makeText(ledControl.this, "Please set half point in settings", Toast.LENGTH_SHORT).show();
//                                        }

                                                if (setTempTextView.getText().toString().length() > 1) {
                                                    int index = setTempTextView.getText().toString().indexOf("°");
                                                    int setTemp = Integer.parseInt(setTempTextView.getText().toString().substring(0, index).trim());
                                                    Log.d("Set Temp: ", Integer.toString(setTemp));
//                                                int deltaTemp = Integer.parseInt(deltaT);
//                                                Log.d("delta Temp: ", Integer.toString(deltaTemp));

                                                    //*********************For cooler mode*****************

                                                    if (tinydb.getInt("TempMode") == 0) {
                                                        if ((roomTemp - setTemp) > 0) {
                                                            if (!wingOpenFlag) {
                                                                wingOpenFlag = true;
                                                                Log.d("Automatic Status", "Sending 2 for wing direction");

                                                                wingDirection = '2';

                                                                String setTempStr = Integer.toString(setTemp);
                                                                Log.d("SetTempStr", setTempStr);

                                                                char b[] = {'<', '1', oppMode, setTempStr.charAt(0), setTempStr.charAt(1), wingDirection, '-', '>'};
                                                                sendSignal(b);
                                                            }


                                                        } else if ((roomTemp - setTemp) <= 0) {
                                                            if (wingOpenFlag) {
                                                                wingOpenFlag = false;
                                                                Log.d("Automatic Status", "Sending 0 for wing direction");

                                                                wingDirection = '0';

                                                                String setTempStr = Integer.toString(setTemp);
                                                                Log.d("SetTempStr", setTempStr);
                                                                char b[] = {'<', '1', oppMode, setTempStr.charAt(0), setTempStr.charAt(1), wingDirection, '-', '>'};
                                                                sendSignal(b);
                                                            }
                                                        }
                                                    } else {
                                                        //*********************For heater mode*****************
                                                        //TODO: Add the UI for Mode of Operation in Settings and Save in Tiny dp. We'll check the mode from tiny dp and then perform opp.
                                                        if (setTemp > roomTemp) {

                                                            if ((setTemp - roomTemp) > 0) {
                                                                if (!wingOpenFlag) {
                                                                    wingOpenFlag = true;
                                                                    Log.d("Automatic Status", "Sending 2 for wing direction");

                                                                    wingDirection = '2';

                                                                    String setTempStr = Integer.toString(setTemp);
                                                                    Log.d("SetTempStr", setTempStr);

                                                                    char b[] = {'<', '1', oppMode, setTempStr.charAt(0), setTempStr.charAt(1), wingDirection, '-', '>'};
                                                                    sendSignal(b);
                                                                }

                                                            } else if ((setTemp - roomTemp) <= 0) {
                                                                if (wingOpenFlag) {
                                                                    wingOpenFlag = false;
                                                                    Log.d("Automatic Status", "Sending 0 for wing direction");

                                                                    wingDirection = '0';

                                                                    String setTempStr = Integer.toString(setTemp);
                                                                    Log.d("SetTempStr", setTempStr);
                                                                    char b[] = {'<', '1', oppMode, setTempStr.charAt(0), setTempStr.charAt(1), wingDirection, '-', '>'};
                                                                    sendSignal(b);
                                                                }
                                                            }

                                                        } else {
                                                            Toast.makeText(ledControl.this, "Please increase Set Temperature", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }


//

                                                }
                                            }
                                        } catch (Exception e) {
                                            Log.e("Unable to convert", e.getMessage());
                                        }

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (tinydb.getInt("TempType") == 0) {
                                                    tempTextView.setText(roomTemp + "°C");
                                                    cg1.setPointSize((int) (roomTemp * 3.857));
                                                    cg1.setVisibility(View.INVISIBLE);
                                                    cg1.setVisibility(View.VISIBLE);
                                                } else {
                                                    tempTextView.setText(roomTemp + "°F");
                                                    cg1.setPointSize((int) ((roomTemp - 32) * 2.1428));
                                                    cg1.setVisibility(View.INVISIBLE);
                                                    cg1.setVisibility(View.VISIBLE);

                                                }
                                            }
                                        });
                                    }
                                } else {
                                    Log.d("CustomErrorzz", "run: ganda format");
                                }
                                mData = "";


                            } else {
                                mData += data;
                            }


                        } catch (IOException e) {
                            e.printStackTrace();

                            Intent intent = new Intent(ledControl.this, DeviceList.class);
                            finish();
                            Toast.makeText(ledControl.this, "Bluetooth Disconnected", Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                            resetConnection();

                            break;


                        }

                    }

                }

            }
        }).start();
    }

    private void resetConnection() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Exception e) {
            }
            inputStream = null;
        }

        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (Exception e) {
            }
            outputStream = null;
        }

        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (Exception e) {
            }
            btSocket = null;
        }

    }


    // Function to convert an Input Stream to String in Java
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getString(InputStream in) throws IOException {
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

    private void sendSignal(String number) {
        if (btSocket != null) {
            try {
                outputStream.write(number.toString().getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void sendSignal(char[] number) {
        if (btSocket != null) {
            try {
                for (int i = 0; i < number.length; i++)
                    outputStream.write(number[i]);
            } catch (IOException e) {
                msg("Error");
            }

        }

    }

    private void Disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                msg("Error");
            }
        }

        finish();
    }

    private void msg(String s) {
        Log.i("Message:", s);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            try {
                progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please Wait!!!");
            } catch (Exception e) {
                Log.e("Exception", e.getMessage());
            }
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                    inputStream = btSocket.getInputStream();
                    outputStream = btSocket.getOutputStream();

                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
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
