package com.example.mhikeandroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mhikeandroid.R;
import com.example.mhikeandroid.databinding.FragmentConfirmBinding;
import com.example.mhikeandroid.databinding.ItemConfirmRowBinding;
import com.example.mhikeandroid.models.Hike;
import com.example.mhikeandroid.utils.DateTimeUtils;
import com.example.mhikeandroid.viewmodels.ConfirmViewModel;
import com.example.mhikeandroid.viewmodels.HikeFormViewModel;

/** Review screen: 2 sections (Basic Information / Trail Details), each with its own "Edit"
 *  link that pops back to that wizard step (MHikeRN's ConfirmScreen ReviewSection). */
public class ConfirmFragment extends Fragment {

    private FragmentConfirmBinding binding;
    private ConfirmViewModel viewModel;
    private HikeFormViewModel formViewModel;
    private Hike hike;
    private boolean isEdit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentConfirmBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ConfirmViewModel.class);
        formViewModel = hikeFormViewModel();
        hike = formViewModel.getDraft();
        isEdit = formViewModel.isEditing();

        binding.titleText.setText(isEdit ? R.string.confirm_title_edit : R.string.confirm_title_add);
        binding.confirmSaveButton.setText(isEdit ? R.string.action_save_changes : R.string.action_save_hike);

        populateSummary();

        binding.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());
        binding.editBasicText.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack(R.id.entryBasicFragment, false));
        binding.editTrailText.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack(R.id.entryRouteFragment, false));
        binding.confirmSaveButton.setOnClickListener(v -> viewModel.save(hike, isEdit));

        viewModel.getSaving().observe(getViewLifecycleOwner(), saving -> {
            binding.progressIndicator.setVisibility(saving ? View.VISIBLE : View.GONE);
            binding.confirmSaveButton.setEnabled(!saving);
            binding.backButton.setEnabled(!saving);
        });
        viewModel.getSaveSuccessEvent().observe(getViewLifecycleOwner(), event -> {
            String hikeId = event.getContentIfNotHandled();
            if (hikeId != null) {
                formViewModel.resetForm();
                if (isEdit) {
                    // Edit started from an existing hike's Detail screen — return there so
                    // the user can see the change take effect in place.
                    Bundle args = new Bundle();
                    args.putString("hikeId", hikeId);
                    Toast.makeText(requireContext(), R.string.confirm_toast_hike_updated, Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this).navigate(R.id.action_confirm_to_detail, args);
                } else {
                    // New hike: land back on the Home dashboard rather than a Detail screen
                    // for a hike the user has never seen yet.
                    Toast.makeText(requireContext(), R.string.confirm_toast_hike_added, Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this).navigate(R.id.action_confirm_to_home);
                }
            }
        });
        viewModel.getSaveErrorEvent().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void populateSummary() {
        binding.summaryBasicContainer.removeAllViews();
        addRow(binding.summaryBasicContainer, "Name", hike.getName());
        addRow(binding.summaryBasicContainer, "Location", hike.getLocation());
        addRow(binding.summaryBasicContainer, "Date", DateTimeUtils.formatDate(hike.getHikeDate()));
        addRow(binding.summaryBasicContainer, "Parking", hike.isParkingAvailable() ? "Yes" : "No");

        binding.summaryTrailContainer.removeAllViews();
        addRow(binding.summaryTrailContainer, "Length", hike.getLength() + " km");
        addRow(binding.summaryTrailContainer, "Difficulty", hike.getDifficulty());
        addRow(binding.summaryTrailContainer, "Estimated Duration",
                isEmpty(hike.getEstimatedDuration()) ? "—" : hike.getEstimatedDuration());
        addRow(binding.summaryTrailContainer, "Terrain Type", hike.getTerrainType());
    }

    private void addRow(LinearLayout container, String label, String value) {
        ItemConfirmRowBinding rowBinding = ItemConfirmRowBinding.inflate(getLayoutInflater(), container, false);
        rowBinding.labelText.setText(label);
        rowBinding.valueText.setText(value);
        container.addView(rowBinding.getRoot());
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
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
