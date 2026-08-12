package com.example.mhikeandroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mhikeandroid.R;
import com.example.mhikeandroid.databinding.FragmentHomeBinding;
import com.example.mhikeandroid.databinding.ItemHomeObservationBinding;
import com.example.mhikeandroid.databinding.ItemHomeStripCardBinding;
import com.example.mhikeandroid.models.Hike;
import com.example.mhikeandroid.utils.DateTimeUtils;
import com.example.mhikeandroid.utils.TerrainImages;
import com.example.mhikeandroid.viewmodels.HomeViewModel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding.greetingText.setText(viewModel.getGreeting());

        binding.viewAllText.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.hikesFragment));

        binding.logoutButton.setOnClickListener(v -> {
            viewModel.logout();
            NavOptions options = new NavOptions.Builder()
                    .setPopUpTo(R.id.loginFragment, true)
                    .build();
            NavHostFragment.findNavController(this).navigate(R.id.loginFragment, null, options);
        });

        binding.addHikeFab.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.entryFragment));
        binding.actionMyHikes.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.hikesFragment));
        binding.actionSearch.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.searchFragment));

        viewModel.getHikeCount().observe(getViewLifecycleOwner(),
                count -> binding.statHikesValue.setText(String.valueOf(count)));
        viewModel.getTotalDistanceKm().observe(getViewLifecycleOwner(),
                km -> binding.statKmValue.setText(String.format("%.1f", km)));
        viewModel.getCompletedCount().observe(getViewLifecycleOwner(),
                count -> binding.statCompletedValue.setText(String.valueOf(count)));

        viewModel.getUpcomingHike().observe(getViewLifecycleOwner(), this::bindUpcomingHike);
        viewModel.getUpcomingStrip().observe(getViewLifecycleOwner(), this::bindStrip);
        viewModel.getRecentObservations().observe(getViewLifecycleOwner(), this::bindObservations);
    }

    private void bindUpcomingHike(@Nullable Hike hike) {
        boolean hasUpcoming = hike != null;
        binding.upcomingHikeCard.setVisibility(hasUpcoming ? View.VISIBLE : View.GONE);
        binding.upcomingEmptyText.setVisibility(hasUpcoming ? View.GONE : View.VISIBLE);
        if (!hasUpcoming) {
            return;
        }
        binding.upcomingImage.setImageResource(TerrainImages.forTerrainType(hike.getTerrainType()));
        binding.upcomingBadgeText.setText(daysUntilLabel(hike.getHikeDate()));
        binding.upcomingTitleText.setText(hike.getName());
        binding.upcomingCaptionText.setText(String.format("%s · %.1f km · %s",
                DateTimeUtils.formatDate(hike.getHikeDate()), hike.getLength(), hike.getDifficulty()));
        binding.upcomingHikeCard.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("hikeId", hike.getId());
            NavHostFragment.findNavController(this).navigate(R.id.detailFragment, args);
        });
    }

    private void bindStrip(List<Hike> hikes) {
        binding.stripContainer.removeAllViews();
        for (Hike hike : hikes) {
            ItemHomeStripCardBinding cardBinding = ItemHomeStripCardBinding.inflate(
                    getLayoutInflater(), binding.stripContainer, false);
            cardBinding.stripImage.setImageResource(TerrainImages.forTerrainType(hike.getTerrainType()));
            cardBinding.stripTitle.setText(hike.getName());
            cardBinding.stripCaption.setText(String.format("%.1f km", hike.getLength()));
            cardBinding.getRoot().setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("hikeId", hike.getId());
                NavHostFragment.findNavController(this).navigate(R.id.detailFragment, args);
            });
            binding.stripContainer.addView(cardBinding.getRoot());
        }
    }

    private void bindObservations(List<HomeViewModel.RecentObservation> observations) {
        binding.observationsContainer.removeAllViews();
        binding.observationsEmptyText.setVisibility(observations.isEmpty() ? View.VISIBLE : View.GONE);
        for (HomeViewModel.RecentObservation item : observations) {
            ItemHomeObservationBinding rowBinding = ItemHomeObservationBinding.inflate(
                    getLayoutInflater(), binding.observationsContainer, false);
            rowBinding.hikeNameText.setText(item.hikeName);
            rowBinding.observedAtText.setText(DateTimeUtils.formatDate(item.observation.getObservedAt()));
            rowBinding.observationTextView.setText(item.observation.getObservationText());
            rowBinding.getRoot().setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("hikeId", item.observation.getHikeId());
                args.putString("hikeName", item.hikeName);
                NavHostFragment.findNavController(this).navigate(R.id.observationsFragment, args);
            });
            binding.observationsContainer.addView(rowBinding.getRoot());
        }
    }

    private static String daysUntilLabel(@Nullable Date hikeDate) {
        if (hikeDate == null) {
            return "";
        }
        Calendar hikeDay = Calendar.getInstance();
        hikeDay.setTime(hikeDate);
        hikeDay.set(Calendar.HOUR_OF_DAY, 0);
        hikeDay.set(Calendar.MINUTE, 0);
        hikeDay.set(Calendar.SECOND, 0);
        hikeDay.set(Calendar.MILLISECOND, 0);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        long diffDays = Math.round((hikeDay.getTimeInMillis() - today.getTimeInMillis()) / 86400000.0);
        if (diffDays <= 0) return "Today";
        if (diffDays == 1) return "Tomorrow";
        return "In " + diffDays + " days";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
