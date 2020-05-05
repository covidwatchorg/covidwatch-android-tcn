package org.covidwatch.android.ui.menu

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import org.covidwatch.android.R

class MenuFragment : Fragment(R.layout.fragment_menu) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuList: RecyclerView = view.findViewById(R.id.menu_list)
        menuList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        menuList.adapter = MenuAdapter { destination ->
            handleMenuItemClick(destination)
        }

        val closeButton: ImageView = view.findViewById(R.id.close_button)
        closeButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun handleMenuItemClick(destination: Destination) {
        when (destination) {
            is Settings -> findNavController().navigate(R.id.settingsFragment)
            is TestResults -> findNavController().navigate(R.id.testQuestionsFragment)
            is Browser -> openBrowser(destination.url)
        }
    }

    private fun openBrowser(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }
}