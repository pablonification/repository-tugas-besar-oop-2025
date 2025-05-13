package com.spakborhills.model.Util; // Atau package yang sesuai

// import com.spakborhills.model.Enum.ItemCategory; // Mungkin diperlukan untuk validasi bahan
// import com.spakborhills.model.Item.Item; 
import com.spakborhills.model.Enum.FishRarity;// Untuk validasi bahan
import com.spakborhills.model.Farm;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap; // Untuk membuat salinan map ingredients

/**
 * Merepresentasikan sebuah resep untuk membuat item Food.
 * Menyimpan nama resep, bahan yang dibutuhkan, item hasil, bahan bakar, dan kondisi unlock.
 * Berdasarkan spesifikasi Halaman 30-32.
 */
public class Recipe {

    private final String name; 
    private final Map<String, Integer> ingredients; 
    private final String resultItemName; 
    private final String fuelRequired; 
    private final String unlockCondition; 

    /**
     * Konstruktor untuk Recipe.
     *
     * @param name             Nama resep (misalnya, "Fish n' Chips Recipe").
     * @param ingredients      Map berisi nama bahan dan kuantitasnya.
     * @param resultItemName   Nama item Food yang dihasilkan.
     * @param fuelRequired     Nama item bahan bakar yang dibutuhkan (bisa null atau string kosong jika tidak ada).
     * @param unlockCondition  String yang mendeskripsikan bagaimana resep ini di-unlock.
     */
    public Recipe(String name, Map<String, Integer> ingredients, String resultItemName, String fuelRequired, String unlockCondition) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nama resep tidak boleh kosong.");
        }
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("Resep harus memiliki setidaknya satu bahan.");
        }
        if (resultItemName == null || resultItemName.isBlank()) {
            throw new IllegalArgumentException("Nama item hasil resep tidak boleh kosong.");
        }
        if (unlockCondition == null || unlockCondition.isBlank()) {
            throw new IllegalArgumentException("Kondisi unlock resep tidak boleh kosong.");
        }

        this.name = name;
        this.ingredients = Collections.unmodifiableMap(new HashMap<>(ingredients)); // read only
        this.resultItemName = resultItemName;
        this.fuelRequired = (fuelRequired == null || fuelRequired.isBlank()) ? null : fuelRequired;
        this.unlockCondition = unlockCondition;
    }

    public String getName() {
        return name;
    }

    /**
     * Mendapatkan daftar bahan yang dibutuhkan dan kuantitasnya.
     * @return Map yang unmodifiable berisi nama bahan dan kuantitas.
     */
    public Map<String, Integer> getIngredients() {
        return ingredients;
    }

    public String getResultItemName() {
        return resultItemName;
    }

    /**
     * Mendapatkan nama item bahan bakar yang dibutuhkan.
     * @return Nama item bahan bakar, atau null jika tidak ada bahan bakar yang dibutuhkan.
     */
    public String getFuelRequired() {
        return fuelRequired;
    }

    public String getUnlockConditionString() { 
        return unlockCondition;
    }

    /**
     * Memeriksa apakah resep ini sudah terbuka (unlocked) berdasarkan kondisi
     * dan statistik pemain saat ini.
     * Implementasi detail dari pengecekan ini akan bergantung pada bagaimana
     * EndGameStatistics dan event game lainnya dilacak.
     *
     * @param statistics Objek EndGameStatistics pemain.
     * @param farm       Objek Farm untuk akses data game lain jika perlu (misal, item yang pernah dipanen).
     * @return true jika resep sudah terbuka, false jika belum.
     */
    public boolean isUnlocked(EndGameStatistics statistics, Farm farm) { 
        if (this.unlockCondition.equalsIgnoreCase("DEFAULT") ||
            this.unlockCondition.equalsIgnoreCase("BAWAAN")) {
            return true; // Resep default selalu terbuka
        }

        // Contoh implementasi untuk kondisi unlock spesifik:
        // Ini perlu disesuaikan dengan bagaimana Anda melacak event di EndGameStatistics atau Farm.

        // "Beli di store" (Halaman 31, recipe_1, recipe_10)
        // Pelacakan ini mungkin lebih cocok di Player (misal, List<String> knownRecipes)
        // yang diupdate saat pemain membeli resep.
        // Untuk sekarang, kita asumsikan jika unlockCondition-nya "BELI_DI_STORE",
        // maka perlu mekanisme lain untuk menandai resep ini diketahui.
        // Atau, jika resep yang dibeli langsung ditambahkan ke daftar resep pemain.
        if (this.unlockCondition.equalsIgnoreCase("BELI_DI_STORE")) {
            // Asumsi: jika ada di daftar resep Farm, berarti sudah dibeli/diketahui.
            // Ini mungkin perlu logika yang lebih baik.
            // Untuk sekarang, jika sudah ada di list resep, anggap unlocked.
            return true;
        }

        // "Setelah memancing 10 ikan" (Halaman 31, recipe_3 Sashimi)
        if (this.unlockCondition.equalsIgnoreCase("FISH_10")) {
            if (statistics == null) return false; // Butuh statistik
            int totalFishCaught = 0;
            for (Map<FishRarity, Integer> rarityMap : statistics.getFishCaught().values()) {
                for (int count : rarityMap.values()) {
                    totalFishCaught += count;
                }
            }
            return totalFishCaught >= 10;
        }

        // "Memancing pufferfish" (Halaman 31, recipe_4 Fugu)
        if (this.unlockCondition.equalsIgnoreCase("FISH_PUFFERFISH")) {
            if (statistics == null) return false;
            return statistics.getFishCaught().containsKey("Pufferfish"); // Asumsi nama ikan "Pufferfish"
        }

        // "Memanen untuk pertama kalinya" (Halaman 31, recipe_7 Veggie Soup)
        // Ini bisa berarti memanen CROP APAPUN untuk pertama kali.
        if (this.unlockCondition.equalsIgnoreCase("FIRST_HARVEST")) {
            if (statistics == null) return false;
            return !statistics.getCropsHarvested().isEmpty(); // Jika map cropsHarvested tidak kosong
        }

        // "Dapatkan "Hot Pepper" terlebih dahulu" (Halaman 31-32, recipe_8 Fish Stew)
        // Ini bisa berarti pemain pernah memiliki Hot Pepper di inventory.
        // Pelacakan ini sulit tanpa histori inventory.
        // Alternatif: resep terbuka jika pemain *saat ini* punya Hot Pepper.
        // Atau, jika "Hot Pepper" adalah nama crop, mungkin saat pertama kali panen Hot Pepper.
        // Untuk sekarang, kita asumsikan ini adalah kondisi yang lebih kompleks.
        if (this.unlockCondition.equalsIgnoreCase("HAVE_HOT_PEPPER")) {
            // Logika ini perlu Player.getInventory().hasItemByName("Hot Pepper")
            // atau pelacakan di statistik "pernah punya item X".
            // Untuk stub:
            // if (farm != null && farm.getPlayer().getInventory().hasItemByName("Hot Pepper")) return true;
            System.out.println("Peringatan: Kondisi unlock 'HAVE_HOT_PEPPER' belum diimplementasikan sepenuhnya di Recipe.isUnlocked().");
            return false; // Placeholder
        }

        // "Memancing "Legend"" (Halaman 32, recipe_11 The Legends of Spakbor)
        if (this.unlockCondition.equalsIgnoreCase("FISH_LEGEND")) {
            if (statistics == null) return false;
            return statistics.getFishCaught().containsKey("Legend"); 
        }

        // Jika kondisi tidak dikenali, anggap belum terbuka
        // System.err.println("Kondisi unlock tidak dikenal untuk resep '" + name + "': " + unlockCondition);
        return false;
    }

    @Override
    public String toString() {
        return "Recipe: " + name + " (Hasil: " + resultItemName +
               ", Bahan Bakar: " + (fuelRequired != null ? fuelRequired : "Tidak ada") +
               ", Unlock: " + unlockCondition + ")";
    }
}
