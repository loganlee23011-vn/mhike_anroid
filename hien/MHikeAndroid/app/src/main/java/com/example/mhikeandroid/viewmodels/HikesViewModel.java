package com.example.mhikeandroid.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.mhikeandroid.models.Hike;
import com.example.mhikeandroid.repositories.AuthRepository;
import com.example.mhikeandroid.repositories.HikeRepository;
import com.example.mhikeandroid.utils.Event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Mirrors MHikeRN's HikesScreen.js Upcoming/Completed segmented tabs. The list itself is not
 *  scoped to the current user (see HikeRepository.observeHikes()) — only resetDatabase() is,
 *  since deleting is owner-only. */
public class HikesViewModel extends ViewModel {

    private final HikeRepository hikeRepository = new HikeRepository();
    private final AuthRepository authRepository = new AuthRepository();

    private final LiveData<List<Hike>> hikes = hikeRepository.observeHikes();
    private final MutableLiveData<Boolean> upcomingTab = new MutableLiveData<>(true);
    private final MediatorLiveData<List<Hike>> filteredHikes = new MediatorLiveData<>();

    private final MutableLiveData<Event<Boolean>> resetSuccessEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> resetErrorEvent = new MutableLiveData<>();

    public HikesViewModel() {
        filteredHikes.addSource(hikes, ignored -> updateFilteredHikes());
        filteredHikes.addSource(upcomingTab, ignored -> updateFilteredHikes());
    }

    private void updateFilteredHikes() {
        List<Hike> hikeList = hikes.getValue();
        Boolean upcoming = upcomingTab.getValue();
        if (hikeList == null || upcoming == null) {
            return;
        }
        List<Hike> result = new ArrayList<>();
        for (Hike hike : hikeList) {
            if (isUpcoming(hike) == upcoming) {
                result.add(hike);
            }
        }
        filteredHikes.setValue(result);
    }

    public LiveData<List<Hike>> getFilteredHikes() {
        return filteredHikes;
    }

    public LiveData<Integer> getUpcomingCount() {
        return Transformations.map(hikes, list -> countMatching(list, true));
    }

    public LiveData<Integer> getCompletedCount() {
        return Transformations.map(hikes, list -> countMatching(list, false));
    }

    public void setTab(boolean upcoming) {
        if (upcoming != isUpcomingTab()) {
            upcomingTab.setValue(upcoming);
        }
    }

    public boolean isUpcomingTab() {
        Boolean value = upcomingTab.getValue();
        return value == null || value;
    }

    public LiveData<Event<Boolean>> getResetSuccessEvent() {
        return resetSuccessEvent;
    }

    public LiveData<Event<String>> getResetErrorEvent() {
        return resetErrorEvent;
    }

    public void resetDatabase() {
        hikeRepository.resetDatabase(currentUserId())
                .addOnSuccessListener(unused -> resetSuccessEvent.setValue(new Event<>(true)))
                .addOnFailureListener(e -> resetErrorEvent.setValue(new Event<>(e.getMessage())));
    }

    /** Lets HikesFragment tell HikeAdapter which items are the signed-in user's own, so it can
     *  show a "Shared" tag on everyone else's hikes (mirrors HikesScreen.js's sharedTag). */
    public String getCurrentUserId() {
        return currentUserId();
    }

    private static int countMatching(List<Hike> list, boolean upcoming) {
        int count = 0;
        for (Hike hike : list) {
            if (isUpcoming(hike) == upcoming) {
                count++;
            }
        }
        return count;
    }

    private static boolean isUpcoming(Hike hike) {
        Date now = new Date();
        return hike.getHikeDate() != null && !hike.getHikeDate().before(now);
    }

    private String currentUserId() {
        return authRepository.getCurrentUser() != null
                ? authRepository.getCurrentUser().getUid() : "";
    }
}
