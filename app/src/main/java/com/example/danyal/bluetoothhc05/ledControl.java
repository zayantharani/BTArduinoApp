package com.example.danyal.bluetoothhc05;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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
    TextView setTempTextView, tv_bluetoothName;
    ImageView bluetoothImageView, settingsImageView;
    static char oppMode = '0';
    static char wingDirection = '2';
    static char tempMode = '0';
    int roomTemp;
    boolean wingOpenFlag = false;
    public static String EXTRA_ACTIVITY = "activity_called";


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
    static boolean switchIsChecked = true;

    static int currentTempType = 0;
    static char hotColdMode = '0';

    void onCreateAndResume() {
        if (currentTempType != tinydb.getInt("TempType")) {
            currentTempType = tinydb.getInt("TempType");
            String setTempText = setTempTextView.getText().toString().substring(0, setTempTextView.getText().toString().indexOf('°'));
            if (setTempText.length() > 1) {
                //Converting to centigrade
                if (currentTempType == 0) {
                    msg("Converting to Centigrade");

                    Integer setTempInt = Integer.parseInt(setTempText);
                    setTempInt = (setTempInt - 32) * 5 / 9;
                    setTempText = setTempInt.toString();
                    setTempTextView.setText(setTempText + "°C");

                } else if (currentTempType == 1) {
                    Integer setTempInt = Integer.parseInt(setTempText);
                    setTempInt = (setTempInt) * 9 / 5 + 32;
                    setTempText = setTempInt.toString();
                    setTempTextView.setText(setTempText + "°F");

                }
            }
        }

        if (tempMode != (char) tinydb.getInt("TempMode")) {
            tempMode = (char) tinydb.getInt("TempMode");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        onCreateAndResume();
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
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
        int setTempPoint = tinydb.getInt("setTempPoint");
        if (currentTempType == 0) {
            initial_temp_val = setTempPoint;
        } else if (currentTempType == 1) {
            initial_temp_val = setTempPoint;
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
        tv_bluetoothName = findViewById(R.id.tv_bluetoothName);

        sendSignalButton = (Button) findViewById(R.id.sendSignalButton);
        setTempTextView = (TextView) findViewById(R.id.tv_currentTemp); //Sending
//        deltaTEditText = (EditText) findViewById(R.id.deltaTEditText);
        bluetoothImageView = findViewById(R.id.bluetoothImageview);
        onOffSwitch = (Switch) findViewById(R.id.onOffSwitch);
        tempTextView = findViewById(R.id.tv_ac_temp);//Receiving


//        try {
//            new ConnectBT().execute();
//        } catch (Exception e) {
//
//        }

        if (getIntent().getStringExtra(DeviceList.EXTRA_BT_NAME) != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_bluetoothName.setText(getIntent().getStringExtra(DeviceList.EXTRA_BT_NAME));
                    Toast.makeText(ledControl.this, "Connected to " + getIntent().getStringExtra(DeviceList.EXTRA_BT_NAME), Toast.LENGTH_SHORT).show();
                }
            });
        }


        cg1.setPointSize(0);
        cg1.setSweepAngle(270);

        cg1.setPointStartColor(Color.parseColor("#00FF2B"));
        cg1.setPointEndColor(Color.parseColor("#FF0000"));


        onCreateAndResume();

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

        bluetoothImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ledControl.this, DeviceList.class);
                finish();
                startActivity(intent);
                resetConnection();

            }
        });

        if (tinydb.getInt("TempType") == 0) {// Celcius


            setTempTextView.setText(initial_temp_val + "°C");

            btnInc.setClickable(true);
            btnDec.setClickable(true);
        } else if (tinydb.getInt("TempType") == 1) {
//            cg1.setPointSize((int)((initial_temp_val - 32) * 2.1428));

//            initial_temp_val = (initial_temp_val * 9 / 5) + 32;
            btnInc.setClickable(true);
            btnDec.setClickable(true);
            setTempTextView.setText(initial_temp_val + "°F");
        }


        btnInc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = setTempTextView.getText().toString().indexOf("°");
                int c_temp_val = Integer.parseInt(setTempTextView.getText().toString().substring(0, index).trim());
                c_temp_val++;

                if (tinydb.getInt("TempType") == 0)
                    setTempTextView.setText(c_temp_val + "°C");
                else if (tinydb.getInt("TempType") == 1)
                    setTempTextView.setText(c_temp_val + "°F");


                tinydb.putInt("setTempPoint", c_temp_val);

            }
        });
        btnDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = setTempTextView.getText().toString().indexOf("°");
                int c_temp_val = Integer.parseInt(setTempTextView.getText().toString().substring(0, index).trim());
                c_temp_val--;

                if (tinydb.getInt("TempType") == 0)
                    setTempTextView.setText(c_temp_val + "°C");
                else if (tinydb.getInt("TempType") == 1)
                    setTempTextView.setText(c_temp_val + "°F");

                tinydb.putInt("setTempPoint", c_temp_val);

            }
        });


        //////////////////////////////////////////
        settingsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ledControl.this, SettingsActivity.class);
                intent.putExtra(EXTRA_ACTIVITY, "ledControl");
                startActivity(intent);
            }
        });


        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    switchIsChecked = true;
                    onOffSwitch.setText("On");
                    char b[] = {'<', tempMode, '-', '-', '-', '-', '-', '>'};
                    sendSignal(b);
                } else {
                    char b[] = {'<', tempMode, '-', '-', '-', '-', '-', '>'};
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
                    char b[] = {'<', tempMode, oppMode, '?', '?', wingDirection, '-', '>'};
                    sendSignal(b);
                    msg("Signal Sent");
                }
            }
        };
        timerObj.schedule(timerTaskObj, 0, 1000);

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

                    if (wingOpenFlag) {
                        wingDirection = '2';
                        twoButton.setBackgroundColor(twoButton.getContext().getResources().getColor(R.color.buttonColourEnabled));
                        zeroButton.setBackgroundColor(zeroButton.getContext().getResources().getColor(R.color.buttonColourDisabled));
                    } else {
                        wingDirection = '0';
                        zeroButton.setBackgroundColor(zeroButton.getContext().getResources().getColor(R.color.buttonColourEnabled));
                        twoButton.setBackgroundColor(twoButton.getContext().getResources().getColor(R.color.buttonColourDisabled));
                    }


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
                    wingOpenFlag = false;

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
                    wingOpenFlag = true;

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

                        char b[] = {'<', tempMode, oppMode, setTempText.charAt(0), setTempText.charAt(1), wingDirection, '-', '>'};
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
                while (isBtConnected) {
                    if (btSocket != null) {
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
                                                Log.d("OppMode: ", Character.toString(oppMode));
                                                Log.d("SetTemp: ", setTempTextView.getText().toString().length() + "");
                                                Log.d("TempMode", Character.toString(tempMode));

                                                if (oppMode == '0') {

                                                    if (setTempTextView.getText().toString().length() > 1) {
                                                        int index = setTempTextView.getText().toString().indexOf("°");
                                                        int setTemp = Integer.parseInt(setTempTextView.getText().toString().substring(0, index).trim());
                                                        Log.d("Set Temp: ", Integer.toString(setTemp));
//                                                int deltaTemp = Integer.parseInt(deltaT);
//                                                Log.d("delta Temp: ", Integer.toString(deltaTemp));

                                                        //*********************For cooler mode*****************

                                                        if (tinydb.getInt("TempMode") == 0) {
                                                            Log.d("Temp Mode: ", "Cool");
                                                            if ((roomTemp - setTemp) > 0) {
                                                                Log.d("Status:", "Opening Wing");

                                                                if (!wingOpenFlag) {
                                                                    wingOpenFlag = true;
                                                                    wingDirection = '2';

                                                                    Log.d("Automatic Status", "Sending 2 for wing direction");
                                                                    runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            Toast.makeText(ledControl.this, "Wing opened", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });

                                                                    String setTempStr = Integer.toString(setTemp);
                                                                    Log.d("SetTempStr", setTempStr);

                                                                    char b[] = {'<', tempMode, oppMode, setTempStr.charAt(0), setTempStr.charAt(1), wingDirection, '-', '>'};
                                                                    sendSignal(b);
                                                                }


                                                            } else if ((roomTemp - setTemp) <= 0) {
                                                                Log.d("Status:", "Closing wing");
                                                                if (wingOpenFlag) {
                                                                    wingOpenFlag = false;
                                                                    wingDirection = '0';

                                                                    Log.d("Automatic Status", "Sending 0 for wing direction");
                                                                    runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            Toast.makeText(ledControl.this, "Wing closed", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });


                                                                    String setTempStr = Integer.toString(setTemp);
                                                                    Log.d("SetTempStr", setTempStr);
                                                                    char b[] = {'<', tempMode, oppMode, setTempStr.charAt(0), setTempStr.charAt(1), wingDirection, '-', '>'};
                                                                    sendSignal(b);
                                                                }
                                                            }
                                                        } else {
                                                            Log.d("Temp Mode: ", "Hot");

                                                            //*********************For heater mode*****************


                                                            if ((setTemp - roomTemp) > 0) {
                                                                if (!wingOpenFlag) {
                                                                    wingOpenFlag = true;
                                                                    Log.d("Automatic Status", "Sending 2 for wing direction");

                                                                    wingDirection = '2';
                                                                    runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            Toast.makeText(ledControl.this, "Wing opened", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });

                                                                    String setTempStr = Integer.toString(setTemp);
                                                                    Log.d("SetTempStr", setTempStr);

                                                                    char b[] = {'<', tempMode, oppMode, setTempStr.charAt(0), setTempStr.charAt(1), wingDirection, '-', '>'};
                                                                    sendSignal(b);
                                                                }

                                                            } else if ((setTemp - roomTemp) <= 0) {
                                                                if (wingOpenFlag) {
                                                                    wingOpenFlag = false;
                                                                    Log.d("Automatic Status", "Sending 0 for wing direction");

                                                                    runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            Toast.makeText(ledControl.this, "Wing closed", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                                    wingDirection = '0';

                                                                    String setTempStr = Integer.toString(setTemp);
                                                                    Log.d("SetTempStr", setTempStr);
                                                                    char b[] = {'<', tempMode, oppMode, setTempStr.charAt(0), setTempStr.charAt(1), wingDirection, '-', '>'};
                                                                    sendSignal(b);
                                                                }
                                                            }


                                                        }


//

                                                    } else
                                                        Log.d("Set Temp: ", "Length is: " + setTempTextView.length());

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
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ledControl.this, "Bluetooth Disconnected", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                startActivity(intent);
                                resetConnection();

                                break;


                            }
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
            } catch (Exception e) {
                msg("CRASHEDDDDD");
            }

        }

    }

    private void Disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                msg("Error");
            } catch (Exception e) {
                msg("CRASHED fff");
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
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ledControl.this, "Bluetooth Device not connected", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ledControl.this, DeviceList.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                Toast.makeText(ledControl.this, "Connection Failed. Is it a SPP Bluetooth? Try again.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ledControl.this, DeviceList.class);
                startActivity(intent);
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
