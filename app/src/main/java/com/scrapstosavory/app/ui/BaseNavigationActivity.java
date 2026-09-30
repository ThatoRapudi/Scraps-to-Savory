package com.scrapstosavory.app.ui;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.scrapstosavory.app.R;

/**
 * Shared base class for the app's main screens (Pantry List, Suggested
 * Recipes and Settings). It adds a toolbar menu so the user can jump
 * between these screens from anywhere, instead of only being able to go
 * back the way they came.
 *
 * Screens reached by drilling into something, such as Add/Edit Ingredient
 * or Recipe Detail, do not extend this class. They use a normal back
 * arrow instead, since they are not top level screens.
 */
public abstract class BaseNavigationActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_nav, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_pantry) {
            openScreen(PantryListActivity.class);
            return true;
        }
        if (id == R.id.action_suggested_recipes) {
            openScreen(SuggestedRecipesActivity.class);
            return true;
        }
        if (id == R.id.action_settings) {
            openScreen(SettingsActivity.class);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /** Opens another main screen, unless the user is already on it. */
    private void openScreen(Class<?> activityClass) {
        if (!getClass().equals(activityClass)) {
            startActivity(new Intent(this, activityClass));
        }
    }
}
