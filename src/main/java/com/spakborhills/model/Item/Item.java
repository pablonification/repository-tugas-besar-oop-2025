package com.spakborhills.model.Item;

import com.spakborhills.model.Enum.ItemCategory;
import com.spakborhills.model.Player;
import java.util.Objects;

public abstract class Item {
    private String name;
    private ItemCategory category;
    private int buyPrice;
    private int sellPrice;

    public Item(String name, ItemCategory category, int buyPrice, int sellPrice) {
        this.name = name;
        this.category = category;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }
    
    public String getName() {
        return name;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public int getBuyPrice() {
        return buyPrice;
    }

    public int getSellPrice() {
        return sellPrice;
    }

    public abstract boolean use(Player player, Object target);

    public abstract Item cloneItem();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(name, item.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
