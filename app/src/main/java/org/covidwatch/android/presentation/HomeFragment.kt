package org.covidwatch.android.presentation

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.covidwatch.android.R

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val testedButton: Button = view.findViewById(R.id.tested_button)
        testedButton.setOnClickListener {
            findNavController().navigate(R.id.testQuestionsFragment)
        }
        val menuButton: ImageView = view.findViewById(R.id.menu_button)
        menuButton.setOnClickListener {
            findNavController().navigate(R.id.menuFragment)
        }
    }
}