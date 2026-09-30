package com.scrapstosavory.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.scrapstosavory.app.R;
import com.scrapstosavory.app.model.DietTag;
import com.scrapstosavory.app.model.MealType;
import com.scrapstosavory.app.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Fills the RecyclerView on the Suggested Recipes screen. Each row shows
 * the recipe name, how many ingredients it needs, and its diet and meal
 * tags, then opens the recipe detail screen when tapped.
 */
public class SuggestedRecipeAdapter extends RecyclerView.Adapter<SuggestedRecipeAdapter.RecipeViewHolder> {

    /** Used to tell the hosting screen which recipe was tapped. */
    public interface OnRecipeClickListener {
        void onRecipeClicked(Recipe recipe);
    }

    private final List<Recipe> recipes = new ArrayList<>();
    private final OnRecipeClickListener listener;

    public SuggestedRecipeAdapter(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    public void setRecipes(List<Recipe> newRecipes) {
        recipes.clear();
        recipes.addAll(newRecipes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_suggested_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);

        holder.textRecipeName.setText(recipe.getName());
        holder.textIngredientCount.setText(holder.textIngredientCount.getContext()
                .getString(R.string.ingredient_count_format, recipe.getIngredients().size()));
        holder.textRecipeTags.setText(buildTagsText(recipe));
        holder.itemView.setOnClickListener(v -> listener.onRecipeClicked(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    /** Builds a short line such as "Vegan, Dinner" out of a recipe's diet and meal tags. */
    private String buildTagsText(Recipe recipe) {
        List<String> labels = new ArrayList<>();
        for (DietTag tag : recipe.getDietTags()) {
            labels.add(tag.getDisplayName());
        }
        for (MealType type : recipe.getMealTypes()) {
            labels.add(type.getDisplayName());
        }

        StringBuilder text = new StringBuilder();
        for (int i = 0; i < labels.size(); i++) {
            if (i > 0) {
                text.append(" • ");
            }
            text.append(labels.get(i));
        }
        return text.toString();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        final TextView textRecipeName;
        final TextView textIngredientCount;
        final TextView textRecipeTags;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            textRecipeName = itemView.findViewById(R.id.textRecipeName);
            textIngredientCount = itemView.findViewById(R.id.textIngredientCount);
            textRecipeTags = itemView.findViewById(R.id.textRecipeTags);
        }
    }
}
