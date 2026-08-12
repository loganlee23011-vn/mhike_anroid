package com.example.mhikeandroid.utils;

import android.text.TextUtils;

import java.util.Date;

/** FR1 required-field validation, shared by add and edit (EntryBasicViewModel/EntryRouteViewModel call each check). */
public final class HikeValidator {

    private HikeValidator() {
    }

    public static String validateName(String name) {
        return TextUtils.isEmpty(name) ? "Hike name is required" : null;
    }

    /** Generic required-field check, reused by ObservationsViewModel. */
    public static String validateRequired(String value, String errorMessage) {
        return TextUtils.isEmpty(value) ? errorMessage : null;
    }

    public static String validateLocation(String location) {
        return TextUtils.isEmpty(location) ? "Location is required" : null;
    }

    public static String validateDate(Date date) {
        return date == null ? "Date is required" : null;
    }

    public static String validateParking(Boolean parkingAvailable) {
        return parkingAvailable == null ? "Select whether parking is available" : null;
    }

    public static String validateDifficulty(String difficulty) {
        return TextUtils.isEmpty(difficulty) ? "Select a difficulty" : null;
    }

    /** Returns an error message, or null if lengthText parses to a positive number of km. */
    public static String validateLength(String lengthText) {
        if (TextUtils.isEmpty(lengthText)) {
            return "Length is required";
        }
        try {
            double length = Double.parseDouble(lengthText);
            if (length <= 0) {
                return "Length must be greater than 0";
            }
        } catch (NumberFormatException e) {
            return "Enter a valid length in km";
        }
        return null;
    }
}
