package com.spakborhills.model.Util; 

import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Enum.Season; 

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Mengelola logika untuk Shipping Bin, tempat pemain menjual item.
 * Penjualan diproses di akhir hari.
 * Berdasarkan spesifikasi Halaman 23-24.
 */
public class ShippingBin {

    private final Map<Item, Integer> itemsToSell;
    private static final int MAX_UNIQUE_SLOTS = 16; // hal 23

    // private int lastSellDay; // Replaced by hasSoldToday for immediate effect
    private boolean hasSoldToday;

    /**
     * Konstruktor untuk ShippingBin.
     * Menginisialisasi bin kosong dan hasSoldToday (misal ke false agar penjualan pertama valid).
     */
    public ShippingBin() {
        this.itemsToSell = new HashMap<>();
        // this.lastSellDay = 0; 
        this.hasSoldToday = false; // Initialize to false, can sell on day 1
    }

    /**
     * Memeriksa apakah pemain dapat melakukan penjualan pada hari ini.
     * Pemain hanya bisa melakukan penjualan sekali sehari.
     *
     * @return true jika pemain bisa menjual hari ini, false jika sudah menjual.
     */
    public boolean canSellToday() { // currentDay parameter no longer needed here for this logic
        return !this.hasSoldToday;
    }

    /**
     * Menandai bahwa penjualan telah terjadi untuk hari ini.
     * Dipanggil setelah item berhasil ditambahkan ke bin.
     */
    private void markSaleOccurredForToday() {
        this.hasSoldToday = true;
    }

    /**
     * Menambahkan item ke dalam bin untuk dijual.
     * Memeriksa batasan jumlah slot item unik.
     *
     * @param item     Objek Item yang akan ditambahkan.
     * @param quantity Jumlah item yang akan ditambahkan (harus > 0).
     * @return true jika item berhasil ditambahkan, false jika gagal (misal, bin penuh untuk item unik baru).
     */
    public boolean addItem(Item item, int quantity) {
        if (item == null || quantity <= 0) {
            System.err.println("ERROR: Tidak bisa menambahkan item null atau kuantitas tidak valid ke Shipping Bin.");
            return false;
        }

        // Logic for canSellToday should be checked by the caller (Player/Controller) before calling addItem
        // However, if we want an absolute guarantee here, we could re-check, but it might be redundant.
        // For now, assume caller checks canSellToday().

        if (!itemsToSell.containsKey(item) && itemsToSell.size() >= MAX_UNIQUE_SLOTS) {
            System.out.println("Shipping Bin sudah penuh untuk jenis item baru (maks " + MAX_UNIQUE_SLOTS + " jenis item unik).");
            return false;
        }

        itemsToSell.put(item, itemsToSell.getOrDefault(item, 0) + quantity);
        markSaleOccurredForToday(); // Mark that a sale has happened today
        return true;
    }

    /**
     * Memproses semua item di Shipping Bin pada akhir hari (saat Player tidur).
     * Menghitung total pendapatan, mengupdate statistik, dan mengosongkan bin.
     *
     * @param statistics Objek EndGameStatistics untuk mencatat pendapatan.
     * @param priceList  Objek PriceList untuk mendapatkan harga jual item.
     * @param currentDay Hari saat ini, untuk menandai hari penjualan.
     * @param currentSeason Musim saat ini, untuk statistik pendapatan musiman.
     * @return Total pendapatan dari penjualan.
     */
    public int processSales(EndGameStatistics statistics, PriceList priceList, int currentDay, Season currentSeason) {
        if (itemsToSell.isEmpty()) {
            this.hasSoldToday = false; // Reset for next day even if nothing was sold
            return 0;
        }

        int totalIncomeToday = 0;
        System.out.println("Memproses penjualan dari Shipping Bin...");

        for (Map.Entry<Item, Integer> entry : itemsToSell.entrySet()) {
            Item currentItem = entry.getKey(); // Renamed to avoid conflict
            int currentQuantity = entry.getValue(); // Renamed to avoid conflict
            int sellPricePerUnit = priceList.getSellPrice(currentItem); 

            if (sellPricePerUnit < 0) { 
                System.err.println("ERROR: Item '" + currentItem.getName() + "' tidak memiliki harga jual valid atau tidak bisa dijual.");
                continue;
            }

            int itemIncome = sellPricePerUnit * currentQuantity;
            totalIncomeToday += itemIncome;

            System.out.println("  - Menjual " + currentQuantity + " " + currentItem.getName() + " seharga " + itemIncome + "g (" + sellPricePerUnit + "g/unit)");
        }

        if (totalIncomeToday > 0 && statistics != null) {
            statistics.recordIncome(totalIncomeToday, currentSeason); 
        }

        // this.lastSellDay = currentDay; // Not strictly needed if hasSoldToday is primary
        this.hasSoldToday = false; // Reset for the next day
        // clearBin(); // Pengosongan bin dilakukan setelah pemanggilan ini di Farm.nextDay()
        return totalIncomeToday;
    }

    /**
     * Mengosongkan semua item dari Shipping Bin.
     * Dipanggil setelah penjualan diproses di akhir hari.
     */
    public void clearBin() {
        this.itemsToSell.clear();
    }

    /**
     * Mendapatkan representasi Map dari item yang saat ini ada di Shipping Bin.
     * Mengembalikan view yang tidak bisa dimodifikasi.
     *
     * @return Map<Item, Integer> yang unmodifiable berisi item dan kuantitasnya.
     */
    public Map<Item, Integer> getItems() {
        return Collections.unmodifiableMap(this.itemsToSell);
    }

    // Metode toString untuk debugging (opsional)
    @Override
    public String toString() {
        if (itemsToSell.isEmpty()) {
            return "Shipping Bin Kosong.";
        }
        StringBuilder sb = new StringBuilder("Isi Shipping Bin (Untuk Dijual):\n");
        for (Map.Entry<Item, Integer> entry : itemsToSell.entrySet()) {
            sb.append("- ").append(entry.getKey().getName())
              .append(" x ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
