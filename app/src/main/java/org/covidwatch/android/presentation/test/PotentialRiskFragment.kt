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
import org.covidwatch.android.databinding.FragmentPotentialRiskBinding
import org.covidwatch.android.databinding.FragmentTestQuestionsBinding
import java.util.*

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

        testQuestionsViewModel.isTested.observe(viewLifecycleOwner, Observer {
            updateUi(it)
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
    }
}