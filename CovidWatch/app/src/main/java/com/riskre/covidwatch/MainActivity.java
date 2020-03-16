package com.riskre.covidwatch;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    // App
    CovidWatchApplication application;

    // Constants
    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;

    // CEN
    private ArrayList<String> contact_event_numbers = new ArrayList<String>();
    private RecyclerView.Adapter cen_adapter;

    // BLE
    private boolean currently_logging_contact_events = false;
    private BluetoothAdapter bluetoothAdapter;


    // Initializes Bluetooth adapter.
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        application = (CovidWatchApplication) this.getApplication();
        setContentView(R.layout.activity_main);

        /**
         * Initialization
         */
        initRecyclerView();
        initBluetoothAdapter();
        initLocationManager();
    }

    /**
     * Initializes the BluetoothAdapter. Manifest file is already setup to allow bluetooth access.
     * The user will be asked to enable bluetooth if it is turned off
     */
    private void initBluetoothAdapter() {

        application.startGattServer(this);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    /**
     * Initializes the RecyclerView used to display Contact Events.
     */
    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        cen_adapter = new ContactEventsAdapter(contact_event_numbers, this);
        recyclerView.setAdapter(cen_adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    /**
     * Initializes the Location Manager used to obtain coarse bluetooth/wifi location
     * and fine GPS location, logged on a contact event.
     *
     * TODO add GPS initialization here, for now we just ask for location permissions
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initLocationManager() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                Toast.makeText(this, "The permission to get BLE location data is required", Toast.LENGTH_SHORT).show();
            }else{
                requestPermissions(
                        new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        }, 1);
            }
        }else{
            Toast.makeText(this, "Location permissions already granted", Toast.LENGTH_SHORT).show();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onClickEnableContactLogging(View view) {
        Button toggle = (Button) view;

        if (currently_logging_contact_events) {
            currently_logging_contact_events = false;
            toggle.setText("ENABLE CONTACT LOGGING");

            application.getBleAdvertiser().stopAdvertiser();
            application.getBleScanner().stopScanning();


        } else {
            currently_logging_contact_events = true;
            toggle.setText("DISABLE CONTACT LOGGING");

            UUID BLE_SERVICE_UUID = UUID.fromString(getString(R.string.peripheral_service_uuid));
            application.getBleAdvertiser().startAdvertiser(BLE_SERVICE_UUID);
            application.getBleScanner().startScanning();
        }
    }
}
