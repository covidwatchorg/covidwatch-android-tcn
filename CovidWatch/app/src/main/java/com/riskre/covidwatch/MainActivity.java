package com.riskre.covidwatch;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
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

import com.riskre.covidwatch.ble.BLEForegroundService;
import com.riskre.covidwatch.data.ContactEvent;
import com.riskre.covidwatch.data.ContactEventDAO;
import com.riskre.covidwatch.data.ContactEventViewModel;
import com.riskre.covidwatch.firestore.PublicContactEventsObserver;
import com.riskre.covidwatch.ui.ContactEventsAdapter;
import com.riskre.covidwatch.data.CovidWatchDatabase;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    // APP
    CovidWatchApplication application;

    // CONSTANTS
    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;

    // CEN
    private ArrayList<String> contact_event_numbers = new ArrayList<String>();
    private ContactEventsAdapter cen_adapter;

    // BLE
    private boolean currently_logging_contact_events = false;
    private BluetoothAdapter bluetoothAdapter;

    // DB
    private ContactEventViewModel cenViewModel;

    // TODO: Separate this into a service
    private PublicContactEventsObserver publicContactEventsObserver;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        application = (CovidWatchApplication) this.getApplication();
        setContentView(R.layout.activity_main);

        // Initialization
        initRecyclerView();
        initBluetoothAdapter();
        initLocationManager();
        initPublicContactEventsObserver();
    }

    private void initPublicContactEventsObserver() {
        publicContactEventsObserver = new PublicContactEventsObserver(application.getApplicationContext());
    }

    /**
     * Initializes the BluetoothAdapter. Manifest file is already setup to allow bluetooth access.
     * The user will be asked to enable bluetooth if it is turned off
     */
    private void initBluetoothAdapter() {

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        currently_logging_contact_events =
                this.getIntent().getBooleanExtra("toggle", false);

        Button toggle = (Button) findViewById(R.id.button);
        if (currently_logging_contact_events) {
            toggle.setText("DISABLE CONTACT LOGGING");
        } else {
            toggle.setText("ENABLE CONTACT LOGGING");
        }

    }

    /**
     * Initializes the RecyclerView used to display Contact Events.
     */
    private void initRecyclerView() {

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        cen_adapter = new ContactEventsAdapter(this);
        recyclerView.setAdapter(cen_adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // display database entries by observing the contact events
        cenViewModel = new ViewModelProvider(this).get(ContactEventViewModel.class);
        cenViewModel.getAllEvents().observe(this, new Observer<List<ContactEvent>>() {
            @Override
            public void onChanged(@Nullable final List<ContactEvent> events) {
                // Update the cached copy of the words in the adapter.
                cen_adapter.setContactEvents(events);
            }
        });
    }


    /**
     * Initializes the Location Manager used to obtain coarse bluetooth/wifi location
     * and fine GPS location, logged on a contact event.
     * <p>
     * TODO add GPS initialization here, for now we just ask for location permissions
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initLocationManager() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "The permission to get BLE location data is required", Toast.LENGTH_SHORT).show();
            } else {
                requestPermissions(
                        new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        }, 1);
            }
        } else {
            Toast.makeText(this, "Location permissions already granted", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * OnClick for Contact Logging Button
     *
     * @param view The button that has been clicked
     */
    public void onClickEnableContactLogging(View view) {

        Button toggle = (Button) view;
        Intent serviceIntent = new Intent(this, BLEForegroundService.class);

        if (currently_logging_contact_events) {
            currently_logging_contact_events = false;
            toggle.setText("ENABLE CONTACT LOGGING");
            stopService(serviceIntent);

        } else {
            currently_logging_contact_events = true;
            toggle.setText("DISABLE CONTACT LOGGING");
            ContextCompat.startForegroundService(this, serviceIntent);
        }
    }

    public void clearDB(View view) {
        CovidWatchDatabase.databaseWriteExecutor.execute(() -> {
            // Populate the database in the background.
            // If you want to start with more words, just add them.
            ContactEventDAO dao = CovidWatchDatabase.getDatabase(this).contactEventDAO();
            dao.deleteAll();
        });
    }
}
