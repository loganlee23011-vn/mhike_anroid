package com.example.mhikeandroid.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mhikeandroid.models.Hike;
import com.example.mhikeandroid.repositories.AuthRepository;
import com.example.mhikeandroid.repositories.HikeRepository;
import com.example.mhikeandroid.utils.Event;

public class DetailViewModel extends ViewModel {

    private final HikeRepository hikeRepository = new HikeRepository();
    private final AuthRepository authRepository = new AuthRepository();

    private final MutableLiveData<Hike> hike = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> deleteSuccessEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> deleteErrorEvent = new MutableLiveData<>();

    public LiveData<Hike> getHike() {
        return hike;
    }

    /** Mirrors DetailScreen.js's isOwner check -- gates the Edit/Delete buttons. */
    public boolean isOwner(Hike hike) {
        String currentUserId = authRepository.getCurrentUser() != null
                ? authRepository.getCurrentUser().getUid() : null;
        return hike != null && hike.getUserId() != null && hike.getUserId().equals(currentUserId);
    }

    public LiveData<Event<Boolean>> getDeleteSuccessEvent() {
        return deleteSuccessEvent;
    }

    public LiveData<Event<String>> getDeleteErrorEvent() {
        return deleteErrorEvent;
    }

    public void loadHike(String hikeId) {
        hikeRepository.getHike(hikeId).addOnSuccessListener(hike::setValue);
    }

    public void deleteHike(String hikeId) {
        hikeRepository.deleteHike(hikeId)
                .addOnSuccessListener(unused -> deleteSuccessEvent.setValue(new Event<>(true)))
                .addOnFailureListener(e -> deleteErrorEvent.setValue(new Event<>(e.getMessage())));
    }
}
