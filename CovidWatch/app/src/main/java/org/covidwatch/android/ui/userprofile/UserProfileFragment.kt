package org.covidwatch.android.ui.userprofile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kotlinx.android.synthetic.main.fragment_user_profile.*
import org.covidwatch.android.R
import org.covidwatch.android.adapters.FragmentDataBindingComponent
import org.covidwatch.android.ble.BLEForegroundService
import org.covidwatch.android.databinding.FragmentUserProfileBinding


class UserProfileFragment : Fragment() {

    private val userProfileViewModel: UserProfileViewModel by viewModels()
    private var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    private var currentlyLoggingContactEvents: Boolean = false
    private lateinit var binding: FragmentUserProfileBinding

    @SuppressLint("CommitPrefEdits")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate<FragmentUserProfileBinding>(
                inflater,
                R.layout.fragment_user_profile,
                container,
                false,
                dataBindingComponent
            ).apply {

                viewModel = userProfileViewModel
                lifecycleOwner = this@UserProfileFragment

                fun setCurrentUserSick(sick: Boolean) {
                    userProfileViewModel.isCurrentUserSick.value = sick
                    val application = context?.applicationContext ?: return
                    val sharedPref = application.getSharedPreferences(
                        application.getString(R.string.preference_file_key), Context.MODE_PRIVATE
                    ) ?: return
                    with(sharedPref.edit()) {
                        putBoolean(
                            application.getString(R.string.preference_is_current_user_sick), sick
                        )
                        commit()
                    }
                }

                noButton.setOnClickListener {
                    setCurrentUserSick(false)
                }

                yesButton.setOnClickListener {
                    setCurrentUserSick(true)
                }

                // TODO cleanup
                val toggleText = {
                    button: Button ->
                    button.text = if (currentlyLoggingContactEvents) {
                        getString(R.string.disable_contact_logging)
                    } else {
                        getString(R.string.enable_contact_logging)
                    }
                }

                // load previous state of button and set the text accordingly
                val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
                currentlyLoggingContactEvents = sharedPref!!.getBoolean("toggleState", false)
                toggleText(toggleContactLoggingButton)

                toggleContactLoggingButton.setOnClickListener {
                    toggleContactLogging()
                    toggleText(toggleContactLoggingButton)
                }
            }

        return binding.root
    }

    /**
     * Checks global state of currentlyLoggingContactEvents
     * and toggles the advertised/subscriber accordingly
     *
     * Also persists the state in SharedPreferences
     */
    private fun toggleContactLogging() {
        if (currentlyLoggingContactEvents) {

            currentlyLoggingContactEvents = false
            Intent(
                requireActivity().applicationContext!!,
                BLEForegroundService::class.java
            ).also { intent ->
                requireActivity().applicationContext!!.stopService(intent)
            }
        } else {
            currentlyLoggingContactEvents = true
            Intent(
                requireActivity().applicationContext!!,
                BLEForegroundService::class.java
            ).also { intent ->
                requireActivity().applicationContext!!.startService(intent)
            }
        }

        // persist this state
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putBoolean("toggleState", currentlyLoggingContactEvents)
            commit()
        }
    }
}
