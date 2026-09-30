package com.scrapstosavory.app.model;

/**
 * The general type of food a pantry item is. This is used to guess how
 * long an item should last (dateAdded + defaultShelfLifeDays) when the
 * user does not type in their own expiry date.
 *
 * The day counts below are just simple starting guesses and can be
 * changed. There is no real food database behind this, just a rough
 * estimate for each category.
 */
public enum PantryCategory {

    FRESH_PRODUCE("Fresh Produce", 5),
    DAIRY("Dairy", 7),
    FROZEN_PROTEIN("Frozen Protein", 90),
    MEAL_PREP("Meal-Prepped Leftovers", 4),
    DRY_GOODS("Dry Goods / Pantry Staples", 180),
    OTHER("Other", 14);

    private final String displayName;
    private final int defaultShelfLifeDays;

    PantryCategory(String displayName, int defaultShelfLifeDays) {
        this.displayName = displayName;
        this.defaultShelfLifeDays = defaultShelfLifeDays;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDefaultShelfLifeDays() {
        return defaultShelfLifeDays;
    }

    /**
     * Stored in the database as a plain string (the enum's name), so it's
     * stable even if displayName wording changes later.
     */
    public static PantryCategory fromDbValue(String value) {
        if (value == null) {
            return OTHER;
        }
        try {
            return PantryCategory.valueOf(value);
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }
}
