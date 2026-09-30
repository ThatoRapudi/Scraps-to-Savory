package com.scrapstosavory.app.util;

/**
 * Values shared across the app.
 *
 * The list of units is fixed on purpose and reused everywhere a unit is
 * picked, both for pantry items and for recipe ingredients. Because the
 * user can only pick from this list (not type their own), a unit like
 * "g" always means the same thing everywhere, so comparing units later
 * is just a simple text comparison instead of needing to convert between
 * different units.
 */
public final class Constants {

    public static final String[] UNITS = {"g", "kg", "ml", "l", "pcs", "tsp", "tbsp", "cup"};

    private Constants() {
    }
}
