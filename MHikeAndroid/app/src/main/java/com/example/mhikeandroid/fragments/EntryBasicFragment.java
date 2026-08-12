package com.example.mhikeandroid.fragments;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mhikeandroid.R;
import com.google.android.material.button.MaterialButton;
import com.example.mhikeandroid.databinding.FragmentEntryBasicBinding;
import com.example.mhikeandroid.models.Hike;
import com.example.mhikeandroid.utils.DateTimeUtils;
import com.example.mhikeandroid.viewmodels.EntryBasicViewModel;
import com.example.mhikeandroid.viewmodels.HikeFormViewModel;

import java.util.Calendar;

/** Step 1/2 of the Add/Edit Hike wizard (MHikeRN's EntryBasicScreen). */
public class EntryBasicFragment extends Fragment {

    private FragmentEntryBasicBinding binding;
    private EntryBasicViewModel viewModel;
    private HikeFormViewModel formViewModel;
    private String editingHikeId;
    private Boolean parkingAvailable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentEntryBasicBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EntryBasicViewModel.class);
        formViewModel = hikeFormViewModel();
        editingHikeId = getArguments() != null ? getArguments().getString("hikeId") : null;
        boolean isEdit = editingHikeId != null || formViewModel.isEditing();

        binding.titleText.setText(isEdit ? R.string.entry_title_edit : R.string.entry_title_add);

        binding.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());
        binding.closeButton.setOnClickListener(v -> {
            formViewModel.resetForm();
            NavHostFragment.findNavController(this).navigate(R.id.homeFragment);
        });

        binding.dateInput.setOnClickListener(v -> showDatePicker());

        binding.parkingYesButton.setOnClickListener(v -> {
            parkingAvailable = true;
            styleParkingButtons();
        });
        binding.parkingNoButton.setOnClickListener(v -> {
            parkingAvailable = false;
            styleParkingButtons();
        });
        styleParkingButtons();

        binding.continueButton.setOnClickListener(v -> {
            viewModel.validate(
                    formViewModel.getDraft(),
                    binding.nameInput.getText().toString().trim(),
                    binding.locationInput.getText().toString().trim(),
                    viewModel.getSelectedDate().getValue(),
                    parkingAvailable,
                    binding.descriptionInput.getText().toString().trim());
        });

        if (editingHikeId != null) {
            formViewModel.loadHikeForEdit(editingHikeId);
        }
        formViewModel.getLoadedHike().observe(getViewLifecycleOwner(), hike -> {
            if (hike != null && viewModel.shouldApplyPrefill()) {
                applyPrefill(hike);
            }
        });

        viewModel.getSelectedDate().observe(getViewLifecycleOwner(),
                date -> binding.dateInput.setText(DateTimeUtils.formatDate(date)));

        viewModel.getNameError().observe(getViewLifecycleOwner(), error -> {
            binding.nameLayout.setError(error);
            if (error != null) binding.nameInput.requestFocus();
        });
        viewModel.getLocationError().observe(getViewLifecycleOwner(), error -> {
            binding.locationLayout.setError(error);
            if (error != null && binding.nameLayout.getError() == null) {
                binding.locationInput.requestFocus();
            }
        });
        viewModel.getDateError().observe(getViewLifecycleOwner(), error -> {
            binding.dateLayout.setError(error);
            if (error != null && binding.nameLayout.getError() == null
                    && binding.locationLayout.getError() == null) {
                binding.dateInput.requestFocus();
            }
        });
        viewModel.getParkingError().observe(getViewLifecycleOwner(), error -> {
            binding.parkingErrorText.setVisibility(error != null ? View.VISIBLE : View.GONE);
            binding.parkingErrorText.setText(error);
        });

        viewModel.getValidatedEvent().observe(getViewLifecycleOwner(), event -> {
            if (event.getContentIfNotHandled() != null) {
                NavHostFragment.findNavController(this).navigate(R.id.entryRouteFragment);
            }
        });
    }

    private void applyPrefill(Hike hike) {
        binding.nameInput.setText(hike.getName());
        binding.locationInput.setText(hike.getLocation());
        viewModel.setSelectedDate(hike.getHikeDate());
        parkingAvailable = hike.isParkingAvailable();
        styleParkingButtons();
        binding.descriptionInput.setText(hike.getDescription());
    }

    /** Pill toggle styling: selected = light-green fill + primary stroke/text, matching
     *  RN's Yes/No segmented buttons in EntryBasicScreen.js. */
    private void styleParkingButtons() {
        styleParkingButton(binding.parkingYesButton, Boolean.TRUE.equals(parkingAvailable));
        styleParkingButton(binding.parkingNoButton, Boolean.FALSE.equals(parkingAvailable));
    }

    private void styleParkingButton(MaterialButton button, boolean selected) {
        int primary = ContextCompat.getColor(requireContext(), R.color.mhike_primary);
        int primaryLight = ContextCompat.getColor(requireContext(), R.color.mhike_primary_light);
        int outline = ContextCompat.getColor(requireContext(), R.color.mhike_outline);
        int textPrimary = ContextCompat.getColor(requireContext(), R.color.mhike_text_primary);
        if (selected) {
            button.setBackgroundTintList(ColorStateList.valueOf(primaryLight));
            button.setStrokeColor(ColorStateList.valueOf(primary));
            button.setTextColor(primary);
            button.setTypeface(button.getTypeface(), Typeface.BOLD);
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
            button.setStrokeColor(ColorStateList.valueOf(outline));
            button.setTextColor(textPrimary);
            button.setTypeface(button.getTypeface(), Typeface.NORMAL);
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (viewModel.getSelectedDate().getValue() != null) {
            calendar.setTime(viewModel.getSelectedDate().getValue());
        }
        new DatePickerDialog(requireContext(), (picker, year, month, dayOfMonth) ->
                viewModel.setSelectedDate(DateTimeUtils.fromYmd(year, month, dayOfMonth)),
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private HikeFormViewModel hikeFormViewModel() {
        NavBackStackEntry entry = NavHostFragment.findNavController(this).getBackStackEntry(R.id.entryFragment);
        return new ViewModelProvider(entry).get(HikeFormViewModel.class);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
