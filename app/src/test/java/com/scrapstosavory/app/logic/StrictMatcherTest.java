package com.scrapstosavory.app.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.scrapstosavory.app.model.PantryItem;
import com.scrapstosavory.app.model.Recipe;
import com.scrapstosavory.app.model.RecipeIngredient;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Plain JUnit tests for StrictMatcher. These do not need an emulator or
 * device to run — right-click this file in Android Studio and choose
 * "Run" to check the matching logic on its own.
 */
public class StrictMatcherTest {

    @Test
    public void recipeIsSuggested_whenPantryHasExactlyWhatIsNeeded() {
        Recipe recipe = recipeNeeding(
                new RecipeIngredient("tomato", 2, "pcs"),
                new RecipeIngredient("rice", 200, "g"));

        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("tomato", 2, "pcs", null, null, null),
                new PantryItem("rice", 200, "g", null, null, null));

        assertTrue(StrictMatcher.canMake(recipe, pantry));
    }

    @Test
    public void recipeIsNotSuggested_whenOneIngredientIsMissing() {
        Recipe recipe = recipeNeeding(
                new RecipeIngredient("tomato", 2, "pcs"),
                new RecipeIngredient("rice", 200, "g"));

        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("tomato", 2, "pcs", null, null, null));
        // rice is missing entirely

        assertFalse(StrictMatcher.canMake(recipe, pantry));
    }

    @Test
    public void almostThereList_catchesRecipesMissingExactlyOneIngredient() {
        Recipe recipe = recipeNeeding(
                new RecipeIngredient("tomato", 2, "pcs"),
                new RecipeIngredient("rice", 200, "g"));

        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("tomato", 2, "pcs", null, null, null));

        List<Recipe> almostThere = StrictMatcher.getAlmostThereRecipes(
                Arrays.asList(recipe), pantry);

        assertEquals(1, almostThere.size());
    }

    @Test
    public void namesStillMatch_whenOneIsPluralAndOneIsSingular() {
        Recipe recipe = recipeNeeding(new RecipeIngredient("tomato", 1, "pcs"));

        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Tomatoes", 3, "pcs", null, null, null));

        assertTrue(StrictMatcher.canMake(recipe, pantry));
    }

    @Test
    public void recipeIsNotSuggested_whenPantryHasNotEnoughQuantity() {
        Recipe recipe = recipeNeeding(new RecipeIngredient("rice", 500, "g"));

        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("rice", 100, "g", null, null, null));

        assertFalse(StrictMatcher.canMake(recipe, pantry));
    }

    @Test
    public void quantityStillMatches_whenUnitsAreDifferentButConvertible() {
        Recipe recipe = recipeNeeding(new RecipeIngredient("rice", 500, "g"));

        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("rice", 1, "kg", null, null, null)); // 1 kg = 1000 g, more than enough

        assertTrue(StrictMatcher.canMake(recipe, pantry));
    }

    @Test
    public void quantityDoesNotMatch_whenUnitsCannotBeCompared() {
        Recipe recipe = recipeNeeding(new RecipeIngredient("oats", 2, "tbsp"));

        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("oats", 5, "pcs", null, null, null)); // "pcs" and "tbsp" cannot be compared

        assertFalse(StrictMatcher.canMake(recipe, pantry));
    }

    private Recipe recipeNeeding(RecipeIngredient... ingredients) {
        Recipe recipe = new Recipe(1, "Test Recipe", "Test steps");
        recipe.setIngredients(new ArrayList<>(Arrays.asList(ingredients)));
        return recipe;
    }
}
