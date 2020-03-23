package org.covidwatch.android.ui.contactevents

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import kotlinx.android.synthetic.main.fragment_contact_events.view.*
import org.covidwatch.android.R
import org.covidwatch.android.adapters.FragmentDataBindingComponent
import org.covidwatch.android.data.ContactEventDAO
import org.covidwatch.android.data.CovidWatchDatabase
import org.covidwatch.android.databinding.FragmentContactEventsBinding


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
        val viewModel: ContactEventsViewModel by viewModels(factoryProducer = {
            ContactEventsViewModelFactory(database.contactEventDAO(), context.applicationContext as Application)
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

        val adapter = ContactEventsAdapter()
        binding.contactEventsRecyclerview.adapter = adapter
        binding.contactEventsRecyclerview.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )
        viewModel.contactEvents.observe(viewLifecycleOwner, Observer { adapter.submitList(it) })

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val isContactEventLoggingEnabled =
            contactEventsViewModel.isContactEventLoggingEnabled.value ?: false
        if (isContactEventLoggingEnabled) {
            inflater.inflate(R.menu.menu_contact_events_stop, menu)
        } else {
            inflater.inflate(R.menu.menu_contact_events_start, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.clear -> {
                CovidWatchDatabase.databaseWriteExecutor.execute {
                    val dao: ContactEventDAO =
                        CovidWatchDatabase.getInstance(requireActivity()).contactEventDAO()
                    dao.deleteAll()
                }
            }
            R.id.start_logging -> {
                setContactEventLogging(true)
                activity?.invalidateOptionsMenu()
            }
            R.id.stop_logging -> {
                setContactEventLogging(false)
                activity?.invalidateOptionsMenu()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setContactEventLogging(enabled: Boolean) {

        contactEventsViewModel.isContactEventLoggingEnabled.value = enabled

        val application = context?.applicationContext ?: return
        val sharedPref = application.getSharedPreferences(
            application.getString(R.string.preference_file_key), Context.MODE_PRIVATE
        ) ?: return
        with(sharedPref.edit()) {
            putBoolean(
                application.getString(R.string.preference_is_contact_event_logging_enabled),
                enabled
            )
            commit()
        }
    }

}
