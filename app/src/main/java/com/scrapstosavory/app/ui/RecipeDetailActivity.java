package com.scrapstosavory.app.ui;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.scrapstosavory.app.R;
import com.scrapstosavory.app.data.DatabaseHelper;
import com.scrapstosavory.app.data.PantryDao;
import com.scrapstosavory.app.data.RecipeDao;
import com.scrapstosavory.app.logic.StrictMatcher;
import com.scrapstosavory.app.logic.UnitConverter;
import com.scrapstosavory.app.model.DietTag;
import com.scrapstosavory.app.model.MealType;
import com.scrapstosavory.app.model.PantryItem;
import com.scrapstosavory.app.model.Recipe;
import com.scrapstosavory.app.model.RecipeIngredient;
import com.scrapstosavory.app.util.QuantityUtils;

import java.util.List;

/**
 * Shows everything about one recipe: its name, its full ingredient list
 * with quantities, and the steps to make it. Opened from the Suggested
 * Recipes screen by tapping a recipe, with the recipe's id passed in
 * through EXTRA_RECIPE_ID.
 *
 * Also lets the user mark the recipe as made, which deducts each
 * ingredient's amount from the matching pantry item so the pantry stays
 * accurate without the user needing to remember to update it by hand,
 * and leave a thumbs up or thumbs down rating with an optional note so
 * they can remember whether it was worth making again. Both of these
 * are saved through PantryDao and RecipeDao respectively.
 *
 * This is a detail screen, not one of the app's main screens, so it uses
 * a normal back arrow rather than the shared navigation menu.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "com.scrapstosavory.app.EXTRA_RECIPE_ID";

    private PantryDao pantryDao;
    private RecipeDao recipeDao;
    private Recipe recipe;
    private Boolean selectedLiked; // true = thumbs up, false = thumbs down, null = not rated

    private TextView buttonThumbsUp;
    private TextView buttonThumbsDown;
    private EditText editRecipeNote;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.recipe_detail_title);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        pantryDao = new PantryDao(databaseHelper);
        recipeDao = new RecipeDao(databaseHelper);
        recipe = recipeDao.getById(recipeId);

        if (recipe == null) {
            Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showRecipe(recipe);
        setUpFeedback();

        findViewById(R.id.buttonMarkAsMade).setOnClickListener(v -> onMarkAsMadeClicked());
    }

    /**
     * Deducts each ingredient this recipe needs from the pantry item that
     * covers it, the same way StrictMatcher decided the recipe could be
     * made in the first place. Refuses to touch the pantry at all if the
     * recipe is no longer fully makeable (the pantry may have changed
     * since this screen was opened), rather than leaving some ingredients
     * deducted and others not.
     */
    private void onMarkAsMadeClicked() {
        List<PantryItem> pantry = pantryDao.getAll();

        if (!StrictMatcher.canMake(recipe, pantry)) {
            Toast.makeText(this, R.string.recipe_mark_as_made_insufficient, Toast.LENGTH_SHORT).show();
            return;
        }

        for (RecipeIngredient needed : recipe.getIngredients()) {
            PantryItem coveringItem = StrictMatcher.findCoveringItem(needed, pantry);
            if (coveringItem == null) {
                continue; // should not happen since canMake() just confirmed every ingredient is covered
            }

            Double neededInPantryUnit = UnitConverter.convert(needed.getQuantity(), needed.getUnit(), coveringItem.getUnit());
            if (neededInPantryUnit == null) {
                continue;
            }

            double remaining = Math.max(0, coveringItem.getQuantity() - neededInPantryUnit);
            coveringItem.setQuantity(remaining);
            pantryDao.update(coveringItem);
        }

        Toast.makeText(this, R.string.recipe_mark_as_made_success, Toast.LENGTH_SHORT).show();
    }

    private void showRecipe(Recipe recipe) {
        ((TextView) findViewById(R.id.textRecipeName)).setText(recipe.getName());
        ((TextView) findViewById(R.id.textRecipeTags)).setText(buildTagsText(recipe));
        ((TextView) findViewById(R.id.textIngredientsList)).setText(buildIngredientsText(recipe));
        ((TextView) findViewById(R.id.textSteps)).setText(recipe.getSteps());
    }

    /** Wires up the thumbs up/down toggle buttons, note field, and save button. */
    private void setUpFeedback() {
        buttonThumbsUp = findViewById(R.id.buttonThumbsUp);
        buttonThumbsDown = findViewById(R.id.buttonThumbsDown);
        editRecipeNote = findViewById(R.id.editRecipeNote);

        selectedLiked = recipe.getLiked();
        if (recipe.getNote() != null) {
            editRecipeNote.setText(recipe.getNote());
        }
        refreshFeedbackButtons();

        buttonThumbsUp.setOnClickListener(v -> {
            selectedLiked = Boolean.TRUE.equals(selectedLiked) ? null : true;
            refreshFeedbackButtons();
        });

        buttonThumbsDown.setOnClickListener(v -> {
            selectedLiked = Boolean.FALSE.equals(selectedLiked) ? null : false;
            refreshFeedbackButtons();
        });

        findViewById(R.id.buttonSaveFeedback).setOnClickListener(v -> onSaveFeedbackClicked());
    }

    /** Fills in the thumb that matches selectedLiked, leaving the other one plain. */
    private void refreshFeedbackButtons() {
        boolean isThumbsUp = Boolean.TRUE.equals(selectedLiked);
        boolean isThumbsDown = Boolean.FALSE.equals(selectedLiked);

        buttonThumbsUp.setBackgroundResource(isThumbsUp
                ? R.drawable.bg_feedback_selected : R.drawable.bg_feedback_unselected);
        buttonThumbsDown.setBackgroundResource(isThumbsDown
                ? R.drawable.bg_feedback_selected : R.drawable.bg_feedback_unselected);
    }

    private void onSaveFeedbackClicked() {
        String noteText = editRecipeNote.getText() != null
                ? editRecipeNote.getText().toString().trim() : "";

        recipeDao.updateFeedback(recipe.getId(), selectedLiked, noteText);
        Toast.makeText(this, R.string.recipe_feedback_saved, Toast.LENGTH_SHORT).show();
    }

    private String buildTagsText(Recipe recipe) {
        StringBuilder text = new StringBuilder();
        for (DietTag tag : recipe.getDietTags()) {
            appendWithSeparator(text, tag.getDisplayName());
        }
        for (MealType type : recipe.getMealTypes()) {
            appendWithSeparator(text, type.getDisplayName());
        }
        return text.toString();
    }

    private String buildIngredientsText(Recipe recipe) {
        StringBuilder text = new StringBuilder();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            text.append("• ")
                    .append(QuantityUtils.format(ingredient.getQuantity()))
                    .append(" ")
                    .append(ingredient.getUnit())
                    .append(" ")
                    .append(ingredient.getName())
                    .append("\n");
        }
        return text.toString().trim();
    }

    private void appendWithSeparator(StringBuilder text, String value) {
        if (text.length() > 0) {
            text.append("  •  ");
        }
        text.append(value);
    }
}
