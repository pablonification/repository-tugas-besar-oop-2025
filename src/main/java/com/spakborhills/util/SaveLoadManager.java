package com.spakborhills.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spakborhills.data.SaveData;
import com.spakborhills.model.Farm;
import com.spakborhills.model.Player;
import com.spakborhills.model.Util.GameTime;
import com.spakborhills.data.InventoryData;
import com.spakborhills.data.InventoryItemData;
import com.spakborhills.data.FarmTileData;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Map.Tile;
import com.spakborhills.model.Item.Crop;
import com.spakborhills.model.Item.Seed;
import com.spakborhills.model.Map.MapArea;
import com.spakborhills.model.Enum.Season;
import com.spakborhills.model.Enum.TileType;
import com.spakborhills.model.Enum.Weather;
import com.spakborhills.model.NPC.NPC;
import com.spakborhills.model.Util.ShippingBin;
import com.spakborhills.model.Item.Furniture;
import java.awt.Point;
import com.spakborhills.model.Object.DeployedObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashSet;

public class SaveLoadManager {

    private static final String SAVE_DIRECTORY = "saves";
    private static final String SAVE_FILE_EXTENSION = ".json";
    private Gson gson;

    public SaveLoadManager() {
        // GsonBuilder untuk pretty printing agar file JSON mudah dibaca
        this.gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .create();
        
        // Create saves directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(SAVE_DIRECTORY));
        } catch (IOException e) {
            System.err.println("Error creating saves directory: " + e.getMessage());
        }
    }
    
    /**
     * Get a list of all available save files
     * @return List of save slot information with file names and metadata
     */
    public List<SaveSlot> getSaveSlots() {
        List<SaveSlot> saveSlots = new ArrayList<>();
        File saveDir = new File(SAVE_DIRECTORY);
        
        if (!saveDir.exists() || !saveDir.isDirectory()) {
            return saveSlots; // Return empty list if directory doesn't exist
        }
        
        File[] saveFiles = saveDir.listFiles((dir, name) -> name.endsWith(SAVE_FILE_EXTENSION));
        if (saveFiles != null) {
            for (File saveFile : saveFiles) {
                try {
                    // Try to load basic metadata from each save file
                    SaveData saveData = loadBasicSaveData(saveFile.getName());
                    if (saveData != null) {
                        SaveSlot slot = new SaveSlot();
                        slot.setFileName(saveFile.getName());
                        slot.setPlayerName(saveData.getPlayerName());
                        slot.setFarmName(saveData.getPlayerFarmName());
                        slot.setDay(saveData.getCurrentDay());
                        slot.setSeason(saveData.getCurrentSeason());
                        slot.setYear(saveData.getCurrentYear());
                        slot.setLastModified(new Date(saveFile.lastModified()));
                        saveSlots.add(slot);
                    }
                } catch (Exception e) {
                    System.err.println("Error reading save file " + saveFile.getName() + ": " + e.getMessage());
                }
            }
        }
        
        return saveSlots;
    }

    /**
     * Load just basic metadata from a save file without the full game state
     */
    private SaveData loadBasicSaveData(String fileName) {
        String filePath = SAVE_DIRECTORY + "/" + fileName;
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, SaveData.class);
        } catch (IOException e) {
            System.err.println("Error reading basic save data from " + fileName + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Generates a default save file name based on player and farm name
     */
    private String generateSaveFileName(String playerName, String farmName) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());
        String safeName = (playerName + "_" + farmName)
            .replaceAll("[^a-zA-Z0-9_-]", "_")  // Replace special chars with underscore
            .toLowerCase();
            
        return safeName + "_" + timestamp + SAVE_FILE_EXTENSION;
    }

    /**
     * Save the game to a specific file name
     * @param fileName The file name to save to, or null to generate a new one
     * @param farm The Farm object
     * @param player The Player object
     * @param timeService The GameTime service
     * @return The file name used for saving
     */
    public String saveGame(String fileName, Farm farm, Player player, GameTime timeService) {
        // Generate filename if not provided
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = generateSaveFileName(player.getName(), player.getFarmName());
        } else {
            // For existing files, we'll use the exact name as provided
            // This is important for overwriting existing saves
            
            // Check if this is a full path or just a filename
            if (!fileName.contains("/") && !fileName.contains("\\")) {
                // It's just a filename, so ensure it has safe characters
                String safeFileName = fileName.replaceAll("[^a-zA-Z0-9_.-]", "_");
                
                // If it doesn't have the extension, add it
                if (!safeFileName.toLowerCase().endsWith(SAVE_FILE_EXTENSION.toLowerCase())) {
                    safeFileName = safeFileName + SAVE_FILE_EXTENSION;
                }
                
                fileName = safeFileName;
            }
        }
        
        // If the file doesn't have a path, add the save directory
        String filePath;
        if (fileName.contains("/") || fileName.contains("\\")) {
            filePath = fileName;
        } else {
            filePath = SAVE_DIRECTORY + "/" + fileName;
        }
        
        // Check if file exists
        File saveFile = new File(filePath);
        if (saveFile.exists()) {
            System.out.println("Overwriting existing save file: " + fileName);
        }
        
        SaveData saveData = createSaveData(farm, player, timeService);
        
        // Ensure save directory exists
        File saveDir = new File(SAVE_DIRECTORY);
        if (!saveDir.exists()) {
            if (!saveDir.mkdirs()) {
                System.err.println("Failed to create save directory: " + SAVE_DIRECTORY);
                return null;
            }
        }
        
        // Write to JSON file
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(saveData, writer);
            System.out.println("Game saved successfully to " + filePath);
            return fileName;
        } catch (IOException e) {
            System.err.println("Error saving game: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Creates the SaveData object with all game state
     */
    private SaveData createSaveData(Farm farm, Player player, GameTime timeService) {
        SaveData saveData = new SaveData();
        MapArea farmMap = farm.getFarmMap(); // Get farmMap once

        // 1. Populate Player Data
        saveData.setPlayerName(player.getName());
        saveData.setPlayerGender(player.getGender());
        saveData.setPlayerX(player.getCurrentTileX());
        saveData.setPlayerY(player.getCurrentTileY());
        if (player.getCurrentMap() != null) {
            saveData.setCurrentMapId(player.getCurrentMap().getName());
        } else {
            System.err.println("Peringatan saat menyimpan: Player currentMap is null!");
            saveData.setCurrentMapId(farm.getFarmMap().getName()); // Fallback
        }
        saveData.setPlayerMoney(player.getGold());
        saveData.setPlayerEnergy(player.getEnergy());
        saveData.setPlayerFarmName(player.getFarmName());

        if (player.getPartner() != null) {
            saveData.setPlayerPartner(new SaveData.PartnerData(player.getPartner().getName(), player.getPartner().getRelationshipStatus()));
        } else {
            saveData.setPlayerPartner(null);
        }

        // 2. Populate Inventory Data
        InventoryData inventoryData = new InventoryData();
        if (player.getInventory() != null && player.getInventory().getItems() != null) {
            for (Map.Entry<Item, Integer> entry : player.getInventory().getItems().entrySet()) {
                Item item = entry.getKey();
                Integer quantity = entry.getValue();
                if (item != null && quantity != null && quantity > 0) {
                    inventoryData.addItem(new InventoryItemData(item.getName(), quantity)); // Assuming Item name is its ID
                }
            }
        }
        saveData.setPlayerInventory(inventoryData);
        saveData.setUnlockedRecipes(player.getUnlockedRecipes()); // Assuming Player has getUnlockedRecipes()

        // 3. Populate Time & Game World Data
        saveData.setCurrentDay(timeService.getCurrentDay());
        saveData.setCurrentHour(timeService.getHour());
        saveData.setCurrentMinute(timeService.getMinute());
        saveData.setCurrentSeason(timeService.getCurrentSeason().toString());
        saveData.setCurrentYear(timeService.getCurrentYear());
        saveData.setCurrentWeather(farm.getCurrentWeather().toString()); // Assuming Farm has getCurrentWeather()

        // 4. Populate Farm Tile Data
        Map<String, FarmTileData> farmTilesData = new HashMap<>();
        if (farmMap != null && farmMap.getTiles() != null) {
            // Log entry points for debugging
            if (farmMap instanceof com.spakborhills.model.Map.FarmMap) {
                com.spakborhills.model.Map.FarmMap concreteFarmMap = (com.spakborhills.model.Map.FarmMap) farmMap;
                List<Point> entryPoints = concreteFarmMap.getEntryPoints();
                System.out.println("Debug: Found " + entryPoints.size() + " entry points during save:");
                for (Point p : entryPoints) {
                    TileType type = concreteFarmMap.getTile(p.x, p.y).getType();
                    System.out.println("  Entry point at (" + p.x + "," + p.y + "), current TileType: " + type);
                }
            }
            
            Tile[][] tiles = farmMap.getTiles();
            for (int r = 0; r < tiles.length; r++) {
                for (int c = 0; c < tiles[r].length; c++) {
                    Tile currentTile = tiles[r][c];
                    if (currentTile != null) { // Save all tiles, not just planted ones
                        String cropId = null;
                        int growthStage = 0;
                        if (currentTile.getPlantedSeed() != null) {
                            Seed plantedSeed = currentTile.getPlantedSeed();
                            cropId = plantedSeed.getCropYieldName(); // Or seed.getName() if that's the ID
                            growthStage = currentTile.getGrowthDays();
                        }
                        FarmTileData tileData = new FarmTileData(
                                currentTile.getType().toString(),
                                cropId,
                                growthStage,
                                currentTile.isWatered(),
                                currentTile.getLastWateredDay()
                        );
                        farmTilesData.put(c + "," + r, tileData);
                        
                        // Debug log when saving TILLED tiles
                        if (currentTile.getType() == TileType.TILLED) {
                            System.out.println("DEBUG: Saving TILLED tile at (" + c + "," + r + ")");
                        }
                    }
                }
            }
        }
        saveData.setFarmTiles(farmTilesData);

        // 5. Populate Shipping Bin Contents
        List<SaveData.ShippingBinItemData> shippingBinItems = new ArrayList<>();
        ShippingBin shippingBin = farm.getShippingBin();
        if (shippingBin != null && shippingBin.getItems() != null) {
            for (Map.Entry<Item, Integer> entry : shippingBin.getItems().entrySet()) {
                shippingBinItems.add(new SaveData.ShippingBinItemData(entry.getKey().getName(), entry.getValue()));
            }
        }
        saveData.setShippingBinContents(shippingBinItems);

        // 6. Populate NPC Data
        Map<String, SaveData.NpcData> npcDataMap = new HashMap<>();
        if (farm.getNpcs() != null) { // Assuming Farm has a way to get all NPCs
            for (NPC npc : farm.getNpcs()) {
                if (npc != null) {
                    npcDataMap.put(npc.getName(), new SaveData.NpcData(npc.getHeartPoints(), npc.getRelationshipStatus()));
                }
            }
        }
        saveData.setNpcDataMap(npcDataMap);

        // 7. Populate Milestones
        saveData.setMilestonesAchieved(farm.getAchievedMilestones()); // Assuming Farm has getAchievedMilestones()

        // 8. Populate Bonus Data (Example for Furniture)
        SaveData.BonusData bonusData = new SaveData.BonusData();
        List<SaveData.FurnitureData> furnitureList = new ArrayList<>();
        if (farm.getHouse() != null && farm.getHouse().getFurnitures() != null) {
             for (Furniture furniture : farm.getHouse().getFurnitures()) {
                 furnitureList.add(new SaveData.FurnitureData(furniture.getName(), furniture.getX(), furniture.getY()));
             }
        }
        bonusData.setHouseFurniture(furnitureList);
        saveData.setBonusData(bonusData);

        // 9. Populate Farm Deployed Objects
        List<SaveData.PlacedObjectData> farmObjectsData = new ArrayList<>();
        // Ensure farmMap is an instance of FarmMap before casting or calling specific methods
        if (farmMap instanceof com.spakborhills.model.Map.FarmMap) {
            com.spakborhills.model.Map.FarmMap concreteFarmMap = (com.spakborhills.model.Map.FarmMap) farmMap;
            if (concreteFarmMap.getDeployedObjectsMap() != null) { 
                for (Map.Entry<Point, DeployedObject> entry : concreteFarmMap.getDeployedObjectsMap().entrySet()) {
                    Point anchor = entry.getKey();
                    DeployedObject deployedObject = entry.getValue();
                    if (deployedObject != null) {
                        farmObjectsData.add(new SaveData.PlacedObjectData(
                            deployedObject.getName(),
                            deployedObject.getClass().getName(), // Get the full class name for reflection
                            anchor.x,
                            anchor.y
                        ));
                    }
                }
            }
        }
        saveData.setFarmDeployedObjects(farmObjectsData);

        // 10. Populate Statistics Data
        if (farm.getStatistics() != null) {
            com.spakborhills.model.Util.EndGameStatistics statistics = farm.getStatistics();
            SaveData.StatisticsData statsData = new SaveData.StatisticsData();
            
            // Basic statistics
            statsData.setTotalIncome(statistics.getTotalIncome());
            statsData.setTotalExpenditure(statistics.getTotalExpenditure());
            statsData.setTotalDaysPlayed(statistics.getTotalDaysPlayed());
            
            // Convert Season keys to String for serialization
            Map<String, Integer> seasonalIncome = new HashMap<>();
            for (Map.Entry<Season, Integer> entry : statistics.getSeasonalIncome().entrySet()) {
                seasonalIncome.put(entry.getKey().toString(), entry.getValue());
            }
            statsData.setSeasonalIncome(seasonalIncome);
            
            Map<String, Integer> seasonalExpenditure = new HashMap<>();
            for (Map.Entry<Season, Integer> entry : statistics.getSeasonalExpenditure().entrySet()) {
                seasonalExpenditure.put(entry.getKey().toString(), entry.getValue());
            }
            statsData.setSeasonalExpenditure(seasonalExpenditure);
            
            // NPC interactions
            statsData.setChatFrequency(new HashMap<>(statistics.getChatFrequency()));
            statsData.setGiftFrequency(new HashMap<>(statistics.getGiftFrequency()));
            statsData.setVisitFrequency(new HashMap<>(statistics.getVisitFrequency()));
            
            // Crops and fish data
            statsData.setCropsHarvestedCount(new HashMap<>(statistics.getCropsHarvestedCount()));
            statsData.setUniqueCropsHarvested(new HashSet<>(statistics.getUniqueCropsHarvested()));
            
            // Convert FishRarity keys to String for serialization
            Map<String, Map<String, Integer>> fishCaught = new HashMap<>();
            for (Map.Entry<String, Map<com.spakborhills.model.Enum.FishRarity, Integer>> entry : statistics.getFishCaught().entrySet()) {
                Map<String, Integer> rarityMap = new HashMap<>();
                for (Map.Entry<com.spakborhills.model.Enum.FishRarity, Integer> rarityEntry : entry.getValue().entrySet()) {
                    rarityMap.put(rarityEntry.getKey().toString(), rarityEntry.getValue());
                }
                fishCaught.put(entry.getKey(), rarityMap);
            }
            statsData.setFishCaught(fishCaught);
            
            statsData.setUniqueFishCaught(new HashSet<>(statistics.getUniqueFishCaught()));
            
            // Achievement tracking
            statsData.setKeyEventsOrItemsObtained(new HashSet<>(statistics.getKeyEventsOrItemsObtained()));
            
            saveData.setStatisticsData(statsData);
            System.out.println("Statistics data saved: Total Income: " + statsData.getTotalIncome() + 
                               ", Total Expenditure: " + statsData.getTotalExpenditure() + 
                               ", Seasonal Income: " + statsData.getSeasonalIncome().size() + " seasons" +
                               ", Seasonal Expenditure: " + statsData.getSeasonalExpenditure().size() + " seasons" +
                               ", Unique Crops: " + statsData.getUniqueCropsHarvested().size() +
                               ", Unique Fish: " + statsData.getUniqueFishCaught().size() +
                               ", Events: " + statsData.getKeyEventsOrItemsObtained().size());
        }

        return saveData;
    }

    /**
     * Load a game from a specific save file
     * @param fileName The name of the save file to load
     * @return The loaded SaveData, or null if loading failed
     */
    public SaveData loadGame(String fileName) {
        String filePath = SAVE_DIRECTORY + "/" + fileName;
        try (FileReader reader = new FileReader(filePath)) {
            SaveData saveData = gson.fromJson(reader, SaveData.class);
            if (saveData != null) {
                System.out.println("Game loaded successfully from " + filePath);
            }
            return saveData;
        } catch (IOException e) {
            System.err.println("Error loading game (file not found or corrupt?): " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete a save file
     * @param fileName The name of the save file to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteSave(String fileName) {
        String filePath = SAVE_DIRECTORY + "/" + fileName;
        File saveFile = new File(filePath);
        
        if (saveFile.exists()) {
            boolean deleted = saveFile.delete();
            if (deleted) {
                System.out.println("Save file deleted: " + filePath);
            } else {
                System.err.println("Failed to delete save file: " + filePath);
            }
            return deleted;
        } else {
            System.err.println("Save file not found: " + filePath);
            return false;
        }
    }

    public void applySaveDataToGame(SaveData saveData, Farm farm, Player player, GameTime timeService) {
        if (saveData == null) {
            System.err.println("No save data to apply.");
            return;
        }

        Map<String, Item> itemRegistry = farm.getItemRegistry();
        if (itemRegistry == null) {
            System.err.println("CRITICAL ERROR saat load: ItemRegistry di Farm adalah null. Tidak bisa memuat item/crop.");
            return;
        }
        
        // Clear existing NPC list in Farm before loading, or update existing ones
        // farm.clearNpcs(); // Or similar logic

        // 1. Apply Player Data
        player.setName(saveData.getPlayerName());
        player.setGender(saveData.getPlayerGender());
        player.setFarmName(saveData.getPlayerFarmName());
        player.setGold(saveData.getPlayerMoney());
        player.setEnergy(saveData.getPlayerEnergy());

        if (saveData.getPlayerPartner() != null && farm.getNpcs() !=null) {
            Optional<NPC> partnerNpcOpt = farm.findNPC(saveData.getPlayerPartner().getName());
            if (partnerNpcOpt.isPresent()) {
                NPC partnerNpc = partnerNpcOpt.get();
                player.setPartner(partnerNpc);
                partnerNpc.setRelationshipStatus(saveData.getPlayerPartner().getStatus());
                 // Also update the NPC in the main list if not done by reference
                Optional<NPC> npcInListOpt = farm.findNPC(partnerNpc.getName());
                if (npcInListOpt.isPresent()) {
                    NPC npcInList = npcInListOpt.get();
                    npcInList.setRelationshipStatus(saveData.getPlayerPartner().getStatus());
                }
            } else {
                 System.err.println("Error loading partner: NPC with name '" + saveData.getPlayerPartner().getName() + "' not found.");
            }
        } else {
            player.setPartner(null);
        }
        
        MapArea targetMap = farm.getWorldMap().getMapAreaByName(saveData.getCurrentMapId());
        if (targetMap == null && farm.getFarmMap().getName().equals(saveData.getCurrentMapId())) {
            targetMap = farm.getFarmMap();
        }

        if (targetMap != null) {
            player.setCurrentMap(targetMap); 
            player.setPosition(saveData.getPlayerX(), saveData.getPlayerY());
            System.out.println("Player map set to: " + targetMap.getName() + " and position to (" + saveData.getPlayerX() + "," + saveData.getPlayerY() + ")");
        } else {
            System.err.println("Error loading player map: Map ID '" + saveData.getCurrentMapId() + "' tidak ditemukan. Defaulting to farm map.");
            player.setCurrentMap(farm.getFarmMap()); // Default to farm map
            player.setPosition(farm.getFarmMap().getPlayerSpawnX(), farm.getFarmMap().getPlayerSpawnY()); // Default spawn
        }


        // 2. Apply Inventory Data
        if (player.getInventory() != null) {
            player.getInventory().clear(); 
            if (saveData.getPlayerInventory() != null && saveData.getPlayerInventory().getItems() != null) {
                for (InventoryItemData itemData : saveData.getPlayerInventory().getItems()) {
                    Item itemTemplate = itemRegistry.get(itemData.getItemId());
                    if (itemTemplate != null) {
                        player.getInventory().addItem(itemTemplate.cloneItem(), itemData.getQuantity()); // cloneItem if necessary
                    } else {
                        System.err.println("Error loading inventory: Item ID '" + itemData.getItemId() + "' tidak ditemukan di registry.");
                    }
                }
            }
        }
        if (saveData.getUnlockedRecipes() != null) {
             player.setUnlockedRecipes(new ArrayList<>(saveData.getUnlockedRecipes())); // Assuming Player has setUnlockedRecipes()
        }
        System.out.println("Inventory and recipes loaded.");

        // 3. Apply Time Data
        timeService.setDayOfMonth(saveData.getCurrentDay());
        timeService.setTime(saveData.getCurrentHour(), saveData.getCurrentMinute()); 
        try {
            Season season = Season.valueOf(saveData.getCurrentSeason().toUpperCase()); 
            timeService.setSeason(season);
        } catch (IllegalArgumentException e) {
            System.err.println("Error loading time: Season string '" + saveData.getCurrentSeason() + "' tidak valid.");
            timeService.setSeason(Season.SPRING); // Fallback
        }
        timeService.setYear(saveData.getCurrentYear());
        try {
            Weather weather = Weather.valueOf(saveData.getCurrentWeather().toUpperCase());
            farm.setCurrentWeather(weather); // Assuming Farm has setCurrentWeather()
        } catch (IllegalArgumentException e) {
            System.err.println("Error loading weather: Weather string '" + saveData.getCurrentWeather() + "' tidak valid.");
            farm.setCurrentWeather(Weather.SUNNY); // Fallback
        }
        System.out.println("Time and weather loaded: Day " + timeService.getCurrentDay() + ", Time " + timeService.getHour() + ":" + timeService.getMinute() + ", Season " + timeService.getCurrentSeason() + ", Year " + timeService.getCurrentYear() + ", Weather " + farm.getCurrentWeather());

        // 4. Apply Farm Tile Data
        MapArea farmMapToRestore = farm.getFarmMap(); 
        if (farmMapToRestore != null && farmMapToRestore.getTiles() != null && saveData.getFarmTiles() != null) {
            // Cast to FarmMap to get entry points (if it is a FarmMap)
            List<Point> entryPoints = new ArrayList<>();
            if (farmMapToRestore instanceof com.spakborhills.model.Map.FarmMap) {
                com.spakborhills.model.Map.FarmMap concreteFarmMap = (com.spakborhills.model.Map.FarmMap) farmMapToRestore;
                entryPoints = concreteFarmMap.getEntryPoints();
                System.out.println("Loaded " + entryPoints.size() + " entry points to preserve during tile restoration.");
            }
            
            Tile[][] tiles = farmMapToRestore.getTiles();
                for (Map.Entry<String, FarmTileData> entry : saveData.getFarmTiles().entrySet()) {
                    String[] coords = entry.getKey().split(",");
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    FarmTileData tileData = entry.getValue();

                    if (y >= 0 && y < tiles.length && x >= 0 && x < tiles[y].length) {
                        Tile targetTile = tiles[y][x];
                    
                    // Check if this tile is an entry point - if so, preserve it
                    if (entryPoints.contains(new Point(x, y))) {
                        System.out.println("Preserving entry point at (" + x + "," + y + ")");
                        continue; // Skip modifying entry points completely
                    }
                    
                        if (targetTile != null) {
                        try {
                            // First store the original type from the save data
                            TileType originalTileType = TileType.valueOf(tileData.getTileType().toUpperCase());
                            
                            // Debug for TILLED tiles
                            if (originalTileType == TileType.TILLED) {
                                System.out.println("DEBUG: Loading TILLED tile at (" + x + "," + y + ")");
                            }
                            
                            // Now clear any existing seeds - this no longer changes the type thanks to our Tile.java fix
                            targetTile.setPlantedSeedForLoad(null);
                            
                            // If there's a crop to plant
                            if (tileData.getCropId() != null && !tileData.getCropId().isEmpty()) {
                                Seed seedToPlant = null;
                                for (Item itemFromRegistry : itemRegistry.values()) {
                                    if (itemFromRegistry instanceof Seed) {
                                        Seed currentSeed = (Seed) itemFromRegistry;
                                        if (currentSeed.getCropYieldName().equals(tileData.getCropId())) {
                                            seedToPlant = (Seed) currentSeed.cloneItem(); // Clone if seeds are stateful
                                            break;
                                        }
                                    }
                                }

                                if (seedToPlant != null) {
                                    targetTile.setPlantedSeedForLoad(seedToPlant); 
                                    targetTile.setGrowthDays(tileData.getGrowthStage());
                                    if (tileData.isWatered()) {
                                        targetTile.markAsWatered();
                                    } else {
                                        targetTile.clearWatered(); // Ensure it's marked as not watered
                                    }
                                    targetTile.setLastWateredDay(tileData.getLastWateredDay());
                                } else {
                                    System.err.println("Error loading farm tile at ("+x+","+y+"): Seed untuk crop ID '" + tileData.getCropId() + "' tidak ditemukan.");
                                    targetTile.setType(TileType.TILLABLE); 
                                }
                            } else {
                                // If no crop, make sure we set the right tile type from the save data
                                // This will restore TILLED and any other types properly
                                targetTile.setType(originalTileType);
                                
                                // Set water status if applicable
                                if (originalTileType == TileType.TILLED && tileData.isWatered()) {
                                    targetTile.markAsWatered();
                                    targetTile.setLastWateredDay(tileData.getLastWateredDay());
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            System.err.println("Error loading farm tile at ("+x+","+y+"): TileType string '" + tileData.getTileType() + "' tidak valid. Defaulting to TILLABLE.");
                            targetTile.setType(TileType.TILLABLE);
                        }
                    }
                }
            }
        }
        System.out.println("Farm tiles loaded.");

        // 5. Apply Shipping Bin Contents
        ShippingBin shippingBin = farm.getShippingBin();
        if (shippingBin != null) {
            shippingBin.clearBin(); // Clear existing items
            if (saveData.getShippingBinContents() != null) {
                for (SaveData.ShippingBinItemData itemData : saveData.getShippingBinContents()) {
                    Item itemTemplate = itemRegistry.get(itemData.getItemId());
                    if (itemTemplate != null) {
                        shippingBin.addItem(itemTemplate.cloneItem(), itemData.getQuantity()); // cloneItem if necessary
                    } else {
                        System.err.println("Error loading shipping bin: Item ID '" + itemData.getItemId() + "' tidak ditemukan di registry.");
                    }
                }
            }
        }
        System.out.println("Shipping bin loaded.");

        // 6. Apply NPC Data
        if (saveData.getNpcDataMap() != null && farm.getNpcs() != null) {
            for (Map.Entry<String, SaveData.NpcData> entry : saveData.getNpcDataMap().entrySet()) {
                Optional<NPC> npcOpt = farm.findNPC(entry.getKey());
                if (npcOpt.isPresent()) {
                    NPC npc = npcOpt.get();
                    SaveData.NpcData npcSaveData = entry.getValue();
                    npc.setHeartPoints(npcSaveData.getHeartPoints());
                    if (player.getPartner() == null || !player.getPartner().getName().equals(npc.getName())) {
                         npc.setRelationshipStatus(npcSaveData.getRelationshipStatus());
                    } else {
                         npc.setRelationshipStatus(player.getPartner().getRelationshipStatus());
                    }
                } else {
                    System.err.println("Error loading NPC data: NPC with name '" + entry.getKey() + "' not found.");
                }
            }
        }
        System.out.println("NPC data loaded.");

        // 7. Apply Milestones
        if (saveData.getMilestonesAchieved() != null) {
            farm.setAchievedMilestones(new ArrayList<>(saveData.getMilestonesAchieved())); // Assuming Farm has setAchievedMilestones()
        }
        System.out.println("Milestones loaded.");

        // 8. Apply Bonus Data (Example for Furniture)
        if (saveData.getBonusData() != null && saveData.getBonusData().getHouseFurniture() != null) {
            if (farm.getHouse() != null) {
                farm.getHouse().clearFurniture(); // Clear existing furniture
                for (SaveData.FurnitureData furnitureData : saveData.getBonusData().getHouseFurniture()) {
                    Item furnitureItem = itemRegistry.get(furnitureData.getItemId());
                    if (furnitureItem instanceof Furniture) {
                        Furniture newFurniture = (Furniture) furnitureItem.cloneItem(); // Clone if necessary
                        newFurniture.setPosition(furnitureData.getX(), furnitureData.getY()); // Assuming Furniture has setPosition
                        farm.getHouse().addFurniture(newFurniture);
                    } else {
                        System.err.println("Error loading furniture: Item ID '" + furnitureData.getItemId() + "' is not a valid Furniture item or not found.");
                    }
                }
            }
        }
        System.out.println("Bonus data (furniture) loaded.");
        
        // 9. Apply Farm Deployed Objects
        if (farm.getFarmMap() instanceof com.spakborhills.model.Map.FarmMap) {
            com.spakborhills.model.Map.FarmMap concreteFarmMap = (com.spakborhills.model.Map.FarmMap) farm.getFarmMap();
            concreteFarmMap.clearAllDeployedObjects(); // Clear existing objects first

            if (saveData.getFarmDeployedObjects() != null) {
                for (SaveData.PlacedObjectData placedObjectData : saveData.getFarmDeployedObjects()) {
                    try {
                        Class<?> deployedObjectClass = Class.forName(placedObjectData.getObjectClassType());
                        // Assuming a no-arg constructor for all DeployedObject types
                        DeployedObject deployedObjectInstance = (DeployedObject) deployedObjectClass.getDeclaredConstructor().newInstance();
                        
                        boolean placed = concreteFarmMap.placeObject(deployedObjectInstance, placedObjectData.getX(), placedObjectData.getY());
                        if (placed) {
                            System.out.println("Loaded and placed deployed object: " + deployedObjectInstance.getName() + " of type " + placedObjectData.getObjectClassType() + " at (" + placedObjectData.getX() + "," + placedObjectData.getY() + ")");
                        } else {
                            System.err.println("Error loading deployed object: Failed to place " + placedObjectData.getObjectName() + " of type " + placedObjectData.getObjectClassType() + " at (" + placedObjectData.getX() + "," + placedObjectData.getY() + ")");
                        }
                    } catch (ClassNotFoundException e) {
                        System.err.println("Error loading deployed object: Class not found - " + placedObjectData.getObjectClassType() + ". " + e.getMessage());
                    } catch (NoSuchMethodException e) {
                        System.err.println("Error loading deployed object: No-arg constructor not found for " + placedObjectData.getObjectClassType() + ". " + e.getMessage());
                    } catch (InstantiationException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                        System.err.println("Error loading deployed object: Failed to instantiate " + placedObjectData.getObjectClassType() + ". " + e.getMessage());
                    } catch (Exception e) { // Catch any other unexpected errors during object restoration
                        System.err.println("Unexpected error loading deployed object " + placedObjectData.getObjectName() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.err.println("Could not load deployed farm objects: FarmMap is not an instance of com.spakborhills.model.Map.FarmMap");
        }
        System.out.println("Farm deployed objects loading process completed.");

        // 10. Apply Statistics Data
        if (saveData.getStatisticsData() != null && farm.getStatistics() != null) {
            com.spakborhills.model.Util.EndGameStatistics currentStats = farm.getStatistics();
            SaveData.StatisticsData statsData = saveData.getStatisticsData();
            
            // Create a new EndGameStatistics instance with the saved data
            // We'll use the existing stats for data we can't load (like references to NPCs)
            // Start by getting values from the saved data
            int totalIncome = statsData.getTotalIncome();
            int totalExpenditure = statsData.getTotalExpenditure();
            int totalDaysPlayed = statsData.getTotalDaysPlayed();
            
            // Directly set the total values to ensure they match exactly what was saved
            currentStats.setTotalIncome(totalIncome);
            currentStats.setTotalExpenditure(totalExpenditure);
            currentStats.setTotalDaysPlayed(totalDaysPlayed);
            
            Map<String, Integer> chatFrequency = statsData.getChatFrequency();
            Map<String, Integer> giftFrequency = statsData.getGiftFrequency();
            Map<String, Integer> visitFrequency = statsData.getVisitFrequency();
            
            // Now use the EndGameStatistics API to update the current stats
            // For each income amount by season
            for (Map.Entry<String, Integer> entry : statsData.getSeasonalIncome().entrySet()) {
                try {
                    Season season = Season.valueOf(entry.getKey().toUpperCase());
                    // This will update the total income as well
                    currentStats.recordIncome(entry.getValue(), season);
                } catch (IllegalArgumentException e) {
                    System.err.println("Error loading season data: " + e.getMessage());
                }
            }
            
            // For each expenditure amount by season
            for (Map.Entry<String, Integer> entry : statsData.getSeasonalExpenditure().entrySet()) {
                try {
                    Season season = Season.valueOf(entry.getKey().toUpperCase());
                    // This will update the total expenditure as well
                    currentStats.recordExpenditure(entry.getValue(), season);
                } catch (IllegalArgumentException e) {
                    System.err.println("Error loading season data: " + e.getMessage());
                }
            }
            
            // To update NPC-related data, we need to loop through the maps
            if (chatFrequency != null) {
                for (Map.Entry<String, Integer> entry : chatFrequency.entrySet()) {
                    // Record each chat interaction the number of times it was recorded
                    for (int i = 0; i < entry.getValue(); i++) {
                        currentStats.recordChat(entry.getKey());
                    }
                }
            }
            
            if (giftFrequency != null) {
                for (Map.Entry<String, Integer> entry : giftFrequency.entrySet()) {
                    // Record each gift interaction the number of times it was recorded
                    for (int i = 0; i < entry.getValue(); i++) {
                        currentStats.recordGift(entry.getKey());
                    }
                }
            }
            
            if (visitFrequency != null) {
                for (Map.Entry<String, Integer> entry : visitFrequency.entrySet()) {
                    // Record each visit interaction the number of times it was recorded
                    for (int i = 0; i < entry.getValue(); i++) {
                        currentStats.recordVisit(entry.getKey());
                    }
                }
            }
            
            // For fish caught data, we need to handle the conversion of string rarity back to enum
            if (statsData.getFishCaught() != null) {
                for (Map.Entry<String, Map<String, Integer>> entry : statsData.getFishCaught().entrySet()) {
                    String fishName = entry.getKey();
                    Map<String, Integer> rarityMap = entry.getValue();
                    
                    for (Map.Entry<String, Integer> rarityEntry : rarityMap.entrySet()) {
                        try {
                            com.spakborhills.model.Enum.FishRarity rarity = 
                                com.spakborhills.model.Enum.FishRarity.valueOf(rarityEntry.getKey().toUpperCase());
                            // Record this fish catch the number of times it was caught
                            for (int i = 0; i < rarityEntry.getValue(); i++) {
                                currentStats.recordFishCatch(fishName, rarity);
                            }
                        } catch (IllegalArgumentException e) {
                            System.err.println("Error loading fish rarity data: " + e.getMessage());
                        }
                    }
                }
            }
            
            // We can't easily handle crop harvests without crop objects, so we'll skip that for now
            
            // For key events, we can just record each one
            if (statsData.getKeyEventsOrItemsObtained() != null) {
                for (String eventKey : statsData.getKeyEventsOrItemsObtained()) {
                    currentStats.recordKeyEventOrItem(eventKey);
                }
            }
            
            // Set crops harvested data
            if (statsData.getCropsHarvestedCount() != null) {
                Map<String, Integer> cropsHarvestedCount = statsData.getCropsHarvestedCount();
                for (Map.Entry<String, Integer> entry : cropsHarvestedCount.entrySet()) {
                    // We'll call recordHarvest once for each crop type with the total quantity
                    currentStats.recordHarvest(entry.getKey(), entry.getValue());
                }
            }
            
            System.out.println("Statistics data loaded: Total Income: " + statsData.getTotalIncome() + 
                              ", Total Expenditure: " + statsData.getTotalExpenditure() + 
                              ", Unique Crops: " + (statsData.getUniqueCropsHarvested() != null ? statsData.getUniqueCropsHarvested().size() : 0) +
                              ", Unique Fish: " + (statsData.getUniqueFishCaught() != null ? statsData.getUniqueFishCaught().size() : 0));
        }

        System.out.println("All save data applied to game state.");
    }
    
    /**
     * SaveSlot class for storing metadata about available save files
     */
    public static class SaveSlot {
        private String fileName;
        private String playerName;
        private String farmName;
        private int day;
        private String season;
        private int year;
        private Date lastModified;
        
        // Getters and setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getPlayerName() { return playerName; }
        public void setPlayerName(String playerName) { this.playerName = playerName; }
        
        public String getFarmName() { return farmName; }
        public void setFarmName(String farmName) { this.farmName = farmName; }
        
        public int getDay() { return day; }
        public void setDay(int day) { this.day = day; }
        
        public String getSeason() { return season; }
        public void setSeason(String season) { this.season = season; }
        
        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }
        
        public Date getLastModified() { return lastModified; }
        public void setLastModified(Date lastModified) { this.lastModified = lastModified; }
        
        @Override
        public String toString() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            return playerName + "'s " + farmName + " - " + 
                   season + " Day " + day + ", Year " + year + " - " +
                   dateFormat.format(lastModified);
        }
    }
} 