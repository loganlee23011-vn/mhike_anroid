package com.example.mhikeandroid.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mhikeandroid.R;
import com.example.mhikeandroid.databinding.ItemObservationBinding;
import com.example.mhikeandroid.models.Observation;
import com.example.mhikeandroid.utils.DateTimeUtils;

import java.util.Objects;

public class ObservationAdapter extends ListAdapter<Observation, ObservationAdapter.ObservationViewHolder> {

    public interface OnObservationActionListener {
        void onEdit(Observation observation);

        void onDelete(Observation observation);
    }

    private final OnObservationActionListener listener;
    private String currentUserId;
    private boolean isHikeOwner;

    public ObservationAdapter(OnObservationActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    /** Author-or-hike-owner may manage (delete); only the author may edit. Re-binds visible
     *  rows since this can resolve after the hike-owner lookup completes. */
    public void setPermissions(String currentUserId, boolean isHikeOwner) {
        this.currentUserId = currentUserId;
        this.isHikeOwner = isHikeOwner;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ObservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemObservationBinding binding = ItemObservationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ObservationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ObservationViewHolder holder, int position) {
        Observation observation = getItem(position);
        boolean isAuthor = currentUserId != null && currentUserId.equals(observation.getUserId());
        holder.bind(observation, listener, isAuthor, isAuthor || isHikeOwner);
    }

    static class ObservationViewHolder extends RecyclerView.ViewHolder {
        private final ItemObservationBinding binding;

        ObservationViewHolder(ItemObservationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Observation observation, OnObservationActionListener listener, boolean isAuthor, boolean canManage) {
            binding.observedAtText.setText(DateTimeUtils.formatDateTime(observation.getObservedAt()));
            binding.observationTextView.setText(observation.getObservationText());
            boolean hasComments = observation.getComments() != null && !observation.getComments().trim().isEmpty();
            binding.commentsText.setVisibility(hasComments ? View.VISIBLE : View.GONE);
            binding.commentsText.setText(observation.getComments());

            // Not the author and not this hike's owner: nothing to manage, hide the menu entirely
            // (mirrors ObservationScreen.js's `item.userId === uid || isHikeOwner` gate).
            binding.moreButton.setVisibility(canManage ? View.VISIBLE : View.GONE);
            binding.moreButton.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.inflate(R.menu.observation_item_menu);
                // A hike owner moderating another author's note may only delete it, not edit the text.
                popup.getMenu().findItem(R.id.action_edit_observation).setVisible(isAuthor);
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_edit_observation) {
                        listener.onEdit(observation);
                        return true;
                    } else if (item.getItemId() == R.id.action_delete_observation) {
                        listener.onDelete(observation);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }
    }

    private static final DiffUtil.ItemCallback<Observation> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Observation>() {
                @Override
                public boolean areItemsTheSame(@NonNull Observation oldItem, @NonNull Observation newItem) {
                    return Objects.equals(oldItem.getId(), newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Observation oldItem, @NonNull Observation newItem) {
                    return Objects.equals(oldItem.getObservationText(), newItem.getObservationText())
                            && Objects.equals(oldItem.getObservedAt(), newItem.getObservedAt())
                            && Objects.equals(oldItem.getComments(), newItem.getComments());
                }
            };
}
