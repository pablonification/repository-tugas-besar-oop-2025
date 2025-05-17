package com.spakborhills.model.Util; // Atau package yang sesuai

// Import yang mungkin diperlukan
// import com.spakborhills.model.Farm; // Tidak lagi dibutuhkan di isUnlocked jika stats cukup
import com.spakborhills.model.Player; // Mungkin dibutuhkan untuk cek inventory jika ada kondisi "punya item X saat ini"

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

public class Recipe {

    private final String name;
    private final Map<String, Integer> ingredients;
    private final String resultItemName;
    private final String fuelRequired;
    private final String unlockConditionKey; // Menggunakan kunci yang lebih standar

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
        this.unlockConditionKey = unlockConditionKey.toUpperCase(); // Simpan dalam uppercase untuk konsistensi
    }

    // --- Getters ---
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
    public boolean isUnlocked(EndGameStatistics statistics, Player player) { // Player ditambahkan untuk kondisi tertentu
        if (statistics == null) {
            // Jika tidak ada statistik, mungkin hanya resep default yang bisa terbuka
            return this.unlockConditionKey.equals("DEFAULT") || this.unlockConditionKey.equals("BAWAAN");
        }

        switch (this.unlockConditionKey) {
            case "DEFAULT":
            case "BAWAAN":
                return true;

            // Kondisi dari spesifikasi Halaman 31-32:
            case "BELI_DI_STORE":
                // Pelacakan resep yang dibeli sebaiknya ada di Player atau daftar resep yang diketahui.
                // Untuk sekarang, kita bisa asumsikan jika resep ini ada di daftar resep game,
                // dan unlock condition-nya "BELI_DI_STORE", maka pemain harus membelinya.
                // Atau, EndGameStatistics bisa punya Set<String> purchasedRecipeNames.
                // Untuk implementasi sederhana:
                return statistics.hasAchieved("BOUGHT_RECIPE_" + this.name.toUpperCase().replace(" ", "_"));
                // Anda perlu memanggil statistics.recordKeyEventOrItem("BOUGHT_RECIPE_NAMA_RESEP") saat pemain membeli.

            case "FISH_10": // recipe_3 Sashimi
                return statistics.getTotalFishCaughtCount() >= 10;

            case "FISH_PUFFERFISH": // recipe_4 Fugu
                // Menggunakan event key yang dicatat oleh EndGameStatistics.recordFishCatch
                return statistics.hasAchieved("FISH_PUFFERFISH");

            case "FIRST_HARVEST": // recipe_7 Veggie Soup
                return statistics.hasHarvestedAnyCrop();

            case "OBTAINED_HOT_PEPPER": // recipe_8 Fish Stew (Dapatkan "Hot Pepper" terlebih dahulu)
                // Menggunakan event key yang dicatat oleh EndGameStatistics.recordHarvest
                // atau EndGameStatistics.recordKeyEventOrItem("OBTAINED_HOT_PEPPER") jika didapat dari cara lain.
                return statistics.hasAchieved("OBTAINED_HOT_PEPPER");
                // Alternatif jika ingin cek inventory saat ini (membutuhkan parameter Player):
                // if (player != null && player.getInventory().hasItemByName("Hot Pepper")) { // Perlu hasItemByName di Inventory
                //     return true;
                // }
                // return false;

            case "FISH_LEGEND": // recipe_11 The Legends of Spakbor
                return statistics.hasAchieved("FISH_LEGEND");

            // Tambahkan case lain jika ada kondisi unlock baru
            // case "HARVEST_PARSNIP": // Contoh jika ada resep yang butuh panen parsnip
            //     return statistics.hasAchieved("HARVEST_PARSNIP");

            default:
                // Jika kondisi unlock tidak dikenali, anggap belum terbuka
                // atau bisa juga mengandalkan statistics.hasAchieved(this.unlockConditionKey)
                // jika semua kondisi adalah event yang dicatat.
                System.err.println("Peringatan: Kunci kondisi unlock tidak dikenal untuk resep '" + name + "': " + unlockConditionKey);
                return false;
        }
    }

    @Override
    public String toString() {
        return "Recipe: " + name + " (Hasil: " + resultItemName +
               ", Bahan Bakar: " + (fuelRequired != null ? fuelRequired : "Tidak ada") +
               ", Unlock: " + unlockConditionKey + ")";
    }
}
