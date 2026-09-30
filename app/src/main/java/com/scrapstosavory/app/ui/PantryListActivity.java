package com.scrapstosavory.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.scrapstosavory.app.R;
import com.scrapstosavory.app.adapter.PantryAdapter;
import com.scrapstosavory.app.data.DatabaseHelper;
import com.scrapstosavory.app.data.PantryDao;
import com.scrapstosavory.app.model.PantryItem;

import java.util.List;

/**
 * Launcher screen: the "Read" half of pantry CRUD. Shows every ingredient
 * currently in the pantry via a RecyclerView bound to PantryDao, and hands
 * off to AddEditIngredientActivity (via Intent) for Create/Update, per the
 * assignment's Intents requirement (Section 3.1).
 *
 * The list is reloaded in onResume() rather than onCreate() so that coming
 * back from adding, editing, or deleting an ingredient always shows fresh
 * data — this is also what makes the "data survives close/reopen" proof in
 * the video demo (Section 5.1.2) work correctly.
 */
public class PantryListActivity extends AppCompatActivity implements PantryAdapter.OnPantryItemListener {

    private PantryDao pantryDao;
    private PantryAdapter adapter;
    private RecyclerView recyclerView;
    private TextView textEmptyPantry;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pantryDao = new PantryDao(new DatabaseHelper(this));

        recyclerView = findViewById(R.id.recyclerPantryItems);
        textEmptyPantry = findViewById(R.id.textEmptyPantry);
        adapter = new PantryAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAddItem = findViewById(R.id.fabAddItem);
        fabAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(PantryListActivity.this, AddEditIngredientActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPantryList();
    }

    private void refreshPantryList() {
        List<PantryItem> items = pantryDao.getAll();
        adapter.setItems(items);

        boolean isEmpty = items.isEmpty();
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        textEmptyPantry.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onEditClicked(PantryItem item) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClicked(PantryItem item) {
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
}
