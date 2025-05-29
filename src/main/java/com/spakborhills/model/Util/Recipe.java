package com.spakborhills.model.Util; 

import com.spakborhills.model.Player; 

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

public class Recipe {

    private final String name;
    private final Map<String, Integer> ingredients;
    private final String resultItemName;
    private final String fuelRequired;
    private final String unlockConditionKey; 

    /**
     * Konstruktor untuk Recipe.
     *
     * @param name             Nama resep.
     * @param ingredients      Map bahan (Nama Item -> Kuantitas).
     * @param resultItemName   Nama item Food yang dihasilkan.
     * @param fuelRequired     Nama item bahan bakar (bisa null).
     * @param unlockConditionKey Kunci string untuk kondisi unlock (misal, "DEFAULT", "FISH_10").
     */
    public Recipe(String name, Map<String, Integer> ingredients, String resultItemName, String fuelRequired, String unlockConditionKey) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Nama resep tidak boleh kosong.");
        if (ingredients == null || ingredients.isEmpty()) throw new IllegalArgumentException("Resep harus memiliki bahan.");
        if (resultItemName == null || resultItemName.isBlank()) throw new IllegalArgumentException("Nama item hasil tidak boleh kosong.");
        if (unlockConditionKey == null || unlockConditionKey.isBlank()) throw new IllegalArgumentException("Kunci kondisi unlock tidak boleh kosong.");

        this.name = name;
        this.ingredients = Collections.unmodifiableMap(new HashMap<>(ingredients));
        this.resultItemName = resultItemName;
        this.fuelRequired = (fuelRequired == null || fuelRequired.isBlank()) ? null : fuelRequired;
        this.unlockConditionKey = unlockConditionKey.toUpperCase(); 
    }

    // Getters
    public String getName() { return name; }
    public Map<String, Integer> getIngredients() { return ingredients; }
    public String getResultItemName() { return resultItemName; }
    public String getFuelRequired() { return fuelRequired; }
    public String getUnlockConditionKey() { return unlockConditionKey; }

    /**
     * Memeriksa apakah resep ini sudah terbuka (unlocked) berdasarkan kondisi
     * dan statistik pemain saat ini.
     *
     * @param statistics Objek EndGameStatistics pemain.
     * @param player     Objek Player (opsional, untuk cek inventory jika ada kondisi "punya item X saat ini").
     * @return true jika resep sudah terbuka, false jika belum.
     */
     public boolean isUnlocked(EndGameStatistics statistics, Player player) { // Player ditambahkan
        if (statistics == null) {
            return this.unlockConditionKey.equals("DEFAULT") || this.unlockConditionKey.equals("BAWAAN");
        }

        switch (this.unlockConditionKey) {
            case "DEFAULT":
            case "BAWAAN":
                return true;

            case "BELI_DI_STORE":
                return statistics.hasAchieved("BOUGHT_RECIPE_" + this.getName().toUpperCase().replace(" ", "_"));

            case "FISH_10": // recipe_3 Sashimi
                return statistics.getTotalFishCaughtCount() >= 10;

            case "PUFFERFISH": // recipe_4 Fugu (Kunci sudah di-uppercase oleh konstruktor)
                return statistics.hasAchieved("FISH_PUFFERFISH");

            case "FIRST_HARVEST": // recipe_7 Veggie Soup
                return statistics.hasHarvestedAnyCrop();

            case "OBTAINED_HOT_PEPPER": // recipe_8 Fish Stew
                return statistics.hasAchieved("OBTAINED_HOT_PEPPER");

            case "FISH_LEGEND": // recipe_11 The Legends of Spakbor
                return statistics.hasAchieved("FISH_LEGEND");

            default:
                System.err.println("Peringatan: Kunci kondisi unlock tidak dikenal untuk resep '" + getName() + "': " + getUnlockConditionKey());
                return false; // Defaultnya tidak terbuka jika kunci tidak dikenal
        }
    }

    @Override
    public String toString() {
        return "Recipe: " + name + " (Hasil: " + resultItemName +
               ", Bahan Bakar: " + (fuelRequired != null ? fuelRequired : "Tidak ada") +
               ", Unlock: " + unlockConditionKey + ")";
    }
}
