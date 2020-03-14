package com.riskre.covidwatch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String>  contact_event_numbers = new ArrayList<String>();
    private RecyclerView.Adapter cen_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        contact_event_numbers.add("TEST");
        contact_event_numbers.add("TEST");
        contact_event_numbers.add("TEST");
        contact_event_numbers.add("TEST");
        contact_event_numbers.add("TEST");

        initRecyclerView();
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        cen_adapter = new ContactEventsAdapter(contact_event_numbers, this);
        recyclerView.setAdapter(cen_adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
