package org.covidwatch.android.presentation.menu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.covidwatch.android.R

class MenuAdapter(
    private val onClick: ((destination: Destination) -> Unit)
) : RecyclerView.Adapter<MenuItemViewHolder>() {

    private val items = listOf(
        MenuItem("Settings", 0, Settings),
        MenuItem("Test Results", 0, TestResults),
        MenuItem("How does this work?", R.drawable.ic_exit_to_app, Browser("https://www.covid-watch.org/")),
        MenuItem("Covid Watch Website", R.drawable.ic_exit_to_app, Browser("https://www.covid-watch.org/")),
        MenuItem("Health Guidelines", R.drawable.ic_exit_to_app, Browser("https://www.covid-watch.org/")),
        MenuItem("Terms of Use", R.drawable.ic_exit_to_app, Browser("https://www.covid-watch.org/")),
        MenuItem("Privacy Policy", R.drawable.ic_exit_to_app, Browser("https://www.covid-watch.org/"))
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        val root = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        return MenuItemViewHolder(root)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        val menuItem = items[position]
        holder.bind(menuItem)

        holder.itemView.setOnClickListener {
            onClick(menuItem.destination)
        }
    }
}