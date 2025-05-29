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
        System.out.println("PriceList diinisialisasi.");
    }

    /**
     * Menginisialisasi harga default secara hardcode berdasarkan spesifikasi.
     * Berguna untuk testing awal atau sebagai fallback.
     */
    public void initializeDefaultPrices() {
        // Seeds (Halaman 15)
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

        // Crops (Halaman 19)
        // Harga Beli (jika dari store), Harga Jual
        addPrice("Parsnip", 50, 35);
        addPrice("Cauliflower", 200, 150);
        addPrice("Potato", 0, 80);          
        addPrice("Wheat", 50, 30);
        addPrice("Blueberry", 150, 40);
        addPrice("Tomato", 90, 60);
        addPrice("Hot Pepper", 0, 40);      
        addPrice("Melon", 0, 250);          
        addPrice("Cranberry", 0, 25);       
        addPrice("Pumpkin", 300, 250);
        addPrice("Grape", 100, 10);

        // Food (Halaman 20)
        // Nama, Harga Beli, Harga Jual
        addPrice("Fish n' Chips", 150, 135);
        addPrice("Baguette", 100, 80);
        addPrice("Sashimi", 300, 275);
        addPrice("Fugu", 0, 135);              
        addPrice("Wine", 100, 90);
        addPrice("Pumpkin Pie", 120, 100);
        addPrice("Veggie Soup", 140, 120);
        addPrice("Fish Stew", 280, 260);
        addPrice("Spakbor Salad", 0, 250);     
        addPrice("Fish Sandwich", 200, 180);
        addPrice("The Legends of Spakbor", 0, 2000);  
        addPrice("Cooked Pig's Head", 1000, 0);

        // Misc Items (Halaman 20)
        // Harga jual harus lebih murah dari harga beli. Ditentuin sendiri
        addPrice("Coal", 20, 10);
        addPrice("Firewood", 15, 5); 
        addPrice("Stone", 5, 2);

        // Equipment (Halaman 20, 23)
        // tidak dibeli/dijual, harga 0.
        addPrice("Hoe", 0, 0);
        addPrice("Watering Can", 0, 0);
        addPrice("Pickaxe", 0, 0);
        addPrice("Fishing Rod", 0, 0);

        // Special Items
        addPrice("Proposal Ring", 0, 0); 
        addPrice("Koran Edisi Baru", 0, 0); 

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
            
        }
        if (sellPrice >= 0) {
            this.sellPrices.put(lowerItemName, sellPrice);
        } else {
            
        }
    }

    public int getBuyPrice(String itemName) {
        if (itemName == null || itemName.isBlank()) return -1; 
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

        if (item instanceof Fish) {
             return ((Fish) item).getSellPrice(); 
        }
        return getSellPrice(item.getName());
    }

    public void loadFromFile(String filePath) {
        this.buyPrices.clear();
        this.sellPrices.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) continue;

                String[] values = line.split(","); 
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
