package com.example.mhikeandroid.viewmodels;

import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mhikeandroid.models.Hike;
import com.example.mhikeandroid.repositories.HikeRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * FR4, mirroring MHikeRN's SearchScreen.js: all filters are always visible (no collapsed
 * "advanced" panel) and are only applied when the user taps "Apply Filters" — not live as they
 * type. `hasSearched` distinguishes "haven't searched yet" (no empty-state message) from
 * "searched, zero matches" (mirrors RN's `results !== null` check). Filtering itself stays
 * client-side over the user's already-synced hikes, same as before.
 */
public class SearchViewModel extends ViewModel {

    public static final String DIFFICULTY_ALL = "All";

    private final HikeRepository hikeRepository = new HikeRepository();

    private final LiveData<List<Hike>> allHikes = hikeRepository.observeHikes();

    private String nameQuery = "";
    private String difficultyFilter = DIFFICULTY_ALL;
    private String locationFilter = "";
    private Double minLength;
    private Double maxLength;
    private Date dateFrom;
    private Date dateTo;

    private final MutableLiveData<Date> dateFromLiveData = new MutableLiveData<>();
    private final MutableLiveData<Date> dateToLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasSearched = new MutableLiveData<>(false);
    private final MediatorLiveData<List<Hike>> results = new MediatorLiveData<>();

    public SearchViewModel() {
        results.setValue(new ArrayList<>());
        // allHikes is a FirestoreQueryLiveData: its snapshot listener only attaches while it
        // has an active observer. Nothing else observes it directly, so without this source
        // it stays permanently null and applyFilters() below would always match zero hikes.
        results.addSource(allHikes, ignored -> { });
    }

    public LiveData<List<Hike>> getResults() {
        return results;
    }

    public LiveData<Boolean> getHasSearched() {
        return hasSearched;
    }

    public LiveData<Date> getDateFrom() {
        return dateFromLiveData;
    }

    public LiveData<Date> getDateTo() {
        return dateToLiveData;
    }

    public void setNameQuery(String query) {
        nameQuery = query;
    }

    public void setDifficultyFilter(String difficulty) {
        difficultyFilter = difficulty;
    }

    public void setLocationFilter(String location) {
        locationFilter = location;
    }

    public void setMinLength(Double km) {
        minLength = km;
    }

    public void setMaxLength(Double km) {
        maxLength = km;
    }

    public void setDateFrom(Date date) {
        dateFrom = date;
        dateFromLiveData.setValue(date);
    }

    public void setDateTo(Date date) {
        dateTo = date;
        dateToLiveData.setValue(date);
    }

    public void resetFilters() {
        nameQuery = "";
        difficultyFilter = DIFFICULTY_ALL;
        locationFilter = "";
        minLength = null;
        maxLength = null;
        dateFrom = null;
        dateTo = null;
        dateFromLiveData.setValue(null);
        dateToLiveData.setValue(null);
        hasSearched.setValue(false);
        results.setValue(new ArrayList<>());
    }

    public void applyFilters() {
        List<Hike> source = allHikes.getValue();
        List<Hike> filtered = new ArrayList<>();
        if (source != null) {
            for (Hike hike : source) {
                if (matches(hike)) {
                    filtered.add(hike);
                }
            }
        }
        hasSearched.setValue(true);
        results.setValue(filtered);
    }

    private boolean matches(Hike hike) {
        if (!TextUtils.isEmpty(nameQuery)) {
            String name = hike.getName() != null ? hike.getName() : "";
            if (!name.toLowerCase().startsWith(nameQuery.trim().toLowerCase())) {
                return false;
            }
        }
        if (!DIFFICULTY_ALL.equals(difficultyFilter)
                && !difficultyFilter.equals(hike.getDifficulty())) {
            return false;
        }
        if (!TextUtils.isEmpty(locationFilter)) {
            String location = hike.getLocation() != null ? hike.getLocation() : "";
            if (!location.toLowerCase().contains(locationFilter.trim().toLowerCase())) {
                return false;
            }
        }
        if (minLength != null && hike.getLength() < minLength) {
            return false;
        }
        if (maxLength != null && hike.getLength() > maxLength) {
            return false;
        }
        if (dateFrom != null && hike.getHikeDate() != null && hike.getHikeDate().before(dateFrom)) {
            return false;
        }
        if (dateTo != null && hike.getHikeDate() != null && hike.getHikeDate().after(dateTo)) {
            return false;
        }
        return true;
    }
}
