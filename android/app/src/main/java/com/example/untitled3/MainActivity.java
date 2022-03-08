package com.example.untitled3;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

@RequiresApi(api = VERSION_CODES.LOLLIPOP)
public class MainActivity extends FlutterActivity {
    private static final String TAG = "12";
    private List resultdevices;
    private ArrayList<String> listBlu = new ArrayList<String>();
    private final BluetoothAdapter BA = BluetoothAdapter.getDefaultAdapter();
    private final BluetoothLeScanner bluetoothLeScanner = BA.getBluetoothLeScanner();
    private boolean scanning;
    private final Handler handler = new Handler();
    private static final String CHANNEL = "untitled3/simple";

    private static final long SCAN_PERIOD = 8000;

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            if (call.method.equals("getBatteryLevel")) {
                                int batteryLevel = getBatteryLevel();
                                if (batteryLevel != -1) {
                                    result.success(batteryLevel);
                                } else {
                                    result.error("UNAVAILABLE", "Battery level not available.", null);
                                }
                            } else if (call.method.equals("bluetooth")) {
                                String results = turnOnBluetooth();
                            } else if (call.method.equals("off")) {
                                turnOffBluetooth();
                            } else if (call.method.equals("scan")) {
                                resultdevices = returnDeviceList();
                                result.success(listBlu);
                            } else {
                                result.notImplemented();
                            }
                        }
                );
    }

    private ArrayList<String> returnDeviceList() {
        String[] permissions = new String[2];
        if (VERSION.SDK_INT >= VERSION_CODES.S) {
            permissions[0] = Manifest.permission.BLUETOOTH_SCAN;
            permissions[1] = Manifest.permission.ACCESS_FINE_LOCATION;
        }
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) !=PackageManager.PERMISSION_GRANTED) {


                        ActivityCompat.requestPermissions(MainActivity.this,
                                permissions,
                                1002);
                        return;
                    }
                    bluetoothLeScanner.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }

        return listBlu;
    }

    private String turnOnBluetooth() {
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            String[] permissions = new String[1];
            if (VERSION.SDK_INT >= VERSION_CODES.S) {
                permissions[0] = Manifest.permission.BLUETOOTH_CONNECT;
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 1001);
                return "";
            }
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }
        return "hi There";
    }

    private void turnOffBluetooth() {
        if (BA.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            BA.disable();
            Toast.makeText(getApplicationContext(), "Turned off", Toast.LENGTH_LONG).show();

        }
    }

    private int getBatteryLevel() {
        int batteryLevel = -1;
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
            batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            Intent intent = new ContextWrapper(getApplicationContext()).
                    registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            batteryLevel = (intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100) /
                    intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        }

        return batteryLevel;
    }

    private final ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    String[] permissions = new String[1];
                    if (VERSION.SDK_INT >= VERSION_CODES.S) {
                        permissions[0] = Manifest.permission.BLUETOOTH_CONNECT;
                    }
                    super.onScanResult(callbackType, result);
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, permissions, 1003);
                        return;
                    }
                    if (result.getDevice().getName() != null) {

                        if (listBlu.contains(result.getDevice())) {
                        } else {

                            listBlu.add(result.getDevice().getName());
                        }
                    }
                    Log.d(TAG, listBlu.toString());
                }
            };

}
