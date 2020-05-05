package org.covidwatch.android.ui.menu

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.covidwatch.android.R

class MenuItemViewHolder(root: View) : RecyclerView.ViewHolder(root) {

    private val title: TextView = root.findViewById(R.id.title)

    fun bind(item: MenuItem) {
        title.setText(item.title)
        title.setCompoundDrawablesWithIntrinsicBounds(0, 0, item.iconEnd, 0)
    }
}