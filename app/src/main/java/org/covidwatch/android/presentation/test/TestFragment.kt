package org.covidwatch.android.presentation.test

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.covidwatch.android.R
import org.covidwatch.android.databinding.FragmentTestBinding
import java.util.*

class TestFragment : Fragment(R.layout.fragment_test) {

    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!

    private val testViewModel: TestViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        testViewModel.testDate.observe(viewLifecycleOwner, Observer {
            binding.dateButton.text = it?.formattedDate
        })
        testViewModel.isTested.observe(viewLifecycleOwner, Observer {
            toggleButtonVisibility(it)
        })
        testViewModel.isReportButtonVisible.observe(viewLifecycleOwner, Observer {
            toggleReportButton(it)
        })

        initClickListeners()
    }

    private fun initClickListeners() {
        binding.closeButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.continueButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.dateButton.setOnClickListener {
            showDatePicker()
        }
        binding.positiveButton.setOnClickListener {
            testViewModel.onRadioButtonClicked(true)
        }
        binding.negativeButton.setOnClickListener {
            testViewModel.onRadioButtonClicked(false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun toggleButtonVisibility(isTested: Boolean) {
        binding.positiveButtonText.isVisible = !isTested
        binding.continueButton.isVisible = !isTested

        binding.negativeButtonText.isVisible = isTested
        binding.dateButton.isVisible = isTested
    }

    private fun toggleReportButton(isVisible: Boolean) {
        binding.reportButton.isVisible = isVisible
        binding.reportButtonText.isVisible = isVisible
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                testViewModel.onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.datePicker.maxDate = Date().time

        dialog.show()
    }
}