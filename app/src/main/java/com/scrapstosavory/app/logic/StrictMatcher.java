package com.scrapstosavory.app.logic;

import com.scrapstosavory.app.model.PantryItem;
import com.scrapstosavory.app.model.Recipe;
import com.scrapstosavory.app.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Works out which recipes the user can actually cook right now, using
 * only what is already in their pantry.
 *
 * A recipe only counts as makeable if every single ingredient it needs
 * is in the pantry, in at least the amount needed. Missing even one
 * ingredient, or not having enough of one, means the recipe does not
 * qualify — this is the main rule the whole app is built around.
 *
 * Ingredient names are compared after a bit of simple clean-up (lower
 * case, trimmed, and a basic singular/plural check), so that small
 * differences like "tomato" vs "tomatoes" do not stop a match that
 * should otherwise work.
 */
public final class StrictMatcher {

    private StrictMatcher() {
    }

    /** Returns only the recipes that can be made from the pantry right now. */
    public static List<Recipe> getSuggestedRecipes(List<Recipe> allRecipes, List<PantryItem> pantry) {
        List<Recipe> suggested = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (canMake(recipe, pantry)) {
                suggested.add(recipe);
            }
        }
        return suggested;
    }

    /**
     * Extra, optional list: recipes that are missing exactly one
     * ingredient. Not part of the main suggestions, but there if a
     * "so close!" style list gets added later.
     */
    public static List<Recipe> getAlmostThereRecipes(List<Recipe> allRecipes, List<PantryItem> pantry) {
        List<Recipe> almostThere = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (countMissingIngredients(recipe, pantry) == 1) {
                almostThere.add(recipe);
            }
        }
        return almostThere;
    }

    /** True if every ingredient the recipe needs is in the pantry, in enough quantity. */
    public static boolean canMake(Recipe recipe, List<PantryItem> pantry) {
        return countMissingIngredients(recipe, pantry) == 0;
    }

    /** How many of the recipe's ingredients are not covered by the pantry. */
    private static int countMissingIngredients(Recipe recipe, List<PantryItem> pantry) {
        int missing = 0;
        for (RecipeIngredient needed : recipe.getIngredients()) {
            if (!pantryCovers(needed, pantry)) {
                missing++;
            }
        }
        return missing;
    }

    /** True if some item in the pantry matches this ingredient's name and has enough of it. */
    private static boolean pantryCovers(RecipeIngredient needed, List<PantryItem> pantry) {
        String neededName = normalizeName(needed.getName());
        for (PantryItem item : pantry) {
            boolean sameIngredient = normalizeName(item.getName()).equals(neededName);
            if (sameIngredient && quantityIsEnough(item.getQuantity(), item.getUnit(),
                    needed.getQuantity(), needed.getUnit())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Makes an ingredient name easier to compare: lower case, trimmed, and
     * a simple check for common plural endings, so "tomato" and "tomatoes"
     * (or "onion" and "onions") count as the same ingredient.
     */
    static String normalizeName(String name) {
        String cleaned = name == null ? "" : name.trim().toLowerCase();

        if (cleaned.endsWith("oes") && cleaned.length() > 3) {
            return cleaned.substring(0, cleaned.length() - 2); // tomatoes -> tomato, potatoes -> potato
        }
        if (cleaned.endsWith("ies") && cleaned.length() > 3) {
            return cleaned.substring(0, cleaned.length() - 3) + "y"; // berries -> berry
        }
        if (cleaned.endsWith("s") && !cleaned.endsWith("ss") && cleaned.length() > 1) {
            return cleaned.substring(0, cleaned.length() - 1); // onions -> onion
        }
        return cleaned;
    }

    /**
     * Checks the pantry has enough of an ingredient. Grams/kilograms and
     * millilitres/litres are converted so, for example, 1 kg in the
     * pantry can cover a recipe that needs 500 g. If the two units can't
     * be compared at all (say, "pcs" against "tbsp"), this returns false
     * instead of guessing.
     */
    static boolean quantityIsEnough(double haveQty, String haveUnit, double neededQty, String neededUnit) {
        if (!sameUnitFamily(haveUnit, neededUnit)) {
            return false;
        }
        Double haveInBaseUnit = toBaseUnit(haveQty, haveUnit);
        Double neededInBaseUnit = toBaseUnit(neededQty, neededUnit);
        if (haveInBaseUnit == null || neededInBaseUnit == null) {
            return false;
        }
        return haveInBaseUnit >= neededInBaseUnit;
    }

    /** True if these two units can be sensibly compared against each other. */
    private static boolean sameUnitFamily(String unitA, String unitB) {
        String familyA = unitFamily(unitA);
        return familyA != null && familyA.equals(unitFamily(unitB));
    }

    /** Groups units that can be converted into each other. Everything else only matches itself. */
    private static String unitFamily(String unit) {
        if (unit == null) {
            return null;
        }
        switch (unit.trim().toLowerCase()) {
            case "g":
            case "kg":
                return "mass";
            case "ml":
            case "l":
                return "volume";
            default:
                return unit.trim().toLowerCase();
        }
    }

    /** Converts a quantity into a common base unit (grams or millilitres) so amounts can be compared fairly. */
    private static Double toBaseUnit(double quantity, String unit) {
        if (unit == null) {
            return null;
        }
        switch (unit.trim().toLowerCase()) {
            case "g":
            case "ml":
            case "pcs":
            case "tsp":
            case "tbsp":
            case "cup":
                return quantity;
            case "kg":
            case "l":
                return quantity * 1000;
            default:
                return null;
        }
    }
}
