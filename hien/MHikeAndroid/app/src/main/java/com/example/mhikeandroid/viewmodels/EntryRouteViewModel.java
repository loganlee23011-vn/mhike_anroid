package com.example.mhikeandroid.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mhikeandroid.models.Hike;
import com.example.mhikeandroid.utils.Event;
import com.example.mhikeandroid.utils.HikeValidator;

/**
 * Backs EntryRouteFragment (step 2/2 of the Add Hike wizard). Same shape as
 * EntryBasicViewModel: only local prefill-once state + this step's validation errors, values
 * live in the shared HikeFormViewModel draft.
 *
 * Deliberately validates both Length AND Difficulty as required (MHikeRN's EntryRouteScreen
 * only validates Length despite the "*" on Difficulty) — Difficulty is a required field per
 * this app's own FR1, so skipping it would be a real regression, not parity.
 */
public class EntryRouteViewModel extends ViewModel {

    private boolean prefillApplied = false;

    private final MutableLiveData<String> lengthError = new MutableLiveData<>();
    private final MutableLiveData<String> difficultyError = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> validatedEvent = new MutableLiveData<>();

    public boolean shouldApplyPrefill() {
        if (prefillApplied) {
            return false;
        }
        prefillApplied = true;
        return true;
    }

    public LiveData<String> getLengthError() {
        return lengthError;
    }

    public LiveData<String> getDifficultyError() {
        return difficultyError;
    }

    public LiveData<Event<Boolean>> getValidatedEvent() {
        return validatedEvent;
    }

    public void validate(Hike draft, String lengthText, String difficulty,
                          String estimatedDuration, String terrainType) {
        String lengthErr = HikeValidator.validateLength(lengthText);
        String difficultyErr = HikeValidator.validateDifficulty(difficulty);

        lengthError.setValue(lengthErr);
        difficultyError.setValue(difficultyErr);

        if (lengthErr != null || difficultyErr != null) {
            return;
        }

        draft.setLength(Double.parseDouble(lengthText));
        draft.setDifficulty(difficulty);
        draft.setEstimatedDuration(estimatedDuration);
        draft.setTerrainType(terrainType);

        validatedEvent.setValue(new Event<>(true));
    }
}
