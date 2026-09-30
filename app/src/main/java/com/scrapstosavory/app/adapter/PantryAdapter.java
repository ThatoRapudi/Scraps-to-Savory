package com.scrapstosavory.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.scrapstosavory.app.R;
import com.scrapstosavory.app.model.PantryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Binds the pantry_items table to the RecyclerView on the Pantry List screen.
 * Each row shows the ingredient's name, quantity/unit + category, and its
 * expiry date if one was set, plus edit/delete actions.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /** Lets the hosting Activity react to row-level actions without the adapter knowing about Intents or dialogs. */
    public interface OnPantryItemListener {
        void onEditClicked(PantryItem item);
        void onDeleteClicked(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final OnPantryItemListener listener;

    public PantryAdapter(OnPantryItemListener listener) {
        this.listener = listener;
    }

    /** Replaces the whole data set — called after every DB read (initial load, add, edit, delete). */
    public void setItems(List<PantryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);

        holder.textItemName.setText(item.getName());
        holder.textItemQuantity.setText(String.format("%s %s • %s",
                formatQuantity(item.getQuantity()), item.getUnit(), item.getCategory().getDisplayName()));

        if (item.hasExpiryDate()) {
            holder.textItemExpiry.setVisibility(View.VISIBLE);
            holder.textItemExpiry.setText(holder.textItemExpiry.getContext()
                    .getString(R.string.expires_on_format, item.getExpiryDate()));
        } else {
            holder.textItemExpiry.setVisibility(View.GONE);
        }

        holder.buttonEdit.setOnClickListener(v -> listener.onEditClicked(item));
        holder.buttonDelete.setOnClickListener(v -> listener.onDeleteClicked(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Strips a trailing ".0" so whole numbers ("3 pcs") don't show as "3.0 pcs". */
    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity) && !Double.isInfinite(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {
        final TextView textItemName;
        final TextView textItemQuantity;
        final TextView textItemExpiry;
        final ImageButton buttonEdit;
        final ImageButton buttonDelete;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            textItemName = itemView.findViewById(R.id.textItemName);
            textItemQuantity = itemView.findViewById(R.id.textItemQuantity);
            textItemExpiry = itemView.findViewById(R.id.textItemExpiry);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
