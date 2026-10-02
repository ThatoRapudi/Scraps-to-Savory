package com.scrapstosavory.app.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.scrapstosavory.app.R;
import com.scrapstosavory.app.data.DatabaseHelper;
import com.scrapstosavory.app.data.PantryDao;
import com.scrapstosavory.app.model.PantryCategory;
import com.scrapstosavory.app.model.PantryItem;
import com.scrapstosavory.app.util.DateUtils;
import com.scrapstosavory.app.util.QuantityUtils;
import com.scrapstosavory.app.util.SettingsManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the first screen the app opens on. It shows every ingredient
 * currently in the pantry, grouped into an expandable section per food
 * category (meat and poultry, vegetables, and so on) instead of one
 * long flat list, so it is easier to see what has already been added.
 * The + button opens AddIngredientsActivity to add new ingredients, and
 * the edit icon on a row opens EditIngredientActivity to fine-tune one
 * that is already in the pantry.
 *
 * The list is reloaded in onResume() instead of onCreate() so that
 * coming back from adding, editing, or deleting an ingredient always
 * shows the latest data, and so the pantry still shows correctly after
 * closing and reopening the app.
 */
public class PantryListActivity extends BaseNavigationActivity {

    private PantryDao pantryDao;
    private LinearLayout containerPantryGroups;
    private TextView textEmptyPantry;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.pantry_list_title);
        setSupportActionBar(toolbar);
        setUpNavigationDrawer(toolbar, R.id.action_pantry);

        pantryDao = new PantryDao(new DatabaseHelper(this));

        containerPantryGroups = findViewById(R.id.containerPantryGroups);
        textEmptyPantry = findViewById(R.id.textEmptyPantry);

        FloatingActionButton fabAddItem = findViewById(R.id.fabAddItem);
        fabAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(PantryListActivity.this, AddIngredientsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPantryList();
    }

    /** Groups the pantry by category and rebuilds the accordion from scratch. */
    private void refreshPantryList() {
        List<PantryItem> items = pantryDao.getAll();
        containerPantryGroups.removeAllViews();

        boolean isEmpty = items.isEmpty();
        containerPantryGroups.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        textEmptyPantry.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (isEmpty) {
            return;
        }

        Map<PantryCategory, List<PantryItem>> grouped = groupByCategory(items);
        for (PantryCategory category : PantryCategory.values()) {
            List<PantryItem> itemsInCategory = grouped.get(category);
            if (itemsInCategory != null && !itemsInCategory.isEmpty()) {
                addCategorySection(category, itemsInCategory);
            }
        }
    }

    /**
     * Buckets the items by category, keeping PantryCategory's own
     * declaration order (meat and poultry first, then vegetables, and
     * so on) rather than whatever order the database happened to
     * return, since that order is what the accordion is built in.
     */
    private Map<PantryCategory, List<PantryItem>> groupByCategory(List<PantryItem> items) {
        Map<PantryCategory, List<PantryItem>> grouped = new LinkedHashMap<>();
        for (PantryCategory category : PantryCategory.values()) {
            grouped.put(category, new ArrayList<>());
        }
        for (PantryItem item : items) {
            grouped.get(item.getCategory()).add(item);
        }
        return grouped;
    }

    /** Adds one expandable header plus its item rows to the accordion for a single category. */
    private void addCategorySection(PantryCategory category, List<PantryItem> itemsInCategory) {
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);
        headerRow.setPadding(dp(16), dp(14), dp(16), dp(14));
        headerRow.setBackground(selectableBackground());
        headerRow.setClickable(true);
        headerRow.setFocusable(true);

        TextView label = new TextView(this);
        label.setText(getString(R.string.pantry_category_header_format, category.getDisplayName(), itemsInCategory.size()));
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

        for (PantryItem item : itemsInCategory) {
            itemsContainer.addView(buildPantryRow(item));
        }

        headerRow.setOnClickListener(v -> {
            boolean isExpanded = itemsContainer.getVisibility() == View.VISIBLE;
            itemsContainer.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
            arrow.setText(isExpanded ? R.string.accordion_collapsed_arrow : R.string.accordion_expanded_arrow);
        });

        containerPantryGroups.addView(headerRow);
        containerPantryGroups.addView(itemsContainer);
    }

    /** Inflates one item_pantry card for a single PantryItem and wires up its edit/delete buttons. */
    private View buildPantryRow(PantryItem item) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_pantry, containerPantryGroups, false);

        TextView textItemName = row.findViewById(R.id.textItemName);
        TextView textItemQuantity = row.findViewById(R.id.textItemQuantity);
        TextView textItemExpiry = row.findViewById(R.id.textItemExpiry);
        ImageButton buttonEdit = row.findViewById(R.id.buttonEdit);
        ImageButton buttonDelete = row.findViewById(R.id.buttonDelete);

        textItemName.setText(item.getName());
        if (item.hasWeight()) {
            textItemQuantity.setText(String.format("%s %s (%s %s) • %s",
                    QuantityUtils.format(item.getQuantity()), item.getUnit(),
                    QuantityUtils.format(item.getWeightValue()), item.getWeightUnit(),
                    item.getCategory().getDisplayName()));
        } else {
            textItemQuantity.setText(String.format("%s %s • %s",
                    QuantityUtils.format(item.getQuantity()), item.getUnit(), item.getCategory().getDisplayName()));
        }

        Context context = textItemExpiry.getContext();
        String effectiveExpiryDate = item.getEffectiveExpiryDate();
        int labelRes = item.hasManualExpiryDate() ? R.string.expires_on_format : R.string.expires_estimated_format;

        textItemExpiry.setVisibility(View.VISIBLE);
        textItemExpiry.setText(context.getString(labelRes, DateUtils.toDisplay(effectiveExpiryDate)));

        boolean alertsEnabled = SettingsManager.isExpiringSoonAlertsEnabled(context);
        boolean isExpiringSoon = alertsEnabled
                && DateUtils.daysUntil(effectiveExpiryDate) <= SettingsManager.getExpiringSoonDays(context);
        int colorRes = isExpiringSoon ? R.color.pantry_error : R.color.pantry_text_secondary;
        textItemExpiry.setTextColor(ContextCompat.getColor(context, colorRes));

        buttonEdit.setOnClickListener(v -> onEditClicked(item));
        buttonDelete.setOnClickListener(v -> onDeleteClicked(item));

        return row;
    }

    private void onEditClicked(PantryItem item) {
        Intent intent = new Intent(this, EditIngredientActivity.class);
        intent.putExtra(EditIngredientActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    private void onDeleteClicked(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(getString(R.string.delete_confirm_message, item.getName()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    pantryDao.delete(item.getId());
                    refreshPantryList();
                    Toast.makeText(this, R.string.item_deleted, Toast.LENGTH_SHORT).show();
                })
                .show();
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
