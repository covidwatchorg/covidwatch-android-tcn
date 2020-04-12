package org.covidwatch.android

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import org.covidwatch.android.databinding.FragmentLocationPermissionBinding
import org.covidwatch.android.ui.MainActivity

/**
 * A simple [Fragment] subclass.
 * Use the [LocationPermissionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LocationPermissionFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentLocationPermissionBinding>(inflater,
            R.layout.fragment_location_permission,container,false)
        binding.getStarted2.setOnClickListener { view ->
            (getActivity() as MainActivity).initLocationManager {
                view.findNavController().navigate(R.id.action_onboardingFragment_to_mainFragment)
            }
        }
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            LocationPermissionFragment().apply {
                arguments = Bundle().apply {}//Keeping in case we need arguments passed at a later date
            }
    }
}
