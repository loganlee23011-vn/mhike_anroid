package com.example.mhikeandroid.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.mhikeandroid.R;
import com.example.mhikeandroid.repositories.AuthRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Single Activity host: NavHostFragment + BottomNavigationView per CLAUDE.md architecture.
 * Bottom nav is hidden on auth / confirm / observation-entry destinations since those
 * are not top-level tabs.
 */
public class MainActivity extends AppCompatActivity {

    private static final int[] BOTTOM_NAV_DESTINATIONS = {
            R.id.homeFragment, R.id.entryFragment, R.id.hikesFragment, R.id.searchFragment
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        NavController navController = ((NavHostFragment) navHostFragment).getNavController();

        if (new AuthRepository().getCurrentUser() != null) {
            NavOptions options = new NavOptions.Builder()
                    .setPopUpTo(R.id.loginFragment, true)
                    .build();
            navController.navigate(R.id.homeFragment, null, options);
        }

        NavigationUI.setupWithNavController(bottomNav, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean isTopLevel = false;
            for (int id : BOTTOM_NAV_DESTINATIONS) {
                if (destination.getId() == id) {
                    isTopLevel = true;
                    break;
                }
            }
            bottomNav.setVisibility(isTopLevel ? android.view.View.VISIBLE : android.view.View.GONE);
        });
    }
}
