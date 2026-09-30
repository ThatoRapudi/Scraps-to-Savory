package com.scrapstosavory.app.util;

/**
 * Shared quantity formatting, used anywhere a PantryItem or
 * RecipeIngredient quantity is shown as text (Pantry List rows, the
 * Add/Edit form) so there's a single implementation instead of a copy
 * in each screen.
 */
public final class QuantityUtils {

    private QuantityUtils() {
    }

    /** Strips a trailing ".0" so a whole number ("3 pcs") doesn't display as "3.0 pcs". */
    public static String format(double quantity) {
        if (quantity == Math.floor(quantity) && !Double.isInfinite(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }
}
