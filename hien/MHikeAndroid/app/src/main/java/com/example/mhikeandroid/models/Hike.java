package com.example.mhikeandroid.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

/**
 * Mirrors the `hikes` Firestore collection (see CLAUDE.md data model). Field names match
 * MHikeRN's hikeService.js exactly. latitude/longitude/weather exist in the shared schema
 * for the RN app's location feature but are never set from Android.
 * Serializable so a draft can travel through Fragment Bundle args (Entry -> Confirm).
 */
@IgnoreExtraProperties
public class Hike implements Serializable, FirestoreEntity {

    private String id;
    private String userId;
    private String name;
    private String location;
    private Date hikeDate;
    private boolean parkingAvailable;
    private double length;
    private String difficulty;
    private String description;
    private String estimatedDuration;
    private String terrainType;
    @ServerTimestamp
    private Date createdAt;
    @ServerTimestamp
    private Date updatedAt;

    public Hike() {
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getHikeDate() {
        return hikeDate;
    }

    public void setHikeDate(Date hikeDate) {
        this.hikeDate = hikeDate;
    }

    public boolean isParkingAvailable() {
        return parkingAvailable;
    }

    public void setParkingAvailable(boolean parkingAvailable) {
        this.parkingAvailable = parkingAvailable;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(String estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public String getTerrainType() {
        return terrainType;
    }

    public void setTerrainType(String terrainType) {
        this.terrainType = terrainType;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
