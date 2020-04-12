package org.covidwatch.android.ui

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.work.*
import kotlinx.android.synthetic.main.fragment_self_report.*
import org.covidwatch.android.DatePickerFragment
import org.covidwatch.android.R
import org.covidwatch.android.ble.BluetoothManagerImpl
import org.covidwatch.android.data.ContactEventDAO
import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.data.CovidWatchDatabase
import org.covidwatch.android.data.firestore.ContactEventsDownloadWorker

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO #15: COMMENTING OUT BECAUSE USING EMULATOR
        // BRING BACK AFTER MERGING UX FIRST RUN
        // REMOVE adding example CEN
        initBluetoothAdapter()
        Log.i("test", "did we make it here?")
        addDummyCEN()

        setContactEventLogging(true)
    }

    public override fun onResume() {
        super.onResume()
        refreshPublicContactEvents()
    }

    public fun addDummyCEN() {
        val cen = BluetoothManagerImpl.DefaultCenGenerator().generate()
        Log.i("test", "how about here?")
        Log.i("CEN BOI", cen.data.toString())
        CovidWatchDatabase.databaseWriteExecutor.execute {
            val dao: ContactEventDAO = CovidWatchDatabase.getInstance(this).contactEventDAO()
            val contactEvent = ContactEvent(cen.data.toString())
            val isCurrentUserSick = this.getSharedPreferences(
                this.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            ).getBoolean(this.getString(R.string.preference_is_current_user_sick), false)
            contactEvent.wasPotentiallyInfectious = isCurrentUserSick
            dao.insert(contactEvent)
        }
    }

    private fun setContactEventLogging(enabled: Boolean) {

        val application = this?.applicationContext ?: return
        val sharedPref = application.getSharedPreferences(
            application.getString(R.string.preference_file_key), Context.MODE_PRIVATE
        ) ?: return
        with(sharedPref.edit()) {
            putBoolean(
                application.getString(org.covidwatch.android.R.string.preference_is_contact_event_logging_enabled),
                enabled
            )
            commit()
        }
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * Initializes the Location Manager used to obtain coarse bluetooth/wifi location
     * and fine GPS location, logged on a contact event.
     *
     * TODO add GPS initialization here, for now we just ask for location permissions
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun initLocationManager(permissionGrantedCallback: ()->Unit) {
        val permissionCheck = ContextCompat.checkSelfPermission(
            this,
            ACCESS_FINE_LOCATION
        )
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)){
                Toast.makeText(
                    this,
                    getString(R.string.ble_location_permission),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val permissions = arrayOf(ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION)
                requestPermissions(permissions, 1)
            }
        } else {
            permissionGrantedCallback()
        }
    }

    /**
     * TODO: Move this to fragment
     */
    fun showDatePicker(view: View?) {
        val newFragment: DialogFragment =
            DatePickerFragment()
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

        textView.text = dateMessage
    }


}
