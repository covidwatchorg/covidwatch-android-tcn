package org.covidwatch.android

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_self_report.*
import org.covidwatch.android.firestore.ContactEventsDownloadWorker

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO #15: COMMENTING OUT BECAUSE USING EMULATOR
        // BRING BACK AFTER MERGING UX FIRST RUN
        // initBluetoothAdapter()
        initLocationManager()
    }

    public override fun onResume() {
        super.onResume()
        refreshPublicContactEvents()
    }

    private fun refreshPublicContactEvents() {
        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val downloadRequest =
            OneTimeWorkRequestBuilder<ContactEventsDownloadWorker>()
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            ContactEventsDownloadWorker.WORKER_NAME,
            ExistingWorkPolicy.REPLACE,
            downloadRequest
        )
    }


    /**
     * Initializes the BluetoothAdapter. Manifest file is already setup to allow bluetooth access.
     * The user will be asked to enable bluetooth if it is turned off
     */
    private fun initBluetoothAdapter() {
        bluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
        }
    }

    /**
     * Initializes the Location Manager used to obtain coarse bluetooth/wifi location
     * and fine GPS location, logged on a contact event.
     *
     * TODO add GPS initialization here, for now we just ask for location permissions
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public fun initLocationManager() {
        val permissionCheck = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                Toast.makeText(
                    this,
                    getString(R.string.ble_location_permission),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), 1
                )
            }
        } else {
            // Toast.makeText(this, "Location permissions already granted", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * TODO: Move this to fragment
     */
    fun showDatePicker(view: View?) {
        val newFragment: DialogFragment = DatePickerFragment()
        newFragment.show(
            supportFragmentManager,
            getString(R.string.datepicker)
        )
    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun processDatePickerResult(year: Int, month: Int, day: Int) {

        val day_string = Integer.toString(day)
        val year_string = Integer.toString(year)

        val cal: Calendar = Calendar.getInstance()
        val month_date = SimpleDateFormat("MMMM")
        cal.set(Calendar.MONTH, month)
        val month_name: String = month_date.format(cal.getTime())

        val dateMessage = "$month_name $day_string, $year_string"

        textView.text = dateMessage;
    }


}
