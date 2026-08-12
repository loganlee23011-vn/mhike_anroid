package com.example.mhikeandroid.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mhikeandroid.R;
import com.example.mhikeandroid.adapters.HikeAdapter;
import com.example.mhikeandroid.databinding.FragmentSearchBinding;
import com.example.mhikeandroid.utils.DateTimeUtils;
import com.example.mhikeandroid.viewmodels.SearchViewModel;

import java.util.Calendar;
import java.util.function.Consumer;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private SearchViewModel viewModel;
    private HikeAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        adapter = new HikeAdapter(hike -> {
            Bundle args = new Bundle();
            args.putString("hikeId", hike.getId());
            NavHostFragment.findNavController(this).navigate(R.id.action_search_to_detail, args);
        });
        binding.resultsRecyclerView.setAdapter(adapter);

        watch(binding.nameQueryInput, viewModel::setNameQuery);
        watch(binding.locationFilterInput, viewModel::setLocationFilter);
        watch(binding.minLengthInput, text -> viewModel.setMinLength(parseOrNull(text)));
        watch(binding.maxLengthInput, text -> viewModel.setMaxLength(parseOrNull(text)));

        binding.difficultyChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                com.google.android.material.chip.Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    viewModel.setDifficultyFilter(chip.getText().toString());
                }
            }
        });

        binding.dateFromInput.setOnClickListener(v -> showDatePicker(viewModel::setDateFrom));
        binding.dateToInput.setOnClickListener(v -> showDatePicker(viewModel::setDateTo));

        binding.resetFiltersText.setOnClickListener(v -> {
            binding.nameQueryInput.setText("");
            binding.locationFilterInput.setText("");
            binding.minLengthInput.setText("");
            binding.maxLengthInput.setText("");
            binding.difficultyChipGroup.check(binding.chipAll.getId());
            viewModel.resetFilters();
        });

        binding.applyFiltersButton.setOnClickListener(v -> viewModel.applyFilters());

        viewModel.getDateFrom().observe(getViewLifecycleOwner(), date ->
                binding.dateFromInput.setText(date != null ? DateTimeUtils.formatDate(date) : ""));
        viewModel.getDateTo().observe(getViewLifecycleOwner(), date ->
                binding.dateToInput.setText(date != null ? DateTimeUtils.formatDate(date) : ""));

        viewModel.getResults().observe(getViewLifecycleOwner(), hikes -> {
            adapter.submitList(hikes);
            boolean hasSearched = Boolean.TRUE.equals(viewModel.getHasSearched().getValue());
            binding.emptyStateText.setVisibility(hasSearched && hikes.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getHasSearched().observe(getViewLifecycleOwner(), hasSearched -> {
            boolean empty = adapter.getCurrentList().isEmpty();
            binding.emptyStateText.setVisibility(hasSearched && empty ? View.VISIBLE : View.GONE);
        });
    }

    private void showDatePicker(Consumer<java.util.Date> onPicked) {
        Calendar calendar = Calendar.getInstance();
        new android.app.DatePickerDialog(requireContext(), (picker, year, month, dayOfMonth) ->
                onPicked.accept(DateTimeUtils.fromYmd(year, month, dayOfMonth)),
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private static Double parseOrNull(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void watch(android.widget.EditText editText, Consumer<String> onChange) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                onChange.accept(s.toString());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
