package com.spakborhills.controller;

import com.spakborhills.model.Farm;
import com.spakborhills.model.Player;
import com.spakborhills.model.Store;
import com.spakborhills.model.Util.PriceList;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Item.Seed;
import com.spakborhills.model.Enum.Direction;
import com.spakborhills.model.Enum.Season;
import com.spakborhills.model.Enum.TileType;
import com.spakborhills.model.Map.FarmMap;
import com.spakborhills.model.Map.Tile;
import com.spakborhills.model.Util.GameTime;
import java.util.Map;
import java.util.List; // For returning list of items
import java.util.ArrayList; // For creating list of items
// GamePanel might be needed later for more complex interactions or direct view updates
// import com.spakborhills.view.GamePanel;
// GameTime might be needed if Farm.nextDay() isn't comprehensive enough for all time updates
// import com.spakborhills.model.GameTime; 

public class GameController {

    private Farm farmModel;
    // private GamePanel gamePanel; // If needed later

    public GameController(Farm farmModel) {
        this.farmModel = farmModel;
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
            // Assuming move() might cost energy and could lead to pass out
            // Or, if move has no energy cost, this check is only relevant for actions like till.
            // For now, let's keep it simple and check after any successful state change.
            // If player.move() itself can reduce energy to MIN_ENERGY and the game rules dictate
            // passing out even from movement, this check here is appropriate.
            // If move() doesn't cost energy, this specific call to checkPassOut() after move might be redundant
            // unless other side effects of move could trigger it.
            // Based on spec, 'Moving' action does not have energy cost listed directly, but player.changeEnergy is a general mechanic.
            // Let's assume for now that only explicit energy-costing actions are the primary trigger for pass out for simplicity.
            // So, we will call checkPassOut from the action handlers like requestTillLandAtPlayerPosition.
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

        // Prevent action if already passed out (at min energy)
        if (player.getEnergy() <= Player.MIN_ENERGY) {
            System.out.println("Player is too tired to till land.");
            return false; // Return false as no state changed that needs repaint for *this* action
        }

        Tile targetTile = farmMap.getTile(player.getCurrentTileX(), player.getCurrentTileY());
        if (targetTile == null) {
            System.err.println("GameController: Tile at player position is null.");
            return false;
        }

        boolean tilled = player.till(targetTile);
        if (tilled) {
            checkPassOut(); // Check for pass out condition after successful tilling
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
            // If gameTime is null here, it's likely due to an incorrect import or initialization issue elsewhere.
            return false;
        }

        if (player.getEnergy() <= Player.MIN_ENERGY) {
            System.out.println("Player is too tired to plant.");
            return false;
        }

        Tile targetTile = farmMap.getTile(player.getCurrentTileX(), player.getCurrentTileY());
        if (targetTile == null || targetTile.getType() != TileType.TILLED) {
            return false; 
        }

        Seed seedToPlant = null;
        if (player.getInventory() != null && player.getInventory().getItems() != null) {
            for (Map.Entry<Item, Integer> entry : player.getInventory().getItems().entrySet()) {
                if (entry.getKey() instanceof Seed && entry.getValue() > 0) {
                    Seed currentSeed = (Seed) entry.getKey();
                    if (currentSeed.getTargetSeason() == gameTime.getCurrentSeason() || 
                        currentSeed.getTargetSeason() == Season.ANY) {
                        seedToPlant = currentSeed;
                        break;
                    }
                }
            }
        }

        if (seedToPlant == null) {
            return false;
        }

        boolean planted = player.plant(seedToPlant, targetTile, gameTime);
        if (planted) {
            System.out.println("Planted " + seedToPlant.getName());
            checkPassOut();
        }
        return planted;
    }

    /**
     * Checks if the player should pass out due to low energy.
     * If so, initiates the pass out sequence (sleep, next day).
     */
    private void checkPassOut() {
        if (farmModel == null) return;
        Player player = farmModel.getPlayer();
        if (player == null) return;

        if (player.getEnergy() <= Player.MIN_ENERGY) {
            System.out.println("Player has passed out from exhaustion!");
            // Player.sleep() handles energy restoration with penalty if applicable.
            // The energy value passed to sleep is the one *before* sleep, used for penalty calculation.
            // Since we are already at MIN_ENERGY, this value is correct for the penalty check.
            player.sleep(player.getEnergy(), false); // false for not using a bonus bed
            
            farmModel.nextDay(); // Advance to the next day, update weather, crops etc.
            
            System.out.println("A new day has begun. Player energy: " + player.getEnergy());
            // The repaint will be handled by the GamePanel's keyPressed method because
            // requestTillLandAtPlayerPosition (which calls this) will return true.
        }
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
} 