package org.covidwatch.android.ui

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.covidwatch.android.R
import org.covidwatch.android.domain.UserFlowRepository
import org.koin.android.ext.android.inject
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

private const val LOCATION_PERMISSION = 100

class SetupBluetoothFragment : Fragment(R.layout.fragment_setup_bluetooth),
    EasyPermissions.PermissionCallbacks {

    private val userFlowRepository: UserFlowRepository by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val grantLocationAccessButton: Button = view.findViewById(R.id.grant_location_access_button)
        grantLocationAccessButton.setOnClickListener {
            grantLocationPermission()
        }
    }

    @AfterPermissionGranted(LOCATION_PERMISSION)
    private fun grantLocationPermission() {
        val perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (EasyPermissions.hasPermissions(requireContext(), *perms)) {
            permissionGranted()
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.bluetooth_explanation_subtext),
                LOCATION_PERMISSION, *perms
            )
        }
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
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        permissionGranted()
    }

    private fun permissionGranted() {
        userFlowRepository.updateSetupUserFlow()
        findNavController().popBackStack(R.id.homeFragment, false)
    }
}