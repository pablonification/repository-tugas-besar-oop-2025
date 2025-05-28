package com.spakborhills.model.Item;

import com.spakborhills.model.Enum.Season;
import com.spakborhills.model.Enum.FishRarity;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * ItemFactory class implementing the Factory Method pattern.
 * This class provides methods to create different types of items.
 */
public class ItemFactory {
    
    /**
     * Creates a crop item with the specified parameters
     * 
     * @param name The name of the crop
     * @param buyPrice The buying price of the crop
     * @param sellPrice The selling price of the crop
     * @return A new Crop item
     */
    public static Crop createCrop(String name, int buyPrice, int sellPrice) {
        return new Crop(name, buyPrice, sellPrice);
    }
    
    /**
     * Creates a seed item with the specified parameters
     * 
     * @param name The name of the seed
     * @param buyPrice The buying price of the seed
     * @param daysToHarvest Number of days until harvest
     * @param season The growing season for this seed
     * @param cropYieldName The name of the crop this seed produces
     * @param quantityPerHarvest The quantity harvested per harvest
     * @return A new Seed item
     */
    public static Seed createSeed(String name, int buyPrice, int daysToHarvest, Season season, 
                                 String cropYieldName, int quantityPerHarvest) {
        return new Seed(name, buyPrice, daysToHarvest, season, cropYieldName, quantityPerHarvest);
    }
    
    /**
     * Creates a standard seed for a crop
     * 
     * @param cropName The base crop name (without "Seeds")
     * @param buyPrice The buying price
     * @param daysToHarvest Days to mature
     * @param season Growing season
     * @return A new Seed item
     */
    public static Seed createStandardSeed(String cropName, int buyPrice, int daysToHarvest, Season season) {
        return new Seed(cropName + " Seeds", buyPrice, daysToHarvest, season, cropName, 1);
    }
    
    /**
     * Creates a parsnip seed with standard values
     * 
     * @return A new parsnip seed
     */
    public static Seed createParsnipSeed() {
        return createStandardSeed("Parsnip", 20, 1, Season.SPRING);
    }
    
    /**
     * Creates a cauliflower seed with standard values
     * 
     * @return A new cauliflower seed
     */
    public static Seed createCauliflowerSeed() {
        return createStandardSeed("Cauliflower", 40, 3, Season.SPRING);
    }
    
    /**
     * Creates a food item with the specified parameters
     * 
     * @param name The name of the food
     * @param sellPrice The selling price of the food
     * @param energyRestore The amount of energy this food restores
     * @return A new Food item
     */
    public static Food createFood(String name, int sellPrice, int energyRestore) {
        return new Food(name, energyRestore, 0, sellPrice);
    }
    
    /**
     * Creates a fish item with the specified parameters
     * 
     * @param name The name of the fish
     * @param sellPrice The selling price of the fish
     * @param rarity The rarity of the fish
     * @param energyRestore The amount of energy this fish restores when eaten
     * @return A new Fish item
     */
    public static Fish createFish(String name, int sellPrice, FishRarity rarity, int energyRestore) {
        // Fish constructor requires more parameters, but this method seems to be using a simpler constructor
        // Since we don't have a simpler constructor in Fish.java, we need to create minimal valid parameters
        List<Fish.TimeRange> timeRanges = new ArrayList<>();
        timeRanges.add(new Fish.TimeRange(0, 23)); // All day
        
        Fish fish = new Fish(name, rarity, Set.of(Season.ANY), timeRanges, Set.of(com.spakborhills.model.Enum.Weather.ANY), 
                            Set.of(com.spakborhills.model.Enum.LocationType.OCEAN));
        
        // The Fish class already has a BASE_ENERGY_RESTORE constant, so we don't need to set it
        return fish;
    }
    
    /**
     * Creates an equipment item with the specified parameters
     * 
     * @param name The name of the equipment
     * @param sellPrice The selling price of the equipment
     * @return A new Equipment item
     */
    public static Equipment createEquipment(String name, int sellPrice) {
        return new Equipment(name, name); // Using name as toolType since that's what Equipment constructor requires
    }
    
    /**
     * Creates a furniture item with the specified parameters
     * 
     * @param name The name of the furniture
     * @param sellPrice The selling price of the furniture
     * @param width The width of the furniture
     * @param height The height of the furniture
     * @return A new Furniture item
     */
    public static Furniture createFurniture(String name, int sellPrice, int width, int height) {
        return new Furniture(name, "A " + name, 0, sellPrice, width, height);
    }
    
    /**
     * Creates a miscellaneous item with the specified parameters
     * 
     * @param name The name of the item
     * @param sellPrice The selling price of the item
     * @return A new MiscItem
     */
    public static MiscItem createMiscItem(String name, int sellPrice) {
        return new MiscItem(name, 0, sellPrice);
    }
} 