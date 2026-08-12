package com.example.mhikeandroid.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mhikeandroid.databinding.ItemHikeBinding;
import com.example.mhikeandroid.models.Hike;
import com.example.mhikeandroid.utils.DateTimeUtils;
import com.example.mhikeandroid.utils.TerrainImages;

import java.util.Objects;

public class HikeAdapter extends ListAdapter<Hike, HikeAdapter.HikeViewHolder> {

    public interface OnHikeClickListener {
        void onHikeClick(Hike hike);
    }

    private final OnHikeClickListener listener;
    private String currentUserId;

    public HikeAdapter(OnHikeClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    /** Lets HikesFragment tell this adapter which hikes are the signed-in user's own, so items
     *  owned by someone else can show a "Shared" tag (mirrors HikesScreen.js's sharedTag). */
    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HikeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHikeBinding binding = ItemHikeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new HikeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HikeViewHolder holder, int position) {
        Hike hike = getItem(position);
        boolean isMine = currentUserId != null && currentUserId.equals(hike.getUserId());
        holder.bind(hike, listener, isMine);
    }

    static class HikeViewHolder extends RecyclerView.ViewHolder {
        private final ItemHikeBinding binding;

        HikeViewHolder(ItemHikeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Hike hike, OnHikeClickListener listener, boolean isMine) {
            binding.thumbnailImage.setImageResource(TerrainImages.forTerrainType(hike.getTerrainType()));
            binding.nameText.setText(hike.getName());
            binding.sharedTagText.setVisibility(isMine ? View.GONE : View.VISIBLE);
            binding.locationText.setText(hike.getLocation());
            binding.detailText.setText(String.format("%s · %.1f km · %s",
                    DateTimeUtils.formatDate(hike.getHikeDate()), hike.getLength(), hike.getDifficulty()));
            binding.getRoot().setOnClickListener(v -> listener.onHikeClick(hike));
        }
    }

    private static final DiffUtil.ItemCallback<Hike> DIFF_CALLBACK = new DiffUtil.ItemCallback<Hike>() {
        @Override
        public boolean areItemsTheSame(@NonNull Hike oldItem, @NonNull Hike newItem) {
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Hike oldItem, @NonNull Hike newItem) {
            return Objects.equals(oldItem.getName(), newItem.getName())
                    && Objects.equals(oldItem.getLocation(), newItem.getLocation())
                    && Objects.equals(oldItem.getHikeDate(), newItem.getHikeDate())
                    && oldItem.getLength() == newItem.getLength()
                    && Objects.equals(oldItem.getDifficulty(), newItem.getDifficulty())
                    && oldItem.isParkingAvailable() == newItem.isParkingAvailable();
        }
    };
}
