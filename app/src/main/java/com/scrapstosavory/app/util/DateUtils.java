package com.scrapstosavory.app.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Helper class for working with dates. Two formats are used, for two
 * different reasons:
 *
 * - ISO_PATTERN ("yyyy-MM-dd") is what actually gets saved to the
 *   database. Dates written this way sort correctly as plain text,
 *   which matters later for checking if something is expiring soon.
 *
 * - DISPLAY_PATTERN ("dd/MM/yyyy") is only used to show a date on
 *   screen. It is never saved, it is just built from the stored ISO
 *   date using toDisplay() right before showing it in a TextView.
 *
 * This is kept simple on purpose (no extra date libraries) so it still
 * works on older Android versions without extra setup.
 */
public final class DateUtils {

    public static final String ISO_PATTERN = "yyyy-MM-dd";
    public static final String DISPLAY_PATTERN = "dd/MM/yyyy";

    private DateUtils() {
    }

    /** Today's date as "yyyy-MM-dd", used as PantryItem.dateAdded for new items. */
    public static String todayIso() {
        return format(Calendar.getInstance());
    }

    /** Builds an ISO date string from the (year, month, day) a DatePickerDialog returns. */
    public static String toIso(int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, monthOfYear, dayOfMonth);
        return format(calendar);
    }

    /** Splits a stored "yyyy-MM-dd" string back into a Calendar, e.g. to pre-fill the DatePickerDialog when editing. */
    public static Calendar parseIso(String isoDate) {
        Calendar calendar = Calendar.getInstance();
        if (isoDate == null || isoDate.trim().isEmpty()) {
            return calendar;
        }
        try {
            Date parsed = new SimpleDateFormat(ISO_PATTERN, Locale.getDefault()).parse(isoDate);
            if (parsed != null) {
                calendar.setTime(parsed);
            }
        } catch (Exception ignored) {
            // Falls back to "today" if the stored value is somehow malformed.
        }
        return calendar;
    }

    /**
     * Converts a stored ISO date ("2026-10-02") into the DD/MM/YYYY string
     * shown to the user ("02/10/2026"). Returns an empty string for
     * null/blank input, and falls back to the raw value if it's somehow
     * not a valid ISO date, rather than crashing the screen over a
     * malformed date.
     */
    public static String toDisplay(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) {
            return "";
        }
        try {
            Date parsed = new SimpleDateFormat(ISO_PATTERN, Locale.getDefault()).parse(isoDate);
            if (parsed == null) {
                return isoDate;
            }
            return new SimpleDateFormat(DISPLAY_PATTERN, Locale.getDefault()).format(parsed);
        } catch (Exception e) {
            return isoDate;
        }
    }

    private static String format(Calendar calendar) {
        return new SimpleDateFormat(ISO_PATTERN, Locale.getDefault()).format(calendar.getTime());
    }
}
