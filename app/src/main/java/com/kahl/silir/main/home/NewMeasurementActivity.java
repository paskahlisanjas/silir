package com.kahl.silir.main.home;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.kahl.silir.R;
import com.kahl.silir.entity.MeasurementProfile;
import com.kahl.silir.main.MainActivity;
import com.kahl.silir.main.history.MeasurementResultActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NewMeasurementActivity extends AppCompatActivity {
    private final Activity activity = this;
    private final int BLUETOOTH_ACTIVATION_REQUEST = 1204;
    private final int MESSAGE_WHAT = 1;
    private final String targetAddress = "98:D3:32:30:A8:4E";
    public static final String PROFILE_EXTRA = "profileExtra";
    public static final String KEY_EXTRA = "keyExtra";
    public static final String RESULT_EXTRA = "resultExtra";
    public static final String FROM_HERE = "fromNewMEasurementActivity";

    private boolean isDeviceBonded = false;
    private boolean isReceiverRegistered = false;
    private boolean isConnectionBuilt = false;
    private boolean isWaitingForBonding = false;
    private MeasurementProfile profile;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    private Dialog btActivationDialog;
    private Dialog noDeviceFoundDialog;
    private ProgressDialog progressDialog;
    private LineChart lineChart;
    private TextView displayData;
    private float xValue = 0;
    private float idleValue = 4.6f;

    private ArrayList<Float> result = new ArrayList<>();
    private List<Entry> entryList = new ArrayList<>();
    private LineDataSet lineDataSet = new LineDataSet(entryList, null);
    private LineData lineData = new LineData(/*lineDataSet*/);
    private boolean measured = false;
    private String key;

    private BroadcastReceiver btDiscoveringReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            filterAction(intent.getAction(), intent);
        }

        private void filterAction(String action, Intent intent) {
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getAddress().equals(targetAddress)) {
                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        isDeviceBonded = true;
                        bluetoothDevice = device;
                        Toast.makeText(activity, "Device's already Bonded. Preparing...", Toast.LENGTH_SHORT).show();
                    } else {
                        isWaitingForBonding = true;
                        Toast.makeText(activity, "Device's found. Please input the passkey : 1234", Toast.LENGTH_LONG).show();
                        bluetoothAdapter.cancelDiscovery();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            bluetoothDevice = device;
                            bluetoothDevice.createBond();
                        } else {
                            Toast.makeText(activity, "Can't create a bond with device, please pair your " +
                                    "smartphone with the device via bluetooth", Toast.LENGTH_LONG).show();
                            onBackPressed();
                        }
                    }
                }
            }
        }
    };
    private BroadcastReceiver bondStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            filterAction(intent.getAction(), intent);
        }

        private void filterAction(String action, Intent intent) {
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    isDeviceBonded = true;
                    isWaitingForBonding = false;
                    Toast.makeText(activity, "Device's bonded (" + device.getAddress() +
                            "), preparing...", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    noDeviceFoundDialog.dismiss();
                } else if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    isWaitingForBonding = false;
                }
            }
        }
    };
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            byte[] writeBuf = (byte[]) msg.obj;
            int begin = (int) msg.arg1;
            int end = (int) msg.arg2;
            if (msg.what == MESSAGE_WHAT) {
                String writeMessage = new String(writeBuf);
                writeMessage = writeMessage.substring(begin, end);
                displayData.setText(writeMessage);
//                Log.d("SILIR", "received message: " + writeMessage);
                float yValue;
                try {
                    yValue = Float.parseFloat(writeMessage);
                } catch (NumberFormatException exp) {
                    yValue = 4.6f;
                }
                Log.d("SILIR", "yValue : " + yValue);
//                if (xValue == 100) idleValue = yValue;
//                if (entryList.size() >= 100) lineDataSet.removeFirst();
                /*if (xValue > 100 && yValue >= idleValue) {
                    measured = true;
                    result.add(yValue);
                    Log.d("SILIR", "result[" + xValue + "] = " + writeMessage);
                } else if (measured) {
                    if (result.size() >= 300) {
                        Log.d("SILIR", "cekkkkkkkkkkkkkkkkkkkkk");
                        connectThread.cancel();
                        Intent intent = new Intent(activity, MeasurementResultActivity.class);
                        intent.putExtra(PROFILE_EXTRA, profile);
                        intent.putExtra(KEY_EXTRA, key);
                        intent.putExtra(RESULT_EXTRA, result);
                        intent.putExtra(FROM_HERE, true);
                        startActivity(intent);
                    } else {
                        measured = false;
                    }
                }*/
                lineDataSet.addEntry(new Entry(xValue, yValue));
                lineData.notifyDataChanged();
                lineChart.notifyDataSetChanged();
                lineChart.invalidate();
                xValue += 1;
            }
        }
    };

    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;
        private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        public ConnectThread(BluetoothDevice bd) {
            device = bd;
            BluetoothSocket bs = null;
            try {
                bs = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.d("SILIR", "NewMeasurementActivity, ConnectThread(): IOException");
            }
            socket = bs;
        }

        @Override
        public void run() {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            try {
                socket.connect();
            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException e1) {
                }
            }
            connectedThread = new ConnectedThread(socket);
            connectedThread.start();
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e1) {
                Log.d("SILIR", "NewMeasurementActivity, ConnectThread().run(): IOException");
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream input;
        private final OutputStream output;

        public ConnectedThread(BluetoothSocket bs) {
            socket = bs;
            InputStream is = null;
            OutputStream os = null;
            try {
                is = socket.getInputStream();
                os = socket.getOutputStream();
            } catch (IOException e) {
            }
            input = is;
            output = os;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[256];
            int bytes = 0;
            int begin = 0;
            while (true) {
                try {
                    bytes += input.read(buffer, bytes, buffer.length - bytes);
                    for (int i = begin; i < bytes; i++) {
                        if (buffer[i] == "\n".getBytes()[0]) {
                            handler.obtainMessage(MESSAGE_WHAT, begin, i, buffer).sendToTarget();
                            begin = i + 1;
                            if (i == bytes - 1) {
                                bytes = 0;
                                begin = 0;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                output.write(bytes);
            } catch (IOException e) {
            }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        profile = (MeasurementProfile) intent.getSerializableExtra(ChooseProfileActivity.CHOSEN_PROFILE);
        key = intent.getStringExtra(ChooseProfileActivity.CHOSEN_KEY);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.activity_new_measurement);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        String[] nameSplitted = profile.getName().split(" ");
        String name = nameSplitted[0];
        if (nameSplitted.length >= 2) name += " " + nameSplitted[1];
        toolbar.setTitle(name);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setElevation(0);

        progressDialog = new ProgressDialog(this);

        displayData = (TextView) findViewById(R.id.data_display);
        lineChart = (LineChart) findViewById(R.id.line_chart);

        lineDataSet.setHighlightEnabled(false);
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setLineWidth(2f);
        lineDataSet.setColor(getResources().getColor(R.color.colorPrimary));
        lineChart.getAxisRight().setEnabled(false);
        YAxis left = lineChart.getAxisLeft();
        left.setDrawGridLines(false);
        left.setDrawZeroLine(true);
        left.setAxisMaximum(12f);
        left.setAxisMinimum(-4f);
        left.setAxisLineColor(Color.parseColor("#EFEFEF"));
        left.setTextColor(getResources().getColor(R.color.colorPrimary));
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setEnabled(false);
//        lineChart.setData(lineData); ((ini yang perlu dicari tau buat ditaruh dimana))

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(activity, "Your smartphone doesn\'t not support any bluetooth connection.",
                    Toast.LENGTH_SHORT).show();
            onBackPressed();
        } else if (!bluetoothAdapter.isEnabled()) {
            btActivationDialog = new Dialog(activity);
            btActivationDialog.setCancelable(false);
            btActivationDialog.setContentView(R.layout.signout_warning_dialog);
            btActivationDialog.setTitle("Warning");
            TextView warningTextView = (TextView) btActivationDialog.findViewById(R.id.warning_text);
            warningTextView.setText("Bluetooth is not activated. Please activate the bluetooth " +
                    "to connect your smartphone to the device.");
            Button okButton = (Button) btActivationDialog.findViewById(R.id.ok_button);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                            BLUETOOTH_ACTIVATION_REQUEST);
                }
            });
            Button cancelButton = (Button) btActivationDialog.findViewById(R.id.cancel_button);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btActivationDialog.dismiss();
                    onBackPressed();
                }
            });
            btActivationDialog.show();
        } else {
            checkBondingStatus();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == BLUETOOTH_ACTIVATION_REQUEST) {
                if (bluetoothAdapter.isEnabled()) {
                    btActivationDialog.dismiss();
                    checkBondingStatus();
                }
            }
        }
    }

    private void checkBondingStatus() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        if (Build.VERSION.SDK_INT > 23) {
            int permissionCheck = checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 1204); /*can be any number*/
            }
        }
        progressDialog.setMessage("discovering device...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        bluetoothAdapter.startDiscovery();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int seconds = 0;
                while (seconds < 7 && !isDeviceBonded && !isWaitingForBonding) {
                    try {
                        Thread.sleep(1000);
                        seconds++;
                    } catch (InterruptedException e) {
                        bluetoothAdapter.cancelDiscovery();
                        Log.d("SILIR", "New Measurement: Interrupted Exception");
                        onBackPressed();
                    }
                }
                if (isWaitingForBonding) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.setMessage("Bonding...");
                        }
                    });
                }
                while (isWaitingForBonding) ;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bluetoothAdapter.cancelDiscovery();
                        progressDialog.dismiss();
                        if (!isDeviceBonded) {
                            noDeviceFoundDialog = new Dialog(activity);
                            noDeviceFoundDialog.setTitle("Warning");
                            noDeviceFoundDialog.setCancelable(false);
                            noDeviceFoundDialog.setContentView(R.layout.signout_warning_dialog);
                            TextView warning = (TextView) noDeviceFoundDialog.findViewById(R.id.warning_text);
                            warning.setText("We couldn't find any reliable device. Please make " +
                                    "sure that you have turned the device on.");
                            Button okButton = (Button) noDeviceFoundDialog.findViewById(R.id.ok_button);
                            okButton.setText("TRY AGAIN");
                            okButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    noDeviceFoundDialog.dismiss();
                                    checkBondingStatus();
                                }
                            });
                            Button cancelButton = (Button) noDeviceFoundDialog.findViewById(R.id.cancel_button);
                            cancelButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    noDeviceFoundDialog.dismiss();
                                    Intent intent = new Intent(activity, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }
                            });
                            noDeviceFoundDialog.show();
                        } else {
                            buildConnection();
                        }
                    }
                });
            }
        }).start();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(btDiscoveringReceiver, filter);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(bondStateReceiver, filter2);
        isReceiverRegistered = true;
    }

    private void buildConnection() {
        isConnectionBuilt = true;
        connectThread = new ConnectThread(bluetoothDevice);
        connectThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isConnectionBuilt) {
            connectedThread.cancel();
            connectThread.cancel();
        }
        if (isReceiverRegistered) {
            unregisterReceiver(btDiscoveringReceiver);
            unregisterReceiver(bondStateReceiver);
        }
    }
}
