package com.example.mhikeandroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mhikeandroid.R;
import com.example.mhikeandroid.databinding.FragmentEntryRouteBinding;
import com.example.mhikeandroid.models.Hike;
import com.example.mhikeandroid.viewmodels.EntryRouteViewModel;
import com.example.mhikeandroid.viewmodels.HikeFormViewModel;
import com.google.android.material.chip.Chip;

/** Step 2/2 of the Add/Edit Hike wizard (MHikeRN's EntryRouteScreen). */
public class EntryRouteFragment extends Fragment {

    private FragmentEntryRouteBinding binding;
    private EntryRouteViewModel viewModel;
    private HikeFormViewModel formViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentEntryRouteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EntryRouteViewModel.class);
        formViewModel = hikeFormViewModel();
        boolean isEdit = formViewModel.isEditing();

        binding.titleText.setText(isEdit ? R.string.entry_title_edit : R.string.entry_title_add);

        binding.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());
        binding.closeButton.setOnClickListener(v -> {
            formViewModel.resetForm();
            NavHostFragment.findNavController(this).navigate(R.id.homeFragment);
        });
        binding.backStepButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        if (viewModel.shouldApplyPrefill()) {
            applyPrefill(formViewModel.getDraft());
        }

        binding.continueButton.setOnClickListener(v -> {
            String difficulty = selectedChipText(binding.difficultyChipGroup);
            String terrainType = selectedChipText(binding.terrainChipGroup);
            viewModel.validate(
                    formViewModel.getDraft(),
                    binding.lengthInput.getText().toString().trim(),
                    difficulty,
                    binding.estimatedDurationInput.getText().toString().trim(),
                    terrainType);
        });

        viewModel.getLengthError().observe(getViewLifecycleOwner(), error -> {
            binding.lengthLayout.setError(error);
            if (error != null) binding.lengthInput.requestFocus();
        });
        viewModel.getDifficultyError().observe(getViewLifecycleOwner(), error -> {
            binding.difficultyErrorText.setVisibility(error != null ? View.VISIBLE : View.GONE);
            binding.difficultyErrorText.setText(error);
        });

        viewModel.getValidatedEvent().observe(getViewLifecycleOwner(), event -> {
            if (event.getContentIfNotHandled() != null) {
                NavHostFragment.findNavController(this).navigate(R.id.confirmFragment);
            }
        });
    }

    private void applyPrefill(Hike draft) {
        if (draft.getLength() > 0) {
            binding.lengthInput.setText(String.valueOf(draft.getLength()));
        }
        checkChipByText(binding.difficultyChipGroup, draft.getDifficulty());
        binding.estimatedDurationInput.setText(draft.getEstimatedDuration());
        checkChipByText(binding.terrainChipGroup, draft.getTerrainType());
    }

    private static void checkChipByText(com.google.android.material.chip.ChipGroup group, String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof Chip && text.equalsIgnoreCase(((Chip) child).getText().toString())) {
                ((Chip) child).setChecked(true);
                return;
            }
        }
    }

    private static String selectedChipText(com.google.android.material.chip.ChipGroup group) {
        int checkedId = group.getCheckedChipId();
        if (checkedId == View.NO_ID) {
            return "";
        }
        Chip chip = group.findViewById(checkedId);
        return chip != null ? chip.getText().toString() : "";
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
