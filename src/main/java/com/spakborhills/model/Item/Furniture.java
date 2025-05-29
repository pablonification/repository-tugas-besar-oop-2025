package com.spakborhills.model.Item;

import com.spakborhills.model.Player;
import com.spakborhills.model.Enum.ItemCategory;

public class Furniture extends Item {
    private int x;
    private int y;
    private String description;

    // Constructor
    public Furniture(String name, String description, int buyPrice, int sellPrice, int x, int y) {
        super(name, ItemCategory.FURNITURE, buyPrice, sellPrice);
        this.description = description;
        this.x = x;
        this.y = y;
    }

    // Getters and Setters
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String getDescription() {
        return description;
    }

    // Implemented abstract use method (placeholder logic)
    @Override
    public boolean use(Player player, Object target) {
        System.out.println("Using furniture: " + getName());
        return true;
    }

    public Item cloneItem() {
        Furniture cloned = new Furniture(this.getName(), this.getDescription(), this.getBuyPrice(), this.getSellPrice(), this.x, this.y);
        return cloned;
    }
} 