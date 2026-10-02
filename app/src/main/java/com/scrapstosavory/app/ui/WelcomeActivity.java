package com.scrapstosavory.app.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.scrapstosavory.app.R;

/**
 * The first screen shown when the app opens. It just explains what
 * Scraps to Savory is for, in plain language, before handing off to the
 * Pantry List screen. This screen is never navigated back to, tapping
 * the button closes it so the back button from Pantry List exits the
 * app instead of returning here.
 */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        findViewById(R.id.buttonGetStarted).setOnClickListener(v -> {
            startActivity(new Intent(this, PantryListActivity.class));
            finish();
        });
    }
}
