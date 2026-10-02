package com.scrapstosavory.app.model;

/**
 * The general type of food a pantry item is. This is used two ways:
 * to guess how long an item should last (dateAdded + defaultShelfLifeDays)
 * when the user does not type in their own expiry date, and to group
 * the preset ingredient names on the Add/Edit screen (see
 * IngredientNameCatalog), so a tomato sits under Vegetables, rice sits
 * under Grains and starches, and so on.
 *
 * The day counts below are just simple starting guesses and can be
 * changed. There is no real food database behind this, just a rough
 * estimate for each category.
 */
public enum PantryCategory {

    MEAT_AND_POULTRY("Meat and poultry", 3),
    VEGETABLES("Vegetables", 5),
    SPICES_AND_SEASONINGS("Spices and seasonings", 365),
    DAIRY_AND_EGGS("Dairy and eggs", 7),
    GRAINS_AND_STARCHES("Grains and starches", 180),
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
     * stable even if displayName wording changes later. Falls back to
     * OTHER for anything that does not match, which also covers pantry
     * items saved under the older set of category names.
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
