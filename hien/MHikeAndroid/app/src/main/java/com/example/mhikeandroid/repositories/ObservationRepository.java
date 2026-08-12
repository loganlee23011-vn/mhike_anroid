package com.example.mhikeandroid.repositories;

import androidx.lifecycle.LiveData;

import com.example.mhikeandroid.models.Observation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ObservationRepository {

    private final CollectionReference observationsRef =
            FirebaseFirestore.getInstance().collection("observations");

    public Task<DocumentReference> addObservation(Observation observation) {
        if (observation.getObservedAt() == null) {
            observation.setObservedAt(new Date());
        }
        return observationsRef.add(observation);
    }

    public Task<Void> updateObservation(Observation observation) {
        return observationsRef.document(observation.getId()).set(observation);
    }

    public Task<Void> deleteObservation(String observationId) {
        return observationsRef.document(observationId).delete();
    }

    /** Deletes every observation belonging to a hike — used by HikeRepository's cascade delete. */
    public Task<Void> deleteObservationsForHike(String hikeId) {
        return observationsRef.whereEqualTo("hikeId", hikeId).get()
                .continueWithTask(task -> {
                    QuerySnapshot snapshot = task.getResult();
                    List<Task<Void>> deletes = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        deletes.add(doc.getReference().delete());
                    }
                    return Tasks.whenAll(deletes);
                });
    }

    /**
     * This user's own observations, most recent first — mirrors MHikeRN's
     * subscribeToRecentObservations(userId, count, ...) (observationService.js), which filters
     * by userId and sorts client-side rather than serving a global cross-user feed. Single-field
     * where(), sorted client-side — same reasoning as observeObservations below: avoids a
     * userId+observedAt composite index.
     */
    public LiveData<List<Observation>> observeObservationsByUser(String userId) {
        return new FirestoreQueryLiveData<>(
                observationsRef.whereEqualTo("userId", userId),
                Observation.class,
                (o1, o2) -> {
                    Date d1 = o1.getObservedAt();
                    Date d2 = o2.getObservedAt();
                    if (d1 == null && d2 == null) return 0;
                    if (d1 == null) return -1;
                    if (d2 == null) return 1;
                    return d2.compareTo(d1);
                });
    }

    /** Sorted client-side by observedAt descending to avoid a hikeId+observedAt composite index. */
    public LiveData<List<Observation>> observeObservations(String hikeId) {
        return new FirestoreQueryLiveData<>(
                observationsRef.whereEqualTo("hikeId", hikeId),
                Observation.class,
                (o1, o2) -> {
                    Date d1 = o1.getObservedAt();
                    Date d2 = o2.getObservedAt();
                    if (d1 == null && d2 == null) return 0;
                    if (d1 == null) return -1;
                    if (d2 == null) return 1;
                    return d2.compareTo(d1);
                });
    }
}
