package com.scrapstosavory.app.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.scrapstosavory.app.R;
import com.scrapstosavory.app.util.SettingsManager;

/**
 * Lets the user turn expiring soon warnings on or off, and choose how
 * many days before an ingredient's expiry date counts as "soon".
 *
 * These are saved through SettingsManager, which is the same place the
 * Pantry List screen reads them from when deciding whether to colour an
 * item's expiry line as a warning.
 */
public class SettingsActivity extends BaseNavigationActivity {

    private SwitchMaterial switchExpiringSoonAlerts;
    private TextInputLayout layoutExpiringDays;
    private TextInputEditText editExpiringDays;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings_title);
        setSupportActionBar(toolbar);
        setUpBottomNavigation(R.id.action_settings);

        switchExpiringSoonAlerts = findViewById(R.id.switchExpiringSoonAlerts);
        layoutExpiringDays = findViewById(R.id.layoutExpiringDays);
        editExpiringDays = findViewById(R.id.editExpiringDays);

        loadSavedSettings();

        switchExpiringSoonAlerts.setOnCheckedChangeListener((buttonView, isChecked) ->
                layoutExpiringDays.setEnabled(isChecked));

        findViewById(R.id.buttonSaveSettings).setOnClickListener(v -> onSaveClicked());
    }

    private void loadSavedSettings() {
        boolean alertsEnabled = SettingsManager.isExpiringSoonAlertsEnabled(this);
        int days = SettingsManager.getExpiringSoonDays(this);

        switchExpiringSoonAlerts.setChecked(alertsEnabled);
        editExpiringDays.setText(String.valueOf(days));
        layoutExpiringDays.setEnabled(alertsEnabled);
    }

    private void onSaveClicked() {
        layoutExpiringDays.setError(null);

        boolean alertsEnabled = switchExpiringSoonAlerts.isChecked();
        String daysText = editExpiringDays.getText() != null
                ? editExpiringDays.getText().toString().trim() : "";

        int days = SettingsManager.DEFAULT_EXPIRING_SOON_DAYS;
        if (alertsEnabled) {
            if (TextUtils.isEmpty(daysText)) {
                layoutExpiringDays.setError(getString(R.string.error_days_invalid));
                return;
            }
            try {
                days = Integer.parseInt(daysText);
            } catch (NumberFormatException e) {
                layoutExpiringDays.setError(getString(R.string.error_days_invalid));
                return;
            }
            if (days < 0) {
                layoutExpiringDays.setError(getString(R.string.error_days_invalid));
                return;
            }
        }

        SettingsManager.setExpiringSoonAlertsEnabled(this, alertsEnabled);
        SettingsManager.setExpiringSoonDays(this, days);

        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
    }
}
