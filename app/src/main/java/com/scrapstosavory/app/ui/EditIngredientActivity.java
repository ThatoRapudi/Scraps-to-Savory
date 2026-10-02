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
import com.scrapstosavory.app.model.PantryItem;
import com.scrapstosavory.app.util.Constants;
import com.scrapstosavory.app.util.DateUtils;
import com.scrapstosavory.app.util.QuantityUtils;

import java.util.Calendar;

/**
 * Lets the user fine-tune the details of a pantry item that already
 * exists. Which ingredient it is and its category are fixed by this
 * point (they were set when it was added through the Add Ingredients
 * screen), so this screen only covers quantity, unit, an optional exact
 * weight or volume, and the expiry date.
 *
 * Always reached through the pencil edit icon on a Pantry List row,
 * with the item's id passed in through EXTRA_ITEM_ID.
 */
public class EditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "com.scrapstosavory.app.EXTRA_ITEM_ID";
    private static final long NO_ITEM_ID = -1L;

    private PantryDao pantryDao;
    private PantryItem existingItem;
    private String selectedExpiryDate; // null until the user picks one

    private TextView textIngredientName;
    private TextView textIngredientCategory;
    private TextInputLayout layoutQuantity;
    private TextInputEditText editIngredientQuantity;
    private Spinner spinnerUnit;
    private TextInputLayout layoutWeight;
    private TextInputEditText editWeight;
    private Spinner spinnerWeightUnit;
    private TextView textExpiryDate;
    private TextView buttonClearExpiry;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_ingredient);

        pantryDao = new PantryDao(new DatabaseHelper(this));

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.edit_ingredient_title);
        setSupportActionBar(toolbar);

        bindViews();
        setUpUnitSpinners();
        setUpExpiryDatePicker();

        long itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, NO_ITEM_ID);
        existingItem = itemId != NO_ITEM_ID ? pantryDao.getById(itemId) : null;

        if (existingItem == null) {
            // this can happen if the item was deleted somewhere else before the edit button was tapped
            Toast.makeText(this, R.string.item_deleted, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        populateFields();

        findViewById(R.id.buttonSaveIngredient).setOnClickListener(v -> onSaveClicked());
    }

    private void bindViews() {
        textIngredientName = findViewById(R.id.textIngredientName);
        textIngredientCategory = findViewById(R.id.textIngredientCategory);
        layoutQuantity = findViewById(R.id.layoutQuantity);
        editIngredientQuantity = findViewById(R.id.editIngredientQuantity);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        layoutWeight = findViewById(R.id.layoutWeight);
        editWeight = findViewById(R.id.editWeight);
        spinnerWeightUnit = findViewById(R.id.spinnerWeightUnit);
        textExpiryDate = findViewById(R.id.textExpiryDate);
        buttonClearExpiry = findViewById(R.id.buttonClearExpiry);
    }

    private void setUpUnitSpinners() {
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, Constants.UNITS);
        unitAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerUnit.setAdapter(unitAdapter);

        ArrayAdapter<String> weightUnitAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, Constants.UNITS);
        weightUnitAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerWeightUnit.setAdapter(weightUnitAdapter);
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

    private void populateFields() {
        textIngredientName.setText(existingItem.getName());
        textIngredientCategory.setText(existingItem.getCategory().getDisplayName());

        editIngredientQuantity.setText(QuantityUtils.format(existingItem.getQuantity()));
        selectSpinnerValue(spinnerUnit, existingItem.getUnit());

        if (existingItem.hasWeight()) {
            editWeight.setText(QuantityUtils.format(existingItem.getWeightValue()));
            selectSpinnerValue(spinnerWeightUnit, existingItem.getWeightUnit());
        }

        if (existingItem.hasExpiryDate()) {
            selectedExpiryDate = existingItem.getExpiryDate();
            textExpiryDate.setText(DateUtils.toDisplay(selectedExpiryDate));
            buttonClearExpiry.setVisibility(View.VISIBLE);
        }
    }

    private void onSaveClicked() {
        String quantityText = editIngredientQuantity.getText() != null
                ? editIngredientQuantity.getText().toString().trim() : "";

        layoutQuantity.setError(null);
        layoutWeight.setError(null);

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

        Double weightValue = null;
        String weightUnit = null;
        String weightText = editWeight.getText() != null ? editWeight.getText().toString().trim() : "";
        if (!TextUtils.isEmpty(weightText)) {
            try {
                weightValue = Double.parseDouble(weightText);
            } catch (NumberFormatException e) {
                layoutWeight.setError(getString(R.string.error_weight_invalid));
                return;
            }
            if (weightValue <= 0) {
                layoutWeight.setError(getString(R.string.error_weight_invalid));
                return;
            }
            weightUnit = (String) spinnerWeightUnit.getSelectedItem();
        }

        existingItem.setQuantity(quantity);
        existingItem.setUnit((String) spinnerUnit.getSelectedItem());
        existingItem.setWeightValue(weightValue);
        existingItem.setWeightUnit(weightUnit);
        existingItem.setExpiryDate(selectedExpiryDate);

        pantryDao.update(existingItem);

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
