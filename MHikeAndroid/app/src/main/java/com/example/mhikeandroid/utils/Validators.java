package com.example.mhikeandroid.utils;

import android.text.TextUtils;
import android.util.Patterns;

/** Pure email/password validation helpers shared by Login and Register. */
public final class Validators {

    public static final int MIN_PASSWORD_LENGTH = 6; // Firebase Auth's own minimum.

    private Validators() {
    }

    public static String validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return "Email is required";
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Enter a valid email address";
        }
        return null;
    }

    public static String validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            return "Password is required";
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "Password must be at least " + MIN_PASSWORD_LENGTH + " characters";
        }
        return null;
    }

    public static String validateConfirmPassword(String password, String confirmPassword) {
        if (TextUtils.isEmpty(confirmPassword)) {
            return "Please confirm your password";
        }
        if (!confirmPassword.equals(password)) {
            return "Passwords do not match";
        }
        return null;
    }
}
