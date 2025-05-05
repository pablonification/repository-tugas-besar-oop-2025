package com.spakborhills;

import com.spakborhills.model.*;
import com.spakborhills.model.Enum.*;
import com.spakborhills.model.Item.*;
import com.spakborhills.model.Map.*;
import com.spakborhills.model.NPC.*;
// import com.spakborhills.model.Store.*;
import com.spakborhills.model.Util.*;

import java.util.ArrayList;
import java.util.Arrays; // Untuk Arrays.asList
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// import java.util.Optional;
import java.util.Set; // Untuk Set di Fish
import java.awt.Point;

public class Main {

    // Helper untuk mencetak status Player (mengurangi repetisi)
    private static void printPlayerStatus(Player player) {
        System.out.println("  Status Pemain: [Nama: " + player.getName() +
                           ", Energi: " + player.getEnergy() +
                           ", Gold: " + player.getGold() +
                           ", Posisi: (" + player.getCurrentTileX() + "," + player.getCurrentTileY() + ")" +
                           ", Map: " + player.getCurrentMap().getName() +
                           "]");
        System.out.println(player.getInventory()); // Cetak inventory
    }

    // Helper untuk membuat TimeRange (jika tidak publik)
    // Jika TimeRange publik, ini tidak perlu
    private static Fish.TimeRange createTimeRange(int start, int end) {
        // Fish dummyFish = new Fish("dummy", FishRarity.COMMON, Set.of(Season.ANY), null, Set.of(Weather.ANY), Set.of(LocationType.FARM));
        return new Fish.TimeRange(start, end);
    }


    public static void main(String[] args) {
        System.out.println("=============================================");
        System.out.println("=== Memulai Driver Testing Spakbor Hills ===");
        System.out.println("=============================================");

        // --- Tahap 1: Setup Item Registry & Komponen Dasar ---
        System.out.println("\n--- Tahap 1: Setup Item Registry & Komponen Dasar ---");
        Map<String, Item> itemRegistry = new HashMap<>();
        try {
            // Seeds (Contoh beberapa)
            Seed parsnipSeeds = new Seed("Parsnip Seeds", 20, 1, Season.SPRING, "Parsnip", 1);
            Seed cauliflowerSeeds = new Seed("Cauliflower Seeds", 80, 5, Season.SPRING, "Cauliflower", 1);
            Seed blueberrySeeds = new Seed("Blueberry Seeds", 80, 7, Season.SUMMER, "Blueberry", 3);
            itemRegistry.put(parsnipSeeds.getName(), parsnipSeeds);
            itemRegistry.put(cauliflowerSeeds.getName(), cauliflowerSeeds);
            itemRegistry.put(blueberrySeeds.getName(), blueberrySeeds);

            // Crops (Contoh beberapa, harga beli 0 jika hanya dari panen)
            Crop parsnip = new Crop("Parsnip", 0, 35); // Harga beli 0 asumsi
            Crop cauliflower = new Crop("Cauliflower", 0, 150);
            Crop blueberry = new Crop("Blueberry", 0, 40);
            itemRegistry.put(parsnip.getName(), parsnip);
            itemRegistry.put(cauliflower.getName(), cauliflower);
            itemRegistry.put(blueberry.getName(), blueberry);

            // Equipment (Item Awal)
            Equipment hoe = new Equipment("Hoe", "Hoe");
            Equipment wateringCan = new Equipment("Watering Can", "WateringCan");
            Equipment pickaxe = new Equipment("Pickaxe", "Pickaxe");
            Equipment fishingRod = new Equipment("Fishing Rod", "FishingRod");
            itemRegistry.put(hoe.getName(), hoe);
            itemRegistry.put(wateringCan.getName(), wateringCan);
            itemRegistry.put(pickaxe.getName(), pickaxe);
            itemRegistry.put(fishingRod.getName(), fishingRod);

            // Misc Items (Contoh)
            MiscItem coal = new MiscItem("Coal", 20, 10);
            MiscItem firewood = new MiscItem("Firewood", 10, 5);
            itemRegistry.put(coal.getName(), coal);
            itemRegistry.put(firewood.getName(), firewood);

            // Food (Contoh)
            Food fishNChips = new Food("Fish n' Chips", 50, 150, 135);
            itemRegistry.put(fishNChips.getName(), fishNChips);

            // Special Items
            ProposalRing proposalRing = new ProposalRing();
            itemRegistry.put(proposalRing.getName(), proposalRing);

            // Fish (Contoh - perlu helper TimeRange atau TimeRange publik)
             List<Fish.TimeRange> anyTime = List.of(createTimeRange(0, 23)); // Sepanjang hari
             List<Fish.TimeRange> halibutTimes = Arrays.asList(createTimeRange(6, 11), createTimeRange(19, 2));
             Fish bullhead = new Fish("Bullhead", FishRarity.COMMON, Set.of(Season.ANY), anyTime, Set.of(Weather.ANY), Set.of(LocationType.MOUNTAIN_LAKE));
             Fish halibut = new Fish("Halibut", FishRarity.REGULAR, Set.of(Season.ANY), halibutTimes, Set.of(Weather.ANY), Set.of(LocationType.OCEAN));
             itemRegistry.put(bullhead.getName(), bullhead);
             itemRegistry.put(halibut.getName(), halibut);

            System.out.println("Item Registry berhasil dibuat dengan " + itemRegistry.size() + " item.");

        } catch (Exception e) {
            System.err.println("ERROR saat membuat Item Registry: " + e.getMessage());
            e.printStackTrace();
            return; // Hentikan jika registry gagal
        }

        // Komponen Dasar (Menggunakan Stub/Implementasi Dasar)
        FarmMap farmMap = new FarmMap();
        GameTime gameTime = new GameTime();
        ShippingBin shippingBin = new ShippingBin();
        EndGameStatistics statistics = new EndGameStatistics();
        PriceList priceList = new PriceList();
        WorldMap worldMap = new WorldMap(); // Stub
        Store store = new Store(); // Stub

        // --- Tahap 2: Setup NPC & Recipe ---
        System.out.println("\n--- Tahap 2: Setup NPC & Recipe ---");
        List<NPC> npcList = new ArrayList<>();
        NPC mayor = new MayorTadi(); // Asumsi sudah ada
        NPC caroline = new Caroline(); // Asumsi sudah ada
        NPC perry = new Perry(); // Asumsi sudah ada
        npcList.add(mayor);
        npcList.add(caroline);
        npcList.add(perry);
        System.out.println("List NPC dibuat dengan " + npcList.size() + " NPC.");

        List<Recipe> recipeList = new ArrayList<>();
        // Tambahkan resep stub atau resep nyata jika kelas Recipe mendukungnya
        // Recipe fishStewRecipe = new Recipe("Fish Stew", "Fish Stew Result", Map.of("Fish", 1, "Vegetable", 1), "Coal");
        // recipeList.add(fishStewRecipe);
        System.out.println("List Recipe dibuat.");

        // --- Tahap 3: Setup Player & Farm ---
        System.out.println("\n--- Tahap 3: Setup Player & Farm ---");
        Player player = null;
        Farm farm = null;
        try {
            player = new Player("Tester", Gender.FEMALE, "Kebun Uji", farmMap, 5, 5, itemRegistry);
            System.out.println("Objek Player berhasil dibuat.");
            farm = new Farm(
                player.getFarmName(), player, farmMap, worldMap, store,
                npcList, recipeList, gameTime, shippingBin, statistics, priceList
            );
            System.out.println("Objek Farm berhasil dibuat.");
        } catch (Exception e) {
            System.err.println("ERROR saat membuat Player atau Farm: " + e.getMessage());
            e.printStackTrace();
            return; // Hentikan jika gagal
        }

        System.out.println("\n=============================================");
        System.out.println("=== Memulai Sesi Testing ===");
        System.out.println("=============================================");

        // --- Testing Player State & Basic Modifiers ---
        System.out.println("\n--- Testing: Player State & Basic Modifiers ---");
        printPlayerStatus(player);
        System.out.println("Test: Menambah 100 gold...");
        player.addGold(100);
        System.out.println("Test: Mencoba menghabiskan 50 gold (Harusnya berhasil)...");
        boolean spent1 = player.spendGold(50);
        System.out.println("  Hasil: " + spent1);
        System.out.println("Test: Mencoba menghabiskan 1000 gold (Harusnya gagal)...");
        boolean spent2 = player.spendGold(1000);
        System.out.println("  Hasil: " + spent2);
        System.out.println("Test: Mencoba menghabiskan -10 gold (Harusnya gagal)...");
        boolean spent3 = player.spendGold(-10);
        System.out.println("  Hasil: " + spent3);
        System.out.println("Test: Mengurangi 20 energi...");
        player.changeEnergy(-20);
        System.out.println("Test: Menambah 50 energi (Harusnya cap di MAX)...");
        player.changeEnergy(50);
        System.out.println("Test: Mengurangi energi hingga minimum (-120)...");
        player.changeEnergy(-120); // 100 - 120 = -20 (MIN_ENERGY)
        System.out.println("Test: Mencoba mengurangi energi lagi (-10, Harusnya tetap di MIN)...");
        player.changeEnergy(-10);
        printPlayerStatus(player);
        player.changeEnergy(Player.MAX_ENERGY - player.getEnergy()); // Reset energi ke MAX

        // --- Testing Inventory ---
        System.out.println("\n--- Testing: Inventory ---");
        Item coal = itemRegistry.get("Coal");
        Item parsnip = itemRegistry.get("Parsnip");
        System.out.println("Test: Menambah 5 Coal...");
        player.getInventory().addItem(coal, 5);
        System.out.println("  Jumlah Coal: " + player.getInventory().getItemCount(coal));
        System.out.println("Test: Menambah 3 Parsnip...");
        player.getInventory().addItem(parsnip, 3);
        printPlayerStatus(player);
        System.out.println("Test: Menghapus 2 Coal (Harusnya berhasil)...");
        boolean removed1 = player.getInventory().removeItem(coal, 2);
        System.out.println("  Hasil: " + removed1 + ", Sisa Coal: " + player.getInventory().getItemCount(coal));
        System.out.println("Test: Menghapus 5 Coal (Harusnya gagal)...");
        boolean removed2 = player.getInventory().removeItem(coal, 5); // Sisa 3
        System.out.println("  Hasil: " + removed2 + ", Sisa Coal: " + player.getInventory().getItemCount(coal));
        System.out.println("Test: Menghapus 3 Coal (Harusnya berhasil)...");
        boolean removed3 = player.getInventory().removeItem(coal, 3);
        System.out.println("  Hasil: " + removed3 + ", Sisa Coal: " + player.getInventory().getItemCount(coal));
        System.out.println("Test: Cek punya Hoe (Harusnya true)... " + player.getInventory().hasTool("Hoe"));
        System.out.println("Test: Cek punya Axe (Harusnya false)... " + player.getInventory().hasTool("Axe"));
        System.out.println("Test: Cek punya >= 2 Parsnip (Harusnya true)... " + player.getInventory().hasItem(parsnip, 2));
        System.out.println("Test: Cek punya >= 5 Parsnip (Harusnya false)... " + player.getInventory().hasItem(parsnip, 5));
        player.getInventory().removeItem(parsnip, 3); // Bersihkan parsnip

        // --- Testing Movement ---
        System.out.println("\n--- Testing: Movement ---");
        Point startPos = player.getPosition();
        System.out.println("Test: Bergerak ke Selatan (Harusnya berhasil)...");
        player.move(Direction.SOUTH);
        System.out.println("Test: Bergerak ke Timur (Harusnya berhasil)...");
        player.move(Direction.EAST);
        printPlayerStatus(player);
        System.out.println("Test: Bergerak ke Utara (Harusnya berhasil)...");
        player.move(Direction.NORTH);
        System.out.println("Test: Bergerak ke Barat (Harusnya berhasil, kembali ke awal)...");
        player.move(Direction.WEST);
        printPlayerStatus(player);
        System.out.println("Test: Mencoba bergerak ke Utara (dari 5,5 -> 5,4, Harusnya berhasil)...");
        player.move(Direction.NORTH);
        printPlayerStatus(player);
        System.out.println("Test: Mencoba bergerak ke Utara lagi berkali-kali (Harusnya gagal di batas map)...");
        for (int i = 0; i < 10; i++) {
            if (!player.move(Direction.NORTH)) break;
        }
        printPlayerStatus(player);
        // Kembalikan ke posisi awal
        player.setPosition(startPos.x, startPos.y);

        // --- Testing Farming Actions (Menggunakan Stub Tile) ---
        System.out.println("\n--- Testing: Farming Actions ---");
        Tile tileDepan = farmMap.getTile(player.getCurrentTileX(), player.getCurrentTileY() + 1); // Tile di Selatan
        Tile tileKanan = farmMap.getTile(player.getCurrentTileX() + 1, player.getCurrentTileY()); // Tile di Timur
        if (tileDepan != null) {
            System.out.println("Test: Mencangkul tile depan (Harusnya berhasil)...");
            player.till(tileDepan); // Energi tidak berkurang di sini (delegasi)
            System.out.println("  Tipe Tile Depan: " + tileDepan.getType());
            System.out.println("Test: Mencangkul lagi tile depan (Harusnya gagal)...");
            player.till(tileDepan);
            System.out.println("Test: Menyiram tile depan (Harusnya berhasil)...");
            player.water(tileDepan);
            System.out.println("  Tile Depan Disiram: " + tileDepan.isWatered());
            System.out.println("Test: Menyiram lagi tile depan (Harusnya gagal)...");
            player.water(tileDepan);
            System.out.println("Test: Menanam Parsnip Seeds di tile depan (Harusnya berhasil)...");
            player.plant((Seed) itemRegistry.get("Parsnip Seeds"), tileDepan);
            System.out.println("  Tipe Tile Depan: " + tileDepan.getType());
            System.out.println("  Benih Tertanam: " + (tileDepan.getPlantedSeed() != null ? tileDepan.getPlantedSeed().getName() : "null"));
            System.out.println("Test: Mencoba menanam lagi (Harusnya gagal)...");
            player.plant((Seed) itemRegistry.get("Cauliflower Seeds"), tileDepan);
            System.out.println("Test: Memanen tile depan (Harusnya berhasil, stub)...");
            player.harvest(tileDepan, itemRegistry);
            System.out.println("  Tipe Tile Depan setelah panen: " + tileDepan.getType());
            System.out.println("Test: Memulihkan tile depan (Harusnya berhasil)...");
            player.recoverLand(tileDepan);
            System.out.println("  Tipe Tile Depan setelah pulih: " + tileDepan.getType());
        }
        if (tileKanan != null) {
             System.out.println("Test: Mencoba mencangkul tile kanan tanpa Hoe (Hapus Hoe dulu)...");
             Item hoeToRemove = itemRegistry.get("Hoe");
             player.getInventory().removeItem(hoeToRemove, 1);
             player.till(tileKanan);
             player.getInventory().addItem(hoeToRemove, 1); // Kembalikan Hoe
        }

        // --- Testing Interaction Actions ---
        System.out.println("\n--- Testing: Interaction Actions ---");
        NPC targetNpc = farm.findNPC("Mayor Tadi").orElse(null);
        if (targetNpc != null) {
            System.out.println("Test: Chat dengan " + targetNpc.getName() + "...");
            int hpAwal = targetNpc.getHeartPoints();
            player.chat(targetNpc);
            System.out.println("  HP Awal: " + hpAwal + ", HP Akhir: " + targetNpc.getHeartPoints());

            System.out.println("Test: Gift Coal (Hated by Mayor Tadi - asumsi) ke " + targetNpc.getName() + "...");
            Item coalToGift = itemRegistry.get("Coal");
            player.getInventory().addItem(coalToGift, 1); // Pastikan punya
            hpAwal = targetNpc.getHeartPoints();
            player.gift(targetNpc, coalToGift);
            System.out.println("  HP Awal: " + hpAwal + ", HP Akhir: " + targetNpc.getHeartPoints());

            System.out.println("Test: Gift Parsnip (Neutral - asumsi) ke " + targetNpc.getName() + "...");
            Item parsnipToGift = itemRegistry.get("Parsnip");
            player.getInventory().addItem(parsnipToGift, 1);
            hpAwal = targetNpc.getHeartPoints();
            player.gift(targetNpc, parsnipToGift);
            System.out.println("  HP Awal: " + hpAwal + ", HP Akhir: " + targetNpc.getHeartPoints());

            // Anda perlu menambahkan item Loved/Liked ke registry dan NPC MayorTadi
            // untuk menguji kasus +25/+20
        } else {
            System.out.println("WARNING: Mayor Tadi tidak ditemukan untuk testing interaksi.");
        }

        // --- Testing Other Actions ---
        System.out.println("\n--- Testing: Other Actions ---");
        System.out.println("Test: Makan Parsnip...");
        Item parsnipToEat = itemRegistry.get("Parsnip");
        player.getInventory().addItem(parsnipToEat, 1);
        player.changeEnergy(-10);
        int energyAwal = player.getEnergy();
        player.eat(parsnipToEat);
        System.out.println("  Energi Awal: " + energyAwal + ", Energi Akhir: " + player.getEnergy());
        System.out.println("Test: Makan Hoe (Harusnya gagal)...");
        player.eat(itemRegistry.get("Hoe"));

        System.out.println("Test: Tidur (Energi normal, tanpa bonus bed)...");
        player.sleep(player.getEnergy(), false);
        printPlayerStatus(player);
        System.out.println("Test: Tidur (Energi <= 0, tanpa bonus bed)...");
        player.changeEnergy(-Player.MAX_ENERGY - 5); // Buat energi <= 0
        player.sleep(player.getEnergy(), false);
        printPlayerStatus(player);
        // Reset energi
        player.changeEnergy(Player.MAX_ENERGY - player.getEnergy());

        System.out.println("Test: Menjual 5 Parsnip Seeds ke Bin...");
        player.sellItemToBin(itemRegistry.get("Parsnip Seeds"), 5, shippingBin);
        System.out.println("  Jumlah Parsnip Seeds di Inv: " + player.getInventory().getItemCount(itemRegistry.get("Parsnip Seeds")));
        System.out.println("Test: Mencoba menjual 20 Parsnip Seeds (Harusnya gagal)...");
        player.sellItemToBin(itemRegistry.get("Parsnip Seeds"), 20, shippingBin);

        // --- Testing Farm Logic ---
        System.out.println("\n--- Testing: Farm Logic ---");
        System.out.println("Test: Memanggil nextDay...");
        farm.nextDay(); // Akan memanggil processSales (stub), updateDailyTiles (stub), dll.
        System.out.println("  Hari Sekarang: " + gameTime.getCurrentDay());
        System.out.println("  Musim Sekarang: " + gameTime.getCurrentSeason());
        System.out.println("  Cuaca Sekarang: " + gameTime.getCurrentWeather());
        System.out.println("Test: Cek End Condition (Harusnya false)...");
        farm.checkEndConditions();
        System.out.println("Test: Set gold tinggi & cek End Condition (Harusnya true)...");
        player.addGold(20000); // Tambah gold banyak
        farm.checkEndConditions();
        player.spendGold(20000); // Kembalikan gold

        System.out.println("\n=============================================");
        System.out.println("=== Sesi Testing Selesai ===");
        System.out.println("=============================================");
    }
}
