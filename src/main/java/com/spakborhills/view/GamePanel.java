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
import com.spakborhills.model.Enum.GameState; // Added import for GameState

import javax.imageio.ImageIO; // For loading placeholder image
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent; // Added for Timer
import java.awt.event.ActionListener; // Added for Timer
import java.awt.event.KeyEvent; // Import KeyEvent
import java.awt.event.KeyListener; // Import KeyListener
import java.awt.image.BufferedImage; // For placeholder image
import java.io.IOException; // For image loading
import java.util.List;
import java.util.ArrayList; // For creating list of sellable items
import java.util.Map; // For iterating inventory

public class GamePanel extends JPanel implements KeyListener { // Implement KeyListener

    private static final int TILE_SIZE = 96;
    private static final int VIEWPORT_WIDTH_IN_TILES = 20;
    private static final int VIEWPORT_HEIGHT_IN_TILES = 10;
    private static final int INFO_PANEL_HEIGHT = 100;
    private Farm farmModel;
    private GameController gameController;
    private static final Font DIALOG_FONT = new Font("Arial", Font.PLAIN, 20); // Updated font size to 20
    private static final Font NPC_DIALOG_FONT = new Font("Arial", Font.PLAIN, 16); // Font for NPC dialogues

    private javax.swing.Timer gameTimer;
    private boolean statisticsShown = false; // Flag to ensure stats are shown only once

    // Main Menu state
    private String[] menuOptions = {"New Game", "Load Game", "Help", "Credits", "Exit"};
    private int currentMenuSelection = 0;
    private static final Font MENU_FONT = new Font("Arial", Font.BOLD, 30);
    private static final Font MENU_ITEM_FONT = new Font("Arial", Font.PLAIN, 24);
    private static final Color MENU_BACKGROUND_COLOR = new Color(50, 50, 100); // Dark blue
    private static final Color MENU_TEXT_COLOR = Color.WHITE;
    private static final Color MENU_SELECTED_TEXT_COLOR = Color.YELLOW;

    // NPC Dialogue State
    private boolean isNpcDialogueActive = false;
    private String currentNpcName;
    private String currentNpcDialogue;
    private Rectangle npcDialogueBox;
    private Image npcPortraitPlaceholder;
    private static final int PORTRAIT_SIZE = 80;
    private static final int DIALOGUE_PADDING = 20;
    private static final Font DIALOGUE_TEXT_FONT = new Font("Arial", Font.PLAIN, 18);
    private static final Font DIALOGUE_NAME_FONT = new Font("Arial", Font.BOLD, 20);

    // Store UI State
    private boolean isStoreUiActive = false;
    private List<Item> storeItemsForDisplay;
    private int currentStoreItemSelectionIndex = 0;
    private int currentBuyQuantity = 1;
    private String storeInputMode = "selecting_item"; // "selecting_item", "inputting_quantity"
    private Rectangle storePanelRect;
    private Rectangle storeItemListRect;
    private Rectangle storeQuantityRect;
    private Rectangle storeBuyButtonRect;
    private Rectangle storeCloseButtonRect;
    private static final Font STORE_FONT = new Font("Arial", Font.PLAIN, 18);
    private static final Font STORE_ITEM_FONT = new Font("Monospaced", Font.PLAIN, 16);
    private static final Color STORE_BG_COLOR = new Color(0, 0, 0, 200); // Semi-transparent black
    private static final Color STORE_TEXT_COLOR = Color.WHITE;
    private static final Color STORE_HIGHLIGHT_COLOR = Color.YELLOW;
    private String storeFeedbackMessage = "";
    private Color storeFeedbackColor = STORE_TEXT_COLOR; 
    private Timer storeFeedbackTimer;

    public GamePanel(Farm farmModel, GameController gameController) {
        this.farmModel = farmModel;
        this.gameController = gameController;

        setPreferredSize(new Dimension(VIEWPORT_WIDTH_IN_TILES * TILE_SIZE, 
                                       VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE + INFO_PANEL_HEIGHT));
        setBackground(Color.GRAY);
        addKeyListener(this);
        setFocusable(true); // Important to receive key events

        // Initialize and start the game timer
        // 1 real second = 5 game minutes
        gameTimer = new javax.swing.Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (farmModel != null && farmModel.getCurrentTime() != null && gameController != null) {
                    // Only advance time and check end conditions if IN_GAME
                    if (farmModel.getCurrentGameState() == GameState.IN_GAME) {
                        // Check for end game conditions FIRST
                        if (!statisticsShown && farmModel.checkEndConditions()) {
                            System.out.println("GAME PANEL: End game condition met! Requesting stats display.");
                            gameController.requestShowStatistics(); // This will also stop the timer
                            statisticsShown = true; // Set flag so it doesn't trigger repeatedly
                            return; // Crucial: Do not advance time or repaint if stats are shown
                        }

                        // If timer is still running (i.e., stats not shown and timer not stopped by stats display)
                        if (gameTimer.isRunning()) { 
                            farmModel.getCurrentTime().advance(5); // Advance 5 game minutes
                            repaint(); // Redraw the panel to update time, etc.
                        }
                    } else if (farmModel.getCurrentGameState() == GameState.MAIN_MENU) {
                        repaint(); // Keep repainting menu for potential animations or cursor blink later
                    }
                }
            }
        });
        gameTimer.start();

        // Set default font for JOptionPane dialogs
        UIManager.put("OptionPane.messageFont", DIALOG_FONT);
        UIManager.put("OptionPane.buttonFont", DIALOG_FONT);
        UIManager.put("TextField.font", DIALOG_FONT);
        // UIManager.put("Label.font", DIALOG_FONT); // If needed for labels within JOptionPane

        // Initialize NPC Dialogue Box based on preferred size
        // These are initial values; they will be updated if the panel is resized and showNPCDialogue is called.
        int preferredWidth = VIEWPORT_WIDTH_IN_TILES * TILE_SIZE;
        int preferredHeightTotal = VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE + INFO_PANEL_HEIGHT;

        int dialogueBoxWidth = preferredWidth * 3 / 4;
        int dialogueBoxHeight = preferredHeightTotal / 3;
        int dialogueBoxX = (preferredWidth - dialogueBoxWidth) / 2;
        int dialogueBoxY = preferredHeightTotal - dialogueBoxHeight - 20; // 20px from bottom of the entire panel
        npcDialogueBox = new Rectangle(dialogueBoxX, dialogueBoxY, dialogueBoxWidth, dialogueBoxHeight);

        try {
            // Load placeholder portrait (replace with actual path or use a default colored square)
            // Assuming a placeholder.png exists in resources or adjust path
            // BufferedImage tempImg = ImageIO.read(getClass().getResourceAsStream("/assets/images/npc/placeholder_portrait.png"));
            // if (tempImg != null) {
            //     npcPortraitPlaceholder = tempImg.getScaledInstance(PORTRAIT_SIZE, PORTRAIT_SIZE, Image.SCALE_SMOOTH);
            // } else {
                npcPortraitPlaceholder = new BufferedImage(PORTRAIT_SIZE, PORTRAIT_SIZE, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = ((BufferedImage)npcPortraitPlaceholder).createGraphics();
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillRect(0, 0, PORTRAIT_SIZE, PORTRAIT_SIZE);
                g2d.setColor(Color.BLACK);
                g2d.drawString("P", PORTRAIT_SIZE/2 - 5, PORTRAIT_SIZE/2 + 5);
                g2d.dispose();
            // }
        } catch (Exception e) { // Catch broader exception for ImageIO or NullPointer
            System.err.println("Failed to load NPC portrait placeholder: " + e.getMessage());
            // Create a fallback placeholder if loading failed
            npcPortraitPlaceholder = new BufferedImage(PORTRAIT_SIZE, PORTRAIT_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = ((BufferedImage)npcPortraitPlaceholder).createGraphics();
            g2d.setColor(Color.GRAY);
            g2d.fillRect(0, 0, PORTRAIT_SIZE, PORTRAIT_SIZE);
            g2d.dispose();
        }
        
        // Initialize Store UI Rectangles (example values, adjust as needed)
        int storePanelWidth = VIEWPORT_WIDTH_IN_TILES * TILE_SIZE * 3 / 4;
        int storePanelHeight = VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE; // FULL HEIGHT supaya muat semua item
        int storePanelX = (VIEWPORT_WIDTH_IN_TILES * TILE_SIZE - storePanelWidth) / 2;
        int storePanelY = (VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE - storePanelHeight) / 2;
        storePanelRect = new Rectangle(storePanelX, storePanelY, storePanelWidth, storePanelHeight);

        // Perbesar area daftar item agar muat lebih banyak
        storeItemListRect = new Rectangle(storePanelX + 20, storePanelY + 60, storePanelWidth - 40, storePanelHeight - 100);
        storeQuantityRect = new Rectangle(storePanelX + 20, storePanelY + storePanelHeight - 80, storePanelWidth / 2 - 30, 40);
        storeBuyButtonRect = new Rectangle(storePanelX + storePanelWidth / 2, storePanelY + storePanelHeight - 80, 100, 40);
        storeCloseButtonRect = new Rectangle(storePanelX + storePanelWidth - 120, storePanelY + 20, 100, 30);

        // Inisialisasi Timer untuk feedback Toko
        storeFeedbackTimer = new Timer(3000, new ActionListener() { // Feedback hilang setelah 3 detik
            @Override
            public void actionPerformed(ActionEvent e) {
                storeFeedbackMessage = "";
                repaint(); 
            }
        });
        storeFeedbackTimer.setRepeats(false); // Hanya berjalan sekali per trigger
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (farmModel == null) { // Simplified initial check
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Loading Game Data...", 20, getHeight() / 2);
            return;
        }

        if (farmModel.getCurrentGameState() == GameState.MAIN_MENU) {
            drawMainMenu(g);
        } else if (farmModel.getCurrentGameState() == GameState.IN_GAME) {
            if (farmModel.getPlayer() == null || farmModel.getPlayer().getCurrentMap() == null) {
                // Still loading or error state after selecting new game but before player is ready
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 16));
                g.drawString("Initializing Game World...", 20, getHeight() / 2);
                return;
            }
            // Draw player info panel first (unclipped)
        drawPlayerInfo(g);

            // Store original clip and set new clip for map area
            Shape originalClip = g.getClip();
            g.setClip(0, INFO_PANEL_HEIGHT, getWidth(), getHeight() - INFO_PANEL_HEIGHT);

            // These are drawn within the new clipped area
        drawCurrentMap(g);
        drawNPCs(g);
        drawPlayer(g);
            // drawDayNightTint(g); // Commented out as per user request

            // Restore original clip
            g.setClip(originalClip);

            // Draw NPC Dialogue if active (on top of everything else in game world)
            if (isNpcDialogueActive) {
                drawNpcDialogue(g);
            }
            if (isStoreUiActive) {
                drawStoreUI((Graphics2D) g); // Explicitly cast here
            }
        }
    }

    private void drawMainMenu(Graphics g) {
        // Draw background
        g.setColor(MENU_BACKGROUND_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw Title (similar to Harvest Moon image)
        g.setFont(MENU_FONT.deriveFont(Font.BOLD, 60f)); // Larger for title
        g.setColor(MENU_TEXT_COLOR);
        String title = "Spakbor Hills";
        FontMetrics fmTitle = g.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(title);
        g.drawString(title, (getWidth() - titleWidth) / 2, getHeight() / 4);

        // Draw Menu Items
        g.setFont(MENU_ITEM_FONT);
        FontMetrics fmItems = g.getFontMetrics();
        int itemHeight = fmItems.getHeight();
        int startY = getHeight() / 2; // Start items from midpoint

        for (int i = 0; i < menuOptions.length; i++) {
            String itemText = menuOptions[i];
            if (i == currentMenuSelection) {
                g.setColor(MENU_SELECTED_TEXT_COLOR);
                itemText = "> " + itemText + " <"; // Indicator for selection
            } else {
                g.setColor(MENU_TEXT_COLOR);
            }
            int itemWidth = fmItems.stringWidth(itemText);
            g.drawString(itemText, (getWidth() - itemWidth) / 2, startY + i * (itemHeight + 15)); // 15px spacing
        }
    }

    private void drawDayNightTint(Graphics g) {
        if (farmModel == null || farmModel.getCurrentTime() == null) {
            return;
        }

        int currentHour = farmModel.getCurrentTime().getHour();
        Color tintColor = null;

        // Define tint colors and alpha based on the hour
        if (currentHour >= 22 || currentHour < 5) { // Night (10 PM - 4:59 AM)
            tintColor = new Color(0, 0, 70, 100); 
        } else if (currentHour >= 18) { // Dusk (6 PM - 9:59 PM)
            tintColor = new Color(200, 100, 0, 70);
        } else if (currentHour >= 5 && currentHour < 7) { // Dawn (5 AM - 6:59 AM)
            tintColor = new Color(255, 204, 153, 60);
        } else { // Daytime (7 AM - 5:59 PM)
            // No tint, or a very transparent one if desired
            // tintColor = new Color(0, 0, 0, 0); // Example: completely transparent
        }

        if (tintColor != null) {
            g.setColor(tintColor);
            // The tint should cover the game world area, which is already clipped
            // So, drawing from (0,0) within the current clip will cover the correct area.
            // The clip is (0, INFO_PANEL_HEIGHT, getWidth(), getHeight() - INFO_PANEL_HEIGHT)
            // So relative to this clip, we draw at (0,0) with size (getWidth(), getHeight() - INFO_PANEL_HEIGHT)
            // However, Graphics g is already translated if setClip was used correctly.
            // The coordinates for fillRect should be relative to the component's coordinate system.
            // The current clip starts at y = INFO_PANEL_HEIGHT.
            g.fillRect(0, INFO_PANEL_HEIGHT, getWidth(), getHeight() - INFO_PANEL_HEIGHT);
        }
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
        if (gameController == null || farmModel == null) return; // Added farmModel check

        int keyCode = e.getKeyCode();

        if (farmModel.getCurrentGameState() == GameState.MAIN_MENU) {
            handleMainMenuInput(keyCode);
            return; // Don't process further input if in main menu
        }
        
        // If NPC dialogue is active, it takes precedence
        if (isNpcDialogueActive) {
            if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_X || keyCode == KeyEvent.VK_E) {
                isNpcDialogueActive = false;
                // Potentially trigger next part of dialogue or close if it's the end
                repaint();
            }
            return; // Consume the input, don't process other game actions
        }

        if (isStoreUiActive) {
            handleStoreInput(keyCode);
            repaint();
            return; 
        }

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
                System.out.println("G key pressed - Attempting Gift");
                    gameController.handleGiftRequest();
                actionTaken = true;
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
            case KeyEvent.VK_V: // Added for Watching TV
                System.out.println("V key pressed - Attempting to Watch TV");
                gameController.requestWatchTV();
                actionTaken = true;
                break;
            case KeyEvent.VK_I: // Added for View Player Info
                System.out.println("I key pressed - Viewing Player Info");
                gameController.requestViewPlayerInfo();
                actionTaken = true; // Technically not an action that changes game state, but good to acknowledge
                break;
            case KeyEvent.VK_O: // Added for View Statistics
                System.out.println("O key pressed - Viewing Statistics");
                gameController.requestShowStatistics();
                actionTaken = true; // This action does stop the timer
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
        if (gameController == null || farmModel == null || farmModel.getPlayer() == null) {
            // JOptionPane.showMessageDialog(this, "Sistem toko belum siap.", "Error", JOptionPane.ERROR_MESSAGE);
            displayMessage("Sistem toko belum siap."); // Use in-game message
            return;
        }

        Player player = farmModel.getPlayer();
        MapArea currentMap = player.getCurrentMap();

        // Check if player is in the store location
        boolean isInStoreLocation = false;
        if (currentMap != null) {
            // Primary check: if the current map is an instance of the Store class
            if (currentMap instanceof com.spakborhills.model.Store) {
                isInStoreLocation = true;
            } 
            // Fallback or alternative: check if map name is "STORE" (less robust)
            // else if (currentMap.getName().equalsIgnoreCase(LocationType.STORE.toString())) {
            //     isInStoreLocation = true;
            // }
        }

        if (!isInStoreLocation) {
            displayMessage("Kamu harus berada di Toko untuk mengaksesnya.");
            return;
        }

        // ---- NEW In-Game UI Logic ----
        this.storeItemsForDisplay = gameController.getStoreItemsForDisplay();
        if (this.storeItemsForDisplay == null || this.storeItemsForDisplay.isEmpty()) {
            displayMessage("Toko sedang kosong saat ini.");
            return;
        }
        this.currentStoreItemSelectionIndex = 0;
        this.currentBuyQuantity = 1;
        this.storeInputMode = "selecting_item";
        this.isStoreUiActive = true;
        repaint();


        // ---- OLD JOptionPane Logic ----
        // Frame parentFrame = JOptionPane.getFrameForComponent(this);
        // StoreDialog storeDialog = new StoreDialog(parentFrame, gameController, farmModel);
        // storeDialog.setVisible(true); // This will block until the dialog is closed
        // repaint(); // Repaint after dialog closes
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
        } else if (command.equals("time")) { // Added time cheat
            if (parts.length == 3) {
                try {
                    int hour = Integer.parseInt(parts[1]);
                    int minute = Integer.parseInt(parts[2]);
                    if (gameController != null) {
                        boolean success = gameController.requestSetTime(hour, minute);
                        if (success) {
                            JOptionPane.showMessageDialog(this, "Time changed to " + String.format("%02d:%02d", hour, minute), "Cheat Activated", JOptionPane.INFORMATION_MESSAGE);
                            repaint(); // Update display
                        } else {
                            JOptionPane.showMessageDialog(this, "Invalid time values. Hour (0-23), Minute (0-59).", "Cheat Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid number format for time. Usage: time HH MM", "Cheat Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Usage: time HH MM (e.g., time 8 0 for 8:00 AM, time 22 30 for 10:30 PM)", "Cheat Error", JOptionPane.ERROR_MESSAGE);
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

        helpText.append("<p><b>Time Control:</b><br>"); // Added Time Control
        helpText.append("• time HH MM - Sets time (e.g., time 8 0 for 8:00 AM, time 22 30 for 10:30 PM)</p>");

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
        helpText.append("• T - Open Store\n" +
                        "• B - Open Shipping Bin\n" +
                        "• 1, 2: Cycle Inventory\n" +
                        "• X: Chat with NPC\n" +
                        "• G: Gift to NPC\n" +
                        "• L: Sleep\n" +
                        "• K: Cook\n" +
                        "• V: Watch TV\n" +
                        "• I: View Player Info\n" +
                        "• O: View Current Progress\n" +
                        "• C: Open Cheat Menu\n\n" +
                        "Menu Controls:\n" +
                        "• UP/DOWN Arrows: Navigate\n" +
                        "• ENTER: Select");
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
        // JOptionPane.showMessageDialog(this, dialogue, npcName + " says:", JOptionPane.PLAIN_MESSAGE);
        this.currentNpcName = npcName;
        this.currentNpcDialogue = dialogue;
        this.isNpcDialogueActive = true;

        // Update dialogue box dimensions based on current panel size
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        if (panelWidth == 0 || panelHeight == 0) { // If panel not yet laid out, use preferred size
            panelWidth = getPreferredSize().width;
            panelHeight = getPreferredSize().height;
        }

        int dialogueBoxWidth = panelWidth * 3 / 4;
        int dialogueBoxHeight = panelHeight / 3;
        int dialogueBoxX = (panelWidth - dialogueBoxWidth) / 2;
        int dialogueBoxY = panelHeight - dialogueBoxHeight - 20; // 20px from bottom of the entire panel
        
        npcDialogueBox = new Rectangle(dialogueBoxX, dialogueBoxY, dialogueBoxWidth, dialogueBoxHeight);
        
        System.out.println("GamePanel: Activating NPC Dialogue - Name: " + npcName + ", Dialogue: " + dialogue);
        repaint();
    }

    /**
     * Displays an option dialog to the player and returns the chosen option index.
     * @param message The message to display.
     * @param title The title of the dialog.
     * @param options An array of strings representing the options.
     * @return The index of the chosen option, or JOptionPane.CLOSED_OPTION if the dialog was closed.
     */
    public int showOptionDialog(String message, String title, String[] options) {
        return JOptionPane.showOptionDialog(this, message, title,
                                            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                                            null, options, options[0]);
    }

    /**
     * Displays the end-game statistics summary in a dialog.
     * @param statisticsSummary The formatted string of statistics.
     */
    public void showStatisticsDialog(String statisticsSummary) {
        // For now, keep JOptionPane for this as it's a less frequent, game-ending dialog.
        // Future: Could also be an in-game panel.
        stopGameTimer(); // Stop the game timer when statistics are shown
        // For better readability in JOptionPane, we can wrap the text in a JTextArea inside a JScrollPane.
        JTextArea textArea = new JTextArea(statisticsSummary);
        textArea.setEditable(false);
        textArea.setFont(NPC_DIALOG_FONT); // Use a readable font
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400)); // Adjust size as needed

        JOptionPane.showMessageDialog(this, scrollPane, "Current Progress", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Stops the main game timer.
     */
    public void stopGameTimer() {
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
            System.out.println("Game Timer stopped.");
        }
    }

    /**
     * Starts or restarts the main game timer if it's not already running.
     */
    public void startGameTimer() {
        if (gameTimer != null && !gameTimer.isRunning()) {
            gameTimer.start();
            System.out.println("Game Timer started.");
        }
    }

    /**
     * Displays the player's information in a dialog.
     * @param playerInfoSummary The formatted string of player information.
     */
    public void showPlayerInfoDialog(String playerInfoSummary) {
        JTextArea textArea = new JTextArea(playerInfoSummary);
        textArea.setEditable(false);
        textArea.setFont(NPC_DIALOG_FONT); // Use a readable font, similar to NPC dialog
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 250)); // Adjust size as needed

        JOptionPane.showMessageDialog(this,
                scrollPane,
                "Player Information",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleMainMenuInput(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP:
                currentMenuSelection--;
                if (currentMenuSelection < 0) {
                    currentMenuSelection = menuOptions.length - 1;
                }
                break;
            case KeyEvent.VK_DOWN:
                currentMenuSelection++;
                if (currentMenuSelection >= menuOptions.length) {
                    currentMenuSelection = 0;
                }
                break;
            case KeyEvent.VK_ENTER:
                selectMainMenuItem();
                break;
        }
        repaint(); // Repaint after menu navigation
    }

    private void selectMainMenuItem() {
        String selectedOption = menuOptions[currentMenuSelection];
        System.out.println("Main Menu item selected: " + selectedOption);
        switch (selectedOption) {
            case "New Game":
                // The player name/gender prompt is still in Main.java
                // For a true in-game menu, this prompt should also be moved to a GameState
                // or handled after transitioning to IN_GAME.
                // For now, we'll just switch state. Main.java already collected player info.
                farmModel.setCurrentGameState(GameState.IN_GAME);
                // Game timer is already created, ensure it starts/resumes if it was paused for menu
                if (gameTimer != null && !gameTimer.isRunning()) {
                    gameTimer.start();
                }
                // Reset statisticsShown flag for a new game
                statisticsShown = false; 
                break;
            case "Load Game":
                JOptionPane.showMessageDialog(this, "Load Game feature is not yet implemented.", "Load Game", JOptionPane.INFORMATION_MESSAGE);
                break;
            case "Help":
                // Re-use existing JOptionPane for help for now
                 JOptionPane.showMessageDialog(this,
                            "Spakbor Hills - A Farming Adventure Game!\n\n" +
                            "Objective: Become a successful farmer and achieve milestones!\n\n" +
                            "Controls (In-Game):\n" +
                            "• WASD/Arrows: Move\n" +
                            "• E: Interact/Use Tool/Harvest\n" +
                            "• F: Eat Selected Item\n" +
                            "• T: Open Store\n" +
                            "• B: Open Shipping Bin\n" +
                            "• 1, 2: Cycle Inventory\n" +
                            "• X: Chat with NPC\n" +
                            "• G: Gift to NPC\n" +
                            "• L: Sleep\n" +
                            "• K: Cook\n" +
                            "• V: Watch TV\n" +
                            "• I: View Player Info\n" +
                            "• O: View Current Progress\n" +
                            "• C: Open Cheat Menu\n\n" +
                            "Menu Controls:\n" +
                            "• UP/DOWN Arrows: Navigate\n" +
                            "• ENTER: Select",
                            "Help", JOptionPane.INFORMATION_MESSAGE);
                break;
            case "Credits":
                // Re-use existing JOptionPane
                JOptionPane.showMessageDialog(this,
                            "Spakbor Hills - Game created by Kelompok Kito\n" +
                            "Inspired by Harvest Moon Series", 
                            "Credits", JOptionPane.INFORMATION_MESSAGE);
                break;
            case "Exit":
                System.out.println("Exiting Spakbor Hills via menu.");
                System.exit(0);
                break;
        }
    }

    private void drawNpcDialogue(Graphics g) {
        if (!isNpcDialogueActive) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create(); // Create a copy to not affect other drawings

        // 1. Draw Dialogue Box Background
        g2d.setColor(new Color(0, 0, 0, 200)); // Semi-transparent black
        g2d.fill(npcDialogueBox);
        g2d.setColor(Color.WHITE);
        g2d.draw(npcDialogueBox);

        // 2. Draw NPC Portrait (Placeholder)
        int portraitX = npcDialogueBox.x + DIALOGUE_PADDING;
        int portraitY = npcDialogueBox.y + DIALOGUE_PADDING;
        g2d.drawImage(npcPortraitPlaceholder, portraitX, portraitY, PORTRAIT_SIZE, PORTRAIT_SIZE, this);
        g2d.setColor(Color.GRAY);
        g2d.drawRect(portraitX,portraitY, PORTRAIT_SIZE, PORTRAIT_SIZE);


        // 3. Draw NPC Name
        g2d.setFont(DIALOGUE_NAME_FONT);
        FontMetrics nameFm = g2d.getFontMetrics();
        int nameX = portraitX + PORTRAIT_SIZE + DIALOGUE_PADDING;
        int nameY = npcDialogueBox.y + DIALOGUE_PADDING + nameFm.getAscent();
        g2d.setColor(Color.YELLOW); // Or any color for the name
        g2d.drawString(currentNpcName + " says:", nameX, nameY);

        // 4. Draw Dialogue Text (with basic word wrapping)
        g2d.setFont(DIALOGUE_TEXT_FONT);
        g2d.setColor(Color.WHITE);
        FontMetrics textFm = g2d.getFontMetrics();
        int textBlockStartX = npcDialogueBox.x + DIALOGUE_PADDING + PORTRAIT_SIZE + DIALOGUE_PADDING;
        int availableTextWidth = (npcDialogueBox.x + npcDialogueBox.width - DIALOGUE_PADDING) - textBlockStartX;
        
        List<String> lines = new ArrayList<>();
        String[] words = currentNpcDialogue.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (textFm.stringWidth(currentLine.toString() + word) < availableTextWidth) {
                currentLine.append(word).append(" ");
            } else {
                lines.add(currentLine.toString().trim());
                currentLine = new StringBuilder(word + " ");
            }
        }
        lines.add(currentLine.toString().trim()); // Add the last line

        int lineY = nameY + DIALOGUE_PADDING;
        for (String line : lines) {
            if (lineY + textFm.getHeight() > npcDialogueBox.y + npcDialogueBox.height - DIALOGUE_PADDING) { // Check bounds
                g2d.drawString("...", textBlockStartX, lineY); // Indicate more text if it overflows
                break;
            }
            g2d.drawString(line, textBlockStartX, lineY);
            lineY += textFm.getHeight();
        }


        // 5. Draw "Press Enter to continue" prompt
        g2d.setFont(DIALOGUE_TEXT_FONT.deriveFont(Font.ITALIC));
        String continuePrompt = "Press ENTER to continue...";
        int promptWidth = textFm.stringWidth(continuePrompt); // Use textFm from DIALOGUE_TEXT_FONT
        int promptX = npcDialogueBox.x + npcDialogueBox.width - promptWidth - DIALOGUE_PADDING;
        int promptY = npcDialogueBox.y + npcDialogueBox.height - DIALOGUE_PADDING;
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString(continuePrompt, promptX, promptY);

        g2d.dispose();
    }

    private void handleStoreInput(int keyCode) {
        if (!isStoreUiActive) return;

        if (storeInputMode.equals("selecting_item")) {
            switch (keyCode) {
                case KeyEvent.VK_UP:
                    if (currentStoreItemSelectionIndex > 0) {
                        currentStoreItemSelectionIndex--;
                    } else {
                        currentStoreItemSelectionIndex = storeItemsForDisplay.size() - 1; // Wrap around
                    }
                    storeFeedbackMessage = ""; // Hapus feedback saat navigasi
                    break;
                case KeyEvent.VK_DOWN:
                    if (currentStoreItemSelectionIndex < storeItemsForDisplay.size() - 1) {
                        currentStoreItemSelectionIndex++;
                    } else {
                        currentStoreItemSelectionIndex = 0; // Wrap around
                    }
                    storeFeedbackMessage = ""; // Hapus feedback saat navigasi
                    break;
                case KeyEvent.VK_ENTER:
                case KeyEvent.VK_E: // Use E as confirm
                    if (storeItemsForDisplay != null && !storeItemsForDisplay.isEmpty()) {
                        storeInputMode = "inputting_quantity";
                        currentBuyQuantity = 1; // Reset quantity when selecting new item
                        quantityInputString = "1"; // Reset juga string input kuantitas
                        storeFeedbackMessage = ""; // Hapus feedback saat ganti mode
                    }
                    break;
                case KeyEvent.VK_ESCAPE:
                case KeyEvent.VK_T: // Allow T to also close the store
                    isStoreUiActive = false;
                    storeFeedbackMessage = ""; // Hapus feedback saat tutup toko
                    break;
            }
        } else if (storeInputMode.equals("inputting_quantity")) {
            switch (keyCode) {
                case KeyEvent.VK_UP:
                    currentBuyQuantity++;
                    if (currentBuyQuantity > 999) currentBuyQuantity = 999; // Max quantity
                    quantityInputString = String.valueOf(currentBuyQuantity);
                    storeFeedbackMessage = "";
                    break;
                case KeyEvent.VK_DOWN:
                    if (currentBuyQuantity > 1) {
                        currentBuyQuantity--;
                    }
                    quantityInputString = String.valueOf(currentBuyQuantity);
                    storeFeedbackMessage = "";
                    break;
                case KeyEvent.VK_ENTER:
                case KeyEvent.VK_E: // Use E as confirm buy
                    if (storeItemsForDisplay != null && !storeItemsForDisplay.isEmpty()) {
                        Item selectedItem = storeItemsForDisplay.get(currentStoreItemSelectionIndex);
                        // Memanggil requestBuyItem dari GameController
                        // Asumsi requestBuyItem sekarang mengembalikan pesan atau status yang lebih detail
                        String buyAttemptMessage = gameController.requestBuyItemAndGetMessage(selectedItem.getName(), currentBuyQuantity);
                        
                        boolean success = !buyAttemptMessage.toLowerCase().contains("gagal") && 
                                          !buyAttemptMessage.toLowerCase().contains("tidak cukup") && 
                                          !buyAttemptMessage.toLowerCase().contains("tidak tersedia") &&
                                          !buyAttemptMessage.toLowerCase().contains("tidak dapat dibeli");

                        if (success) {
                            setStoreFeedback(buyAttemptMessage, false); // Pesan sukses
                            storeItemsForDisplay = gameController.getStoreItemsForDisplay(); 
                            if (storeItemsForDisplay == null || storeItemsForDisplay.isEmpty()) {
                                isStoreUiActive = false; 
                            } else {
                                currentStoreItemSelectionIndex = Math.min(currentStoreItemSelectionIndex, storeItemsForDisplay.size() - 1);
                                if (currentStoreItemSelectionIndex <0) currentStoreItemSelectionIndex = 0;
                            }
                        } else {
                            setStoreFeedback(buyAttemptMessage, true); // Pesan error
                        }
                        storeInputMode = "selecting_item"; 
                    }
                    break;
                case KeyEvent.VK_BACK_SPACE: // Go back to item selection without buying
                     storeInputMode = "selecting_item";
                     storeFeedbackMessage = "";
                     break;
                case KeyEvent.VK_ESCAPE:
                    isStoreUiActive = false;
                    storeFeedbackMessage = "";
                    break;
                 // Allow number input (basic, no text field yet)
                case KeyEvent.VK_0: case KeyEvent.VK_NUMPAD0: updateQuantityInput(0); storeFeedbackMessage = ""; break;
                case KeyEvent.VK_1: case KeyEvent.VK_NUMPAD1: updateQuantityInput(1); storeFeedbackMessage = ""; break;
                case KeyEvent.VK_2: case KeyEvent.VK_NUMPAD2: updateQuantityInput(2); storeFeedbackMessage = ""; break;
                case KeyEvent.VK_3: case KeyEvent.VK_NUMPAD3: updateQuantityInput(3); storeFeedbackMessage = ""; break;
                case KeyEvent.VK_4: case KeyEvent.VK_NUMPAD4: updateQuantityInput(4); storeFeedbackMessage = ""; break;
                case KeyEvent.VK_5: case KeyEvent.VK_NUMPAD5: updateQuantityInput(5); storeFeedbackMessage = ""; break;
                case KeyEvent.VK_6: case KeyEvent.VK_NUMPAD6: updateQuantityInput(6); storeFeedbackMessage = ""; break;
                case KeyEvent.VK_7: case KeyEvent.VK_NUMPAD7: updateQuantityInput(7); storeFeedbackMessage = ""; break;
                case KeyEvent.VK_8: case KeyEvent.VK_NUMPAD8: updateQuantityInput(8); storeFeedbackMessage = ""; break;
                case KeyEvent.VK_9: case KeyEvent.VK_NUMPAD9: updateQuantityInput(9); storeFeedbackMessage = ""; break;
            }
        }
        repaint(); // Selalu repaint setelah input toko
    }

    // Helper for basic number input for quantity (appends digit)
    private String quantityInputString = ""; 
    private void updateQuantityInput(int digit) {
        if (storeInputMode.equals("inputting_quantity")) {
            if (quantityInputString.length() < 3) { // Max 3 digits for quantity (e.g., up to 999)
                quantityInputString += digit;
                try {
                    currentBuyQuantity = Integer.parseInt(quantityInputString);
                    if (currentBuyQuantity == 0 && quantityInputString.length() > 0) currentBuyQuantity = 1; // Avoid 0 if user types "0" then another digit
                    if (currentBuyQuantity > 999) currentBuyQuantity = 999;

                } catch (NumberFormatException e) {
                    // Should not happen if only digits are appended
                    quantityInputString = ""; 
                    currentBuyQuantity = 1;
                }
            }
             // If user presses a number, reset quantity and start new input.
            if (digit != -1) { // -1 could signify a reset or non-digit action
                 if (currentBuyQuantity > 0 && currentBuyQuantity <100 && quantityInputString.length() <2 ) { // if current quant is 1-99 and we add a digit
                    currentBuyQuantity = currentBuyQuantity * 10 + digit;
                 } else {
                     currentBuyQuantity = digit; // Start new number
                 }
                 if (currentBuyQuantity == 0) currentBuyQuantity =1; // Min 1
                 if (currentBuyQuantity > 999) currentBuyQuantity = 999; // Max 999
                 quantityInputString = String.valueOf(currentBuyQuantity); // Keep string in sync for next potential digit.
            }
        }
    }

    private void drawStoreUI(Graphics2D g2d) {
        if (!isStoreUiActive || storeItemsForDisplay == null) return;

        g2d.setColor(STORE_BG_COLOR);
        g2d.fillRect(storePanelRect.x, storePanelRect.y, storePanelRect.width, storePanelRect.height);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(storePanelRect.x, storePanelRect.y, storePanelRect.width, storePanelRect.height);

        g2d.setFont(DIALOGUE_NAME_FONT);
        g2d.setColor(STORE_TEXT_COLOR);
        String title = "Toko Spakbor Hills";
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(title);
        g2d.drawString(title, storePanelRect.x + (storePanelRect.width - titleWidth) / 2, storePanelRect.y + 30);

        g2d.setFont(STORE_FONT);
        g2d.setColor(STORE_TEXT_COLOR);
        g2d.drawString("[Esc] Tutup", storeCloseButtonRect.x + 5, storeCloseButtonRect.y + 20);

        g2d.setFont(STORE_ITEM_FONT);
        int itemY = storeItemListRect.y;
        int itemLineHeight = g2d.getFontMetrics().getHeight() + 2;
        int maxNameLength = 0;
        if (storeItemsForDisplay != null && !storeItemsForDisplay.isEmpty()) {
            for (Item item : storeItemsForDisplay) {
                maxNameLength = Math.max(maxNameLength, item.getName().length());
            }
        }
        maxNameLength = Math.max(maxNameLength, "Nama Item".length());
        String formatString = "%-" + (maxNameLength + 2) + "s %7s";
        g2d.drawString(String.format(formatString, "Nama Item", "Harga"), storeItemListRect.x, itemY);
        itemY += itemLineHeight;
        g2d.drawString(String.format(formatString, "---------", "-----"), storeItemListRect.x, itemY);
        itemY += itemLineHeight;

        int bottomReservedSpace = (itemLineHeight * 3) + 20; // Naikkan sedikit untuk feedback message

        if (storeItemsForDisplay != null) {
            for (int i = 0; i < storeItemsForDisplay.size(); i++) {
                if (itemY > (storeItemListRect.y + storeItemListRect.height - bottomReservedSpace)) {
                    g2d.drawString("...", storeItemListRect.x, itemY);
                    break;
                }
                Item item = storeItemsForDisplay.get(i);
                int price = farmModel.getPriceList().getBuyPrice(item.getName());
                String priceText = price == 0 ? "Gratis" : price + " G";
                String itemText = String.format(formatString, item.getName(), priceText);
                if (i == currentStoreItemSelectionIndex) {
                    g2d.setColor(STORE_HIGHLIGHT_COLOR);
                    g2d.drawString("> " + itemText, storeItemListRect.x, itemY);
                    g2d.setColor(STORE_TEXT_COLOR);
                } else {
                    g2d.drawString("  " + itemText, storeItemListRect.x, itemY);
                }
                itemY += itemLineHeight;
            }
        }
        
        FontMetrics bottomFontMetrics = g2d.getFontMetrics(STORE_FONT);
        int bottomTextHeight = bottomFontMetrics.getHeight();
        int goldTextY = storePanelRect.y + storePanelRect.height - 20;
        String goldText = "Gold: " + farmModel.getPlayer().getGold() + " G";

        // Posisi untuk feedback message, di atas Gold
        int feedbackTextY = goldTextY - bottomTextHeight - 5; // 5px spasi di atas Gold

        if (storeItemsForDisplay != null && !storeItemsForDisplay.isEmpty() && currentStoreItemSelectionIndex < storeItemsForDisplay.size()) {
            if (storeInputMode.equals("inputting_quantity")) {
                Item selectedItem = storeItemsForDisplay.get(currentStoreItemSelectionIndex);
                g2d.setFont(STORE_FONT);
                g2d.setColor(STORE_TEXT_COLOR);
                String promptText1 = "Beli " + selectedItem.getName() + "? Jumlah: " + currentBuyQuantity;
                String promptText2 = "([Up]/[Down] Ubah Jumlah)";
                int quantityPromptX = storePanelRect.x + 20; // Mulai dari kiri seperti Gold
                
                // Posisi Y untuk prompt kuantitas, di atas feedback atau Gold
                int quantityPromptY1 = feedbackTextY - bottomTextHeight - 5; // Di atas feedback
                int quantityPromptY2 = feedbackTextY; // Sejajar dengan Y feedback (atau sedikit di bawahnya jika feedback panjang)

                g2d.drawString(promptText1, quantityPromptX, quantityPromptY1);
                g2d.drawString(promptText2, quantityPromptX, quantityPromptY2);

                g2d.setColor(STORE_HIGHLIGHT_COLOR);
                g2d.drawString("[E/Enter] Beli", storeBuyButtonRect.x, quantityPromptY1);
                g2d.setColor(STORE_TEXT_COLOR);
                g2d.drawString("[Esc/Bksp] Batal", storeBuyButtonRect.x, quantityPromptY2);
            } else {
                g2d.setFont(STORE_FONT);
                g2d.setColor(STORE_TEXT_COLOR);
                String instructionText = "([Up]/[Down] Pilih Item, [E/Enter] Pilih)";
                FontMetrics instructionFm = g2d.getFontMetrics();
                // Gambar instruksi umum di atas feedback atau Gold
                g2d.drawString(instructionText, storePanelRect.x + 20, feedbackTextY - bottomTextHeight -5 ); 
            }
        } else if (storeItemsForDisplay == null || storeItemsForDisplay.isEmpty()) {
             g2d.setFont(STORE_FONT);
             g2d.setColor(Color.YELLOW);
             String emptyStoreMsg = "Toko sedang kosong!";
             FontMetrics msgFm = g2d.getFontMetrics();
             g2d.drawString(emptyStoreMsg, storePanelRect.x + (storePanelRect.width - msgFm.stringWidth(emptyStoreMsg))/2, storeItemListRect.y + storeItemListRect.height / 2 );
        }

        // Gambar feedback message jika ada
        if (storeFeedbackMessage != null && !storeFeedbackMessage.isEmpty()) {
            g2d.setFont(STORE_FONT);
            g2d.setColor(storeFeedbackColor);
            FontMetrics feedbackFm = g2d.getFontMetrics();
            // Pusatkan teks feedback jika memungkinkan, atau letakkan di kiri
            int feedbackX = storePanelRect.x + 20; 
            // Jika ingin tengah: storePanelRect.x + (storePanelRect.width - feedbackFm.stringWidth(storeFeedbackMessage)) / 2;
            g2d.drawString(storeFeedbackMessage, feedbackX, feedbackTextY);
        }
        
        // Gambar Gold terakhir agar selalu di atas jika ada overlap (seharusnya tidak dengan layout baru)
        g2d.setFont(STORE_FONT);
        g2d.setColor(STORE_TEXT_COLOR);
        g2d.drawString(goldText, storePanelRect.x + 20, goldTextY);
    }

    // Metode baru untuk mengatur feedback di UI Toko
    private void setStoreFeedback(String message, boolean isError) {
        this.storeFeedbackMessage = message;
        this.storeFeedbackColor = isError ? Color.RED : new Color(144, 238, 144); // Merah untuk error, hijau muda untuk sukses
        if (storeFeedbackTimer.isRunning()) {
            storeFeedbackTimer.restart();
        } else {
            storeFeedbackTimer.start();
        }
        repaint(); // Langsung repaint untuk menampilkan feedback
    }
}
