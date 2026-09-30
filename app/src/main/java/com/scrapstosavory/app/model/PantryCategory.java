package com.scrapstosavory.app.model;

/**
 * Broad category a pantry item falls into. Used to estimate a sensible
 * expiry date automatically (dateAdded + shelfLifeDays) when the user
 * doesn't type an exact expiry date themselves — feeds the "expiring
 * soon" alert toggle on the Settings screen.
 *
 * These day counts are starting defaults and can be tuned later; they are
 * intentionally simple (no NLP / no external food database) per the
 * assignment's scope.
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
