package com.scrapstosavory.app.ui;

import android.app.DatePickerDialog;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.scrapstosavory.app.R;
import com.scrapstosavory.app.data.DatabaseHelper;
import com.scrapstosavory.app.data.IngredientNameCatalog;
import com.scrapstosavory.app.data.PantryDao;
import com.scrapstosavory.app.model.PantryCategory;
import com.scrapstosavory.app.model.PantryItem;
import com.scrapstosavory.app.util.Constants;
import com.scrapstosavory.app.util.DateUtils;
import com.scrapstosavory.app.util.QuantityUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This screen is used for both adding a new pantry item and editing an
 * existing one. It opens in "add" mode from the + button on the Pantry
 * List screen, or in "edit" mode when a row's edit button is tapped,
 * which passes the item's id in through EXTRA_ITEM_ID.
 *
 * The ingredient name is picked from one expandable section per food
 * type (Meat and poultry, Vegetables, and so on) instead of typed in,
 * so there is nothing to spell correctly or misname. Picking a name
 * also picks its category, since each section already is a category,
 * and sets a sensible starting unit for it (tomatoes default to
 * pieces, rice defaults to kilograms), instead of leaving the unit
 * stuck on grams for everything. Quantity stays a plain count of how
 * many were bought; an exact weight in grams is a separate, optional
 * field for anyone who wants that extra detail.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "com.scrapstosavory.app.EXTRA_ITEM_ID";
    private static final long NO_ITEM_ID = -1L;

    private PantryDao pantryDao;
    private long editingItemId = NO_ITEM_ID;
    private String dateAddedForSave;
    private String selectedExpiryDate; // null until the user picks one

    private LinearLayout containerIngredientGroups;
    private TextView textSelectedIngredient;
    private TextInputLayout layoutQuantity;
    private TextInputEditText editIngredientQuantity;
    private TextInputLayout layoutWeightGrams;
    private TextInputEditText editWeightGrams;
    private Spinner spinnerUnit;
    private TextView textExpiryDate;
    private TextView buttonClearExpiry;

    // What the user has picked from the expandable ingredient sections.
    private String selectedIngredientName;
    private PantryCategory selectedCategory;

    // The row view currently shown as picked, so it can be un-bolded if the user picks a different one.
    private TextView selectedItemView;

    // The default unit for each preset ingredient name, filled in while the sections are built.
    private final Map<String, String> defaultUnitByName = new HashMap<>();

    // Lets selectIngredient() find and re-style a row by name, and setUpIngredientGroups()
    // find and expand the right section when editing an existing item.
    private final Map<String, TextView> itemRowViews = new HashMap<>();
    private final Map<PantryCategory, LinearLayout> groupItemsContainers = new HashMap<>();
    private final Map<PantryCategory, TextView> groupArrowViews = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        pantryDao = new PantryDao(new DatabaseHelper(this));
        editingItemId = getIntent().getLongExtra(EXTRA_ITEM_ID, NO_ITEM_ID);
        boolean isEditMode = editingItemId != NO_ITEM_ID;

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(isEditMode ? R.string.edit_ingredient_title : R.string.add_ingredient_title);
        setSupportActionBar(toolbar);

        bindViews();
        setUpUnitSpinner();
        setUpExpiryDatePicker();

        if (isEditMode) {
            populateFieldsForEdit();
        } else {
            dateAddedForSave = DateUtils.todayIso();
            setUpIngredientGroups(null, null);
        }

        findViewById(R.id.buttonSaveIngredient).setOnClickListener(v -> onSaveClicked(isEditMode));
    }

    private void bindViews() {
        containerIngredientGroups = findViewById(R.id.containerIngredientGroups);
        textSelectedIngredient = findViewById(R.id.textSelectedIngredient);
        layoutQuantity = findViewById(R.id.layoutQuantity);
        editIngredientQuantity = findViewById(R.id.editIngredientQuantity);
        layoutWeightGrams = findViewById(R.id.layoutWeightGrams);
        editWeightGrams = findViewById(R.id.editWeightGrams);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        textExpiryDate = findViewById(R.id.textExpiryDate);
        buttonClearExpiry = findViewById(R.id.buttonClearExpiry);
    }

    private void setUpUnitSpinner() {
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, Constants.UNITS);
        unitAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerUnit.setAdapter(unitAdapter);
    }

    /**
     * Builds one expandable section per food type, each listing its
     * preset ingredient names. Passing null for existingName is a new
     * item, nothing is selected to start with. Passing an existing name
     * and category tries to find that exact name among the presets; if
     * it is not there (the item was added before this list existed, or
     * was free typed with a name that does not match a preset), that
     * name is kept by adding it as its own row under its saved category,
     * rather than quietly swapping it for a different name the next
     * time this item is saved.
     */
    private void setUpIngredientGroups(@Nullable String existingName, @Nullable PantryCategory existingCategory) {
        containerIngredientGroups.removeAllViews();
        itemRowViews.clear();
        groupItemsContainers.clear();
        groupArrowViews.clear();
        defaultUnitByName.clear();
        selectedItemView = null;

        boolean existingIsPreset = false;
        if (existingName != null) {
            for (IngredientNameCatalog.Entry[] entries : IngredientNameCatalog.GROUPS.values()) {
                for (IngredientNameCatalog.Entry entry : entries) {
                    if (entry.name.equals(existingName)) {
                        existingIsPreset = true;
                    }
                }
            }
        }

        PantryCategory legacyRowCategory = (existingName != null && !existingIsPreset)
                ? (existingCategory != null ? existingCategory : PantryCategory.OTHER)
                : null;

        PantryCategory categoryToExpand = null;

        for (Map.Entry<PantryCategory, IngredientNameCatalog.Entry[]> group : IngredientNameCatalog.GROUPS.entrySet()) {
            PantryCategory category = group.getKey();
            List<IngredientNameCatalog.Entry> entries = new ArrayList<>(Arrays.asList(group.getValue()));

            if (category == legacyRowCategory) {
                entries.add(0, new IngredientNameCatalog.Entry(existingName, null));
            }

            for (IngredientNameCatalog.Entry entry : entries) {
                if (entry.defaultUnit != null) {
                    defaultUnitByName.put(entry.name, entry.defaultUnit);
                }
                if (entry.name.equals(existingName)) {
                    categoryToExpand = category;
                }
            }

            addGroupSection(category, entries);
        }

        if (existingName != null) {
            selectIngredient(existingName, categoryToExpand != null ? categoryToExpand : PantryCategory.OTHER, false);
            setGroupExpanded(categoryToExpand, true);
        } else {
            textSelectedIngredient.setText(R.string.ingredient_not_selected_yet);
        }
    }

    /** Adds one tappable heading (a food type) plus its collapsed list of ingredient name rows. */
    private void addGroupSection(PantryCategory category, List<IngredientNameCatalog.Entry> entries) {
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
        label.setTextSize(15);
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

        for (IngredientNameCatalog.Entry entry : entries) {
            TextView itemRow = new TextView(this);
            itemRow.setText(entry.name);
            itemRow.setTextColor(ContextCompat.getColor(this, R.color.pantry_text_primary));
            itemRow.setTextSize(14);
            itemRow.setPadding(dp(8), dp(10), dp(8), dp(10));
            itemRow.setBackground(selectableBackground());
            itemRow.setClickable(true);
            itemRow.setFocusable(true);
            itemRow.setOnClickListener(v -> selectIngredient(entry.name, category, true));
            itemsContainer.addView(itemRow);
            itemRowViews.put(entry.name, itemRow);
        }

        headerRow.setOnClickListener(v -> toggleGroup(category));

        containerIngredientGroups.addView(headerRow);
        containerIngredientGroups.addView(itemsContainer);

        groupItemsContainers.put(category, itemsContainer);
        groupArrowViews.put(category, arrow);
    }

    private void toggleGroup(PantryCategory category) {
        boolean isExpanded = groupItemsContainers.get(category).getVisibility() == View.VISIBLE;
        setGroupExpanded(category, !isExpanded);
    }

    private void setGroupExpanded(@Nullable PantryCategory category, boolean expanded) {
        if (category == null) {
            return;
        }
        LinearLayout itemsContainer = groupItemsContainers.get(category);
        TextView arrow = groupArrowViews.get(category);
        if (itemsContainer == null || arrow == null) {
            return;
        }
        itemsContainer.setVisibility(expanded ? View.VISIBLE : View.GONE);
        arrow.setText(expanded ? R.string.accordion_expanded_arrow : R.string.accordion_collapsed_arrow);
    }

    /**
     * Records the chosen ingredient and category, updates the row
     * styling so the current pick is clear, and (only when a person
     * actually tapped a row, not while the screen is first loading an
     * existing item) jumps the unit spinner to that ingredient's
     * default unit.
     */
    private void selectIngredient(String name, PantryCategory category, boolean updateUnitAutomatically) {
        selectedIngredientName = name;
        selectedCategory = category;
        textSelectedIngredient.setText(getString(R.string.ingredient_selected_format, name));

        if (selectedItemView != null) {
            selectedItemView.setTypeface(null, Typeface.NORMAL);
        }
        TextView newRowView = itemRowViews.get(name);
        if (newRowView != null) {
            newRowView.setTypeface(null, Typeface.BOLD);
        }
        selectedItemView = newRowView;

        if (updateUnitAutomatically) {
            String defaultUnit = defaultUnitByName.get(name);
            if (defaultUnit != null) {
                selectSpinnerValue(spinnerUnit, defaultUnit);
            }
        }
    }

    private void setUpExpiryDatePicker() {
        textExpiryDate.setOnClickListener(v -> {
            Calendar initial = selectedExpiryDate != null
                    ? DateUtils.parseIso(selectedExpiryDate)
                    : Calendar.getInstance();

            new DatePickerDialog(this, (picker, year, month, dayOfMonth) -> {
                selectedExpiryDate = DateUtils.toIso(year, month, dayOfMonth);
                textExpiryDate.setText(DateUtils.toDisplay(selectedExpiryDate));
                buttonClearExpiry.setVisibility(View.VISIBLE);
            }, initial.get(Calendar.YEAR), initial.get(Calendar.MONTH), initial.get(Calendar.DAY_OF_MONTH))
                    .show();
        });

        buttonClearExpiry.setOnClickListener(v -> {
            selectedExpiryDate = null;
            textExpiryDate.setText(R.string.ingredient_expiry_not_set);
            buttonClearExpiry.setVisibility(View.GONE);
        });
    }

    private void populateFieldsForEdit() {
        PantryItem existing = pantryDao.getById(editingItemId);
        if (existing == null) {
            // this can happen if the item was deleted somewhere else before the edit button was tapped
            Toast.makeText(this, R.string.item_deleted, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dateAddedForSave = existing.getDateAdded();
        setUpIngredientGroups(existing.getName(), existing.getCategory());
        editIngredientQuantity.setText(QuantityUtils.format(existing.getQuantity()));
        selectSpinnerValue(spinnerUnit, existing.getUnit());

        if (existing.hasWeightInGrams()) {
            editWeightGrams.setText(QuantityUtils.format(existing.getWeightInGrams()));
        }

        if (existing.hasExpiryDate()) {
            selectedExpiryDate = existing.getExpiryDate();
            textExpiryDate.setText(DateUtils.toDisplay(selectedExpiryDate));
            buttonClearExpiry.setVisibility(View.VISIBLE);
        }
    }

    private void onSaveClicked(boolean isEditMode) {
        if (selectedIngredientName == null) {
            Toast.makeText(this, R.string.error_name_required, Toast.LENGTH_SHORT).show();
            return;
        }

        String quantityText = editIngredientQuantity.getText() != null
                ? editIngredientQuantity.getText().toString().trim() : "";

        layoutQuantity.setError(null);
        layoutWeightGrams.setError(null);

        double quantity;
        if (TextUtils.isEmpty(quantityText)) {
            layoutQuantity.setError(getString(R.string.error_quantity_required));
            return;
        }
        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            layoutQuantity.setError(getString(R.string.error_quantity_invalid));
            return;
        }
        if (quantity <= 0) {
            layoutQuantity.setError(getString(R.string.error_quantity_invalid));
            return;
        }

        Double weightInGrams = null;
        String weightText = editWeightGrams.getText() != null ? editWeightGrams.getText().toString().trim() : "";
        if (!TextUtils.isEmpty(weightText)) {
            try {
                weightInGrams = Double.parseDouble(weightText);
            } catch (NumberFormatException e) {
                layoutWeightGrams.setError(getString(R.string.error_weight_grams_invalid));
                return;
            }
            if (weightInGrams <= 0) {
                layoutWeightGrams.setError(getString(R.string.error_weight_grams_invalid));
                return;
            }
        }

        String unit = (String) spinnerUnit.getSelectedItem();

        PantryItem item = new PantryItem(
                isEditMode ? editingItemId : -1,
                selectedIngredientName, quantity, unit, selectedExpiryDate, dateAddedForSave, selectedCategory);
        item.setWeightInGrams(weightInGrams);

        if (isEditMode) {
            pantryDao.update(item);
        } else {
            pantryDao.insert(item);
        }

        Toast.makeText(this, R.string.ingredient_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    /** Selects the spinner entry matching this text, leaving its current selection if nothing matches. */
    private void selectSpinnerValue(Spinner spinner, String value) {
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                return;
            }
        }
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
