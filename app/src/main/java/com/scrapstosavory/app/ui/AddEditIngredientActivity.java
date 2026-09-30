package com.scrapstosavory.app.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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
import com.scrapstosavory.app.data.DatabaseHelper;
import com.scrapstosavory.app.data.PantryDao;
import com.scrapstosavory.app.model.PantryCategory;
import com.scrapstosavory.app.model.PantryItem;
import com.scrapstosavory.app.util.Constants;
import com.scrapstosavory.app.util.DateUtils;
import com.scrapstosavory.app.util.QuantityUtils;

import java.util.Calendar;

/**
 * This screen is used for both adding a new pantry item and editing an
 * existing one. It opens in "add" mode from the + button on the Pantry
 * List screen, or in "edit" mode when a row's edit button is tapped,
 * which passes the item's id in through EXTRA_ITEM_ID.
 *
 * The name and quantity are checked before anything is saved. Once
 * saving is done, the screen closes and the Pantry List screen reloads
 * itself with the latest data.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "com.scrapstosavory.app.EXTRA_ITEM_ID";
    private static final long NO_ITEM_ID = -1L;

    private PantryDao pantryDao;
    private long editingItemId = NO_ITEM_ID;
    private String dateAddedForSave;
    private String selectedExpiryDate; // null until the user picks one

    private TextInputLayout layoutName;
    private TextInputLayout layoutQuantity;
    private TextInputEditText editIngredientName;
    private TextInputEditText editIngredientQuantity;
    private Spinner spinnerUnit;
    private Spinner spinnerCategory;
    private TextView textExpiryDate;
    private TextView buttonClearExpiry;

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
        }

        findViewById(R.id.buttonSaveIngredient).setOnClickListener(v -> onSaveClicked(isEditMode));
    }

    private void bindViews() {
        layoutName = findViewById(R.id.layoutName);
        layoutQuantity = findViewById(R.id.layoutQuantity);
        editIngredientName = findViewById(R.id.editIngredientName);
        editIngredientQuantity = findViewById(R.id.editIngredientQuantity);
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
        editIngredientName.setText(existing.getName());
        editIngredientQuantity.setText(QuantityUtils.format(existing.getQuantity()));
        selectSpinnerValue(spinnerUnit, existing.getUnit());
        selectSpinnerValue(spinnerCategory, existing.getCategory().getDisplayName());

        if (existing.hasExpiryDate()) {
            selectedExpiryDate = existing.getExpiryDate();
            textExpiryDate.setText(DateUtils.toDisplay(selectedExpiryDate));
            buttonClearExpiry.setVisibility(View.VISIBLE);
        }
    }

    private void onSaveClicked(boolean isEditMode) {
        String name = editIngredientName.getText() != null
                ? editIngredientName.getText().toString().trim() : "";
        String quantityText = editIngredientQuantity.getText() != null
                ? editIngredientQuantity.getText().toString().trim() : "";

        layoutName.setError(null);
        layoutQuantity.setError(null);

        if (TextUtils.isEmpty(name)) {
            layoutName.setError(getString(R.string.error_name_required));
            return;
        }

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

    /** Selects the spinner entry matching this text, leaving position 0 selected if nothing matches. */
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
