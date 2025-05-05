// STUB
package com.spakborhills.model.Util;

import com.spakborhills.model.Item.Item;
import java.util.HashMap;
import java.util.Map;
// import com.spakborhills.model.Util.EndGameStatistics;
// import com.spakborhills.model.Util.PriceList;


public class ShippingBin {
    private Map<Item, Integer> itemsToSell = new HashMap<>();
    private static final int MAX_UNIQUE_SLOTS = 16; // Sesuai spek Halaman 24

    // Stub: Logika sebenarnya perlu cek MAX_UNIQUE_SLOTS
    public boolean addItem(Item item, int quantity) {
        if (itemsToSell.size() >= MAX_UNIQUE_SLOTS && !itemsToSell.containsKey(item)) {
             System.out.println("Stub: Shipping Bin penuh untuk item unik baru.");
             return false;
        }
        itemsToSell.put(item, itemsToSell.getOrDefault(item, 0) + quantity);
        System.out.println("Stub: Menambahkan " + quantity + " " + item.getName() + " ke Shipping Bin.");
        return true;
    }

    // Stub: Logika sebenarnya akan hitung harga pakai PriceList & update stats
    public int processSales(EndGameStatistics stats, PriceList priceList) {
        System.out.println("Stub: Memproses penjualan Shipping Bin.");
        int totalIncome = 0;
        // Iterasi itemsToSell, panggil priceList.getSellPrice(), tambahkan ke totalIncome
        // Panggil stats.recordIncome(...)
        return totalIncome; // Stub: Kembalikan 0
    }

    public void clearBin() {
        itemsToSell.clear();
        System.out.println("Stub: Shipping Bin dikosongkan.");
    }
}