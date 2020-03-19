package org.covidwatch.android.ui.contactevents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import org.covidwatch.android.R
import org.covidwatch.android.adapters.FragmentDataBindingComponent
import org.covidwatch.android.data.ContactEvent
import org.covidwatch.android.data.ContactEventDAO
import org.covidwatch.android.data.CovidWatchDatabase
import org.covidwatch.android.databinding.FragmentContactEventsBinding
import java.util.*

class ContactEventsFragment : Fragment() {

    private lateinit var contactEventsViewModel: ContactEventsViewModel
    private lateinit var binding: FragmentContactEventsBinding
    private var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = context ?: return null

        val database = CovidWatchDatabase.getInstance(context)
//        CovidWatchDatabase.databaseWriteExecutor.execute {
//            val dao: ContactEventDAO = CovidWatchDatabase.getInstance(context).contactEventDAO()
//            val cen = ContactEvent(UUID.randomUUID().toString())
//            dao.insert(cen)
//        }
        val viewModel: ContactEventsViewModel by viewModels(factoryProducer = {
            ContactEventsViewModelFactory(database.contactEventDAO())
        })
        contactEventsViewModel = viewModel

        binding =
            DataBindingUtil.inflate<FragmentContactEventsBinding>(
                inflater,
                R.layout.fragment_contact_events,
                container,
                false,
                dataBindingComponent
            ).apply {
                lifecycleOwner = this@ContactEventsFragment
            }

        val adapter =
            ContactEventsAdapter()
        binding.contactEventsRecyclerview.adapter = adapter
        viewModel.contactEvents.observe(viewLifecycleOwner, Observer { adapter.submitList(it) })

        return binding.root
    }
}
