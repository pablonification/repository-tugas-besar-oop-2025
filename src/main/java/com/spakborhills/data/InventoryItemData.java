package com.spakborhills.data;

import java.io.Serializable;

public class InventoryItemData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String itemId;
    private int quantity;
   
    public InventoryItemData() {
    }

    public InventoryItemData(String itemId, int quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
} 