package org.covidwatch.android.ui.test

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
import org.covidwatch.android.databinding.FragmentPotentialRiskBinding
import org.covidwatch.android.ui.util.showDatePicker

class PotentialRiskFragment : Fragment() {

    private var _binding: FragmentPotentialRiskBinding? = null
    private val binding get() = _binding!!

    private val testQuestionsViewModel: TestQuestionsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPotentialRiskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        testQuestionsViewModel.testDate.observe(viewLifecycleOwner, Observer {
            val checkedIconId = if (it.isChecked) R.drawable.ic_check_true else 0
            binding.riskDateButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkedIconId, 0)
            binding.riskDateButton.text = it?.formattedDate
        })
        testQuestionsViewModel.isTested.observe(viewLifecycleOwner, Observer {
            updateUi(it)
        })
        testQuestionsViewModel.isReportButtonVisible.observe(viewLifecycleOwner, Observer {
            toggleReportButton(it)
        })

        initClickListeners()
    }

    private fun initClickListeners() {
        binding.closeButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.riskNegativeButton.setOnClickListener {
            testQuestionsViewModel.onRadioButtonClicked(false)
        }
        binding.riskPositiveButton.setOnClickListener {
            testQuestionsViewModel.onRadioButtonClicked(true)
        }
        binding.riskContinueButton.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
        binding.riskDateButton.setOnClickListener {
            showDatePicker {
                testQuestionsViewModel.onDateSelected(it)
            }
        }
        binding.riskReportButton.setOnClickListener {
            findNavController().navigate(R.id.testConfirmationFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUi(isTested: Boolean) {
        val negativeIconId = if (isTested) 0 else R.drawable.ic_check_true
        val positiveIconId = if (isTested) R.drawable.ic_check_true else 0
        binding.riskNegativeButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, negativeIconId, 0)
        binding.riskPositiveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, positiveIconId, 0)

        binding.negativeButtonText.isVisible = !isTested
        binding.riskContinueButton.isVisible = !isTested
        binding.positiveButtonText.isVisible = isTested
        binding.riskDateButton.isVisible = isTested
    }

    private fun toggleReportButton(isVisible: Boolean) {
        binding.riskReportButton.isVisible = isVisible
        binding.reportButtonText.isVisible = isVisible
    }
}