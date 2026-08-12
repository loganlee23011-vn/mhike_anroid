package com.example.mhikeandroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mhikeandroid.R;
import com.example.mhikeandroid.databinding.FragmentLoginBinding;
import com.example.mhikeandroid.utils.CredentialStore;
import com.example.mhikeandroid.viewmodels.AuthViewModel;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        String savedEmail = CredentialStore.getEmail(requireContext());
        String savedPassword = CredentialStore.getPassword(requireContext());
        if (savedEmail != null && savedPassword != null) {
            binding.emailInput.setText(savedEmail);
            binding.passwordInput.setText(savedPassword);
            binding.rememberMeCheckBox.setChecked(true);
        }

        binding.loginButton.setOnClickListener(v -> viewModel.login(
                binding.emailInput.getText().toString().trim(),
                binding.passwordInput.getText().toString()));

        binding.goToRegisterText.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_login_to_register));

        viewModel.getEmailError().observe(getViewLifecycleOwner(), error -> {
            binding.emailLayout.setError(error);
            if (error != null) {
                binding.emailInput.requestFocus();
            }
        });
        viewModel.getPasswordError().observe(getViewLifecycleOwner(), error -> {
            binding.passwordLayout.setError(error);
            if (error != null && binding.emailLayout.getError() == null) {
                binding.passwordInput.requestFocus();
            }
        });
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.loginButton.setEnabled(!loading);
        });
        viewModel.getAuthSuccessEvent().observe(getViewLifecycleOwner(), event -> {
            if (event.getContentIfNotHandled() != null) {
                if (binding.rememberMeCheckBox.isChecked()) {
                    CredentialStore.save(requireContext(),
                            binding.emailInput.getText().toString().trim(),
                            binding.passwordInput.getText().toString());
                } else {
                    CredentialStore.clear(requireContext());
                }
                NavOptions options = new NavOptions.Builder()
                        .setPopUpTo(R.id.loginFragment, true)
                        .build();
                NavHostFragment.findNavController(this).navigate(R.id.homeFragment, null, options);
            }
        });
        viewModel.getAuthErrorEvent().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
