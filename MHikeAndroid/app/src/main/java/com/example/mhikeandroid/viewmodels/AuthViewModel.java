package com.example.mhikeandroid.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mhikeandroid.repositories.AuthRepository;
import com.example.mhikeandroid.utils.Event;
import com.example.mhikeandroid.utils.Validators;

public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository = new AuthRepository();

    private final MutableLiveData<String> emailError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();
    private final MutableLiveData<String> confirmPasswordError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<Boolean>> authSuccessEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> authErrorEvent = new MutableLiveData<>();

    public LiveData<String> getEmailError() {
        return emailError;
    }

    public LiveData<String> getPasswordError() {
        return passwordError;
    }

    public LiveData<String> getConfirmPasswordError() {
        return confirmPasswordError;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Event<Boolean>> getAuthSuccessEvent() {
        return authSuccessEvent;
    }

    public LiveData<Event<String>> getAuthErrorEvent() {
        return authErrorEvent;
    }

    public void login(String email, String password) {
        String emailErr = Validators.validateEmail(email);
        String passwordErr = Validators.validatePassword(password);
        emailError.setValue(emailErr);
        passwordError.setValue(passwordErr);
        if (emailErr != null || passwordErr != null) {
            return;
        }
        loading.setValue(true);
        authRepository.login(email, password)
                .addOnSuccessListener(result -> {
                    loading.setValue(false);
                    authSuccessEvent.setValue(new Event<>(true));
                })
                .addOnFailureListener(this::onAuthFailure);
    }

    public void register(String email, String password, String confirmPassword) {
        String emailErr = Validators.validateEmail(email);
        String passwordErr = Validators.validatePassword(password);
        String confirmErr = Validators.validateConfirmPassword(password, confirmPassword);
        emailError.setValue(emailErr);
        passwordError.setValue(passwordErr);
        confirmPasswordError.setValue(confirmErr);
        if (emailErr != null || passwordErr != null || confirmErr != null) {
            return;
        }
        loading.setValue(true);
        authRepository.register(email, password)
                .addOnSuccessListener(result -> {
                    loading.setValue(false);
                    authSuccessEvent.setValue(new Event<>(true));
                })
                .addOnFailureListener(this::onAuthFailure);
    }

    private void onAuthFailure(@NonNull Exception e) {
        loading.setValue(false);
        authErrorEvent.setValue(new Event<>(e.getMessage()));
    }
}
