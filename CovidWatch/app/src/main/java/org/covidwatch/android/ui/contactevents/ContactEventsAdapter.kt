package org.covidwatch.android.ui.contactevents

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.covidwatch.android.R
import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.databinding.ListItemContactEventBinding


class ContactEventsAdapter() :
    PagedListAdapter<ContactEvent, ContactEventsAdapter.ContactEventViewHolder>(
        DIFF_CALLBACK
    ) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_contact_event
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactEventViewHolder {
        return ContactEventViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                viewType, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ContactEventViewHolder, position: Int) {
        val contactEvent: ContactEvent = getItem(position) ?: return
        holder.bind(contactEvent)
    }

    class ContactEventViewHolder(
        private val binding: ListItemContactEventBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ContactEvent) {
            binding.apply {
                contactEvent = item
                executePendingBindings()
            }
        }

    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<ContactEvent>() {
            // Contact event details may have changed if reloaded from the database,
            // but ID is fixed.
            override fun areItemsTheSame(
                oldConctactEvent: ContactEvent,
                newContactEvent: ContactEvent
            ) = oldConctactEvent.identifier == newContactEvent.identifier

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldContactEvent: ContactEvent,
                newContactEvent: ContactEvent
            ) = oldContactEvent == newContactEvent
        }
    }

}
