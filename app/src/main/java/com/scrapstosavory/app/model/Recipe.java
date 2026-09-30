package com.scrapstosavory.app.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * A recipe: a name, its required ingredients, preparation steps, and the
 * dietary tags it carries (a recipe can be more than one — e.g. VEGAN and
 * HIGH_PROTEIN at the same time). The ingredient list is populated
 * separately (via RecipeDao) after the base recipe row is read, since
 * ingredients live in their own table.
 */
public class Recipe {

    private long id;
    private String name;
    private String steps; // simple multi-line preparation method
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    private Set<DietTag> dietTags = EnumSet.noneOf(DietTag.class);
    private Set<MealType> mealTypes = EnumSet.noneOf(MealType.class);

    public Recipe() {
    }

    public Recipe(long id, String name, String steps) {
        this.id = id;
        this.name = name;
        this.steps = steps;
    }

    public Recipe(long id, String name, String steps, Set<DietTag> dietTags) {
        this(id, name, steps);
        if (dietTags != null) {
            this.dietTags = dietTags;
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void addIngredient(RecipeIngredient ingredient) {
        this.ingredients.add(ingredient);
    }

    public Set<DietTag> getDietTags() {
        return dietTags;
    }

    public void setDietTags(Set<DietTag> dietTags) {
        this.dietTags = dietTags != null ? dietTags : EnumSet.noneOf(DietTag.class);
    }

    public void addDietTag(DietTag tag) {
        if (tag != null) {
            this.dietTags.add(tag);
        }
    }

    public boolean hasTag(DietTag tag) {
        return dietTags.contains(tag);
    }

    public Set<MealType> getMealTypes() {
        return mealTypes;
    }

    public void setMealTypes(Set<MealType> mealTypes) {
        this.mealTypes = mealTypes != null ? mealTypes : EnumSet.noneOf(MealType.class);
    }

    public void addMealType(MealType mealType) {
        if (mealType != null) {
            this.mealTypes.add(mealType);
        }
    }

    public boolean hasMealType(MealType mealType) {
        return mealTypes.contains(mealType);
    }

    @Override
    public String toString() {
        return "Recipe{id=" + id + ", name='" + name + "', ingredients=" + ingredients.size()
                + ", tags=" + dietTags + "}";
    }
}
