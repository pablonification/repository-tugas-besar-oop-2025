package com.spakborhills.model.Util;

import com.spakborhills.model.Enum.Season;
import com.spakborhills.model.Enum.RelationshipStatus;
import com.spakborhills.model.Enum.FishRarity; 
import com.spakborhills.model.NPC.NPC; 
import com.spakborhills.model.Player; 

import java.util.HashMap;
import java.util.HashSet; 
import java.util.List;
import java.util.Map;
import java.util.Set; 
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
    private final Map<Season, Integer> daysPlayedInSeason;
    private int totalDaysPlayed;

    private final Map<String, RelationshipStatus> npcFriendshipStatus;
    private final Map<String, Integer> npcHeartPoints;
    private final Map<String, Integer> chatFrequency;
    private final Map<String, Integer> giftFrequency;
    private final Map<String, Integer> visitFrequency; 
    private final Map<String, Integer> cropsHarvestedCount; 
    private final Set<String> uniqueCropsHarvested;      
    private final Map<String, Map<FishRarity, Integer>> fishCaught;
    private final Set<String> uniqueFishCaught;          
    private final Set<String> keyEventsOrItemsObtained;

    public EndGameStatistics(List<NPC> initialNpcs, Player initialPlayer) {
        this.totalIncome = 0;
        this.totalExpenditure = 0;
        this.seasonalIncome = new HashMap<>();
        this.seasonalExpenditure = new HashMap<>();
        this.daysPlayedInSeason = new HashMap<>(); 
        this.totalDaysPlayed = 0;

        this.npcFriendshipStatus = new HashMap<>();
        this.npcHeartPoints = new HashMap<>();
        this.chatFrequency = new HashMap<>();
        this.giftFrequency = new HashMap<>();
        this.visitFrequency = new HashMap<>(); 

        this.cropsHarvestedCount = new HashMap<>();
        this.uniqueCropsHarvested = new HashSet<>(); 
        this.fishCaught = new HashMap<>();
        this.uniqueFishCaught = new HashSet<>(); 
        this.keyEventsOrItemsObtained = new HashSet<>(); 

        for (Season s : Season.values()) {
            if (s != Season.ANY) {
                this.seasonalIncome.put(s, 0);
                this.seasonalExpenditure.put(s, 0);
                this.daysPlayedInSeason.put(s, 0); 
            }
        }

        if (initialNpcs != null) {
            for (NPC npc : initialNpcs) {
                this.npcFriendshipStatus.put(npc.getName(), npc.getRelationshipStatus());
                this.npcHeartPoints.put(npc.getName(), npc.getHeartPoints());
                this.chatFrequency.put(npc.getName(), 0);
                this.giftFrequency.put(npc.getName(), 0);
                this.visitFrequency.put(npc.getName(), 0);
            }
        }
    }

    // Metode Perekaman Data Inkremental 
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
            this.uniqueCropsHarvested.add(cropName); 
            if (cropName.equalsIgnoreCase("Parsnip")) { 
                this.keyEventsOrItemsObtained.add("HARVEST_PARSNIP");
            }
            if (cropName.equalsIgnoreCase("Hot Pepper")) {
                this.keyEventsOrItemsObtained.add("OBTAINED_HOT_PEPPER");
            }
        }
    }

    public void recordFishCatch(String fishName, FishRarity fishRarity) {
        if (fishName != null && !fishName.isBlank() && fishRarity != null) {
            System.out.println("DEBUG: Recording fish catch in statistics - " + fishName + " (Rarity: " + fishRarity + ")");
            
            synchronized (this) { 
                if (!this.fishCaught.containsKey(fishName)) {
                    this.fishCaught.put(fishName, new HashMap<>());
                    System.out.println("DEBUG: Created new entry for fish: " + fishName);
                }
                
                Map<FishRarity, Integer> rarityMap = this.fishCaught.get(fishName);
                
                int currentCount = rarityMap.getOrDefault(fishRarity, 0);
                int newCount = currentCount + 1;
                rarityMap.put(fishRarity, newCount);
                System.out.println("DEBUG: Updated count for " + fishName + " (" + fishRarity + "): " + currentCount + " -> " + newCount);
                
                boolean wasNewFish = !this.uniqueFishCaught.contains(fishName);
                this.uniqueFishCaught.add(fishName);
                if (wasNewFish) {
                    System.out.println("DEBUG: Added new unique fish: " + fishName);
                }
                
                int totalFishCount = getTotalFishCaughtCount();
                System.out.println("DEBUG: After recording - Total Fish Caught: " + totalFishCount);
                System.out.println("DEBUG: Unique Fish Types: " + uniqueFishCaught.size() + " - " + String.join(", ", uniqueFishCaught));
                
                System.out.println("DEBUG: Full fish caught map:");
                for (Map.Entry<String, Map<FishRarity, Integer>> entry : fishCaught.entrySet()) {
                    System.out.println("DEBUG:   Fish: " + entry.getKey());
                    for (Map.Entry<FishRarity, Integer> rarityEntry : entry.getValue().entrySet()) {
                        System.out.println("DEBUG:     " + rarityEntry.getKey() + ": " + rarityEntry.getValue());
                    }
                }
                
                if (fishName.equalsIgnoreCase("Pufferfish")) {
                    this.keyEventsOrItemsObtained.add("FISH_PUFFERFISH");
                }
                if (fishName.equalsIgnoreCase("Legend")) {
                    this.keyEventsOrItemsObtained.add("FISH_LEGEND");
                }
            }
        } else {
            System.err.println("ERROR: Attempted to record invalid fish catch - Name: " + 
                (fishName != null ? fishName : "null") + ", Rarity: " + 
                (fishRarity != null ? fishRarity.toString() : "null"));
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
            this.keyEventsOrItemsObtained.add(eventKey.toUpperCase()); 
            System.out.println("Event/Item Tercatat di Statistik: " + eventKey.toUpperCase());
        }
    }

    // Metode untuk Mengecek Kondisi (digunakan oleh Recipe.isUnlocked) 
    public boolean hasAchieved(String eventKey) {
        return this.keyEventsOrItemsObtained.contains(eventKey);
    }

    public int getTotalFishCaughtCount() {
        synchronized (this) { 
            int total = 0;
            for (Map<FishRarity, Integer> rarityMap : fishCaught.values()) {
                for (int count : rarityMap.values()) {
                    total += count;
                }
            }
            return total;
        }
    }

    public boolean hasHarvestedAnyCrop() {
        return !this.uniqueCropsHarvested.isEmpty();
    }


    // Metode untuk Menghitung Statistik Turunan & Mendapatkan Ringkasan 
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
        sb.append("Total Expenditure: ").append(totalExpenditure).append("g\n");

        sb.append("\nIncome by Season (Average per day):\n");
        for (Season s : Season.values()) {
            if (s != Season.ANY && daysPlayedInSeason.getOrDefault(s, 0) > 0) {
                sb.append(String.format("  - %s: %dg (Avg: %.2fg)\n",
                                        s,
                                        seasonalIncome.getOrDefault(s, 0),
                                        getAverageSeasonalIncome(s)));
            }
        }
        sb.append("\nExpenditure by Season (Average per day):\n");
        for (Season s : Season.values()) {
            if (s != Season.ANY && daysPlayedInSeason.getOrDefault(s, 0) > 0) {
                sb.append(String.format("  - %s: %dg (Avg: %.2fg)\n",
                                        s,
                                        seasonalExpenditure.getOrDefault(s, 0),
                                        getAverageSeasonalExpenditure(s)));
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

        System.out.println("DEBUG: Generating Statistics Summary");
        System.out.println("DEBUG: Crops Harvested Count entries: " + cropsHarvestedCount.size());
        System.out.println("DEBUG: Unique Crops Harvested: " + uniqueCropsHarvested.size());

        sb.append("\nCrops Harvested (Total):\n");
        if (cropsHarvestedCount.isEmpty()) sb.append("  No crops harvested yet.\n");
        for (Map.Entry<String, Integer> entry : cropsHarvestedCount.entrySet()) {
            sb.append("  • ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" units\n");
        }

        int totalFish = getTotalFishCaughtCount();
        System.out.println("DEBUG: Total Fish Caught Count: " + totalFish);
        System.out.println("DEBUG: Unique Fish Count: " + uniqueFishCaught.size());
        System.out.println("DEBUG: Fish Caught Map entries: " + fishCaught.size());
        
        System.out.println("DEBUG: Detailed fish caught map state before summary generation:");
        for (Map.Entry<String, Map<FishRarity, Integer>> fishEntry : fishCaught.entrySet()) {
            String fishNameForLog = fishEntry.getKey();
            Map<FishRarity, Integer> innerMap = fishEntry.getValue();
            System.out.println("DEBUG:   Processing Fish: [" + fishNameForLog + "]");
            if (innerMap == null) {
                System.out.println("DEBUG:     INNER MAP FOR [" + fishNameForLog + "] IS NULL!");
            } else if (innerMap.isEmpty()) {
                System.out.println("DEBUG:     INNER MAP FOR [" + fishNameForLog + "] IS EMPTY!");
            } else {
                System.out.println("DEBUG:     Inner map for [" + fishNameForLog + "] contains:");
                for (Map.Entry<FishRarity, Integer> rarityEntry : innerMap.entrySet()) {
                    System.out.println("DEBUG:       - Rarity: " + rarityEntry.getKey() + ", Count: " + rarityEntry.getValue());
                }
            }
        }

        sb.append("\nFish Caught (Total by Type & Rarity):\n");
        if (fishCaught.isEmpty()) sb.append("  No fish caught yet.\n");
        for (Map.Entry<String, Map<FishRarity, Integer>> entry : fishCaught.entrySet()) {
            sb.append("  - ").append(entry.getKey()).append(":\n");
            for (Map.Entry<FishRarity, Integer> rarityEntry : entry.getValue().entrySet()) {
                sb.append("    - ").append(rarityEntry.getKey()).append(": ").append(rarityEntry.getValue()).append(" fish\n");
            }
        }
        return sb.toString();
    }

    // Getters untuk Statistik Individual 
    public int getTotalIncome() { return totalIncome; }
    public int getTotalExpenditure() { return totalExpenditure; }
    
    /**
     * Directly sets the total income value.
     * This is used when loading from a save file to ensure exact values are preserved.
     * @param totalIncome The total income value to set
     */
    public void setTotalIncome(int totalIncome) {
        this.totalIncome = totalIncome;
    }
    
    /**
     * Directly sets the total expenditure value.
     * This is used when loading from a save file to ensure exact values are preserved.
     * @param totalExpenditure The total expenditure value to set
     */
    public void setTotalExpenditure(int totalExpenditure) {
        this.totalExpenditure = totalExpenditure;
    }
    
    /**
     * Directly sets the total days played value.
     * This is used when loading from a save file to ensure exact values are preserved.
     * @param totalDaysPlayed The total days played value to set
     */
    public void setTotalDaysPlayed(int totalDaysPlayed) {
        this.totalDaysPlayed = totalDaysPlayed;
    }

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
    public Map<String, Integer> getCropsHarvestedCount() {
        return Collections.unmodifiableMap(cropsHarvestedCount);
    }

    /**
     * Mengembalikan set nama tanaman unik yang pernah dipanen.
     * @return Set yang unmodifiable.
     */
    public Set<String> getUniqueCropsHarvested() {
        return Collections.unmodifiableSet(uniqueCropsHarvested);
    }

    /**
     * Returns a copy of the uniqueFishCaught set to prevent external modification.
     * @return A new Set containing the same elements as uniqueFishCaught.
     */
    public Set<String> getUniqueFishCaught() {
        synchronized (this) {
            // Return a new HashSet to prevent external modification of our internal set
            return new HashSet<>(uniqueFishCaught);
        }
    }

    /**
     * Mengembalikan set event atau item kunci yang pernah diperoleh.
     * @return Set yang unmodifiable.
     */
    public Set<String> getKeyEventsOrItemsObtained() {
        return Collections.unmodifiableSet(keyEventsOrItemsObtained);
    }

    /**
     * Returns a deep copy of the fishCaught map to prevent external modification.
     * This is safer than returning an unmodifiable view that might still allow inner map modification.
     * @return A deep copy of the fishCaught map.
     */
    public Map<String, Map<FishRarity, Integer>> getFishCaught() {
        synchronized (this) {
            Map<String, Map<FishRarity, Integer>> deepCopy = new HashMap<>();
            for (Map.Entry<String, Map<FishRarity, Integer>> entry : fishCaught.entrySet()) {
                Map<FishRarity, Integer> innerCopy = new HashMap<>(entry.getValue());
                deepCopy.put(entry.getKey(), innerCopy);
            }
            return deepCopy;
        }
    }

    /**
     * Directly sets the fish caught data from a save.
     * This is used when loading a save to ensure fish data is preserved.
     * @param fishCaught The fish caught map to set
     * @param uniqueFishCaught The unique fish caught set to set
     */
    public void setFishData(Map<String, Map<FishRarity, Integer>> fishCaught, Set<String> uniqueFishCaught) {
        if (fishCaught != null && uniqueFishCaught != null) {
            synchronized (this) {
                this.fishCaught.clear();
                for (Map.Entry<String, Map<FishRarity, Integer>> entry : fishCaught.entrySet()) {
                    this.fishCaught.put(entry.getKey(), new HashMap<>(entry.getValue()));
                }
                
                this.uniqueFishCaught.clear();
                this.uniqueFishCaught.addAll(uniqueFishCaught);
                
                System.out.println("DEBUG: Fish data loaded from save: " + this.uniqueFishCaught.size() + 
                                  " unique fish, total caught: " + getTotalFishCaughtCount());
            }
        } else {
            System.err.println("WARNING: Attempted to set null fish data");
        }
    }
}
