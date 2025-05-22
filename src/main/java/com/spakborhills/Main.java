package com.spakborhills;

import com.spakborhills.model.*;
import com.spakborhills.model.Enum.*;
import com.spakborhills.model.Item.*;
import com.spakborhills.model.Map.*;
import com.spakborhills.model.NPC.*;
// import com.spakborhills.model.Store.*;
import com.spakborhills.model.Util.*;
import com.spakborhills.model.Util.GameTime;
import com.spakborhills.model.Util.ShippingBin;
import com.spakborhills.model.Store;
import com.spakborhills.model.Util.Recipe;
import com.spakborhills.model.Player;
import com.spakborhills.model.Farm;
import com.spakborhills.model.Item.Item;

import java.util.ArrayList;
import java.util.Arrays; // Untuk Arrays.asList
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set; // Untuk Set di Fish
import java.awt.Point;
// import java.awt.Dimension;

// Swing imports for GUI
import javax.swing.SwingUtilities;
import com.spakborhills.view.GameFrame; // Import for GameFrame
import com.spakborhills.model.Util.EndGameStatistics;
import com.spakborhills.model.Util.PriceList;
import com.spakborhills.model.Enum.Gender;
import com.spakborhills.controller.GameController; // Ensure this import is present

/**
 * Main class untuk testing Spakbor Hills.
 * Kelas ini berisi berbagai test case untuk menguji fungsionalitas
 * dasar dan edge case dari implementasi game Spakbor Hills.
 */
public class Main {

    // Helper untuk mencetak status Player
    private static void printPlayerStatus(Player player) {
        System.out.println("  Status Pemain: [Nama: " + player.getName() +
                           ", Energi: " + player.getEnergy() +
                           ", Gold: " + player.getGold() +
                           ", Posisi: (" + player.getCurrentTileX() + "," + player.getCurrentTileY() + ")" +
                           ", Map: " + player.getCurrentMap().getName() +
                           "]");
        System.out.println(player.getInventory()); // Cetak inventory
    }

    // Helper untuk membuat TimeRange
    private static com.spakborhills.model.Item.Fish.TimeRange createTimeRange(int start, int end) {
        return new com.spakborhills.model.Item.Fish.TimeRange(start, end);
    }

    // Helper untuk mencetak batas section
    private static void printSectionHeader(String title) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("=== " + title + " ===");
        System.out.println("=".repeat(70));
    }

    // Helper untuk mencetak sub-section
    private static void printSubsectionHeader(String title) {
        System.out.println("\n--- " + title + " ---");
    }

    // Helper untuk mencetak hasil test
    private static void printTestResult(String testName, boolean success) {
        System.out.println(testName + ": " + (success ? "PASSED" : "FAILED"));
    }

    public static void main(String[] args) {
        // printSectionHeader("MEMULAI COMPREHENSIVE TESTING SPAKBOR HILLS");

        Map<String, Item> itemRegistry = setupItemRegistry();
        if (itemRegistry == null || itemRegistry.isEmpty()) {
            System.err.println("ERROR: Gagal setup Item Registry. GUI tidak dapat dimulai.");
            return;
        }
        // System.out.println("Item Registry berhasil dibuat dengan " + itemRegistry.size() + " item.");

        FarmMap farmMap = new FarmMap();
        GameTime gameTime = new GameTime();
        ShippingBin shippingBin = new ShippingBin();
        Store store = new Store();
        WorldMap worldMap = new WorldMap("Spakbor Hills World", store);
        List<NPC> npcList = setupNPCs();
        List<Recipe> recipeList = setupRecipes();
        PriceList priceList = setupPriceList();

        Player player = new Player("Hero", Gender.MALE, "My Farm", farmMap, 5, 5, itemRegistry);
        EndGameStatistics statistics = new EndGameStatistics(new ArrayList<>(), player);

        Farm farm = new Farm(
            player.getFarmName(), player, farmMap, worldMap, store,
            npcList, recipeList, gameTime, shippingBin, statistics, priceList,
            itemRegistry
        );

        // Create GameController instance
        GameController gameController = new GameController(farm);

        // Launch the GUI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GameFrame(farm, gameController);
            }
        });

        // Comment out the rest of the test cases
        /*
        GameTime gameTime = new GameTime();
        ShippingBin shippingBin = new ShippingBin();
        Store store = new Store();
        // Initialize WorldMap with required parameters
        WorldMap worldMap = new WorldMap("Spakbor Hills World", store);

        // === SETUP NPCs & RECIPES ===
        printSubsectionHeader("Setup NPC & Recipe");
        List<NPC> npcList = setupNPCs();
        System.out.println("List NPC dibuat dengan " + npcList.size() + " NPC.");

        List<Recipe> recipeList = setupRecipes();
        System.out.println("List Recipe dibuat dengan " + recipeList.size() + " recipe.");

        // === SETUP PLAYER & FARM ===
        printSubsectionHeader("Setup Player & Farm");
        Player player = null;
        Farm farm = null;
        try {
            player = new Player("Tester", Gender.FEMALE, "Kebun Uji", farmMap, 5, 5, itemRegistry);
            System.out.println("Objek Player berhasil dibuat.");
            EndGameStatistics statistics = new EndGameStatistics(new ArrayList<>(), player);
            farm = new Farm(
                player.getFarmName(), player, farmMap, worldMap, store,
                npcList, recipeList, gameTime, shippingBin, statistics, setupPriceList(),
                itemRegistry
            );
            System.out.println("Objek Farm berhasil dibuat.");
        } catch (Exception e) {
            System.err.println("ERROR saat membuat Player atau Farm: " + e.getMessage());
            e.printStackTrace();
            return; // Hentikan jika gagal
        }

        printSectionHeader("MEMULAI TEST CASES");

        // === BASIC TESTS ===
        testBasicPlayerFunctions(player, itemRegistry);
        
        // === INVENTORY TESTS ===
        testInventoryFunctions(player, itemRegistry);
        
        // === MOVEMENT TESTS ===
        testMovement(player, farmMap);

        // === FARMING TESTS ===
        testFarming(player, farmMap, gameTime, itemRegistry);

        // === NPC INTERACTION TESTS ===
        testNPCInteractions(player, farm, itemRegistry);

        // === TIME AND WEATHER TESTS ===
        testTimeAndWeather(gameTime, farm, player);

        // === SHOP AND ECONOMY TESTS ===
        testShopAndEconomy(player, store, shippingBin, setupPriceList(), itemRegistry, gameTime);

        // === FISHING TESTS ===
        testFishing(player, farm, itemRegistry);

        // === COOKING TESTS ===
        testCooking(player, farm, itemRegistry);

        // === EDGE CASES AND ERROR HANDLING ===
        testEdgeCases(player, farm, itemRegistry);

        // === END GAME CONDITIONS ===
        testEndGameConditions(player, farm);

        printSectionHeader("COMPREHENSIVE TESTING SELESAI");
        */
    }

    /**
     * Setup Item Registry dengan berbagai jenis item
     */
    private static Map<String, Item> setupItemRegistry() {
        Map<String, Item> registry = new HashMap<>();
        try {
            // === SEEDS ===
            // Spring Seeds
            Seed parsnipSeeds = new Seed("Parsnip Seeds", 20, 1, Season.SPRING, "Parsnip", 1);
            Seed cauliflowerSeeds = new Seed("Cauliflower Seeds", 80, 5, Season.SPRING, "Cauliflower", 1);
            Seed potatoSeeds = new Seed("Potato Seeds", 50, 3, Season.SPRING, "Potato", 1);
            Seed wheatSeeds = new Seed("Wheat Seeds", 60, 1, Season.SPRING, "Wheat", 3);
            
            // Summer Seeds
            Seed blueberrySeeds = new Seed("Blueberry Seeds", 80, 7, Season.SUMMER, "Blueberry", 3);
            Seed tomatoSeeds = new Seed("Tomato Seeds", 50, 3, Season.SUMMER, "Tomato", 1);
            Seed hotPepperSeeds = new Seed("Hot Pepper Seeds", 40, 1, Season.SUMMER, "Hot Pepper", 1);
            Seed melonSeeds = new Seed("Melon Seeds", 80, 4, Season.SUMMER, "Melon", 1);
            
            // Fall Seeds
            Seed cranberrySeeds = new Seed("Cranberry Seeds", 100, 2, Season.FALL, "Cranberry", 10);
            Seed pumpkinSeeds = new Seed("Pumpkin Seeds", 150, 7, Season.FALL, "Pumpkin", 1);
            Seed fallWheatSeeds = new Seed("Fall Wheat Seeds", 60, 1, Season.FALL, "Wheat", 3);
            Seed grapeSeeds = new Seed("Grape Seeds", 60, 3, Season.FALL, "Grape", 20);
            
            // === CROPS ===
            Crop parsnip = new Crop("Parsnip", 50, 35);
            Crop cauliflower = new Crop("Cauliflower", 200, 150);
            Crop potato = new Crop("Potato", 0, 80);
            Crop wheat = new Crop("Wheat", 50, 30);
            Crop blueberry = new Crop("Blueberry", 150, 40);
            Crop tomato = new Crop("Tomato", 90, 60);
            Crop hotPepper = new Crop("Hot Pepper", 0, 40);
            Crop melon = new Crop("Melon", 0, 250);
            Crop cranberry = new Crop("Cranberry", 0, 25);
            Crop pumpkin = new Crop("Pumpkin", 300, 250);
            Crop grape = new Crop("Grape", 100, 10);
            
            // === EQUIPMENT ===
            Equipment hoe = new Equipment("Hoe", "Hoe");
            Equipment wateringCan = new Equipment("Watering Can", "WateringCan");
            Equipment pickaxe = new Equipment("Pickaxe", "Pickaxe");
            Equipment fishingRod = new Equipment("Fishing Rod", "Fishing Rod");
            
            // === MISC ITEMS ===
            MiscItem coal = new MiscItem("Coal", 20, 10);
            MiscItem firewood = new MiscItem("Firewood", 10, 5);
            
            // === FOOD ===
            Food fishNChips = new Food("Fish n' Chips", 50, 150, 135);
            Food baguette = new Food("Baguette", 25, 100, 80);
            Food sashimi = new Food("Sashimi", 70, 300, 275);
            Food fugu = new Food("Fugu", 50, 0, 135);
            Food wine = new Food("Wine", 20, 100, 90);
            Food pumpkinPie = new Food("Pumpkin Pie", 35, 120, 100);
            Food veggieSoup = new Food("Veggie Soup", 40, 140, 120);
            Food fishStew = new Food("Fish Stew", 70, 280, 260);
            Food spakborSalad = new Food("Spakbor Salad", 70, 0, 250);
            Food fishSandwich = new Food("Fish Sandwich", 50, 200, 180);
            Food legendsOfSpakbor = new Food("The Legends of Spakbor", 100, 0, 2000);
            Food cookedPigsHead = new Food("Cooked Pig's Head", 100, 1000, 0);
            
            // === SPECIAL ITEMS ===
            ProposalRing proposalRing = new ProposalRing();
            
            // === FISH ===
            // Common Fish
            List<Fish.TimeRange> anyTime = List.of(createTimeRange(0, 23));
            Fish bullhead = new Fish("Bullhead", FishRarity.COMMON, Set.of(Season.ANY), anyTime, Set.of(Weather.ANY), Set.of(LocationType.MOUNTAIN_LAKE));
            Fish carp = new Fish("Carp", FishRarity.COMMON, Set.of(Season.ANY), anyTime, Set.of(Weather.ANY), Set.of(LocationType.MOUNTAIN_LAKE, LocationType.POND));
            Fish chub = new Fish("Chub", FishRarity.COMMON, Set.of(Season.ANY), anyTime, Set.of(Weather.ANY), Set.of(LocationType.FOREST_RIVER, LocationType.MOUNTAIN_LAKE));
            
            // Regular Fish
            List<Fish.TimeRange> dayTime = List.of(createTimeRange(6, 18));
            List<Fish.TimeRange> eveningTime = List.of(createTimeRange(20, 2));
            List<Fish.TimeRange> halibutTimes = Arrays.asList(createTimeRange(6, 11), createTimeRange(19, 2));
            List<Fish.TimeRange> catfishTime = List.of(createTimeRange(6, 22)); // For Catfish 06:00 - 22:00
            List<Fish.TimeRange> flounderOctopusTime = List.of(createTimeRange(6, 22)); // For Flounder & Octopus
            List<Fish.TimeRange> pufferfishTime = List.of(createTimeRange(0, 16)); // For Pufferfish
            List<Fish.TimeRange> superCucumberTime = List.of(createTimeRange(18, 2)); // For Super Cucumber
            
            Fish largemouthBass = new Fish("Largemouth Bass", FishRarity.REGULAR, Set.of(Season.ANY), dayTime, Set.of(Weather.ANY), Set.of(LocationType.MOUNTAIN_LAKE));
            Fish rainbowTrout = new Fish("Rainbow Trout", FishRarity.REGULAR, Set.of(Season.SUMMER), dayTime, Set.of(Weather.SUNNY), Set.of(LocationType.FOREST_RIVER, LocationType.MOUNTAIN_LAKE));
            Fish sturgeon = new Fish("Sturgeon", FishRarity.REGULAR, Set.of(Season.SUMMER, Season.WINTER), dayTime, Set.of(Weather.ANY), Set.of(LocationType.MOUNTAIN_LAKE));
            Fish midnightCarp = new Fish("Midnight Carp", FishRarity.REGULAR, Set.of(Season.WINTER, Season.FALL), eveningTime, Set.of(Weather.ANY), Set.of(LocationType.MOUNTAIN_LAKE, LocationType.POND));
            Fish halibut = new Fish("Halibut", FishRarity.REGULAR, Set.of(Season.ANY), halibutTimes, Set.of(Weather.ANY), Set.of(LocationType.OCEAN));
            Fish salmon = new Fish("Salmon", FishRarity.REGULAR, Set.of(Season.FALL), dayTime, Set.of(Weather.ANY), Set.of(LocationType.FOREST_RIVER));
            Fish catfish = new Fish("Catfish", FishRarity.REGULAR, Set.of(Season.SPRING, Season.SUMMER, Season.FALL), catfishTime, Set.of(Weather.RAINY), Set.of(LocationType.FOREST_RIVER, LocationType.POND));
            // Added missing fish
            Fish flounder = new Fish("Flounder", FishRarity.REGULAR, Set.of(Season.SPRING, Season.SUMMER), flounderOctopusTime, Set.of(Weather.ANY), Set.of(LocationType.OCEAN));
            Fish octopus = new Fish("Octopus", FishRarity.REGULAR, Set.of(Season.SUMMER), flounderOctopusTime, Set.of(Weather.ANY), Set.of(LocationType.OCEAN));
            Fish pufferfish = new Fish("Pufferfish", FishRarity.REGULAR, Set.of(Season.SUMMER), pufferfishTime, Set.of(Weather.SUNNY), Set.of(LocationType.OCEAN));
            Fish sardine = new Fish("Sardine", FishRarity.REGULAR, Set.of(Season.ANY), dayTime, Set.of(Weather.ANY), Set.of(LocationType.OCEAN)); // Uses dayTime (06:00-18:00)
            Fish superCucumber = new Fish("Super Cucumber", FishRarity.REGULAR, Set.of(Season.SUMMER, Season.FALL, Season.WINTER), superCucumberTime, Set.of(Weather.ANY), Set.of(LocationType.OCEAN));
            
            // Legendary Fish
            List<Fish.TimeRange> legendaryTime = List.of(createTimeRange(8, 20));
            Fish angler = new Fish("Angler", FishRarity.LEGENDARY, Set.of(Season.FALL), legendaryTime, Set.of(Weather.ANY), Set.of(LocationType.POND));
            Fish crimsonfish = new Fish("Crimsonfish", FishRarity.LEGENDARY, Set.of(Season.SUMMER), legendaryTime, Set.of(Weather.ANY), Set.of(LocationType.OCEAN));
            Fish glacierfish = new Fish("Glacierfish", FishRarity.LEGENDARY, Set.of(Season.WINTER), legendaryTime, Set.of(Weather.ANY), Set.of(LocationType.FOREST_RIVER));
            Fish legend = new Fish("Legend", FishRarity.LEGENDARY, Set.of(Season.SPRING), legendaryTime, Set.of(Weather.RAINY), Set.of(LocationType.MOUNTAIN_LAKE));
            
            // Tambahkan semua item ke registry
            // Seeds
            registry.put(parsnipSeeds.getName(), parsnipSeeds);
            registry.put(cauliflowerSeeds.getName(), cauliflowerSeeds);
            registry.put(potatoSeeds.getName(), potatoSeeds);
            registry.put(wheatSeeds.getName(), wheatSeeds);
            registry.put(blueberrySeeds.getName(), blueberrySeeds);
            registry.put(tomatoSeeds.getName(), tomatoSeeds);
            registry.put(hotPepperSeeds.getName(), hotPepperSeeds);
            registry.put(melonSeeds.getName(), melonSeeds);
            registry.put(cranberrySeeds.getName(), cranberrySeeds);
            registry.put(pumpkinSeeds.getName(), pumpkinSeeds);
            registry.put(fallWheatSeeds.getName(), fallWheatSeeds);
            registry.put(grapeSeeds.getName(), grapeSeeds);
            
            // Crops
            registry.put(parsnip.getName(), parsnip);
            registry.put(cauliflower.getName(), cauliflower);
            registry.put(potato.getName(), potato);
            registry.put(wheat.getName(), wheat);
            registry.put(blueberry.getName(), blueberry);
            registry.put(tomato.getName(), tomato);
            registry.put(hotPepper.getName(), hotPepper);
            registry.put(melon.getName(), melon);
            registry.put(cranberry.getName(), cranberry);
            registry.put(pumpkin.getName(), pumpkin);
            registry.put(grape.getName(), grape);
            
            // Equipment
            registry.put(hoe.getName(), hoe);
            registry.put(wateringCan.getName(), wateringCan);
            registry.put(pickaxe.getName(), pickaxe);
            registry.put(fishingRod.getName(), fishingRod);
            
            // Misc
            registry.put(coal.getName(), coal);
            registry.put(firewood.getName(), firewood);
            
            // Food
            registry.put(fishNChips.getName(), fishNChips);
            registry.put(baguette.getName(), baguette);
            registry.put(sashimi.getName(), sashimi);
            registry.put(fugu.getName(), fugu);
            registry.put(wine.getName(), wine);
            registry.put(pumpkinPie.getName(), pumpkinPie);
            registry.put(veggieSoup.getName(), veggieSoup);
            registry.put(fishStew.getName(), fishStew);
            registry.put(spakborSalad.getName(), spakborSalad);
            registry.put(fishSandwich.getName(), fishSandwich);
            registry.put(legendsOfSpakbor.getName(), legendsOfSpakbor);
            registry.put(cookedPigsHead.getName(), cookedPigsHead);
            
            // Special
            registry.put(proposalRing.getName(), proposalRing);
            
            // Fish
            registry.put(bullhead.getName(), bullhead);
            registry.put(carp.getName(), carp);
            registry.put(chub.getName(), chub);
            registry.put(largemouthBass.getName(), largemouthBass);
            registry.put(rainbowTrout.getName(), rainbowTrout);
            registry.put(sturgeon.getName(), sturgeon);
            registry.put(midnightCarp.getName(), midnightCarp);
            registry.put(halibut.getName(), halibut);
            registry.put(salmon.getName(), salmon);
            registry.put(catfish.getName(), catfish);
            registry.put(angler.getName(), angler);
            registry.put(crimsonfish.getName(), crimsonfish);
            registry.put(glacierfish.getName(), glacierfish);
            registry.put(legend.getName(), legend);
            registry.put(flounder.getName(), flounder);
            registry.put(octopus.getName(), octopus);
            registry.put(pufferfish.getName(), pufferfish);
            registry.put(sardine.getName(), sardine);
            registry.put(superCucumber.getName(), superCucumber);
            
            return registry;
        } catch (Exception e) {
            System.err.println("ERROR saat membuat Item Registry: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Setup PriceList dengan harga default
     */
    private static PriceList setupPriceList() {
        PriceList priceList = new PriceList();
        // Seharusnya sudah memiliki harga default, bisa tambahkan item custom di sini
        priceList.initializeDefaultPrices();
        return priceList;
    }

    /**
     * Setup NPC List
     */
    private static List<NPC> setupNPCs() {
        List<NPC> npcList = new ArrayList<>();
        
        // Buat NPCs sesuai spesifikasi
        NPC mayorTadi = new MayorTadi();
        NPC caroline = new Caroline();
        NPC perry = new Perry();
        NPC dasco = new Dasco();
        NPC emily = new Emily();
        NPC abigail = new Abigail();
        
        npcList.add(mayorTadi);
        npcList.add(caroline);
        npcList.add(perry);
        npcList.add(dasco);
        npcList.add(emily);
        npcList.add(abigail);
        
        return npcList;
    }

    /**
     * Setup Recipe List
     */
    private static List<Recipe> setupRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        
        // Buat maps untuk ingredientReferences
        Map<String, Integer> fishNChipsIngredients = new HashMap<>();
        fishNChipsIngredients.put("Fish", 2);
        fishNChipsIngredients.put("Wheat", 1);
        fishNChipsIngredients.put("Potato", 1);
        
        Map<String, Integer> baguetteIngredients = new HashMap<>();
        baguetteIngredients.put("Wheat", 3);
        
        Map<String, Integer> sashimiIngredients = new HashMap<>();
        sashimiIngredients.put("Salmon", 3);
        
        Map<String, Integer> fuguIngredients = new HashMap<>();
        fuguIngredients.put("Pufferfish", 1);
        
        Map<String, Integer> wineIngredients = new HashMap<>();
        wineIngredients.put("Grape", 2);
        
        // Buat recipe objects
        Recipe fishNChips = new Recipe("Fish n' Chips", fishNChipsIngredients, "Fish n' Chips", "Coal", "default");
        Recipe baguette = new Recipe("Baguette", baguetteIngredients, "Baguette", "Coal", "default");
        Recipe sashimi = new Recipe("Sashimi", sashimiIngredients, "Sashimi", "Coal", "fish_10");
        Recipe fugu = new Recipe("Fugu", fuguIngredients, "Fugu", "Coal", "pufferfish");
        Recipe wine = new Recipe("Wine", wineIngredients, "Wine", "Coal", "default");
        
        // Tambahkan recipes ke list
        recipes.add(fishNChips);
        recipes.add(baguette);
        recipes.add(sashimi);
        recipes.add(fugu);
        recipes.add(wine);
        
        return recipes;
    }

    /**
     * Test basic player functions seperti gold, energy, dsb.
     */
    private static void testBasicPlayerFunctions(Player player, Map<String, Item> itemRegistry) {
        printSubsectionHeader("Testing Player Basic Functions");
        
        // Simpan nilai awal
        int initialEnergy = player.getEnergy();
        int initialGold = player.getGold();
        
        System.out.println("Status awal Player:");
        printPlayerStatus(player);
        
        // Testing gold operations
        System.out.println("\nTest: Operasi Gold");
        System.out.println("Menambah 1000 gold...");
        player.addGold(1000);
        printTestResult("Add Gold", player.getGold() == initialGold + 1000);
        
        System.out.println("Spend 500 gold (should succeed)...");
        boolean spendResult1 = player.spendGold(500);
        printTestResult("Spend Valid Gold Amount", spendResult1 && player.getGold() == initialGold + 500);
        
        System.out.println("Spend 2000 gold (should fail due to insufficient funds)...");
        boolean spendResult2 = player.spendGold(2000);
        printTestResult("Spend Invalid Gold Amount", !spendResult2 && player.getGold() == initialGold + 500);
        
        System.out.println("Spend -100 gold (should fail due to negative amount)...");
        boolean spendResult3 = player.spendGold(-100);
        printTestResult("Spend Negative Gold Amount", !spendResult3 && player.getGold() == initialGold + 500);
        
        // Reset gold
        player.spendGold(500);
        
        // Testing energy operations
        System.out.println("\nTest: Operasi Energy");
        System.out.println("Mengurangi 30 energy...");
        player.changeEnergy(-30);
        printTestResult("Reduce Energy", player.getEnergy() == initialEnergy - 30);
        
        System.out.println("Menambah energy melebihi MAX...");
        player.changeEnergy(200);
        printTestResult("Add Energy Beyond MAX", player.getEnergy() == Player.MAX_ENERGY);
        
        System.out.println("Mengurangi energy ke bawah MIN...");
        player.changeEnergy(-Player.MAX_ENERGY - 50);
        printTestResult("Reduce Energy Below MIN", player.getEnergy() == Player.MIN_ENERGY);
        
        // Reset energy
        player.changeEnergy(initialEnergy - player.getEnergy());
        
        System.out.println("\nStatus Player setelah testing:");
        printPlayerStatus(player);
    }
    
    /**
     * Test inventory functions
     */
    private static void testInventoryFunctions(Player player, Map<String, Item> itemRegistry) {
        printSubsectionHeader("Testing Inventory Functions");
        
        Inventory inventory = player.getInventory();
        
        // Tools yang seharusnya sudah ada
        System.out.println("Testing: Cek tools awal");
        printTestResult("Has Hoe", inventory.hasTool("Hoe"));
        printTestResult("Has Watering Can", inventory.hasTool("Watering Can"));
        printTestResult("Has Pickaxe", inventory.hasTool("Pickaxe"));
        printTestResult("Has Fishing Rod", inventory.hasTool("Fishing Rod"));
        
        // Testing add item
        System.out.println("\nTesting: Add items");
        Item coal = itemRegistry.get("Coal");
        System.out.println("Menambah 5 Coal...");
        inventory.addItem(coal, 5);
        printTestResult("Add Coal", inventory.getItemCount(coal) == 5);
        
        Item parsnip = itemRegistry.get("Parsnip");
        System.out.println("Menambah 10 Parsnip...");
        inventory.addItem(parsnip, 10);
        printTestResult("Add Parsnip", inventory.getItemCount(parsnip) == 10);
        
        // Testing removing items
        System.out.println("\nTesting: Remove items");
        System.out.println("Menghapus 3 Coal...");
        boolean removeResult1 = inventory.removeItem(coal, 3);
        printTestResult("Remove Valid Coal Amount", removeResult1 && inventory.getItemCount(coal) == 2);
        
        System.out.println("Menghapus 15 Parsnip (melebihi jumlah)...");
        boolean removeResult2 = inventory.removeItem(parsnip, 15);
        printTestResult("Remove Invalid Parsnip Amount", !removeResult2 && inventory.getItemCount(parsnip) == 10);
        
        // Testing has item
        System.out.println("\nTesting: Has item functions");
        printTestResult("Has 2 Coal", inventory.hasItem(coal, 2));
        printTestResult("Has 5 Coal (should fail)", !inventory.hasItem(coal, 5));
        
        // Testing add item with 0 or negative quantity
        System.out.println("\nTesting: Edge cases - bad quantities");
        System.out.println("Menambah 0 Coal...");
        inventory.addItem(coal, 0);
        printTestResult("Add Zero Quantity", inventory.getItemCount(coal) == 2);
        
        System.out.println("Menambah -5 Coal...");
        int coalCountBeforeNegativeAdd = inventory.getItemCount(coal);
        inventory.addItem(coal, -5);
        printTestResult("Add Negative Quantity", inventory.getItemCount(coal) == coalCountBeforeNegativeAdd);
        
        // Test bulk operations
        System.out.println("\nTesting: Bulk operations");
        Item blueberry = itemRegistry.get("Blueberry");
        System.out.println("Menambah 100 Blueberry...");
        inventory.addItem(blueberry, 100);
        printTestResult("Add Bulk Items", inventory.getItemCount(blueberry) == 100);
        
        System.out.println("Menghapus 50 Blueberry...");
        boolean bulkRemoveResult = inventory.removeItem(blueberry, 50);
        printTestResult("Remove Bulk Items", bulkRemoveResult && inventory.getItemCount(blueberry) == 50);
        
        // Bersihkan inventory untuk test selanjutnya
        inventory.removeItem(coal, inventory.getItemCount(coal));
        inventory.removeItem(parsnip, inventory.getItemCount(parsnip));
        inventory.removeItem(blueberry, inventory.getItemCount(blueberry));
        
        System.out.println("\nInventory setelah cleaning:");
        System.out.println(inventory);
    }
    
    /**
     * Test movement functions
     */
    private static void testMovement(Player player, FarmMap farmMap) {
        printSubsectionHeader("Testing Movement");
        
        // Cek posisi awal
        Point startPos = player.getPosition();
        System.out.println("Posisi awal: " + startPos);
        
        // Test regular movement
        System.out.println("\nTesting: Regular movement");
        System.out.println("Bergerak ke Selatan...");
        boolean moveResult1 = player.move(Direction.SOUTH);
        printTestResult("Move South", moveResult1 && player.getCurrentTileY() == startPos.y + 1);
        
        System.out.println("Bergerak ke Timur...");
        boolean moveResult2 = player.move(Direction.EAST);
        printTestResult("Move East", moveResult2 && player.getCurrentTileX() == startPos.x + 1);
        
        System.out.println("Bergerak ke Utara...");
        boolean moveResult3 = player.move(Direction.NORTH);
        printTestResult("Move North", moveResult3 && player.getCurrentTileY() == startPos.y);
        
        System.out.println("Bergerak ke Barat...");
        boolean moveResult4 = player.move(Direction.WEST);
        printTestResult("Move West", moveResult4 && player.getCurrentTileX() == startPos.x);
        
        // Test boundary movement
        System.out.println("\nTesting: Boundary movement");
        System.out.println("Bergerak ke Utara hingga batas map...");
        boolean hitBoundary = false;
        int moveCount = 0;
        while (!hitBoundary && moveCount < 100) {
            boolean success = player.move(Direction.NORTH);
            moveCount++;
            if (!success) {
                hitBoundary = true;
            }
        }
        printTestResult("Hit North Boundary", hitBoundary);
        System.out.println("Mencapai batas setelah " + moveCount + " langkah");
        
        // Test collision with objects
        System.out.println("\nTesting: Collision detection");
        // Cari deployed object terdekat dan coba bergerak ke sana
        // Ini tergantung implementasi FarmMap, jadi kita gunakan pendekatan umum
        
        // Reset position
        player.setPosition(startPos.x, startPos.y);
        System.out.println("Posisi reset ke: " + player.getPosition());
    }
    
    /**
     * Test farming functions
     */
    private static void testFarming(Player player, FarmMap farmMap, GameTime gameTime, Map<String, Item> itemRegistry) {
        printSubsectionHeader("Testing Farming Actions");
        
        Tile farmTestTile = null;
        int testTileX = -1, testTileY = -1;
        // Find a TILLABLE tile for testing
        for (int r = 0; r < farmMap.getSize().height && farmTestTile == null; r++) {
            for (int c = 0; c < farmMap.getSize().width && farmTestTile == null; c++) {
                Tile current = farmMap.getTile(c, r);
                if (current != null && current.getType() == TileType.TILLABLE) {
                    farmTestTile = current;
                    testTileX = c;
                    testTileY = r;
                    System.out.println("Using TILLABLE tile for farming test at: (" + c + "," + r + ")");
                    break;
                }
            }
        }

        if (farmTestTile == null) {
            System.out.println("WARNING: Could not find any TILLABLE tile for farming test! Skipping farming tests.");
            return;
        }
        
        System.out.println("Tipe Tile (" + testTileX + "," + testTileY + ") awal: " + farmTestTile.getType());
        
        // Test tilling
        System.out.println("\nTesting: Tilling");
        // Ensure player has Hoe for tilling (already checked by Player.till, but good for clarity)
        if (!player.getInventory().hasTool("Hoe")) player.getInventory().addItem(itemRegistry.get("Hoe"),1);
        boolean tillResult = player.till(farmTestTile);
        printTestResult("Till Land", tillResult && farmTestTile.getType() == TileType.TILLED);
        
        // Test tilling yang sudah tilled
        boolean tillAgainResult = player.till(farmTestTile);
        printTestResult("Till Already Tilled Land", !tillAgainResult);
        
        // Test watering
        System.out.println("\nTesting: Watering");
        if (!player.getInventory().hasTool("Watering Can")) player.getInventory().addItem(itemRegistry.get("Watering Can"),1);
        boolean waterResult = player.water(farmTestTile, Weather.SUNNY); // Use SUNNY for predictable test
        printTestResult("Water Tilled Land", waterResult && farmTestTile.isWatered());
        
        // Test watering yang sudah disiram
        boolean waterAgainResult = player.water(farmTestTile, Weather.SUNNY);
        printTestResult("Water Already Watered Land", !waterAgainResult);
        
        // Test planting
        System.out.println("\nTesting: Planting");
        Seed parsnipSeeds = (Seed) itemRegistry.get("Parsnip Seeds");
        if (!player.getInventory().hasItem(parsnipSeeds,1)) player.getInventory().addItem(parsnipSeeds, 5);
        
        boolean plantResult = player.plant(parsnipSeeds, farmTestTile, gameTime);
        printTestResult("Plant Seeds", plantResult && farmTestTile.getType() == TileType.PLANTED && farmTestTile.getPlantedSeed() != null);
        
        // Test planting pada tanah yang sudah ditanam
        Seed cauliflowerSeeds = (Seed) itemRegistry.get("Cauliflower Seeds");
        if (!player.getInventory().hasItem(cauliflowerSeeds,1)) player.getInventory().addItem(cauliflowerSeeds, 5);
        boolean plantAgainResult = player.plant(cauliflowerSeeds, farmTestTile, gameTime);
        printTestResult("Plant on Already Planted Land", !plantAgainResult);
        
        // Test harvest (belum waktunya)
        System.out.println("\nTesting: Harvesting (Not ready yet)");
        boolean harvestNotReadyResult = player.harvest(farmTestTile, itemRegistry);
        printTestResult("Harvest Not Ready Crop", !harvestNotReadyResult);
        
        // Simulasi growth hingga panen
        System.out.println("\nSimulating growth...");
        int daysToGrow = parsnipSeeds.getDaysToHarvest();
        for (int i = 0; i < daysToGrow; i++) {
            // Ensure tile is considered watered for growth simulation if weather isn't RAINY
            // or rely on player.water() which already happened once.
            // For robust test, explicitly mark as watered if needed for growth mechanics in Tile.updateDaily
            if (gameTime.getCurrentWeather() != Weather.RAINY) { // Or just always mark for test consistency
                 farmTestTile.markAsWatered(); // Ensure it's watered before daily update if not raining
            }
            farmTestTile.updateDaily(gameTime.getCurrentWeather(), gameTime.getCurrentSeason());
            System.out.println("  Day " + (i+1) + ": Growth days = " + farmTestTile.getGrowthDays() + ", Watered: " + farmTestTile.isWatered());
        }
        
        // Test harvest (sudah waktunya)
        System.out.println("\nTesting: Harvesting (Ready)");
        // Tile might need to be watered on the harvest day too, depending on isHarvestable logic vs updateDaily logic
        // If harvestable relies on current growthDays only, prior watering is enough.
        boolean harvestReadyResult = player.harvest(farmTestTile, itemRegistry);
        printTestResult("Harvest Ready Crop", harvestReadyResult && farmTestTile.getType() == TileType.TILLED && farmTestTile.getPlantedSeed() == null);
        if(harvestReadyResult) System.out.println("Hasil panen didapatkan: " + player.getInventory().getItemCount(itemRegistry.get("Parsnip")));
        else System.out.println("Gagal panen. Tile type: " + farmTestTile.getType() + ", Seed: " + farmTestTile.getPlantedSeed() + ", Growth: " + farmTestTile.getGrowthDays() + ", Harvestable: " + farmTestTile.isHarvestable());

        
        // Test recover land
        System.out.println("\nTesting: Recovering land");
        if (!player.getInventory().hasTool("Pickaxe")) player.getInventory().addItem(itemRegistry.get("Pickaxe"),1);
        // Ensure tile is in a state that can be recovered (e.g., TILLED or PLANTED)
        // If previous harvest failed, it might still be PLANTED. If succeeded, it's TILLED.
        if (farmTestTile.getType() != TileType.TILLED && farmTestTile.getType() != TileType.PLANTED) {
             player.till(farmTestTile); // Make it tillable if it became TILLABLE from a successful harvest then recover test
        }
        boolean recoverResult = player.recoverLand(farmTestTile);
        printTestResult("Recover Tilled Land", recoverResult && farmTestTile.getType() == TileType.TILLABLE);
        
        // Test season mismatch
        System.out.println("\nTesting: Planting in wrong season");
        player.till(farmTestTile); // Till a fresh TILLABLE tile
        player.water(farmTestTile, Weather.SUNNY); // Water it
        
        Season originalSeason = gameTime.getCurrentSeason();
        try {
            java.lang.reflect.Field seasonField = GameTime.class.getDeclaredField("currentSeason");
            seasonField.setAccessible(true);
            seasonField.set(gameTime, Season.FALL); // Change to a season where Blueberry (summer seed) shouldn't grow
            
            Seed summerSeed = (Seed) itemRegistry.get("Blueberry Seeds");
            if (!player.getInventory().hasItem(summerSeed,1)) player.getInventory().addItem(summerSeed, 5);
            
            boolean wrongSeasonResult = player.plant(summerSeed, farmTestTile, gameTime);
            printTestResult("Plant in Wrong Season", !wrongSeasonResult); // Expect planting to fail
            
            seasonField.set(gameTime, originalSeason); // Reset season
        } catch (Exception e) {
            System.out.println("Cannot test season mismatch due to reflection limitations: " + e.getMessage());
        }
        
        player.recoverLand(farmTestTile); // Clean up tile
    }
    
    /**
     * Test NPC interactions
     */
    private static void testNPCInteractions(Player player, Farm farm, Map<String, Item> itemRegistry) {
        printSectionHeader("TESTING NPC INTERACTIONS");

        if (farm == null) {
            System.err.println("testNPCInteractions: Farm object is null. Skipping tests.");
            return;
        }
        GameTime gameTime = farm.getCurrentTime(); // Get GameTime from Farm
        if (gameTime == null) {
            System.err.println("testNPCInteractions: GameTime from Farm is null. Skipping tests.");
            return;
        }
        
        Optional<NPC> mayorOpt = farm.findNPC("Mayor Tadi");
        Optional<NPC> carolineOpt = farm.findNPC("Caroline");

        if (mayorOpt.isEmpty()) {
            printTestResult("Find Mayor Tadi", false);
            System.out.println("  ERROR: Mayor Tadi not found in Farm's NPC list.");
            return; // Cannot proceed if Mayor is not found
        }
        NPC mayor = mayorOpt.get();
        printTestResult("Find Mayor Tadi", true);

        Item coal = itemRegistry.get("Coal");
        Item legend = itemRegistry.get("Legend"); // Loved by Mayor Tadi
        Item parsnip = itemRegistry.get("Parsnip"); // Neutral for Caroline

        if (coal == null || legend == null || parsnip == null) {
            System.err.println("  ERROR: One or more test items (Coal, Legend, Parsnip) not found in registry.");
            return;
        }

        // --- Test Chat ---
        printSubsectionHeader("Chatting with Mayor Tadi");
        player.setCurrentMap(farm.getMapArea(mayor.getHomeLocation())); // Move player to Mayor's map
        player.setPosition(mayor.getCurrentTileX(), mayor.getCurrentTileY() + 1); // Position player adjacent
        
        // Corrected call to player.chat()
        MapArea mayorsHomeMap = farm.getMapArea(mayor.getHomeLocation());
        if (mayorsHomeMap == null) {
            System.err.println("  ERROR: Mayor Tadi's home map not found. Skipping chat test.");
        } else {
            boolean chatResult = player.chat(mayor, gameTime, mayorsHomeMap);
            printTestResult("Chat with Mayor (adjacent)", chatResult);
            // Add more assertions here based on expected outcomes (energy, heart points)
        }

        // --- Test Gifting Hated Item ---
        printSubsectionHeader("Gifting Hated Item (Coal) to Mayor Tadi");
        player.getInventory().addItem(coal, 1); // Ensure player has coal
        player.setCurrentMap(mayorsHomeMap); // Ensure player is on the correct map
        player.setPosition(mayor.getCurrentTileX(), mayor.getCurrentTileY() + 1); // Position player adjacent
        
        // Corrected call to player.gift()
        boolean giftHatedResult = player.gift(mayor, coal, gameTime, mayorsHomeMap);
        printTestResult("Gift Hated (Coal to Mayor)", giftHatedResult);
        // Assert heart points decreased, item removed from inventory, etc.

        // --- Test Gifting Loved Item ---
        printSubsectionHeader("Gifting Loved Item (Legend) to Mayor Tadi");
        player.getInventory().addItem(legend, 1); // Ensure player has Legend
        // Player should still be on Mayor's map and adjacent
        
        // Corrected call to player.gift()
        boolean giftLovedResult = player.gift(mayor, legend, gameTime, mayorsHomeMap);
        printTestResult("Gift Loved (Legend to Mayor)", giftLovedResult);
        // Assert heart points increased significantly, item removed.

        // --- Test Gifting Neutral Item to another NPC (Caroline) ---
        if (carolineOpt.isPresent()) {
            NPC caroline = carolineOpt.get();
            printSubsectionHeader("Gifting Neutral Item (Parsnip) to Caroline");
            player.getInventory().addItem(parsnip, 1);

            MapArea carolinesHomeMap = farm.getMapArea(caroline.getHomeLocation());
            if (carolinesHomeMap == null) {
                System.err.println("  ERROR: Caroline's home map not found. Skipping gift test.");
            } else {
                player.setCurrentMap(carolinesHomeMap);
                player.setPosition(caroline.getCurrentTileX(), caroline.getCurrentTileY() - 1); // Adjacent

                // Corrected call to player.gift()
                boolean giftNeutralResultCaroline = player.gift(caroline, parsnip, gameTime, carolinesHomeMap);
                printTestResult("Gift Neutral (Parsnip to Caroline)", giftNeutralResultCaroline);
                // Assert heart points unchanged (or changed by neutral amount if defined), item removed.
            }
        } else {
            System.out.println("  INFO: Caroline not found, skipping her specific gift test.");
        }

        // ... other interaction tests ...
        // Ensure player.setCurrentMap() is called before interactions if map changes
        // Ensure player.setPosition() is called to place player correctly for proximity checks
    }

    /**
     * Test Time dan Weather
     */
    private static void testTimeAndWeather(GameTime gameTime, Farm farm, Player player) {
        printSubsectionHeader("Testing Time and Weather");
        
        // Test initial time
        System.out.println("\nTesting: Initial time");
        System.out.println("Current time: " + gameTime.getTimeString());
        System.out.println("Current day: " + gameTime.getCurrentDay());
        System.out.println("Current season: " + gameTime.getCurrentSeason());
        System.out.println("Current weather: " + gameTime.getCurrentWeather());
        printTestResult("Initial Time Valid", gameTime.getHour() >= 0 && gameTime.getHour() < 24);
        
        // Test time advancement
        System.out.println("\nTesting: Time advancement");
        int originalHour = gameTime.getHour();
        int originalMinute = gameTime.getMinute();
        
        // Advance by 15 minutes
        System.out.println("Advancing time by 15 minutes...");
        gameTime.advance(15);
        
        // Calculate expected time
        int expectedMinute = (originalMinute + 15) % 60;
        int expectedHour = originalHour;
        if (originalMinute + 15 >= 60) {
            expectedHour = (originalHour + 1) % 24;
        }
        
        printTestResult("Time Advanced by 15 Minutes", 
            gameTime.getHour() == expectedHour && gameTime.getMinute() == expectedMinute);
        System.out.println("New time: " + gameTime.getTimeString());
        
        // Test day advancement
        System.out.println("\nTesting: Day advancement");
        int currentDay = gameTime.getCurrentDay();
        Season currentSeason = gameTime.getCurrentSeason();
        
        // Simulate a day passing
        farm.nextDay();
        
        printTestResult("Day Advanced", gameTime.getCurrentDay() == (currentDay + 1) % GameTime.DAYS_IN_SEASON);
        System.out.println("New day: " + gameTime.getCurrentDay());
        
        // If we were at the last day of the season, verify season changed
        if (currentDay == GameTime.DAYS_IN_SEASON - 1) {
            Season expectedSeason = null;
            switch (currentSeason) {
                case SPRING:
                    expectedSeason = Season.SUMMER;
                    break;
                case SUMMER:
                    expectedSeason = Season.FALL;
                    break;
                case FALL:
                    expectedSeason = Season.WINTER;
                    break;
                case WINTER:
                    expectedSeason = Season.SPRING;
                    break;
                case ANY:
                    expectedSeason = currentSeason; // No change for ANY
                    break;
            }
            printTestResult("Season Advanced", gameTime.getCurrentSeason() == expectedSeason);
        }
        
        // Test weather change
        System.out.println("\nTesting: Weather change");
        Weather initialWeather = gameTime.getCurrentWeather();
        
        // Force weather change
        try {
            java.lang.reflect.Field weatherField = GameTime.class.getDeclaredField("currentWeather");
            weatherField.setAccessible(true);
            
            Weather newWeather = (initialWeather == Weather.SUNNY) ? Weather.RAINY : Weather.SUNNY;
            weatherField.set(gameTime, newWeather);
            
            printTestResult("Weather Changed", gameTime.getCurrentWeather() == newWeather);
            System.out.println("New weather: " + gameTime.getCurrentWeather());
        } catch (Exception e) {
            System.out.println("Cannot test weather change due to reflection limitations: " + e.getMessage());
        }
        
        // Test passing out at 2AM
        System.out.println("\nTesting: Pass out at 2AM");
        try {
            java.lang.reflect.Field hourField = GameTime.class.getDeclaredField("hour");
            hourField.setAccessible(true);
            java.lang.reflect.Field minuteField = GameTime.class.getDeclaredField("minute");
            minuteField.setAccessible(true);
            
            // Set time to 1:55 AM
            hourField.set(gameTime, 1);
            minuteField.set(gameTime, 55);
            
            System.out.println("Time set to: " + gameTime.getTimeString());
            
            // Advance 10 minutes to trigger pass out
            System.out.println("Advancing 10 minutes...");
            gameTime.advance(10); // This should trigger auto-sleep behavior in a real implementation
            
            System.out.println("New time: " + gameTime.getTimeString());
            
            // Note: This won't actually make the player pass out in our test since we're not running
            // the game controller loop, but it's a good check for the game engine
        } catch (Exception e) {
            System.out.println("Cannot test pass out due to reflection limitations: " + e.getMessage());
        }
    }
    
    /**
     * Test Shop and Economy
     */
    private static void testShopAndEconomy(Player player, Store store, ShippingBin shippingBin, PriceList priceList, Map<String, Item> itemRegistry, GameTime gameTime) {
        printSubsectionHeader("Testing Shop and Economy");
        
        // Test buying from store
        System.out.println("\nTesting: Buying from store");
        
        // Make sure player has enough gold
        player.addGold(1000);
        int initialGold = player.getGold();
        
        // Get available items - menggunakan metode dengan tanda tangan yang benar
        List<Item> availableItems = store.getAvailableItemsForDisplay(itemRegistry, priceList);
        
        if (availableItems.isEmpty()) {
            System.out.println("WARNING: No items available in store. Skipping buy test.");
        } else {
            // Try to buy the first available item
            Item itemToBuy = availableItems.get(0);
            int initialCount = player.getInventory().getItemCount(itemToBuy);
            int buyPrice = priceList.getBuyPrice(itemToBuy);
            
            System.out.println("Trying to buy: " + itemToBuy.getName() + " for " + buyPrice + "g");
            boolean buyResult = store.sellToPlayer(player, itemToBuy, 1, priceList, itemRegistry);
            
            printTestResult("Buy Item from Store", 
                buyResult && 
                player.getGold() == initialGold - buyPrice && 
                player.getInventory().getItemCount(itemToBuy) == initialCount + 1);
        }
        
        // Test selling to shipping bin
        System.out.println("\nTesting: Selling to shipping bin");
        
        // Add some items to player inventory for selling
        Item parsnip = itemRegistry.get("Parsnip");
        player.getInventory().addItem(parsnip, 10);
        
        int initialBinSize = shippingBin.getItems().size();
        System.out.println("Initial bin size: " + initialBinSize);
        int initialInventoryCount = player.getInventory().getItemCount(parsnip);
        
        System.out.println("Selling 5 Parsnip to bin...");
        // Get current day from the gameTime instance
        int currentDayForSale = gameTime.getCurrentDay(); // Use passed gameTime
        boolean sellResult = player.sellItemToBin(parsnip, 5, shippingBin, currentDayForSale);
        
        printTestResult("Sell to Shipping Bin", 
            sellResult && 
            player.getInventory().getItemCount(parsnip) == initialInventoryCount - 5);
            
        // Check bin contains the sold items
        Map<Item, Integer> binItems = shippingBin.getItems();
        boolean binContainsItem = binItems.containsKey(parsnip) && binItems.get(parsnip) == 5;
        printTestResult("Shipping Bin Contains Sold Item", binContainsItem);
        
        // Test processing sales
        System.out.println("\nTesting: Processing bin sales");
        int expectedGold = 5 * priceList.getSellPrice(parsnip);
        System.out.println("Expected gold from sales: " + expectedGold);
        
        // Buat EndGameStatistics baru dengan constructor yang benar
        EndGameStatistics stats = new EndGameStatistics(new ArrayList<>(), player);
        // Gunakan tanda tangan metode yang benar (tambah currentDay dan currentSeason)
        int currentDay = 1; // Asumsi hari ke-1
        Season currentSeason = Season.SPRING; // Asumsi musim Spring
        int salesGold = shippingBin.processSales(stats, priceList, currentDay, currentSeason);
        
        printTestResult("Process Sales Returns Correct Amount", salesGold == expectedGold);
        
        // Manually clear bin for this isolated test, as Farm.nextDay() would normally do it
        shippingBin.clearBin(); 
        printTestResult("Shipping Bin Cleared After Sales", shippingBin.getItems().isEmpty());
    }
    
    /**
     * Test Fishing mechanics
     */
    private static void testFishing(Player player, Farm farm, Map<String, Item> itemRegistry) {
        printSubsectionHeader("Testing Fishing Mechanics");
        
        // Setup locations for fishing
        LocationType[] fishingLocations = {
            LocationType.POND,
            LocationType.FOREST_RIVER,
            LocationType.MOUNTAIN_LAKE,
            LocationType.OCEAN
        };
        
        // Make sure player has fishing rod
        Item fishingRod = itemRegistry.get("Fishing Rod");
        if (!player.getInventory().hasItem(fishingRod, 1)) {
            player.getInventory().addItem(fishingRod, 1);
        }
        
        // Test fishing at different locations
        System.out.println("\nTesting: Fishing at different locations");
        for (LocationType location : fishingLocations) {
            System.out.println("Fishing at " + location + "...");
            
            int initialEnergy = player.getEnergy();
            int initialInventorySize = player.getInventory().getItems().size();
            System.out.println("Initial Inventory Size: " + initialInventorySize);
            
            // Try fishing
            player.fish(location);
            
            // Energy should decrease
            printTestResult("Energy Decreased After Fishing", player.getEnergy() < initialEnergy);
            
            // Note: Can't reliably test if a fish was caught since it's RNG-based
            // and we're not implementing the full fishing minigame in this test
            System.out.println("Inventory after fishing: " + player.getInventory());
            
            // Reset player energy for next test
            player.changeEnergy(Player.MAX_ENERGY - player.getEnergy());
        }
        
        // Test fishing without a fishing rod
        System.out.println("\nTesting: Fishing without fishing rod");
        Item fishingRodItem = itemRegistry.get("Fishing Rod"); // Get the item instance for removal
        player.getInventory().removeItem(fishingRodItem, 1);
        int energyBeforeNoRodFish = player.getEnergy();
        try {
            player.fish(LocationType.POND);
            // Test passes if energy did not change, meaning fishing action was correctly prevented
            printTestResult("Fishing Without Rod (No Energy Change)", player.getEnergy() == energyBeforeNoRodFish);
        } catch (Exception e) {
            // This catch block implies an unexpected failure if player.fish() should handle no-rod state internally
            printTestResult("Fishing Without Rod Exception", false); // If an exception occurs, the test for graceful handling fails
            System.out.println("Unexpected exception: " + e.getMessage());
        }
        
        // Return fishing rod for subsequent tests if any, or for general state
        if (fishingRodItem != null) player.getInventory().addItem(fishingRodItem, 1);
    }
    
    /**
     * Test Cooking mechanics
     */
    private static void testCooking(Player player, Farm farm, Map<String, Item> itemRegistry) {
        printSubsectionHeader("Testing Cooking Mechanics");
        
        // Get recipes from farm
        List<Recipe> recipes = farm.getRecipes();
        if (recipes.isEmpty()) {
            System.out.println("WARNING: No recipes available. Skipping cooking test.");
            return;
        }
        
        // Prepare for testing - add fuel and ingredients
        Item coal = itemRegistry.get("Coal");
        player.getInventory().addItem(coal, 5);
        
        // Find a simple recipe to test
        Recipe recipeToTest = null;
        for (Recipe recipe : recipes) {
            if (recipe.getName().equals("Baguette")) {
                recipeToTest = recipe;
                break;
            }
        }
        
        if (recipeToTest == null) {
            System.out.println("WARNING: Could not find Baguette recipe. Looking for any recipe.");
            recipeToTest = recipes.get(0);
        }
        
        System.out.println("\nTesting: Cooking " + recipeToTest.getName());
        
        // Add ingredients for the recipe
        Map<String, Integer> ingredients = recipeToTest.getIngredients();
        for (Map.Entry<String, Integer> entry : ingredients.entrySet()) {
            Item ingredient = itemRegistry.get(entry.getKey());
            if (ingredient != null) {
                player.getInventory().addItem(ingredient, entry.getValue() + 1); // +1 for safety
            } else {
                System.out.println("WARNING: Could not find ingredient: " + entry.getKey());
            }
        }
        
        // Check if we have all ingredients
        boolean hasAllIngredients = true;
        for (Map.Entry<String, Integer> entry : ingredients.entrySet()) {
            Item ingredient = itemRegistry.get(entry.getKey());
            if (ingredient == null || !player.getInventory().hasItem(ingredient, entry.getValue())) {
                hasAllIngredients = false;
                System.out.println("Missing ingredient: " + entry.getKey());
            }
        }
        
        if (!hasAllIngredients) {
            System.out.println("WARNING: Missing ingredients for cooking. Skipping actual cook test.");
            return;
        }
        
        // Test cooking
        int initialEnergy = player.getEnergy();
        String resultItemName = recipeToTest.getResultItemName();
        Item resultItem = itemRegistry.get(resultItemName);
        int initialResultCount = player.getInventory().getItemCount(resultItem);
        System.out.println("Initial Result Count: " + initialResultCount);
        
        boolean cookResult = player.cook(recipeToTest, coal, itemRegistry);
        System.out.println("Cook Result: " + cookResult);
        
        // Can't fully test since cooking is a passive action, but we can check energy decreased
        printTestResult("Energy Decreased After Cooking", player.getEnergy() < initialEnergy);
        
        // Reset player energy
        player.changeEnergy(Player.MAX_ENERGY - player.getEnergy());

        // Test cook without fuel
        System.out.println("\nTesting: Cooking without fuel");
        player.getInventory().removeItem(coal, player.getInventory().getItemCount(coal));
        
        boolean cookWithoutFuelResult = player.cook(recipeToTest, coal, itemRegistry);
        printTestResult("Cook Without Fuel Fails", !cookWithoutFuelResult);
        
        // Test cook without ingredients
        System.out.println("\nTesting: Cooking without ingredients");
        player.getInventory().addItem(coal, 1);
        
        // Remove ingredients
        for (Map.Entry<String, Integer> entry : ingredients.entrySet()) {
            Item ingredient = itemRegistry.get(entry.getKey());
            if (ingredient != null) {
                player.getInventory().removeItem(ingredient, player.getInventory().getItemCount(ingredient));
            }
        }
        
        boolean cookWithoutIngredientsResult = player.cook(recipeToTest, coal, itemRegistry);
        printTestResult("Cook Without Ingredients Fails", !cookWithoutIngredientsResult);
    }
    
    /**
     * Test Edge Cases
     */
    private static void testEdgeCases(Player player, Farm farm, Map<String, Item> itemRegistry) {
        printSectionHeader("TESTING EDGE CASES AND ERROR HANDLING");
        GameTime gameTime = farm.getCurrentTime();
        NPC someNpc = farm.getNPCs().isEmpty() ? null : farm.getNPCs().get(0);
        MapArea npcMapForEdgeCase = null;
        if (someNpc != null) {
            npcMapForEdgeCase = farm.getMapArea(someNpc.getHomeLocation());
        }

        // Example for the null NPC gift test from your logs
        printSubsectionHeader("Gifting with Null NPC");
        Item validItemForNullTest = itemRegistry.get("Parsnip");
        if (validItemForNullTest != null && gameTime != null && npcMapForEdgeCase != null) { // npcMapForEdgeCase is a bit of a placeholder here
            // For a true null NPC test, the map argument might not matter if the NPC is null first
            // However, the method signature requires a MapArea.
            // If someNpc is null, npcMapForEdgeCase would also be null, and the call would fail earlier or inside player.gift.
            // The error was for player.gift(null, item), meaning npcTarget is null.
            // The method signature requires: NPC, Item, GameTime, MapArea
            // To test player.gift(null, item, gameTime, mapArea):
            // We need a valid mapArea, even if the NPC is null, to satisfy the signature.
            // Let's use player's current map if it's valid, or farmMap as a fallback.
            MapArea mapForNullNPCTest = player.getCurrentMap() != null ? player.getCurrentMap() : farm.getFarmMap();
            
            boolean giftNullNpcResult = player.gift(null, validItemForNullTest, gameTime, mapForNullNPCTest);
            printTestResult("Gift to null NPC", !giftNullNpcResult); // Expecting false (failure)

        } else {
            System.out.println("  Skipping gift to null NPC test due to missing components (item, time, or a map for context).");
        }

        // Test Gifting with Null Item
        if (someNpc != null && gameTime != null && npcMapForEdgeCase != null) {
            player.setCurrentMap(npcMapForEdgeCase); // Ensure player is on a map
            player.setPosition(someNpc.getCurrentTileX(), someNpc.getCurrentTileY() + 1); // Position player
            boolean giftNullItemResult = player.gift(someNpc, null, gameTime, npcMapForEdgeCase);
            printTestResult("Gift null Item to NPC", !giftNullItemResult); // Expecting false
        } else {
             System.out.println("  Skipping gift null item test due to missing components (NPC, time, or map).");
        }

        // Test Gifting with Null GameTime (should log error but might proceed with game logic if not critical for gift itself)
        if (someNpc != null && validItemForNullTest != null && npcMapForEdgeCase != null) {
            player.setCurrentMap(npcMapForEdgeCase);
            player.setPosition(someNpc.getCurrentTileX(), someNpc.getCurrentTileY() + 1);
            // Temporarily allow inventory to ensure the item exists for this specific test
            if (!player.getInventory().hasItem(validItemForNullTest, 1)) player.getInventory().addItem(validItemForNullTest,1);

            boolean giftNullTimeResult = player.gift(someNpc, validItemForNullTest, null, npcMapForEdgeCase);
            // The result depends on whether time advancement is critical for the gift's success logic
            // Player.gift logs an error but proceeds. So, this might be true if other conditions met.
            printTestResult("Gift with null GameTime (expect proceed with error log)", giftNullTimeResult); 
        } else {
            System.out.println("  Skipping gift with null GameTime test due to missing components.");
        }

        // Test Gifting with Null MapArea
         if (someNpc != null && validItemForNullTest != null && gameTime != null) {
            // Ensure item is in inventory for this test
            if (!player.getInventory().hasItem(validItemForNullTest, 1)) player.getInventory().addItem(validItemForNullTest,1);
            boolean giftNullMapResult = player.gift(someNpc, validItemForNullTest, gameTime, null);
            printTestResult("Gift with null NPC MapArea", !giftNullMapResult); // Expecting false
        } else {
            System.out.println("  Skipping gift with null NPC MapArea test due to missing components.");
        }

        // --- Chat Edge Cases ---
        // Test Chat with Null NPC
        if (gameTime != null) {
            MapArea mapForNullNPCChatTest = player.getCurrentMap() != null ? player.getCurrentMap() : farm.getFarmMap();
            boolean chatNullNpcResult = player.chat(null, gameTime, mapForNullNPCChatTest);
            printTestResult("Chat with null NPC", !chatNullNpcResult); // Expecting false
        } else {
            System.out.println("  Skipping chat with null NPC test due to missing GameTime.");
        }
        
        // Test Chat with Null GameTime
        if (someNpc != null && npcMapForEdgeCase != null) {
            player.setCurrentMap(npcMapForEdgeCase);
            player.setPosition(someNpc.getCurrentTileX(), someNpc.getCurrentTileY() + 1);
            boolean chatNullTimeResult = player.chat(someNpc, null, npcMapForEdgeCase);
            // Similar to gift, player.chat logs error but might return true if other conditions met.
            printTestResult("Chat with null GameTime (expect proceed with error log)", chatNullTimeResult);
        } else {
            System.out.println("  Skipping chat with null GameTime test due to missing components.");
        }

        // Test Chat with Null MapArea
        if (someNpc != null && gameTime != null) {
            boolean chatNullMapResult = player.chat(someNpc, gameTime, null);
            printTestResult("Chat with null NPC MapArea", !chatNullMapResult); // Expecting false
        } else {
             System.out.println("  Skipping chat with null NPC MapArea test due to missing components.");
        }

        // ... other edge cases
    }
    
    /**
     * Test End Game Conditions
     */
    private static void testEndGameConditions(Player player, Farm farm) {
        printSubsectionHeader("Testing End Game Conditions");
        
        // Check initial end game state
        System.out.println("\nTesting: Initial end game state");
        boolean initialEndCondition = farm.checkEndConditions();
        printTestResult("Initial End Condition False", !initialEndCondition);
        
        // Test gold end condition
        System.out.println("\nTesting: Gold end condition");
        int initialGold = player.getGold();
        player.addGold(20000); // Ensure we surpass the threshold of 17,209g
        
        boolean goldEndCondition = farm.checkEndConditions();
        printTestResult("Gold End Condition True", goldEndCondition);
        
        // Reset gold
        player.spendGold(player.getGold() - initialGold);
        
        // Test marriage end condition
        System.out.println("\nTesting: Marriage end condition");
        Optional<NPC> mayorOpt = farm.findNPC("Mayor Tadi");
        if (mayorOpt.isPresent()) {
            NPC mayor = mayorOpt.get();
            
            try {
                // Set heart points to max
                java.lang.reflect.Field heartsField = NPC.class.getDeclaredField("heartPoints");
                heartsField.setAccessible(true);
                heartsField.set(mayor, 150);
                
                // Set relationship status to SPOUSE
                java.lang.reflect.Field statusField = NPC.class.getDeclaredField("relationshipStatus");
                statusField.setAccessible(true);
                statusField.set(mayor, RelationshipStatus.SPOUSE);
                
                // Set player's partner
                player.setPartner(mayor);
                
                boolean marriageEndCondition = farm.checkEndConditions();
                printTestResult("Marriage End Condition True", marriageEndCondition);
                
                // Reset
                statusField.set(mayor, RelationshipStatus.SINGLE);
                player.setPartner(null);
            } catch (Exception e) {
                System.out.println("Cannot test marriage end condition due to reflection limitations: " + e.getMessage());
            }
        } else {
            System.out.println("WARNING: Mayor Tadi not found for testing marriage end condition");
        }
        
        // Test statistics
        System.out.println("\nTesting: Game statistics");
        EndGameStatistics stats = farm.getStatistics();
        
        // Simulate some statistics records
        stats.recordIncome(500, farm.getCurrentTime().getCurrentSeason());
        stats.recordExpenditure(300, farm.getCurrentTime().getCurrentSeason());
        stats.recordHarvest("Parsnip", 10);
        
        // End game would typically display these stats
        System.out.println("Sample statistics:");
        System.out.println("Total Income: $500");
        System.out.println("Total Expenditure: $300");
        System.out.println("Crops Harvested: 10 Parsnip");
    }
}
