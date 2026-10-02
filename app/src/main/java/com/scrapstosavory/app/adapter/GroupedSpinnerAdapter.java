package com.scrapstosavory.app.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.scrapstosavory.app.R;

import java.util.List;

/**
 * A Spinner adapter that can show group headings (such as "Vegetables")
 * mixed in with the actual selectable names. Headings are bold, shown
 * in a muted colour, and cannot be tapped, they are only there to break
 * up a long list of ingredient names so it is easier to scan.
 */
public class GroupedSpinnerAdapter extends ArrayAdapter<String> {

    private final List<Integer> headerPositions;

    public GroupedSpinnerAdapter(Context context, List<String> items, List<Integer> headerPositions) {
        super(context, android.R.layout.simple_spinner_dropdown_item, items);
        this.headerPositions = headerPositions;
    }

    /** Headers are not real choices, so the Spinner should skip over them when tapped. */
    @Override
    public boolean isEnabled(int position) {
        return !headerPositions.contains(position);
    }

    @NonNull
    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView textView = (TextView) view;

        boolean isHeader = headerPositions.contains(position);
        textView.setTypeface(null, isHeader ? Typeface.BOLD : Typeface.NORMAL);
        textView.setTextColor(ContextCompat.getColor(getContext(),
                isHeader ? R.color.pantry_text_secondary : R.color.pantry_text_primary));

        return view;
    }
}
