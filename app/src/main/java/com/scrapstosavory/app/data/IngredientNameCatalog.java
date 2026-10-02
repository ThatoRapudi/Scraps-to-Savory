package com.scrapstosavory.app.data;

import com.scrapstosavory.app.model.PantryCategory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A fixed list of common South African home-cooking ingredients, grouped
 * by PantryCategory, with a sensible default unit and a per-tap step
 * quantity for each one.
 *
 * This exists so the Add Ingredients screen can offer a pick list of
 * names instead of free typing, with one expandable section per
 * category. Picking a name also picks its category for you, since they
 * are the same grouping.
 *
 * Names and units here are kept lined up with how the seeded recipes
 * describe their own ingredients (for example "Chicken breast" rather
 * than just "Chicken", and grams for things like Cabbage and Butternut
 * rather than a plain piece count), since Suggested Recipes can only
 * ever match a pantry item to a recipe ingredient when the name and the
 * unit family both agree.
 *
 * defaultQuantityPerTap is how much tapping + once adds, in defaultUnit.
 * For things naturally counted (an onion, an egg) that is just 1. For
 * things naturally weighed or measured (mince, milk, oil) it is set to
 * a realistic single-purchase amount, so a couple of taps already adds
 * up to a usable amount instead of the user having to tap hundreds of
 * times to reach, say, 500 grams.
 */
public final class IngredientNameCatalog {

    /** One preset ingredient name, the unit it should default to, and how much one tap adds. */
    public static class Entry {
        public final String name;
        public final String defaultUnit;
        public final double defaultQuantityPerTap;

        public Entry(String name, String defaultUnit, double defaultQuantityPerTap) {
            this.name = name;
            this.defaultUnit = defaultUnit;
            this.defaultQuantityPerTap = defaultQuantityPerTap;
        }
    }

    public static final Map<PantryCategory, Entry[]> GROUPS = new LinkedHashMap<>();

    static {
        GROUPS.put(PantryCategory.MEAT_AND_POULTRY, new Entry[]{
                new Entry("Chicken breast", "g", 500),
                new Entry("Chicken thighs", "g", 500),
                new Entry("Beef mince", "g", 500),
                new Entry("Stewing beef", "g", 500),
                new Entry("Pork chops", "g", 500),
                new Entry("Boerewors", "g", 500),
                new Entry("Bacon", "g", 250),
                new Entry("Hake", "g", 300),
        });

        GROUPS.put(PantryCategory.VEGETABLES, new Entry[]{
                new Entry("Onion", "pcs", 1),
                new Entry("Tomato", "pcs", 1),
                new Entry("Potato", "pcs", 1),
                new Entry("Butternut", "g", 500),
                new Entry("Carrot", "pcs", 1),
                new Entry("Cabbage", "g", 300),
                new Entry("Spinach", "g", 150),
                new Entry("Green pepper", "pcs", 1),
                new Entry("Garlic", "pcs", 1),
                new Entry("Pumpkin", "g", 500),
                new Entry("Beetroot", "pcs", 1),
                new Entry("Green beans", "g", 150),
        });

        GROUPS.put(PantryCategory.SPICES_AND_SEASONINGS, new Entry[]{
                new Entry("Salt", "tsp", 1),
                new Entry("Black pepper", "g", 50),
                new Entry("Paprika", "g", 50),
                new Entry("Curry powder", "g", 50),
                new Entry("Mixed herbs", "g", 50),
                new Entry("Stock cubes", "pcs", 1),
                new Entry("Chili powder", "tsp", 1),
                new Entry("Garlic and herb seasoning", "g", 50),
                new Entry("Baking powder", "tsp", 1),
                new Entry("Sugar", "tsp", 1),
        });

        GROUPS.put(PantryCategory.DAIRY_AND_EGGS, new Entry[]{
                new Entry("Milk", "ml", 500),
                new Entry("Eggs", "pcs", 1),
                new Entry("Cheese", "g", 100),
                new Entry("Butter", "tbsp", 1),
                new Entry("Plain yoghurt", "ml", 250),
                new Entry("Margarine", "g", 100),
        });

        GROUPS.put(PantryCategory.GRAINS_AND_STARCHES, new Entry[]{
                new Entry("Rice", "kg", 1),
                new Entry("Maize meal", "g", 250),
                new Entry("Samp", "kg", 1),
                new Entry("Bread", "pcs", 1),
                new Entry("Pasta", "g", 250),
                new Entry("Oats", "g", 80),
                new Entry("Flour", "kg", 1),
                new Entry("Dombolo", "g", 250),
                new Entry("Instant noodles", "pcs", 1),
                new Entry("Weet-Bix", "pcs", 1),
                new Entry("Cereal", "g", 60),
        });

        GROUPS.put(PantryCategory.OTHER, new Entry[]{
                new Entry("Cooking oil", "ml", 250),
                new Entry("Baked beans", "g", 200),
                new Entry("Canned tomatoes", "pcs", 1),
                new Entry("Peanut butter", "tbsp", 1),
                new Entry("Leftovers (meal prep)", "pcs", 1),
                new Entry("Lentils", "g", 250),
                new Entry("Chickpeas", "g", 250),
                new Entry("Canned tuna", "g", 100),
                new Entry("Tofu", "g", 250),
        });
    }

    private IngredientNameCatalog() {
    }
}
