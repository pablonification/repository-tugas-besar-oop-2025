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

        // validasi aturan hal 20, harga jual < harga beli
        // hanya berlaku jika item memang bisa dibeli (buyPrice > 0)
        if (buyPrice > 0 && sellPrice >= buyPrice) {
            throw new IllegalArgumentException(
                "Untuk item Misc '" + name + "', harga jual (" + sellPrice +
                ") harus lebih rendah dari harga beli (" + buyPrice + ")."
            );
        }
    }

    @Override
    public boolean use(Player player, Object target) {
        // Item misc seperti Coal tidak digunakan secara aktif oleh pemain
        // Melainkan dikonsumsi oleh proses lain seperti cooking
        System.out.println(getName() + " tidak dapat digunakan secara langsung.");
        return false;
    }
}