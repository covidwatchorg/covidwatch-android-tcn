package com.riskre.covidwatch.firestore;
//  Created by Zsombor SZABO on 18/03/2020.

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.riskre.covidwatch.data.ContactEventDAO;
import com.riskre.covidwatch.data.CovidWatchDatabase;

import java.util.ArrayList;
import java.util.List;

public class PublicContactEventsObserver {

    private static final String TAG = "PublicContactEventsObserver";

    Context context;

    ListenerRegistration registration;

    public PublicContactEventsObserver(Context ctx) {
        context = ctx;
        startObservingPublicContactEvents();
    }

    public void startObservingPublicContactEvents() {
        if (registration != null) {
            registration.remove();
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        registration = db.collection("contact_events").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listening for realtime updates of contact events failed", e);
                    return;
                }
                Log.d(TAG, "Listened for realtime updates of " + queryDocumentSnapshots.size() + " contact event(s)");
                List<DocumentChange> addedDocumentChanges = new ArrayList<>();
                List<DocumentChange> removedDocumentChanges = new ArrayList<>();
                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    switch (dc.getType()) {
                        case ADDED:
                            addedDocumentChanges.add(dc);
                            break;
                        case MODIFIED:
                            break;
                        case REMOVED:
                            removedDocumentChanges.add(dc);
                            break;
                    }
                }
                markLocalContactEvents(addedDocumentChanges, true);
                markLocalContactEvents(removedDocumentChanges, false);
            }
        });
    }

    private void markLocalContactEvents(List<DocumentChange> documentChanges, Boolean infectious) {
        if (documentChanges.isEmpty()) {
            return;
        }
        CovidWatchDatabase.databaseWriteExecutor.execute(() -> {
            ContactEventDAO dao = CovidWatchDatabase.getDatabase(context).contactEventDAO();
            ArrayList<String> identifiers = new ArrayList<>();
            for (DocumentChange obj : documentChanges) {
                identifiers.add(obj.getDocument().getId());
            }
            // TODO: Handle "SQLiteException too many SQL variables (Sqlite code 1)" in the case of more than 999 parameters
            dao.update(identifiers, infectious);
        });
    }

}
