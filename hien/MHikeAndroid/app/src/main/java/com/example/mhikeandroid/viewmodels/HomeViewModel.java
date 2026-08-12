package com.example.mhikeandroid.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.mhikeandroid.models.Hike;
import com.example.mhikeandroid.models.Observation;
import com.example.mhikeandroid.repositories.AuthRepository;
import com.example.mhikeandroid.repositories.HikeRepository;
import com.example.mhikeandroid.repositories.ObservationRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mirrors MHikeRN's HomeScreen.js: this is the signed-in user's personal dashboard, so every
 * stat, the upcoming hike (+ strip), and the recent observations feed are scoped to this
 * user's own data (myHikes = hikes.filter(h => h.userId === uid) and
 * subscribeToRecentObservations(uid, ...) on the RN side). The full community list — every
 * signed-in user's hikes — lives on HikesFragment instead, matching RN's "All Hikes" tab.
 * Observation hike-name lookups still use the full (unfiltered) hikes list, since an
 * observation you wrote can reference a hike someone else owns.
 */
public class HomeViewModel extends ViewModel {

    private static final int RECENT_OBSERVATIONS_LIMIT = 5;
    private static final int UPCOMING_STRIP_SIZE = 5;

    private final HikeRepository hikeRepository = new HikeRepository();
    private final ObservationRepository observationRepository = new ObservationRepository();
    private final AuthRepository authRepository = new AuthRepository();

    private final String currentUserId = authRepository.getCurrentUser() != null
            ? authRepository.getCurrentUser().getUid() : null;

    private final LiveData<List<Hike>> hikes = hikeRepository.observeHikes();
    private final LiveData<List<Hike>> myHikes = Transformations.map(hikes, list -> {
        List<Hike> mine = new ArrayList<>();
        for (Hike hike : list) {
            if (currentUserId != null && currentUserId.equals(hike.getUserId())) {
                mine.add(hike);
            }
        }
        return mine;
    });
    private final LiveData<List<Observation>> myObservationsRaw =
            observationRepository.observeObservationsByUser(currentUserId);

    private final LiveData<List<Hike>> upcomingHikes = Transformations.map(myHikes, list -> {
        Date now = new Date();
        List<Hike> upcoming = new ArrayList<>();
        for (Hike hike : list) {
            if (hike.getHikeDate() != null && !hike.getHikeDate().before(now)) {
                upcoming.add(hike);
            }
        }
        Collections.sort(upcoming, Comparator.comparing(Hike::getHikeDate));
        return upcoming;
    });

    private final MediatorLiveData<List<RecentObservation>> recentObservations = new MediatorLiveData<>();

    public HomeViewModel() {
        recentObservations.addSource(hikes, ignored -> combineRecentObservations());
        recentObservations.addSource(myObservationsRaw, ignored -> combineRecentObservations());
    }

    private void combineRecentObservations() {
        List<Hike> hikeList = hikes.getValue();
        List<Observation> observationList = myObservationsRaw.getValue();
        if (hikeList == null || observationList == null) {
            return;
        }
        Map<String, String> nameByHikeId = new HashMap<>();
        for (Hike hike : hikeList) {
            nameByHikeId.put(hike.getId(), hike.getName());
        }
        List<RecentObservation> combined = new ArrayList<>();
        int limit = Math.min(RECENT_OBSERVATIONS_LIMIT, observationList.size());
        for (int i = 0; i < limit; i++) {
            Observation observation = observationList.get(i);
            String hikeName = nameByHikeId.get(observation.getHikeId());
            combined.add(new RecentObservation(observation, hikeName != null ? hikeName : "Hike"));
        }
        recentObservations.setValue(combined);
    }

    public String getGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Good morning!";
        if (hour < 18) return "Good afternoon!";
        return "Good evening!";
    }

    public LiveData<Integer> getHikeCount() {
        return Transformations.map(myHikes, List::size);
    }

    public LiveData<Double> getTotalDistanceKm() {
        return Transformations.map(myHikes, list -> {
            double total = 0;
            for (Hike hike : list) {
                total += hike.getLength();
            }
            return total;
        });
    }

    public LiveData<Integer> getCompletedCount() {
        return Transformations.map(myHikes, list -> {
            Date now = new Date();
            int count = 0;
            for (Hike hike : list) {
                if (hike.getHikeDate() != null && hike.getHikeDate().before(now)) {
                    count++;
                }
            }
            return count;
        });
    }

    public LiveData<Hike> getUpcomingHike() {
        return Transformations.map(upcomingHikes, list -> list.isEmpty() ? null : list.get(0));
    }

    public LiveData<List<Hike>> getUpcomingStrip() {
        return Transformations.map(upcomingHikes, list ->
                list.subList(Math.min(1, list.size()), Math.min(UPCOMING_STRIP_SIZE + 1, list.size())));
    }

    public LiveData<List<RecentObservation>> getRecentObservations() {
        return recentObservations;
    }

    public void logout() {
        authRepository.logout();
    }

    /** An Observation paired with its parent hike's name, resolved once here so the Fragment
     *  doesn't have to join the two lists itself (mirrors HomeScreen.js's hikesById lookup). */
    public static final class RecentObservation {
        public final Observation observation;
        public final String hikeName;

        RecentObservation(Observation observation, String hikeName) {
            this.observation = observation;
            this.hikeName = hikeName;
        }
    }
}
