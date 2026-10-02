package com.scrapstosavory.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.scrapstosavory.app.R;
import com.scrapstosavory.app.adapter.SuggestedRecipeAdapter;
import com.scrapstosavory.app.data.DatabaseHelper;
import com.scrapstosavory.app.data.PantryDao;
import com.scrapstosavory.app.data.RecipeDao;
import com.scrapstosavory.app.logic.StrictMatcher;
import com.scrapstosavory.app.model.DietTag;
import com.scrapstosavory.app.model.MealType;
import com.scrapstosavory.app.model.PantryItem;
import com.scrapstosavory.app.model.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Shows the recipes the user can actually cook right now, using the
 * strict matching rule against whatever is currently in the pantry.
 *
 * The chips at the top let the user narrow the list further by diet and
 * by meal, but they are just a filter on top of the strict matching
 * result. A recipe still has to pass strict matching first, chips only
 * ever remove recipes from what is already shown, never add extra ones.
 *
 * The list is reloaded every time this screen is shown, in onResume(),
 * so it always reflects the pantry as it stands right now, not whatever
 * it looked like the last time this screen was opened.
 */
public class SuggestedRecipesActivity extends BaseNavigationActivity
        implements SuggestedRecipeAdapter.OnRecipeClickListener {

    private PantryDao pantryDao;
    private RecipeDao recipeDao;
    private SuggestedRecipeAdapter adapter;
    private RecyclerView recyclerView;
    private TextView textEmptySuggestions;
    private ChipGroup chipGroupDiet;
    private ChipGroup chipGroupMeal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.suggested_recipes_title);
        setSupportActionBar(toolbar);
        setUpNavigationDrawer(toolbar, R.id.action_suggested_recipes);

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        pantryDao = new PantryDao(databaseHelper);
        recipeDao = new RecipeDao(databaseHelper);

        recyclerView = findViewById(R.id.recyclerSuggestedRecipes);
        textEmptySuggestions = findViewById(R.id.textEmptySuggestions);
        adapter = new SuggestedRecipeAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        chipGroupDiet = findViewById(R.id.chipGroupDiet);
        chipGroupMeal = findViewById(R.id.chipGroupMeal);
        setUpFilterChips();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSuggestions();
    }

    /** Adds one chip per DietTag and one per MealType, all unchecked to start with. */
    private void setUpFilterChips() {
        for (DietTag tag : DietTag.values()) {
            chipGroupDiet.addView(buildFilterChip(tag.getDisplayName(), tag));
        }
        for (MealType type : MealType.values()) {
            chipGroupMeal.addView(buildFilterChip(type.getDisplayName(), type));
        }
    }

    private Chip buildFilterChip(String label, Object tagValue) {
        Chip chip = new Chip(this);
        chip.setText(label);
        chip.setCheckable(true);
        chip.setTag(tagValue);
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> refreshSuggestions());
        return chip;
    }

    private void refreshSuggestions() {
        List<PantryItem> pantry = pantryDao.getAll();
        List<Recipe> allRecipes = recipeDao.getAll();

        List<Recipe> strictMatches = StrictMatcher.getSuggestedRecipes(allRecipes, pantry);
        List<Recipe> filtered = applyChipFilters(strictMatches);

        adapter.setRecipes(filtered);

        boolean isEmpty = filtered.isEmpty();
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        textEmptySuggestions.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    /**
     * Narrows the strict matches down using the chips the user has
     * checked. A recipe needs all of the checked diet chips (so
     * checking Vegan and High-Protein together asks for both at once),
     * but only one of the checked meal chips (so checking Breakfast and
     * Lunch shows either kind, not just recipes tagged as both).
     */
    private List<Recipe> applyChipFilters(List<Recipe> recipes) {
        List<DietTag> selectedDietTags = getSelectedTags(chipGroupDiet, DietTag.class);
        List<MealType> selectedMealTypes = getSelectedTags(chipGroupMeal, MealType.class);

        List<Recipe> result = new ArrayList<>();
        for (Recipe recipe : recipes) {
            boolean dietOk = selectedDietTags.isEmpty() || recipe.getDietTags().containsAll(selectedDietTags);
            boolean mealOk = selectedMealTypes.isEmpty()
                    || containsAnyOf(recipe.getMealTypes(), selectedMealTypes);
            if (dietOk && mealOk) {
                result.add(recipe);
            }
        }
        return result;
    }

    private <T> List<T> getSelectedTags(ChipGroup chipGroup, Class<T> tagType) {
        List<T> selected = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selected.add(tagType.cast(chip.getTag()));
            }
        }
        return selected;
    }

    private <T> boolean containsAnyOf(Set<T> values, List<T> lookingFor) {
        for (T value : lookingFor) {
            if (values.contains(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRecipeClicked(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
