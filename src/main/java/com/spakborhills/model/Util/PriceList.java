package com.spakborhills.model.Util;

import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Item.Fish;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PriceList {

    private final Map<String, Integer> buyPrices;
    private final Map<String, Integer> sellPrices;

    public PriceList() {
        this.buyPrices = new HashMap<>();
        this.sellPrices = new HashMap<>();
        // Panggil initializeDefaultPrices() di sini jika Anda ingin harga default
        // selalu ada saat PriceList dibuat dan sebelum loadFromFile dipanggil.
        // Atau, panggil secara manual dari Main.java untuk testing.
        // initializeDefaultPrices(); // Contoh: aktifkan untuk testing
        System.out.println("PriceList diinisialisasi.");
    }

    /**
     * Menginisialisasi harga default secara hardcode berdasarkan spesifikasi.
     * Berguna untuk testing awal atau sebagai fallback.
     */
    public void initializeDefaultPrices() {
        // --- Seeds (Halaman 15) ---
        // Harga jual seed adalah setengah harga belinya.
        addPrice("Parsnip Seeds", 20, 10);
        addPrice("Cauliflower Seeds", 80, 40);
        addPrice("Potato Seeds", 50, 25);
        addPrice("Wheat Seeds", 60, 30);       
        addPrice("Blueberry Seeds", 80, 40);
        addPrice("Tomato Seeds", 50, 25);
        addPrice("Hot Pepper Seeds", 40, 20);
        addPrice("Melon Seeds", 80, 40);
        addPrice("Cranberry Seeds", 100, 50);
        addPrice("Pumpkin Seeds", 150, 75);
        addPrice("Grape Seeds", 60, 30);

        // --- Crops (Halaman 19) ---
        // Harga Beli (jika dari store), Harga Jual
        addPrice("Parsnip", 50, 35);
        addPrice("Cauliflower", 200, 150);
        addPrice("Potato", 0, 80);          // Tidak bisa dibeli (Harga Beli 0)
        addPrice("Wheat", 50, 30);
        addPrice("Blueberry", 150, 40);
        addPrice("Tomato", 90, 60);
        addPrice("Hot Pepper", 0, 40);      // Tidak bisa dibeli
        addPrice("Melon", 0, 250);          // Tidak bisa dibeli
        addPrice("Cranberry", 0, 25);       // Tidak bisa dibeli
        addPrice("Pumpkin", 300, 250);
        addPrice("Grape", 100, 10);

        // --- Food (Halaman 20) ---
        // Nama, Harga Beli, Harga Jual
        addPrice("Fish n' Chips", 150, 135);
        addPrice("Baguette", 100, 80);
        addPrice("Sashimi", 300, 275);
        addPrice("Fugu", 0, 135);             // Tidak bisa dibeli ('-')
        addPrice("Wine", 100, 90);
        addPrice("Pumpkin Pie", 120, 100);
        addPrice("Veggie Soup", 140, 120);
        addPrice("Fish Stew", 280, 260);
        addPrice("Spakbor Salad", 0, 250);    // Tidak bisa dibeli ('-')
        addPrice("Fish Sandwich", 200, 180);
        addPrice("The Legends of Spakbor", 0, 2000); // Tidak bisa dibeli ('-')
        addPrice("Cooked Pig's Head", 1000, 0); // Harga jual 0g

        // --- Misc Items (Halaman 20) ---
        // Harga jual harus lebih murah dari harga beli. Ditentuin sendiri
        addPrice("Coal", 20, 10);
        addPrice("Firewood", 15, 5); // Contoh harga

        // --- Equipment (Halaman 20, 23) ---
        // tidak dibeli/dijual, harga 0.
        addPrice("Hoe", 0, 0);
        addPrice("Watering Can", 0, 0);
        addPrice("Pickaxe", 0, 0);
        addPrice("Fishing Rod", 0, 0);

        // --- Special Items ---
        addPrice("Proposal Ring", 0, 0); // Tidak dijual/dibeli secara normal

        // --- Fish (Halaman 18) ---
        // Harga jual ikan dihitung dinamis oleh Fish.getSellPrice().
        // PriceList bisa saja tidak menyimpan harga jual ikan secara eksplisit.
        // Jika Anda ingin PriceList menjadi SATU-SATUNYA sumber harga jual,
        // Anda perlu menghitung harga jual setiap ikan dan menambahkannya di sini.
        // Contoh:
        // Fish bullhead = new Fish("Bullhead", Fish.FishRarity.COMMON, Set.of(Season.ANY), List.of(new Fish.TimeRange(0,23)), Set.of(Weather.ANY), Set.of(LocationType.MOUNTAIN_LAKE));
        // addPrice("Bullhead", 0, bullhead.getSellPrice()); // Harga beli 0
        // Ini akan membuat PriceList bergantung pada pembuatan objek Fish.
        // Untuk sekarang, kita biarkan harga jual ikan ditangani di luar PriceList jika dinamis.
        // Jika ada ikan yang BISA DIBELI (tidak ada di spek), tambahkan harga belinya di sini.

        System.out.println("Harga default telah dimuat ke PriceList untuk " + (buyPrices.size() + sellPrices.size()) / 2 + " item unik (kurang lebih).");
    }

    public void addPrice(String itemName, int buyPrice, int sellPrice) {
        if (itemName == null || itemName.isBlank()) {
            System.err.println("Kesalahan: Nama item tidak boleh kosong saat menambahkan harga.");
            return;
        }
        // Simpan nama item dalam lowercase untuk pencarian case-insensitive
        String lowerItemName = itemName.toLowerCase();

        if (buyPrice >= 0) {
            this.buyPrices.put(lowerItemName, buyPrice);
        } else {
            // System.out.println("Info: Harga beli untuk '" + itemName + "' tidak valid (<0), tidak ditambahkan.");
        }
        if (sellPrice >= 0) {
            this.sellPrices.put(lowerItemName, sellPrice);
        } else {
            // System.out.println("Info: Harga jual untuk '" + itemName + "' tidak valid (<0), tidak ditambahkan.");
        }
    }

    public int getBuyPrice(String itemName) {
        if (itemName == null || itemName.isBlank()) return -1; // Indikasi tidak valid/tidak ditemukan
        return this.buyPrices.getOrDefault(itemName.toLowerCase(), -1);
    }

    public int getBuyPrice(Item item) {
        if (item == null) return -1;
        return getBuyPrice(item.getName());
    }

    public int getSellPrice(String itemName) {
        if (itemName == null || itemName.isBlank()) return -1;
        return this.sellPrices.getOrDefault(itemName.toLowerCase(), -1);
    }

    public int getSellPrice(Item item) {
        if (item == null) return -1;

        // OPSI: Jika ingin harga jual ikan selalu dari objek Fish itu sendiri
        if (item instanceof Fish) {
             return ((Fish) item).getSellPrice(); // Panggil metode di Fish
        }
        // Jika tidak, atau jika harga ikan juga disimpan di PriceList, ambil dari map
        return getSellPrice(item.getName());
    }

    public void loadFromFile(String filePath) {
        this.buyPrices.clear();
        this.sellPrices.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Anda mungkin punya header, jika ya: br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) continue;

                String[] values = line.split(","); // Asumsi pemisah koma
                if (values.length == 3) {
                    try {
                        String itemName = values[0].trim();
                        int buyPrice = Integer.parseInt(values[1].trim());
                        int sellPrice = Integer.parseInt(values[2].trim());
                        addPrice(itemName, buyPrice, sellPrice);
                    } catch (NumberFormatException e) {
                        System.err.println("Kesalahan format angka di baris file harga: \"" + line + "\" -> " + e.getMessage());
                    }
                } else {
                    System.err.println("Format baris tidak sesuai di file harga: \"" + line + "\" (harap gunakan: NamaItem,HargaBeli,HargaJual)");
                }
            }
            System.out.println("Daftar harga berhasil dimuat dari: " + filePath);
        } catch (IOException e) {
            System.err.println("Gagal memuat daftar harga dari file: " + filePath + " -> " + e.getMessage());
            System.out.println("Menggunakan harga default jika ada (panggil initializeDefaultPrices() jika perlu).");
        }
    }
}
