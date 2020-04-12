package org.covidwatch.android

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import org.covidwatch.android.databinding.FragmentMainBinding

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {
    private var preferences : SharedPreferences? = null
    private val INITIAL_VISIT = "INITIAL_VISIT"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentMainBinding>(inflater,
            R.layout.fragment_main,container,false)
        binding.shareTheAppButton.setOnClickListener {shareApp()}
        binding.selfReportButton.setOnClickListener { view : View ->
            view.findNavController().navigate(R.id.action_mainFragment_to_selfReportFragment)
        }
        preferences = activity?.getPreferences(Context.MODE_PRIVATE) ?: return binding.root
        val initialVisit = preferences?.getBoolean(INITIAL_VISIT,true) ?: true
        if (!initialVisit) {
            binding.mainTitle.text = getString(R.string.welcome_back_title)
            binding.mainText.text = getString(R.string.not_detected_text)
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        with (preferences?.edit() ?: return) {
            putBoolean(INITIAL_VISIT, false)
            commit()
        }
    }

    private fun shareApp() {
        val shareText = getString(R.string.share_intent_text)
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "$shareText https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
        )
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = MainFragment()
    }
}
