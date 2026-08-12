package com.example.mhikeandroid.repositories;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.example.mhikeandroid.models.FirestoreEntity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * LiveData backed by a Firestore snapshot listener. The listener is attached in onActive()
 * and detached in onInactive(), so it automatically stops (no leaked listener, per CLAUDE.md's
 * battery NFR) whenever the last observer goes away — e.g. when a Fragment's view is
 * destroyed and it was observing via getViewLifecycleOwner().
 *
 * Sorting is done client-side via the optional comparator instead of Query.orderBy(), so
 * screens can combine an equality filter with a sort on a different field without needing a
 * Firestore composite index (see CLAUDE.md's note on observations).
 */
class FirestoreQueryLiveData<T extends FirestoreEntity> extends LiveData<List<T>> {

    private final Query query;
    private final Class<T> modelClass;
    private final Comparator<T> comparator;
    private ListenerRegistration registration;

    FirestoreQueryLiveData(@NonNull Query query, @NonNull Class<T> modelClass,
                            @Nullable Comparator<T> comparator) {
        this.query = query;
        this.modelClass = modelClass;
        this.comparator = comparator;
    }

    @Override
    protected void onActive() {
        registration = query.addSnapshotListener((snapshot, error) -> {
            if (error != null || snapshot == null) {
                return;
            }
            List<T> items = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                T item = doc.toObject(modelClass);
                if (item != null) {
                    item.setId(doc.getId());
                    items.add(item);
                }
            }
            if (comparator != null) {
                Collections.sort(items, comparator);
            }
            setValue(items);
        });
    }

    @Override
    protected void onInactive() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }
}
