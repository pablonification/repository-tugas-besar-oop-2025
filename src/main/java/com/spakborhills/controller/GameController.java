package com.spakborhills.controller;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import javax.swing.JOptionPane;

import com.spakborhills.model.Farm;
import com.spakborhills.model.Player;
import com.spakborhills.model.Store;
import com.spakborhills.model.Enum.Direction;
import com.spakborhills.model.Enum.FishRarity;
import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Enum.Season;
import com.spakborhills.model.Enum.TileType;
import com.spakborhills.model.Enum.Weather;
import com.spakborhills.model.Item.*;
import com.spakborhills.model.Map.FarmMap;
import com.spakborhills.model.Map.MapArea;
import com.spakborhills.model.Map.Tile;
import com.spakborhills.model.NPC.NPC;
import com.spakborhills.model.Util.*;
import com.spakborhills.view.GamePanel;
import com.spakborhills.model.Object.House; 
import com.spakborhills.model.Util.ShippingBin; 
import com.spakborhills.model.Enum.GameState; 
import com.spakborhills.model.Object.DeployedObject; 
import com.spakborhills.util.SaveLoadManager; 

public class GameController {

    private Farm farmModel;
    private GamePanel gamePanel; // Referensi ke GamePanel untuk menampilkan pesan

    public GameController(Farm farmModel) {
        this.farmModel = farmModel;
        this.gamePanel = null;
    }

    /**
     * Mengatur referensi GamePanel untuk Controller.
     * Ini harus dipanggil setelah GamePanel diinstansiasi.
     * @param panel Referensi ke GamePanel.
     */
    public void setGamePanel(GamePanel panel) {
        this.gamePanel = panel;
    }

    /**
     * Attempts to move the player in the specified direction.
     * Player's energy and position are updated by the Player model itself.
     * @param direction The direction to move.
     * @return true if the player successfully moved, false otherwise.
     */
    public boolean requestPlayerMove(Direction direction) {
        if (farmModel == null) {
            System.err.println("GameController: Farm model is null, cannot move player.");
            return false;
        }
        Player player = farmModel.getPlayer();
        if (player == null) {
            System.err.println("GameController: Player is null in Farm model, cannot move.");
            return false;
        }
        boolean moved = player.move(direction);
        if (moved) {
            if (player.isOnEntryPoint()) {
                System.out.println("Player is on an entry point of " + player.getCurrentMap().getName() + ". Triggering world map dialog...");
                if (gamePanel != null) {
                    gamePanel.showWorldMapSelectionDialog();
                } else {
                    System.err.println("GameController: gamePanel is null, cannot show world map dialog.");
                }
            }
        }
        return moved;
    }

    /**
     * Checks if the player should pass out due to late time and processes it.
     * This method is intended to be called periodically by the game loop/timer.
     */
    public void checkTimeBasedPassOut() {
        if (farmModel == null || farmModel.getPlayer() == null || farmModel.getCurrentTime() == null || gamePanel == null) {
            return; 
        }

        if (farmModel.getCurrentGameState() != GameState.IN_GAME) {
            return;
        }

        if (farmModel.getCurrentTime().isPastBedtime()) {
            System.out.println("GameController: Player is past bedtime. Initiating pass out.");
            Player player = farmModel.getPlayer();
            GameTime currentTime = farmModel.getCurrentTime();
            EndGameStatistics statistics = farmModel.getStatistics();
            PriceList priceList = farmModel.getPriceList();

            String eventMessage = "You stayed up too late and passed out!";
            
            int income = farmModel.getShippingBin().processSales(statistics, priceList, currentTime.getCurrentDay(), currentTime.getCurrentSeason());
            
            player.passOut(farmModel); 

            if (gamePanel != null) {
                gamePanel.showEndOfDayMessage(eventMessage, income, generateNewDayInfoString());
            }
        }
    }

    /**
     * Attempts to till the land at the player's current position.
     * @return true if the tilling action was successful and the view should update, false otherwise.
     */
    public boolean requestTillLandAtPlayerPosition() {
        if (farmModel == null) {
            System.err.println("GameController: Farm model is null, cannot till land.");
            return false;
        }
        Player player = farmModel.getPlayer();
        
        if (player == null) {
            System.err.println("GameController: Player is null, cannot till land.");
            return false;
        }

        if (!(player.getCurrentMap() instanceof FarmMap)) {
            System.out.println("Hoe (Tilling) can only be used on the Farm.");
            return false;
        }
        FarmMap farmMap = (FarmMap) player.getCurrentMap(); 

        if (player.getEnergy() <= Player.MIN_ENERGY) {
            System.out.println("Player is too tired to till land.");
            return false; 
        }

        Tile targetTile = farmMap.getTile(player.getCurrentTileX(), player.getCurrentTileY());
        if (targetTile == null) {
            System.err.println("GameController: Tile at player position is null.");
            return false;
        }

        boolean tilled = player.till(targetTile);
        if (tilled) {
            player.changeEnergy(-5); 
            System.out.println("Tilled land. Energy: " + player.getEnergy());
            checkPassOut(); 
        }
        return tilled;
    }

    /**
     * Attempts to plant a seed at the player's current position.
     * @return true if the planting action was successful and the view should update, false otherwise.
     */
    public boolean requestPlantSeedAtPlayerPosition() {
        if (farmModel == null) {
            System.err.println("GameController: Farm model is null, cannot plant.");
            return false;
        }
        Player player = farmModel.getPlayer();
        GameTime gameTime = farmModel.getCurrentTime();

        if (player == null || gameTime == null) {
            System.err.println("GameController: Player or GameTime is null, cannot plant.");
            return false;
        }

        if (!(player.getCurrentMap() instanceof FarmMap)) {
            System.out.println("Planting seeds can only be done on the Farm.");
            return false;
        }
        FarmMap farmMap = (FarmMap) player.getCurrentMap();

        if (player.getEnergy() <= Player.MIN_ENERGY) {
            System.out.println("Player is too tired to plant.");
            return false;
        }

        Tile targetTile = farmMap.getTile(player.getCurrentTileX(), player.getCurrentTileY());
        if (targetTile == null) {
            System.err.println("[GameController] Target tile is null. Cannot plant.");
            return false;
        }

        Item selected = player.getSelectedItem();
        if (!(selected instanceof Seed)) {
            return false; 
        }
        Seed seedToPlant = (Seed) selected;

        boolean planted = player.plant(seedToPlant, targetTile, gameTime);
        if (planted) {
            player.changeEnergy(-5); 
            System.out.println("Planted " + seedToPlant.getName() + " at ("+ player.getCurrentTileX() + "," + player.getCurrentTileY() +"). Energy: " + player.getEnergy());
            checkPassOut();
        }
        return planted;
    }

    /**
     * Attempts to water the tile at the player's current position.
     * @return true if the watering action was successful and the view should update, false otherwise.
     */
    public boolean requestWaterTileAtPlayerPosition() {
        if (farmModel == null) {
            System.err.println("GameController: Farm model is null, cannot water tile.");
            return false;
        }
        Player player = farmModel.getPlayer();
        GameTime gameTime = farmModel.getCurrentTime(); 

        if (player == null || gameTime == null) {
            System.err.println("GameController: Player, FarmMap, or GameTime is null, cannot water tile.");
            return false;
        }

        if (!(player.getCurrentMap() instanceof FarmMap)) {
            System.out.println("Watering Can can only be used on the Farm.");
            return false;
        }
        FarmMap farmMap = (FarmMap) player.getCurrentMap();

        if (player.getEnergy() <= Player.MIN_ENERGY) {
            System.out.println("Player is too tired to water tile.");
            return false; 
        }

        Tile targetTile = farmMap.getTile(player.getCurrentTileX(), player.getCurrentTileY());
        if (targetTile == null) {
            System.err.println("GameController: Tile at player position is null for watering.");
            return false;
        }

        boolean watered = player.water(targetTile, gameTime.getCurrentWeather());
        if (watered) {
            player.changeEnergy(-5); 
            System.out.println("Watered tile at (" + player.getCurrentTileX() + "," + player.getCurrentTileY() + "). Energy: " + player.getEnergy());
            checkPassOut(); 
        }
        return watered;
    }

    /**
     * Attempts to harvest the crop at the player's current position.
     * @return true if the harvesting action was successful and the view should update, false otherwise.
     */
    public boolean requestHarvestAtPlayerPosition() {
        if (farmModel == null) {
            System.err.println("GameController: Farm model is null, cannot harvest.");
            return false;
        }
        Player player = farmModel.getPlayer();
        Map<String, Item> itemRegistry = farmModel.getItemRegistry();
        if (player == null || itemRegistry == null) {
            System.err.println("GameController: Player or ItemRegistry is null, cannot harvest.");
            return false;
        }

        if (!(player.getCurrentMap() instanceof FarmMap)) {
            System.out.println("Harvesting can only be done on the Farm.");
            return false;
        }
        FarmMap farmMap = (FarmMap) player.getCurrentMap();

        if (player.getEnergy() <= Player.MIN_ENERGY) {
            System.out.println("Player is too tired to harvest.");
            return false;
        }

        Tile targetTile = farmMap.getTile(player.getCurrentTileX(), player.getCurrentTileY());
        if (targetTile == null || !targetTile.isHarvestable()) {
            return false; 
        }

        boolean harvested = player.harvest(targetTile, itemRegistry, farmModel.getStatistics()); // Pass statistics

        if (harvested) {
            System.out.println("Successfully harvested from tile (" + player.getCurrentTileX() + "," + player.getCurrentTileY() + ")");
            player.changeEnergy(-5); 
            checkPassOut(); 
        }
        return harvested;
    }

    /**
     * Attempts to recover the land at the player's current position using a Pickaxe.
     * @return true if the recovery action was successful, false otherwise.
     */
    public boolean requestRecoverLandAtPlayerPosition() {
        if (farmModel == null) {
            System.err.println("GameController: Farm model is null, cannot recover land.");
            return false;
        }
        Player player = farmModel.getPlayer();

        if (player == null) {
            System.err.println("GameController: Player or FarmMap is null, cannot recover land.");
            return false;
        }

        if (!(player.getCurrentMap() instanceof FarmMap)) {
            System.out.println("Pickaxe (Recover Land) can only be used on the Farm.");
            return false;
        }
        FarmMap farmMap = (FarmMap) player.getCurrentMap();

        if (player.getEnergy() <= Player.MIN_ENERGY) {
            System.out.println("Player is too tired to recover land.");
            return false;
        }

        Tile targetTile = farmMap.getTile(player.getCurrentTileX(), player.getCurrentTileY());
        if (targetTile == null) {
            System.err.println("GameController: Tile at player position is null for recovery.");
            return false;
        }

        boolean recovered = player.recoverLand(targetTile);
        if (recovered) {
            player.changeEnergy(-5); 
            System.out.println("Recovered land. Energy: " + player.getEnergy());
            checkPassOut();
        }
        return recovered;
    }

    /**
     * Memproses permintaan pemain untuk memakan item yang sedang dipilih.
     * Mengurangi item dari inventory, memulihkan energi, dan memajukan waktu.
     * @return true jika aksi makan berhasil, false jika tidak (misal, item tidak bisa dimakan, energi penuh).
     */
    public boolean requestEatSelectedItem() {
        if (farmModel == null) {
            System.err.println("GameController: Farm model is null, cannot process eat action.");
            return false;
        }
        Player player = farmModel.getPlayer();
        GameTime gameTime = farmModel.getCurrentTime();

        if (player == null || gameTime == null) {
            System.err.println("GameController: Player or GameTime is null, cannot process eat action.");
            return false;
        }

        Item selectedItem = player.getSelectedItem();
        if (selectedItem == null) {
            System.out.println("GameController: Tidak ada item yang dipilih untuk dimakan.");
            return false;
        }

        // Cek energi sebelum makan, untuk kasus pass-out jika makan item beracun (energi negatif)
        // atau jika pemain sudah pingsan.
        if (player.getEnergy() <= Player.MIN_ENERGY && (!(selectedItem instanceof EdibleItem) || ((EdibleItem)selectedItem).getEnergyRestore() <=0 ) ) {
            System.out.println("Player is too tired to eat (or item provides no energy).");
            return false;
        }

        boolean eaten = player.eat(selectedItem);

        if (eaten) {
            int timeCostEat = 5; 
            gameTime.advance(timeCostEat);
            System.out.println("Player finished eating. Time advanced by " + timeCostEat + " minutes. Current time: " + gameTime.getTimeString());
            
            // Setelah makan, selectedItem mungkin menjadi null jika stacknya habis.
            // Perlu di-handle agar selectedItem di Player konsisten.
            if (player.getInventory().getItemCount(selectedItem) == 0) {
                System.out.println("GameController: Item " + selectedItem.getName() + " habis setelah dimakan.");
                
                if (player.getSelectedItem() != null && player.getInventory().getItemCount(player.getSelectedItem()) == 0) {
                     System.out.println("GameController: Selected item " + player.getSelectedItem().getName() + " habis, mencoba memilih item lain.");
                     
                     selectNextItem(); 
                     if (player.getSelectedItem() == null) { 
                         System.out.println("GameController: Inventory kosong setelah makan, selected item menjadi null.");
                     } else {
                         System.out.println("GameController: Selected item baru setelah makan: " + player.getSelectedItem().getName());
                     }
                }
            }
            checkPassOut(); 
            return true;
        }
        return false;
    }

    /**
     * Checks if the player should pass out due to low energy.
     * If so, processes the pass out logic (handled by Player and Farm model),
     * and informs the GamePanel to display an end-of-day message with income.
     */
    private void checkPassOut() {
        if (farmModel == null || farmModel.getPlayer() == null) {
            System.err.println("GameController.checkPassOut: Critical component (farmModel or player) is null.");
            return;
        }
        Player player = farmModel.getPlayer();
        if (player.getEnergy() <= Player.MIN_ENERGY) {
            int incomeFromSales = player.passOut(farmModel); 

            String eventMessage = player.getName() + " pingsan karena kelelahan!";
            String newDayInfo = generateNewDayInfoString();
            
            if (gamePanel != null) {
                gamePanel.stopGameTimer(); 
                gamePanel.showEndOfDayMessage(eventMessage, incomeFromSales, newDayInfo);
                gamePanel.startGameTimer(); 
            } else {
                System.out.println(eventMessage + " " + newDayInfo + " Pendapatan: " + incomeFromSales + "G (GamePanel belum siap untuk dialog)");
            }
        }
    }

    /**
     * Generates a string summarizing the new day's date and weather.
     * @return A string like "Sekarang adalah Hari ke-X, Musim Y, Cuaca Z."
     */
    private String generateNewDayInfoString() {
        if (farmModel == null || farmModel.getCurrentTime() == null) {
            return "Informasi hari baru tidak tersedia.";
        }
        GameTime currentTime = farmModel.getCurrentTime();
        return String.format("Sekarang adalah Hari ke-%d, Musim %s, Cuaca %s.",
                             currentTime.getCurrentDay(),
                             currentTime.getCurrentSeason().toString(),
                             currentTime.getCurrentWeather().toString());
    }

    /**
     * Retrieves a list of items available for purchase from the store.
     * @return A list of Item objects or null if an error occurs.
     */
    public List<Item> getStoreItemsForDisplay() {
        if (farmModel == null || farmModel.getStore() == null || farmModel.getItemRegistry() == null || farmModel.getPriceList() == null) {
            System.err.println("Error: Model, Toko, ItemRegistry, atau PriceList null di GameController.getStoreItemsForDisplay.");
            if (gamePanel != null) {
            }
            return Collections.emptyList();
        }
        Store store = farmModel.getStore();
        return store.getAvailableItemsForDisplay(farmModel.getItemRegistry(), farmModel.getPriceList());
    }

    /**
     * Processes a player's request to buy an item from the store.
     * Returns a String message indicating success or the reason for failure.
     * @param itemName The name of the item to buy.
     * @param quantity The quantity of the item to buy.
     * @return A message string detailing the outcome.
     */
    public String requestBuyItemAndGetMessage(String itemName, int quantity) {
        if (farmModel == null || farmModel.getPlayer() == null || farmModel.getStore() == null || farmModel.getPriceList() == null || farmModel.getItemRegistry() == null) {
            return "Gagal: Sistem toko tidak tersedia atau data tidak lengkap.";
        }
        Player player = farmModel.getPlayer();
        Store store = farmModel.getStore();
        PriceList priceList = farmModel.getPriceList();
        Map<String, Item> itemRegistry = farmModel.getItemRegistry();
        Item itemToBuy = itemRegistry.get(itemName);

        if (itemToBuy == null) {
            return "Gagal: Item '" + itemName + "' tidak ditemukan.";
        }

        if (quantity <= 0) {
            return "Gagal: Jumlah pembelian harus lebih dari 0.";
        }

        int buyPrice = priceList.getBuyPrice(itemName);
        if (buyPrice == -1) { 
            return "Gagal: Item '" + itemName + "' tidak dapat dibeli atau tidak dijual.";
        }
        
        boolean foundInStoreDisplayList = false;
        List<Item> displayItems = store.getAvailableItemsForDisplay(itemRegistry, priceList);
        for (Item displayItem : displayItems) {
            if (displayItem.getName().equals(itemName)) {
                foundInStoreDisplayList = true;
                break;
            }
        }
        if (!foundInStoreDisplayList) {
            if (buyPrice <= 0 && buyPrice != -1) {
                 
            } else if (buyPrice == -1) { 
                return "Gagal: Item '" + itemName + "' tidak terdaftar untuk dijual (kode: C01).";
            }
        }


        int totalPrice = buyPrice * quantity;

        if (player.getGold() < totalPrice) {
            return "Gagal: Gold tidak cukup. Butuh " + totalPrice + "G, kamu punya " + player.getGold() + "G.";
        }
        
        boolean success = store.sellToPlayer(player, itemToBuy, quantity, priceList, itemRegistry);

        if (success) {
            if (farmModel.getStatistics() != null && totalPrice > 0) {
                farmModel.getStatistics().recordExpenditure(totalPrice, farmModel.getCurrentTime().getCurrentSeason());
                System.out.println("Recorded expenditure: " + totalPrice + "g for " + quantity + " " + itemToBuy.getName());
            }
            
            if (gamePanel != null) {
                 gamePanel.updatePlayerInfoPanel();
            }
            String priceString = totalPrice == 0 ? "Gratis" : totalPrice + "G";
            return "Berhasil membeli " + quantity + " " + itemToBuy.getName() + " (" + priceString + ").";
        } else {
            return "Gagal membeli item. Mungkin inventory penuh atau item tidak lagi tersedia.";
        }
    }

    /**
     * Processes a player's request to buy an item from the store.
     * @param itemName The name of the item to buy.
     * @param quantity The quantity of the item to buy.
     * @return true if the purchase was successful, false otherwise.
    //  * @deprecated Use requestBuyItemAndGetMessage for detailed feedback.
     */
    // @Deprecated
    public boolean requestBuyItem(String itemName, int quantity) {
        if (farmModel == null || farmModel.getStore() == null || farmModel.getPlayer() == null || 
            farmModel.getPriceList() == null || farmModel.getItemRegistry() == null) {
            System.err.println("GameController: Critical model component is null. Cannot process purchase.");
            return false;
        }
        if (quantity <= 0) {
            System.err.println("GameController: Quantity must be positive.");
            return false;
        }

        Store store = farmModel.getStore();
        Player player = farmModel.getPlayer();
        PriceList priceList = farmModel.getPriceList();
        Map<String, Item> itemRegistry = farmModel.getItemRegistry();

        Item itemToBuy = itemRegistry.get(itemName);
        if (itemToBuy == null) {
            System.err.println("GameController: Item '" + itemName + "' not found in registry.");
            return false;
        }

        boolean success = store.sellToPlayer(player, itemToBuy, quantity, priceList, itemRegistry);
        if (success) {
            System.out.println("Purchased " + quantity + " of " + itemName);

            int buyPrice = priceList.getBuyPrice(itemName);
            int totalPrice = buyPrice * quantity;
            if (farmModel.getStatistics() != null && totalPrice > 0) {
                farmModel.getStatistics().recordExpenditure(totalPrice, farmModel.getCurrentTime().getCurrentSeason());
                System.out.println("Recorded expenditure: " + totalPrice + "g for " + quantity + " " + itemToBuy.getName());
            }

            
            if (itemToBuy instanceof Food) { 
                String eventKey = null;

                if (itemName.equalsIgnoreCase("Fish n' Chips")) {
                    eventKey = "BOUGHT_RECIPE_FISH_N'_CHIPS"; 
                } else if (itemName.equalsIgnoreCase("Fish Sandwich")) {
                    eventKey = "BOUGHT_RECIPE_FISH_SANDWICH"; 
                }

                if (eventKey != null && farmModel.getStatistics() != null) {
                    farmModel.getStatistics().recordKeyEventOrItem(eventKey);
                    System.out.println("Recipe unlock event recorded: " + eventKey);
                }
            }
            if (gamePanel != null) {
                gamePanel.updatePlayerInfoPanel();
            }
        }
        return success;
    }

    /**
     * Handles the player's request to sell an item to the Shipping Bin.
     * @param itemName The name of the item to sell.
     * @param quantity The quantity to sell.
     * @return true if the item was successfully placed in the bin, false otherwise.
     */
    public boolean requestSellItemToBin(String itemName, int quantity) {
        if (farmModel == null || farmModel.getPlayer() == null || farmModel.getShippingBin() == null || 
            farmModel.getItemRegistry() == null || farmModel.getCurrentTime() == null) {
            System.err.println("GameController: Critical model component is null. Cannot process sell request.");
            return false;
        }
        if (itemName == null || itemName.isBlank()){
            System.err.println("GameController: Item name cannot be null or blank for selling.");
            return false;
        }
        if (quantity <= 0) {
            System.err.println("GameController: Quantity to sell must be positive.");
            return false;
        }

        Player player = farmModel.getPlayer();
        ShippingBin shippingBin = farmModel.getShippingBin();
        Map<String, Item> itemRegistry = farmModel.getItemRegistry();
        GameTime gameTime = farmModel.getCurrentTime();

        Item itemToSell = itemRegistry.get(itemName);
        if (itemToSell == null) {
            System.err.println("GameController: Item '" + itemName + "' not found in registry for selling.");
            return false;
        }

        if (!shippingBin.canSellToday()) {
            System.out.println("GameController: Cannot sell today, already sold items via Shipping Bin.");
            return false;
        }

        boolean sold = player.sellItemToBin(itemToSell, quantity, shippingBin, gameTime.getCurrentDay());
        if (sold) {
            System.out.println("GameController: Request to sell " + quantity + " of " + itemName + " processed.");
        }
        return sold;
    }

    /**
     * Mengambil daftar semua Item yang dimiliki pemain di inventory.
     * Berguna untuk ditampilkan di UI atau untuk mekanisme pemilihan item.
     * @return List dari Item, atau list kosong jika tidak ada.
     */
    public List<Item> getPlayerInventoryItems() {
        if (farmModel != null && farmModel.getPlayer() != null && farmModel.getPlayer().getInventory() != null) {
            return new ArrayList<>(farmModel.getPlayer().getInventory().getItems().keySet()); 
        }
            return new ArrayList<>();
    }

    /**
     * Memilih item berikutnya dari daftar item di inventory pemain.
     */
    public void selectNextItem() {
        if (farmModel == null || farmModel.getPlayer() == null) return;
        Player player = farmModel.getPlayer();
        List<Item> allItems = getPlayerInventoryItems();

        if (allItems.isEmpty()) {
            player.setSelectedItem(null); 
            System.out.println("Inventory kosong. Tidak ada item untuk dipilih.");
            return;
        }

        Item currentSelectedItem = player.getSelectedItem();
        int currentIndex = -1;
        if (currentSelectedItem != null) {
            for (int i = 0; i < allItems.size(); i++) {
                if (allItems.get(i).equals(currentSelectedItem)) {
                    currentIndex = i;
                    break;
                }
            }
        }

        int nextIndex = (currentIndex + 1) % allItems.size();
        player.setSelectedItem(allItems.get(nextIndex));
        System.out.println("Selected item: " + player.getSelectedItem().getName());
    }

    /**
     * Memilih item sebelumnya dari daftar item di inventory pemain.
     */
    public void selectPreviousItem() {
        if (farmModel == null || farmModel.getPlayer() == null) return;
        Player player = farmModel.getPlayer();
        List<Item> allItems = getPlayerInventoryItems(); 

        if (allItems.isEmpty()) {
            player.setSelectedItem(null); 
            System.out.println("Inventory kosong. Tidak ada item untuk dipilih.");
            return;
        }

        Item currentSelectedItem = player.getSelectedItem();
        int currentIndex = -1;
        if (currentSelectedItem != null) {
            for (int i = 0; i < allItems.size(); i++) {
                if (allItems.get(i).equals(currentSelectedItem)) {
                    currentIndex = i;
                    break;
                }
            }
        }
        
        int prevIndex;
        if (currentIndex <= 0) { 
            prevIndex = allItems.size() - 1;
        } else {
            prevIndex = currentIndex - 1;
        }
        player.setSelectedItem(allItems.get(prevIndex));
        System.out.println("Selected item: " + player.getSelectedItem().getName());
    }

    /**
     * Mengambil daftar Equipment (alat) yang dimiliki pemain.
     * @return List dari Equipment, atau list kosong jika tidak ada.
     */
    public List<Equipment> getPlayerTools() {
        List<Equipment> tools = new ArrayList<>();
        Player player = farmModel.getPlayer();
        if (player == null || player.getInventory() == null || player.getInventory().getItems() == null) {
            return tools; 
        }
        if (player.getInventory() != null && player.getInventory().getItems() != null) {
            for (Item item : player.getInventory().getItems().keySet()) {
                if (item instanceof Equipment) {
                    tools.add((Equipment) item);
                }
            }
        }
        Collections.sort(tools, Comparator.comparing(Item::getName));
        return tools;
    }

    private Point findSafeSpawnPoint(MapArea map, int preferredX, int preferredY) {
        if (map == null) {
            System.err.println("findSafeSpawnPoint: Map is null, returning preferred coordinates as fallback (1,1).");
            return new Point(1, 1); 
        }

        Tile preferredTile = map.getTile(preferredX, preferredY);
        if (preferredTile != null && isTileWalkableForSpawn(preferredTile, map, preferredX, preferredY)) {
            return new Point(preferredX, preferredY);
        }

        int[][] offsets = {
            {0, -1}, {0, 1}, {1, 0}, {-1, 0}, // N, S, E, W
            {1, -1}, {-1, -1}, {1, 1}, {-1, 1}  // NE, NW, SE, SW
        };

        for (int[] offset : offsets) {
            int checkX = preferredX + offset[0];
            int checkY = preferredY + offset[1];

            if (map.isWithinBounds(checkX, checkY)) {
                Tile adjacentTile = map.getTile(checkX, checkY);
                if (adjacentTile != null && isTileWalkableForSpawn(adjacentTile, map, checkX, checkY)) {
                    System.out.println("Safe spawn found at adjacent tile: (" + checkX + ", " + checkY + ") for preferred: (" + preferredX + ", " + preferredY + ") on map " + map.getName());
                    return new Point(checkX, checkY);
                }
            }
        }

        System.out.println("findSafeSpawnPoint: Preferred ("+preferredX+","+preferredY+") and adjacent tiles unsafe on map " + map.getName() + ". Checking map entry points.");

        List<Point> entryPoints = map.getEntryPoints();
        if (entryPoints != null && !entryPoints.isEmpty()) {
            for (Point entry : entryPoints) {
                if (map.isWithinBounds(entry.x, entry.y)) {
                    Tile entryTile = map.getTile(entry.x, entry.y);
                    if (entryTile != null && isTileWalkableForSpawn(entryTile, map, entry.x, entry.y)) {
                        System.out.println("Safe spawn found at map entry point: (" + entry.x + ", " + entry.y + ") on map " + map.getName());
                        return entry;
                    }
                }
            }
            System.err.println("findSafeSpawnPoint: No walkable entry point found on map " + map.getName() + ". Fallback to first entry point or (1,1).");
            if (!entryPoints.isEmpty()) {
                Point firstEntryPoint = entryPoints.get(0);
                Tile firstEntryTile = map.getTile(firstEntryPoint.x, firstEntryPoint.y);
                if (firstEntryTile != null && isTileWalkableForSpawn(firstEntryTile, map, firstEntryPoint.x, firstEntryPoint.y)) {
                    return firstEntryPoint;
                }
            }
        }

        System.err.println("findSafeSpawnPoint: No safe adjacent tile and no suitable entry points defined/walkable for map " + map.getName() + ". Iterating all GRASS tiles as last resort.");
        if (map.getTiles() != null) {
            Tile[][] allTiles = map.getTiles();
            for (int r = 0; r < allTiles.length; r++) {
                for (int c = 0; c < allTiles[r].length; c++) {
                    if (allTiles[r][c] != null && isTileWalkableForSpawn(allTiles[r][c], map, c, r)) {
                        System.out.println("Safe spawn found via full map scan (GRASS/WALKABLE_SPAWN): (" + c + ", " + r + ") on map " + map.getName());
                        return new Point(c, r);
                    }
                }
            }
        }
        
        System.err.println("ULTIMATE FALLBACK: No safe spawn point found anywhere on map " + map.getName() + ". Returning absolute fallback (1,1).");
        return new Point(1, 1); 
    }

    /**
     * Handles the player's request to visit a new location.
     * 
     * @param destination The LocationType of the destination map.
     * @return true if the visit was successful and map changed, false otherwise.
     */
    public boolean requestVisit(LocationType destination) {
        if (farmModel == null || farmModel.getPlayer() == null || farmModel.getCurrentTime() == null) {
            System.err.println("GameController: Critical model component null, cannot process visit request.");
            return false;
        }

        Player player = farmModel.getPlayer();
        GameTime gameTime = farmModel.getCurrentTime();
        MapArea currentMap = player.getCurrentMap();
        MapArea targetMap = farmModel.getMapArea(destination);

        if (targetMap == null) {
            System.err.println("GameController: Target map for destination " + destination + " is null.");
            if (gamePanel != null) {
                javax.swing.JOptionPane.showMessageDialog(gamePanel, 
                    "The location '" + destination.toString() + "' is not accessible yet.", 
                    "Cannot Visit", 
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            }
            return false;
        }

        int preferredX = 0; 
        int preferredY = 0; 

        List<Point> targetEntryPoints = targetMap.getEntryPoints();

        if (destination == LocationType.FARM) {
            if (targetEntryPoints != null && !targetEntryPoints.isEmpty()) {
                preferredX = targetEntryPoints.get(0).x;
                preferredY = targetEntryPoints.get(0).y;
            } else {
                preferredX = targetMap.getSize().width / 2; 
                preferredY = targetMap.getSize().height / 2;
        }
        } else if (currentMap instanceof FarmMap) {
            int playerExitX = player.getCurrentTileX();
            int farmMapWidth = currentMap.getSize().width;

            if (targetEntryPoints != null && !targetEntryPoints.isEmpty()) {
                if (playerExitX == 0 && targetMap.getName().equalsIgnoreCase("Forest River")) { 
                    Point entry = targetEntryPoints.stream().filter(p -> p.x == targetMap.getSize().width -1).findFirst().orElse(targetEntryPoints.get(0));
                    preferredX = entry.x; preferredY = entry.y;
                } else if (playerExitX >= farmMapWidth - 1  && targetMap.getName().equalsIgnoreCase("Forest River")) { 
                    Point entry = targetEntryPoints.stream().filter(p -> p.x == 0).findFirst().orElse(targetEntryPoints.get(0));
                    preferredX = entry.x; preferredY = entry.y;
                } else if (targetEntryPoints.size() > 0) { 
                     preferredX = targetEntryPoints.get(0).x;
                     preferredY = targetEntryPoints.get(0).y;
                } else { 
                    preferredX = targetMap.getSize().width / 2;
                    preferredY = targetMap.getSize().height / 2;
                }
            } else { 
                preferredX = targetMap.getSize().width / 2;
                preferredY = targetMap.getSize().height / 2;
            }
        } else {
            if (targetEntryPoints != null && !targetEntryPoints.isEmpty()) {
                preferredX = targetEntryPoints.get(0).x;
                preferredY = targetEntryPoints.get(0).y;
            } else {
                preferredX = targetMap.getSize().width / 2;
                preferredY = targetMap.getSize().height / 2;
            }
        }
        
        if (preferredX >= targetMap.getSize().width) preferredX = targetMap.getSize().width - 1;
        if (preferredY >= targetMap.getSize().height) preferredY = targetMap.getSize().height - 1;
        if (preferredX < 0) preferredX = 0;
        if (preferredY < 0) preferredY = 0;

        Point safeSpawn = findSafeSpawnPoint(targetMap, preferredX, preferredY);
        int targetX = safeSpawn.x;
        int targetY = safeSpawn.y;

        boolean visited = player.visit(targetMap, targetX, targetY);

        if (visited) {
            System.out.println("Player visited " + destination + ". New map: " + targetMap.getName());
            player.changeEnergy(-10); 
            gameTime.advance(15);  

            if (gamePanel != null) {
                gamePanel.repaint(); 
            }
            checkPassOut(); 
            return true;
        } else {
            System.err.println("GameController: player.visit() returned false for " + destination);
             if (gamePanel != null) {
                javax.swing.JOptionPane.showMessageDialog(gamePanel, 
                    "Failed to move to '" + destination.toString() + "'.", 
                    "Visit Failed", 
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }
    }

    /**
     * Handles a fishing request from the player.
     * Checks if the player has a fishing rod selected, is near water, and has enough energy.
     * Implements the fishing minigame with RNG as specified.
     * 
     * @return true if fishing action was processed, false otherwise
     */
    public boolean requestFish() {
        if (farmModel == null) {
            System.err.println("GameController: Farm model is null, cannot process fishing action.");
            return false;
        }
        
        Player player = farmModel.getPlayer();
        GameTime gameTime = farmModel.getCurrentTime();
        Map<String, Item> itemRegistry = farmModel.getItemRegistry();
        
        if (player == null || gameTime == null || itemRegistry == null) {
            System.err.println("GameController: Critical components null, cannot process fishing action.");
            return false;
        }
        
        if (player.getEnergy() <= Player.MIN_ENERGY) {
            System.out.println("Player is too tired to fish.");
            return false;
        }
        
        Item selectedItem = player.getSelectedItem();
        if (selectedItem == null || !selectedItem.getName().equals("Fishing Rod")) {
            System.out.println("You need to select the Fishing Rod to fish.");
            return false;
        }
        
        MapArea currentMap = player.getCurrentMap();
        LocationType fishingLocation = null;
        
        if (currentMap instanceof FarmMap) {
            boolean nearPond = isPlayerNearWater(player, (FarmMap)currentMap);
            if (nearPond) {
                fishingLocation = LocationType.POND;
            } else {
                System.out.println("You need to be near water to fish.");
                return false;
            }
        } else {
            String mapName = currentMap.getName();
            if (mapName.contains("Forest River")) {
                fishingLocation = LocationType.FOREST_RIVER;
            } else if (mapName.contains("Mountain Lake")) {
                fishingLocation = LocationType.MOUNTAIN_LAKE;
            } else if (mapName.contains("Ocean")) {
                fishingLocation = LocationType.OCEAN;
            } else {
                System.out.println("You can't fish here.");
                return false;
            }
            
            Tile playerTile = currentMap.getTile(player.getCurrentTileX(), player.getCurrentTileY());
            if (playerTile == null || !isNearWaterTile(player, currentMap)) {
                System.out.println("You need to be near water to fish.");
                return false;
            }
        }
        
        player.changeEnergy(-5); 
        gameTime.advance(15); 
        
        if (gamePanel != null) {
            return startFishingMinigame(fishingLocation);
        } else {
            System.err.println("GameController: gamePanel is null, cannot show fishing minigame dialog.");
            return false;
        }
    }
    
    /**
     * Checks if player is near water on a map.
     * For FarmMap, this checks if the player is adjacent to a pond.
     */
    private boolean isPlayerNearWater(Player player, FarmMap farmMap) {
        int playerX = player.getCurrentTileX();
        int playerY = player.getCurrentTileY();
        
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        
        for (int[] dir : directions) {
            int checkX = playerX + dir[0];
            int checkY = playerY + dir[1];
            
            if (farmMap.isWithinBounds(checkX, checkY)) {
                Tile adjacentTile = farmMap.getTile(checkX, checkY);
                if (adjacentTile != null && adjacentTile.getType() == TileType.WATER) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Checks if player is near water on a general map.
     */
    private boolean isNearWaterTile(Player player, MapArea map) {
        int playerX = player.getCurrentTileX();
        int playerY = player.getCurrentTileY();
        
        Tile playerTile = map.getTile(playerX, playerY);
        if (playerTile != null && playerTile.getType() == TileType.WATER) {
            return true;
        }
        
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        
        for (int[] dir : directions) {
            int checkX = playerX + dir[0];
            int checkY = playerY + dir[1];
            
            if (map.isWithinBounds(checkX, checkY)) {
                Tile adjacentTile = map.getTile(checkX, checkY);
                if (adjacentTile != null && adjacentTile.getType() == TileType.WATER) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Implements the fishing minigame with RNG.
     * Shows a dialog for player to guess a number, with difficulty based on fish type.
     */
    private boolean startFishingMinigame(LocationType fishingLocation) {
        Season currentSeason = farmModel.getCurrentTime().getCurrentSeason();
        Weather currentWeather = farmModel.getCurrentTime().getCurrentWeather();
        int currentHour = farmModel.getCurrentTime().getHour();
        
        boolean canCatchLegendary = canCatchLegendaryFish(fishingLocation, currentSeason, currentHour, currentWeather);
        boolean canCatchRegular = canCatchRegularFish(fishingLocation, currentSeason, currentHour, currentWeather);
        
        String fishType = "Common";
        int maxGuess = 10;    
        int maxAttempts = 10; 
        
        Random rng = new Random();
        if (canCatchLegendary && rng.nextDouble() < 0.05) { 
            fishType = "Legendary";
            maxGuess = 500;    
            maxAttempts = 7;   
        } else if (canCatchRegular && rng.nextDouble() < 0.3) { 
            fishType = "Regular";
            maxGuess = 100;    
            maxAttempts = 10; 
        }
        
        int targetNumber = rng.nextInt(maxGuess) + 1; 

        if ("Legendary".equals(fishType)) {
            System.out.println("[SPOILER] Legendary Fish attempt at: " + fishingLocation + ". Target Number: " + targetNumber);
        }
        
        if (gamePanel == null) {
            System.err.println("GameController: gamePanel is null, cannot show fishing dialog.");
            return false;
        }
        
        String fishingMessage = "You're fishing for a " + fishType + " fish!\n" +
                               "Guess the number between 1 and " + maxGuess + ".\n" +
                               "You have " + maxAttempts + " attempts.";
        
        boolean caughtFish = false;
        int attemptsLeft = maxAttempts;
        
        while (attemptsLeft > 0) {
            String guessStr = JOptionPane.showInputDialog(gamePanel, 
                fishingMessage + "\nAttempts left: " + attemptsLeft + "\nEnter your guess (or cancel to stop fishing) (DEBUG, answer):" + targetNumber);
            
            if (guessStr == null) {
                
                System.out.println("Fishing canceled.");
                return true; 
            }
            
            try {
                int guess = Integer.parseInt(guessStr);
                
                if (guess == targetNumber) {
                    caughtFish = true;
                    break;
                } else if (guess < targetNumber) {
                    JOptionPane.showMessageDialog(gamePanel, "Higher than " + guess + "!");
                } else {
                    JOptionPane.showMessageDialog(gamePanel, "Lower than " + guess + "!");
                }
                
                attemptsLeft--;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(gamePanel, "Please enter a valid number.");
            }
        }
        
        if (caughtFish) {
            Item itemCaught = generateRandomFish(fishType, fishingLocation, currentSeason, currentWeather);
            if (itemCaught != null && itemCaught instanceof Fish) { 
                Fish fishCaughtObject = (Fish) itemCaught;
                farmModel.getPlayer().getInventory().addItem(fishCaughtObject, 1);
                
                if (farmModel.getStatistics() != null) {
                    EndGameStatistics stats = farmModel.getStatistics();
                    
                    System.out.println("BEFORE recording fish: " + stats.getTotalFishCaughtCount() + " total fish caught");
                    stats.recordFishCatch(fishCaughtObject.getName(), fishCaughtObject.getRarity());
                    System.out.println("AFTER recording fish: " + stats.getTotalFishCaughtCount() + " total fish caught");
                    
                    if (gamePanel != null) {
                        JOptionPane.showMessageDialog(gamePanel, "You caught a " + fishCaughtObject.getName() + "!");
                        
                        refreshStatisticsData();
                    } else {
                        System.out.println("You caught a " + fishCaughtObject.getName() + "!");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(gamePanel, "You caught a fish, but it got away!");
            }
        } else {
            JOptionPane.showMessageDialog(gamePanel, "The fish got away! Better luck next time.");
        }
        
        checkPassOut();
        
        return true;
    }
    
    /**
     * Checks if legendary fish can be caught based on location, season, time, and weather.
     */
    private boolean canCatchLegendaryFish(LocationType location, Season season, int hour, Weather weather) {
        switch (location) {
            case POND:
                return season == Season.FALL && hour >= 8 && hour <= 20;
            case OCEAN:
                return season == Season.SUMMER && hour >= 8 && hour <= 20;
            case FOREST_RIVER:
                return season == Season.WINTER && hour >= 8 && hour <= 20;
            case MOUNTAIN_LAKE:
                return season == Season.SPRING && hour >= 8 && hour <= 20 && weather == Weather.RAINY;
            default:
                return false;
        }
    }
    
    /**
     * Checks if regular fish can be caught based on location, season, time, and weather.
     */
    private boolean canCatchRegularFish(LocationType location, Season season, int hour, Weather weather) {
        if (farmModel == null || farmModel.getItemRegistry() == null || farmModel.getCurrentTime() == null) {
            return false; 
        }
        GameTime currentTime = farmModel.getCurrentTime();

        for (Item item : farmModel.getItemRegistry().values()) {
            if (item instanceof Fish) {
                Fish fish = (Fish) item;
                if (fish.getRarity() == FishRarity.REGULAR && fish.canBeCaught(season, currentTime, weather, location)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Generates a random fish based on the type and conditions.
     */
    private Item generateRandomFish(String fishType, LocationType location, Season season, Weather weather) {
        if (farmModel == null || farmModel.getItemRegistry() == null || farmModel.getCurrentTime() == null) {
            return null;
        }
        GameTime currentTime = farmModel.getCurrentTime(); 

        Map<String, Item> itemRegistry = farmModel.getItemRegistry();
        List<Fish> catchableFish = new ArrayList<>();

        // 1. Filter all fish that can be caught under current conditions
        for (Item item : itemRegistry.values()) {
            if (item instanceof Fish) {
                Fish fish = (Fish) item;
                if (fish.canBeCaught(season, currentTime, weather, location)) {
                    catchableFish.add(fish);
                }
            }
        }

        if (catchableFish.isEmpty()) {
            return null; 
        }

        // 2. Filter by the determined fishType (rarity)
        List<Fish> fishOfTargetRarity = new ArrayList<>();
        FishRarity targetRarity = null;
        if ("Legendary".equalsIgnoreCase(fishType)) {
            targetRarity = FishRarity.LEGENDARY;
        } else if ("Regular".equalsIgnoreCase(fishType)) {
            targetRarity = FishRarity.REGULAR;
        } else if ("Common".equalsIgnoreCase(fishType)) {
            targetRarity = FishRarity.COMMON;
        }

        if (targetRarity != null) {
            for (Fish fish : catchableFish) {
                if (fish.getRarity() == targetRarity) {
                    fishOfTargetRarity.add(fish);
                }
            }
        }
        
        List<Fish> listToPickFrom = fishOfTargetRarity.isEmpty() ? catchableFish : fishOfTargetRarity;

        if (listToPickFrom.isEmpty()) {
            return null;
        }

        Random rng = new Random();
        return listToPickFrom.get(rng.nextInt(listToPickFrom.size()));
    }

    /**
     * Menangani permintaan pemain untuk memasak.
     * Ini akan menampilkan UI untuk memilih resep dan bahan bakar,
     * kemudian memanggil Player.cook() dan menangani hasilnya.
     */
    public void handleCookRequest() {
        if (farmModel == null || gamePanel == null) {
            if (gamePanel != null) gamePanel.displayMessage("Sistem memasak belum siap atau ada data yang hilang.");
            System.err.println("GameController.handleCookRequest: Komponen penting null.");
            return;
        }
        
        Player player = farmModel.getPlayer();
        GameTime gameTime = farmModel.getCurrentTime();
        Map<String, Item> itemRegistry = farmModel.getItemRegistry();
        List<Recipe> availableRecipes = farmModel.getRecipes();

        if (player == null || gameTime == null || itemRegistry == null) {
            gamePanel.displayMessage("Error internal: Data pemain, waktu, atau item tidak lengkap.");
            System.err.println("GameController.handleCookRequest: Player, GameTime, atau ItemRegistry adalah null.");
            return;
        }

        if (availableRecipes == null) { 
            gamePanel.displayMessage("Daftar resep tidak tersedia saat ini.");
            System.err.println("GameController.handleCookRequest: farmModel.getRecipes() mengembalikan null.");
            return;
        }

        // KONDISI LOKASI MEMASAK (Spesifikasi hal 29)
        // Player must be in the PlayerHouseInterior map OR on FarmMap adjacent to a House object
        boolean canCookLocation = false;
        if (player.getCurrentMap() instanceof FarmMap) {
            FarmMap farmMap = (FarmMap) player.getCurrentMap();
            if (farmMap.getObjectAt(player.getCurrentTileX(), player.getCurrentTileY()) instanceof House) {
                canCookLocation = true;
            }
        }

        if (!canCookLocation) {
            gamePanel.displayMessage("Kamu hanya bisa memasak di dalam rumahmu.");
            return;
        }

        if (availableRecipes.isEmpty()) {
            gamePanel.displayMessage("Tidak ada resep yang tersedia saat ini.");
            return;
        }

        // BAGIAN UI UNTUK MEMILIH RESEP (Contoh dengan JOptionPane) 
        List<String> unlockedRecipeNames = new ArrayList<>();
        for (Recipe r : availableRecipes) {
            if (r.isUnlocked(farmModel.getStatistics(), player)) {
                unlockedRecipeNames.add(r.getName());
            }
        }

        if (unlockedRecipeNames.isEmpty()) {
            gamePanel.displayMessage("Kamu belum membuka resep apapun!");
            return;
        }

        String chosenRecipeName = (String) JOptionPane.showInputDialog(
                gamePanel,
                "Pilih resep yang ingin dimasak:",
                "Memasak",
                JOptionPane.PLAIN_MESSAGE,
                null,
                unlockedRecipeNames.toArray(),
                unlockedRecipeNames.get(0)
        );

        if (chosenRecipeName == null) return; 

        Recipe selectedRecipe = null;
        for (Recipe r : availableRecipes) {
            if (r.getName().equals(chosenRecipeName)) {
                selectedRecipe = r;
                break;
            }
        }
        if (selectedRecipe == null) { 
            gamePanel.displayMessage("Resep tidak valid.");
            return;
        }

        // BAGIAN UI UNTUK MEMILIH BAHAN BAKAR (Contoh dengan JOptionPane) 
        List<String> availableFuelOptions = new ArrayList<>();
        Item coalItem = itemRegistry.get("Coal");
        Item firewoodItem = itemRegistry.get("Firewood");

        if (coalItem != null && player.getInventory().hasItem(coalItem, 1)) {
            availableFuelOptions.add("Coal");
        }
        if (firewoodItem != null && player.getInventory().hasItem(firewoodItem, 1)) {
            availableFuelOptions.add("Firewood");
        }

        if (availableFuelOptions.isEmpty()) {
            gamePanel.displayMessage("Kamu tidak memiliki bahan bakar (Coal/Firewood) untuk memasak.");
            return;
        }
        
        String chosenFuelName = (String) JOptionPane.showInputDialog(
                gamePanel,
                "Pilih bahan bakar:",
                "Bahan Bakar Memasak",
                JOptionPane.PLAIN_MESSAGE,
                null,
                availableFuelOptions.toArray(),
                availableFuelOptions.get(0)
        );

        if (chosenFuelName == null) return; 

        Item selectedFuelItem = itemRegistry.get(chosenFuelName);
        if (selectedFuelItem == null) { 
            gamePanel.displayMessage("Bahan bakar tidak valid.");
            return;
        }
        
        // EFEK INISIASI & MEMANGGIL PLAYER.COOK() 
        final int COOK_ENERGY_COST = 10;
        if (player.getEnergy() < COOK_ENERGY_COST) {
            gamePanel.displayMessage("Energi tidak cukup untuk memulai memasak (butuh " + COOK_ENERGY_COST + ").");
            return;
        }
        player.changeEnergy(-COOK_ENERGY_COST); 

        String cookResultOutcome = player.cook(selectedRecipe, selectedFuelItem, itemRegistry);

        if (cookResultOutcome != null && itemRegistry.containsKey(cookResultOutcome)) {
            Item foodProduct = itemRegistry.get(cookResultOutcome);
            int servingsMade = 1;

            // Logika efisiensi Coal
            if (selectedFuelItem.getName().equals("Coal")) {
                boolean canMakeSecondServing = true;
                for (Map.Entry<String, Integer> entry : selectedRecipe.getIngredients().entrySet()) {
                    Item ingredient = itemRegistry.get(entry.getKey());
                    if (ingredient == null || !player.getInventory().hasItem(ingredient, entry.getValue())) {
                        canMakeSecondServing = false;
                        break;
                    }
                }

                if (canMakeSecondServing) {
                    for (Map.Entry<String, Integer> entry : selectedRecipe.getIngredients().entrySet()) {
                        Item ingredient = itemRegistry.get(entry.getKey());
                        player.getInventory().removeItem(ingredient, entry.getValue());
                    }
                    servingsMade = 2;
                    gamePanel.displayMessage("Dengan Coal, kamu membuat porsi ganda!");
                } else {
                    gamePanel.displayMessage("Kamu menggunakan Coal, tapi tidak cukup bahan untuk porsi kedua.");
                }
            }

            gamePanel.displayMessage("Kamu mulai memasak " + servingsMade + " " + cookResultOutcome + ". Akan selesai dalam 1 jam.");
            System.out.println(player.getName() + " mulai memasak " + servingsMade + " " + cookResultOutcome + ". Energi: " + player.getEnergy() + ", Waktu: " + gameTime.getTimeString());

            // Simulasi selesai masak setelah 1 jam
            final int finalServings = servingsMade;

            for (int i = 0; i < finalServings; i++) {
                 player.getInventory().addItem(foodProduct, 1); // Tambah item sejumlah porsi
            }
            gameTime.advance(60); // Majukan waktu 1 jam SEKALI untuk seluruh batch
            gamePanel.displayMessage(finalServings + " " + cookResultOutcome + " sudah matang!");


            System.out.println("Inventory: " + player.getInventory().toString());

        } else {
            // Ada pesan error dari player.cook(), tampilkan
            gamePanel.displayMessage("Gagal memasak: " + cookResultOutcome); // cookResultOutcome akan berisi pesan error
            System.out.println(player.getName() + " gagal memasak. Alasan: " + cookResultOutcome + ". Energi: " + player.getEnergy());
            // Kembalikan energi jika gagal di tahap persiapan bahan oleh player.cook (misal kurang bahan)
            // Jika energi dikurangi SEBELUM player.cook(), maka tidak perlu dikembalikan di sini.
            // Berdasarkan implementasi kita, energi dikurangi SEBELUM player.cook(), jadi jika player.cook gagal
            // karena kurang bahan, energi tetap terkurang untuk "percobaan". Ini sesuai dengan "10 energi untuk tiap percobaan memasak".
        }

        if (gamePanel != null) {
            gamePanel.updatePlayerInfoPanel();
            gamePanel.updateGameRender();
        }
        checkPassOut();
    }

    /**
     * Handles the player's request to chat with a nearby NPC.
     * It identifies an NPC the player might be facing or is very close to.
     */
    public void handleChatRequest() {
        if (farmModel == null || gamePanel == null) {
            System.err.println("GameController: Farm model or GamePanel is null. Cannot handle chat request.");
            if (gamePanel != null) gamePanel.displayMessage("Error: Model atau Panel tidak siap untuk chat.");
            return;
        }
        Player player = farmModel.getPlayer();
        GameTime gameTime = farmModel.getCurrentTime();

        if (player == null || gameTime == null) {
            System.err.println("GameController: Player or GameTime is null. Cannot handle chat request.");
            if (gamePanel != null) gamePanel.displayMessage("Error: Player atau GameTime tidak siap.");
            return;
        }

        NPC targetNPC = findNearbyNPCForChat(player); 

        if (targetNPC == null) {
            return;
        }

        if (targetNPC.getName().equals("Emily")) {
            String[] options = {"Chat", "Open Store"};
            int choice = gamePanel.showOptionDialog(
                "Kamu bertemu Emily. Apa yang ingin kamu lakukan?",
                "Interaksi dengan Emily",
                options
            );

            if (choice == 0) { // Chat
                MapArea npcCurrentMap = player.getCurrentMap();
                boolean chatSuccess = player.chat(targetNPC, gameTime, npcCurrentMap);
                if (chatSuccess) {
                    String dialogue = targetNPC.getDialogue(player);
                    // KOREKSI DI SINI: Kirim objek NPC, bukan hanya nama
                    gamePanel.showNPCDialogue(targetNPC, dialogue); 
                } else {
                    if (player.getEnergy() < Player.CHAT_ENERGY_COST) {
                        gamePanel.displayMessage(player.getName() + " tidak punya cukup energi untuk berbicara (butuh " + Player.CHAT_ENERGY_COST + ").");
                    }
                }
            } else if (choice == 1) { // Open Store
                gamePanel.openStoreDialog();
            }
        } else {
            MapArea npcCurrentMap = player.getCurrentMap();
            boolean chatSuccess = player.chat(targetNPC, gameTime, npcCurrentMap);

            if (chatSuccess) {
                String dialogue = targetNPC.getDialogue(player);
                // KOREKSI DI SINI: Kirim objek NPC, bukan hanya nama
                gamePanel.showNPCDialogue(targetNPC, dialogue); 
            } else {
                if (player.getEnergy() < Player.CHAT_ENERGY_COST) {
                    gamePanel.displayMessage(player.getName() + " tidak punya cukup energi untuk berbicara (butuh " + Player.CHAT_ENERGY_COST + ").");
                }
            }
        }
        if (gamePanel != null) { 
            gamePanel.updatePlayerInfoPanel();
            gamePanel.updateGameRender();
        }
    }

    /**
     * Finds an NPC within chat range of the player.
     * Displays a message via GamePanel if no NPC is found.
     *
     * @param player The player performing the action.
     * @return The found NPC, or null if no NPC is in range.
     */
    private NPC findNearbyNPCForChat(Player player) {
        if (farmModel == null || farmModel.getNpcs() == null || gamePanel == null || player == null) { 
            System.err.println("GameController.findNearbyNPCForChat: Critical component is null.");
            if (gamePanel != null) gamePanel.displayMessage("Error internal: Tidak bisa mencari NPC.");
            return null;
        }

        MapArea playerMap = player.getCurrentMap();
        if (playerMap == null) {
            System.err.println("GameController.findNearbyNPCForChat: Player's current map is null.");
            if (gamePanel != null) gamePanel.displayMessage("Error: Player tidak berada di map yang valid.");
            return null;
        }
        
        int playerX = player.getCurrentTileX();
        int playerY = player.getCurrentTileY();

        Optional<NPC> nearbyNPCOptional = farmModel.getNpcs().stream()
            .filter(npc -> {
                MapArea npcMapContext = farmModel.getMapArea(npc.getHomeLocation());
                if (npcMapContext != playerMap) {
                    return false; 
                }
                int npcX = npc.getCurrentTileX(); 
                int npcY = npc.getCurrentTileY();
                int distance = Math.abs(playerX - npcX) + Math.abs(playerY - npcY);
                return distance <= Player.CHAT_MAX_DISTANCE;
            })
            .min(Comparator.comparingInt(npc -> { 
                int npcX = npc.getCurrentTileX();
                int npcY = npc.getCurrentTileY();
                return Math.abs(playerX - npcX) + Math.abs(playerY - npcY);
            }
            ));

        if (nearbyNPCOptional.isPresent()) {
            NPC foundNpc = nearbyNPCOptional.get();
            System.out.println("DEBUG: Found nearby NPC: " + foundNpc.getName() + " at (" + foundNpc.getCurrentTileX() + "," + foundNpc.getCurrentTileY() + ") on map " + playerMap.getName());
            return foundNpc;
        } else {
            System.out.println("DEBUG: No NPC found nearby on map " + playerMap.getName() + " at player pos (" + playerX + "," + playerY + ")");
            gamePanel.displayMessage("Tidak ada NPC di dekatmu untuk diajak bicara.");
            return null;
        }
    }

    // START OF handleGiftRequest METHOD
    public void handleGiftRequest() {
        if (farmModel == null || gamePanel == null) {
            System.err.println("GameController.handleGiftRequest: Critical component is null.");
            return;
        }
        Player player = farmModel.getPlayer();
        GameTime gameTime = farmModel.getCurrentTime();

        if (player == null || gameTime == null) {
            System.err.println("GameController.handleGiftRequest: Player or GameTime is null.");
            if (gamePanel != null) gamePanel.displayMessage("Error internal: Tidak bisa melakukan aksi gifting.");
            return;
        }

        final int GIFT_ENERGY_COST = 5;
        if (player.getEnergy() < GIFT_ENERGY_COST) {
            gamePanel.displayMessage("Energi tidak cukup untuk memberi hadiah (butuh " + GIFT_ENERGY_COST + ", punya " + player.getEnergy() + ").");
            return;
        }

        NPC targetNPC = findNearbyNPCForChat(player); 

        if (targetNPC == null) {
            gamePanel.displayMessage("Tidak ada NPC di dekatmu untuk diberi hadiah.");
            return;
        }

        Item itemToGift = player.getSelectedItem();
        if (itemToGift == null) {
            gamePanel.displayMessage("Pilih item dari inventory untuk diberikan.");
            return;
        }
        
        MapArea npcMapContext = farmModel.getMapArea(targetNPC.getHomeLocation());
        if (npcMapContext == null) {
            System.err.println("GameController.handleGiftRequest: Could not determine NPC's map context for gifting.");
            gamePanel.displayMessage("Error: Tidak bisa menemukan lokasi NPC.");
            return;
        }

        boolean gifted = player.gift(targetNPC, itemToGift, gameTime, npcMapContext);

        if (gifted) {
            String reaction = targetNPC.reactToGift(itemToGift, player);
            // KOREKSI DI SINI: Kirim objek NPC, bukan hanya nama
            gamePanel.showNPCDialogue(targetNPC, reaction);  
            System.out.println("Gift successful. NPC: " + targetNPC.getName() + ", Item: " + itemToGift.getName());
        } else {
            // Pesan kegagalan gifting biasanya sudah dihandle di dalam player.gift() atau oleh gamePanel
            System.out.println("Gifting failed. See Player.gift() logs for details (e.g., energy, distance, item possession).");
        }
        
        if (gamePanel != null) {
            gamePanel.updatePlayerInfoPanel();
            gamePanel.updateGameRender();
        }
        checkPassOut();
    }
    // END OF handleGiftRequest METHOD

    public void handleProposeRequest() {
        if (farmModel == null || gamePanel == null) {
            System.err.println("GameController.handleProposeRequest: Critical component (farmModel or gamePanel) is null.");
            return;
        }
        Player player = farmModel.getPlayer();
        GameTime gameTime = farmModel.getCurrentTime(); // Ambil GameTime dari farmModel

        if (player == null || gameTime == null) { // Periksa juga gameTime
            System.err.println("GameController.handleProposeRequest: Player or GameTime is null.");
            if (gamePanel != null) gamePanel.displayMessage("Error internal: Cannot attempt proposal.");
            return;
        }

        NPC targetNPC = findNearbyNPCForChat(player); 

        if (targetNPC == null) {
            gamePanel.displayMessage("Tidak ada NPC di dekatmu untuk dilamar.");
            return;
        }

        Item selectedItem = player.getSelectedItem();
        if (selectedItem == null) {
            gamePanel.displayMessage("Select the Proposal Ring from your inventory first!");
            return;
        }

        if (selectedItem instanceof ProposalRing) {
            ProposalRing ring = (ProposalRing) selectedItem;
            
            // Panggil Item.use() dulu untuk validasi dasar (misal, memastikan target adalah NPC)
            if (ring.use(player, targetNPC)) { // Panggil Item.use()
                // Jika Item.use() mengembalikan true (target valid), baru panggil Player.propose()
                int currentTotalDays = gameTime.getTotalDaysPlayed();
                String proposalMessage = player.propose(targetNPC, ring, currentTotalDays); // Panggil Player.propose() dengan semua argumen

                if (proposalMessage == null) { // Lamaran diterima
                    player.changeEnergy(-10); // Sesuai spesifikasi: -10 energi jika diterima
                    gameTime.advance(60);   // Sesuai spesifikasi: -1 jam (60 menit)
                    gamePanel.displayMessage(player.getName() + " dan " + targetNPC.getName() + " sekarang bertunangan! Waktu maju 1 jam.");
                    System.out.println("Lamaran diterima! Energi player: " + player.getEnergy());
                } else { // Lamaran ditolak
                    player.changeEnergy(-20); // Sesuai spesifikasi: -20 energi jika ditolak
                    gameTime.advance(60);   // Sesuai spesifikasi: -1 jam (60 menit)
                    gamePanel.displayMessage(proposalMessage + " Waktu tetap maju 1 jam.");
                    System.out.println("Lamaran gagal!\nPesan: " + proposalMessage + "\nEnergi player: " + player.getEnergy());
                }
            } else {
                // Pesan error dari ProposalRing.use() sudah dicetak jika target tidak valid
                gamePanel.displayMessage("Proposal Ring tidak bisa digunakan pada target ini."); // Opsional
            }
        } else {
            gamePanel.displayMessage("Kamu harus memegang cincin lamaran untuk melamar.");
        }

        if (gamePanel != null) {
            gamePanel.updatePlayerInfoPanel();
            gamePanel.updateGameRender();
        }
        checkPassOut(); 
    }

    private NPC findTargetNPCForInteraction(Player player, int maxDistance) {
        if (farmModel == null || farmModel.getNpcs() == null || player == null || player.getCurrentMap() == null) {
            System.err.println("GameController.findTargetNPCForInteraction: Komponen kritis null.");
            return null;
        }
        MapArea playerMap = player.getCurrentMap();
        int playerX = player.getCurrentTileX();
        int playerY = player.getCurrentTileY();

        Optional<NPC> nearbyNPCOptional = farmModel.getNpcs().stream()
            .filter(npc -> {
                MapArea npcExpectedMap = farmModel.getMapArea(npc.getHomeLocation());
                if (npcExpectedMap != playerMap) {
                    return false;
                }
                int npcX = npc.getCurrentTileX();
                int npcY = npc.getCurrentTileY();
                int distance = Math.abs(playerX - npcX) + Math.abs(playerY - npcY);
                return distance <= maxDistance;
            })
            .min(Comparator.comparingInt(npc -> {
                int npcX = npc.getCurrentTileX();
                int npcY = npc.getCurrentTileY();
                return Math.abs(playerX - npcX) + Math.abs(playerY - npcY);
            }));
        
        return nearbyNPCOptional.orElse(null);
    }



    public void handleMarryRequest() {
        if (farmModel == null || gamePanel == null) {
            System.err.println("GameController.handleMarryRequest: Critical component null.");
            return;
        }
        Player player = farmModel.getPlayer();
        GameTime gameTime = farmModel.getCurrentTime();
        FarmMap farmMap = farmModel.getFarmMap(); 

        if (player == null || gameTime == null || farmMap == null) {
            System.err.println("GameController.handleMarryRequest: Player, GameTime, or FarmMap is null.");
            if (gamePanel != null) gamePanel.displayMessage("Error internal: Tidak bisa memproses pernikahan.");
            return;
        }

        final int MARRY_ENERGY_COST = 80;
        if (player.getEnergy() < MARRY_ENERGY_COST) {
            gamePanel.displayMessage("Energi tidak cukup untuk menikah. Kamu membutuhkan " + MARRY_ENERGY_COST + " energi, hanya punya " + player.getEnergy() + ".");
            if (gamePanel != null) {
                 gamePanel.updatePlayerInfoPanel();
                 gamePanel.updateGameRender();
            }
            return;
        }

        // 1. Tentukan NPC yang DIDEKATI pemain untuk interaksi
        NPC npcInteraksi = findTargetNPCForInteraction(player, Player.CHAT_MAX_DISTANCE); 

        if (npcInteraksi == null) {
            gamePanel.displayMessage("Kamu tidak berada cukup dekat dengan NPC manapun untuk diajak menikah.");
            return;
        }
        
        // Tidak perlu lagi validasi di controller apakah npcInteraksi adalah tunangan,
        // karena Player.marry() akan menangani semua validasi tersebut dan mengembalikan pesan error jika perlu.
        
        int currentTotalDays = gameTime.getTotalDaysPlayed();
        String marryMessage = player.marry(npcInteraksi, currentTotalDays); // Gunakan npcInteraksi sebagai target

        if (marryMessage == null) { // Sukses menikah (Player.marry() mengembalikan null jika sukses)
            player.changeEnergy(-MARRY_ENERGY_COST);

            int currentHour = gameTime.getHour();
            int currentMinute = gameTime.getMinute();
            int minutesToAdvance = 0;
            if (currentHour < 22) {
                minutesToAdvance = (22 - currentHour) * 60 - currentMinute;
            } else if (currentHour == 22 && currentMinute == 0) {
                minutesToAdvance = 0;
            } else {
                 if (currentHour > 22 || (currentHour == 22 && currentMinute > 0)) {
                    System.out.println("Pernikahan terjadi setelah atau tepat pukul 22:00, waktu tidak di-skip mundur.");
                }
            }
            if (minutesToAdvance > 0) {
                gameTime.advance(minutesToAdvance);
            }

            int homeX = 5; 
            int homeY = 5; 
            player.setCurrentMap(farmMap);
            player.setPosition(homeX, homeY);

            gamePanel.displayMessage("Selamat! Kamu dan " + npcInteraksi.getName() + " telah menikah! Waktu sekarang " + gameTime.getTimeString() + ".");
            System.out.println("Pernikahan berhasil! Energi player: " + player.getEnergy() + ", Waktu: " + gameTime.getTimeString());
            
        } else { 
            // Ada pesan error dari Player.marry()
            gamePanel.displayMessage(marryMessage);
            System.out.println("Pernikahan gagal/tidak valid. Pesan: " + marryMessage);
            // Tidak ada perubahan energi atau waktu jika pernikahan gagal di tahap validasi Player.marry()
        }

        if (gamePanel != null) {
            gamePanel.updatePlayerInfoPanel();
            gamePanel.updateGameRender();
        }
        checkPassOut();
    }    

    public void requestNormalSleep() {
        if (farmModel == null || gamePanel == null) {
            System.err.println("GameController: Critical component null, cannot process normal sleep.");
            return;
        }
        Player player = farmModel.getPlayer();
        if (player == null) {
            System.err.println("GameController: Player is null, cannot process normal sleep.");
            return;
        }

        // Condition: Player must be in their house interior
        if (!(player.getCurrentMap() instanceof FarmMap)) {
            gamePanel.displayMessage("Kamu hanya bisa tidur di dalam rumahmu.");
            return;
        }

        FarmMap farmMap = (FarmMap) player.getCurrentMap();
        if (!(farmMap.getObjectAt(player.getCurrentTileX(), player.getCurrentTileY()) instanceof House)) {
            gamePanel.displayMessage("Kamu hanya bisa tidur di dalam rumahmu.");
            return;
        }

        int energyBeforeSleep = player.getEnergy();
        player.sleep(energyBeforeSleep, false); // false for usedBonusBed for now

        // forceSleepAndProcessNextDay will advance time, update crops, calculate income, etc.
        int incomeFromSales = farmModel.forceSleepAndProcessNextDay(); 

        String eventMessage = player.getName() + " tidur nyenyak."; // Or a different message if energy was low
        // Could refine message based on energyBeforeSleep if desired:
        if (energyBeforeSleep < Player.LOW_ENERGY_THRESHOLD) {
            eventMessage = player.getName() + " tidur dengan energi rendah, tapi berhasil memulihkan diri.";
        }
        String newDayInfo = generateNewDayInfoString(); // Re-use existing helper
        
        if (gamePanel != null) { // Added null check for safety, though it should be set
            gamePanel.stopGameTimer(); // Stop timer before modal dialog
            gamePanel.showEndOfDayMessage(eventMessage, incomeFromSales, newDayInfo);
            gamePanel.startGameTimer(); // Restart timer after modal dialog
        }
       }

    /**
     * Handles the player's request to watch TV to see tomorrow's weather forecast.
     * Player must be in their house.
     */
    public void requestWatchTV() {
        if (farmModel == null || gamePanel == null) {
            System.err.println("GameController: Critical component null, cannot process watch TV request.");
            return;
        }
        Player player = farmModel.getPlayer();
        GameTime gameTime = farmModel.getCurrentTime();

        if (player == null || gameTime == null) {
            System.err.println("GameController: Player or GameTime is null for watching TV.");
            return;
        }

        boolean isInHouse = false;
        if (player.getCurrentMap() instanceof FarmMap) {
            FarmMap farmMap = (FarmMap) player.getCurrentMap();
            if (farmMap.getObjectAt(player.getCurrentTileX(), player.getCurrentTileY()) instanceof House) {
                isInHouse = true;
            }
        }

        if (!isInHouse) {
            gamePanel.displayMessage("You can only watch TV inside your house.");
            return;
        }

        Weather currentDayWeather = gameTime.getCurrentWeather();
        String weatherMessage = "Today's weather: " + currentDayWeather.toString();
        gamePanel.displayMessage(weatherMessage);
        System.out.println("TV: " + weatherMessage);

        gameTime.advance(15); 
        player.changeEnergy(-5); 

        if (gamePanel != null) {
            gamePanel.updatePlayerInfoPanel(); 
            gamePanel.updateGameRender(); 
        }
        checkPassOut(); 
    }

    /**
     * Ensures statistics data is properly refreshed before displaying.
     * This method forces the repaint of the statistics view with fresh data.
     */
    public void refreshStatisticsData() {
        if (farmModel != null && farmModel.getStatistics() != null) {
            System.out.println("CALLED_FROM_GAMEPANEL: GameController.refreshStatisticsData() - Refreshing statistics data:"); 
            System.out.println("- Crops Harvested Count: " + farmModel.getStatistics().getCropsHarvestedCount().size());
            System.out.println("- Unique Fish Caught: " + farmModel.getStatistics().getUniqueFishCaught().size());
            System.out.println("- Total Fish Caught: " + farmModel.getStatistics().getTotalFishCaughtCount());
            
            if (gamePanel != null) {
                gamePanel.repaint();
            }
        }
    }

    /**
     * Requests the display of end-of-game statistics.
     * This will fetch the summary from EndGameStatistics and tell GamePanel to show it.
     * It will also stop the game timer in GamePanel.
     */
    public void requestShowStatistics() {
        if (farmModel != null && farmModel.getStatistics() != null) {
            refreshStatisticsData();
            
            farmModel.setCurrentGameState(GameState.STATISTICS_VIEW);
            if (gamePanel != null) {
                gamePanel.stopGameTimer(); 
                gamePanel.repaint(); 
            }
            System.out.println("GameController: Statistics requested. GameState set to STATISTICS_VIEW.");
        } else {
            System.err.println("GameController: Cannot show statistics - model or statistics object is null.");
            if (gamePanel != null) gamePanel.displayMessage("Error: Statistics not available.");
        }
    }

    /**
     * Gathers player information and requests GamePanel to display it.
     */
    public void requestViewPlayerInfo() {
        if (farmModel != null && farmModel.getPlayer() != null) {
            farmModel.setCurrentGameState(GameState.PLAYER_INFO_VIEW); 
            if (gamePanel != null) gamePanel.repaint(); 
        } else {
            System.err.println("GameController: Cannot view player info - model or player is null.");
            if (gamePanel != null) gamePanel.displayMessage("Error: Player data not available.");
        }
    }

    /**
     * Handles a cheat request to set the game time.
     * @param hour The hour to set (0-23).
     * @param minute The minute to set (0-59).
     * @return true if the time was successfully set, false otherwise.
     */
    public boolean requestSetTime(int hour, int minute) {
        if (farmModel != null && farmModel.getCurrentTime() != null) {
            GameTime gameTime = farmModel.getCurrentTime();
            if (hour >= 0 && hour < GameTime.HOURS_IN_DAY && minute >= 0 && minute < GameTime.MINUTES_IN_HOUR) {
                gameTime.setTime(hour, minute);
                if (gamePanel != null) {
                    gamePanel.updateGameRender();
                }
                return true;
            }
        }
        return false;
    }

    // Shipping Bin UI Interaction  

    public void requestOpenShippingBin() {
        if (farmModel == null || gamePanel == null || farmModel.getPlayer() == null) {
            System.err.println("GameController: Critical model/view component missing for opening shipping bin.");
            return;
        }

        ShippingBin shippingBin = farmModel.getShippingBin();
        if (shippingBin == null) {
            System.err.println("GameController: ShippingBin model is null.");
            return;
        }

        if (!shippingBin.canSellToday()) {
            gamePanel.shippingBinActionFailed("You have already used the shipping bin today.");
            return;
        }

        farmModel.getCurrentTime().setPaused(true);
        farmModel.setCurrentGameState(GameState.SHIPPING_BIN);
        gamePanel.openShippingBinUI();
    }

    public void requestAddItemToShippingBin(Item item, int quantity) {
        if (farmModel == null || gamePanel == null || farmModel.getPlayer() == null) {
            System.err.println("GameController: Critical model/view component missing for adding to shipping bin.");
            return;
        }
        Player player = farmModel.getPlayer();
        ShippingBin shippingBin = farmModel.getShippingBin();
        Inventory inventory = player.getInventory();

        if (item == null || shippingBin == null || inventory == null) {
            gamePanel.shippingBinActionFailed("Error: System components missing.");
            return;
        }

        if (quantity <= 0) {
            gamePanel.shippingBinActionFailed("Quantity must be positive.");
            return;
        }

        if (!inventory.hasItem(item, quantity)) {
            gamePanel.shippingBinActionFailed("Not enough " + item.getName() + " in inventory.");
            return;
        }

        // Check if adding this item would exceed the 16 unique slots IF it's a new unique item
        if (!shippingBin.getItems().containsKey(item) && shippingBin.getItems().size() >= ShippingBin.MAX_UNIQUE_SLOTS) {
             gamePanel.shippingBinActionFailed("Shipping Bin full (max 16 unique item types).");
             return;
        }

        // Attempt to remove from player and add to bin
        if (inventory.removeItem(item, quantity)) {
            if (shippingBin.addItem(item, quantity)) {
                // Success
                gamePanel.itemAddedToBinSuccessfully(item, quantity);
            } else {
                inventory.addItem(item, quantity);
                gamePanel.shippingBinActionFailed("Could not add " + item.getName() + " to bin. Bin might be full.");
            }
        } else {
            gamePanel.shippingBinActionFailed("Failed to remove " + item.getName() + " from inventory.");
        }
    }

    public void requestCloseShippingBin() {
        if (farmModel == null || gamePanel == null) {
            System.err.println("GameController: Critical model/view component missing for closing shipping bin.");
            return;
        }
        
        farmModel.getCurrentTime().advance(15); 
        farmModel.getCurrentTime().setPaused(false);
        farmModel.setCurrentGameState(GameState.IN_GAME);
        gamePanel.closeShippingBinUI();
        if (farmModel.getShippingBin() != null && !farmModel.getShippingBin().getItems().isEmpty()){    
            gamePanel.displayMessage("Items in bin will be sold overnight.");
        } else {
            gamePanel.displayMessage("Shipping bin closed.");
        }
    }

    // Method to set the player's selected (held) item
    public void setSelectedItem(Item item) {
        if (farmModel != null && farmModel.getPlayer() != null) {
            farmModel.getPlayer().setSelectedItem(item);
            if (gamePanel != null) {
                gamePanel.updatePlayerInfoPanel();
            }
        }
    }

    public void ensureSafePlayerSpawn() {
        Player player = farmModel.getPlayer();
        MapArea currentMap = player.getCurrentMap();
        if (currentMap == null) {
            System.err.println("ensureSafePlayerSpawn: Player's current map is null. Cannot ensure safe spawn.");
            return;
        }

        Tile currentTile = currentMap.getTile(player.getCurrentTileX(), player.getCurrentTileY());
        if (currentTile == null || !isTileWalkableForSpawn(currentTile, currentMap, player.getCurrentTileX(), player.getCurrentTileY())) {
            System.out.println("Player spawned on an unsafe tile (" + (currentTile != null ? currentTile.getType() : "null tile") +
                               " at " + player.getCurrentTileX() + "," + player.getCurrentTileY() +"). Finding a safe spot...");
            Point safeSpot = findSafeSpawnPoint(currentMap, player.getCurrentTileX(), player.getCurrentTileY());
            player.setPosition(safeSpot.x, safeSpot.y); 
            System.out.println("Player moved to a safe spawn point: (" + safeSpot.x + "," + safeSpot.y + ") on map " + currentMap.getName());
        }
    }

    private boolean isTileWalkableForSpawn(Tile tile, MapArea map, int x, int y) {
        if (tile == null) return false;
        TileType type = tile.getType();
        boolean isSpawnableBaseType = type == TileType.GRASS || 
                                      type == TileType.ENTRY_POINT ||
                                      type == TileType.TILLABLE ||
                                      type == TileType.TILLED || 
                                      type == TileType.WOOD_FLOOR ||
                                      type == TileType.STONE_FLOOR ||
                                      type == TileType.CARPET_FLOOR ||
                                      type == TileType.LUXURY_FLOOR ||
                                      type == TileType.DIRT_FLOOR;

        if (!isSpawnableBaseType) {
            return false; 
        }

        if (type == TileType.DEPLOYED_OBJECT) {
        }

        if (map.isOccupied(x, y)) {
            DeployedObject objectOnTile = map.getObjectAt(x, y);
            if (objectOnTile != null && !(objectOnTile instanceof com.spakborhills.model.Object.House)) {
                return false;
            }
        }
        
        if (type == TileType.PLANTED) {
            return true;
        }

        return isSpawnableBaseType;
    }
    
    // Method to save the game state
    public void saveGame() {
        if (farmModel == null || farmModel.getPlayer() == null || farmModel.getCurrentTime() == null) {
            System.err.println("GameController: Cannot save game. Essential models are null.");
            if (gamePanel != null) {
                gamePanel.setGeneralGameMessage("Error: Could not save game state.", true);
            }
            return;
        }
        SaveLoadManager saveLoadManager = new SaveLoadManager();
        
        String savedFileName = saveLoadManager.saveGame(null, farmModel, farmModel.getPlayer(), farmModel.getCurrentTime());
        
        if (savedFileName != null) {
            if (gamePanel != null) {
                gamePanel.setGeneralGameMessage("Game Saved as: " + savedFileName, false);
            }
            System.out.println("GameController: Game saved successfully as " + savedFileName);
        } else {
            if (gamePanel != null) {
                gamePanel.setGeneralGameMessage("Error: Failed to save game.", true);
            }
            System.err.println("GameController: Failed to save game.");
        }
    }
    
    /**
     * Save game with a specific filename
     * @param fileName The filename to save as, or null for auto-generation
     * @return The filename that was actually used
     */
    public String saveGameAs(String fileName) {
        if (farmModel == null || farmModel.getPlayer() == null || farmModel.getCurrentTime() == null) {
            System.err.println("GameController: Cannot save game. Essential models are null.");
            return null;
        }
        
        SaveLoadManager saveLoadManager = new SaveLoadManager();
        return saveLoadManager.saveGame(fileName, farmModel, farmModel.getPlayer(), farmModel.getCurrentTime());
    }
    
    /**
     * Get a list of all available save files
     * @return List of SaveSlot objects with save file metadata
     */
    public List<SaveLoadManager.SaveSlot> getSaveSlots() {
        SaveLoadManager saveLoadManager = new SaveLoadManager();
        return saveLoadManager.getSaveSlots();
    }
    
    /**
     * Delete a save file
     * @param fileName The name of the save file to delete
     * @return true if deletion was successful
     */
    public boolean deleteSaveFile(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        SaveLoadManager saveLoadManager = new SaveLoadManager();
        return saveLoadManager.deleteSave(fileName);
    }

    /**
     * Overwrite an existing save file
     * @param existingFileName The full filename of the save file to overwrite (with extension)
     * @return The filename that was actually used, or null if the operation failed
     */
    public String overwriteSaveFile(String existingFileName) {
        if (farmModel == null || farmModel.getPlayer() == null || farmModel.getCurrentTime() == null) {
            System.err.println("GameController: Cannot save game. Essential models are null.");
            return null;
        }
        
        if (existingFileName == null || existingFileName.trim().isEmpty()) {
            System.err.println("GameController: Cannot overwrite save. Filename is null or empty.");
            return null;
        }
        
        SaveLoadManager saveLoadManager = new SaveLoadManager();
        return saveLoadManager.saveGame(existingFileName, farmModel, farmModel.getPlayer(), farmModel.getCurrentTime());
    }
}