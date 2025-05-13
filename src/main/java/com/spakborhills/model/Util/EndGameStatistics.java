package com.spakborhills.model.Util; // Atau package yang sesuai

import com.spakborhills.model.Enum.Season;
import com.spakborhills.model.Enum.RelationshipStatus;
import com.spakborhills.model.Enum.FishRarity; 
// import com.spakborhills.model.Farm; 
import com.spakborhills.model.NPC.NPC; 
import com.spakborhills.model.Player; 

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private int totalDaysPlayed;

    // Statistik NPC
    private final Map<String, RelationshipStatus> npcFriendshipStatus;
    private final Map<String, Integer> npcHeartPoints;
    private final Map<String, Integer> chatFrequency;
    private final Map<String, Integer> giftFrequency;
    private final Map<String, Integer> visitFrequency; 

    // Statistik Item
    private final Map<String, Integer> cropsHarvested; // Nama Crop -> Jumlah
    // Fish Name -> (FishRarity -> Jumlah)
    private final Map<String, Map<FishRarity, Integer>> fishCaught;

    /**
     * Konstruktor untuk EndGameStatistics.
     * Menginisialisasi semua statistik ke nilai awal.
     *
     * @param initialNpcs List NPC awal untuk inisialisasi map status NPC.
     * @param initialPlayer Player untuk mendapatkan pengeluaran awal (jika ada).
     */
    public EndGameStatistics(List<NPC> initialNpcs, Player initialPlayer) {
        this.totalIncome = 0;
        this.totalExpenditure = 0; 
        this.seasonalIncome = new HashMap<>();
        this.seasonalExpenditure = new HashMap<>();
        this.totalDaysPlayed = 0; // Atau 1 jika dihitung dari hari pertama, entar pertimbangkan lagi aja

        this.npcFriendshipStatus = new HashMap<>();
        this.npcHeartPoints = new HashMap<>();
        this.chatFrequency = new HashMap<>();
        this.giftFrequency = new HashMap<>();
        this.visitFrequency = new HashMap<>();

        this.cropsHarvested = new HashMap<>();
        this.fishCaught = new HashMap<>();

        // Inisialisasi map musiman
        for (Season s : Season.values()) {
            if (s != Season.ANY) { 
                this.seasonalIncome.put(s, 0);
                this.seasonalExpenditure.put(s, 0);
            }
        }

        // Inisialisasi status awal NPC
        if (initialNpcs != null) {
            for (NPC npc : initialNpcs) {
                this.npcFriendshipStatus.put(npc.getName(), npc.getRelationshipStatus());
                this.npcHeartPoints.put(npc.getName(), npc.getHeartPoints());
                this.chatFrequency.put(npc.getName(), 0);
                this.giftFrequency.put(npc.getName(), 0);
                this.visitFrequency.put(npc.getName(), 0);
            }
        }

        // Jika ada pengeluaran awal dari Player (misal, membeli sesuatu sebelum game dimulai)
        // if (initialPlayer != null) {
        //     this.totalExpenditure += initialPlayer.getInitialExpenditure(); // Perlu metode di Player
        // }
    }


    public void recordIncome(int amount, Season season) {
        if (amount > 0) {
            this.totalIncome += amount;
            if (season != Season.ANY) {
                this.seasonalIncome.put(season, this.seasonalIncome.getOrDefault(season, 0) + amount);
            }
        }
    }

    public void recordExpenditure(int amount, Season season) {
        if (amount > 0) {
            this.totalExpenditure += amount;
            if (season != Season.ANY) {
                this.seasonalExpenditure.put(season, this.seasonalExpenditure.getOrDefault(season, 0) + amount);
            }
        }
    }

    public void incrementDay() {
        this.totalDaysPlayed++;
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

    // recordVisit bisa lebih kompleks, tergantung bagaimana "visit" ditrackj
    public void recordVisit(String npcName) {
        if (npcName != null && !npcName.isBlank()) {
            this.visitFrequency.put(npcName, this.visitFrequency.getOrDefault(npcName, 0) + 1);
        }
    }

    public void recordHarvest(String cropName, int quantity) {
        if (cropName != null && !cropName.isBlank() && quantity > 0) {
            this.cropsHarvested.put(cropName, this.cropsHarvested.getOrDefault(cropName, 0) + quantity);
        }
    }

    public void recordFishCatch(String fishName, FishRarity fishRarity) {
        if (fishName != null && !fishName.isBlank() && fishRarity != null) {
            this.fishCaught.putIfAbsent(fishName, new HashMap<>());
            Map<FishRarity, Integer> rarityMap = this.fishCaught.get(fishName);
            rarityMap.put(fishRarity, rarityMap.getOrDefault(fishRarity, 0) + 1);
        }
    }

    /**
     * Mengupdate status dan heart points NPC.
     * Dipanggil saat ada perubahan signifikan (misal, setelah gifting, proposing, marrying).
     */
    public void updateNpcStatus(String npcName, RelationshipStatus status, int hearts) {
        if (npcName != null && !npcName.isBlank()) {
            this.npcFriendshipStatus.put(npcName, status);
            this.npcHeartPoints.put(npcName, hearts);
        }
    }


    // INI BENERAN HARUS DI ADJUST DEH
    /**
     * Menghitung rata-rata pendapatan per musim.
     * @param season Musim yang diinginkan.
     * @return Rata-rata pendapatan, atau 0 jika tidak ada data/hari.
     */
    public double getAverageSeasonalIncome(Season season) {
        if (season == Season.ANY || totalDaysPlayed == 0) return 0; // Tidak ada rata-rata untuk ANY
        // Perlu cara untuk mengetahui berapa banyak hari yang telah berlalu di musim tertentu
        // Untuk simplifikasi, kita bisa bagi dengan total hari / jumlah musim yang telah lewat
        // Atau, bagi dengan jumlah hari dalam satu musim jika game sudah melewati musim itu.
        // Ini bisa jadi kompleks. Untuk sekarang, kita bagi dengan total hari saja (kurang akurat).
        int income = seasonalIncome.getOrDefault(season, 0);
        // Asumsi sederhana: bagi dengan total hari / 4 (jika semua musim sudah dilewati)
        // Atau jika ingin lebih akurat, perlu melacak jumlah hari per musim yang telah dimainkan.
        // Untuk sekarang, kita buat rata-rata sederhana:
        if (totalDaysPlayed > 0) {
            // Ini adalah rata-rata pendapatan di musim X per total hari main, bukan per hari di musim X
            // return (double) income / totalDaysPlayed;
            // Lebih baik: jika kita tahu jumlah hari di musim itu yang sudah lewat
            int daysInOneSeason = GameTime.DAYS_IN_SEASON; // Asumsi ada konstanta ini di GameTime
            int seasonsPassed = (totalDaysPlayed / daysInOneSeason);
            if (totalDaysPlayed % daysInOneSeason > 0 && season == getCurrentSeasonFromDays(totalDaysPlayed)) {
                seasonsPassed++; // Hitung musim saat ini jika belum selesai
            }
            if (seasonsPassed > 0) {
                 // Ini masih belum sempurna, karena hanya menghitung rata-rata jika musim sudah lewat penuh
                 // atau sedang berjalan.
                 // Untuk statistik akhir, mungkin lebih baik hitung total pendapatan musim / jumlah hari di musim itu
                 // jika musim itu sudah selesai.
                 return (double) income / Math.max(1, seasonsPassed * daysInOneSeason / 4.0); // Placeholder kasar
            }
        }
        return 0;
    }
     // Helper untuk mendapatkan musim saat ini berdasarkan total hari (perlu disesuaikan dengan GameTime)
    private Season getCurrentSeasonFromDays(int totalDays) {
        int dayInYear = (totalDays -1) % (GameTime.DAYS_IN_SEASON * 4) + 1;
        if (dayInYear <= GameTime.DAYS_IN_SEASON) return Season.SPRING;
        if (dayInYear <= GameTime.DAYS_IN_SEASON * 2) return Season.SUMMER;
        if (dayInYear <= GameTime.DAYS_IN_SEASON * 3) return Season.FALL;
        return Season.WINTER;
    }


    public double getAverageSeasonalExpenditure(Season season) {
        // Logika serupa dengan getAverageSeasonalIncome
        if (season == Season.ANY || totalDaysPlayed == 0) return 0;
        int expenditure = seasonalExpenditure.getOrDefault(season, 0);
        // Logika pembagian yang lebih akurat diperlukan di sini juga.
        if (totalDaysPlayed > 0) {
            return (double) expenditure / Math.max(1, totalDaysPlayed / 4.0); // Placeholder kasar
        }
        return 0;
    }

    /**
     * Metode computeAll(farm: Farm) dari diagram mungkin tidak diperlukan jika
     * semua data dicatat secara inkremental. Jika diperlukan untuk kalkulasi akhir
     * atau validasi, bisa ditambahkan.
     * Untuk saat ini, kita asumsikan perekaman inkremental sudah cukup.
     */
    // public void computeAll(Farm farm) {
    //     // Metode ini bisa digunakan untuk mengkalkulasi ulang semua statistik dari state Farm saat ini
    //     // jika perekaman inkremental terlewat atau untuk validasi.
    //     // Contoh: this.totalDaysPlayed = farm.getCurrentTime().getTotalDaysPlayed();
    //     //         this.totalIncome = ... (perlu cara mengambil histori transaksi)
    // }

    /**
     * Menghasilkan ringkasan statistik dalam format String untuk ditampilkan.
     * @return String berisi ringkasan statistik.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Statistik Akhir Permainan ---\n");
        sb.append("Total Hari Dimainkan: ").append(totalDaysPlayed).append("\n");
        sb.append("Total Pendapatan: ").append(totalIncome).append("g\n");
        sb.append("Total Pengeluaran: ").append(totalExpenditure).append("g\n");

        sb.append("\nPendapatan per Musim:\n");
        for (Season s : Season.values()) {
            if (s != Season.ANY) {
                sb.append("  - ").append(s).append(": ").append(seasonalIncome.getOrDefault(s, 0)).append("g");
                sb.append(" (Rata-rata: ").append(String.format("%.2f", getAverageSeasonalIncome(s))).append("g/hari di musim itu)\n"); // Perlu logika rata-rata yg benar
                sb.append("\n");
            }
        }
        // Tambahkan pengeluaran per musim jika perlu

        sb.append("\nStatus NPC:\n");
        for (Map.Entry<String, RelationshipStatus> entry : npcFriendshipStatus.entrySet()) {
            String npcName = entry.getKey();
            sb.append("  - ").append(npcName).append(": ")
              .append(entry.getValue())
              .append(" (Hearts: ").append(npcHeartPoints.getOrDefault(npcName, 0)).append(")")
              .append(" | Chat: ").append(chatFrequency.getOrDefault(npcName, 0))
              .append(" | Gift: ").append(giftFrequency.getOrDefault(npcName, 0))
              .append(" | Visit: ").append(visitFrequency.getOrDefault(npcName, 0))
              .append("\n");
        }

        sb.append("\nTanaman Dipanen:\n");
        if (cropsHarvested.isEmpty()) sb.append("  Belum ada tanaman yang dipanen.\n");
        for (Map.Entry<String, Integer> entry : cropsHarvested.entrySet()) {
            sb.append("  - ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" buah\n");
        }

        sb.append("\nIkan Ditangkap:\n");
        if (fishCaught.isEmpty()) sb.append("  Belum ada ikan yang ditangkap.\n");
        for (Map.Entry<String, Map<FishRarity, Integer>> entry : fishCaught.entrySet()) {
            sb.append("  - ").append(entry.getKey()).append(":\n");
            for (Map.Entry<FishRarity, Integer> rarityEntry : entry.getValue().entrySet()) {
                sb.append("    - ").append(rarityEntry.getKey()).append(": ").append(rarityEntry.getValue()).append(" ekor\n");
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
        return Collections.unmodifiableMap(cropsHarvested);
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
