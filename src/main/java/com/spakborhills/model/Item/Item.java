/*
 *   abstract class Item {
    # name: String
    # category: ItemCategory
    # buyPrice: int
    # sellPrice: int
    + getName(): String
    + getCategory(): ItemCategory
    + getBuyPrice(): int
    + getSellPrice(): int
    + {abstract} use(player: Player, target: Object): boolean
  }
 */

package com.spakborhills.model.Item;

import com.spakborhills.model.Enum.ItemCategory;
import com.spakborhills.model.Player;

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

}
