package com.scrapstosavory.app.model;

/**
 * One line item in a recipe's ingredient list — e.g. "2 eggs" or "200 g flour".
 * Mirrors one row of the recipe_ingredients table (foreign key: recipeId).
 */
public class RecipeIngredient {

    private long id;
    private long recipeId;
    private String name;
    private double quantity;
    private String unit;

    public RecipeIngredient() {
    }

    public RecipeIngredient(long id, long recipeId, String name, double quantity, String unit) {
        this.id = id;
        this.recipeId = recipeId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    // Convenience constructor used by the recipe seed data (no id/recipeId yet)
    public RecipeIngredient(String name, double quantity, String unit) {
        this(-1, -1, name, quantity, unit);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(long recipeId) {
        this.recipeId = recipeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return quantity + " " + unit + " " + name;
    }
}
