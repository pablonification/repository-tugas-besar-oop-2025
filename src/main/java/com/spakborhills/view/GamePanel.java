package com.spakborhills.view;

import com.spakborhills.model.Farm; // Import Farm
import com.spakborhills.model.Map.FarmMap;
import com.spakborhills.model.Map.Tile;
import com.spakborhills.model.Enum.TileType;
import com.spakborhills.model.Player; // Import Player
import com.spakborhills.model.Enum.Direction; // Import Direction
import com.spakborhills.controller.GameController; // Import GameController
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Item.Seed;
import com.spakborhills.model.Item.Equipment; // For filtering, not sold
import com.spakborhills.model.Item.Crop; // Sellable
import com.spakborhills.model.Item.Fish; // Sellable
import com.spakborhills.model.Item.Food; // Sellable
import com.spakborhills.model.Item.MiscItem; // Sellable
import com.spakborhills.model.Util.PriceList; // For getting item prices in store
import com.spakborhills.model.Object.ShippingBinObject; // For checking instance
import com.spakborhills.model.Util.Inventory;
import com.spakborhills.model.Enum.Weather; // Tambahkan impor untuk Weather
import com.spakborhills.model.Enum.Season; // Tambahkan impor untuk Season

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent; // Import KeyEvent
import java.awt.event.KeyListener; // Import KeyListener
import java.util.List;
import java.util.ArrayList; // For creating list of sellable items
import java.util.Map; // For iterating inventory

public class GamePanel extends JPanel implements KeyListener { // Implement KeyListener

    private static final int TILE_SIZE = 32;
    private static final int INFO_PANEL_HEIGHT = 80; // Increased height for more info
    private Farm farmModel;
    private GameController gameController;
    private static final Font DIALOG_FONT = new Font("Arial", Font.PLAIN, 18); // Added font for dialogs

    public GamePanel(Farm farmModel, GameController gameController) {
        this.farmModel = farmModel;
        this.gameController = gameController;

        setPreferredSize(new Dimension(farmModel.getFarmMap().getSize().width * TILE_SIZE, 
                                       farmModel.getFarmMap().getSize().height * TILE_SIZE + INFO_PANEL_HEIGHT));
        setBackground(Color.GRAY);
        addKeyListener(this);
        setFocusable(true); // Important to receive key events

        // Set default font for JOptionPane dialogs
        UIManager.put("OptionPane.messageFont", DIALOG_FONT);
        UIManager.put("OptionPane.buttonFont", DIALOG_FONT);
        UIManager.put("TextField.font", DIALOG_FONT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw Player Info at the top
        drawPlayerInfo(g);

        // Translate graphics context for map and player drawing below info panel
        Graphics g2 = g.create(); // Create a new graphics context to not affect info panel
        g2.translate(0, INFO_PANEL_HEIGHT);

        if (farmModel != null && farmModel.getFarmMap() != null) {
            drawFarmMap((Graphics) g2); // Cast to Graphics
        }
        if (farmModel != null && farmModel.getPlayer() != null) {
            drawPlayer((Graphics) g2); // Cast to Graphics
        }
        g2.dispose(); // Dispose of the translated graphics context
    }

    private void drawPlayerInfo(Graphics g) {
        if (farmModel == null || farmModel.getPlayer() == null) {
            return;
        }
        Player player = farmModel.getPlayer();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), INFO_PANEL_HEIGHT); // Background for info panel

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20)); // Increased font size and made it bold

        // Baris 1: Nama, Energi, Gold
        String playerStats = String.format("Name: %s | Energy: %d | Gold: %d G",
                                    player.getName(), player.getEnergy(), player.getGold());
        g.drawString(playerStats, 10, 20);

        // Baris 2: Waktu, Hari, Musim, Cuaca
        String timeInfo = String.format("Time: %s | Day: %d | Season: %s | Weather: %s",
                                    farmModel.getCurrentTime().getTimeString(),
                                    farmModel.getCurrentTime().getCurrentDay(),
                                    farmModel.getCurrentTime().getCurrentSeason(),
                                    farmModel.getCurrentTime().getCurrentWeather());
        g.drawString(timeInfo, 10, 40); // Adjusted Y position for larger font

        // Baris 3: Selected Item
        Item selectedItem = player.getSelectedItem();
        String selectedItemName = (selectedItem != null) ? selectedItem.getName() : "None";
        g.drawString("Selected: " + selectedItemName, 10, 60); // Adjusted Y position for larger font

        // Baris 4: Inventory Hotbar (menggantikan Toolbelt)
        if (gameController != null) {
            List<Item> allPlayerItems = gameController.getPlayerInventoryItems(); // Menggunakan metode baru
            StringBuilder hotbarString = new StringBuilder("Items: ");
            if (allPlayerItems.isEmpty()) {
                hotbarString.append("Inventory Empty");
            } else {
                // Tampilkan maksimal N item pertama untuk menghindari string terlalu panjang
                // Atau bisa dibuat lebih canggih untuk menampilkan item di sekitar selectedItem
                int displayLimit = 5; // Tampilkan maksimal 5 item di hotbar teks ini
                for (int i = 0; i < allPlayerItems.size(); i++) {
                    if (i >= displayLimit && !(selectedItem != null && allPlayerItems.get(i).equals(selectedItem))) {
                        // Jika sudah mencapai batas dan item saat ini bukan yang terpilih, cek apakah selectedItem ada di sisa list
                        boolean selectedItemFurther = false;
                        if (selectedItem != null) {
                            for (int j = i; j < allPlayerItems.size(); j++) {
                                if (allPlayerItems.get(j).equals(selectedItem)) {
                                    selectedItemFurther = true;
                                    break;
                                }
                            }
                        }
                        if (!selectedItemFurther || i > displayLimit + 2) { // Beri sedikit ruang jika selected item jauh
                            hotbarString.append("... (" + (allPlayerItems.size() - i) + " more)");
                            break;
                        }
                    }

                    Item currentItem = allPlayerItems.get(i);
                    boolean isSelected = (selectedItem != null && selectedItem.equals(currentItem));
                    if (isSelected) {
                        hotbarString.append("[");
                    }
                    hotbarString.append(currentItem.getName());
                    if (isSelected) {
                        hotbarString.append("]");
                    }
                    if (i < allPlayerItems.size() - 1 && (i < displayLimit -1 || (selectedItem !=null && allPlayerItems.get(i+1).equals(selectedItem) ) ) ) {
                         // Hanya tambah separator jika belum item terakhir DAN (masih dalam batas ATAU item berikutnya adalah yg terpilih)
                        hotbarString.append(" | ");
                    }
                }
            }
            g.drawString(hotbarString.toString(), 10, 80);
        }
    }

    private void drawFarmMap(Graphics g) {
        FarmMap map = farmModel.getFarmMap();
        for (int row = 0; row < map.getSize().height; row++) {
            for (int col = 0; col < map.getSize().width; col++) {
                Tile tile = map.getTile(col, row);
                if (tile != null) {
                    Color tileColor;
                    switch (tile.getType()) {
                        case TileType.GRASS:
                            tileColor = new Color(34, 177, 76); // Green
                            break;
                        case TileType.TILLABLE:
                            tileColor = new Color(181, 101, 29); // Brown
                            break;
                        case TileType.TILLED:
                            tileColor = new Color(139, 69, 19); // Darker Brown
                            if (tile.isWatered()) {
                                tileColor = new Color(90, 45, 10); // Darker, wet brown
                            }
                            break;
                        case TileType.PLANTED:
                            Seed currentSeed = tile.getPlantedSeed();
                            if (currentSeed != null) {
                                if (tile.isHarvestable()) {
                                    tileColor = Color.YELLOW; // Ready to harvest - Distinct color
                                } else {
                                    // Calculate growth progress for distinct colors
                                    // Ensure daysToHarvest is not zero to avoid division by zero if seed data is incorrect
                                    double growthProgress = 0;
                                    if (currentSeed.getDaysToHarvest() > 0) {
                                         growthProgress = (double) tile.getGrowthDays() / currentSeed.getDaysToHarvest();
                                    }

                                    if (growthProgress < 0.33) {
                                        tileColor = new Color(152, 251, 152); // PaleGreen - Tahap Awal
                                    } else if (growthProgress < 0.66) {
                                        tileColor = new Color(60, 179, 113); // MediumSeaGreen - Tahap Tengah
                                    } else if (growthProgress < 1.0) { // Less than 1.0, as 1.0 should be YELLOW (harvestable)
                                        tileColor = new Color(34, 139, 34);  // ForestGreen - Tahap Akhir (matang, belum siap panen)
                                    } else { // Should ideally be caught by isHarvestable, but as a fallback for >= 1.0
                                        tileColor = Color.YELLOW; // Fallback to harvestable color
                                    }

                                    if (tile.isWatered()) {
                                        // Darken the color slightly if watered to show it's wet
                                        // but ensure it's still distinguishable from the next growth stage
                                        // One simple way: make it a bit darker, but not too much.
                                        // Or, add a small blueish tint if complex color mixing is desired.
                                        // For simplicity, let's use darker(). It might blend for very dark greens.
                                        // A more robust way would be to define specific watered colors for each stage.
                                        tileColor = tileColor.darker();
                                    }
                                }
                            } else {
                                // Fallback if seed is somehow null but type is PLANTED (should not happen ideally)
                                tileColor = Color.PINK; // Error color
                            }
                            break;
                        case TileType.OBSTACLE:
                            tileColor = Color.DARK_GRAY;
                            break;
                        case TileType.WATER:
                            tileColor = Color.BLUE;
                            break;
                        case TileType.ENTRY_POINT:
                            tileColor = Color.ORANGE;
                            break;
                        default:
                            tileColor = Color.LIGHT_GRAY;
                            break;
                    }
                    g.setColor(tileColor);
                    g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    g.setColor(Color.BLACK); // Border
                    g.drawRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    private void drawPlayer(Graphics g) {
        Player player = farmModel.getPlayer();
        g.setColor(Color.RED);
        // Player is drawn as an oval, slightly smaller than the tile size
        int playerSize = TILE_SIZE - (TILE_SIZE / 4); 
        int playerOffset = (TILE_SIZE - playerSize) / 2;
        g.fillOval(player.getCurrentTileX() * TILE_SIZE + playerOffset, 
                   player.getCurrentTileY() * TILE_SIZE + playerOffset, 
                   playerSize, playerSize);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameController == null) return;

        boolean actionTaken = false;
        int keyCode = e.getKeyCode();

        switch (keyCode) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                actionTaken = gameController.requestPlayerMove(Direction.NORTH);
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                actionTaken = gameController.requestPlayerMove(Direction.SOUTH);
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                actionTaken = gameController.requestPlayerMove(Direction.WEST);
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                actionTaken = gameController.requestPlayerMove(Direction.EAST);
                break;
            case KeyEvent.VK_E: // Action key
                actionTaken = tryGeneralAction();
                break;
            case KeyEvent.VK_R: // Alternative water key (jika ada)
                // Bisa juga ditambahkan untuk aksi spesifik jika E sudah terlalu umum
                // Jika diaktifkan, pastikan requestWaterTileAtPlayerPosition mengembalikan boolean
                // actionTaken = gameController.requestWaterTileAtPlayerPosition(); 
                break;
            case KeyEvent.VK_1:
                gameController.selectPreviousItem();
                actionTaken = true; // Selecting an item is an action that needs repaint
                break;
            case KeyEvent.VK_2:
                gameController.selectNextItem();
                actionTaken = true; // Selecting an item is an action that needs repaint
                break;
            case KeyEvent.VK_C: // Cheat key
                handleCheatInput();
                actionTaken = true; // Cheat input is an action that might change display (weather)
                break;
            case KeyEvent.VK_P: // 'P' for Open Store
                openStoreDialog(); // Assumed to be void, dialog handles its own flow
                actionTaken = true; // Opening a dialog is an interaction
                break;
            case KeyEvent.VK_B: // 'B' for Shipping Bin interaction
                actionTaken = tryOpenShippingBinDialog(); // This method already returns boolean
                break;
            case KeyEvent.VK_F: // 'F' for Eat action
                if (gameController != null) {
                    actionTaken = gameController.requestEatSelectedItem();
                    if (actionTaken) {
                        System.out.println("GamePanel: Eat action initiated by F key.");
                    } else {
                        // Optional: feedback jika makan gagal (misal, item tidak bisa dimakan, energi penuh)
                        System.out.println("GamePanel: Eat action failed or not applicable.");
                    }
                }
                break;
            // Tambahkan case lain jika perlu
        }

        if (actionTaken) {
            repaint(); // Repaint the panel if an action was taken that might change the state
        }
    }

    private boolean tryGeneralAction() {
        Player player = farmModel.getPlayer();
        if (player == null) return false;
        Item currentItem = player.getSelectedItem();
        boolean actionProcessed = false;

        if (currentItem == null) {
            // If no item is selected, try harvesting as a default action.
            return gameController.requestHarvestAtPlayerPosition();
        }

        if (currentItem instanceof Equipment) {
            String itemName = currentItem.getName();
            if (itemName.equals("Hoe")) {
                actionProcessed = gameController.requestTillLandAtPlayerPosition();
            } else if (itemName.equals("Watering Can")) {
                actionProcessed = gameController.requestWaterTileAtPlayerPosition();
            } else if (itemName.equals("Pickaxe")) {
                actionProcessed = gameController.requestRecoverLandAtPlayerPosition();
            } else if (itemName.equals("Fishing Rod")) {
                // TODO: Implement fishing logic trigger here or in a dedicated fishing spot interaction
                System.out.println("Fishing Rod selected. Implement fishing action or interaction with water tile.");
                // actionProcessed = gameController.requestFish(); // Example if such method exists
            }
        } else if (currentItem instanceof Seed) {
            actionProcessed = gameController.requestPlantSeedAtPlayerPosition();
        } else if (currentItem instanceof Food) { // Assuming Food is an EdibleItem
            // TODO: Implement eating logic. Maybe a different key for eating like 'F'.
            // For now, 'E' won't trigger eating if it's food.
            // actionProcessed = gameController.requestEatSelectedItem();
            System.out.println("Food item selected: " + currentItem.getName() + ". Press a dedicated key (e.g., F) to eat.");
        }
        // Add other item type checks if necessary (e.g., for EdibleItem that is not Food)

        if (actionProcessed) {
            return true;
        } else {
            // Fallback: if no specific item action was processed OR currentItem was not actionable with 'E',
            // try harvesting. This makes 'E' also a harvest key if standing on a harvestable plant.
            return gameController.requestHarvestAtPlayerPosition();
        }
    }

    private void openStoreDialog() {
        if (gameController == null || farmModel == null || farmModel.getPriceList() == null) {
            JOptionPane.showMessageDialog(this, "Store is currently unavailable.", "Store Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Item> storeItems = gameController.getStoreItemsForDisplay();
        if (storeItems == null || storeItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No items available in the store.", "Store Empty", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        PriceList priceList = farmModel.getPriceList();

        StringBuilder storeItemList = new StringBuilder("Welcome to the Store! What would you like to buy?\nAvailable Items (Name - Price):\n");
        for (int i = 0; i < storeItems.size(); i++) {
            Item item = storeItems.get(i);
            // Only display items that are buyable (seeds, tools, etc.)
            // For now, we assume all items from getStoreItemsForDisplay are buyable.
            // A more robust check might involve `instanceof Seed` or checking a flag on Item.
            int price = priceList.getBuyPrice(item);
            storeItemList.append(String.format("%d. %s - %d G\n", i + 1, item.getName(), price));
        }
        storeItemList.append("\nEnter the number of the item to buy (or cancel):");

        String itemNumberStr = JOptionPane.showInputDialog(this, storeItemList.toString(), "Spakbor Hills Store", JOptionPane.PLAIN_MESSAGE);

        if (itemNumberStr == null || itemNumberStr.trim().isEmpty()) {
            return; // User cancelled or entered nothing
        }

        try {
            int itemNumber = Integer.parseInt(itemNumberStr);
            if (itemNumber < 1 || itemNumber > storeItems.size()) {
                JOptionPane.showMessageDialog(this, "Invalid item number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Item selectedItem = storeItems.get(itemNumber - 1);

            String quantityStr = JOptionPane.showInputDialog(this, "Enter quantity for " + selectedItem.getName() + ":", "Quantity", JOptionPane.PLAIN_MESSAGE);
            if (quantityStr == null || quantityStr.trim().isEmpty()) {
                return; // User cancelled
            }

            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be positive.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = gameController.requestBuyItem(selectedItem.getName(), quantity);

            if (success) {
                JOptionPane.showMessageDialog(this, "Purchased " + quantity + " of " + selectedItem.getName() + "!", "Purchase Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // GameController should provide reasons for failure via console logs or could return a more detailed status
                JOptionPane.showMessageDialog(this, "Could not complete purchase. (Not enough gold, item unavailable, or other error)", "Purchase Failed", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            repaint(); // Always repaint to reflect changes (e.g. gold)
        }
    }

    /**
     * Attempts to open the shipping bin dialog if the player is adjacent to the bin.
     * @return true if an interaction occurred (dialog shown, sale attempted), false otherwise.
     */
    private boolean tryOpenShippingBinDialog() {
        if (farmModel == null || gameController == null) {
            System.err.println("GamePanel: FarmModel or GameController is null in tryOpenShippingBinDialog.");
            return false;
        }
        Player player = farmModel.getPlayer();
        FarmMap farmMap = farmModel.getFarmMap();
        if (player == null || farmMap == null) {
            System.err.println("GamePanel: Player or FarmMap is null in tryOpenShippingBinDialog.");
            return false;
        }

        int playerX = player.getCurrentTileX();
        int playerY = player.getCurrentTileY();
        boolean nearBin = false;

        // Define offsets for N, S, W, E, and also NE, NW, SE, SW for more generous interaction
        // Order: N, S, W, E (Primary cardinal directions)
        int[][] neighbors = {
            {0, -1}, {0, 1}, {-1, 0}, {1, 0},  // N, S, W, E
            // Optional: Add diagonals if you want interaction from corners
            // {-1, -1}, {1, -1}, {-1, 1}, {1, 1} // NW, NE, SW, SE
        };

        for (int[] offset : neighbors) {
            int checkX = playerX + offset[0];
            int checkY = playerY + offset[1];

            if (farmMap.isWithinBounds(checkX, checkY)) {
                Tile adjacentTile = farmMap.getTile(checkX, checkY);
                // Assuming Tile has getAssociatedObject() that returns DeployedObject or null
                if (adjacentTile != null && adjacentTile.getAssociatedObject() instanceof ShippingBinObject) {
                    nearBin = true;
                    break; 
                }
            }
        }

        if (nearBin) {
            System.out.println("Player is near the Shipping Bin. Opening sell dialog...");
            return showSellDialog(); // showSellDialog will handle repaint and return true if action taken
        } else {
            System.out.println("Player is not near the Shipping Bin.");
            // JOptionPane.showMessageDialog(this, "You are not close enough to the Shipping Bin.", "Shipping Bin", JOptionPane.INFORMATION_MESSAGE);
            return false; // No interaction occurred
        }
    }

    /**
     * Shows a dialog for the player to select items from their inventory to sell.
     * Handles the interaction for choosing an item and quantity, then calls the controller.
     * @return true if a sale was successfully made and game state changed (time advanced), false otherwise.
     */
    private boolean showSellDialog() {
        if (farmModel == null || gameController == null || farmModel.getPlayer() == null) {
            System.err.println("GamePanel: Critical model component null in showSellDialog.");
            return false;
        }
        Player player = farmModel.getPlayer();
        Inventory inventory = player.getInventory();
        if (inventory == null || inventory.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your inventory is empty.", "Shipping Bin", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        List<Item> sellableItems = new ArrayList<>();
        StringBuilder sellListText = new StringBuilder("Select an item to sell (or Cancel):\n-------------------------------------\n");
        int itemNumber = 1;

        // Populate list of sellable items
        for (Map.Entry<Item, Integer> entry : inventory.getItems().entrySet()) {
            Item item = entry.getKey();
            // Filter: Only allow Crop, Fish, Food, MiscItem to be sold
            if (item instanceof Crop || item instanceof Fish || item instanceof Food || item instanceof MiscItem) {
                if (entry.getValue() > 0) { // Only list items player actually has
                    sellableItems.add(item);
                    sellListText.append(String.format("%d. %s (Qty: %d)\n", itemNumber++, item.getName(), entry.getValue()));
                }
            }
        }

        if (sellableItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You have no items that can be sold to the Shipping Bin.", "Shipping Bin", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        sellListText.append("-------------------------------------\nEnter item number to sell:");

        String itemChoiceStr = JOptionPane.showInputDialog(this, sellListText.toString(), "Sell to Shipping Bin", JOptionPane.PLAIN_MESSAGE);
        if (itemChoiceStr == null) { // User pressed Cancel or closed dialog
            return false; 
        }

        try {
            int selectedIdx = Integer.parseInt(itemChoiceStr.trim()) - 1;
            if (selectedIdx < 0 || selectedIdx >= sellableItems.size()) {
                JOptionPane.showMessageDialog(this, "Invalid item number selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            Item selectedItem = sellableItems.get(selectedIdx);
            int maxQuantity = inventory.getItemCount(selectedItem);

            String quantityStr = JOptionPane.showInputDialog(this, "Enter quantity of '" + selectedItem.getName() + "' to sell (Max: " + maxQuantity + "):", "Sell Quantity", JOptionPane.PLAIN_MESSAGE);
            if (quantityStr == null) { // User cancelled
                return false; 
            }

            int quantityToSell = Integer.parseInt(quantityStr.trim());
            if (quantityToSell <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (quantityToSell > maxQuantity) {
                JOptionPane.showMessageDialog(this, "You don't have that many " + selectedItem.getName() + " to sell.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Attempt to sell through controller
            boolean success = gameController.requestSellItemToBin(selectedItem.getName(), quantityToSell);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Successfully placed " + quantityToSell + " " + selectedItem.getName() + " in the Shipping Bin!\nGold will be received overnight.", "Sale Successful", JOptionPane.INFORMATION_MESSAGE);
                farmModel.getCurrentTime().advance(15); // Advance time by 15 minutes as per spec
                return true; // Indicate that an action leading to state change occurred
            } else {
                // Controller or Player model should have printed specific error to console
                // e.g., if already sold today or bin has too many unique items.
                JOptionPane.showMessageDialog(this, "Could not place item in Shipping Bin.\n(Have you already sold today, or is the bin full of unique items?)", "Sale Failed", JOptionPane.WARNING_MESSAGE);
                return false; // No successful sale, no time advance, but dialog was shown
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number entered. Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void handleCheatInput() {
        String cheatCode = JOptionPane.showInputDialog(this, "Enter cheat code (e.g., weather sunny/rainy, season spring/summer/fall/winter):", "Cheat Console", JOptionPane.PLAIN_MESSAGE);
        if (cheatCode == null || cheatCode.trim().isEmpty()) {
            return;
        }

        String[] parts = cheatCode.trim().toLowerCase().split("\\s+");
        if (parts.length > 0) {
            String command = parts[0];
            if (command.equals("weather") && parts.length > 1) {
                String weatherType = parts[1];
                if (weatherType.equals("sunny")) {
                    farmModel.getCurrentTime().setWeather(Weather.SUNNY);
                    JOptionPane.showMessageDialog(this, "Cheat activated: Weather set to SUNNY", "Cheat", JOptionPane.INFORMATION_MESSAGE);
                } else if (weatherType.equals("rainy")) {
                    farmModel.getCurrentTime().setWeather(Weather.RAINY);
                    JOptionPane.showMessageDialog(this, "Cheat activated: Weather set to RAINY", "Cheat", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid weather type. Use 'sunny' or 'rainy'.", "Cheat Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (command.equals("season") && parts.length > 1) {
                String seasonType = parts[1];
                Season newSeason = null;
                switch (seasonType) {
                    case "spring":
                        newSeason = Season.SPRING;
                        break;
                    case "summer":
                        newSeason = Season.SUMMER;
                        break;
                    case "fall":
                        newSeason = Season.FALL;
                        break;
                    case "winter":
                        newSeason = Season.WINTER;
                        break;
                    default:
                        JOptionPane.showMessageDialog(this, "Invalid season type. Use 'spring', 'summer', 'fall', or 'winter'.", "Cheat Error", JOptionPane.ERROR_MESSAGE);
                        return;
                }
                farmModel.getCurrentTime().setSeason(newSeason);
                JOptionPane.showMessageDialog(this, "Cheat activated: Season set to " + newSeason, "Cheat", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Unknown cheat command or insufficient arguments: " + cheatCode, "Cheat Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Not used
    }
} 