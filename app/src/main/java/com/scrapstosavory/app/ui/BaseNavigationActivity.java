package com.scrapstosavory.app.ui;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.scrapstosavory.app.R;

/**
 * Shared base class for the app's main screens (Pantry List, Suggested
 * Recipes and Settings). It sets up the bottom navigation bar so the
 * user can jump between these screens from anywhere, instead of only
 * being able to go back the way they came.
 *
 * Screens reached by drilling into something, such as Add/Edit Ingredient
 * or Recipe Detail, do not extend this class. They use a normal back
 * arrow instead, since they are not top level screens.
 */
public abstract class BaseNavigationActivity extends AppCompatActivity {

    /**
     * Wires up the BottomNavigationView so it opens the right screen
     * when tapped, and shows selectedItemId as already selected.
     *
     * Call this from onCreate(), after setContentView(), since the
     * screen's layout must already include a BottomNavigationView with
     * id bottomNavigation for findViewById() to find it.
     */
    protected void setUpBottomNavigation(int selectedItemId) {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(selectedItemId);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == selectedItemId) {
                return true;
            }
            if (id == R.id.action_pantry) {
                startActivity(new Intent(this, PantryListActivity.class));
                return true;
            }
            if (id == R.id.action_suggested_recipes) {
                startActivity(new Intent(this, SuggestedRecipesActivity.class));
                return true;
            }
            if (id == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }
}
