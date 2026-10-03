package com.scrapstosavory.app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.scrapstosavory.app.model.PantryCategory;
import com.scrapstosavory.app.model.PantryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles all the database work for pantry_items (adding, reading,
 * updating and deleting). Any screen that needs to touch the pantry goes
 * through this class instead of writing its own SQLite code.
 */
public class PantryDao {

    private final DatabaseHelper databaseHelper;

    public PantryDao(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /** Create. Returns the new row's id, or -1 if the insert failed. */
    public long insert(PantryItem item) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        long id = db.insert(DatabaseHelper.TABLE_PANTRY_ITEMS, null, toContentValues(item));
        db.close();
        return id;
    }

    /**
     * Read (all). Grouped by category in a fixed, sensible order (meat
     * and poultry first, then vegetables, spices and seasonings, dairy
     * and eggs, grains and starches, with anything else last), and
     * alphabetical by name within each group, so the list is easy to
     * scan rather than just a flat alphabetical jumble.
     */
    public List<PantryItem> getAll() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String categoryOrder = "CASE " + DatabaseHelper.COL_PANTRY_CATEGORY
                + " WHEN 'MEAT_AND_POULTRY' THEN 0"
                + " WHEN 'VEGETABLES' THEN 1"
                + " WHEN 'SPICES_AND_SEASONINGS' THEN 2"
                + " WHEN 'DAIRY_AND_EGGS' THEN 3"
                + " WHEN 'GRAINS_AND_STARCHES' THEN 4"
                + " ELSE 5 END";
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PANTRY_ITEMS,
                null, null, null, null, null,
                categoryOrder + ", " + DatabaseHelper.COL_PANTRY_NAME + " ASC");

        while (cursor.moveToNext()) {
            items.add(fromCursor(cursor));
        }
        cursor.close();
        db.close();
        return items;
    }

    /** Read (single). Returns null if no row has that id. */
    public PantryItem getById(long id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PANTRY_ITEMS,
                null,
                DatabaseHelper.COL_PANTRY_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);

        PantryItem item = null;
        if (cursor.moveToFirst()) {
            item = fromCursor(cursor);
        }
        cursor.close();
        db.close();
        return item;
    }

    /** Update. Returns the number of rows affected (0 or 1). */
    public int update(PantryItem item) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int rows = db.update(
                DatabaseHelper.TABLE_PANTRY_ITEMS,
                toContentValues(item),
                DatabaseHelper.COL_PANTRY_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
        db.close();
        return rows;
    }

    /** Delete. Returns the number of rows affected (0 or 1). */
    public int delete(long id) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int rows = db.delete(
                DatabaseHelper.TABLE_PANTRY_ITEMS,
                DatabaseHelper.COL_PANTRY_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    /** Delete every item in one category at once. Returns how many rows were removed. */
    public int deleteByCategory(PantryCategory category) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int rows = db.delete(
                DatabaseHelper.TABLE_PANTRY_ITEMS,
                DatabaseHelper.COL_PANTRY_CATEGORY + " = ?",
                new String[]{category.name()});
        db.close();
        return rows;
    }

    /** Delete every item in the pantry at once. Returns how many rows were removed. */
    public int deleteAll() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_PANTRY_ITEMS, null, null);
        db.close();
        return rows;
    }

    private ContentValues toContentValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_PANTRY_NAME, item.getName());
        values.put(DatabaseHelper.COL_PANTRY_QUANTITY, item.getQuantity());
        values.put(DatabaseHelper.COL_PANTRY_UNIT, item.getUnit());
        values.put(DatabaseHelper.COL_PANTRY_EXPIRY_DATE, item.getExpiryDate());
        values.put(DatabaseHelper.COL_PANTRY_DATE_ADDED, item.getDateAdded());
        values.put(DatabaseHelper.COL_PANTRY_CATEGORY, item.getCategory().name());
        if (item.hasWeight()) {
            values.put(DatabaseHelper.COL_PANTRY_WEIGHT_VALUE, item.getWeightValue());
            values.put(DatabaseHelper.COL_PANTRY_WEIGHT_UNIT, item.getWeightUnit());
        } else {
            values.putNull(DatabaseHelper.COL_PANTRY_WEIGHT_VALUE);
            values.putNull(DatabaseHelper.COL_PANTRY_WEIGHT_UNIT);
        }
        return values;
    }

    private PantryItem fromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_NAME));
        double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_QUANTITY));
        String unit = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_UNIT));
        String expiryDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_EXPIRY_DATE));
        String dateAdded = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_DATE_ADDED));
        String categoryValue = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_CATEGORY));

        PantryItem item = new PantryItem(id, name, quantity, unit, expiryDate, dateAdded,
                PantryCategory.fromDbValue(categoryValue));

        int weightValueIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_WEIGHT_VALUE);
        int weightUnitIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_WEIGHT_UNIT);
        if (!cursor.isNull(weightValueIndex)) {
            item.setWeightValue(cursor.getDouble(weightValueIndex));
            item.setWeightUnit(cursor.getString(weightUnitIndex));
        }

        return item;
    }
}
