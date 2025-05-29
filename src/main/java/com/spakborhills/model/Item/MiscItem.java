/*
 *   class MiscItem extends Item {
    + use(player: Player, target: Object): boolean
  }
 */
package com.spakborhills.model.Item;

import com.spakborhills.model.Player;
import com.spakborhills.model.Enum.ItemCategory;

public class MiscItem extends Item {
    public MiscItem(String name, int buyPrice, int sellPrice) {
        super(name, ItemCategory.MISC, buyPrice, sellPrice);
        
        if (buyPrice > 0 && sellPrice >= buyPrice) {
            throw new IllegalArgumentException(
                "Untuk item Misc '" + name + "', harga jual (" + sellPrice +
                ") harus lebih rendah dari harga beli (" + buyPrice + ")."
            );
        }
    }

    @Override
    public boolean use(Player player, Object target) {
        System.out.println(getName() + " tidak dapat digunakan secara langsung.");
        return false;
    }

    @Override
    public Item cloneItem() {
        return new MiscItem(this.getName(), this.getBuyPrice(), this.getSellPrice());
    }
}