package com.example.mhikeandroid.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mhikeandroid.R;
import com.example.mhikeandroid.adapters.HikeAdapter;
import com.example.mhikeandroid.databinding.FragmentHikesBinding;
import com.example.mhikeandroid.viewmodels.HikesViewModel;
import com.google.android.material.button.MaterialButton;

public class HikesFragment extends Fragment {

    private FragmentHikesBinding binding;
    private HikesViewModel viewModel;
    private HikeAdapter adapter;
    private int upcomingCount;
    private int completedCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentHikesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HikesViewModel.class);

        adapter = new HikeAdapter(hike -> {
            Bundle args = new Bundle();
            args.putString("hikeId", hike.getId());
            NavHostFragment.findNavController(this).navigate(R.id.action_hikes_to_detail, args);
        });
        adapter.setCurrentUserId(viewModel.getCurrentUserId());
        binding.hikesRecyclerView.setAdapter(adapter);

        binding.toolbar.inflateMenu(R.menu.hikes_menu);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_reset_database) {
                confirmResetDatabase();
                return true;
            }
            return false;
        });

        binding.addHikeFab.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_hikes_to_entry));

        binding.segmentUpcoming.setOnClickListener(v -> viewModel.setTab(true));
        binding.segmentCompleted.setOnClickListener(v -> viewModel.setTab(false));

        viewModel.getUpcomingCount().observe(getViewLifecycleOwner(), count -> {
            upcomingCount = count;
            updateSegmentLabels();
        });
        viewModel.getCompletedCount().observe(getViewLifecycleOwner(), count -> {
            completedCount = count;
            updateSegmentLabels();
        });
        updateSegmentStyle();

        viewModel.getFilteredHikes().observe(getViewLifecycleOwner(), hikes -> {
            adapter.submitList(hikes);
            binding.emptyStateText.setVisibility(hikes.isEmpty() ? View.VISIBLE : View.GONE);
            updateSegmentStyle();
        });

        viewModel.getResetSuccessEvent().observe(getViewLifecycleOwner(), event -> {
            if (event.getContentIfNotHandled() != null) {
                Toast.makeText(requireContext(), R.string.reset_dialog_title, Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.getResetErrorEvent().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateSegmentLabels() {
        binding.segmentUpcoming.setText(String.format("Upcoming (%d)", upcomingCount));
        binding.segmentCompleted.setText(String.format("Completed (%d)", completedCount));
    }

    /** Toggles the pill style: active tab = filled primary bg + white text, matching RN's
     *  segment/segmentActive style in HikesScreen.js. */
    private void updateSegmentStyle() {
        boolean upcoming = viewModel.isUpcomingTab();
        styleSegment(binding.segmentUpcoming, upcoming);
        styleSegment(binding.segmentCompleted, !upcoming);
    }

    private void styleSegment(MaterialButton button, boolean active) {
        int primary = ContextCompat.getColor(requireContext(), R.color.mhike_primary);
        int onPrimary = ContextCompat.getColor(requireContext(), R.color.mhike_on_primary);
        int surface = ContextCompat.getColor(requireContext(), R.color.mhike_surface);
        int textPrimary = ContextCompat.getColor(requireContext(), R.color.mhike_text_primary);
        if (active) {
            button.setBackgroundTintList(ColorStateList.valueOf(primary));
            button.setTextColor(onPrimary);
            button.setStrokeColor(ColorStateList.valueOf(primary));
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(surface));
            button.setTextColor(textPrimary);
            button.setStrokeColor(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.mhike_outline)));
        }
    }

    private void confirmResetDatabase() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.reset_dialog_title)
                .setMessage(R.string.reset_dialog_message)
                .setPositiveButton(R.string.action_reset, (dialog, which) -> viewModel.resetDatabase())
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
