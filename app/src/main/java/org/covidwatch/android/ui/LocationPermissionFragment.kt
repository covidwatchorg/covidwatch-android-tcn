package org.covidwatch.android

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import org.covidwatch.android.databinding.FragmentOnboardingBinding
import org.covidwatch.android.ui.MainActivity

/**
 * A simple [Fragment] subclass.
 * Use the [OnboardingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LocationPermissionFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (getActivity() as MainActivity).initLocationManager()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentOnboardingBinding>(inflater,
            R.layout.fragment_location_permission,container,false)
        binding.takeAction.setOnClickListener { view : View ->
            view.findNavController().navigate(R.id.action_onboardingFragment_to_mainFragment)
        }
        return binding.root
    }
    companion object {
        @JvmStatic
        fun newInstance() =
            LocationPermissionFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}
