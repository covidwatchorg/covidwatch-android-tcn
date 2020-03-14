package com.riskre.covidwatch;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ContactEventsAdapter extends RecyclerView.Adapter<ContactEventsAdapter.ViewHolder> {

    private static final String TAG = "ContactEventsAdapter";
    private ArrayList<String> contact_logs;
    private Context mContext;

    public ContactEventsAdapter(ArrayList<String> contact_logs, Context mContext) {
        this.contact_logs = contact_logs;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder: called");
        holder.data.setText(contact_logs.get(position));
    }

    @Override
    public int getItemCount() {
        return contact_logs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView data;
        RelativeLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            data = itemView.findViewById(R.id.data);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }
}