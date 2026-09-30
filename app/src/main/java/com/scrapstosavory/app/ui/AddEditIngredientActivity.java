package com.scrapstosavory.app.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.scrapstosavory.app.R;
import com.scrapstosavory.app.data.DatabaseHelper;
import com.scrapstosavory.app.data.PantryDao;
import com.scrapstosavory.app.model.PantryItem;

/**
 * Create/Update screen for a single pantry ingredient.
 *
 * PLACEHOLDER SCAFFOLD (Step 3 of the build): this currently only shows the
 * ingredient's name and demonstrates the Intent contract (EXTRA_ITEM_ID) that
 * PantryListActivity already uses to reach this screen in both Add and Edit
 * mode. Quantity/unit/category/expiry inputs, validation, and the real
 * save-to-database logic are added in the very next step — deliberately left
 * for its own commit rather than folded in here.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "com.scrapstosavory.app.EXTRA_ITEM_ID";
    private static final long NO_ITEM_ID = -1L;

    private PantryDao pantryDao;
    private long editingItemId = NO_ITEM_ID;
    private TextInputEditText editIngredientName;

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

        editIngredientName = findViewById(R.id.editIngredientName);

        if (isEditMode) {
            PantryItem existing = pantryDao.getById(editingItemId);
            if (existing != null) {
                editIngredientName.setText(existing.getName());
            }
        }

        findViewById(R.id.buttonSaveIngredient).setOnClickListener(v -> {
            // TODO (next step): validate input, build a PantryItem with
            // quantity/unit/category/expiry, and call pantryDao.insert()
            // or pantryDao.update() depending on isEditMode.
            Toast.makeText(this, R.string.add_edit_placeholder_notice, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
