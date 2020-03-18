package com.riskre.covidwatch.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.riskre.covidwatch.R;
import com.riskre.covidwatch.data.ContactEvent;

import java.util.List;

public class ContactEventsAdapter extends RecyclerView.Adapter<ContactEventsAdapter.ContactEventsHolder> {

    private final LayoutInflater mInflater;
    private List<ContactEvent> contactEvents; // Cached copy of ContactEvents

    class ContactEventsHolder extends RecyclerView.ViewHolder {
        private final TextView ContactEventItemView;

        private ContactEventsHolder(View itemView) {
            super(itemView);
            ContactEventItemView = itemView.findViewById(R.id.data);
        }
    }

    public ContactEventsAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ContactEventsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.layout_listitem, parent, false);
        return new ContactEventsHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ContactEventsHolder holder, int position) {
        if (contactEvents != null) {
            ContactEvent current = contactEvents.get(position);
            holder.ContactEventItemView.setText(
                    "Strongest RSSI: " + current.getSignalStrength() +
                            "\nCEN: " + current.getIdentifier() +
                            "\nAEN: " + current.getAdvertisingCEN() +
                            "\nClosest @  " + current.getTimestamp());

        } else {
            // Covers the case of data not being ready yet.
            holder.ContactEventItemView.setText("No ContactEvent");
        }
    }


    // getItemCount() is called many times, and when it is first called,
    // contactEvents has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (contactEvents != null)
            return contactEvents.size();
        else return 0;
    }

    /**
     * @param ContactEvents
     */
    public void setContactEvents(List<ContactEvent> ContactEvents) {
        contactEvents = ContactEvents;
        notifyDataSetChanged();
    }
}