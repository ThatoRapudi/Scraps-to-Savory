package com.scrapstosavory.app.logic;

/**
 * Shared unit handling used by both StrictMatcher (checking whether the
 * pantry has enough of something) and the "I made this" pantry deduction
 * on the Recipe Detail screen (actually subtracting the amount used).
 *
 * Grams/kilograms and millilitres/litres can be converted between each
 * other. Everything else (pcs, tsp, tbsp, cup) only ever matches itself,
 * since there's no sensible way to convert, say, teaspoons into pieces.
 */
public final class UnitConverter {

    private UnitConverter() {
    }

    /** True if these two units can be sensibly compared or converted against each other. */
    public static boolean sameUnitFamily(String unitA, String unitB) {
        String familyA = unitFamily(unitA);
        return familyA != null && familyA.equals(unitFamily(unitB));
    }

    /** Groups units that can be converted into each other. Everything else only matches itself. */
    private static String unitFamily(String unit) {
        if (unit == null) {
            return null;
        }
        switch (unit.trim().toLowerCase()) {
            case "g":
            case "kg":
                return "mass";
            case "ml":
            case "l":
                return "volume";
            default:
                return unit.trim().toLowerCase();
        }
    }

    /** Converts a quantity into a common base unit (grams or millilitres) so amounts can be compared fairly. */
    public static Double toBaseUnit(double quantity, String unit) {
        if (unit == null) {
            return null;
        }
        switch (unit.trim().toLowerCase()) {
            case "g":
            case "ml":
            case "pcs":
            case "tsp":
            case "tbsp":
            case "cup":
                return quantity;
            case "kg":
            case "l":
                return quantity * 1000;
            default:
                return null;
        }
    }

    /**
     * Converts a quantity from one unit into another, as long as the two
     * units are in the same family (e.g. g into kg). Returns null if
     * they can't be sensibly compared, instead of guessing.
     */
    public static Double convert(double quantity, String fromUnit, String toUnit) {
        if (!sameUnitFamily(fromUnit, toUnit)) {
            return null;
        }
        Double inBaseUnit = toBaseUnit(quantity, fromUnit);
        if (inBaseUnit == null) {
            return null;
        }
        switch (toUnit.trim().toLowerCase()) {
            case "kg":
            case "l":
                return inBaseUnit / 1000;
            default:
                return inBaseUnit;
        }
    }
}
