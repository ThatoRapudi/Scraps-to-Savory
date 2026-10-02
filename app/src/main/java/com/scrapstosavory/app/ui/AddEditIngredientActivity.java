package com.scrapstosavory.app.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.scrapstosavory.app.R;
import com.scrapstosavory.app.adapter.GroupedSpinnerAdapter;
import com.scrapstosavory.app.data.DatabaseHelper;
import com.scrapstosavory.app.data.IngredientNameCatalog;
import com.scrapstosavory.app.data.PantryDao;
import com.scrapstosavory.app.model.PantryCategory;
import com.scrapstosavory.app.model.PantryItem;
import com.scrapstosavory.app.util.Constants;
import com.scrapstosavory.app.util.DateUtils;
import com.scrapstosavory.app.util.QuantityUtils;

import java.util.ArrayList;
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
 * The ingredient name is picked from a preset list instead of typed in,
 * grouped under headings like Vegetables or Dairy and eggs, so there is
 * nothing to spell correctly or misname. Picking a name also sets a
 * sensible starting unit for it (tomatoes default to pieces, rice
 * defaults to kilograms, and so on), instead of leaving the unit stuck
 * on grams for everything. The quantity is still checked before saving.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "com.scrapstosavory.app.EXTRA_ITEM_ID";
    private static final long NO_ITEM_ID = -1L;

    private PantryDao pantryDao;
    private long editingItemId = NO_ITEM_ID;
    private String dateAddedForSave;
    private String selectedExpiryDate; // null until the user picks one

    private TextInputLayout layoutQuantity;
    private TextInputEditText editIngredientQuantity;
    private Spinner spinnerIngredientName;
    private Spinner spinnerUnit;
    private Spinner spinnerCategory;
    private TextView textExpiryDate;
    private TextView buttonClearExpiry;

    // The default unit for each preset ingredient name, looked up when the
    // user picks a name so the unit field can jump to something sensible.
    private final Map<String, String> defaultUnitByName = new HashMap<>();

    // While the ingredient name spinner is still being set up (including
    // for an existing item being edited), picking its starting selection
    // should not also overwrite the unit the item was actually saved
    // with. This stays true until that initial setup is finished.
    private boolean suppressAutoUnitUpdate = true;

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
        setUpSpinners();
        setUpExpiryDatePicker();

        if (isEditMode) {
            populateFieldsForEdit();
        } else {
            dateAddedForSave = DateUtils.todayIso();
            setUpIngredientNameSpinner(null);
            suppressAutoUnitUpdate = false;
        }

        findViewById(R.id.buttonSaveIngredient).setOnClickListener(v -> onSaveClicked(isEditMode));
    }

    private void bindViews() {
        layoutQuantity = findViewById(R.id.layoutQuantity);
        editIngredientQuantity = findViewById(R.id.editIngredientQuantity);
        spinnerIngredientName = findViewById(R.id.spinnerIngredientName);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        textExpiryDate = findViewById(R.id.textExpiryDate);
        buttonClearExpiry = findViewById(R.id.buttonClearExpiry);
    }

    private void setUpSpinners() {
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, Constants.UNITS);
        spinnerUnit.setAdapter(unitAdapter);

        String[] categoryDisplayNames = new String[PantryCategory.values().length];
        PantryCategory[] categories = PantryCategory.values();
        for (int i = 0; i < categories.length; i++) {
            categoryDisplayNames[i] = categories[i].getDisplayName();
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, categoryDisplayNames);
        spinnerCategory.setAdapter(categoryAdapter);

        spinnerIngredientName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (suppressAutoUnitUpdate) {
                    return;
                }
                String name = (String) spinnerIngredientName.getSelectedItem();
                String defaultUnit = defaultUnitByName.get(name);
                if (defaultUnit != null) {
                    selectSpinnerValue(spinnerUnit, defaultUnit);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // nothing to do, the spinner always has something selected once it has an adapter
            }
        });
    }

    /**
     * Builds the grouped list of preset ingredient names (with a heading
     * above each group) and selects one to start with. Passing null
     * means this is a new item, so the first real name in the list is
     * selected. Passing an existing name tries to find that exact name
     * in the preset list; if it is not there (the item was added before
     * this list existed, or was free typed with a name that does not
     * match a preset), that name is kept by adding it as its own entry
     * at the very top, rather than quietly swapping it for a different
     * name the next time this item is saved.
     */
    private void setUpIngredientNameSpinner(@Nullable String existingName) {
        List<String> items = new ArrayList<>();
        List<Integer> headerPositions = new ArrayList<>();

        for (Map.Entry<String, IngredientNameCatalog.Entry[]> group : IngredientNameCatalog.GROUPS.entrySet()) {
            headerPositions.add(items.size());
            items.add(group.getKey());
            for (IngredientNameCatalog.Entry entry : group.getValue()) {
                items.add(entry.name);
                defaultUnitByName.put(entry.name, entry.defaultUnit);
            }
        }

        int selectedPosition = 1; // the first real name, right after the first heading

        if (!TextUtils.isEmpty(existingName)) {
            int matchPosition = items.indexOf(existingName);
            if (matchPosition != -1) {
                selectedPosition = matchPosition;
            } else {
                items.add(0, existingName);
                for (int i = 0; i < headerPositions.size(); i++) {
                    headerPositions.set(i, headerPositions.get(i) + 1);
                }
                selectedPosition = 0;
            }
        }

        spinnerIngredientName.setAdapter(new GroupedSpinnerAdapter(this, items, headerPositions));
        spinnerIngredientName.setSelection(selectedPosition);
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
        setUpIngredientNameSpinner(existing.getName());
        editIngredientQuantity.setText(QuantityUtils.format(existing.getQuantity()));
        selectSpinnerValue(spinnerUnit, existing.getUnit());
        selectSpinnerValue(spinnerCategory, existing.getCategory().getDisplayName());
        suppressAutoUnitUpdate = false;

        if (existing.hasExpiryDate()) {
            selectedExpiryDate = existing.getExpiryDate();
            textExpiryDate.setText(DateUtils.toDisplay(selectedExpiryDate));
            buttonClearExpiry.setVisibility(View.VISIBLE);
        }
    }

    private void onSaveClicked(boolean isEditMode) {
        String name = (String) spinnerIngredientName.getSelectedItem();
        String quantityText = editIngredientQuantity.getText() != null
                ? editIngredientQuantity.getText().toString().trim() : "";

        layoutQuantity.setError(null);

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

        String unit = (String) spinnerUnit.getSelectedItem();
        PantryCategory category = PantryCategory.values()[spinnerCategory.getSelectedItemPosition()];

        PantryItem item = new PantryItem(
                isEditMode ? editingItemId : -1,
                name, quantity, unit, selectedExpiryDate, dateAddedForSave, category);

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
}
