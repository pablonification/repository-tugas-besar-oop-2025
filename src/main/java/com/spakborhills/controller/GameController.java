package com.spakborhills.controller;

import com.spakborhills.model.Farm;
import com.spakborhills.model.Player;
import com.spakborhills.model.Store;
import com.spakborhills.model.Util.PriceList;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Item.Seed;
import com.spakborhills.model.Item.EdibleItem;
import com.spakborhills.model.Enum.Direction;
import com.spakborhills.model.Enum.Season;
import com.spakborhills.model.Enum.TileType;
import com.spakborhills.model.Map.FarmMap;
import com.spakborhills.model.Map.Tile;
import com.spakborhills.model.Util.GameTime;
import com.spakborhills.model.Util.ShippingBin;
import java.util.Map;
import java.util.List; // For returning list of items
import java.util.ArrayList; // For creating list of items
import java.util.stream.Collectors; // Diperlukan untuk stream
import com.spakborhills.model.Item.Equipment;
import java.util.Collections;
import java.util.Comparator;
// GamePanel might be needed later for more complex interactions or direct view updates
import com.spakborhills.view.GamePanel;
// GameTime might be needed if Farm.nextDay() isn't comprehensive enough for all time updates
// import com.spakborhills.model.GameTime; 
import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Map.MapArea;

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
        FarmMap farmMap = farmModel.getFarmMap();

        if (player == null || farmMap == null) {
            System.err.println("GameController: Player or FarmMap is null, cannot till land.");
            return false;
        }

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
        FarmMap farmMap = farmModel.getFarmMap();
        GameTime gameTime = farmModel.getCurrentTime();

        if (player == null || farmMap == null || gameTime == null) {
            System.err.println("GameController: Player, FarmMap, or GameTime is null, cannot plant.");
            return false;
        }

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
        FarmMap farmMap = farmModel.getFarmMap();
        GameTime gameTime = farmModel.getCurrentTime(); // Dipertahankan jika Player.water() membutuhkannya di masa depan, atau untuk konsistensi

        if (player == null || farmMap == null || gameTime == null) {
            System.err.println("GameController: Player, FarmMap, or GameTime is null, cannot water tile.");
            return false;
        }

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
        FarmMap farmMap = farmModel.getFarmMap();
        Map<String, Item> itemRegistry = farmModel.getItemRegistry(); // For Player.harvest()

        if (player == null || farmMap == null || itemRegistry == null) {
            System.err.println("GameController: Player, FarmMap, or ItemRegistry is null, cannot harvest.");
            return false;
        }

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
        boolean harvested = player.harvest(targetTile, itemRegistry);

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
        FarmMap farmMap = farmModel.getFarmMap();

        if (player == null || farmMap == null) {
            System.err.println("GameController: Player or FarmMap is null, cannot recover land.");
            return false;
        }

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
} 