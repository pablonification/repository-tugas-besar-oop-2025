package com.spakborhills.controller;

import java.util.ArrayList; // For creating list of items
import java.util.Collections;
import java.util.Comparator;
import java.util.List; // For returning list of items
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.awt.Point; // Added for Point

import javax.swing.JOptionPane;

import com.spakborhills.model.Farm;
import com.spakborhills.model.Player;
import com.spakborhills.model.Store;
import com.spakborhills.model.Enum.Direction;
import com.spakborhills.model.Enum.FishRarity;
// GameTime might be needed if Farm.nextDay() isn't comprehensive enough for all time updates
// import com.spakborhills.model.GameTime; 
import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Enum.RelationshipStatus;
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
import com.spakborhills.model.Object.DeployedObject; // Added import
import com.spakborhills.model.Object.House; // Added import

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
                gamePanel.showEndOfDayMessage(eventMessage, incomeFromSales, newDayInfo);
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
        if (farmModel == null || farmModel.getStore() == null || farmModel.getPriceList() == null) {
            System.err.println("GameController: Farm, Store, or PriceList is null. Cannot fetch store items.");
            return new ArrayList<>(); // Return empty list to prevent null pointer in UI
        }
        Store store = farmModel.getStore();
        // The ItemRegistry is needed by store.getAvailableItemsForDisplay
        // Assuming farmModel can provide access to something like an ItemRegistry if store needs it directly
        // For now, let's assume ItemRegistry is implicitly handled or Main.setupItemRegistry() is the source of truth
        // and Store's getAvailableItemsForDisplay can work with the farm's pricelist.
        // The method signature in Main.java test case for Store was: 
        // store.getAvailableItemsForDisplay(itemRegistry, priceList)
        // We need ItemRegistry. We can get it from Farm if Farm stores it, or pass from Main.
        // Let's assume Farm has a way to get the itemRegistry, or Store is initialized with it.
        // For now, this controller method will rely on the Store object having what it needs.
        // A more robust way would be for Farm to hold the ItemRegistry.
        // Let's assume farm.getItemRegistry() exists for now. If not, we'll need to adjust.
        Map<String, Item> itemRegistry = farmModel.getItemRegistry(); // ASSUMPTION: Farm has this getter
        if (itemRegistry == null) {
             System.err.println("GameController: ItemRegistry is null in Farm. Cannot fetch store items.");
            return new ArrayList<>();
        }

        return store.getAvailableItemsForDisplay(itemRegistry, farmModel.getPriceList());
    }

    /**
     * Handles the player's request to buy an item from the store.
     * @param itemName The name of the item to buy.
     * @param quantity The quantity to buy.
     * @return true if the purchase was successful, false otherwise.
     */
    public boolean requestBuyItem(String itemName, int quantity) {
        if (farmModel == null || farmModel.getStore() == null || farmModel.getPlayer() == null || 
            farmModel.getPriceList() == null || farmModel.getItemRegistry() == null) { // Added itemRegistry check
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
        Map<String, Item> itemRegistry = farmModel.getItemRegistry(); // ASSUMPTION: Farm has this getter

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
                String normalizedItemName = itemName.toUpperCase().replace(" ", "_");
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
        if (farmModel == null || farmModel.getPlayer() == null || farmModel.getPlayer().getInventory() == null) {
            return new ArrayList<>();
        }
        Player player = farmModel.getPlayer();
        // Mengambil semua item unik dari inventory
        List<Item> items = new ArrayList<>(player.getInventory().getItems().keySet());
        
        // Urutkan item berdasarkan nama untuk konsistensi tampilan/pemilihan
        // Bisa juga diurutkan berdasarkan kategori atau kriteria lain jika perlu
        Collections.sort(items, Comparator.comparing(Item::getName));
        return items;
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
        MapArea targetMap = farmModel.getMapArea(destination);

        if (targetMap == null) {
            System.err.println("GameController: Target map for destination " + destination + " is null.");
            if (gamePanel != null) {
                // Show a message to the player if the map isn't available
                javax.swing.JOptionPane.showMessageDialog(gamePanel, 
                    "The location '" + destination.toString() + "' is not accessible yet.", 
                    "Cannot Visit", 
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            }
            return false;
        }

        // Determine entry point. For now, center of the map.
        // A more robust solution would involve predefined entry points for each map/transition.
        int entryX = 0;
        int entryY = 0;
        if (targetMap.getSize() != null) {
            entryX = targetMap.getSize().width / 2;
            entryY = targetMap.getSize().height / 2;
        }
        
        // Ensure entry points are within bounds, especially for very small maps
        if (targetMap.getSize() != null) {
            if (entryX >= targetMap.getSize().width) entryX = Math.max(0, targetMap.getSize().width - 1);
            if (entryY >= targetMap.getSize().height) entryY = Math.max(0, targetMap.getSize().height - 1);
        }


        boolean visited = player.visit(targetMap, entryX, entryY);

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
        if (player.getCurrentMap() instanceof com.spakborhills.model.Map.PlayerHouseInterior) {
            canCookLocation = true;
        } else if (player.getCurrentMap() instanceof FarmMap) {
            FarmMap farmMap = (FarmMap) player.getCurrentMap();
            if (findAdjacentHouse(player, farmMap) != null) {
                canCookLocation = true;
            }
        }

        if (!canCookLocation) {
            gamePanel.displayMessage("Kamu hanya bisa memasak di dalam rumah atau di dekat rumah (di kebun).");
            return;
        }
        // TODO: Jika ada bonus Stove, tambahkan pengecekan isNearStove di PlayerHouseInterior.

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
        String[] fuelOptions = {"Coal", "Firewood"};
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
            gamePanel.displayMessage("Error: Player atau GameTime tidak siap.");
            return;
        }

        // 1. Find a nearby NPC
        NPC targetNPC = findNearbyNPCForChat(player);

        if (targetNPC == null) {
            return;
        }

        // Special handling for Emily
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
                    gamePanel.showNPCDialogue(targetNPC.getName(), dialogue);
                }
            } else if (choice == 1) { // Open Store
                gamePanel.openStoreDialog();
            }
        } else {
            // Existing chat logic for other NPCs
            MapArea npcCurrentMap = player.getCurrentMap();
            boolean chatSuccess = player.chat(targetNPC, gameTime, npcCurrentMap);

            if (chatSuccess) {
                String dialogue = targetNPC.getDialogue(player);
                gamePanel.showNPCDialogue(targetNPC.getName(), dialogue);
            }
        }
        gamePanel.updatePlayerInfoPanel();
        gamePanel.updateGameRender();
    }

    /**
     * Finds an NPC within chat range of the player.
     * Displays a message via GamePanel if no NPC is found.
     *
     * @param player The player performing the action.
     * @return The found NPC, or null if no NPC is in range.
     */
    private NPC findNearbyNPCForChat(Player player) {
        if (farmModel == null || farmModel.getNPCs() == null || gamePanel == null || player == null) { // Added player null check
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
                // Check 1: Is the NPC supposed to be on the player's current map?
                // This assumes NPCs are generally found at their homeLocation.
                // More complex roaming would require NPCs to store their current actual map.
                MapArea npcMapContext = farmModel.getMapArea(npc.getHomeLocation());
                if (npcMapContext != playerMap) {
                    // Special case: If player is on FarmMap, NPCs might visit.
                    // This needs a more robust "NPC is currently on X map" flag or list.
                    // For now, if homeLocation doesn't match player's map, we assume they are not there for chat,
                    // UNLESS the player is on a generic map and the NPC is somewhere specific (e.g. Emily in Store)
                    // OR if we add logic for NPCs visiting the FarmMap.
                    // A simple initial rule: if the player is on a specific NPC_HOME map, only that NPC is findable.
                    // If player is on FARM_MAP, any NPC *could* be there if their coordinates are updated for FarmMap.
                    // This current filter is strict: NPC must reside on the map player is currently on.
                    return false; 
                }

                // Check 2: Proximity on that map
                int npcX = npc.getCurrentTileX(); // These are coordinates within their npcMapContext
                int npcY = npc.getCurrentTileY();
                
                int distance = Math.abs(playerX - npcX) + Math.abs(playerY - npcY);
                return distance <= Player.CHAT_MAX_DISTANCE;
            })
            .min(Comparator.comparingInt(npc -> { // Find the closest one
                // Distance calculation needs to be relative to the common map (playerMap)
                // NPC coordinates (npc.getCurrentTileX/Y) are assumed to be valid for playerMap if npcMapContext == playerMap
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

        // Check energy first before proceeding
        final int GIFT_ENERGY_COST = 5;
        if (player.getEnergy() < GIFT_ENERGY_COST) {
            gamePanel.displayMessage("Energi tidak cukup untuk memberi hadiah (butuh " + GIFT_ENERGY_COST + ", punya " + player.getEnergy() + ").");
            return;
        }

        NPC targetNPC = findNearbyNPCForChat(player); // Reuse for finding gift target

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
            gamePanel.showNPCDialogue(targetNPC.getName(), reaction); 
            System.out.println("Gift successful. NPC: " + targetNPC.getName() + ", Item: " + itemToGift.getName());
        } else {
            System.out.println("Gifting failed. See Player.gift() logs for details.");
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

    /**
     * Handles the player's request to enter their house.
     */
    public void handleEnterHouseRequest() {
        if (farmModel == null || gamePanel == null) {
            System.err.println("GameController: Farm model or GamePanel is null. Cannot handle enter house request.");
            return;
        }
        Player player = farmModel.getPlayer();
        if (player == null) {
            System.err.println("GameController: Player is null. Cannot handle enter house request.");
            return;
        }

        // Check 1: Player must be on the FarmMap
        if (!(player.getCurrentMap() instanceof FarmMap)) {
            // gamePanel.displayMessage("Kamu tidak bisa masuk rumah dari sini."); // Optional message
            return; // Silently fail or provide feedback
        }
        FarmMap farmMap = (FarmMap) player.getCurrentMap();

        // Check 2: Player must be adjacent to the House object
        DeployedObject houseObject = findAdjacentHouse(player, farmMap);
        if (houseObject == null) {
            // gamePanel.displayMessage("Tidak ada rumah di dekatmu untuk dimasuki."); // Optional message
            return; // Silently fail or provide feedback
        }

        // Transition to PlayerHouseInterior map
        MapArea houseInteriorMap = farmModel.getMapArea(LocationType.PLAYER_HOUSE_INTERIOR);
        if (houseInteriorMap == null) {
            System.err.println("GameController: PlayerHouseInterior map not found in Farm model.");
            gamePanel.displayMessage("Error: Interior rumah tidak ditemukan.");
            return;
        }

        List<Point> entryPoints = houseInteriorMap.getEntryPoints();
        if (entryPoints.isEmpty()) {
            System.err.println("GameController: PlayerHouseInterior map has no entry points defined.");
            gamePanel.displayMessage("Error: Tidak ada titik masuk ke interior rumah.");
            return;
        }
        Point entryPoint = entryPoints.get(0); // Use the first defined entry point

        if (player.visit(houseInteriorMap, entryPoint.x, entryPoint.y)) {
            System.out.println("Player entered house. Now on map: " + player.getCurrentMap().getName());
            // No energy or time cost for entering house specified, can be added here if needed.
            if (gamePanel != null) {
                gamePanel.repaint(); // Update view to show house interior
            }
        } else {
            gamePanel.displayMessage("Gagal masuk rumah.");
        }
    }

    /**
     * Helper method to find a House object adjacent to the player on the FarmMap.
     * @param player The player.
     * @param farmMap The FarmMap.
     * @return The House object if found and adjacent, null otherwise.
     */
    private DeployedObject findAdjacentHouse(Player player, FarmMap farmMap) {
        int playerX = player.getCurrentTileX();
        int playerY = player.getCurrentTileY();
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}}; // N, S, W, E

        for (int[] dir : directions) {
            int checkX = playerX + dir[0];
            int checkY = playerY + dir[1];

            if (farmMap.isWithinBounds(checkX, checkY)) {
                DeployedObject obj = farmMap.getObjectAt(checkX, checkY);
                if (obj instanceof House) {
                    return obj; // Found adjacent House
                }
            }
        }
        return null; // No adjacent House found
    }

    /**
     * Handles the player's request to sleep normally in their house.
     */
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
        if (!(player.getCurrentMap() instanceof com.spakborhills.model.Map.PlayerHouseInterior)) {
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
        
        gamePanel.showEndOfDayMessage(eventMessage, incomeFromSales, newDayInfo);
        // No checkPassOut() needed here as sleep PREVENTS pass out by ending the day.
        // GamePanel updates should be handled by showEndOfDayMessage or by Farm.nextDay() if it triggers repaints.
    }
}
