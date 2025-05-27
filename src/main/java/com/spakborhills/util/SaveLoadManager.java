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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SaveLoadManager {

    private static final String SAVE_FILE_PATH = "savegame.json";
    private Gson gson;

    public SaveLoadManager() {
        // GsonBuilder untuk pretty printing agar file JSON mudah dibaca
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void saveGame(Farm farm, Player player, GameTime timeService) {
        SaveData saveData = new SaveData();

        // 1. Populate Player Data
        saveData.setPlayerX(player.getCurrentTileX());
        saveData.setPlayerY(player.getCurrentTileY());
        if (player.getCurrentMap() != null) {
            saveData.setCurrentMapId(player.getCurrentMap().getName()); // Menggunakan getName()
        } else {
            System.err.println("Peringatan saat menyimpan: Player currentMap is null!");
            saveData.setCurrentMapId("Farm"); // Fallback ke nama default FarmMap jika ada
        }
        saveData.setPlayerMoney(player.getGold());
        saveData.setPlayerEnergy(player.getEnergy());

        // 2. Populate Inventory Data
        InventoryData inventoryData = new InventoryData();
        if (player.getInventory() != null && player.getInventory().getItems() != null) {
            for (Map.Entry<Item, Integer> entry : player.getInventory().getItems().entrySet()) {
                Item item = entry.getKey();
                Integer quantity = entry.getValue();
                if (item != null && quantity != null && quantity > 0) {
                    inventoryData.addItem(new InventoryItemData(item.getName(), quantity));
                }
            }
        }
        saveData.setPlayerInventory(inventoryData);

        // 3. Populate Time Data
        saveData.setCurrentDay(timeService.getCurrentDay());
        saveData.setCurrentHour(timeService.getHour());
        saveData.setCurrentSeason(timeService.getCurrentSeason().toString());
        saveData.setCurrentYear(timeService.getCurrentYear());

        // 4. Populate Farm Tile Data
        Map<String, FarmTileData> farmTilesData = new HashMap<>();
        // Always save tiles from the main FarmMap
        MapArea farmMap = farm.getFarmMap(); 
        if (farmMap != null && farmMap.getTiles() != null) {
            Tile[][] tiles = farmMap.getTiles();
            for (int r = 0; r < tiles.length; r++) {
                for (int c = 0; c < tiles[r].length; c++) {
                    Tile currentTile = tiles[r][c];
                    if (currentTile != null && currentTile.getPlantedSeed() != null) {
                        Seed plantedSeed = currentTile.getPlantedSeed();
                        FarmTileData tileData = new FarmTileData(
                                plantedSeed.getCropYieldName(),
                                currentTile.getGrowthDays(),
                                currentTile.isWatered()
                        );
                        farmTilesData.put(c + "," + r, tileData);
                    }
                }
            }
        }
        saveData.setFarmTiles(farmTilesData);

        // Tulis ke file JSON
        try (FileWriter writer = new FileWriter(SAVE_FILE_PATH)) {
            gson.toJson(saveData, writer);
            System.out.println("Game saved successfully to " + SAVE_FILE_PATH);
        } catch (IOException e) {
            System.err.println("Error saving game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public SaveData loadGame() {
        try (FileReader reader = new FileReader(SAVE_FILE_PATH)) {
            SaveData saveData = gson.fromJson(reader, SaveData.class);
            if (saveData != null) {
                System.out.println("Game loaded successfully from " + SAVE_FILE_PATH);
            }
            return saveData;
        } catch (IOException e) {
            System.err.println("Error loading game (file not found or corrupt?): " + e.getMessage());
            return null;
        }
    }

    public void applySaveDataToGame(SaveData saveData, Farm farm, Player player, GameTime timeService) {
        if (saveData == null) {
            System.err.println("No save data to apply.");
            return;
        }

        // Referensi ke ItemRegistry dari Farm, sangat penting untuk membuat objek Item/Seed dari ID
        Map<String, Item> itemRegistry = farm.getItemRegistry();
        if (itemRegistry == null) {
            System.err.println("KRITICAL ERROR saat load: ItemRegistry di Farm adalah null. Tidak bisa memuat item/crop.");
            // Mungkin lempar exception atau hentikan proses load
            return;
        }

        // 1. Apply Player Data
        // Perpindahan map dan posisi HARUS ditangani dengan benar.
        // Untuk sementara, kita set posisi. Idealnya, panggil GameController.
        // gameController.requestVisitByID(saveData.getCurrentMapId(), saveData.getPlayerX(), saveData.getPlayerY());
        // Jika GameController tidak diinject, Farm atau Main class perlu memanggil ini setelah load.
        // Untuk sekarang, kita set map dan posisi di player, TAPI ini mungkin tidak cukup untuk render dll.
        MapArea targetMap = farm.getWorldMap().getMapAreaByName(saveData.getCurrentMapId());
        if (targetMap == null && farm.getFarmMap().getName().equals(saveData.getCurrentMapId())) {
            targetMap = farm.getFarmMap();
        }

        if (targetMap != null) {
            player.setCurrentMap(targetMap); // Ini mungkin tidak cukup, perlu koordinasi dengan GamePanel/Controller
            player.setPosition(saveData.getPlayerX(), saveData.getPlayerY());
            System.out.println("Player map set to: " + targetMap.getName() + " and position to (" + saveData.getPlayerX() + "," + saveData.getPlayerY() + ")");
        } else {
            System.err.println("Error loading player map: Map ID '" + saveData.getCurrentMapId() + "' tidak ditemukan.");
            // Fallback ke map default jika perlu
        }
        player.setGold(saveData.getPlayerMoney());
        player.setEnergy(saveData.getPlayerEnergy());

        // 2. Apply Inventory Data
        if (player.getInventory() != null) {
            player.getInventory().clear(); // Bersihkan inventaris lama
            if (saveData.getPlayerInventory() != null && saveData.getPlayerInventory().getItems() != null) {
                for (InventoryItemData itemData : saveData.getPlayerInventory().getItems()) {
                    Item itemTemplate = itemRegistry.get(itemData.getItemId());
                    if (itemTemplate != null) {
                        // Kuantitas sudah ada di itemData.getQuantity()
                        // addItem di Inventory menangani kuantitas
                        player.getInventory().addItem(itemTemplate, itemData.getQuantity());
                    } else {
                        System.err.println("Error loading inventory: Item ID '" + itemData.getItemId() + "' tidak ditemukan di registry.");
                    }
                }
            }
        }
        System.out.println("Inventory loaded.");

        // 3. Apply Time Data
        timeService.setDayOfMonth(saveData.getCurrentDay());
        timeService.setTime(saveData.getCurrentHour(), 0); // Set menit ke 0
        try {
            Season season = Season.valueOf(saveData.getCurrentSeason().toUpperCase()); // Pastikan case sesuai enum
            timeService.setSeason(season);
        } catch (IllegalArgumentException e) {
            System.err.println("Error loading time: Season string '" + saveData.getCurrentSeason() + "' tidak valid.");
            timeService.setSeason(Season.SPRING); // Fallback
        }
        timeService.setYear(saveData.getCurrentYear());
        System.out.println("Time loaded: Day " + timeService.getCurrentDay() + ", Hour " + timeService.getHour() + ", Season " + timeService.getCurrentSeason() + ", Year " + timeService.getCurrentYear());

        // 4. Apply Farm Tile Data
        // This section will now specifically restore tiles to the FarmMap.
        MapArea farmMapToRestore = farm.getFarmMap(); 

        if (farmMapToRestore != null && farmMapToRestore.getTiles() != null && saveData.getFarmTiles() != null) {
            // First, reset all tiles on the FarmMap to a default state.
            // This ensures that tiles not in the save data (e.g., previously planted but now empty) are cleared.
            Tile[][] tilesToReset = farmMapToRestore.getTiles();
            if (tilesToReset != null) {
                for (int r = 0; r < tilesToReset.length; r++) {
                    for (int c = 0; c < tilesToReset[r].length; c++) {
                        Tile tile = tilesToReset[r][c];
                        if (tile != null) {
                            // Resetting the tile:
                            // - setPlantedSeedForLoad(null) should remove any crop and ideally reset type to TILLABLE or default.
                            // - It should also ideally make the tile unwatered.
                            // If Tile.java's setPlantedSeedForLoad(null) doesn't fully reset (e.g., type or watered status),
                            // additional calls like tile.setType(TileType.TILLABLE); or an explicit tile.setWatered(false); might be needed.
                            tile.setPlantedSeedForLoad(null); 
                        }
                    }
                }
            }
            
            Tile[][] tiles = farmMapToRestore.getTiles(); // Re-fetch, though tilesToReset is the same array
            if (tiles != null) {
                for (Map.Entry<String, FarmTileData> entry : saveData.getFarmTiles().entrySet()) {
                    String[] coords = entry.getKey().split(",");
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    FarmTileData tileData = entry.getValue();

                    if (y >= 0 && y < tiles.length && x >= 0 && x < tiles[y].length) {
                        Tile targetTile = tiles[y][x];
                        if (targetTile != null) {
                            // Reset tile ke TILLABLE atau PLANTED dulu
                            // targetTile.setType(TileType.TILLABLE); // atau reset total
                            
                            if (tileData.getCropId() != null && !tileData.getCropId().isEmpty()) {
                                // Cari Seed berdasarkan nama CropYield-nya
                                Seed seedToPlant = null;
                                for (Item itemFromRegistry : itemRegistry.values()) {
                                    if (itemFromRegistry instanceof Seed) {
                                        Seed currentSeed = (Seed) itemFromRegistry;
                                        if (currentSeed.getCropYieldName().equals(tileData.getCropId())) {
                                            seedToPlant = currentSeed;
                                            break;
                                        }
                                    }
                                }

                                if (seedToPlant != null) {
                                    // Gunakan metode khusus untuk load, atau set manual
                                    targetTile.setPlantedSeedForLoad(seedToPlant); // Mengatur seed dan tipe tile
                                    targetTile.setGrowthDays(tileData.getGrowthStage());
                                    if (tileData.isWatered()) {
                                        targetTile.markAsWatered();
                                    } else {
                                        // Jika tidak disiram di save, pastikan flag isWatered di tile juga false
                                        // Tile.setPlantedSeedForLoad mungkin sudah mengatur ini, atau perlu setter eksplisit
                                        // Untuk sekarang, kita asumsikan setPlantedSeedForLoad cukup
                                    }
                                } else {
                                    System.err.println("Error loading farm tile at ("+x+","+y+"): Seed untuk crop ID '" + tileData.getCropId() + "' tidak ditemukan.");
                                    targetTile.setType(TileType.TILLABLE); // Reset jika gagal load crop
                                    targetTile.setPlantedSeedForLoad(null); // Ensure no seed if it failed
                                }
                            } else {
                                // Tidak ada cropId, pastikan tile bukan PLANTED
                                // The reset loop above should have handled this, but as a safeguard:
                                targetTile.setPlantedSeedForLoad(null);
                                if(targetTile.getType() == TileType.PLANTED){
                                   targetTile.setType(TileType.TILLABLE); 
                                }
                            }
                        }
                    } else {
                        System.err.println("Error loading farm tile: Koordinat (" + x + "," + y + ") di luar batas untuk map " + farmMapToRestore.getName());
                    }
                }
            }
             System.out.println("Farm tiles loaded for map: " + farmMapToRestore.getName());
        } else {
            if (farmMapToRestore == null) {
                System.out.println("FarmMap not found, cannot load farm tiles.");
            } else {
                System.out.println("No farm tiles to load or FarmMap tiles array is null.");
            }
        }
        System.out.println("Save data applied to game state.");
    }
} 