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
import org.covidwatch.android.databinding.FragmentTestQuestionsBinding
import org.covidwatch.android.ui.util.showDatePicker

class TestQuestionsFragment : Fragment() {

    private var _binding: FragmentTestQuestionsBinding? = null
    private val binding get() = _binding!!

    private val testQuestionsViewModel: TestQuestionsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTestQuestionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        testQuestionsViewModel.testDate.observe(viewLifecycleOwner, Observer {
            val checkedIconId = if (it.isChecked) R.drawable.ic_check_true else 0
            binding.dateButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkedIconId, 0)
            binding.dateButton.text = it?.formattedDate
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
        binding.negativeButton.setOnClickListener {
            testQuestionsViewModel.onRadioButtonClicked(false)
        }
        binding.positiveButton.setOnClickListener {
            testQuestionsViewModel.onRadioButtonClicked(true)
        }
        binding.continueButton.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
        binding.dateButton.setOnClickListener {
            showDatePicker {
                testQuestionsViewModel.onDateSelected(it)
            }
        }
        binding.reportButton.setOnClickListener {
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
        binding.negativeButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, negativeIconId, 0)
        binding.positiveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, positiveIconId, 0)

        binding.negativeButtonText.isVisible = !isTested
        binding.continueButton.isVisible = !isTested
        binding.positiveButtonText.isVisible = isTested
        binding.dateButton.isVisible = isTested
    }

    private fun toggleReportButton(isVisible: Boolean) {
        binding.reportButton.isVisible = isVisible
        binding.reportButtonText.isVisible = isVisible
    }
}
