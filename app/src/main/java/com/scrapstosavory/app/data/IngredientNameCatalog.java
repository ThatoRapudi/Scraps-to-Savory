package com.scrapstosavory.app.data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A fixed list of common South African home-cooking ingredients, grouped
 * under simple headings, with a sensible default unit for each one.
 *
 * This exists so the Add/Edit Ingredient screen can offer a dropdown of
 * names instead of free typing. Each name also carries a starting unit
 * that makes sense for it, since a tomato is naturally counted in
 * pieces while rice is naturally weighed in kilograms, rather than
 * leaving the unit stuck on grams no matter what was picked. The user
 * can still change the unit afterwards if theirs is different.
 */
public final class IngredientNameCatalog {

    /** One preset ingredient name and the unit it should default to. */
    public static class Entry {
        public final String name;
        public final String defaultUnit;

        public Entry(String name, String defaultUnit) {
            this.name = name;
            this.defaultUnit = defaultUnit;
        }
    }

    public static final Map<String, Entry[]> GROUPS = new LinkedHashMap<>();

    static {
        GROUPS.put("Meat and poultry", new Entry[]{
                new Entry("Chicken breast", "g"),
                new Entry("Chicken thighs", "g"),
                new Entry("Beef mince", "g"),
                new Entry("Beef stewing meat", "g"),
                new Entry("Pork chops", "g"),
                new Entry("Boerewors", "g"),
                new Entry("Bacon", "g"),
        });

        GROUPS.put("Vegetables", new Entry[]{
                new Entry("Onion", "pcs"),
                new Entry("Tomato", "pcs"),
                new Entry("Potato", "pcs"),
                new Entry("Butternut", "pcs"),
                new Entry("Carrot", "pcs"),
                new Entry("Cabbage", "pcs"),
                new Entry("Spinach", "g"),
                new Entry("Green pepper", "pcs"),
                new Entry("Garlic", "pcs"),
                new Entry("Pumpkin", "pcs"),
        });

        GROUPS.put("Spices and seasonings", new Entry[]{
                new Entry("Salt", "g"),
                new Entry("Black pepper", "g"),
                new Entry("Paprika", "g"),
                new Entry("Curry powder", "g"),
                new Entry("Mixed herbs", "g"),
                new Entry("Stock cubes", "pcs"),
                new Entry("Chilli powder", "g"),
                new Entry("Garlic and herb seasoning", "g"),
        });

        GROUPS.put("Dairy and eggs", new Entry[]{
                new Entry("Milk", "ml"),
                new Entry("Eggs", "pcs"),
                new Entry("Cheese", "g"),
                new Entry("Butter", "g"),
                new Entry("Plain yoghurt", "ml"),
                new Entry("Margarine", "g"),
        });

        GROUPS.put("Grains and starches", new Entry[]{
                new Entry("Rice", "kg"),
                new Entry("Pap (maize meal)", "kg"),
                new Entry("Samp", "kg"),
                new Entry("Bread", "pcs"),
                new Entry("Pasta", "g"),
                new Entry("Oats", "g"),
                new Entry("Flour", "kg"),
        });

        GROUPS.put("Other", new Entry[]{
                new Entry("Cooking oil", "ml"),
                new Entry("Canned beans", "pcs"),
                new Entry("Canned tomatoes", "pcs"),
                new Entry("Peanut butter", "g"),
                new Entry("Sugar", "kg"),
        });
    }

    private IngredientNameCatalog() {
    }
}
