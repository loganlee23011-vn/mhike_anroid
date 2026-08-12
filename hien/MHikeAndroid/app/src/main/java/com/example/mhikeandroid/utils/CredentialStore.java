package com.example.mhikeandroid.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/** "Remember me" storage for LoginFragment. Backed by EncryptedSharedPreferences (AES-256,
 *  Android Keystore-wrapped master key) rather than plain SharedPreferences, since a raw
 *  password is being persisted on disk (OWASP Mobile Top 10 M2: Insecure Data Storage). */
public final class CredentialStore {

    private static final String PREFS_FILE = "mhike_secure_credentials";
    private static final String KEY_EMAIL = "saved_email";
    private static final String KEY_PASSWORD = "saved_password";

    private CredentialStore() {
    }

    public static void save(Context context, String email, String password) {
        SharedPreferences prefs = prefs(context);
        if (prefs == null) {
            return;
        }
        prefs.edit()
                .putString(KEY_EMAIL, email)
                .putString(KEY_PASSWORD, password)
                .apply();
    }

    public static void clear(Context context) {
        SharedPreferences prefs = prefs(context);
        if (prefs == null) {
            return;
        }
        prefs.edit().clear().apply();
    }

    public static String getEmail(Context context) {
        SharedPreferences prefs = prefs(context);
        return prefs == null ? null : prefs.getString(KEY_EMAIL, null);
    }

    public static String getPassword(Context context) {
        SharedPreferences prefs = prefs(context);
        return prefs == null ? null : prefs.getString(KEY_PASSWORD, null);
    }

    private static SharedPreferences prefs(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            return EncryptedSharedPreferences.create(
                    context,
                    PREFS_FILE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException | IOException e) {
            // Keystore unavailable (rare, e.g. corrupted keystore) — fail safe by not
            // remembering credentials rather than crashing the login screen.
            Log.w("CredentialStore", "EncryptedSharedPreferences unavailable", e);
            return null;
        }
    }
}
