package com.example.mhikeandroid.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mhikeandroid.models.Hike;
import com.example.mhikeandroid.repositories.HikeRepository;
import com.example.mhikeandroid.utils.Event;

public class ConfirmViewModel extends ViewModel {

    private final HikeRepository hikeRepository = new HikeRepository();

    private final MutableLiveData<Boolean> saving = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> saveSuccessEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> saveErrorEvent = new MutableLiveData<>();

    public LiveData<Boolean> getSaving() {
        return saving;
    }

    /** Carries the saved hike's id (new doc id on add, same id on edit). On edit the caller
     *  navigates straight to Detail (mirrors MHikeRN's navigation.replace("Detail")); on add
     *  the caller returns to Home since there's no prior Detail screen to go back to. */
    public LiveData<Event<String>> getSaveSuccessEvent() {
        return saveSuccessEvent;
    }

    public LiveData<Event<String>> getSaveErrorEvent() {
        return saveErrorEvent;
    }

    public void save(Hike hike, boolean isEdit) {
        saving.setValue(true);
        if (isEdit) {
            hikeRepository.updateHike(hike)
                    .addOnSuccessListener(unused -> onSaveSuccess(hike.getId()))
                    .addOnFailureListener(this::onSaveFailure);
        } else {
            hikeRepository.addHike(hike)
                    .addOnSuccessListener(docRef -> onSaveSuccess(docRef.getId()))
                    .addOnFailureListener(this::onSaveFailure);
        }
    }

    private void onSaveSuccess(String hikeId) {
        saving.setValue(false);
        saveSuccessEvent.setValue(new Event<>(hikeId));
    }

    private void onSaveFailure(Exception e) {
        saving.setValue(false);
        saveErrorEvent.setValue(new Event<>(e.getMessage()));
    }
}
