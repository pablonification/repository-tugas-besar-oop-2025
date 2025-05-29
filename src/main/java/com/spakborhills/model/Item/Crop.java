package com.spakborhills.model.Item;

import com.spakborhills.model.Player;
import com.spakborhills.model.Enum.ItemCategory;

public class Crop extends Item implements EdibleItem {
    private static final int ENERGY_RESTORED_AMOUNT = 3;
    /**
     * Konstruktor untuk item Crop.
     * Harga didasarkan pada tabel di Halaman 19.
     *
     * @param name      Nama crop (misalnya, "Parsnip").
     * @param buyPrice  Harga beli crop ini (jika tersedia di toko).
     * @param sellPrice Harga jual crop ini.
     */
    public Crop(String name, int buyPrice, int sellPrice) {
        super(name, ItemCategory.CROP, buyPrice, sellPrice);
    }

    /**
     * Mendapatkan jumlah energi yang dipulihkan saat crop ini dimakan.
     * @return Jumlah energi yang dipulihkan (selalu 3 untuk Crop).
     */
    @Override
    public int getEnergyRestore() {
        return ENERGY_RESTORED_AMOUNT;
    }

    /**
     * Mengimplementasikan aksi 'use' untuk Crop, yaitu memakannya.
     * Diasumsikan Controller sudah memverifikasi pemain memiliki crop ini.
     * Controller bertanggung jawab menghapus crop dari inventory jika penggunaan berhasil.
     *
     * @param player Pemain yang menggunakan (memakan) crop.
     * @param target Target aksi (tidak relevan untuk memakan crop).
     * @return true jika crop berhasil dimakan.
     */
    @Override
    public boolean use(Player player, Object target) {
        // makan crop
        player.changeEnergy(getEnergyRestore());
        System.out.println("Kamu memakan " + getName() + " dan mendapatkan sedikit energi.");
        // Controller harus menghapus 1 crop dari inventory setelah ini return true
        return true;
    }

    @Override
    public Item cloneItem() {
        return new Crop(this.getName(), this.getBuyPrice(), this.getSellPrice());
    }
}