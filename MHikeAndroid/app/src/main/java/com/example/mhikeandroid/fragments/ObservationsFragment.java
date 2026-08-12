package com.example.mhikeandroid.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mhikeandroid.R;
import com.example.mhikeandroid.adapters.ObservationAdapter;
import com.example.mhikeandroid.databinding.FragmentObservationsBinding;
import com.example.mhikeandroid.models.Observation;
import com.example.mhikeandroid.utils.DateTimeUtils;
import com.example.mhikeandroid.viewmodels.ObservationsViewModel;

import java.util.Calendar;
import java.util.Date;

/** List + inline add/edit editor in one screen (MHikeRN's ObservationScreen.js). */
public class ObservationsFragment extends Fragment {

    private FragmentObservationsBinding binding;
    private ObservationsViewModel viewModel;
    private ObservationAdapter adapter;
    private String hikeId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentObservationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ObservationsViewModel.class);

        Bundle args = requireArguments();
        hikeId = args.getString("hikeId");
        String hikeName = args.getString("hikeName");
        if (hikeName != null) {
            binding.toolbar.setTitle(hikeName);
        }

        Drawable backIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_back);
        if (backIcon != null) {
            backIcon = DrawableCompat.wrap(backIcon.mutate());
            DrawableCompat.setTint(backIcon, ContextCompat.getColor(requireContext(), R.color.mhike_text_primary));
        }
        binding.toolbar.setNavigationIcon(backIcon);
        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        // Back arrow steps back to this hike's Detail screen (standard "return to where you
        // came from"); this toolbar action is a shortcut straight to Home, since reaching Home
        // otherwise takes 2+ back presses (Observations -> Detail -> Hikes -> Home tab).
        binding.toolbar.inflateMenu(R.menu.observations_menu);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_go_home) {
                NavOptions options = new NavOptions.Builder()
                        .setPopUpTo(R.id.loginFragment, true)
                        .build();
                NavHostFragment.findNavController(this).navigate(R.id.homeFragment, null, options);
                return true;
            }
            return false;
        });

        adapter = new ObservationAdapter(new ObservationAdapter.OnObservationActionListener() {
            @Override
            public void onEdit(Observation observation) {
                viewModel.startEdit(observation);
            }

            @Override
            public void onDelete(Observation observation) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.delete_observation_dialog_title)
                        .setMessage(R.string.delete_observation_dialog_message)
                        .setPositiveButton(R.string.action_delete,
                                (dialog, which) -> viewModel.deleteObservation(observation))
                        .setNegativeButton(R.string.action_cancel, null)
                        .show();
            }
        });
        binding.observationsRecyclerView.setAdapter(adapter);

        binding.addObservationFab.setOnClickListener(v -> viewModel.startAdd());
        binding.cancelEditorButton.setOnClickListener(v -> viewModel.closeEditor());
        binding.saveObservationButton.setOnClickListener(v -> viewModel.save(
                binding.observationTextInput.getText().toString().trim(),
                binding.commentsInput.getText().toString().trim()));
        binding.observedAtRow.setOnClickListener(v -> showDateTimePicker());

        viewModel.init(hikeId);
        adapter.setPermissions(viewModel.getCurrentUserId(), false);
        viewModel.getIsHikeOwner().observe(getViewLifecycleOwner(),
                owner -> adapter.setPermissions(viewModel.getCurrentUserId(), owner));
        viewModel.getObservations().observe(getViewLifecycleOwner(), observations -> {
            adapter.submitList(observations);
            binding.emptyStateText.setVisibility(observations.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getAdding().observe(getViewLifecycleOwner(), adding -> {
            binding.editorContainer.setVisibility(adding ? View.VISIBLE : View.GONE);
            binding.addObservationFab.setVisibility(adding ? View.GONE : View.VISIBLE);
        });

        viewModel.getPrefillEvent().observe(getViewLifecycleOwner(), event -> {
            if (event.getContentIfNotHandled() == null) {
                return;
            }
            Observation editing = viewModel.getEditingObservation();
            binding.observationTextInput.setText(editing != null ? editing.getObservationText() : "");
            binding.commentsInput.setText(editing != null ? editing.getComments() : "");
            binding.observationTextInput.requestFocus();
        });

        viewModel.getObservedAt().observe(getViewLifecycleOwner(),
                date -> binding.observedAtText.setText(DateTimeUtils.formatDateTime(date)));

        viewModel.getObservationTextError().observe(getViewLifecycleOwner(),
                error -> binding.observationTextLayout.setError(error));

        viewModel.getSaving().observe(getViewLifecycleOwner(), saving -> {
            binding.progressIndicator.setVisibility(saving ? View.VISIBLE : View.GONE);
            binding.saveObservationButton.setEnabled(!saving);
            binding.cancelEditorButton.setEnabled(!saving);
        });
        viewModel.getSaveErrorEvent().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
        viewModel.getDeleteErrorEvent().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        Date current = viewModel.getObservedAt().getValue();
        if (current != null) {
            calendar.setTime(current);
        }
        new DatePickerDialog(requireContext(), (picker, year, month, dayOfMonth) -> {
            Date datePart = DateTimeUtils.fromYmd(year, month, dayOfMonth);
            new TimePickerDialog(requireContext(), (timePicker, hourOfDay, minute) ->
                    viewModel.setObservedAt(DateTimeUtils.withTime(datePart, hourOfDay, minute)),
                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
                    .show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
