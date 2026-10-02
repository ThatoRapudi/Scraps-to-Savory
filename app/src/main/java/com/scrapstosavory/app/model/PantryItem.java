package com.scrapstosavory.app.model;

import com.scrapstosavory.app.util.DateUtils;

/**
 * Represents a single ingredient the user currently has at home.
 * Mirrors one row of the pantry_items table.
 */
public class PantryItem {

    private long id;
    private String name;
    private double quantity;
    private String unit;
    private String expiryDate;  // stored as yyyy-MM-dd, can be left empty if there is no expiry date
    private String dateAdded;   // stored as yyyy-MM-dd, the day this item was added to the pantry
    private PantryCategory category = PantryCategory.OTHER; // used to guess a shelf life if no expiry date is given
    private Double weightInGrams; // optional extra detail, null when the user did not give one

    public PantryItem() {
    }

    public PantryItem(long id, String name, double quantity, String unit, String expiryDate,
                       String dateAdded, PantryCategory category) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
        this.dateAdded = dateAdded;
        this.category = category != null ? category : PantryCategory.OTHER;
    }

    // used when adding a brand new item, since it does not have an id yet (the database gives it one)
    public PantryItem(String name, double quantity, String unit, String expiryDate,
                       String dateAdded, PantryCategory category) {
        this(-1, name, quantity, unit, expiryDate, dateAdded, category);
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

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean hasExpiryDate() {
        return expiryDate != null && !expiryDate.trim().isEmpty();
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public PantryCategory getCategory() {
        return category;
    }

    public void setCategory(PantryCategory category) {
        this.category = category != null ? category : PantryCategory.OTHER;
    }

    /**
     * An optional extra weight for this item in grams, on top of the
     * quantity and unit. Quantity is meant to answer "how many" (2
     * onions, 1 bag of rice), this answers "how heavy" when the user
     * happens to know it and wants the extra detail. Null when they did
     * not give one, since it is never required.
     */
    public Double getWeightInGrams() {
        return weightInGrams;
    }

    public void setWeightInGrams(Double weightInGrams) {
        this.weightInGrams = weightInGrams;
    }

    public boolean hasWeightInGrams() {
        return weightInGrams != null;
    }

    /**
     * Same check as hasExpiryDate(), just named to make it clear this is a
     * date the user actually typed in themselves, not an estimated one.
     */
    public boolean hasManualExpiryDate() {
        return hasExpiryDate();
    }

    /**
     * The date this item should be treated as expiring on, for working
     * out "expiring soon" warnings. If the user typed in their own
     * expiry date, that is used as is. Otherwise this guesses one by
     * adding the category's default shelf life onto the day the item
     * was added to the pantry.
     */
    public String getEffectiveExpiryDate() {
        if (hasManualExpiryDate()) {
            return expiryDate;
        }
        return DateUtils.addDays(dateAdded, category.getDefaultShelfLifeDays());
    }

    @Override
    public String toString() {
        return "PantryItem{id=" + id + ", name='" + name + "', quantity=" + quantity
                + ", unit='" + unit + "', expiryDate='" + expiryDate + "', dateAdded='" + dateAdded
                + "', category=" + category + "}";
    }
}
