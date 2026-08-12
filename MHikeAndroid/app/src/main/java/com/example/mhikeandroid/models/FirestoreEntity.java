package com.example.mhikeandroid.models;

/** Implemented by every Firestore-backed model so repository code can set the doc id
 * (excluded from the mapped fields) after deserialization, generically. */
public interface FirestoreEntity {
    void setId(String id);
}
