package com.example.mhikeandroid.repositories;

import androidx.lifecycle.LiveData;

import com.example.mhikeandroid.models.Hike;
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

public class HikeRepository {

    private final CollectionReference hikesRef = FirebaseFirestore.getInstance().collection("hikes");
    private final ObservationRepository observationRepository = new ObservationRepository();

    public Task<DocumentReference> addHike(Hike hike) {
        return hikesRef.add(hike);
    }

    public Task<Void> updateHike(Hike hike) {
        // Null out updatedAt so @ServerTimestamp stamps a fresh value on this write;
        // createdAt is left as whatever the caller loaded so it isn't clobbered.
        hike.setUpdatedAt(null);
        return hikesRef.document(hike.getId()).set(hike);
    }

    public Task<Hike> getHike(String hikeId) {
        return hikesRef.document(hikeId).get().continueWith(task -> {
            DocumentSnapshot doc = task.getResult();
            Hike hike = doc.toObject(Hike.class);
            if (hike != null) {
                hike.setId(doc.getId());
            }
            return hike;
        });
    }

    /** Cascades to the hike's observations first (see CLAUDE.md's hikeService.js reference). */
    public Task<Void> deleteHike(String hikeId) {
        return observationRepository.deleteObservationsForHike(hikeId)
                .continueWithTask(task -> hikesRef.document(hikeId).delete());
    }

    /** Deletes every hike (and its observations) owned by this user — not the whole collection. */
    public Task<Void> resetDatabase(String userId) {
        return hikesRef.whereEqualTo("userId", userId).get()
                .continueWithTask(task -> {
                    QuerySnapshot snapshot = task.getResult();
                    List<Task<Void>> deletes = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        deletes.add(deleteHike(doc.getId()));
                    }
                    return Tasks.whenAll(deletes);
                });
    }

    /** Not filtered by userId — mirrors RN's subscribeToHikes(): every signed-in user can read
     *  every hike (matches firestore.rules's "allow read: if isSignedIn()" and the spec's
     *  "upload them to a server where they can later be shared with others"). Writes/deletes
     *  are still owner-only, enforced by security rules and by scoping resetDatabase(userId).
     *  Sorted client-side by hikeDate ascending, matching RN's orderBy("hikeDate", "asc"). */
    public LiveData<List<Hike>> observeHikes() {
        return new FirestoreQueryLiveData<>(
                hikesRef,
                Hike.class,
                (h1, h2) -> {
                    Date d1 = h1.getHikeDate();
                    Date d2 = h2.getHikeDate();
                    if (d1 == null && d2 == null) return 0;
                    if (d1 == null) return -1;
                    if (d2 == null) return 1;
                    return d1.compareTo(d2);
                });
    }
}
