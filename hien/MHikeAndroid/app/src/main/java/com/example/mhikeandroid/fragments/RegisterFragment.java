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
import com.example.mhikeandroid.databinding.FragmentRegisterBinding;
import com.example.mhikeandroid.viewmodels.AuthViewModel;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.registerButton.setOnClickListener(v -> viewModel.register(
                binding.emailInput.getText().toString().trim(),
                binding.passwordInput.getText().toString(),
                binding.confirmPasswordInput.getText().toString()));

        binding.goToLoginText.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

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
        viewModel.getConfirmPasswordError().observe(getViewLifecycleOwner(), error -> {
            binding.confirmPasswordLayout.setError(error);
            if (error != null && binding.emailLayout.getError() == null
                    && binding.passwordLayout.getError() == null) {
                binding.confirmPasswordInput.requestFocus();
            }
        });
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.registerButton.setEnabled(!loading);
        });
        viewModel.getAuthSuccessEvent().observe(getViewLifecycleOwner(), event -> {
            if (event.getContentIfNotHandled() != null) {
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
