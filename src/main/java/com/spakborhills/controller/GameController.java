package com.spakborhills.controller;

import java.awt.Point;
import java.util.ArrayList; // For creating list of items
import java.util.Collections;
import java.util.Comparator;
import java.util.List; // For returning list of items
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import javax.swing.JOptionPane;

import com.spakborhills.model.Farm;
import com.spakborhills.model.Player;
import com.spakborhills.model.Store;
import com.spakborhills.model.Enum.Direction;
import com.spakborhills.model.Enum.FishRarity;
// GameTime might be needed if Farm.nextDay() isn't comprehensive enough for all time updates
// import com.spakborhills.model.GameTime; 
import com.spakborhills.model.Enum.LocationType;
// import com.spakborhills.model.Enum.RelationshipStatus;
import com.spakborhills.model.Enum.Season;
import com.spakborhills.model.Enum.TileType;
import com.spakborhills.model.Enum.Weather;
import com.spakborhills.model.Item.*;
import com.spakborhills.model.Map.FarmMap;
import com.spakborhills.model.Map.MapArea;
import com.spakborhills.model.Map.Tile;
import com.spakborhills.model.NPC.NPC;
import com.spakborhills.model.Util.*;
// GamePanel might be needed later for more complex interactions or direct view updates
import com.spakborhills.view.GamePanel;
import com.spakborhills.model.Object.House; // Added import
import com.spakborhills.model.Util.ShippingBin; // Import ShippingBin
import com.spakborhills.model.Enum.GameState; // Import GameState
import com.spakborhills.model.Object.DeployedObject; // Added import for DeployedObject

public class GameController {

    private Farm farmModel;
    private GamePanel gamePanel; // Referensi ke GamePanel untuk menampilkan pesan

    public GameController(Farm farmModel) {
        this.farmModel = farmModel;
        this.gamePanel = null; // Inisialisasi null, akan di-set nanti
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
            // Setelah bergerak, cek apakah pemain ada di entry point map saat ini
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
            // Avoid critical errors if components aren't ready, though this check should ideally only run when in_game
            return; 
        }

        // Only check if in game and not already in a state that prevents pass out (like EOD summary)
        if (farmModel.getCurrentGameState() != GameState.IN_GAME) {
            return;
        }

        if (farmModel.getCurrentTime().isPastBedtime()) {
            System.out.println("GameController: Player is past bedtime. Initiating pass out.");
            Player player = farmModel.getPlayer();
            GameTime currentTime = farmModel.getCurrentTime();
            EndGameStatistics statistics = farmModel.getStatistics(); // Assuming Farm has getStatistics()
            PriceList priceList = farmModel.getPriceList(); // Assuming Farm has getPriceList()

            String eventMessage = "You stayed up too late and passed out!";
            
            // Process sales before advancing to next day and changing player state
            int income = farmModel.getShippingBin().processSales(statistics, priceList, currentTime.getCurrentDay(), currentTime.getCurrentSeason());
            
            // Player.passOut() now returns the energy penalty, but we might not need it here directly
            // if the EndOfDayMessage just needs the event and income.
            // Farm farm = farmModel; // Pass the farmModel instance itself
            player.passOut(farmModel); // Pass the farm model instance
            
            // farmModel.getCurrentTime().nextDay(); // Player.passOut should handle calling farm.nextDayLogic() or similar
            // player.passOut() should set location to home and reset energy.
            // The nextDay logic is now expected to be handled within Farm model when passOut is called on player, 
            // or GameController's passOut should call farmModel.nextDayLogic() if player.passOut doesn't trigger it.
            // For now, assume player.passOut handles becoming the new day via farmModel reference.
            // If not, farmModel.nextDayLogic() or similar should be called here AFTER player.passOut() and BEFORE showEndOfDayMessage.
            // Let's assume player.passOut(farmModel) correctly triggers the day change logic via the farmModel reference.

            if (gamePanel != null) {
                // generateNewDayInfoString() might need to be called *after* the day has officially ticked over.
                // If player.passOut(farmModel) ensures the new day's state is set in currentTime, this is fine.
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
        // FarmMap farmMap = farmModel.getFarmMap(); // farmMap variable can be obtained from player.getCurrentMap() if it's FarmMap

        if (player == null) {
            System.err.println("GameController: Player is null, cannot till land.");
            return false;
        }

        if (!(player.getCurrentMap() instanceof FarmMap)) {
            System.out.println("Hoe (Tilling) can only be used on the Farm.");
            return false;
        }
        FarmMap farmMap = (FarmMap) player.getCurrentMap(); // Now we know it's a FarmMap

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
            player.changeEnergy(-5); // Biaya energi untuk mencangkul
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
        // FarmMap farmMap = farmModel.getFarmMap();
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
        // FarmMap farmMap = farmModel.getFarmMap();
        GameTime gameTime = farmModel.getCurrentTime(); // Dipertahankan jika Player.water() membutuhkannya di masa depan, atau untuk konsistensi

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

        // Pemeriksaan kepemilikan Watering Can dan apakah itu item yang dipilih
        // sekarang ditangani di dalam player.water() melalui player.getSelectedItem()
        // Jadi, kita tidak perlu cek inventory.hasTool("Watering Can") di sini lagi.

        Tile targetTile = farmMap.getTile(player.getCurrentTileX(), player.getCurrentTileY());
        if (targetTile == null) {
            System.err.println("GameController: Tile at player position is null for watering.");
            return false;
        }

        boolean watered = player.water(targetTile, gameTime.getCurrentWeather());
        if (watered) {
            player.changeEnergy(-5); // Biaya energi untuk menyiram
            System.out.println("Watered tile at (" + player.getCurrentTileX() + "," + player.getCurrentTileY() + "). Energy: " + player.getEnergy());
            checkPassOut(); 
        }
        // Jika tidak berhasil (misal, tile tidak bisa disiram, atau tidak ada watering can dipilih), 
        // player.water() akan return false dan pesan error akan dicetak dari Player.java
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
        // FarmMap farmMap = farmModel.getFarmMap();
        Map<String, Item> itemRegistry = farmModel.getItemRegistry(); // For Player.harvest()

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
            // System.out.println("GameController: Nothing to harvest at player position or tile is null.");
            return false; // Nothing to harvest or tile invalid
        }

        // Player.harvest() should handle: 
        // 1. Calling targetTile.processHarvest(itemRegistry)
        // 2. Adding the returned items to its inventory
        // 3. Deducting energy (e.g., 5 energy per crop)
        // 4. Returning true/false
        // Now also needs EndGameStatistics
        boolean harvested = player.harvest(targetTile, itemRegistry, farmModel.getStatistics()); // Pass statistics

        if (harvested) {
            System.out.println("Successfully harvested from tile (" + player.getCurrentTileX() + "," + player.getCurrentTileY() + ")");
            player.changeEnergy(-5); // Jika ada biaya energi untuk panen
            checkPassOut(); // Check for pass out condition after successful harvesting
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
        // FarmMap farmMap = farmModel.getFarmMap();

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
            player.changeEnergy(-5); // Biaya energi untuk memulihkan tanah
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
            // Memungkinkan makan item penambah energi bahkan jika sudah pingsan, jika itu satu-satunya cara untuk pulih sedikit.
            // Namun, jika item tidak menambah energi atau energi negatif, dan sudah pingsan, maka tidak bisa.
            return false;
        }

        boolean eaten = player.eat(selectedItem);

        if (eaten) {
            int timeCostEat = 5; // Biaya waktu 5 menit untuk makan
            gameTime.advance(timeCostEat);
            System.out.println("Player finished eating. Time advanced by " + timeCostEat + " minutes. Current time: " + gameTime.getTimeString());
            
            // Setelah makan, selectedItem mungkin menjadi null jika stacknya habis.
            // Perlu di-handle agar selectedItem di Player konsisten.
            if (player.getInventory().getItemCount(selectedItem) == 0) {
                // Item habis, coba pilih item berikutnya secara otomatis
                // Ini bisa jadi kompleks, untuk sekarang kita set null saja, 
                // dan biarkan pemain memilih ulang atau GamePanel menampilkan "None"
                System.out.println("GameController: Item " + selectedItem.getName() + " habis setelah dimakan.");
                // player.setSelectedItem(null); // Jangan set di sini, biarkan sistem pemilihan item yang ada bekerja.
                                            // Pemain akan otomatis memilih item berikutnya jika ada saat inventory di-cycle.
                                            // Atau, jika ada mekanisme 'auto-select next available item', itu bisa dipanggil di sini.
                                            // Untuk saat ini, kita asumsikan player.getSelectedItem() akan mengembalikan null jika item terakhir habis
                                            // dan player.selectNext/Prev akan skip item yang countnya 0.
                                            // Jika tidak, kita perlu memanggil selectNext/Previous atau semacamnya di sini.
                                            // Untuk memastikan konsistensi, jika item yang dimakan adalah selectedItem dan habis,
                                            // kita perlu memastikan selectedItem di Player di-update. Cara terbaik adalah dengan
                                            // memanggil kembali logika pemilihan item.
                                            // Namun, karena Player.eat() sendiri tidak mengubah selectedItem, kita hanya perlu memastikan
                                            // GamePanel akan me-refresh tampilan selectedItem yang mungkin sudah jadi 0 qty.
                                            // Jika selectedItem adalah objek yang sama, dan Player.getSelectedItem() merujuk ke sana,
                                            // maka pengecekan player.getInventory().getItemCount(selectedItem) == 0 sudah cukup.
                                            // Jika Player.eat() secara internal membuat selectedItem jadi null jika habis, maka tidak perlu apa2.
                                            // Berdasarkan Player.java, dia TIDAK set selectedItem jadi null. Ini adalah tanggung jawab controller atau UI.
                                            // Untuk sekarang, kita biarkan. Jika ini jadi masalah, kita bisa panggil selectNextItem() jika item habis.
                 // Perlu dipastikan selectedItem di Player di-refresh. Cara paling aman:
                if (player.getSelectedItem() != null && player.getInventory().getItemCount(player.getSelectedItem()) == 0) {
                     System.out.println("GameController: Selected item " + player.getSelectedItem().getName() + " habis, mencoba memilih item lain.");
                     // Coba select next, jika gagal (misal inventory jadi kosong), selected item akan jadi null.
                     selectNextItem(); // Ini akan memutar dan memilih item valid berikutnya atau null
                     if (player.getSelectedItem() == null) { // Jika setelah selectNextItem masih null (inventory kosong)
                         System.out.println("GameController: Inventory kosong setelah makan, selected item menjadi null.");
                     } else {
                         System.out.println("GameController: Selected item baru setelah makan: " + player.getSelectedItem().getName());
                     }
                }
            }
            checkPassOut(); // Cek kondisi pingsan setelah energi berubah dan waktu bertambah
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
                gamePanel.stopGameTimer(); // Stop timer before modal dialog
                gamePanel.showEndOfDayMessage(eventMessage, incomeFromSales, newDayInfo);
                gamePanel.startGameTimer(); // Restart timer after modal dialog
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

    // Placeholder for other game actions that the controller will handle
    // public void handleTillRequest() { ... }
    // public void handlePlantRequest(String seedName) { ... }

    /**
     * Retrieves a list of items available for purchase from the store.
     * @return A list of Item objects or null if an error occurs.
     */
    public List<Item> getStoreItemsForDisplay() {
        if (farmModel == null || farmModel.getStore() == null || farmModel.getItemRegistry() == null || farmModel.getPriceList() == null) {
            System.err.println("Error: Model, Toko, ItemRegistry, atau PriceList null di GameController.getStoreItemsForDisplay.");
            if (gamePanel != null) {
                // gamePanel.displayMessage("Error: Data toko tidak dapat dimuat.");
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

        // Check if the item is actually sold by the store
        // This relies on Store.getAvailableItemsForDisplay filtering correctly
        // or Store.sellToPlayer having its own internal check.
        // For a more direct check here, we'd need access to Store's internal list of items for sale.
        // Let's assume Store.sellToPlayer handles this.
        // We can check if the item has a valid buy price.
        int buyPrice = priceList.getBuyPrice(itemName);
        if (buyPrice == -1) { // Assuming -1 means not for sale or price not set
            return "Gagal: Item '" + itemName + "' tidak dapat dibeli atau tidak dijual.";
        }
        
        // Check if item is in the list of items the store *claims* to sell (from getAvailableItemsForDisplay)
        // This is a sanity check. The ultimate truth is if priceList has a buy price.
        boolean foundInStoreDisplayList = false;
        List<Item> displayItems = store.getAvailableItemsForDisplay(itemRegistry, priceList);
        for (Item displayItem : displayItems) {
            if (displayItem.getName().equals(itemName)) {
                foundInStoreDisplayList = true;
                break;
            }
        }
        if (!foundInStoreDisplayList) {
             // This case implies an inconsistency, or the item is valid but was filtered out for display (e.g. buy price 0 before fix)
             // but if it has a valid buyPrice > 0 from pricelist, it should be buyable.
             // If buyPrice is 0, it means it's free.
             if (buyPrice <= 0 && buyPrice != -1) { // Item is free or has an issue, but exists in priceList
                 // Allow free items if they appear in price list with 0
             } else if (buyPrice == -1) { // Definitely not for sale by priceList
                return "Gagal: Item '" + itemName + "' tidak terdaftar untuk dijual (kode: C01).";
             }
             // If it has a positive price but not in display list, it's weird, but let's proceed if pricelist says it's buyable.
        }


        int totalPrice = buyPrice * quantity;

        if (player.getGold() < totalPrice) {
            return "Gagal: Gold tidak cukup. Butuh " + totalPrice + "G, kamu punya " + player.getGold() + "G.";
        }

        // Call the original Store.sellToPlayer method
        // The existing sellToPlayer in Store.java is: sellToPlayer(Player player, Item item, int quantity, PriceList priceList, Map<String, Item> itemRegistry)
        boolean success = store.sellToPlayer(player, itemToBuy, quantity, priceList, itemRegistry);

        if (success) {
            if (gamePanel != null) {
                 gamePanel.updatePlayerInfoPanel();
            }
            String priceString = totalPrice == 0 ? "Gratis" : totalPrice + "G";
            return "Berhasil membeli " + quantity + " " + itemToBuy.getName() + " (" + priceString + ").";
        } else {
            // Attempt to give a more specific reason if possible, otherwise generic.
            // This part depends on Store.sellToPlayer's internal logic and if it provides feedback.
            // For now, a general message.
            return "Gagal membeli item. Mungkin inventory penuh atau item tidak lagi tersedia.";
        }
    }

    /**
     * Processes a player's request to buy an item from the store.
     * @param itemName The name of the item to buy.
     * @param quantity The quantity of the item to buy.
     * @return true if the purchase was successful, false otherwise.
     * @deprecated Use requestBuyItemAndGetMessage for detailed feedback.
     */
    @Deprecated
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

        // The Store.sellToPlayer method should handle gold deduction, adding item to inventory, etc.
        boolean success = store.sellToPlayer(player, itemToBuy, quantity, priceList, itemRegistry);
        if (success) {
            System.out.println("Purchased " + quantity + " of " + itemName);

            // Check if the bought item unlocks a recipe
            if (itemToBuy instanceof Food) { // Recipes bought from store are Food items
                // String normalizedItemName = itemName.toUpperCase().replace(" ", "_");
                String eventKey = null;

                if (itemName.equalsIgnoreCase("Fish n' Chips")) {
                    eventKey = "BOUGHT_RECIPE_FISH_N'_CHIPS"; // Make sure this matches Recipe.isUnlocked
                } else if (itemName.equalsIgnoreCase("Fish Sandwich")) {
                    eventKey = "BOUGHT_RECIPE_FISH_SANDWICH"; // Make sure this matches Recipe.isUnlocked
                }
                // Add other "buy-to-unlock" recipe items here if any

                if (eventKey != null && farmModel.getStatistics() != null) {
                    farmModel.getStatistics().recordKeyEventOrItem(eventKey);
                    System.out.println("Recipe unlock event recorded: " + eventKey);
                }
            }
            // No direct energy cost for buying, so no checkPassOut() here unless specified.
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

        // Validasi canSellToday dari ShippingBin sebelum memanggil Player.sellItemToBin
        if (!shippingBin.canSellToday()) {
            System.out.println("GameController: Cannot sell today, already sold items via Shipping Bin.");
            return false;
        }

        // Panggil metode player yang sudah divalidasi
        boolean sold = player.sellItemToBin(itemToSell, quantity, shippingBin, gameTime.getCurrentDay());
        if (sold) {
            System.out.println("GameController: Request to sell " + quantity + " of " + itemName + " processed.");
            // Pertimbangkan jika ada efek waktu 15 menit yang perlu di-trigger di sini
            // gameTime.advance(15); // Jika interaksi dianggap langsung selesai dan memakan waktu
            // Namun, spesifikasi (No. 20) menyatakan "Menghentikan waktu selama penjualan dan 
            // menghabiskan waktu 15 menit dalam game setelah selesai Selling."
            // Ini mungkin lebih cocok ditangani di GamePanel setelah dialog ditutup.
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
            return new ArrayList<>(farmModel.getPlayer().getInventory().getItems().keySet()); // Returns a list of unique item types
        }
            return new ArrayList<>();
    }

    /**
     * Memilih item berikutnya dari daftar item di inventory pemain.
     */
    public void selectNextItem() {
        if (farmModel == null || farmModel.getPlayer() == null) return;
        Player player = farmModel.getPlayer();
        List<Item> allItems = getPlayerInventoryItems(); // Dapat semua item

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
        List<Item> allItems = getPlayerInventoryItems(); // Dapat semua item

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
        if (currentIndex <= 0) { // Jika tidak ditemukan atau item pertama
            prevIndex = allItems.size() - 1; // Putar ke item terakhir
        } else {
            prevIndex = currentIndex - 1;
        }
        player.setSelectedItem(allItems.get(prevIndex));
        System.out.println("Selected item: " + player.getSelectedItem().getName());
    }

    // Metode getPlayerTools() yang lama bisa dihapus atau diubah jika masih perlu 
    // khusus untuk Equipment. Untuk sekarang, kita fokus pada item umum.
    /**
     * Mengambil daftar Equipment (alat) yang dimiliki pemain.
     * @return List dari Equipment, atau list kosong jika tidak ada.
     */
    public List<Equipment> getPlayerTools() {
        if (farmModel == null || farmModel.getPlayer() == null) {
            return new ArrayList<>();
        }
        Player player = farmModel.getPlayer();
        List<Equipment> tools = new ArrayList<>();
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
            return new Point(1, 1); // Fallback to a generally safe coordinate
        }

        // Check preferred point first
        Tile preferredTile = map.getTile(preferredX, preferredY);
        if (preferredTile != null && isTileWalkable(preferredTile, map, preferredX, preferredY)) {
            return new Point(preferredX, preferredY);
        }

        // Search adjacent tiles to the preferred point
        int[][] offsets = {
            {0, -1}, {0, 1}, {1, 0}, {-1, 0}, // N, S, E, W
            {1, -1}, {-1, -1}, {1, 1}, {-1, 1}  // NE, NW, SE, SW
        };

        for (int[] offset : offsets) {
            int checkX = preferredX + offset[0];
            int checkY = preferredY + offset[1];

            if (map.isWithinBounds(checkX, checkY)) {
                Tile adjacentTile = map.getTile(checkX, checkY);
                if (adjacentTile != null && isTileWalkable(adjacentTile, map, checkX, checkY)) {
                    System.out.println("Safe spawn found at adjacent tile: (" + checkX + ", " + checkY + ") for preferred: (" + preferredX + ", " + preferredY + ") on map " + map.getName());
                    return new Point(checkX, checkY);
                }
            }
        }

        System.out.println("findSafeSpawnPoint: Preferred ("+preferredX+","+preferredY+") and adjacent tiles unsafe on map " + map.getName() + ". Checking map entry points.");

        // If preferred and adjacent are not safe, iterate through the map's defined entry points
        List<Point> entryPoints = map.getEntryPoints();
        if (entryPoints != null && !entryPoints.isEmpty()) {
            for (Point entry : entryPoints) {
                if (map.isWithinBounds(entry.x, entry.y)) {
                    Tile entryTile = map.getTile(entry.x, entry.y);
                    // We also allow ENTRY_POINT itself as a walkable spawn, assuming it's placed on a fundamentally walkable base tile.
                    if (entryTile != null && (isTileWalkable(entryTile, map, entry.x, entry.y) || entryTile.getType() == TileType.ENTRY_POINT)) {
                        System.out.println("Safe spawn found at map entry point: (" + entry.x + ", " + entry.y + ") on map " + map.getName());
                        return entry;
                    }
                }
            }
            System.err.println("findSafeSpawnPoint: No walkable entry point found on map " + map.getName() + ". Fallback to first entry point or (1,1).");
            // If no entry point is walkable, return the first one as a last resort, or a hardcoded safe point.
            return entryPoints.get(0); // Could still be unsafe, but it's an entry point.
        }

        System.err.println("findSafeSpawnPoint: No safe adjacent tile and no entry points defined for map " + map.getName() + ". Returning absolute fallback (1,1).");
        // Absolute fallback if no other options
        return new Point(1, 1); 
    }

    // Helper method to check if a tile is walkable
    private boolean isTileWalkable(Tile tile, MapArea map, int x, int y) {
        if (tile == null) return false;
        TileType type = tile.getType();
        // Walkable types: GRASS, TILLABLE, TILLED, PLANTED, ENTRY_POINT (if base is walkable)
        // Also includes various floor types.
        // Unwalkable: WATER, OBSTACLE, or if there's a DEPLOYED_OBJECT that's not passable
        boolean isBaseTypeWalkable = type == TileType.GRASS || 
                                     type == TileType.TILLABLE || 
                                     type == TileType.TILLED || 
                                     type == TileType.PLANTED || // A planted tile is walkable; harvestability is a state of the plant, not the tile type itself for walkability.
                                     type == TileType.ENTRY_POINT || // Assuming entry points are placed on walkable base
                                     type == TileType.WOOD_FLOOR ||  
                                     type == TileType.STONE_FLOOR ||
                                     type == TileType.CARPET_FLOOR ||
                                     type == TileType.LUXURY_FLOOR ||
                                     type == TileType.DIRT_FLOOR;

        if (!isBaseTypeWalkable) return false;

        // Check for blocking deployed objects. 
        // The isOccupied check might be slightly different from "isWalkable".
        // For now, assume if getAssociatedObject is not null, it's blocking.
        // A more robust way would be DeployedObject having an isPassable() method.
        if (map.getObjectAt(x,y) != null) { 
            // TODO: Check if the object is passable. For now, assume all objects are obstacles.
            // For example, a small rug (DeployedObject) might be on a WOOD_FLOOR tile and should be passable.
            // A House object would not be.
            // This currently uses a simplified check: if there's an object, it's not walkable.
            // This might conflict with the definition of some maps if entry points are on tiles with non-blocking objects.
            // For now, if the object is the map itself (e.g. a house in an NPC map), allow it.
            DeployedObject obj = map.getObjectAt(x,y);
            if (obj!= null && obj.getName().toLowerCase().contains("house") && map.getName().toLowerCase().contains("home")){
                 //This is likely an NPC house map, player can spawn inside.
            } else if (obj != null){
                // System.out.println("Tile ("+x+","+y+") on map "+map.getName()+" has object: "+obj.getName()+", considered not walkable for spawn.");
                // return false; 
                // Temporarily allowing spawn on occupied tiles if base type is walkable, to avoid getting stuck
                // This needs better logic based on object passability.
            }
        }
        return true;
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

        int preferredX = 0; // Default preferred X
        int preferredY = 0; // Default preferred Y

        List<Point> targetEntryPoints = targetMap.getEntryPoints();

        if (destination == LocationType.FARM) {
            // Returning to Farm: Try to use a logical entry point based on where player might be coming from,
            // or a default safe spot on the farm map if no specific entry logic is in place.
            // For now, let's assume the FarmMap has entry points defined for world map access.
            // We could try to find an entry point on FarmMap that isn't on the edge player is on (if currentMap isn't FarmMap)
            // or use player's last known position on FarmMap if available and still valid.
            // Simplest: use the first entry point of the farm map, or a safe default.
            if (targetEntryPoints != null && !targetEntryPoints.isEmpty()) {
                preferredX = targetEntryPoints.get(0).x;
                preferredY = targetEntryPoints.get(0).y;
            } else {
                preferredX = targetMap.getSize().width / 2; // Default to center-ish if no entry points
                preferredY = targetMap.getSize().height / 2;
        }
        } else if (currentMap instanceof FarmMap) {
            // Exiting FarmMap to another location
            int playerExitX = player.getCurrentTileX();
            int playerExitY = player.getCurrentTileY();
            int farmMapWidth = currentMap.getSize().width;
            int farmMapHeight = currentMap.getSize().height;

            // Default to a central entry point on the target map
            if (targetEntryPoints != null && !targetEntryPoints.isEmpty()) {
                // Try to find an entry point on the opposite side of the target map
                if (playerExitX == 0 && targetMap.getName().equalsIgnoreCase("Forest River")) { // Exited left from Farm, entering Forest River (expects right entry)
                    Point entry = targetEntryPoints.stream().filter(p -> p.x == targetMap.getSize().width -1).findFirst().orElse(targetEntryPoints.get(0));
                    preferredX = entry.x; preferredY = entry.y;
                } else if (playerExitX >= farmMapWidth - 1  && targetMap.getName().equalsIgnoreCase("Forest River")) { // Exited right from Farm, entering Forest River (expects left entry)
                    Point entry = targetEntryPoints.stream().filter(p -> p.x == 0).findFirst().orElse(targetEntryPoints.get(0));
                    preferredX = entry.x; preferredY = entry.y;
                } else if (targetEntryPoints.size() > 0) { // Default to first entry point if no specific logic matches
                     preferredX = targetEntryPoints.get(0).x;
                     preferredY = targetEntryPoints.get(0).y;
                } else { // Fallback if target has no entry points
                    preferredX = targetMap.getSize().width / 2;
                    preferredY = targetMap.getSize().height / 2;
                }
            } else { // Fallback if target has no entry points
                preferredX = targetMap.getSize().width / 2;
                preferredY = targetMap.getSize().height / 2;
            }
        } else {
            // Transitioning between two non-FarmMap world locations, or from a world location to another (not Farm)
            // This logic might need to be more sophisticated based on world map connections.
            // For now, use the first entry point of the target map or its center.
            if (targetEntryPoints != null && !targetEntryPoints.isEmpty()) {
                preferredX = targetEntryPoints.get(0).x;
                preferredY = targetEntryPoints.get(0).y;
            } else {
                preferredX = targetMap.getSize().width / 2;
                preferredY = targetMap.getSize().height / 2;
            }
        }
        
        // Ensure preferred coordinates are within the target map bounds before finding safe spawn
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
            player.changeEnergy(-10); // Cost of visiting
            gameTime.advance(15);   // Time cost of visiting

            // It's important that farmModel's player reference is the same one whose currentMap has changed.
            // And GamePanel must be looking at player.getCurrentMap() to see the change.

            if (gamePanel != null) {
                gamePanel.repaint(); // Trigger repaint to show new map
            }
            checkPassOut(); // Check if player passed out due to energy loss
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
        
        // Check if player has enough energy
        if (player.getEnergy() <= Player.MIN_ENERGY) {
            System.out.println("Player is too tired to fish.");
            return false;
        }
        
        // Check if player has fishing rod selected
        Item selectedItem = player.getSelectedItem();
        if (selectedItem == null || !selectedItem.getName().equals("Fishing Rod")) {
            System.out.println("You need to select the Fishing Rod to fish.");
            return false;
        }
        
        // Get current map and determine fishing location type
        MapArea currentMap = player.getCurrentMap();
        LocationType fishingLocation = null;
        
        if (currentMap instanceof FarmMap) {
            // On farm map, must be near (not on) pond
            boolean nearPond = isPlayerNearWater(player, (FarmMap)currentMap);
            if (nearPond) {
                fishingLocation = LocationType.POND;
            } else {
                System.out.println("You need to be near water to fish.");
                return false;
            }
        } else {
            // On world map, determine location from current map
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
            
            // Check if player is near water on this map
            Tile playerTile = currentMap.getTile(player.getCurrentTileX(), player.getCurrentTileY());
            if (playerTile == null || !isNearWaterTile(player, currentMap)) {
                System.out.println("You need to be near water to fish.");
                return false;
            }
        }
        
        // At this point, we know:
        // 1. Player has fishing rod selected
        // 2. Player has enough energy
        // 3. Player is near water in a valid fishing location
        // 4. We have determined the fishing location type
        
        // Time and energy cost
        player.changeEnergy(-5); // Energy cost for attempting to fish
        gameTime.advance(15);    // Time cost for fishing attempt (15 minutes)
        
        // Show fishing minigame dialog
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
        
        // Check adjacent tiles (up, down, left, right)
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
        
        // First check if player is directly on water
        Tile playerTile = map.getTile(playerX, playerY);
        if (playerTile != null && playerTile.getType() == TileType.WATER) {
            return true;
        }
        
        // Then check adjacent tiles
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
        // Determine season, time, and weather
        Season currentSeason = farmModel.getCurrentTime().getCurrentSeason();
        Weather currentWeather = farmModel.getCurrentTime().getCurrentWeather();
        int currentHour = farmModel.getCurrentTime().getHour();
        
        // Use the fishing location, season, time, and weather to determine available fish
        boolean canCatchLegendary = canCatchLegendaryFish(fishingLocation, currentSeason, currentHour, currentWeather);
        boolean canCatchRegular = canCatchRegularFish(fishingLocation, currentSeason, currentHour, currentWeather);
        
        // Default to common fish if no specific fish can be caught
        String fishType = "Common";
        int maxGuess = 10;    // Range 1-10
        int maxAttempts = 10; // 10 attempts
        
        // Determine fish type and difficulty with some randomness
        Random rng = new Random();
        if (canCatchLegendary && rng.nextDouble() < 0.05) { // 5% chance for legendary if conditions are right
            fishType = "Legendary";
            maxGuess = 500;    // Range 1-500
            maxAttempts = 7;   // Only 7 attempts
        } else if (canCatchRegular && rng.nextDouble() < 0.3) { // 30% chance for regular if conditions are right
            fishType = "Regular";
            maxGuess = 100;    // Range 1-100
            maxAttempts = 10;  // 10 attempts
        }
        
        // Generate random number to guess
        int targetNumber = rng.nextInt(maxGuess) + 1; // 1 to maxGuess

        // SPOILER for Legendary Fish
        if ("Legendary".equals(fishType)) {
            System.out.println("[SPOILER] Legendary Fish attempt at: " + fishingLocation + ". Target Number: " + targetNumber);
        }
        
        // Show dialog for fishing minigame
        if (gamePanel == null) {
            System.err.println("GameController: gamePanel is null, cannot show fishing dialog.");
            return false;
        }
        
        // Prepare message
        String fishingMessage = "You're fishing for a " + fishType + " fish!\n" +
                               "Guess the number between 1 and " + maxGuess + ".\n" +
                               "You have " + maxAttempts + " attempts.";
        
        boolean caughtFish = false;
        int attemptsLeft = maxAttempts;
        
        while (attemptsLeft > 0) {
            String guessStr = JOptionPane.showInputDialog(gamePanel, 
                fishingMessage + "\nAttempts left: " + attemptsLeft + "\nEnter your guess (or cancel to stop fishing) (DEBUG, answer):" + targetNumber);
            
            if (guessStr == null) {
                // User canceled the dialog
                System.out.println("Fishing canceled.");
                return true; // Still count as action taken
            }
            
            try {
                int guess = Integer.parseInt(guessStr);
                
                if (guess == targetNumber) {
                    // Correct guess - fish caught!
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
        
        // Process result
        if (caughtFish) {
            // Generate a random fish based on the type and add to inventory
            Item itemCaught = generateRandomFish(fishType, fishingLocation, currentSeason, currentWeather);
            if (itemCaught != null && itemCaught instanceof Fish) { // Ensure it's a Fish object
                Fish fishCaughtObject = (Fish) itemCaught;
                farmModel.getPlayer().getInventory().addItem(fishCaughtObject, 1);
                JOptionPane.showMessageDialog(gamePanel, "You caught a " + fishCaughtObject.getName() + "!");
                
                // Record the catch in statistics
                if (farmModel.getStatistics() != null) {
                    farmModel.getStatistics().recordFishCatch(fishCaughtObject.getName(), fishCaughtObject.getRarity());
                    System.out.println("Fish catch recorded: " + fishCaughtObject.getName() + ", Rarity: " + fishCaughtObject.getRarity());
                }
            } else {
                JOptionPane.showMessageDialog(gamePanel, "You caught a fish, but it got away!");
            }
        } else {
            JOptionPane.showMessageDialog(gamePanel, "The fish got away! Better luck next time.");
        }
        
        // Check for pass out
        checkPassOut();
        
        return true; // Action was processed
    }
    
    /**
     * Checks if legendary fish can be caught based on location, season, time, and weather.
     */
    private boolean canCatchLegendaryFish(LocationType location, Season season, int hour, Weather weather) {
        // Implement conditions for legendary fish from specification
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
        // Iterate through all fish in the registry
        if (farmModel == null || farmModel.getItemRegistry() == null || farmModel.getCurrentTime() == null) {
            return false; 
        }
        GameTime currentTime = farmModel.getCurrentTime(); // Need full GameTime object

        for (Item item : farmModel.getItemRegistry().values()) {
            if (item instanceof Fish) {
                Fish fish = (Fish) item;
                // Check if it's a regular fish and can be caught under current conditions
                if (fish.getRarity() == FishRarity.REGULAR && fish.canBeCaught(season, currentTime, weather, location)) {
                    return true; // At least one regular fish can be caught
                }
            }
        }
        return false; // No regular fish can be caught
    }
    
    /**
     * Generates a random fish based on the type and conditions.
     */
    private Item generateRandomFish(String fishType, LocationType location, Season season, Weather weather) {
        if (farmModel == null || farmModel.getItemRegistry() == null || farmModel.getCurrentTime() == null) {
            return null;
        }
        GameTime currentTime = farmModel.getCurrentTime(); // For Fish.canBeCaught

        // Get all fish from registry
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
            return null; // No fish can be caught at all under these conditions
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

        // If no fish of the target rarity are available from the catchable set,
        // try to fall back gracefully or return null.
        // For now, if specific rarity isn't found, we pick any catchable fish of a different type.
        // A more sophisticated fallback could be: Legendary -> Regular -> Common.
        // Or, strict: if Legendary roll but none available, then no fish.
        // Current: If fishOfTargetRarity is empty, use any catchableFish.
        
        List<Fish> listToPickFrom = fishOfTargetRarity.isEmpty() ? catchableFish : fishOfTargetRarity;

        if (listToPickFrom.isEmpty()) {
             // This case should ideally not be reached if catchableFish was not empty,
             // unless targetRarity was specified but no fish of that rarity were catchable.
            return null;
        }
        
        // Pick a random fish from the final list
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
        List<Recipe> availableRecipes = farmModel.getRecipes(); // Asumsi Farm punya getter ini

        // Pengecekan untuk komponen yang diambil dari farmModel
        if (player == null || gameTime == null || itemRegistry == null) {
            gamePanel.displayMessage("Error internal: Data pemain, waktu, atau item tidak lengkap.");
            System.err.println("GameController.handleCookRequest: Player, GameTime, atau ItemRegistry adalah null.");
            return;
        }

        if (availableRecipes == null) { // Cek spesifik untuk recipes
            gamePanel.displayMessage("Daftar resep tidak tersedia saat ini.");
            System.err.println("GameController.handleCookRequest: farmModel.getRecipes() mengembalikan null.");
            return;
        }

        // --- KONDISI LOKASI MEMASAK (Spesifikasi hal 29) ---
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

        // --- BAGIAN UI UNTUK MEMILIH RESEP (Contoh dengan JOptionPane) ---
        List<String> unlockedRecipeNames = new ArrayList<>();
        for (Recipe r : availableRecipes) {
            // Asumsi EndGameStatistics ada di Farm dan bisa diakses untuk cek unlock
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

        if (chosenRecipeName == null) return; // User cancel

        Recipe selectedRecipe = null;
        for (Recipe r : availableRecipes) {
            if (r.getName().equals(chosenRecipeName)) {
                selectedRecipe = r;
                break;
            }
        }
        if (selectedRecipe == null) { // Seharusnya tidak terjadi jika nama dari list
            gamePanel.displayMessage("Resep tidak valid.");
            return;
        }

        // --- BAGIAN UI UNTUK MEMILIH BAHAN BAKAR (Contoh dengan JOptionPane) ---
        // String[] fuelOptions = {"Coal", "Firewood"};
        // Filter fuel yang dimiliki pemain
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

        if (chosenFuelName == null) return; // User cancel

        Item selectedFuelItem = itemRegistry.get(chosenFuelName);
        if (selectedFuelItem == null) { // Seharusnya tidak terjadi
            gamePanel.displayMessage("Bahan bakar tidak valid.");
            return;
        }
        
        // --- EFEK INISIASI & MEMANGGIL PLAYER.COOK() ---
        final int COOK_ENERGY_COST = 10; // Spesifikasi Hal 29
        if (player.getEnergy() < COOK_ENERGY_COST) {
            gamePanel.displayMessage("Energi tidak cukup untuk memulai memasak (butuh " + COOK_ENERGY_COST + ").");
            return;
        }
        player.changeEnergy(-COOK_ENERGY_COST); // Kurangi energi untuk memulai

        String cookResultOutcome = player.cook(selectedRecipe, selectedFuelItem, itemRegistry);

        if (cookResultOutcome != null && itemRegistry.containsKey(cookResultOutcome)) { // Sukses mempersiapkan bahan
            Item foodProduct = itemRegistry.get(cookResultOutcome);
            int servingsMade = 1;

            // Logika efisiensi Coal
            if (selectedFuelItem.getName().equals("Coal")) {
                // Cek apakah pemain punya cukup bahan untuk porsi kedua
                boolean canMakeSecondServing = true;
                for (Map.Entry<String, Integer> entry : selectedRecipe.getIngredients().entrySet()) {
                    Item ingredient = itemRegistry.get(entry.getKey());
                    if (ingredient == null || !player.getInventory().hasItem(ingredient, entry.getValue())) {
                        canMakeSecondServing = false;
                        break;
                    }
                }

                if (canMakeSecondServing) {
                    // Kurangi bahan untuk porsi kedua
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
            // TODO: Ganti dengan GameTaskManager yang sebenarnya
            final int finalServings = servingsMade;
            // Untuk sekarang, kita simulasikan selesai langsung untuk testing, tapi idealnya pakai TaskManager
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
        if (farmModel == null || farmModel.getNPCs() == null || gamePanel == null || player == null) { 
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

        Optional<NPC> nearbyNPCOptional = farmModel.getNPCs().stream()
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
        if (farmModel == null || farmModel.getNPCs() == null || player == null || player.getCurrentMap() == null) {
            System.err.println("GameController.findTargetNPCForInteraction: Komponen kritis null.");
            return null;
        }
        MapArea playerMap = player.getCurrentMap();
        int playerX = player.getCurrentTileX();
        int playerY = player.getCurrentTileY();

        Optional<NPC> nearbyNPCOptional = farmModel.getNPCs().stream()
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
        // No checkPassOut() needed here as sleep PREVENTS pass out by ending the day.
        // GamePanel updates should be handled by showEndOfDayMessage or by Farm.nextDay() if it triggers repaints.
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

        // Condition 1: Player must be in their house on the FarmMap
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

        // Get current day's weather
        Weather currentDayWeather = gameTime.getCurrentWeather();
        String weatherMessage = "Today's weather: " + currentDayWeather.toString();
        gamePanel.displayMessage(weatherMessage);
        System.out.println("TV: " + weatherMessage);

        // Advance game time by 5 minutes for watching TV (Specification for Action #12 is -15 minutes, but notes said 5)
        // Correcting to -15 minutes from specification.
        gameTime.advance(15); // Specification: -15 menit dalam game
        player.changeEnergy(-5); // Specification: -5 energi

        if (gamePanel != null) {
            gamePanel.updatePlayerInfoPanel(); // Update time and energy display
            gamePanel.updateGameRender(); // Redraw if needed
        }
        checkPassOut(); // Check if player passes out due to energy loss
    }

    /**
     * Requests the display of end-of-game statistics.
     * This will fetch the summary from EndGameStatistics and tell GamePanel to show it.
     * It will also stop the game timer in GamePanel.
     */
    public void requestShowStatistics() {
        if (farmModel != null && farmModel.getStatistics() != null) {
            farmModel.setCurrentGameState(GameState.STATISTICS_VIEW);
            // GamePanel will handle fetching info and drawing. GamePanel should also stop timer.
            if (gamePanel != null) {
                gamePanel.stopGameTimer(); // Ensure timer is stopped when stats are shown
                gamePanel.repaint(); // Trigger repaint for UI
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
            // GamePanel will handle fetching info and drawing
            if (gamePanel != null) gamePanel.repaint(); // Trigger repaint to show UI
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
            // Basic validation, GameTime.setTime will also validate
            if (hour >= 0 && hour < GameTime.HOURS_IN_DAY && minute >= 0 && minute < GameTime.MINUTES_IN_HOUR) {
                gameTime.setTime(hour, minute);
                if (gamePanel != null) {
                    gamePanel.updateGameRender(); // Ensure the display updates immediately
                }
                return true;
            }
        }
        return false;
    }

    // --- Shipping Bin UI Interaction --- 

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
            // This check is also in GamePanel.tryOpenShippingBinDialog, but good to have a safeguard
            gamePanel.shippingBinActionFailed("You have already used the shipping bin today.");
            return;
        }

        farmModel.getCurrentTime().setPaused(true);
        farmModel.setCurrentGameState(GameState.SHIPPING_BIN);
        gamePanel.openShippingBinUI();
        // gamePanel.setShippingBinFeedback("Select item, [Enter] for quantity, [Esc] to close.", false);
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
                // Failed to add to bin (should be rare if pre-checks are done, but good to handle)
                // Re-add to player inventory as a rollback
                inventory.addItem(item, quantity);
                gamePanel.shippingBinActionFailed("Could not add " + item.getName() + " to bin. Bin might be full.");
            }
        } else {
            // Should not happen if hasItem check passed, but as a fallback
            gamePanel.shippingBinActionFailed("Failed to remove " + item.getName() + " from inventory.");
        }
    }

    public void requestCloseShippingBin() {
        if (farmModel == null || gamePanel == null) {
            System.err.println("GameController: Critical model/view component missing for closing shipping bin.");
            return;
        }
        // Finalize sale session logic (e.g. marking bin as used for the day)
        // is done within ShippingBin.addItem() or when canSellToday() is checked.
        // ShippingBin.processSales() will handle the money and clearing at day end.
        
        farmModel.getCurrentTime().advance(15); // Changed from advanceTime(15)
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
            // Optionally, update GamePanel if it needs to know about this change immediately
            // For instance, if the HUD needs to refresh. GamePanel.updatePlayerInfoPanel() or similar.
            if (gamePanel != null) {
                gamePanel.updatePlayerInfoPanel(); // Or a more general updateGameRender()
            }
        }
    }

} // End of GameController class
