package com.spakborhills.model.Util; // Atau package yang sesuai

import com.spakborhills.model.Enum.Season;
import com.spakborhills.model.Enum.RelationshipStatus;
import com.spakborhills.model.Enum.FishRarity; 
// import com.spakborhills.model.Farm; 
import com.spakborhills.model.NPC.NPC; 
import com.spakborhills.model.Player; 

import java.util.HashMap;
import java.util.HashSet; // Untuk item pernah dipanen/dimiliki
import java.util.List;
import java.util.Map;
import java.util.Set; // Untuk item pernah dipanen/dimiliki
import java.util.Collections;

/**
 * Melacak dan menghitung berbagai statistik permainan untuk ditampilkan
 * saat salah satu milestone End Game tercapai.
 * Berdasarkan spesifikasi Halaman 34.
 */
public class EndGameStatistics {

    private int totalIncome;
    private int totalExpenditure;
    private final Map<Season, Integer> seasonalIncome;
    private final Map<Season, Integer> seasonalExpenditure;
    private final Map<Season, Integer> daysPlayedInSeason; // BARU: Melacak hari per musim

    private int totalDaysPlayed;

    private final Map<String, RelationshipStatus> npcFriendshipStatus;
    private final Map<String, Integer> npcHeartPoints;
    private final Map<String, Integer> chatFrequency;
    private final Map<String, Integer> giftFrequency;
    private final Map<String, Integer> visitFrequency; // Diaktifkan

    private final Map<String, Integer> cropsHarvestedCount; // Nama Crop -> Jumlah
    private final Set<String> uniqueCropsHarvested;      // BARU: Nama Crop unik yang pernah dipanen
    private final Map<String, Map<FishRarity, Integer>> fishCaught;
    private final Set<String> uniqueFishCaught;          // BARU: Nama Ikan unik yang pernah ditangkap

    // BARU: Untuk melacak item kunci yang pernah dimiliki/event untuk unlock resep
    private final Set<String> keyEventsOrItemsObtained;

    public EndGameStatistics(List<NPC> initialNpcs, Player initialPlayer) {
        this.totalIncome = 0;
        this.totalExpenditure = 0;
        this.seasonalIncome = new HashMap<>();
        this.seasonalExpenditure = new HashMap<>();
        this.daysPlayedInSeason = new HashMap<>(); // BARU
        this.totalDaysPlayed = 0;

        this.npcFriendshipStatus = new HashMap<>();
        this.npcHeartPoints = new HashMap<>();
        this.chatFrequency = new HashMap<>();
        this.giftFrequency = new HashMap<>();
        this.visitFrequency = new HashMap<>(); // Diaktifkan

        this.cropsHarvestedCount = new HashMap<>();
        this.uniqueCropsHarvested = new HashSet<>(); // BARU
        this.fishCaught = new HashMap<>();
        this.uniqueFishCaught = new HashSet<>(); // BARU
        this.keyEventsOrItemsObtained = new HashSet<>(); // BARU

        for (Season s : Season.values()) {
            if (s != Season.ANY) {
                this.seasonalIncome.put(s, 0);
                this.seasonalExpenditure.put(s, 0);
                this.daysPlayedInSeason.put(s, 0); // BARU
            }
        }

        if (initialNpcs != null) {
            for (NPC npc : initialNpcs) {
                this.npcFriendshipStatus.put(npc.getName(), npc.getRelationshipStatus());
                this.npcHeartPoints.put(npc.getName(), npc.getHeartPoints());
                this.chatFrequency.put(npc.getName(), 0);
                this.giftFrequency.put(npc.getName(), 0);
                this.visitFrequency.put(npc.getName(), 0); // Inisialisasi
            }
        }
        // Pengeluaran awal bisa dicatat jika Player punya metode untuk itu
        // if (initialPlayer != null && initialPlayer.getInitialExpenditure() > 0) {
        //     recordExpenditure(initialPlayer.getInitialExpenditure(), Season.SPRING); // Asumsi musim awal
        // }
    }

    // --- Metode Perekaman Data Inkremental ---

    public void recordIncome(int amount, Season season) {
        if (amount > 0 && season != null && season != Season.ANY) {
            this.totalIncome += amount;
            this.seasonalIncome.put(season, this.seasonalIncome.getOrDefault(season, 0) + amount);
        }
    }

    public void recordExpenditure(int amount, Season season) {
        if (amount > 0 && season != null && season != Season.ANY) {
            this.totalExpenditure += amount;
            this.seasonalExpenditure.put(season, this.seasonalExpenditure.getOrDefault(season, 0) + amount);
        }
    }

    /**
     * Dipanggil setiap hari baru dimulai.
     * @param currentSeason Musim saat ini.
     */
    public void incrementDay(Season currentSeason) {
        this.totalDaysPlayed++;
        if (currentSeason != null && currentSeason != Season.ANY) {
            this.daysPlayedInSeason.put(currentSeason, this.daysPlayedInSeason.getOrDefault(currentSeason, 0) + 1);
        }
    }

    public void recordChat(String npcName) {
        if (npcName != null && !npcName.isBlank()) {
            this.chatFrequency.put(npcName, this.chatFrequency.getOrDefault(npcName, 0) + 1);
        }
    }

    public void recordGift(String npcName) {
        if (npcName != null && !npcName.isBlank()) {
            this.giftFrequency.put(npcName, this.giftFrequency.getOrDefault(npcName, 0) + 1);
        }
    }

    public void recordVisit(String npcName) {
        if (npcName != null && !npcName.isBlank()) {
            this.visitFrequency.put(npcName, this.visitFrequency.getOrDefault(npcName, 0) + 1);
        }
    }

    public void recordHarvest(String cropName, int quantity) {
        if (cropName != null && !cropName.isBlank() && quantity > 0) {
            this.cropsHarvestedCount.put(cropName, this.cropsHarvestedCount.getOrDefault(cropName, 0) + quantity);
            this.uniqueCropsHarvested.add(cropName); // Catat jenis crop unik yang dipanen
            if (cropName.equalsIgnoreCase("Parsnip")) { // Contoh untuk unlock resep
                this.keyEventsOrItemsObtained.add("HARVEST_PARSNIP");
            }
            if (cropName.equalsIgnoreCase("Hot Pepper")) {
                this.keyEventsOrItemsObtained.add("OBTAINED_HOT_PEPPER");
            }
            // Tambahkan event lain jika perlu
        }
    }

    public void recordFishCatch(String fishName, FishRarity fishRarity) {
        if (fishName != null && !fishName.isBlank() && fishRarity != null) {
            this.fishCaught.putIfAbsent(fishName, new HashMap<>());
            Map<FishRarity, Integer> rarityMap = this.fishCaught.get(fishName);
            rarityMap.put(fishRarity, rarityMap.getOrDefault(fishRarity, 0) + 1);
            this.uniqueFishCaught.add(fishName); // Catat jenis ikan unik
            if (fishName.equalsIgnoreCase("Pufferfish")) {
                this.keyEventsOrItemsObtained.add("FISH_PUFFERFISH");
            }
            if (fishName.equalsIgnoreCase("Legend")) {
                this.keyEventsOrItemsObtained.add("FISH_LEGEND");
            }
        }
    }

    public void updateNpcStatus(String npcName, RelationshipStatus status, int hearts) {
        if (npcName != null && !npcName.isBlank()) {
            this.npcFriendshipStatus.put(npcName, status);
            this.npcHeartPoints.put(npcName, hearts);
        }
    }

    /**
     * Mencatat event kunci atau perolehan item kunci untuk unlock resep.
     * @param eventKey Kunci string unik untuk event/item (misal, "OBTAINED_HOT_PEPPER").
     */
    public void recordKeyEventOrItem(String eventKey) {
        if (eventKey != null && !eventKey.isBlank()) {
            this.keyEventsOrItemsObtained.add(eventKey.toUpperCase()); // Simpan dalam uppercase untuk konsistensi
            System.out.println("Event/Item Tercatat di Statistik: " + eventKey.toUpperCase());
        }
    }

    // --- Metode untuk Mengecek Kondisi (digunakan oleh Recipe.isUnlocked) ---
    public boolean hasAchieved(String eventKey) {
        return this.keyEventsOrItemsObtained.contains(eventKey);
    }

    public int getTotalFishCaughtCount() {
        int total = 0;
        for (Map<FishRarity, Integer> rarityMap : fishCaught.values()) {
            for (int count : rarityMap.values()) {
                total += count;
            }
        }
        return total;
    }

    public boolean hasHarvestedAnyCrop() {
        return !this.uniqueCropsHarvested.isEmpty();
    }


    // --- Metode untuk Menghitung Statistik Turunan & Mendapatkan Ringkasan ---
    public double getAverageSeasonalIncome(Season season) {
        if (season == null || season == Season.ANY) return 0;
        int income = seasonalIncome.getOrDefault(season, 0);
        int days = daysPlayedInSeason.getOrDefault(season, 0);
        return (days > 0) ? (double) income / days : 0;
    }

    public double getAverageSeasonalExpenditure(Season season) {
        if (season == null || season == Season.ANY) return 0;
        int expenditure = seasonalExpenditure.getOrDefault(season, 0);
        int days = daysPlayedInSeason.getOrDefault(season, 0);
        return (days > 0) ? (double) expenditure / days : 0;
    }

    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Game Statistics Summary ===\n\n");
        sb.append("Total Days Played: ").append(totalDaysPlayed).append("\n");
        sb.append("Total Income: ").append(totalIncome).append("g\n");
        sb.append("Total Expenses: ").append(totalExpenditure).append("g\n");

        sb.append("\nIncome by Season (Average per day):\n");
        for (Season s : Season.values()) {
            if (s != Season.ANY && daysPlayedInSeason.getOrDefault(s, 0) > 0) {
                sb.append(String.format("  • %s: %dg (Avg: %.2fg)\n",
                                    s,
                                    seasonalIncome.getOrDefault(s, 0),
                                    getAverageSeasonalIncome(s)));
            }
        }

        sb.append("\nNPC Relationships:\n");
        for (Map.Entry<String, RelationshipStatus> entry : npcFriendshipStatus.entrySet()) {
            String npcName = entry.getKey();
            sb.append("  • ").append(npcName).append(": ")
              .append(entry.getValue())
              .append(" (Hearts: ").append(npcHeartPoints.getOrDefault(npcName, 0)).append(")")
              .append(" | Chat: ").append(chatFrequency.getOrDefault(npcName, 0))
              .append(" | Gift: ").append(giftFrequency.getOrDefault(npcName, 0))
              .append(" | Visit: ").append(visitFrequency.getOrDefault(npcName, 0))
              .append("\n");
        }

        sb.append("\nCrops Harvested (Total):\n");
        if (cropsHarvestedCount.isEmpty()) sb.append("  No crops harvested yet.\n");
        for (Map.Entry<String, Integer> entry : cropsHarvestedCount.entrySet()) {
            sb.append("  • ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" units\n");
        }

        sb.append("\nFish Caught (Total by Type & Rarity):\n");
        if (fishCaught.isEmpty()) sb.append("  No fish caught yet.\n");
        for (Map.Entry<String, Map<FishRarity, Integer>> entry : fishCaught.entrySet()) {
            sb.append("  • ").append(entry.getKey()).append(":\n");
            for (Map.Entry<FishRarity, Integer> rarityEntry : entry.getValue().entrySet()) {
                sb.append("    - ").append(rarityEntry.getKey()).append(": ").append(rarityEntry.getValue()).append(" fish\n");
            }
        }
        return sb.toString();
    }
    // --- Getters untuk Statistik Individual ---

    public int getTotalIncome() { return totalIncome; }
    public int getTotalExpenditure() { return totalExpenditure; }

    /**
     * Mengembalikan map pendapatan per musim. Kunci adalah Season, nilai adalah total pendapatan.
     * @return Map yang unmodifiable.
     */
    public Map<Season, Integer> getSeasonalIncome() {
        return Collections.unmodifiableMap(seasonalIncome);
    }

    /**
     * Mengembalikan map pengeluaran per musim. Kunci adalah Season, nilai adalah total pengeluaran.
     * @return Map yang unmodifiable.
     */
    public Map<Season, Integer> getSeasonalExpenditure() {
        return Collections.unmodifiableMap(seasonalExpenditure);
    }

    public int getTotalDaysPlayed() { return totalDaysPlayed; }

    /**
     * Mengembalikan map status pertemanan NPC. Kunci adalah nama NPC, nilai adalah RelationshipStatus.
     * @return Map yang unmodifiable.
     */
    public Map<String, RelationshipStatus> getNpcFriendshipStatus() {
        return Collections.unmodifiableMap(npcFriendshipStatus);
    }

    /**
     * Mengembalikan map heart points NPC. Kunci adalah nama NPC, nilai adalah jumlah heart points.
     * @return Map yang unmodifiable.
     */
    public Map<String, Integer> getNpcHeartPoints() {
        return Collections.unmodifiableMap(npcHeartPoints);
    }

    /**
     * Mengembalikan map frekuensi chat dengan NPC. Kunci adalah nama NPC, nilai adalah jumlah chat.
     * @return Map yang unmodifiable.
     */
    public Map<String, Integer> getChatFrequency() {
        return Collections.unmodifiableMap(chatFrequency);
    }

    /**
     * Mengembalikan map frekuensi pemberian hadiah ke NPC. Kunci adalah nama NPC, nilai adalah jumlah hadiah.
     * @return Map yang unmodifiable.
     */
    public Map<String, Integer> getGiftFrequency() {
        return Collections.unmodifiableMap(giftFrequency);
    }

    /**
     * Mengembalikan map frekuensi kunjungan ke NPC. Kunci adalah nama NPC, nilai adalah jumlah kunjungan.
     * @return Map yang unmodifiable.
     */
    public Map<String, Integer> getVisitFrequency() {
        return Collections.unmodifiableMap(visitFrequency); // Pastikan ini sudah diaktifkan
    }

    /**
     * Mengembalikan map jumlah tanaman yang dipanen. Kunci adalah nama Crop, nilai adalah jumlah.
     * @return Map yang unmodifiable.
     */
    public Map<String, Integer> getCropsHarvested() {
        return Collections.unmodifiableMap(cropsHarvestedCount);
    }

    /**
     * Mengembalikan map jumlah ikan yang ditangkap, dikelompokkan berdasarkan nama dan raritas.
     * Kunci luar adalah nama Ikan, nilai adalah Map lain.
     * Kunci dalam adalah FishRarity, nilai adalah jumlah.
     * @return Map bersarang yang unmodifiable.
     */
    public Map<String, Map<FishRarity, Integer>> getFishCaught() {
        // Membuat deep unmodifiable map bisa sedikit lebih rumit jika ingin benar-benar aman
        // Untuk kesederhanaan, kita buat unmodifiable untuk map luar.
        // Jika map dalam juga ingin unmodifiable, perlu iterasi.
        Map<String, Map<FishRarity, Integer>> unmodifiableOuterMap = new HashMap<>();
        for (Map.Entry<String, Map<FishRarity, Integer>> entry : fishCaught.entrySet()) {
            unmodifiableOuterMap.put(entry.getKey(), Collections.unmodifiableMap(new HashMap<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(unmodifiableOuterMap);
    }
}
