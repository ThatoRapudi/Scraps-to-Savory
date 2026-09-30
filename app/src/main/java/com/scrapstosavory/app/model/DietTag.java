package com.scrapstosavory.app.model;

/**
 * Dietary label attached to a recipe. A recipe can carry more than one
 * (e.g. a lentil stew can be both VEGAN and HIGH_PROTEIN), so Recipe
 * stores a Set<DietTag> rather than a single value.
 *
 * Used for the informational badges AND the tappable filter chips on the
 * Suggested Recipes screen.
 */
public enum DietTag {

    VEGETARIAN("Vegetarian"),
    VEGAN("Vegan"),
    HIGH_PROTEIN("High-Protein");

    private final String displayName;

    DietTag(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DietTag fromDbValue(String value) {
        try {
            return DietTag.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}
