package com.scrapstosavory.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class sets up and manages the app's SQLite database. It creates
 * three tables:
 *
 *   pantry_items       - everything the user currently has at home
 *   recipes             - the list of recipes (name, steps, diet tags, meal type)
 *   recipe_ingredients  - one row per ingredient a recipe needs, linked to a recipe by id
 *
 * The recipes are loaded once, the first time the app runs, inside
 * onCreate() below, so the app already has recipes to suggest without
 * the user having to add anything themselves.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "scraps_to_savory.db";
    private static final int DATABASE_VERSION = 2;

    // --- pantry_items --------------------------------------------------
    public static final String TABLE_PANTRY_ITEMS = "pantry_items";
    public static final String COL_PANTRY_ID = "id";
    public static final String COL_PANTRY_NAME = "name";
    public static final String COL_PANTRY_QUANTITY = "quantity";
    public static final String COL_PANTRY_UNIT = "unit";
    public static final String COL_PANTRY_EXPIRY_DATE = "expiry_date";
    public static final String COL_PANTRY_DATE_ADDED = "date_added";
    public static final String COL_PANTRY_CATEGORY = "category";
    public static final String COL_PANTRY_WEIGHT_GRAMS = "weight_grams"; // optional, null when not given

    // --- recipes ---------------------------------------------------------
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_ID = "id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_STEPS = "steps";
    public static final String COL_RECIPE_DIET_TAGS = "diet_tags";   // comma-separated DietTag names
    public static final String COL_RECIPE_MEAL_TYPES = "meal_types"; // comma-separated MealType names

    // --- recipe_ingredients -----------------------------------------------
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID = "id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_NAME = "name";
    public static final String COL_RI_QUANTITY = "quantity";
    public static final String COL_RI_UNIT = "unit";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY_ITEMS + " (" +
                COL_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PANTRY_NAME + " TEXT NOT NULL, " +
                COL_PANTRY_QUANTITY + " REAL NOT NULL, " +
                COL_PANTRY_UNIT + " TEXT NOT NULL, " +
                COL_PANTRY_EXPIRY_DATE + " TEXT, " +
                COL_PANTRY_DATE_ADDED + " TEXT, " +
                COL_PANTRY_CATEGORY + " TEXT NOT NULL DEFAULT 'OTHER', " +
                COL_PANTRY_WEIGHT_GRAMS + " REAL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " (" +
                COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPE_NAME + " TEXT NOT NULL, " +
                COL_RECIPE_STEPS + " TEXT NOT NULL, " +
                COL_RECIPE_DIET_TAGS + " TEXT NOT NULL DEFAULT '', " +
                COL_RECIPE_MEAL_TYPES + " TEXT NOT NULL DEFAULT '')");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RI_RECIPE_ID + " INTEGER NOT NULL, " +
                COL_RI_NAME + " TEXT NOT NULL, " +
                COL_RI_QUANTITY + " REAL NOT NULL, " +
                COL_RI_UNIT + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_RECIPE_ID + "))");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simplest way to handle a database upgrade for a small app like this:
        // delete the old tables and create them again from scratch.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY_ITEMS);
        onCreate(db);
    }

    // ---------------------------------------------------------------------
    // Seed data: 27 recipes built from a shared ~40-item South African
    // home-cooking / quick-student-meal ingredient pool. Ingredients
    // deliberately overlap across recipes so that adding or removing a
    // single pantry item changes which recipes qualify as "suggested".
    // ---------------------------------------------------------------------

    private void seedRecipes(SQLiteDatabase db) {

        insertRecipe(db, "Pap with Tomato & Onion Relish",
                "1. Bring 750ml water to the boil and stir in the maize meal.\n" +
                        "2. Reduce heat and simmer 20 minutes, stirring occasionally, until firm.\n" +
                        "3. Heat oil and fry the onion and garlic until soft.\n" +
                        "4. Add chopped tomato and chili powder, simmer 10 minutes to make the relish.\n" +
                        "5. Season with salt and serve the pap topped with relish.",
                new String[]{"VEGAN"}, new String[]{"LUNCH", "DINNER"},
                new Object[][]{
                        {"maize meal", 250.0, "g"},
                        {"tomato", 3.0, "pcs"},
                        {"onion", 1.0, "pcs"},
                        {"garlic", 2.0, "pcs"},
                        {"chili powder", 1.0, "tsp"},
                        {"cooking oil", 30.0, "ml"},
                        {"salt", 1.0, "tsp"}
                });

        insertRecipe(db, "Chicken & Rice One-Pot",
                "1. Heat oil and brown the chicken pieces on all sides.\n" +
                        "2. Add onion and garlic, fry until fragrant.\n" +
                        "3. Stir in rice, stock cube and 500ml water.\n" +
                        "4. Cover and simmer 20 minutes until rice is cooked and liquid absorbed.",
                new String[]{"HIGH_PROTEIN"}, new String[]{"LUNCH", "DINNER"},
                new Object[][]{
                        {"chicken", 400.0, "g"},
                        {"rice", 250.0, "g"},
                        {"onion", 1.0, "pcs"},
                        {"garlic", 2.0, "pcs"},
                        {"stock cube", 1.0, "pcs"},
                        {"cooking oil", 30.0, "ml"}
                });

        insertRecipe(db, "Mince & Potato Bake",
                "1. Brown the mince with the chopped onion until cooked through.\n" +
                        "2. Layer sliced potato and tomato in an oven dish with the mince.\n" +
                        "3. Top with grated cheese.\n" +
                        "4. Bake at 180°C for 30 minutes until potatoes are soft and cheese is golden.",
                new String[]{"HIGH_PROTEIN"}, new String[]{"LUNCH", "DINNER"},
                new Object[][]{
                        {"beef mince", 400.0, "g"},
                        {"potato", 4.0, "pcs"},
                        {"onion", 1.0, "pcs"},
                        {"tomato", 2.0, "pcs"},
                        {"cheese", 100.0, "g"},
                        {"cooking oil", 30.0, "ml"}
                });

        insertRecipe(db, "Lentil & Vegetable Stew",
                "1. Fry the onion and garlic in oil until soft.\n" +
                        "2. Add chopped carrot and tomato, cook 5 minutes.\n" +
                        "3. Stir in lentils, stock cube and 600ml water.\n" +
                        "4. Simmer 25 minutes until the lentils are soft.",
                new String[]{"VEGAN", "HIGH_PROTEIN"}, new String[]{"LUNCH", "DINNER"},
                new Object[][]{
                        {"lentils", 250.0, "g"},
                        {"onion", 1.0, "pcs"},
                        {"garlic", 2.0, "pcs"},
                        {"carrot", 2.0, "pcs"},
                        {"tomato", 2.0, "pcs"},
                        {"stock cube", 1.0, "pcs"},
                        {"cooking oil", 30.0, "ml"}
                });

        insertRecipe(db, "Cheesy Tomato Pasta",
                "1. Boil the pasta according to packet instructions.\n" +
                        "2. Fry onion and garlic in oil until soft.\n" +
                        "3. Add chopped tomato and simmer to make a simple sauce.\n" +
                        "4. Mix the sauce through the drained pasta and top with grated cheese.",
                new String[]{"VEGETARIAN"}, new String[]{"LUNCH", "DINNER"},
                new Object[][]{
                        {"pasta", 250.0, "g"},
                        {"tomato", 3.0, "pcs"},
                        {"cheese", 100.0, "g"},
                        {"onion", 1.0, "pcs"},
                        {"garlic", 2.0, "pcs"},
                        {"cooking oil", 30.0, "ml"}
                });

        insertRecipe(db, "Peanut Butter Toast",
                "1. Toast the bread.\n" +
                        "2. Spread peanut butter generously over each slice.",
                new String[]{"VEGAN"}, new String[]{"BREAKFAST"},
                new Object[][]{
                        {"bread", 2.0, "pcs"},
                        {"peanut butter", 2.0, "tbsp"}
                });

        insertRecipe(db, "Tuna & Onion Sandwich",
                "1. Drain the tuna and mix with finely chopped onion.\n" +
                        "2. Spread the mixture between the bread slices and serve.",
                new String[]{"HIGH_PROTEIN"}, new String[]{"LUNCH", "DINNER"},
                new Object[][]{
                        {"bread", 2.0, "pcs"},
                        {"canned tuna", 100.0, "g"},
                        {"onion", 1.0, "pcs"}
                });

        insertRecipe(db, "Chickpea & Spinach Curry",
                "1. Fry onion and garlic in oil until soft.\n" +
                        "2. Add chopped tomato and chili powder, cook 5 minutes.\n" +
                        "3. Stir in chickpeas, stock cube and 300ml water, simmer 10 minutes.\n" +
                        "4. Add spinach and stir until wilted.",
                new String[]{"VEGAN", "HIGH_PROTEIN"}, new String[]{"LUNCH", "DINNER"},
                new Object[][]{
                        {"chickpeas", 250.0, "g"},
                        {"spinach", 150.0, "g"},
                        {"onion", 1.0, "pcs"},
                        {"garlic", 2.0, "pcs"},
                        {"tomato", 2.0, "pcs"},
                        {"chili powder", 1.0, "tsp"},
                        {"stock cube", 1.0, "pcs"},
                        {"cooking oil", 30.0, "ml"}
                });

        insertRecipe(db, "Egg Fried Rice",
                "1. Cook the rice and set aside (or use leftover rice).\n" +
                        "2. Scramble the eggs in a hot pan with a little oil, then remove.\n" +
                        "3. Fry chopped onion and carrot until soft.\n" +
                        "4. Add the rice and scrambled egg, stir-fry together for 5 minutes.",
                new String[]{"VEGETARIAN"}, new String[]{"LUNCH", "DINNER"},
                new Object[][]{
                        {"egg", 2.0, "pcs"},
                        {"rice", 250.0, "g"},
                        {"onion", 1.0, "pcs"},
                        {"carrot", 1.0, "pcs"},
                        {"cooking oil", 30.0, "ml"}
                });

        insertRecipe(db, "Instant Noodle Veg Stir-fry",
                "1. Cook the instant noodles according to packet instructions.\n" +
                        "2. Stir-fry shredded cabbage, carrot and onion in oil until tender.\n" +
                        "3. Toss the cooked noodles through the vegetables and serve.",
                new String[]{"VEGAN"}, new String[]{"LUNCH", "DINNER"},
                new Object[][]{
                        {"instant noodles", 2.0, "pcs"},
                        {"cabbage", 150.0, "g"},
                        {"carrot", 1.0, "pcs"},
                        {"onion", 1.0, "pcs"},
                        {"cooking oil", 30.0, "ml"}
                });

        insertRecipe(db, "Tomato & Lentil Rice",
                "1. Cook the rice separately until tender.\n" +
                        "2. Simmer the lentils with chopped tomato and a little water until soft.\n" +
                        "3. Combine the lentil mixture with the rice and season to taste.",
                new String[]{"VEGAN", "HIGH_PROTEIN"}, new String[]{"LUNCH", "DINNER"},
                new Object[][]{
                        {"tomato", 2.0, "pcs"},
                        {"lentils", 150.0, "g"},
                        {"rice", 200.0, "g"}
                });

        insertRecipe(db, "Baked Bean Toast",
                "1. Toast the bread.\n" +
                        "2. Heat the baked beans in a small pot.\n" +
                        "3. Spoon beans over the toast and top with grated cheese.",
                new String[]{"VEGETARIAN"}, new String[]{"LUNCH", "DINNER"},
                new Object[][]{
                        {"bread", 2.0, "pcs"},
                        {"baked beans", 200.0, "g"},
                        {"cheese", 50.0, "g"}
                });

        insertRecipe(db, "Butternut Soup",
                "1. Fry onion and garlic in oil until soft.\n" +
                        "2. Add chopped butternut, stock cube and 600ml water.\n" +
                        "3. Simmer until the butternut is soft, then mash or blend until smooth.",
                new String[]{"VEGAN"}, new String[]{"LUNCH", "DINNER"},
                new Object[][]{
                        {"butternut", 500.0, "g"},
                        {"onion", 1.0, "pcs"},
                        {"garlic", 2.0, "pcs"},
                        {"stock cube", 1.0, "pcs"},
                        {"cooking oil", 30.0, "ml"}
                });

        insertRecipe(db, "Cabbage & Mince Stew",
                "1. Brown the mince with chopped onion and tomato.\n" +
                        "2. Add shredded cabbage and stock cube.\n" +
                        "3. Cover and simmer 20 minutes until the cabbage is soft.",
                new String[]{"HIGH_PROTEIN"}, new String[]{"LUNCH", "DINNER"},
                new Object[][]{
                        {"beef mince", 300.0, "g"},
                        {"cabbage", 300.0, "g"},
                        {"onion", 1.0, "pcs"},
                        {"tomato", 2.0, "pcs"},
                        {"stock cube", 1.0, "pcs"},
                        {"cooking oil", 30.0, "ml"}
                });

        insertRecipe(db, "Spinach & Cheese Omelette",
                "1. Wilt the spinach in a hot pan with a little oil, then set aside.\n" +
                        "2. Whisk the eggs and pour into the pan.\n" +
                        "3. Add the spinach and cheese, fold the omelette and cook through.",
                new String[]{"VEGETARIAN", "HIGH_PROTEIN"}, new String[]{"BREAKFAST"},
                new Object[][]{
                        {"egg", 3.0, "pcs"},
                        {"spinach", 100.0, "g"},
                        {"cheese", 50.0, "g"},
                        {"cooking oil", 15.0, "ml"}
                });

        insertRecipe(db, "Garlic Butter Pasta",
                "1. Boil the pasta according to packet instructions.\n" +
                        "2. Melt the butter and fry the chopped garlic until fragrant.\n" +
                        "3. Toss the drained pasta through the garlic butter and top with cheese.",
                new String[]{"VEGETARIAN"}, new String[]{"LUNCH", "DINNER"},
                new Object[][]{
                        {"pasta", 250.0, "g"},
                        {"garlic", 3.0, "pcs"},
                        {"butter", 2.0, "tbsp"},
                        {"cheese", 50.0, "g"}
                });

        insertRecipe(db, "Tofu Veg Stir-fry",
                "1. Fry the tofu in oil until golden on all sides.\n" +
                        "2. Add chopped garlic, cabbage and carrot.\n" +
                        "3. Stir-fry until the vegetables are tender.",
                new String[]{"VEGAN", "HIGH_PROTEIN"}, new String[]{"LUNCH", "DINNER"},
                new Object[][]{
                        {"tofu", 250.0, "g"},
                        {"cabbage", 150.0, "g"},
                        {"carrot", 1.0, "pcs"},
                        {"garlic", 2.0, "pcs"},
                        {"cooking oil", 30.0, "ml"}
                });

        insertRecipe(db, "Chicken & Vegetable Soup",
                "1. Simmer the chicken with the stock cube in water for 15 minutes.\n" +
                        "2. Add chopped carrot, potato and onion.\n" +
                        "3. Simmer a further 20 minutes until the vegetables and chicken are tender.",
                new String[]{"HIGH_PROTEIN"}, new String[]{"LUNCH", "DINNER"},
                new Object[][]{
                        {"chicken", 300.0, "g"},
                        {"carrot", 2.0, "pcs"},
                        {"potato", 2.0, "pcs"},
                        {"onion", 1.0, "pcs"},
                        {"stock cube", 1.0, "pcs"}
                });

        insertRecipe(db, "Seven Colours with Chicken",
                "1. Cook the rice until tender.\n" +
                        "2. Fry or roast the chicken pieces until cooked through.\n" +
                        "3. Boil the carrot and green beans until tender.\n" +
                        "4. Boil and grate the beetroot for a beetroot salad.\n" +
                        "5. Shred the cabbage for a simple coleslaw.\n" +
                        "6. Plate the rice, chicken and all three vegetable sides together.",
                new String[]{"HIGH_PROTEIN"}, new String[]{"LUNCH", "DINNER"},
                new Object[][]{
                        {"rice", 250.0, "g"},
                        {"chicken", 400.0, "g"},
                        {"carrot", 2.0, "pcs"},
                        {"green beans", 150.0, "g"},
                        {"beetroot", 2.0, "pcs"},
                        {"cabbage", 150.0, "g"}
                });

        insertRecipe(db, "Seven Colours with Beef Stew",
                "1. Brown the beef with chopped onion and tomato, then simmer until tender (beef stew).\n" +
                        "2. Boil the samp until soft.\n" +
                        "3. Boil the carrot and green beans until tender.\n" +
                        "4. Boil and slice the beetroot for a beetroot salad.\n" +
                        "5. Plate the samp, beef stew and vegetable sides together.",
                new String[]{"HIGH_PROTEIN"}, new String[]{"LUNCH", "DINNER"},
                new Object[][]{
                        {"samp", 250.0, "g"},
                        {"stewing beef", 400.0, "g"},
                        {"onion", 1.0, "pcs"},
                        {"carrot", 2.0, "pcs"},
                        {"green beans", 150.0, "g"},
                        {"beetroot", 2.0, "pcs"},
                        {"tomato", 2.0, "pcs"}
                });

        insertRecipe(db, "Seven Colours with Hake",
                "1. Steam the dombolo (steamed bread) until cooked through.\n" +
                        "2. Fry or grill the hake until cooked through.\n" +
                        "3. Boil the carrot until tender.\n" +
                        "4. Boil and slice the beetroot for a beetroot salad.\n" +
                        "5. Sauté the cabbage until soft.\n" +
                        "6. Plate the dombolo, hake and vegetable sides together with sliced tomato.",
                new String[]{"HIGH_PROTEIN"}, new String[]{"LUNCH", "DINNER"},
                new Object[][]{
                        {"dombolo", 250.0, "g"},
                        {"hake", 300.0, "g"},
                        {"carrot", 2.0, "pcs"},
                        {"cabbage", 150.0, "g"},
                        {"beetroot", 2.0, "pcs"},
                        {"tomato", 2.0, "pcs"}
                });

        insertRecipe(db, "Pap, Chakalaka & Beef Stew",
                "1. Bring water to the boil and stir in the maize meal to make pap, simmer 20 minutes.\n" +
                        "2. Brown the beef with chopped onion, tomato and chili powder.\n" +
                        "3. Simmer the beef until tender to make a simple stew.\n" +
                        "4. Serve the pap with the beef stew.",
                new String[]{"HIGH_PROTEIN"}, new String[]{"LUNCH", "DINNER"},
                new Object[][]{
                        {"maize meal", 250.0, "g"},
                        {"stewing beef", 400.0, "g"},
                        {"tomato", 2.0, "pcs"},
                        {"onion", 1.0, "pcs"},
                        {"chili powder", 1.0, "tsp"},
                        {"cooking oil", 30.0, "ml"}
                });

        insertRecipe(db, "Pap, Relish & Wors",
                "1. Bring water to the boil and stir in the maize meal to make pap, simmer 20 minutes.\n" +
                        "2. Grill or fry the wors until cooked through.\n" +
                        "3. Make a relish by frying onion, tomato and chili powder together.\n" +
                        "4. Serve the pap and wors topped with relish.",
                new String[]{"HIGH_PROTEIN"}, new String[]{"LUNCH", "DINNER"},
                new Object[][]{
                        {"maize meal", 250.0, "g"},
                        {"wors", 400.0, "g"},
                        {"tomato", 2.0, "pcs"},
                        {"onion", 1.0, "pcs"},
                        {"chili powder", 1.0, "tsp"},
                        {"cooking oil", 30.0, "ml"}
                });

        insertRecipe(db, "Cereal with Milk",
                "1. Pour the cereal into a bowl.\n" +
                        "2. Add milk and serve immediately.",
                new String[]{"VEGETARIAN"}, new String[]{"BREAKFAST"},
                new Object[][]{
                        {"cereal", 60.0, "g"},
                        {"milk", 250.0, "ml"}
                });

        insertRecipe(db, "Weet-Bix with Milk",
                "1. Place the Weet-Bix in a bowl.\n" +
                        "2. Pour over the milk and serve.",
                new String[]{"VEGETARIAN"}, new String[]{"BREAKFAST"},
                new Object[][]{
                        {"weet-bix", 3.0, "pcs"},
                        {"milk", 200.0, "ml"}
                });

        insertRecipe(db, "Oats Porridge",
                "1. Simmer the oats in the milk, stirring often, until thick and creamy.\n" +
                        "2. Stir in the sugar and serve warm.",
                new String[]{"VEGETARIAN"}, new String[]{"BREAKFAST"},
                new Object[][]{
                        {"oats", 80.0, "g"},
                        {"milk", 250.0, "ml"},
                        {"sugar", 1.0, "tsp"}
                });

        insertRecipe(db, "Pancakes",
                "1. Whisk the flour, egg, milk, sugar and baking powder into a smooth batter.\n" +
                        "2. Melt a little butter in a hot pan.\n" +
                        "3. Fry small amounts of batter until golden on both sides.",
                new String[]{"VEGETARIAN"}, new String[]{"BREAKFAST"},
                new Object[][]{
                        {"flour", 200.0, "g"},
                        {"egg", 2.0, "pcs"},
                        {"milk", 300.0, "ml"},
                        {"sugar", 1.0, "tsp"},
                        {"baking powder", 1.0, "tsp"},
                        {"butter", 1.0, "tbsp"}
                });
    }

    /**
     * Inserts one recipe row plus all of its recipe_ingredients rows.
     *
     * @param dietTags      DietTag enum names, e.g. {"VEGAN", "HIGH_PROTEIN"} — stored comma-separated
     * @param mealTypes     MealType enum names, e.g. {"LUNCH", "DINNER"} or {"BREAKFAST"} — stored comma-separated
     * @param ingredients   rows of {name (String), quantity (Double), unit (String)}
     */
    private long insertRecipe(SQLiteDatabase db, String name, String steps,
                               String[] dietTags, String[] mealTypes, Object[][] ingredients) {

        ContentValues recipeValues = new ContentValues();
        recipeValues.put(COL_RECIPE_NAME, name);
        recipeValues.put(COL_RECIPE_STEPS, steps);
        recipeValues.put(COL_RECIPE_DIET_TAGS, joinCsv(dietTags));
        recipeValues.put(COL_RECIPE_MEAL_TYPES, joinCsv(mealTypes));
        long recipeId = db.insert(TABLE_RECIPES, null, recipeValues);

        for (Object[] ingredient : ingredients) {
            ContentValues ingredientValues = new ContentValues();
            ingredientValues.put(COL_RI_RECIPE_ID, recipeId);
            ingredientValues.put(COL_RI_NAME, (String) ingredient[0]);
            ingredientValues.put(COL_RI_QUANTITY, (Double) ingredient[1]);
            ingredientValues.put(COL_RI_UNIT, (String) ingredient[2]);
            db.insert(TABLE_RECIPE_INGREDIENTS, null, ingredientValues);
        }

        return recipeId;
    }

    /** Joins enum-name strings into a comma-separated value for storage, e.g. {"LUNCH","DINNER"} -> "LUNCH,DINNER". */
    private static String joinCsv(String[] values) {
        StringBuilder csv = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                csv.append(",");
            }
            csv.append(values[i]);
        }
        return csv.toString();
    }
}
