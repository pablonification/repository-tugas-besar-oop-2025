package com.spakborhills;

import com.spakborhills.model.Enum.*;
import com.spakborhills.model.Item.*;
import com.spakborhills.model.NPC.*;
import com.spakborhills.model.Util.Recipe;
import com.spakborhills.model.Item.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;
import com.spakborhills.view.GameFrame; 
import com.spakborhills.model.Util.PriceList;

/**
 * Main class untuk testing Spakbor Hills.
 * Kelas ini berisi berbagai test case untuk menguji fungsionalitas
 * dasar dan edge case dari implementasi game Spakbor Hills.
 */
public class Main {
    // Helper untuk membuat TimeRange
    private static com.spakborhills.model.Item.Fish.TimeRange createTimeRange(int start, int end) {
        return new com.spakborhills.model.Item.Fish.TimeRange(start, end);
    }

    public static void main(String[] args) {
        // Main Menu Logic removed - Handled by GameFrame and MainMenuPanel
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GameFrame(); 
            }
        });
    }

    /**
     * Setup Item Registry dengan berbagai jenis item
     */
    public static Map<String, Item> setupItemRegistry() {
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
            MiscItem stone = new MiscItem("Stone", 5, 2);
            
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
            List<Fish.TimeRange> catfishTime = List.of(createTimeRange(6, 22)); 
            List<Fish.TimeRange> flounderOctopusTime = List.of(createTimeRange(6, 22)); 
            List<Fish.TimeRange> pufferfishTime = List.of(createTimeRange(0, 16)); 
            List<Fish.TimeRange> superCucumberTime = List.of(createTimeRange(18, 2)); 
            
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
            Fish sardine = new Fish("Sardine", FishRarity.REGULAR, Set.of(Season.ANY), dayTime, Set.of(Weather.ANY), Set.of(LocationType.OCEAN)); 
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
            registry.put(stone.getName(), stone);
            
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
    public static PriceList setupPriceList() {
        PriceList priceList = new PriceList();
        priceList.initializeDefaultPrices();
        return priceList;
    }

    /**
     * Setup NPC List
     */
    public static List<NPC> setupNPCs() {
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
    public static List<Recipe> setupRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        
        // Resep yang sudah ada
        Map<String, Integer> fishNChipsIngredients = new HashMap<>() {{
            put("Any Fish", 2); 
            put("Wheat", 1);
            put("Potato", 1);
        }};
        recipes.add(new Recipe("Fish n' Chips", fishNChipsIngredients, "Fish n' Chips", "Coal", "BELI_DI_STORE")); // ID recipe_1, unlock: Beli di store

        Map<String, Integer> baguetteIngredients = new HashMap<>() {{
            put("Wheat", 3);
        }};
        recipes.add(new Recipe("Baguette", baguetteIngredients, "Baguette", "Coal", "DEFAULT")); // ID recipe_2, unlock: Default

        Map<String, Integer> sashimiIngredients = new HashMap<>() {{
            put("Salmon", 3);
        }};
        recipes.add(new Recipe("Sashimi", sashimiIngredients, "Sashimi", "Coal", "FISH_10")); // ID recipe_3, unlock: Mancing 10 ikan

        Map<String, Integer> fuguIngredients = new HashMap<>() {{
            put("Pufferfish", 1);
        }};
        recipes.add(new Recipe("Fugu", fuguIngredients, "Fugu", "Coal", "PUFFERFISH")); // ID recipe_4, unlock: Memancing pufferfish (key "PUFFERFISH")

        Map<String, Integer> wineIngredients = new HashMap<>() {{
            put("Grape", 2);
        }};
        recipes.add(new Recipe("Wine", wineIngredients, "Wine", "Coal", "DEFAULT")); // ID recipe_5, unlock: Default

        // --- TAMBAHKAN RESEP BARU DI SINI ---
        Map<String, Integer> pumpkinPieIngredients = new HashMap<>() {{
            put("Egg", 1); 
            put("Wheat", 1);
            put("Pumpkin", 1);
        }};
        recipes.add(new Recipe("Pumpkin Pie", pumpkinPieIngredients, "Pumpkin Pie", "Coal", "DEFAULT")); // ID recipe_6, unlock: Default

        Map<String, Integer> veggieSoupIngredients = new HashMap<>() {{
            put("Cauliflower", 1);
            put("Parsnip", 1);
            put("Potato", 1);
            put("Tomato", 1);
        }};
        recipes.add(new Recipe("Veggie Soup", veggieSoupIngredients, "Veggie Soup", "Coal", "FIRST_HARVEST")); // ID recipe_7, unlock: Memanen pertama kali

        Map<String, Integer> fishStewIngredients = new HashMap<>() {{
            put("Any Fish", 2);
            put("Hot Pepper", 1);
            put("Cauliflower", 2);
        }};
        recipes.add(new Recipe("Fish Stew", fishStewIngredients, "Fish Stew", "Firewood", "OBTAINED_HOT_PEPPER")); // ID recipe_8, unlock: Dapatkan Hot Pepper

        Map<String, Integer> spakborSaladIngredients = new HashMap<>() {{
            put("Melon", 1);
            put("Cranberry", 1);
            put("Blueberry", 1);
            put("Tomato", 1);
        }};
        recipes.add(new Recipe("Spakbor Salad", spakborSaladIngredients, "Spakbor Salad", null, "DEFAULT")); // ID recipe_9, unlock: Default, Fuel null (jika tidak perlu)

        Map<String, Integer> fishSandwichIngredients = new HashMap<>() {{
            put("Any Fish", 1);
            put("Wheat", 2);
            put("Tomato", 1);
            put("Hot Pepper", 1);
        }};
        recipes.add(new Recipe("Fish Sandwich", fishSandwichIngredients, "Fish Sandwich", "Firewood", "BELI_DI_STORE")); // ID recipe_10, unlock: Beli di store

        Map<String, Integer> legendsOfSpakborIngredients = new HashMap<>() {{
            put("Legend", 1); 
            put("Potato", 2);
            put("Parsnip", 1);
            put("Tomato", 1);
            put("Eggplant", 1); 
        }};
        recipes.add(new Recipe("The Legends of Spakbor", legendsOfSpakborIngredients, "The Legends of Spakbor", "Coal", "FISH_LEGEND")); // ID recipe_11, unlock: Memancing Legend

        return recipes;
    }  
}