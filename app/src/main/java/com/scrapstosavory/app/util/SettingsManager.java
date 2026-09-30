package com.scrapstosavory.app.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Reads and writes the user's saved preferences, using SharedPreferences
 * so the choices made on the Settings screen are remembered the next
 * time the app is opened.
 *
 * Any screen that needs to check or change a setting should go through
 * this class instead of writing its own SharedPreferences code, so
 * there is only one place that knows the actual preference names and
 * default values.
 */
public final class SettingsManager {

    private static final String PREFS_NAME = "scraps_to_savory_settings";
    private static final String KEY_EXPIRING_SOON_ALERTS_ENABLED = "expiring_soon_alerts_enabled";
    private static final String KEY_EXPIRING_SOON_DAYS = "expiring_soon_days";

    public static final boolean DEFAULT_ALERTS_ENABLED = true;
    public static final int DEFAULT_EXPIRING_SOON_DAYS = 3;

    private SettingsManager() {
    }

    /** Whether the pantry list should warn about ingredients expiring soon at all. */
    public static boolean isExpiringSoonAlertsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_EXPIRING_SOON_ALERTS_ENABLED, DEFAULT_ALERTS_ENABLED);
    }

    public static void setExpiringSoonAlertsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_EXPIRING_SOON_ALERTS_ENABLED, enabled).apply();
    }

    /** How many days before an item's expiry date it should start being flagged as "soon". */
    public static int getExpiringSoonDays(Context context) {
        return prefs(context).getInt(KEY_EXPIRING_SOON_DAYS, DEFAULT_EXPIRING_SOON_DAYS);
    }

    public static void setExpiringSoonDays(Context context, int days) {
        prefs(context).edit().putInt(KEY_EXPIRING_SOON_DAYS, days).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
