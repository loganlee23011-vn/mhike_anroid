package com.example.mhikeandroid.utils;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.example.mhikeandroid.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps a hike's free-text terrainType field to one of the shared terrain photos (copied from
 * MHikeRN/assets/images/hikes/ into res/drawable-nodpi/). Mirrors MHikeRN's
 * src/constants/terrainImages.js getTerrainImage(), including its fallback-to-"other" behaviour
 * for any value that isn't one of the known types.
 */
public final class TerrainImages {

    private static final Map<String, Integer> IMAGES = new HashMap<>();

    static {
        IMAGES.put("mountain", R.drawable.terrain_mountain);
        IMAGES.put("forest", R.drawable.terrain_forest);
        IMAGES.put("coastal", R.drawable.terrain_coastal);
        IMAGES.put("valley", R.drawable.terrain_valley);
    }

    private TerrainImages() {
    }

    @DrawableRes
    public static int forTerrainType(@Nullable String terrainType) {
        if (terrainType == null) {
            return R.drawable.terrain_other;
        }
        Integer resId = IMAGES.get(terrainType.trim().toLowerCase());
        return resId != null ? resId : R.drawable.terrain_other;
    }
}
