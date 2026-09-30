package com.scrapstosavory.app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.scrapstosavory.app.model.PantryCategory;
import com.scrapstosavory.app.model.PantryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Data-access layer for pantry_items. Every screen that touches the user's
 * pantry (Pantry List, Add/Edit, and the strict-matching logic on Suggested
 * Recipes) goes through this class rather than talking to SQLite directly.
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

    /** Read (all). Ordered by name so the list is predictable and easy to scan. */
    public List<PantryItem> getAll() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PANTRY_ITEMS,
                null, null, null, null, null,
                DatabaseHelper.COL_PANTRY_NAME + " ASC");

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

    private ContentValues toContentValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_PANTRY_NAME, item.getName());
        values.put(DatabaseHelper.COL_PANTRY_QUANTITY, item.getQuantity());
        values.put(DatabaseHelper.COL_PANTRY_UNIT, item.getUnit());
        values.put(DatabaseHelper.COL_PANTRY_EXPIRY_DATE, item.getExpiryDate());
        values.put(DatabaseHelper.COL_PANTRY_DATE_ADDED, item.getDateAdded());
        values.put(DatabaseHelper.COL_PANTRY_CATEGORY, item.getCategory().name());
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

        return new PantryItem(id, name, quantity, unit, expiryDate, dateAdded,
                PantryCategory.fromDbValue(categoryValue));
    }
}
