package org.covidwatch.android

import android.app.AlertDialog
import android.content.Context
import androidx.appcompat.view.ContextThemeWrapper
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import org.covidwatch.android.adapters.FragmentDataBindingComponent
import org.covidwatch.android.databinding.FragmentSelfReportBinding
import org.covidwatch.android.ui.selfreport.SelfReportViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SelfReportFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SelfReportFragment : Fragment() {

    private val selfReportProfileViewModel: SelfReportViewModel by viewModels()
    private var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    private lateinit var binding: FragmentSelfReportBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val context = context ?: return null

        binding =
            DataBindingUtil.inflate<FragmentSelfReportBinding>(
                inflater,
                R.layout.fragment_self_report,
                container,
                false,
                dataBindingComponent
            ).apply {

                viewModel = selfReportProfileViewModel
                lifecycleOwner = this@SelfReportFragment

                fun setCurrentUserSick(sick: Boolean) {
                    selfReportProfileViewModel.isCurrentUserSick.value = sick
                    val application = context.applicationContext ?: return
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

                report.setOnClickListener {
                    val confirmBuilder: androidx.appcompat.app.AlertDialog.Builder = androidx.appcompat.app.AlertDialog.Builder(
                        ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom)
                    );
                    // customize style of confirmation alert
                    confirmBuilder.setTitle("Correct Information?");
                    confirmBuilder.setMessage("I attest that the information I've submitted is true to the best of my knowledge");

                    confirmBuilder.setPositiveButton(
                        "CONFIRM"
                    ) { dialog, which ->
                        setCurrentUserSick(true)
                        Toast.makeText(
                            getActivity(),
                            "Your report was submitted.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    confirmBuilder.setNegativeButton(
                        "CANCEL"
                    ) { dialog, which ->
                        Toast.makeText(
                            getActivity(),
                            "Pressed Cancel",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    confirmBuilder.show();
                }

            }

        return binding.root
    }



//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment SelfReportFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            SelfReportFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }





}
