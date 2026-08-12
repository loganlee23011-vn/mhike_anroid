package com.example.mhikeandroid.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mhikeandroid.models.Hike;
import com.example.mhikeandroid.repositories.AuthRepository;
import com.example.mhikeandroid.utils.Event;
import com.example.mhikeandroid.utils.HikeValidator;

import java.util.Date;

/**
 * Backs EntryBasicFragment (step 1/2 of the Add Hike wizard). Holds only what's local to this
 * step and this fragment instance (prefill-once guard, the date picker's displayed value,
 * validation errors) — the actual field values live in the shared HikeFormViewModel draft.
 */
public class EntryBasicViewModel extends ViewModel {

    private final AuthRepository authRepository = new AuthRepository();
    private boolean prefillApplied = false;

    private final MutableLiveData<Date> selectedDate = new MutableLiveData<>();

    private final MutableLiveData<String> nameError = new MutableLiveData<>();
    private final MutableLiveData<String> locationError = new MutableLiveData<>();
    private final MutableLiveData<String> dateError = new MutableLiveData<>();
    private final MutableLiveData<String> parkingError = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> validatedEvent = new MutableLiveData<>();

    public boolean shouldApplyPrefill() {
        if (prefillApplied) {
            return false;
        }
        prefillApplied = true;
        return true;
    }

    public LiveData<Date> getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Date date) {
        selectedDate.setValue(date);
    }

    public LiveData<String> getNameError() {
        return nameError;
    }

    public LiveData<String> getLocationError() {
        return locationError;
    }

    public LiveData<String> getDateError() {
        return dateError;
    }

    public LiveData<String> getParkingError() {
        return parkingError;
    }

    public LiveData<Event<Boolean>> getValidatedEvent() {
        return validatedEvent;
    }

    public void validate(Hike draft, String name, String location, Date hikeDate,
                          Boolean parkingAvailable, String description) {
        String nameErr = HikeValidator.validateName(name);
        String locationErr = HikeValidator.validateLocation(location);
        String dateErr = HikeValidator.validateDate(hikeDate);
        String parkingErr = HikeValidator.validateParking(parkingAvailable);

        nameError.setValue(nameErr);
        locationError.setValue(locationErr);
        dateError.setValue(dateErr);
        parkingError.setValue(parkingErr);

        if (nameErr != null || locationErr != null || dateErr != null || parkingErr != null) {
            return;
        }

        draft.setUserId(authRepository.getCurrentUser() != null
                ? authRepository.getCurrentUser().getUid() : null);
        draft.setName(name);
        draft.setLocation(location);
        draft.setHikeDate(hikeDate);
        draft.setParkingAvailable(parkingAvailable);
        draft.setDescription(description);

        validatedEvent.setValue(new Event<>(true));
    }
}
