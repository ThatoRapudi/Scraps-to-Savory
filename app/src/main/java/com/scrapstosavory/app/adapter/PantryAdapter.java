package com.scrapstosavory.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.scrapstosavory.app.R;
import com.scrapstosavory.app.model.PantryItem;
import com.scrapstosavory.app.util.DateUtils;
import com.scrapstosavory.app.util.QuantityUtils;
import com.scrapstosavory.app.util.SettingsManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Fills the RecyclerView on the Pantry List screen with pantry items.
 * Each row shows the ingredient's name, quantity/unit and category,
 * along with edit and delete buttons.
 *
 * The expiry line always shows something, either the date the user
 * typed in themselves, or one estimated from the category's default
 * shelf life. If the Settings screen has expiring soon alerts turned
 * on, that line is coloured as a warning once the item is within the
 * saved number of days of its expiry.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /** Used to tell PantryListActivity when the user taps edit or delete on a row. */
    public interface OnPantryItemListener {
        void onEditClicked(PantryItem item);
        void onDeleteClicked(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final OnPantryItemListener listener;

    public PantryAdapter(OnPantryItemListener listener) {
        this.listener = listener;
    }

    /** Replaces the whole list of items shown — called every time the pantry is read from the database. */
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
        if (item.hasWeight()) {
            holder.textItemQuantity.setText(String.format("%s %s (%s %s) • %s",
                    QuantityUtils.format(item.getQuantity()), item.getUnit(),
                    QuantityUtils.format(item.getWeightValue()), item.getWeightUnit(), item.getCategory().getDisplayName()));
        } else {
            holder.textItemQuantity.setText(String.format("%s %s • %s",
                    QuantityUtils.format(item.getQuantity()), item.getUnit(), item.getCategory().getDisplayName()));
        }

        Context context = holder.textItemExpiry.getContext();
        String effectiveExpiryDate = item.getEffectiveExpiryDate();
        int labelRes = item.hasManualExpiryDate() ? R.string.expires_on_format : R.string.expires_estimated_format;

        holder.textItemExpiry.setVisibility(View.VISIBLE);
        holder.textItemExpiry.setText(context.getString(labelRes, DateUtils.toDisplay(effectiveExpiryDate)));

        boolean alertsEnabled = SettingsManager.isExpiringSoonAlertsEnabled(context);
        boolean isExpiringSoon = alertsEnabled
                && DateUtils.daysUntil(effectiveExpiryDate) <= SettingsManager.getExpiringSoonDays(context);
        int colorRes = isExpiringSoon ? R.color.pantry_error : R.color.pantry_text_secondary;
        holder.textItemExpiry.setTextColor(ContextCompat.getColor(context, colorRes));

        holder.buttonEdit.setOnClickListener(v -> listener.onEditClicked(item));
        holder.buttonDelete.setOnClickListener(v -> listener.onDeleteClicked(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
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
