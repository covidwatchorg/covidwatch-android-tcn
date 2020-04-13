package org.covidwatch.android.presentation

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.covidwatch.android.R

class SetupBluetoothFragment : Fragment(R.layout.fragment_setup_bluetooth) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val grantLocationAccessButton: Button = view.findViewById(R.id.grant_location_access_button)
        grantLocationAccessButton.setOnClickListener {
            grantLocationPermission()
        }
    }

    private fun grantLocationPermission() {
        AlertDialog.Builder(requireContext())
            .setTitle("Grant Location Access")
            .setMessage("Apps using Bluetooth® Low Energy (BLE) are required to have location access enabled")
            .setPositiveButton(android.R.string.ok) { dialog, which ->
                // ask user to turn on the bluetooth here
                findNavController().navigate(R.id.homeFragment)
            }
            .show()
    }
}