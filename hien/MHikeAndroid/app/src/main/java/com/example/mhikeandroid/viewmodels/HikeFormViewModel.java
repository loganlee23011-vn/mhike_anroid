package com.example.mhikeandroid.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mhikeandroid.models.Hike;
import com.example.mhikeandroid.repositories.HikeRepository;

/**
 * Scoped to the "entryFragment" nested nav graph (EntryBasic -> EntryRoute -> Confirm), shared
 * by all three fragments via NavController.getBackStackEntry(R.id.entryFragment). Plays the
 * role of MHikeRN's HikeFormContext: one mutable draft that every step reads/writes directly.
 * Unlike the RN context (which lives for the whole app and needs an explicit resetForm() to
 * avoid leaking stale data into the next add-hike attempt), this ViewModel is torn down
 * automatically by Navigation Component the moment the nested graph is popped off the back
 * stack, so a fresh instance (and thus a blank draft) is guaranteed the next time the flow is
 * entered even without calling resetForm().
 */
public class HikeFormViewModel extends ViewModel {

    private final HikeRepository hikeRepository = new HikeRepository();

    private final Hike draft = new Hike();
    private String editingHikeId;
    private boolean fetchStarted = false;

    private final MutableLiveData<Hike> loadedHike = new MutableLiveData<>();

    public Hike getDraft() {
        return draft;
    }

    public boolean isEditing() {
        return editingHikeId != null;
    }

    public String getEditingHikeId() {
        return editingHikeId;
    }

    public LiveData<Hike> getLoadedHike() {
        return loadedHike;
    }

    /** Fetches the hike being edited once, then seeds every field of the shared draft from it
     *  (mirrors HikeFormContext's startEdit(hike), which seeds the whole form in one shot). */
    public void loadHikeForEdit(String hikeId) {
        if (fetchStarted) {
            return;
        }
        fetchStarted = true;
        editingHikeId = hikeId;
        hikeRepository.getHike(hikeId).addOnSuccessListener(hike -> {
            if (hike == null) {
                return;
            }
            copyInto(draft, hike);
            loadedHike.setValue(draft);
        });
    }

    public void resetForm() {
        copyInto(draft, new Hike());
        editingHikeId = null;
        fetchStarted = false;
    }

    private static void copyInto(Hike target, Hike source) {
        target.setId(source.getId());
        target.setUserId(source.getUserId());
        target.setName(source.getName());
        target.setLocation(source.getLocation());
        target.setHikeDate(source.getHikeDate());
        target.setParkingAvailable(source.isParkingAvailable());
        target.setLength(source.getLength());
        target.setDifficulty(source.getDifficulty());
        target.setDescription(source.getDescription());
        target.setEstimatedDuration(source.getEstimatedDuration());
        target.setTerrainType(source.getTerrainType());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
    }
}
