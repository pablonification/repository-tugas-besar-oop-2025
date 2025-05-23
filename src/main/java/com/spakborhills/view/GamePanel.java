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
import com.spakborhills.model.Map.MapArea;
import com.spakborhills.model.Store; // Corrected import for Store
import com.spakborhills.model.Util.GameTime; // Added for fishdebug
import com.spakborhills.model.Enum.LocationType; // Added for fishdebug
import com.spakborhills.model.Enum.FishRarity; // Added for fishdebug
import com.spakborhills.model.NPC.NPC; // Make sure NPC is imported

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent; // Import KeyEvent
import java.awt.event.KeyListener; // Import KeyListener
import java.util.List;
import java.util.ArrayList; // For creating list of sellable items
import java.util.Map; // For iterating inventory

public class GamePanel extends JPanel implements KeyListener { // Implement KeyListener

    private static final int TILE_SIZE = 32;
    private static final int INFO_PANEL_HEIGHT = 100; // Increased height for more info + hotbar
    private Farm farmModel;
    private GameController gameController;
    private static final Font DIALOG_FONT = new Font("Arial", Font.PLAIN, 20); // Updated font size to 20
    private static final Font NPC_DIALOG_FONT = new Font("Arial", Font.PLAIN, 16); // Font for NPC dialogues

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
        // UIManager.put("Label.font", DIALOG_FONT); // If needed for labels within JOptionPane
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (farmModel == null || farmModel.getPlayer() == null || farmModel.getPlayer().getCurrentMap() == null) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Loading Game Data...", 20, getHeight() / 2);
            return;
        }

        drawPlayerInfo(g);
        drawCurrentMap(g);
        drawNPCs(g);
        drawPlayer(g);
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

    private void drawCurrentMap(Graphics g) {
        Player player = farmModel.getPlayer();
        MapArea currentMap = player.getCurrentMap();

        Dimension mapSize = currentMap.getSize();
        int mapWidthInTiles = mapSize.width;
        int mapHeightInTiles = mapSize.height;
        int mapWidthInPixels = mapWidthInTiles * TILE_SIZE;
        int mapHeightInPixels = mapHeightInTiles * TILE_SIZE;

        // Visible area for the map (below info panel)
        int viewportWidth = getWidth();
        int viewportHeight = getHeight() - INFO_PANEL_HEIGHT;

        // Kamera berpusat pada pemain
        // Player's position in pixels relative to the entire map
        int playerCenterXInMap = player.getCurrentTileX() * TILE_SIZE + TILE_SIZE / 2;
        int playerCenterYInMap = player.getCurrentTileY() * TILE_SIZE + TILE_SIZE / 2;
        
        // camX and camY are the top-left coordinates of the visible part of the map
        int camX = playerCenterXInMap - viewportWidth / 2;
        int camY = playerCenterYInMap - viewportHeight / 2;

        // Batasi kamera agar tidak keluar dari batas peta
        camX = Math.max(0, Math.min(camX, mapWidthInPixels - viewportWidth));
        camY = Math.max(0, Math.min(camY, mapHeightInPixels - viewportHeight));
        
        // Handle jika map lebih kecil dari panel viewport
        if (mapWidthInPixels < viewportWidth) {
            camX = (mapWidthInPixels - viewportWidth) / 2; // Center map horizontally
        }
        if (mapHeightInPixels < viewportHeight) {
             camY = (mapHeightInPixels - viewportHeight) / 2; // Center map vertically
        }

        for (int yTile = 0; yTile < mapHeightInTiles; yTile++) {
            for (int xTile = 0; xTile < mapWidthInTiles; xTile++) {
                Tile tile = currentMap.getTile(xTile, yTile);
                if (tile == null) continue;

                // Position of the tile on the screen
                int screenX = xTile * TILE_SIZE - camX;
                int screenY = yTile * TILE_SIZE - camY + INFO_PANEL_HEIGHT; // Offset by info panel height

                // Culling: Hanya gambar tile yang terlihat di layar (within the map viewport)
                if (screenX + TILE_SIZE <= 0 || screenX >= getWidth() ||
                    screenY + TILE_SIZE <= INFO_PANEL_HEIGHT || screenY >= getHeight()) {
                    continue;
                }
                
                    Color tileColor;
                    switch (tile.getType()) {
                    case GRASS:
                        tileColor = new Color(34, 139, 34); // ForestGreen
                            break;
                    case TILLABLE: 
                        tileColor = new Color(210, 180, 140); // Tan
                            break;
                    case TILLED: 
                        tileColor = new Color(139, 69, 19); // SaddleBrown
                            if (tile.isWatered()) {
                           tileColor = new Color(90, 45, 10); // Darker, wet brown (adjusted from saddle brown)
                            }
                            break;
                    case PLANTED:
                                if (tile.isHarvestable()) {
                            tileColor = Color.YELLOW; 
                        } else {
                            Seed plantedSeed = tile.getPlantedSeed();
                            if (plantedSeed != null && plantedSeed.getDaysToHarvest() > 0) {
                                double growthPercentage = (double) tile.getGrowthDays() / plantedSeed.getDaysToHarvest();
                                if (growthPercentage < 0.33) {
                                    tileColor = new Color(144, 238, 144); // LightGreen
                                } else if (growthPercentage < 0.66) {
                                    tileColor = new Color(60, 179, 113);  // MediumSeaGreen
                                } else {
                                    tileColor = new Color(34, 139, 34);   // ForestGreen
                                    }
                                    if (tile.isWatered()) {
                                     tileColor = tileColor.darker(); // Darken if watered
                                }
                            } else {
                                 tileColor = new Color(0,100,0); // Default for PLANTED if no valid seed info
                            }
                        }
                        break;
                    case WATER: // Assuming TileType.WATER for water sources
                        tileColor = new Color(0, 100, 200); // Darker Blue
                        break;
                    case ENTRY_POINT:
                        tileColor = Color.MAGENTA; 
                            break;
                    case OBSTACLE:
                            tileColor = Color.DARK_GRAY;
                            break;
                        default:
                            tileColor = Color.LIGHT_GRAY;
                            break;
                    }
                    g.setColor(tileColor);
                g.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
                g.setColor(Color.BLACK);
                g.drawRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private void drawNPCs(Graphics g) {
        Player player = farmModel.getPlayer();
        MapArea currentMap = player.getCurrentMap();
        List<NPC> allNPCs = farmModel.getNPCs();

        if (currentMap == null || allNPCs == null || allNPCs.isEmpty()) {
            return;
        }

        // Camera calculations (copied from drawCurrentMap/drawPlayer for context, can be refactored)
        Dimension mapSize = currentMap.getSize();
        int mapWidthInTiles = mapSize.width;
        int mapHeightInTiles = mapSize.height;
        int mapWidthInPixels = mapWidthInTiles * TILE_SIZE;
        int mapHeightInPixels = mapHeightInTiles * TILE_SIZE;

        int viewportWidth = getWidth();
        int viewportHeight = getHeight() - INFO_PANEL_HEIGHT;

        int playerCenterXInMap = player.getCurrentTileX() * TILE_SIZE + TILE_SIZE / 2;
        int playerCenterYInMap = player.getCurrentTileY() * TILE_SIZE + TILE_SIZE / 2;
        
        int camX = playerCenterXInMap - viewportWidth / 2;
        int camY = playerCenterYInMap - viewportHeight / 2;

        camX = Math.max(0, Math.min(camX, mapWidthInPixels - viewportWidth));
        camY = Math.max(0, Math.min(camY, mapHeightInPixels - viewportHeight));
        
        if (mapWidthInPixels < viewportWidth) {
            camX = (mapWidthInPixels - viewportWidth) / 2;
        }
        if (mapHeightInPixels < viewportHeight) {
             camY = (mapHeightInPixels - viewportHeight) / 2;
        }

        // Iterate through all NPCs and draw them if they are on the current map
        for (NPC npc : allNPCs) {
            MapArea npcHomeMapInstance = farmModel.getMapArea(npc.getHomeLocation());

            // Check if the NPC belongs to the currently displayed map
            if (currentMap == npcHomeMapInstance) {
                int npcScreenX = npc.getCurrentTileX() * TILE_SIZE - camX;
                int npcScreenY = npc.getCurrentTileY() * TILE_SIZE - camY + INFO_PANEL_HEIGHT;

                // Culling: Only draw if NPC is within the visible viewport
                if (npcScreenX + TILE_SIZE <= 0 || npcScreenX >= getWidth() ||
                    npcScreenY + TILE_SIZE <= INFO_PANEL_HEIGHT || npcScreenY >= getHeight()) {
                    continue;
                }

                // Simple representation: a colored rectangle and their initial
                g.setColor(Color.ORANGE); // Example color for NPCs
                g.fillRect(npcScreenX, npcScreenY, TILE_SIZE, TILE_SIZE);
                
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 12));
                // Draw NPC's initial or name (adjust text position for visibility)
                String npcLabel = npc.getName().substring(0, Math.min(npc.getName().length(), 1)); // First letter
                FontMetrics fm = g.getFontMetrics();
                int textWidth = fm.stringWidth(npcLabel);
                g.drawString(npcLabel, npcScreenX + (TILE_SIZE - textWidth) / 2, npcScreenY + TILE_SIZE / 2 + fm.getAscent()/2);
            }
        }
    }

    private void drawPlayer(Graphics g) {
        if (farmModel == null || farmModel.getPlayer() == null || farmModel.getPlayer().getCurrentMap() == null) return;

        Player player = farmModel.getPlayer();
        MapArea currentMap = player.getCurrentMap();
        Dimension mapSize = currentMap.getSize();
        
        int mapWidthInTiles = mapSize.width;
        int mapHeightInTiles = mapSize.height;
        int mapWidthInPixels = mapWidthInTiles * TILE_SIZE;
        int mapHeightInPixels = mapHeightInTiles * TILE_SIZE;

        int viewportWidth = getWidth();
        int viewportHeight = getHeight() - INFO_PANEL_HEIGHT;
        
        int playerCenterXInMap = player.getCurrentTileX() * TILE_SIZE + TILE_SIZE / 2;
        int playerCenterYInMap = player.getCurrentTileY() * TILE_SIZE + TILE_SIZE / 2;

        int camX = playerCenterXInMap - viewportWidth / 2;
        int camY = playerCenterYInMap - viewportHeight / 2;

        camX = Math.max(0, Math.min(camX, mapWidthInPixels - viewportWidth));
        camY = Math.max(0, Math.min(camY, mapHeightInPixels - viewportHeight));
        
        if (mapWidthInPixels < viewportWidth) {
            camX = (mapWidthInPixels - viewportWidth) / 2;
        }
        if (mapHeightInPixels < viewportHeight) {
             camY = (mapHeightInPixels - viewportHeight) / 2;
        }

        // Player position on screen
        int playerScreenX = player.getCurrentTileX() * TILE_SIZE - camX;
        int playerScreenY = player.getCurrentTileY() * TILE_SIZE - camY + INFO_PANEL_HEIGHT;

        g.setColor(Color.RED); // Player color
        g.fillRect(playerScreenX, playerScreenY, TILE_SIZE, TILE_SIZE);
        
        // Optionally, draw an outline or a more detailed player sprite
        g.setColor(Color.BLACK);
        g.drawRect(playerScreenX, playerScreenY, TILE_SIZE, TILE_SIZE);

        // Draw selected item name above player if an item is selected
        Item selectedItem = player.getSelectedItem();
        if (selectedItem != null) {
            String itemName = selectedItem.getName();
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            int stringWidth = fm.stringWidth(itemName);
            g.drawString(itemName, playerScreenX + (TILE_SIZE - stringWidth) / 2, playerScreenY - 5);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameController == null) return;

        int keyCode = e.getKeyCode();
        boolean actionTaken = false;

        switch (keyCode) {
            case KeyEvent.VK_W: case KeyEvent.VK_UP:
                actionTaken = gameController.requestPlayerMove(Direction.NORTH);
                break;
            case KeyEvent.VK_S: case KeyEvent.VK_DOWN:
                actionTaken = gameController.requestPlayerMove(Direction.SOUTH);
                break;
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT:
                actionTaken = gameController.requestPlayerMove(Direction.WEST);
                break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT:
                actionTaken = gameController.requestPlayerMove(Direction.EAST);
                break;
            case KeyEvent.VK_E: // General Action Key
                actionTaken = tryGeneralAction();
                break;
            case KeyEvent.VK_F: // Eat
                actionTaken = gameController.requestEatSelectedItem();
                break;
            case KeyEvent.VK_T: // Store
                openStoreDialog(); // This is a view-specific action opening a dialog
                actionTaken = true; // Assume dialog opening is an action
                break;
            case KeyEvent.VK_B: // Shipping Bin
                 actionTaken = tryOpenShippingBinDialog();
                break;
            case KeyEvent.VK_C:
                handleCheatInput(); // Cheat input is an action itself
                actionTaken = true;
                break;
            case KeyEvent.VK_1:
                gameController.selectPreviousItem();
                actionTaken = true;
                break;
            case KeyEvent.VK_2:
                gameController.selectNextItem();
                actionTaken = true;
                break;
            case KeyEvent.VK_X: // Chat with NPC
                gameController.handleChatRequest(); // Returns void, handles its own feedback
                actionTaken = true; // An attempt to chat was made
                break;
            case KeyEvent.VK_G: // Gift to NPC
                gameController.handleGiftRequest(); // Returns void, handles its own feedback
                actionTaken = true; // An attempt to gift was made
                break;
            case KeyEvent.VK_L: // Sleep (Lodge/Lie down)
                gameController.requestNormalSleep(); // This will handle location check & next day
                actionTaken = true; // Assuming sleep always initiates a process
                break;
            case KeyEvent.VK_P: // Propose
                if (gameController != null) {
                    gameController.handleProposeRequest();
                    actionTaken = true; // Assuming propose request is an action
                }
                break;
            case KeyEvent.VK_M:
                if(gameController != null){
                    gameController.handleMarryRequest();
                    actionTaken = true;
                }
                break;
            case KeyEvent.VK_K: //cooking
                if(gameController != null){
                    gameController.handleCookRequest();
                    actionTaken = true;
                }
                break;
        }

        if (actionTaken) {
            repaint(); // Repaint the panel if an action was taken that might change the view
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
                // dia cuman bisa dipake di farm
                
            } else if (itemName.equals("Watering Can")) {
                actionProcessed = gameController.requestWaterTileAtPlayerPosition();
            } else if (itemName.equals("Pickaxe")) {
                actionProcessed = gameController.requestRecoverLandAtPlayerPosition();
            } else if (itemName.equals("Fishing Rod")) {
                // TODO: Implement fishing logic trigger here or in a dedicated fishing spot interaction
                // System.out.println("Fishing Rod selected. Implement fishing action or interaction with water tile.");
                actionProcessed = gameController.requestFish(); // Call the actual fishing method
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
    
    public void openStoreDialog() {
        if (gameController == null || farmModel == null) {
            JOptionPane.showMessageDialog(this, "Sistem toko belum siap.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get the parent frame to pass to the dialog
        Frame parentFrame = JOptionPane.getFrameForComponent(this);
        StoreDialog storeDialog = new StoreDialog(parentFrame, gameController, farmModel);
        storeDialog.setVisible(true); // This will block until the dialog is closed

        // After the dialog is closed, repaint the game panel to reflect any changes (e.g., gold, inventory)
        repaint();
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
        String cheat = JOptionPane.showInputDialog(this, "Enter cheat code (type 'help' for list of cheats):");
        if (cheat == null || cheat.trim().isEmpty()) {
            return;
        }

        String[] parts = cheat.trim().toLowerCase().split("\\s+");
        String command = parts[0];

        if (command.equals("help")) {
            showCheatsHelp();
            return;
        }

        if (command.equals("weather")) {
            if (parts.length > 1) {
                String weatherType = parts[1];
                Weather newWeather = null;
                if (weatherType.equals("sunny")) {
                    newWeather = Weather.SUNNY;
                } else if (weatherType.equals("rainy")) {
                    newWeather = Weather.RAINY;
                }

                if (newWeather != null) {
                    farmModel.getCurrentTime().setWeather(newWeather);
                    JOptionPane.showMessageDialog(this, "Weather changed to " + newWeather.toString(), "Cheat Activated", JOptionPane.INFORMATION_MESSAGE);
                    repaint(); // Update display
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid weather type. Use 'sunny' or 'rainy'.", "Cheat Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Usage: weather [sunny|rainy]", "Cheat Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (command.equals("season")) {
            if (parts.length > 1) {
                String seasonType = parts[1].toUpperCase(); // Match enum names
                Season newSeason = null;
                try {
                    newSeason = Season.valueOf(seasonType);
                } catch (IllegalArgumentException e) {
                    // Invalid season name
                }

                if (newSeason != null && newSeason != Season.ANY) { // ANY is not a settable season
                    farmModel.getCurrentTime().setSeason(newSeason);
                    JOptionPane.showMessageDialog(this, "Season changed to " + newSeason.toString(), "Cheat Activated", JOptionPane.INFORMATION_MESSAGE);
                    repaint(); // Update display
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid season. Use SPRING, SUMMER, FALL, or WINTER.", "Cheat Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Usage: season [SPRING|SUMMER|FALL|WINTER]", "Cheat Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (command.equals("fishdebug")) {
            StringBuilder debugMessage = new StringBuilder("<html><body>"); // Use HTML for better formatting
            GameTime currentTime = farmModel.getCurrentTime();
            Season currentSeason = currentTime.getCurrentSeason();
            Weather currentWeather = currentTime.getCurrentWeather();
            String timeString = currentTime.getTimeString();

            debugMessage.append(String.format("<b>--- Fish Debug ---</b><br>"));
            debugMessage.append(String.format("Current Conditions:<br>"));
            debugMessage.append(String.format("Season: %s, Day: %d, Time: %s, Weather: %s<br><br>",
                    currentSeason, currentTime.getCurrentDay(), timeString, currentWeather));
            debugMessage.append("<b>Non-Common Fish Currently Catchable:</b><br>");

            Map<String, Item> itemRegistry = farmModel.getItemRegistry();
            boolean foundAny = false;

            LocationType[] fishingSpots = {LocationType.POND, LocationType.MOUNTAIN_LAKE, LocationType.FOREST_RIVER, LocationType.OCEAN};

            for (LocationType location : fishingSpots) {
                List<String> catchableAtLocation = new ArrayList<>();
                for (Item item : itemRegistry.values()) {
                    if (item instanceof Fish) {
                        Fish fish = (Fish) item;
                        if (fish.getRarity() != FishRarity.COMMON) { // Only non-common
                            if (fish.canBeCaught(currentSeason, currentTime, currentWeather, location)) {
                                catchableAtLocation.add(fish.getName());
                            }
                        }
                    }
                }
                if (!catchableAtLocation.isEmpty()) {
                    foundAny = true;
                    debugMessage.append(String.format("- At <b>%s</b>: %s<br>", location.toString(), String.join(", ", catchableAtLocation)));
                }
            }

            if (!foundAny) {
                debugMessage.append("No non-common fish are catchable at any listed location under current conditions.<br>");
            }
            debugMessage.append("</body></html>");

            // Use JEditorPane for HTML rendering in JOptionPane
            JEditorPane editorPane = new JEditorPane("text/html", debugMessage.toString());
            editorPane.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(editorPane);
            scrollPane.setPreferredSize(new Dimension(600, 400)); // Adjust size as needed
            JOptionPane.showMessageDialog(this, scrollPane, "Fish Debug Information", JOptionPane.INFORMATION_MESSAGE);

        } else if (command.equals("gold")) {
            if (parts.length > 1) {
                int newGold = Integer.parseInt(parts[1]);
                farmModel.getPlayer().addGold(newGold);
                JOptionPane.showMessageDialog(this, "Gold changed to " + newGold, "Cheat Activated", JOptionPane.INFORMATION_MESSAGE);
                repaint(); // Update display
            }
        } else {
            JOptionPane.showMessageDialog(this, "Unknown cheat code: " + cheat, "Cheat Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Shows a dialog with all available cheats and keyboard shortcuts
     */
    private void showCheatsHelp() {
        StringBuilder helpText = new StringBuilder();
        helpText.append("<html><body style='width: 400px'>");
        
        // Title
        helpText.append("<h2>🎮 Spakbor Hills Cheats & Controls</h2>");
        
        // Cheat Commands Section
        helpText.append("<h3>📝 Cheat Commands (Press 'C'):</h3>");
        helpText.append("<p><b>Weather Control:</b><br>");
        helpText.append("• weather sunny - Changes weather to sunny<br>");
        helpText.append("• weather rainy - Changes weather to rainy</p>");
        
        helpText.append("<p><b>Season Control:</b><br>");
        helpText.append("• season SPRING - Changes to Spring<br>");
        helpText.append("• season SUMMER - Changes to Summer<br>");
        helpText.append("• season FALL - Changes to Fall<br>");
        helpText.append("• season WINTER - Changes to Winter</p>");

        helpText.append("<p><b>Gold Control:</b><br>");
        helpText.append("• gold 1000 - Adds 1000 gold to player<br>");
        helpText.append("• gold -1000 - Removes 1000 gold from player</p>");
        
        helpText.append("<p><b>Debug Commands:</b><br>");
        helpText.append("• fishdebug - Shows fish availability info<br>");
        helpText.append("• help - Shows this help menu</p>");
        
        // Keyboard Controls Section
        helpText.append("<h3>⌨️ Keyboard Controls:</h3>");
        helpText.append("<p><b>Movement:</b><br>");
        helpText.append("• W/↑ - Move up<br>");
        helpText.append("• S/↓ - Move down<br>");
        helpText.append("• A/← - Move left<br>");
        helpText.append("• D/→ - Move right</p>");
        
        helpText.append("<p><b>Actions:</b><br>");
        helpText.append("• E - Use tool/Harvest<br>");
        helpText.append("• F - Eat selected item<br>");
        helpText.append("• T - Open Store/Trade<br>");
        helpText.append("• B - Open Shipping Bin (when near)<br>");
        helpText.append("• 1 - Select previous item<br>");
        helpText.append("• 2 - Select next item<br>");
        helpText.append("• C - Open Cheat Console</p>");

        helpText.append("</body></html>");

        // Create a JEditorPane for HTML rendering
        JEditorPane editorPane = new JEditorPane("text/html", helpText.toString());
        editorPane.setEditable(false);
        editorPane.setBackground(new Color(250, 250, 250));

        // Create a scrollable panel
        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new Dimension(450, 500));

        // Show the dialog
        JOptionPane.showMessageDialog(
            this,
            scrollPane,
            "Spakbor Hills Cheats & Controls",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Not used
    }

    /**
     * Menampilkan dialog informasi akhir hari (misalnya, karena pingsan atau tidur normal).
     * @param eventMessage Pesan utama kejadian (misal, "Kamu pingsan!" atau "Kamu tidur nyenyak.").
     * @param income Pendapatan dari penjualan di hari sebelumnya.
     * @param newDayInfo Informasi tentang hari baru (Tanggal, Musim, Cuaca).
     */
    public void showEndOfDayMessage(String eventMessage, int income, String newDayInfo) {
        StringBuilder message = new StringBuilder();
        message.append(eventMessage).append("\n\n");
        if (income > 0) {
            message.append("Kamu mendapatkan ").append(income).append("G dari penjualan kemarin.\n");
        } else {
            message.append("Tidak ada pendapatan dari penjualan kemarin.\n");
        }
        message.append("\n").append(newDayInfo);

        JOptionPane.showMessageDialog(this, 
                                      message.toString(), 
                                      "Akhir Hari", 
                                      JOptionPane.INFORMATION_MESSAGE);
        repaint(); // Pastikan UI di-update setelah dialog ditutup
    }

    /**
     * Menampilkan dialog untuk memilih tujuan map saat pemain berada di entry point.
     */
    public void showWorldMapSelectionDialog() {
        if (farmModel == null || gameController == null || farmModel.getPlayer() == null || farmModel.getPlayer().getCurrentMap() == null) {
            System.err.println("GamePanel: Critical model component null, cannot show world map dialog.");
            return;
        }

        Player player = farmModel.getPlayer();
        MapArea currentMap = player.getCurrentMap();
        String currentMapName = currentMap.getName(); // Used for a simple exclusion attempt

        java.util.List<String> destinationNames = new java.util.ArrayList<>();
        for (com.spakborhills.model.Enum.LocationType locType : com.spakborhills.model.Enum.LocationType.values()) {
            // Exclude FARM itself (as you are trying to leave it or another map)
            // Exclude POND always (as it's part of FARM and not a separate visitable world location)
            // Exclude the current location the player is on, if its name matches a LocationType enum string.
            
            boolean isCurrentMapType = locType.toString().equalsIgnoreCase(currentMapName);
            // Special handling if currentMap is FarmMap instance, for more robust exclusion of FARM
            if (currentMap instanceof FarmMap && locType == com.spakborhills.model.Enum.LocationType.FARM) {
                 isCurrentMapType = true;
            }
            // Special handling if currentMap is Store instance, for more robust exclusion of STORE
            // This requires Store to either have a getLocationType() or its name to be predictable.
            // Assuming Store.getName() is "Toko Spakbor Hills" and LocationType.STORE.toString() is "STORE"
            // A direct name check might be needed if Store.java doesn't identify its LocationType.
            // For now, the generic locType.toString().equalsIgnoreCase(currentMapName) might catch some cases.
            if (currentMap instanceof Store && locType == com.spakborhills.model.Enum.LocationType.STORE) {
                isCurrentMapType = true;
            }

            if (locType != com.spakborhills.model.Enum.LocationType.POND && !isCurrentMapType) {
                destinationNames.add(locType.toString());
            }
        }

        if (destinationNames.isEmpty()) {
            // This might happen if on a map like STORE and all other options are somehow filtered out,
            // or if on FARM and only POND was filtered leaving no other valid world locations.
            // A fallback could be to always offer FARM if not currently on FARM.
            // For now, if empty, show a message. This indicates a potential logic issue or lack of destinations.
            if (!(currentMap instanceof FarmMap) && !destinationNames.contains(com.spakborhills.model.Enum.LocationType.FARM.toString())) {
                 // If not on Farm, and Farm is not in the list, always add Farm as an option to return.
                 boolean farmAlreadyExcludedAsCurrent = com.spakborhills.model.Enum.LocationType.FARM.toString().equalsIgnoreCase(currentMapName);
                 if (!farmAlreadyExcludedAsCurrent) {
                    destinationNames.add(com.spakborhills.model.Enum.LocationType.FARM.toString());
                 }
            }
            if (destinationNames.isEmpty()) { // Re-check after potentially adding FARM
                 JOptionPane.showMessageDialog(this, "No other locations available to visit from here.", "Pindah Lokasi", JOptionPane.INFORMATION_MESSAGE);
                 return;
            }
        }

        String[] options = destinationNames.toArray(new String[0]);
        String chosenDestination = (String) JOptionPane.showInputDialog(
                this,
                "Kamu berada di tepi kebun. Mau pergi ke mana?",
                "Pilih Tujuan",
                JOptionPane.PLAIN_MESSAGE,
                null, // No custom icon
                options, // Array of choices
                options[0] // Default choice
        );

        if (chosenDestination != null && !chosenDestination.isEmpty()) {
            try {
                com.spakborhills.model.Enum.LocationType destinationEnum = 
                    com.spakborhills.model.Enum.LocationType.valueOf(chosenDestination.toUpperCase());
                
                // Call the controller to handle the visit
                boolean visitSuccess = gameController.requestVisit(destinationEnum);
                
                if (visitSuccess) {
                    // Repaint is handled by GameController after successful visit and model update
                    // No need to advance time here, GameController handles it.
                    System.out.println("GamePanel: Visit to " + destinationEnum + " requested.");
                } else {
                    // GameController.requestVisit already shows a JOptionPane on failure.
                    // System.out.println("GamePanel: Visit to " + destinationEnum + " failed or map not available.");
                    // Optionally, re-show the dialog or provide other feedback if needed here,
                    // but for now, the JOptionPane in GameController should suffice.
                }
            } catch (IllegalArgumentException e) {
                System.err.println("GamePanel: Invalid destination string chosen: " + chosenDestination + " Error: " + e.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Invalid location selected: " + chosenDestination, 
                    "Selection Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Displays a message to the player using a JOptionPane dialog.
     * @param message The message to display.
     */
    public void displayMessage(String message) {
        // Ensure dialogs use the focus of this panel
        JOptionPane.showMessageDialog(this, message, "Game Message", JOptionPane.INFORMATION_MESSAGE);
    }

    public void updatePlayerInfoPanel() {
        // This method could potentially just call repaint on the info panel area
        // or the whole panel if info is drawn in paintComponent.
        // Forcing a full repaint to ensure info is up-to-date.
        repaint(0, 0, getWidth(), INFO_PANEL_HEIGHT); // Repaint only the info panel area
    }
    
    public void updateGameRender() {
        repaint();
    }

    /**
     * Displays a dialogue message from an NPC.
     * @param npcName The name of the NPC speaking.
     * @param dialogue The dialogue text.
     */
    public void showNPCDialogue(String npcName, String dialogue) {
        // Store original fonts
        Object originalMessageFont = UIManager.get("OptionPane.messageFont");
        Object originalButtonFont = UIManager.get("OptionPane.buttonFont");

        // Set custom font for this dialog
        UIManager.put("OptionPane.messageFont", NPC_DIALOG_FONT);
        UIManager.put("OptionPane.buttonFont", NPC_DIALOG_FONT);

        JOptionPane.showMessageDialog(this, dialogue, npcName + " says:", JOptionPane.PLAIN_MESSAGE);

        // Restore original fonts
        UIManager.put("OptionPane.messageFont", originalMessageFont);
        UIManager.put("OptionPane.buttonFont", originalButtonFont);
    }

    /**
     * Displays an option dialog to the player and returns the chosen option index.
     * @param message The message to display.
     * @param title The title of the dialog.
     * @param options An array of strings representing the options.
     * @return The index of the chosen option, or JOptionPane.CLOSED_OPTION if the dialog was closed.
     */
    public int showOptionDialog(String message, String title, String[] options) {
        // Store original fonts
        Object originalMessageFont = UIManager.get("OptionPane.messageFont");
        Object originalButtonFont = UIManager.get("OptionPane.buttonFont");

        // Set custom font for this dialog
        UIManager.put("OptionPane.messageFont", DIALOG_FONT);
        UIManager.put("OptionPane.buttonFont", DIALOG_FONT);

        int choice = JOptionPane.showOptionDialog(
            this,
            message,
            title,
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null, // no custom icon
            options,
            options[0] // default selection
        );

        // Restore original fonts
        UIManager.put("OptionPane.messageFont", originalMessageFont);
        UIManager.put("OptionPane.buttonFont", originalButtonFont);
        
        return choice;
    }
}
