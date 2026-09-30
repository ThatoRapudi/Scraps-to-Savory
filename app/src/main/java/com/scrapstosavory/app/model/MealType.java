package com.scrapstosavory.app.model;

/**
 * When a recipe is typically eaten. Kept as three separate values (rather
 * than a combined "Lunch & Dinner") so a recipe's meal-time filter chips
 * are precise. A recipe can fit more than one — e.g. "Chicken & Rice
 * One-Pot" is both Lunch and Dinner — so Recipe stores a Set<MealType>,
 * the same pattern used for DietTag.
 */
public enum MealType {

    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner");

    private final String displayName;

    MealType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MealType fromDbValue(String value) {
        try {
            return MealType.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}
