package com.scrapstosavory.app.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.scrapstosavory.app.R;
import com.scrapstosavory.app.data.DatabaseHelper;
import com.scrapstosavory.app.data.IngredientNameCatalog;
import com.scrapstosavory.app.data.PantryDao;
import com.scrapstosavory.app.model.PantryCategory;
import com.scrapstosavory.app.model.PantryItem;
import com.scrapstosavory.app.util.DateUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lets the user quickly add several ingredients to the pantry in one
 * visit, instead of opening a whole form for each one. Each food type
 * is its own expandable section; tapping + next to a name bumps how
 * many of it you are adding, and it shows up in the "To add" list below
 * so you can review or remove it before saving everything at once.
 *
 * Unit, weight and expiry date are not collected here on purpose, to
 * keep this screen fast. A sensible default unit (from the same preset
 * list) is used quietly behind the scenes so recipe matching still
 * works straight away; the Pantry List edit button is where those
 * details get fine-tuned afterwards.
 */
public class AddIngredientsActivity extends AppCompatActivity {

    private PantryDao pantryDao;

    private LinearLayout containerIngredientGroups;
    private LinearLayout containerStagedList;
    private TextView textStagedEmpty;

    // How many of each ingredient are staged so far, in the order they were first tapped.
    private final Map<String, Integer> stagedCounts = new LinkedHashMap<>();
    private final Map<String, PantryCategory> categoryByName = new LinkedHashMap<>();
    private final Map<String, String> defaultUnitByName = new LinkedHashMap<>();
    private final Map<String, TextView> rowCountViews = new LinkedHashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredients);

        pantryDao = new PantryDao(new DatabaseHelper(this));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        containerIngredientGroups = findViewById(R.id.containerIngredientGroups);
        containerStagedList = findViewById(R.id.containerStagedList);
        textStagedEmpty = findViewById(R.id.textStagedEmpty);

        buildIngredientGroups();
        refreshStagedList();

        findViewById(R.id.buttonAddAll).setOnClickListener(v -> onAddAllClicked());
    }

    /** Builds one expandable section per food type, each listing its preset ingredient names. */
    private void buildIngredientGroups() {
        for (Map.Entry<PantryCategory, IngredientNameCatalog.Entry[]> group : IngredientNameCatalog.GROUPS.entrySet()) {
            PantryCategory category = group.getKey();

            LinearLayout headerRow = new LinearLayout(this);
            headerRow.setOrientation(LinearLayout.HORIZONTAL);
            headerRow.setGravity(Gravity.CENTER_VERTICAL);
            headerRow.setPadding(dp(4), dp(12), dp(4), dp(12));
            headerRow.setBackground(selectableBackground());
            headerRow.setClickable(true);
            headerRow.setFocusable(true);

            TextView label = new TextView(this);
            label.setText(category.getDisplayName());
            label.setTextColor(ContextCompat.getColor(this, R.color.pantry_text_primary));
            label.setTextSize(16);
            label.setTypeface(label.getTypeface(), android.graphics.Typeface.BOLD);
            label.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView arrow = new TextView(this);
            arrow.setText(R.string.accordion_collapsed_arrow);
            arrow.setTextColor(ContextCompat.getColor(this, R.color.pantry_text_secondary));
            arrow.setTextSize(15);

            headerRow.addView(label);
            headerRow.addView(arrow);

            LinearLayout itemsContainer = new LinearLayout(this);
            itemsContainer.setOrientation(LinearLayout.VERTICAL);
            itemsContainer.setVisibility(View.GONE);
            itemsContainer.setPadding(dp(16), 0, dp(4), dp(4));

            for (IngredientNameCatalog.Entry entry : group.getValue()) {
                defaultUnitByName.put(entry.name, entry.defaultUnit);
                categoryByName.put(entry.name, category);
                itemsContainer.addView(buildIngredientRow(entry.name));
            }

            headerRow.setOnClickListener(v -> {
                boolean isExpanded = itemsContainer.getVisibility() == View.VISIBLE;
                itemsContainer.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
                arrow.setText(isExpanded ? R.string.accordion_collapsed_arrow : R.string.accordion_expanded_arrow);
            });

            containerIngredientGroups.addView(headerRow);
            containerIngredientGroups.addView(itemsContainer);
        }
    }

    /** One row: the ingredient name on the left, its staged count, then minus and plus controls. */
    private LinearLayout buildIngredientRow(String name) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(8), dp(8), dp(8), dp(8));

        TextView nameView = new TextView(this);
        nameView.setText(name);
        nameView.setTextColor(ContextCompat.getColor(this, R.color.pantry_text_primary));
        nameView.setTextSize(14);
        nameView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView countView = new TextView(this);
        countView.setTextColor(ContextCompat.getColor(this, R.color.pantry_text_secondary));
        countView.setTextSize(14);
        countView.setPadding(dp(8), 0, dp(8), 0);
        rowCountViews.put(name, countView);

        TextView minusButton = stepperButton("−");
        minusButton.setOnClickListener(v -> changeStagedCount(name, -1));

        TextView plusButton = stepperButton("+");
        plusButton.setOnClickListener(v -> changeStagedCount(name, 1));

        row.addView(nameView);
        row.addView(countView);
        row.addView(minusButton);
        row.addView(plusButton);
        return row;
    }

    private TextView stepperButton(String symbol) {
        TextView button = new TextView(this);
        button.setText(symbol);
        button.setTextColor(ContextCompat.getColor(this, R.color.pantry_primary));
        button.setTextSize(16);
        button.setPadding(dp(12), dp(4), dp(12), dp(4));
        button.setBackground(selectableBackground());
        button.setClickable(true);
        button.setFocusable(true);
        return button;
    }

    private void changeStagedCount(String name, int delta) {
        int newCount = Math.max(0, stagedCounts.getOrDefault(name, 0) + delta);
        if (newCount == 0) {
            stagedCounts.remove(name);
        } else {
            stagedCounts.put(name, newCount);
        }

        TextView countView = rowCountViews.get(name);
        if (countView != null) {
            countView.setText(newCount > 0 ? String.valueOf(newCount) : "");
        }

        refreshStagedList();
    }

    /** Rebuilds the "To add" review list from stagedCounts, name on the left, controls on the right. */
    private void refreshStagedList() {
        containerStagedList.removeAllViews();

        if (stagedCounts.isEmpty()) {
            textStagedEmpty.setVisibility(View.VISIBLE);
            return;
        }
        textStagedEmpty.setVisibility(View.GONE);

        for (Map.Entry<String, Integer> staged : stagedCounts.entrySet()) {
            String name = staged.getKey();
            int count = staged.getValue();

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(4), dp(8), dp(4), dp(8));

            TextView nameView = new TextView(this);
            nameView.setText(getString(R.string.add_ingredients_staged_row_format, name, count));
            nameView.setTextColor(ContextCompat.getColor(this, R.color.pantry_text_primary));
            nameView.setTextSize(14);
            nameView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView removeButton = stepperButton("✕");
            removeButton.setTextColor(ContextCompat.getColor(this, R.color.pantry_error));
            removeButton.setOnClickListener(v -> {
                stagedCounts.remove(name);
                TextView countView = rowCountViews.get(name);
                if (countView != null) {
                    countView.setText("");
                }
                refreshStagedList();
            });

            row.addView(nameView);
            row.addView(removeButton);
            containerStagedList.addView(row);
        }
    }

    private void onAddAllClicked() {
        if (stagedCounts.isEmpty()) {
            Toast.makeText(this, R.string.error_no_ingredients_staged, Toast.LENGTH_SHORT).show();
            return;
        }

        String today = DateUtils.todayIso();
        int addedCount = 0;

        for (Map.Entry<String, Integer> staged : stagedCounts.entrySet()) {
            String name = staged.getKey();
            double quantity = staged.getValue();
            String unit = defaultUnitByName.get(name);
            PantryCategory category = categoryByName.get(name);

            PantryItem item = new PantryItem(name, quantity, unit, null, today, category);
            pantryDao.insert(item);
            addedCount++;
        }

        Toast.makeText(this, getString(R.string.add_ingredients_saved_format, addedCount), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, PantryListActivity.class));
        finish();
    }

    private Drawable selectableBackground() {
        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        return ContextCompat.getDrawable(this, outValue.resourceId);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
