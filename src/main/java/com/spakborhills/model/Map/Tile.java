package com.spakborhills.model.Map;

import com.spakborhills.model.Item.Seed;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Item.Crop; // Import Crop
import com.spakborhills.model.Enum.TileType;
import com.spakborhills.model.Enum.Weather;
import java.util.Map;
import java.util.List;
import java.util.ArrayList; // Import ArrayList
import java.util.Collections;

public class Tile {
    private TileType type;
    private boolean isWatered;
    private Seed plantedSeed;
    private int growthDays; // Tambahkan untuk isHarvestable yang lebih baik

    public Tile(TileType initialType) {
        this.type = initialType;
        this.isWatered = false;
        this.plantedSeed = null;
        this.growthDays = 0; // Inisialisasi
    }

    public TileType getType() { return type; }
    public void setType(TileType t) { this.type = t; }
    public Seed getPlantedSeed() { return plantedSeed; }
    public boolean isWatered() { return this.isWatered; }
    public int getGrowthDays() { return growthDays; } // Getter untuk growthDays

    // --- Metode Aksi (Diperbaiki) ---
    public void till() {
        if (type == TileType.TILLABLE) {
            this.type = TileType.TILLED;
            System.out.println("Tile dicangkul."); // Pesan lebih simpel
        } else {
            // Tidak perlu pesan error di sini, Player.till sudah cek canBeTilled
        }
    }

    public void water() {
        // Cek kondisi di sini atau biarkan Player.water yang cek canBeWatered()
        if (canBeWatered()) { // Cek kondisi valid
             this.isWatered = true; // <-- PERBAIKAN: Ubah state
             System.out.println("Tile disiram.");
        } else {
            // Tidak perlu pesan error jika Player sudah cek
        }
    }

    public boolean plant(Seed seed) {
        if (type == TileType.TILLED && this.plantedSeed == null) {
            this.plantedSeed = seed;
            this.type = TileType.PLANTED;
            this.growthDays = 0; // Reset growth days saat tanam
            this.isWatered = false; // Perlu disiram setelah tanam
            // System.out.println("Stub: Menanam " + seed.getName()); // Pesan sudah ada di Player.plant
            return true;
        }
        return false; // Gagal jika tidak tilled atau sudah ada tanaman
    }

    // Perlu itemRegistry untuk membuat objek Crop
    public List<Item> harvest(Map<String, Item> itemRegistry) {
        if (isHarvestable()) {
            System.out.println("Memanen tanaman...");
            String cropName = this.plantedSeed.getCropYieldName();
            int quantity = this.plantedSeed.getQuantityPerHarvest();
            Item cropBase = itemRegistry.get(cropName); // Ambil contoh Crop dari registry

            // Reset tile state
            Seed harvestedSeedInfo = this.plantedSeed; // Simpan info seed sebelum di-reset
            this.type = TileType.TILLED;
            this.plantedSeed = null;
            this.isWatered = false;
            this.growthDays = 0;

            if (cropBase instanceof Crop) {
                List<Item> harvestedItems = new ArrayList<>();
                for (int i = 0; i < quantity; i++) {
                    // Idealnya, buat instance baru atau kloning, tapi untuk stub bisa pakai yg sama
                    harvestedItems.add(cropBase);
                }
                return harvestedItems;
            } else {
                 System.err.println("PERINGATAN: Crop '" + cropName + "' tidak ditemukan di registry atau bukan tipe Crop.");
                 return Collections.emptyList(); // Kembalikan list kosong jika crop tidak ada
            }
        }
        return null; // Kembalikan null jika tidak bisa dipanen
    }

    public void recover() {
        if (type == TileType.TILLED || type == TileType.PLANTED) {
            this.type = TileType.TILLABLE;
            this.plantedSeed = null;
            this.isWatered = false;
            this.growthDays = 0;
            System.out.println("Tile dipulihkan.");
        }
    }

    // --- Metode Pengecekan (Diperbaiki) ---
    public boolean canBeTilled() { return this.type == TileType.TILLABLE; }
    public boolean canBeWatered() {
        // Bisa disiram jika Tilled atau Planted DAN belum disiram
        return (this.type == TileType.TILLED || this.type == TileType.PLANTED) && !this.isWatered;
    }
    public boolean isHarvestable() {
        // Logika panen sebenarnya: jika ada tanaman DAN growthDays >= daysToHarvest
        return this.type == TileType.PLANTED && this.plantedSeed != null && this.growthDays >= this.plantedSeed.getDaysToHarvest();
        // Untuk stub awal, bisa: return this.type == TileType.PLANTED && this.plantedSeed != null;
    }
    public boolean canBeRecovered() { return this.type == TileType.TILLED || this.type == TileType.PLANTED; }

    // Metode untuk update harian (stub dengan logika pertumbuhan minimal)
    public void updateDaily(Weather weather) {
        boolean needsWaterToday = !this.isWatered && weather == Weather.SUNNY;

        if (this.type == TileType.PLANTED && this.plantedSeed != null) {
            // Hanya tumbuh jika disiram atau hujan
            if (this.isWatered || weather == Weather.RAINY) {
                this.growthDays++;
                // System.out.println("Tanaman " + plantedSeed.getName() + " tumbuh hari ke-" + growthDays);
            } else if (needsWaterToday) {
                // Tanaman tidak tumbuh jika butuh air dan tidak disiram/hujan
                 System.out.println("Tanaman " + plantedSeed.getName() + " butuh air!");
            }
        }

        // Reset status siram untuk hari berikutnya (jika tidak hujan)
        if (weather != Weather.RAINY) {
            this.isWatered = false;
        } else if (this.type == TileType.TILLED || this.type == TileType.PLANTED) {
            // Otomatis tersiram jika hujan
            this.isWatered = true;
        }
    }
}
