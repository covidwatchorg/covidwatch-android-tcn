package org.covidwatch.android.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.covidwatch.android.R
import org.covidwatch.android.databinding.FragmentSettingsBinding
import org.covidwatch.android.ui.settings.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

private const val LOCATION_PERMISSION = 100
private const val REQUEST_ENABLE_BT = 101

class SettingsFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val settingsViewModel: SettingsViewModel by viewModel()

    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsViewModel.hasLocationPermissionLiveData.observe(viewLifecycleOwner, Observer {
            val checkedIconId = if (it) R.drawable.ic_check_true else R.drawable.ic_info_red
            binding.locationButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkedIconId, 0)
        })
        settingsViewModel.isBluetoothEnabledLiveData.observe(viewLifecycleOwner, Observer {
            val checkedIconId = if (it) R.drawable.ic_check_true else R.drawable.ic_info_red
            binding.bluetoothButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkedIconId, 0)
        })

        binding.closeButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.bluetoothButton.setOnClickListener {
            ensureBluetoothIsOn()
        }
        binding.locationButton.setOnClickListener {
            ensureLocationPermissionIsGranted()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
        settingsViewModel.onLocationPermissionResult()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        settingsViewModel.onLocationPermissionResult()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            settingsViewModel.onBluetoothResult()
        }
    }

    @AfterPermissionGranted(LOCATION_PERMISSION)
    private fun ensureLocationPermissionIsGranted() {
        val perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (EasyPermissions.hasPermissions(requireContext(), *perms)) {
            return
        }
        EasyPermissions.requestPermissions(
            this,
            getString(R.string.bluetooth_explanation_subtext),
            LOCATION_PERMISSION, *perms
        )
    }

    private fun ensureBluetoothIsOn() {
        if (bluetoothAdapter?.isEnabled == true) {
            return
        }
        turnOnBluetooth()
    }

    private fun turnOnBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent,
            REQUEST_ENABLE_BT
        )
    }
}