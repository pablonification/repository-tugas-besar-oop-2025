// --- PriceList.java (Stub) ---
package com.spakborhills.model.Util;

import com.spakborhills.model.Item.Item;

public class PriceList {
    // Stub: Kembalikan harga default
    public int getSellPrice(Item item) { return item.getName().length(); } // Harga jual = panjang nama (contoh)
    public int getBuyPrice(Item item) { return item.getName().length() * 2; } // Harga beli = 2x panjang nama (contoh)
}