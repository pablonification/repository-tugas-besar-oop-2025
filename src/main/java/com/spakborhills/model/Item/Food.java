package com.spakborhills.model.Item;

import com.spakborhills.model.Player;
import com.spakborhills.model.Enum.ItemCategory;

public class Food extends Item implements EdibleItem {
    private final int energyRestored;

    /**
     * Konstruktor untuk item Food.
     * Data diambil dari tabel di Halaman 20.
     *
     * @param name           Nama makanan (misalnya, "Fish n' Chips").
     * @param energyRestored Jumlah energi yang dipulihkan saat dikonsumsi (+nilai).
     * @param buyPrice       Harga beli makanan ini (0 atau '-' jika tidak bisa dibeli).
     * @param sellPrice      Harga jual makanan ini.
     */
    public Food(String name, int energyRestored, int buyPrice, int sellPrice) {
        super(name, ItemCategory.FOOD, buyPrice, sellPrice);
        this.energyRestored = energyRestored;
    }

    @Override
    public int getEnergyRestore() {
        return energyRestored;
    }

    @Override
    public boolean use(Player player, Object target) {
        // makan food
        player.changeEnergy(getEnergyRestore());
        System.out.println("Kamu memakan " + getName() + " dan merasa lebih berenergi!");
        // Controller harus menghapus 1 food dari inventory setelah ini return true
        return true;
    }

    @Override
    public Item cloneItem() {
        return new Food(this.getName(), this.energyRestored, this.getBuyPrice(), this.getSellPrice());
    }

}
