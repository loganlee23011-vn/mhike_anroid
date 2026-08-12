package com.example.mhikeandroid.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mhikeandroid.models.Observation;
import com.example.mhikeandroid.repositories.AuthRepository;
import com.example.mhikeandroid.repositories.HikeRepository;
import com.example.mhikeandroid.repositories.ObservationRepository;
import com.example.mhikeandroid.utils.Event;
import com.example.mhikeandroid.utils.HikeValidator;

import java.util.Date;
import java.util.List;

/**
 * Backs the single merged Observations screen (list + inline add/edit editor), mirroring
 * MHikeRN's ObservationScreen.js state machine: adding (editor visible vs FAB), editingId
 * (null = add mode), text/comments/observedAt.
 *
 * Deliberately keeps Comments + Observed-at as editable fields in the inline editor even
 * though MHikeRN's own inline editor only exposes observationText — FR3 requires Observed-at
 * (defaulting to now) and optional Comments, so dropping them would regress this app's own
 * spec rather than just matching RN's simplification.
 */
public class ObservationsViewModel extends ViewModel {

    private final ObservationRepository observationRepository = new ObservationRepository();
    private final HikeRepository hikeRepository = new HikeRepository();
    private final AuthRepository authRepository = new AuthRepository();

    private String hikeId;
    private LiveData<List<Observation>> observations;
    private boolean initialized = false;

    // Anyone signed in may add an observation to any hike, but only the author may edit their
    // own text, and only the author or the hike's owner (moderating their own hike) may delete —
    // mirrors MHikeRN's ObservationScreen.js + firestore.rules isHikeOwnerOf().
    private final MutableLiveData<Boolean> isHikeOwner = new MutableLiveData<>(false);

    private Observation editingObservation;
    private final MutableLiveData<Boolean> adding = new MutableLiveData<>(false);
    private final MutableLiveData<Date> observedAt = new MutableLiveData<>();
    private final MutableLiveData<String> observationTextError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saving = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> saveErrorEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> deleteErrorEvent = new MutableLiveData<>();

    /** Fires exactly once per startAdd()/startEdit() call so the Fragment knows to (re)write
     *  the editor's text fields — as opposed to `adding` itself, which an observer also sees
     *  on every rotation and must NOT use as a signal to overwrite the EditTexts' own
     *  auto-restored in-progress text. When it fires, read getEditingObservation() for what to
     *  prefill from (null = add mode, blank fields). */
    private final MutableLiveData<Event<Boolean>> prefillEvent = new MutableLiveData<>();

    public LiveData<Event<Boolean>> getPrefillEvent() {
        return prefillEvent;
    }

    public void init(String hikeId) {
        if (initialized) {
            return;
        }
        initialized = true;
        this.hikeId = hikeId;
        observations = observationRepository.observeObservations(hikeId);
        hikeRepository.getHike(hikeId).addOnSuccessListener(hike -> {
            String uid = getCurrentUserId();
            isHikeOwner.setValue(hike != null && hike.getUserId() != null && hike.getUserId().equals(uid));
        });
    }

    public LiveData<List<Observation>> getObservations() {
        return observations;
    }

    public LiveData<Boolean> getIsHikeOwner() {
        return isHikeOwner;
    }

    public String getCurrentUserId() {
        return authRepository.getCurrentUser() != null ? authRepository.getCurrentUser().getUid() : null;
    }

    public LiveData<Boolean> getAdding() {
        return adding;
    }

    public LiveData<Date> getObservedAt() {
        return observedAt;
    }

    public void setObservedAt(Date date) {
        observedAt.setValue(date);
    }

    public LiveData<String> getObservationTextError() {
        return observationTextError;
    }

    public LiveData<Boolean> getSaving() {
        return saving;
    }

    public LiveData<Event<String>> getSaveErrorEvent() {
        return saveErrorEvent;
    }

    public LiveData<Event<String>> getDeleteErrorEvent() {
        return deleteErrorEvent;
    }

    /** Null when adding a new observation, the observation being edited otherwise. Consumed by
     *  the Fragment once (like the old consumePrefillObservation) to prefill the text fields
     *  without clobbering in-progress typing after a rotation. */
    public Observation getEditingObservation() {
        return editingObservation;
    }

    public void startAdd() {
        editingObservation = null;
        observationTextError.setValue(null);
        observedAt.setValue(new Date());
        adding.setValue(true);
        prefillEvent.setValue(new Event<>(true));
    }

    public void startEdit(Observation observation) {
        editingObservation = observation;
        observationTextError.setValue(null);
        observedAt.setValue(observation.getObservedAt());
        adding.setValue(true);
        prefillEvent.setValue(new Event<>(true));
    }

    public void closeEditor() {
        editingObservation = null;
        observationTextError.setValue(null);
        adding.setValue(false);
    }

    public void save(String observationText, String comments) {
        String error = HikeValidator.validateRequired(observationText, "Observation text is required");
        observationTextError.setValue(error);
        if (error != null) {
            return;
        }

        Observation observation = new Observation();
        observation.setId(editingObservation != null ? editingObservation.getId() : null);
        observation.setHikeId(hikeId);
        observation.setUserId(authRepository.getCurrentUser() != null
                ? authRepository.getCurrentUser().getUid() : null);
        observation.setObservationText(observationText);
        observation.setObservedAt(observedAt.getValue() != null ? observedAt.getValue() : new Date());
        observation.setComments(comments);

        saving.setValue(true);
        if (editingObservation != null) {
            observationRepository.updateObservation(observation)
                    .addOnSuccessListener(unused -> onSaveSuccess())
                    .addOnFailureListener(this::onSaveFailure);
        } else {
            observationRepository.addObservation(observation)
                    .addOnSuccessListener(unused -> onSaveSuccess())
                    .addOnFailureListener(this::onSaveFailure);
        }
    }

    private void onSaveSuccess() {
        saving.setValue(false);
        closeEditor();
    }

    private void onSaveFailure(Exception e) {
        saving.setValue(false);
        saveErrorEvent.setValue(new Event<>(e.getMessage()));
    }

    public void deleteObservation(Observation observation) {
        observationRepository.deleteObservation(observation.getId())
                .addOnFailureListener(e -> deleteErrorEvent.setValue(new Event<>(e.getMessage())));
    }
}
