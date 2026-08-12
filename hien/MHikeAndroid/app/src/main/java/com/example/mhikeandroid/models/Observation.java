package com.example.mhikeandroid.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.Date;

/**
 * Mirrors the `observations` Firestore collection (see CLAUDE.md data model). Field names
 * match MHikeRN's observationService.js exactly. Serializable so an instance can travel
 * through Fragment Bundle args when opening the inline edit editor on ObservationsFragment.
 */
@IgnoreExtraProperties
public class Observation implements Serializable, FirestoreEntity {

    private String id;
    private String hikeId;
    private String userId;
    private String observationText;
    private Date observedAt;
    private String comments;

    public Observation() {
        // Required by Firestore for automatic POJO deserialization.
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getHikeId() {
        return hikeId;
    }

    public void setHikeId(String hikeId) {
        this.hikeId = hikeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getObservationText() {
        return observationText;
    }

    public void setObservationText(String observationText) {
        this.observationText = observationText;
    }

    public Date getObservedAt() {
        return observedAt;
    }

    public void setObservedAt(Date observedAt) {
        this.observedAt = observedAt;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
