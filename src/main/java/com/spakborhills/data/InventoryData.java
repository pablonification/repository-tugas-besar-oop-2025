package com.spakborhills.data;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class InventoryData implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<InventoryItemData> items;
    // Mungkin ada properti lain seperti ukuran maksimal inventaris jika perlu disimpan
    // private int maxSize;

    public InventoryData() {
        this.items = new ArrayList<>();
    }

    public List<InventoryItemData> getItems() {
        return items;
    }

    public void setItems(List<InventoryItemData> items) {
        this.items = items;
    }

    // Metode bantuan jika diperlukan
    public void addItem(InventoryItemData item) {
        this.items.add(item);
    }
} 