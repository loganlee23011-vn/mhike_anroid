package com.example.mhikeandroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mhikeandroid.R;
import com.example.mhikeandroid.databinding.FragmentDetailBinding;
import com.example.mhikeandroid.databinding.ItemDetailRowBinding;
import com.example.mhikeandroid.models.Hike;
import com.example.mhikeandroid.utils.DateTimeUtils;
import com.example.mhikeandroid.utils.TerrainImages;
import com.example.mhikeandroid.viewmodels.DetailViewModel;

public class DetailFragment extends Fragment {

    private FragmentDetailBinding binding;
    private DetailViewModel viewModel;
    private String hikeId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);
        hikeId = requireArguments().getString("hikeId");

        binding.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        binding.editButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("hikeId", hikeId);
            NavHostFragment.findNavController(this).navigate(R.id.action_detail_to_entry, args);
        });
        binding.deleteButton.setOnClickListener(v -> confirmDelete());
        binding.viewObservationsButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("hikeId", hikeId);
            Hike currentHike = viewModel.getHike().getValue();
            if (currentHike != null) {
                args.putString("hikeName", currentHike.getName());
            }
            NavHostFragment.findNavController(this).navigate(R.id.action_detail_to_observations, args);
        });

        viewModel.loadHike(hikeId);
        viewModel.getHike().observe(getViewLifecycleOwner(), this::populate);

        viewModel.getDeleteSuccessEvent().observe(getViewLifecycleOwner(), event -> {
            if (event.getContentIfNotHandled() != null) {
                NavHostFragment.findNavController(this).popBackStack();
            }
        });
        viewModel.getDeleteErrorEvent().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void populate(Hike hike) {
        if (hike == null) {
            return;
        }
        boolean isOwner = viewModel.isOwner(hike);
        binding.editButton.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        binding.deleteButton.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        binding.ownerBadgeText.setText(isOwner
                ? R.string.detail_owner_badge_mine
                : R.string.detail_owner_badge_shared);

        binding.heroImage.setImageResource(TerrainImages.forTerrainType(hike.getTerrainType()));
        binding.heroTitleText.setText(hike.getName());
        binding.heroCaptionText.setText(String.format("%s · %s",
                DateTimeUtils.formatDate(hike.getHikeDate()), hike.getDifficulty()));
        binding.summaryContainer.removeAllViews();
        addRow(R.drawable.ic_place, "Location", hike.getLocation());
        addRow(R.drawable.ic_event, "Date", DateTimeUtils.formatDate(hike.getHikeDate()));
        addRow(R.drawable.ic_straighten, "Length", hike.getLength() + " km");
        addRow(R.drawable.ic_bar_chart, "Difficulty", hike.getDifficulty());
        if (!isEmpty(hike.getEstimatedDuration())) {
            addRow(R.drawable.ic_schedule, "Estimated duration", hike.getEstimatedDuration());
        }
        if (!isEmpty(hike.getTerrainType())) {
            addRow(R.drawable.ic_terrain, "Terrain type", hike.getTerrainType());
        }
        addRow(R.drawable.ic_local_parking, "Parking available", hike.isParkingAvailable() ? "Yes" : "No");

        boolean hasDescription = !isEmpty(hike.getDescription());
        binding.descriptionTitle.setVisibility(hasDescription ? View.VISIBLE : View.GONE);
        binding.descriptionCard.setVisibility(hasDescription ? View.VISIBLE : View.GONE);
        binding.descriptionText.setText(hike.getDescription());
    }

    private void addRow(int iconRes, String label, String value) {
        ItemDetailRowBinding rowBinding = ItemDetailRowBinding.inflate(
                getLayoutInflater(), binding.summaryContainer, false);
        rowBinding.rowIcon.setImageResource(iconRes);
        rowBinding.labelText.setText(label);
        rowBinding.valueText.setText(value);
        binding.summaryContainer.addView(rowBinding.getRoot());
    }

    private void confirmDelete() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_dialog_title)
                .setMessage(R.string.delete_dialog_message)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> viewModel.deleteHike(hikeId))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
