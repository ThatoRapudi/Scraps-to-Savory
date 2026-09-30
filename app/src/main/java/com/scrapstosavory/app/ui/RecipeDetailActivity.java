package com.scrapstosavory.app.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.scrapstosavory.app.R;
import com.scrapstosavory.app.data.DatabaseHelper;
import com.scrapstosavory.app.data.RecipeDao;
import com.scrapstosavory.app.model.DietTag;
import com.scrapstosavory.app.model.MealType;
import com.scrapstosavory.app.model.Recipe;
import com.scrapstosavory.app.model.RecipeIngredient;
import com.scrapstosavory.app.util.QuantityUtils;

/**
 * Shows everything about one recipe: its name, its full ingredient list
 * with quantities, and the steps to make it. Opened from the Suggested
 * Recipes screen by tapping a recipe, with the recipe's id passed in
 * through EXTRA_RECIPE_ID.
 *
 * This is a detail screen, not one of the app's main screens, so it uses
 * a normal back arrow rather than the shared navigation menu.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "com.scrapstosavory.app.EXTRA_RECIPE_ID";

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
        RecipeDao recipeDao = new RecipeDao(new DatabaseHelper(this));
        Recipe recipe = recipeDao.getById(recipeId);

        if (recipe == null) {
            Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showRecipe(recipe);
    }

    private void showRecipe(Recipe recipe) {
        ((TextView) findViewById(R.id.textRecipeName)).setText(recipe.getName());
        ((TextView) findViewById(R.id.textRecipeTags)).setText(buildTagsText(recipe));
        ((TextView) findViewById(R.id.textIngredientsList)).setText(buildIngredientsText(recipe));
        ((TextView) findViewById(R.id.textSteps)).setText(recipe.getSteps());
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
