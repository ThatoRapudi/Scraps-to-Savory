package com.scrapstosavory.app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.scrapstosavory.app.model.DietTag;
import com.scrapstosavory.app.model.MealType;
import com.scrapstosavory.app.model.Recipe;
import com.scrapstosavory.app.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Reads recipes, and their ingredients, back out of the database. Recipes
 * are only ever loaded once when the app first runs (see DatabaseHelper),
 * so this class only ever reads them, there is no add/edit/delete here
 * like there is for pantry items.
 */
public class RecipeDao {

    private final DatabaseHelper databaseHelper;

    public RecipeDao(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /** Every recipe in the database, each one already carrying its full ingredient list. */
    public List<Recipe> getAll() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_RECIPES, null, null, null, null, null,
                DatabaseHelper.COL_RECIPE_NAME + " ASC");

        while (cursor.moveToNext()) {
            Recipe recipe = fromCursor(cursor);
            recipe.setIngredients(getIngredientsForRecipe(db, recipe.getId()));
            recipes.add(recipe);
        }
        cursor.close();
        db.close();
        return recipes;
    }

    /** One recipe by id, with its ingredients attached. Used by the recipe detail screen. */
    public Recipe getById(long id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_RECIPES, null,
                DatabaseHelper.COL_RECIPE_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null);

        Recipe recipe = null;
        if (cursor.moveToFirst()) {
            recipe = fromCursor(cursor);
            recipe.setIngredients(getIngredientsForRecipe(db, recipe.getId()));
        }
        cursor.close();
        db.close();
        return recipe;
    }

    /**
     * Saves the user's thumbs up/down rating and optional note for a
     * recipe. liked can be true, false, or null to clear a rating the
     * user already gave (tapping the same thumb again).
     */
    public void updateFeedback(long recipeId, Boolean liked, String note) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (liked == null) {
            values.putNull(DatabaseHelper.COL_RECIPE_LIKED);
        } else {
            values.put(DatabaseHelper.COL_RECIPE_LIKED, liked ? 1 : 0);
        }
        values.put(DatabaseHelper.COL_RECIPE_NOTE, note);
        db.update(DatabaseHelper.TABLE_RECIPES, values,
                DatabaseHelper.COL_RECIPE_ID + " = ?", new String[]{String.valueOf(recipeId)});
        db.close();
    }

    private List<RecipeIngredient> getIngredientsForRecipe(SQLiteDatabase db, long recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_RECIPE_INGREDIENTS, null,
                DatabaseHelper.COL_RI_RECIPE_ID + " = ?", new String[]{String.valueOf(recipeId)},
                null, null, null);

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RI_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RI_NAME));
            double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RI_QUANTITY));
            String unit = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RI_UNIT));
            ingredients.add(new RecipeIngredient(id, recipeId, name, quantity, unit));
        }
        cursor.close();
        return ingredients;
    }

    private Recipe fromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RECIPE_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RECIPE_NAME));
        String steps = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RECIPE_STEPS));
        String dietTagsCsv = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RECIPE_DIET_TAGS));
        String mealTypesCsv = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RECIPE_MEAL_TYPES));

        int likedColumnIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RECIPE_LIKED);
        Boolean liked = cursor.isNull(likedColumnIndex) ? null : cursor.getInt(likedColumnIndex) != 0;
        String note = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RECIPE_NOTE));

        Recipe recipe = new Recipe(id, name, steps);
        recipe.setDietTags(parseDietTags(dietTagsCsv));
        recipe.setMealTypes(parseMealTypes(mealTypesCsv));
        recipe.setLiked(liked);
        recipe.setNote(note);
        return recipe;
    }

    private Set<DietTag> parseDietTags(String csv) {
        Set<DietTag> tags = EnumSet.noneOf(DietTag.class);
        for (String value : splitCsv(csv)) {
            DietTag tag = DietTag.fromDbValue(value);
            if (tag != null) {
                tags.add(tag);
            }
        }
        return tags;
    }

    private Set<MealType> parseMealTypes(String csv) {
        Set<MealType> types = EnumSet.noneOf(MealType.class);
        for (String value : splitCsv(csv)) {
            MealType type = MealType.fromDbValue(value);
            if (type != null) {
                types.add(type);
            }
        }
        return types;
    }

    /** Splits the comma-separated text stored in the database back out into separate values. */
    private String[] splitCsv(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return new String[0];
        }
        return csv.split(",");
    }
}
