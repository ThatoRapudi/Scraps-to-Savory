package com.scrapstosavory.app.ui;

import android.content.Intent;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.scrapstosavory.app.R;

/**
 * Shared base class for the app's main screens (Pantry List, Suggested
 * Recipes and Settings). It sets up the side navigation drawer so the
 * user can jump between these screens from anywhere, instead of only
 * being able to go back the way they came.
 *
 * Screens reached by drilling into something, such as Add/Edit Ingredient
 * or Recipe Detail, do not extend this class. They use a normal back
 * arrow instead, since they are not top level screens.
 */
public abstract class BaseNavigationActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    /**
     * Wires up the DrawerLayout and NavigationView so tapping the
     * hamburger icon on the toolbar opens and closes the drawer, and
     * tapping an item in it opens the right screen. selectedItemId is
     * shown as already checked.
     *
     * Call this from onCreate(), after setContentView() and after the
     * toolbar has been set as the support action bar, since the screen's
     * layout must already include a DrawerLayout (id drawerLayout) with
     * a NavigationView inside it (id navigationView).
     */
    protected void setUpNavigationDrawer(Toolbar toolbar, int selectedItemId) {
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setCheckedItem(selectedItemId);
        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
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

    @Override
    public void onBackPressed() {
        // Closing the drawer first means pressing back while it's open
        // just closes it, instead of leaving the screen entirely.
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
