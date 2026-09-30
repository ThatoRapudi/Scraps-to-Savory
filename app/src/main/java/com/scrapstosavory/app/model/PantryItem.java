package com.scrapstosavory.app.model;

/**
 * Represents a single ingredient the user currently has at home.
 * Mirrors one row of the pantry_items table.
 */
public class PantryItem {

    private long id;
    private String name;
    private double quantity;
    private String unit;
    private String expiryDate;  // ISO format "yyyy-MM-dd", nullable — user can type an exact date
    private String dateAdded;   // ISO format "yyyy-MM-dd" — when the item was added to the pantry
    private PantryCategory category = PantryCategory.OTHER; // drives the auto-estimated shelf life

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

    // Convenience constructor for inserting a new item (no id yet — SQLite assigns it)
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
     * The date used for "expiring soon" checks: the user's own expiry date if
     * they typed one, otherwise dateAdded + the category's default shelf life.
     * Actual date arithmetic lives in ExpiryUtils (added with the Settings
     * screen) so this class stays a plain data holder.
     */
    public boolean hasManualExpiryDate() {
        return hasExpiryDate();
    }

    @Override
    public String toString() {
        return "PantryItem{id=" + id + ", name='" + name + "', quantity=" + quantity
                + ", unit='" + unit + "', expiryDate='" + expiryDate + "', dateAdded='" + dateAdded
                + "', category=" + category + "}";
    }
}
