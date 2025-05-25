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
import com.spakborhills.model.Util.ShippingBin; // Corrected import path
import com.spakborhills.model.Object.DeployedObject; // Added import for DeployedObject

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
import java.util.Set; // For alreadyDrawnLargeObjects
import java.util.HashSet; // For alreadyDrawnLargeObjects

public class GamePanel extends JPanel implements KeyListener { // Implement KeyListener

    // GamePanel class: Handles all visual rendering and user input for the game.

    private static final int TILE_SIZE = 96;
    private static final int VIEWPORT_WIDTH_IN_TILES = 20;
    private static final int VIEWPORT_HEIGHT_IN_TILES = 10;
    private static final int INFO_PANEL_HEIGHT = 100;
    private Farm farmModel;
    private GameController gameController;
    private static final Font DIALOG_FONT = new Font("Arial", Font.PLAIN, 20); // Updated font size to 20
    private static final Font NPC_DIALOG_FONT = new Font("Arial", Font.PLAIN, 16); // Font for NPC dialogues

    // private NPC currentInteractingNPC;

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

    private NPC currentInteractingNPC;
    private javax.swing.Timer animationTimer;

    // Shipping Bin UI State
    private List<Item> playerSellableItems; // Items from player inventory that can be sold
    private List<Item> itemsInBinSession; // Items added to bin in current session
    private int currentPlayerItemSelectionIndex = 0;
    private int currentBinItemSelectionIndex = 0; // If we allow selecting/managing items in bin UI
    private String shippingBinInputMode = "selecting_player_item"; // "selecting_player_item", "inputting_quantity"
    private String shippingBinQuantityInputString = "";
    private Item currentShippingBinItemForQuantity;
    private Rectangle shippingBinPanelRect;
    private Rectangle playerItemsListRect;
    private Rectangle binItemsListRect;
    private Rectangle shippingBinQuantityRect;
    private Rectangle shippingBinConfirmButtonRect; // Could be implicit with Enter
    private Rectangle shippingBinCloseButtonRect;
    private static final Font SHIPPING_BIN_FONT = new Font("Arial", Font.PLAIN, 18);
    private static final Font SHIPPING_BIN_ITEM_FONT = new Font("Monospaced", Font.PLAIN, 16);
    private static final Color SHIPPING_BIN_BG_COLOR = new Color(30, 30, 70, 220); // Darker blue
    private static final Color SHIPPING_BIN_TEXT_COLOR = Color.WHITE;
    private static final Color SHIPPING_BIN_HIGHLIGHT_COLOR = Color.CYAN;
    private String shippingBinFeedbackMessage = "";
    private Color shippingBinFeedbackColor = SHIPPING_BIN_TEXT_COLOR;
    private Timer shippingBinFeedbackTimer;

    // General In-Game Message State (for non-modal feedback)
    private String generalGameMessage = "";
    private Color generalGameMessageColor = Color.WHITE;
    private Timer generalGameMessageTimer;
    private static final Font GENERAL_MESSAGE_FONT = new Font("Arial", Font.BOLD, 22);

    // Cheat Input UI State
    private String cheatInputString = "";
    private Rectangle cheatInputPanelRect;
    private static final Font CHEAT_INPUT_FONT = new Font("Monospaced", Font.PLAIN, 20);
    private static final Color CHEAT_INPUT_BG_COLOR = new Color(20, 20, 20, 230); // Very dark semi-transparent
    private static final Color CHEAT_INPUT_TEXT_COLOR = Color.GREEN;

    // End of Day Summary UI State
    private String endOfDayEventMessage = "";
    private int endOfDayIncome = 0;
    private String endOfDayNewDayInfo = "";
    private Rectangle endOfDayPanelRect;
    private static final Font END_OF_DAY_FONT_TITLE = new Font("Arial", Font.BOLD, 28);
    private static final Font END_OF_DAY_FONT_TEXT = new Font("Arial", Font.PLAIN, 20);
    private static final Color END_OF_DAY_BG_COLOR = new Color(50, 50, 70, 230); // Dark blueish-purple
    private static final Color END_OF_DAY_TEXT_COLOR = Color.WHITE;

    // World Map Selection UI State
    private List<String> worldMapDestinations;
    private int currentWorldMapSelectionIndex = 0;
    private Rectangle worldMapPanelRect;
    private static final Font WORLD_MAP_FONT_TITLE = new Font("Arial", Font.BOLD, 28);
    private static final Font WORLD_MAP_FONT_ITEM = new Font("Arial", Font.PLAIN, 22);
    private static final Color WORLD_MAP_BG_COLOR = new Color(60, 100, 60, 220); // Forest green-ish
    private static final Color WORLD_MAP_TEXT_COLOR = Color.WHITE;
    private static final Color WORLD_MAP_HIGHLIGHT_COLOR = Color.YELLOW;

    // Tile Images
    private BufferedImage tillableImage;
    private BufferedImage tilledImage;
    private BufferedImage plantedImage;
    private BufferedImage harvestableImage;
    private BufferedImage waterImage; // For watered soil or water bodies
    private BufferedImage grassImage; // Example for GRASS TileType
    private BufferedImage obstacleImage; // Example for OBSTACLE TileType
    private BufferedImage plantWateredImage; // For watered planted tiles
    private BufferedImage shippingBinImage; // For ShippingBinObject
    private BufferedImage houseTileImage; // Added: For individual house tiles
    private BufferedImage portalImage; // Added: For ENTRY_POINT tiles
    private BufferedImage woodFloorImage;
    private BufferedImage stoneFloorImage;
    private BufferedImage carpetFloorImage;
    private BufferedImage luxuryFloorImage;
    private BufferedImage dirtFloorImage;
    private BufferedImage wallImage;
    private BufferedImage storeTileImage;
    // Add more BufferedImages for other tile types as needed

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
                            // Check for time-based pass-out AFTER time has advanced
                            if (gameController != null) {
                                gameController.checkTimeBasedPassOut(); 
                            }
                            // NOTE: repaint() dipindah ke animationTimer untuk sinkronisasi yang lebih baik
                        }
                    } else if (farmModel.getCurrentGameState() == GameState.MAIN_MENU) {
                        repaint(); // Keep repainting menu for potential animations or cursor blink later
                    }
                }
            }
        });
    
        // Timer untuk animasi (100ms = 10 FPS untuk animasi smooth)
        animationTimer = new javax.swing.Timer(80, new ActionListener() { // Misal, sekitar 12 FPS (80ms delay)
            @Override
            public void actionPerformed(ActionEvent e) {
                if (farmModel != null && farmModel.getPlayer() != null) {
                    if (farmModel.getCurrentGameState() == GameState.IN_GAME || 
                        farmModel.getCurrentGameState() == GameState.SHIPPING_BIN || // Tambahkan state UI lain jika perlu animasi player
                        farmModel.getCurrentGameState() == GameState.STORE_UI ||
                        farmModel.getCurrentGameState() == GameState.CHEAT_INPUT) {
                        
                        // Update animasi pemain
                        farmModel.getPlayer().updateAnimation();
                        
                        // Update animasi NPC jika ada
                        // Mengambil semua NPC dari farmModel, lalu cek apakah mereka di peta saat ini
                        MapArea currentPlayerMap = farmModel.getPlayer().getCurrentMap();
                        if (currentPlayerMap != null && farmModel.getNPCs() != null) {
                            for (NPC npc : farmModel.getNPCs()) { // Iterasi semua NPC dari Farm
                                if (npc != null) {
                                    // Cek apakah NPC berada di peta yang sama dengan pemain
                                    MapArea npcMap = farmModel.getMapArea(npc.getHomeLocation()); // Asumsi homeLocation adalah map NPC berada
                                    if (npcMap == currentPlayerMap) {
                                        // Jika NPC memiliki metode updateAnimation, panggil di sini
                                        npc.updateAnimation();
                                        // npc.updateAI();
                                        // Untuk sekarang, kita belum implementasi updateAnimation() di NPC.java abstrak
                                        // Jika sudah ada, uncomment baris di atas.
                                    }
                                }
                            }
                        }
                        repaint(); // Repaint untuk update visual animasi
                    } else if (farmModel.getCurrentGameState() == GameState.MAIN_MENU) {
                        repaint(); // Repaint menu jika ada elemen dinamis
                    }
                }
            }
        });
        animationTimer.start(); // Jangan lupa start animationTimer
    
        // Start kedua timer
        // gameTimer.start(); // GameTimer starts based on game state now
        // animationTimer.start(); // animationTimer starts based on game state now

        loadTileImages(); // Load tile images

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

        // Initialize Shipping Bin UI Rectangles & Timer
        int shippingBinPanelWidth = VIEWPORT_WIDTH_IN_TILES * TILE_SIZE * 5 / 6; // Slightly wider
        int shippingBinPanelHeight = VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE * 5 / 6; // Slightly shorter, more centered
        int shippingBinPanelX = (VIEWPORT_WIDTH_IN_TILES * TILE_SIZE - shippingBinPanelWidth) / 2;
        int shippingBinPanelY = (VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE - shippingBinPanelHeight) / 2 + INFO_PANEL_HEIGHT / 2; // Shift down a bit due to info panel
        shippingBinPanelRect = new Rectangle(shippingBinPanelX, shippingBinPanelY, shippingBinPanelWidth, shippingBinPanelHeight);

        int listWidth = shippingBinPanelWidth / 2 - 30;
        int listHeight = shippingBinPanelHeight - 120; // Space for title, buttons, quantity input

        playerItemsListRect = new Rectangle(shippingBinPanelX + 20, shippingBinPanelY + 60, listWidth, listHeight);
        binItemsListRect = new Rectangle(shippingBinPanelX + shippingBinPanelWidth / 2 + 10, shippingBinPanelY + 60, listWidth, listHeight);
        
        shippingBinQuantityRect = new Rectangle(shippingBinPanelX + 20, shippingBinPanelY + shippingBinPanelHeight - 50, listWidth, 30); // For quantity input
        // Confirm button might be implicit (Enter), Close button for explicit exit
        shippingBinCloseButtonRect = new Rectangle(shippingBinPanelX + shippingBinPanelWidth - 120, shippingBinPanelY + 20, 100, 30);

        shippingBinFeedbackTimer = new Timer(3000, e -> {
            shippingBinFeedbackMessage = "";
            repaint();
        });
        shippingBinFeedbackTimer.setRepeats(false);

        // Initialize Timer for general game messages
        generalGameMessageTimer = new Timer(3500, e -> { // Message disappears after 3.5 seconds
            generalGameMessage = "";
            repaint();
        });
        generalGameMessageTimer.setRepeats(false);

        // Initialize Cheat Input UI Rectangles
        int cheatPanelWidth = VIEWPORT_WIDTH_IN_TILES * TILE_SIZE / 2; // Half viewport width
        int cheatPanelHeight = 80; // Fixed height for a single input line
        int cheatPanelX = (VIEWPORT_WIDTH_IN_TILES * TILE_SIZE - cheatPanelWidth) / 2;
        // Position it a bit above the center vertically
        int cheatPanelY = (VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE + INFO_PANEL_HEIGHT) / 2 - cheatPanelHeight - 20;
        cheatInputPanelRect = new Rectangle(cheatPanelX, cheatPanelY, cheatPanelWidth, cheatPanelHeight);

        // Initialize End of Day Summary Panel Rect
        int eodPanelWidth = VIEWPORT_WIDTH_IN_TILES * TILE_SIZE * 2 / 3;
        int eodPanelHeight = VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE / 2;
        int eodPanelX = (VIEWPORT_WIDTH_IN_TILES * TILE_SIZE - eodPanelWidth) / 2;
        int eodPanelY = (VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE + INFO_PANEL_HEIGHT - eodPanelHeight) / 2; // Centered
        endOfDayPanelRect = new Rectangle(eodPanelX, eodPanelY, eodPanelWidth, eodPanelHeight);

        // Initialize World Map Selection Panel Rect (similar to End of Day summary)
        int wmPanelWidth = VIEWPORT_WIDTH_IN_TILES * TILE_SIZE * 2 / 3;
        int wmPanelHeight = VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE / 2;
        int wmPanelX = (VIEWPORT_WIDTH_IN_TILES * TILE_SIZE - wmPanelWidth) / 2;
        int wmPanelY = (VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE + INFO_PANEL_HEIGHT - wmPanelHeight) / 2; // Centered
        worldMapPanelRect = new Rectangle(wmPanelX, wmPanelY, wmPanelWidth, wmPanelHeight);
    }

    private void loadTileImages() {
        try {
            tillableImage = ImageIO.read(getClass().getResourceAsStream("/assets/sprites/tile/tillable.png"));
            tilledImage = ImageIO.read(getClass().getResourceAsStream("/assets/sprites/tile/tilled.png"));
            plantedImage = ImageIO.read(getClass().getResourceAsStream("/assets/sprites/tile/planted.png"));
            harvestableImage = ImageIO.read(getClass().getResourceAsStream("/assets/sprites/tile/harvestable.png"));
            waterImage = ImageIO.read(getClass().getResourceAsStream("/assets/sprites/tile/water.png"));
            plantWateredImage = ImageIO.read(getClass().getResourceAsStream("/assets/sprites/tile/plant_watered.png"));
            shippingBinImage = ImageIO.read(getClass().getResourceAsStream("/assets/sprites/tile/shippingbin.png"));
            houseTileImage = ImageIO.read(getClass().getResourceAsStream("/assets/sprites/tile/house_tile.png"));
            portalImage = ImageIO.read(getClass().getResourceAsStream("/assets/sprites/tile/portal.png"));
            grassImage = ImageIO.read(getClass().getResourceAsStream("/assets/sprites/tile/grass.png"));
            // obstacleImage = ImageIO.read(getClass().getResourceAsStream("/assets/sprites/tile/obstacle.png")); // Temporarily commented out
            woodFloorImage = ImageIO.read(getClass().getResourceAsStream("/assets/sprites/tile/wood_tile.png"));
            stoneFloorImage = ImageIO.read(getClass().getResourceAsStream("/assets/sprites/tile/stone_tile.png"));
            carpetFloorImage = ImageIO.read(getClass().getResourceAsStream("/assets/sprites/tile/carpet_tile.png"));
            luxuryFloorImage = ImageIO.read(getClass().getResourceAsStream("/assets/sprites/tile/luxury_tile.png"));
            dirtFloorImage = ImageIO.read(getClass().getResourceAsStream("/assets/sprites/tile/dirt_tile.png"));
            wallImage = ImageIO.read(getClass().getResourceAsStream("/assets/sprites/tile/wall.png"));
            storeTileImage = ImageIO.read(getClass().getResourceAsStream("/assets/sprites/tile/store_tile.png"));

            if (tillableImage == null) System.err.println("Failed to load tillable.png");
            if (tilledImage == null) System.err.println("Failed to load tilled.png");
            if (plantedImage == null) System.err.println("Failed to load planted.png");
            if (harvestableImage == null) System.err.println("Failed to load harvestable.png");
            if (waterImage == null) System.err.println("Failed to load water.png");
            if (plantWateredImage == null) System.err.println("Failed to load plant_watered.png");
            if (shippingBinImage == null) System.err.println("Failed to load shippingbin.png");
            if (houseTileImage == null) System.err.println("Failed to load house_tile.png");
            if (portalImage == null) System.err.println("Failed to load portal.png");
            if (grassImage == null) System.err.println("Failed to load grass.png");
            // if (obstacleImage == null) System.err.println("Failed to load obstacle.png"); // Temporarily commented out
            if (woodFloorImage == null) System.err.println("Failed to load wood_tile.png");
            if (stoneFloorImage == null) System.err.println("Failed to load stone_tile.png");
            if (carpetFloorImage == null) System.err.println("Failed to load carpet_tile.png");
            if (luxuryFloorImage == null) System.err.println("Failed to load luxury_tile.png");
            if (dirtFloorImage == null) System.err.println("Failed to load dirt_tile.png");
            if (wallImage == null) System.err.println("Failed to load wall.png");
            if (storeTileImage == null) System.err.println("Failed to load store_tile.png");

        } catch (IOException e) {
            System.err.println("Error loading tile images: " + e.getMessage());
            e.printStackTrace();
            // Consider setting placeholder images or colors if loading fails
        } catch (IllegalArgumentException e) {
            System.err.println("Error with image path (IllegalArgumentException): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setGeneralGameMessage(String message, boolean isError) {
        this.generalGameMessage = message;
        this.generalGameMessageColor = isError ? new Color(255, 80, 80) : new Color(144, 238, 144); // Red for error, light green for info
        if (generalGameMessageTimer.isRunning()) {
            generalGameMessageTimer.restart();
        } else {
            generalGameMessageTimer.start();
        }
        repaint(); // Immediately repaint to show the message
    }

    public void stopAllTimers() {
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
            System.out.println("GamePanel: Game timer stopped.");
        }
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
            System.out.println("GamePanel: Animation timer stopped.");
        }
    }
    
    // Update method yang sudah ada untuk stop timer
    public void stopGameTimer() {
        stopAllTimers(); // Ganti dengan method baru
    }
    
    // Jika ada method untuk restart/resume game, tambahkan ini:
    public void startAllTimers() {
        if (gameTimer != null && !gameTimer.isRunning()) {
            gameTimer.start();
            System.out.println("GamePanel: Game timer started.");
        }
        if (animationTimer != null && !animationTimer.isRunning()) {
            animationTimer.start();
            System.out.println("GamePanel: Animation timer started.");
        }
    }
    
    // Optional: Method untuk pause/resume hanya animasi (misal saat dialog)
    public void pauseAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
    }
    
    public void resumeAnimation() {
        if (animationTimer != null && !animationTimer.isRunning() && 
            farmModel != null && farmModel.getCurrentGameState() == GameState.IN_GAME) {
            animationTimer.start();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create(); // Work with a copy for transformations etc.

        // Clear the panel
        g2d.setColor(getBackground()); // Use the panel's background color
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (farmModel == null || farmModel.getPlayer() == null) {
            // Draw loading screen or simple message
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            String loadingMsg = "Game Model or Player not initialized.";
            FontMetrics fm = g2d.getFontMetrics();
            int msgWidth = fm.stringWidth(loadingMsg);
            g2d.drawString(loadingMsg, (getWidth() - msgWidth) / 2, getHeight() / 2);
            g2d.dispose();
            return;
        }

        GameState currentState = farmModel.getCurrentGameState();

        if (currentState == GameState.MAIN_MENU) {
            drawMainMenu(g2d);
        } else if (currentState == GameState.END_OF_DAY_SUMMARY) {
             // Draw game world behind summary first (optional, or just a dark overlay)
            drawCurrentMap(g2d);
            drawPlayer(g2d);
            drawNPCs(g2d); 
            // Then draw the summary UI on top
            drawEndOfDaySummaryUI(g2d);
        } else {
            // For IN_GAME, STORE_UI, NPC_DIALOGUE, etc., draw the game world
            drawCurrentMap(g2d);
            drawPlayer(g2d);
            drawNPCs(g2d); // Make sure this is called to draw NPCs
            drawPlayerInfo(g2d); // Draw player info panel at the bottom
            // drawDayNightTint(g2d); // Temporarily commented out for testing house rendering
            
            // Draw UI elements on top based on state
            if (currentState == GameState.NPC_DIALOGUE) {
                drawNpcDialogue(g2d);
            } else if (currentState == GameState.STORE_UI) {
                drawStoreUI(g2d);
            } else if (currentState == GameState.SHIPPING_BIN) {
                drawShippingBinUI(g2d);
            } else if (currentState == GameState.CHEAT_INPUT) {
                drawCheatInputUI(g2d);
            } else if (currentState == GameState.WORLD_MAP_SELECTION) { // Added new state
                drawWorldMapSelectionUI(g2d);
            }
        }

        // Draw general game messages on top of everything if active
        drawGeneralGameMessage(g2d);

        g2d.dispose(); // Dispose of the graphics copy
    }

    private void drawGeneralGameMessage(Graphics2D g2d) {
        if (generalGameMessage.isEmpty()) {
            return;
        }

        FontMetrics fm = g2d.getFontMetrics(GENERAL_MESSAGE_FONT);
        int messageWidth = fm.stringWidth(generalGameMessage);
        int messageHeight = fm.getHeight();

        // Position it near the top-center of the game viewport (below info panel)
        int x = (getWidth() - messageWidth) / 2;
        int y = INFO_PANEL_HEIGHT + messageHeight + 15; // 15px padding below info panel

        // Draw a semi-transparent background for the message for better readability
        g2d.setColor(new Color(0, 0, 0, 180)); // Semi-transparent black
        g2d.fillRect(x - 10, y - messageHeight, messageWidth + 20, messageHeight + 10); // Padding around text

        g2d.setFont(GENERAL_MESSAGE_FONT);
        g2d.setColor(generalGameMessageColor);
        g2d.drawString(generalGameMessage, x, y);
    }

    private void drawCheatInputUI(Graphics2D g2d) {
        if (farmModel.getCurrentGameState() != GameState.CHEAT_INPUT) {
            return;
        }

        // Panel Background
        g2d.setColor(CHEAT_INPUT_BG_COLOR);
        g2d.fill(cheatInputPanelRect);
        g2d.setColor(CHEAT_INPUT_TEXT_COLOR.brighter());
        g2d.draw(cheatInputPanelRect);

        // Prompt Text
        g2d.setFont(CHEAT_INPUT_FONT.deriveFont(Font.ITALIC));
        g2d.setColor(CHEAT_INPUT_TEXT_COLOR.darker()); // Slightly dimmer for prompt
        String promptText = "Enter Cheat Code (Esc to Cancel):";
        FontMetrics fmPrompt = g2d.getFontMetrics();
        int promptX = cheatInputPanelRect.x + 10;
        int promptY = cheatInputPanelRect.y + fmPrompt.getAscent() + 5;
        g2d.drawString(promptText, promptX, promptY);

        // Input String
        g2d.setFont(CHEAT_INPUT_FONT);
        g2d.setColor(CHEAT_INPUT_TEXT_COLOR);
        // Show a blinking cursor (simple underscore)
        String displayInput = cheatInputString + (System.currentTimeMillis() / 500 % 2 == 0 ? "_" : "");
        int inputX = cheatInputPanelRect.x + 10;
        int inputY = cheatInputPanelRect.y + cheatInputPanelRect.height - fmPrompt.getDescent() - 10; // Position towards bottom
        g2d.drawString("> " + displayInput, inputX, inputY);
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

        if (currentMap == null) {
            System.err.println("GamePanel.drawCurrentMap: currentMap is null.");
            return;
        }

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

        for (int row = 0; row < mapHeightInTiles; row++) {
            for (int col = 0; col < mapWidthInTiles; col++) {
                int screenX = col * TILE_SIZE - camX;
                int screenY = row * TILE_SIZE - camY + INFO_PANEL_HEIGHT;

                if (screenX + TILE_SIZE <= 0 || screenX >= getWidth() ||
                    screenY + TILE_SIZE <= INFO_PANEL_HEIGHT || screenY >= getHeight()) {
                    continue;
                }

                Tile currentTile = currentMap.getTile(col, row);
                if (currentTile == null) {
                    // Draw a default color if tile is unexpectedly null
                    g.setColor(Color.PINK); // Indicates an error or uninitialized tile
                    g.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
                    continue;
                }

                Image imageToDraw = null;
                TileType type = currentTile.getType();

                // Special handling for Store map
                if (currentMap instanceof com.spakborhills.model.Store) {
                    if (type == TileType.DEPLOYED_OBJECT) {
                        DeployedObject associatedObj = currentTile.getAssociatedObject();
                        // Handle specific deployed objects in store if needed, e.g., counter
                        // For now, assuming they have their own sprites or will be handled by the generic DEPLOYED_OBJECT case below if not specific.
                        // This 'if' block for DEPLOYED_OBJECT in store might need more specific logic
                        // if store objects are different from farm/house objects.
                        // Let it fall through to the main switch for now if no store-specific object logic is here.
                    } else if (type == TileType.WALL) {
                        imageToDraw = wallImage;
                    } else if (type == TileType.ENTRY_POINT) {
                        imageToDraw = portalImage;
                    } else {
                        // For most other tile types on the store map, use storeTileImage as the floor
                        imageToDraw = storeTileImage;
                    }
                }

                // If not in store, or if in store and imageToDraw is still null (e.g., for a DEPLOYED_OBJECT)
                if (imageToDraw == null) {
                    switch (type) {
                        case PLANTED:
                            if (currentTile.isHarvestable()) {
                                imageToDraw = harvestableImage;
                            } else if (currentTile.isWatered()) {
                                imageToDraw = plantWateredImage;
                            } else {
                                imageToDraw = plantedImage;
                            }
                            break;
                        case TILLED:
                            imageToDraw = tilledImage;
                            break;
                        case TILLABLE:
                            imageToDraw = tillableImage;
                            break;
                        case GRASS:
                            imageToDraw = grassImage;
                            break;
                        case OBSTACLE:
                            imageToDraw = obstacleImage;
                            break;
                        case WATER:
                            imageToDraw = waterImage;
                            break;
                        case ENTRY_POINT:
                            imageToDraw = portalImage;
                            break;
                        case DEPLOYED_OBJECT:
                            DeployedObject associatedObj = currentTile.getAssociatedObject();
                            if (associatedObj instanceof com.spakborhills.model.Object.House) {
                                imageToDraw = houseTileImage;
                            } else if (associatedObj instanceof com.spakborhills.model.Object.ShippingBinObject) {
                                imageToDraw = shippingBinImage;
                            } else {
                                // Placeholder for other deployed objects if they have a generic tile
                            }
                            break;
                        case WOOD_FLOOR:
                            imageToDraw = woodFloorImage;
                            break;
                        case STONE_FLOOR:
                            imageToDraw = stoneFloorImage;
                            break;
                        case CARPET_FLOOR:
                            imageToDraw = carpetFloorImage;
                            break;
                        case LUXURY_FLOOR:
                            imageToDraw = luxuryFloorImage;
                            break;
                        case DIRT_FLOOR:
                            imageToDraw = dirtFloorImage;
                            break;
                        case WALL:
                            imageToDraw = wallImage;
                            break;
                        default:
                            // Fallback handled after this switch
                            break;
                    }
                }
                
                // Draw the selected base image
                if (imageToDraw != null) {
                    g.drawImage(imageToDraw, screenX, screenY, TILE_SIZE, TILE_SIZE, this);
                } else { // Simplified fallback logic for tiles that genuinely don't have an image
                    // Fallback for unhandled tile types or if an image is missing.
                    g.setColor(new Color(128, 0, 128, 150)); // Semi-transparent Purple for fallback
                    g.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
                    g.setColor(Color.WHITE);
                    g.drawString(type.toString().substring(0, Math.min(type.toString().length(),3)), screenX + 5, screenY + 20);
                }

                // Overlay for watered state (if applicable)
                // This draws water.png on top of TILLED tiles if they are watered.
                // Planted tiles now have their own watered state image (plantWateredImage).
                if (currentTile.isWatered() && type == TileType.TILLED) { // Only for TILLED type now
                    if (waterImage != null) {
                        g.drawImage(waterImage, screenX, screenY, TILE_SIZE, TILE_SIZE, this);
                    }
                }
                
                // Optional: Draw grid lines for debugging
                // g.setColor(new Color(200, 200, 200, 50)); // Light semi-transparent gray
                // g.drawRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
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
    
        // Kalkulasi kamera (camX, camY)
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
    
        for (NPC npc : allNPCs) {
            MapArea npcHomeMapInstance = farmModel.getMapArea(npc.getHomeLocation());
    
            if (currentMap == npcHomeMapInstance) { // Hanya gambar NPC jika di map yang benar
                int npcTileScreenX = npc.getCurrentTileX() * TILE_SIZE - camX; // Koordinat X tile NPC di layar
                int npcTileScreenY = npc.getCurrentTileY() * TILE_SIZE - camY + INFO_PANEL_HEIGHT; // Koordinat Y tile NPC di layar
    
                // Culling: Periksa apakah area TILE_SIZE tempat NPC akan digambar ada di viewport
                if (npcTileScreenX + TILE_SIZE <= 0 || npcTileScreenX >= getWidth() ||
                    npcTileScreenY + TILE_SIZE <= INFO_PANEL_HEIGHT || npcTileScreenY >= getHeight()) {
                    continue;
                }
    
                Image spriteFrame = npc.getCurrentSpriteFrame(); 
                if (spriteFrame != null) {
                    int originalSpriteWidth = npc.spriteWidth; 
                    int originalSpriteHeight = npc.spriteHeight; 
    
                    if (originalSpriteWidth > 0 && originalSpriteHeight > 0) {
                        
                        int drawHeight = TILE_SIZE; // Buat tinggi sprite sama dengan TILE_SIZE
                        int drawWidth = (int) ((double) TILE_SIZE / originalSpriteHeight * originalSpriteWidth); // Lebar disesuaikan rasio
    
                        // Posisi X dan Y untuk menggambar sprite agar tengah horizontal dan rata bawah di dalam tile
                        int drawX = npcTileScreenX + (TILE_SIZE - drawWidth) / 2; // Tengah horizontal
                        int drawY = npcTileScreenY + (TILE_SIZE - drawHeight);    // Rata bawah
    
                        g.drawImage(spriteFrame, drawX, drawY, drawWidth, drawHeight, this);
                    
                    } else {
                        // Fallback jika dimensi sprite asli tidak valid (misal 0), gambar kotak placeholder
                        g.setColor(Color.MAGENTA); // Warna placeholder yang mencolok jika dimensi asli bermasalah
                        g.fillRect(npcTileScreenX, npcTileScreenY, TILE_SIZE, TILE_SIZE); 
                        g.setColor(Color.BLACK);
                        g.drawString("DIM?", npcTileScreenX + 5, npcTileScreenY + 15);
                    }
                } else {
                    // Fallback jika spriteFrame null (gagal mengambil frame dari NPC)
                    g.setColor(Color.ORANGE);
                    g.fillRect(npcTileScreenX, npcTileScreenY, TILE_SIZE, TILE_SIZE); 
                    g.setColor(Color.BLACK);
                    g.setFont(new Font("Arial", Font.BOLD, 12));
                    String npcLabel = npc.getName().substring(0, Math.min(npc.getName().length(), 1));
                    FontMetrics fm = g.getFontMetrics();
                    int textWidth = fm.stringWidth(npcLabel);
                    g.drawString(npcLabel, npcTileScreenX + (TILE_SIZE - textWidth) / 2, npcTileScreenY + TILE_SIZE / 2 + fm.getAscent() / 2);
                }
            } // Kurung kurawal penutup untuk "if (currentMap == npcHomeMapInstance)"
        } // Kurung kurawal penutup untuk "for (NPC npc : allNPCs)"
    } // Kurung kurawal penutup untuk metode "private void drawNPCs(Graphics g)"

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
    
        // Koordinat tile pemain di layar
        int playerTileScreenX = player.getCurrentTileX() * TILE_SIZE - camX;
        int playerTileScreenY = player.getCurrentTileY() * TILE_SIZE - camY + INFO_PANEL_HEIGHT;
    
        Image spriteFrame = player.getCurrentSpriteFrame();
    
        if (spriteFrame != null) {
            // *** KOREKSI DI SINI ***
            int originalSpriteWidth = player.spriteWidthPlayer;  // Ambil dari Player (menggunakan nama field yang baru)
            int originalSpriteHeight = player.spriteHeightPlayer; // Ambil dari Player (menggunakan nama field yang baru)
    
            if (originalSpriteWidth > 0 && originalSpriteHeight > 0) {
                int drawHeight = TILE_SIZE; // Target tinggi
                int drawWidth = (int) ((double) TILE_SIZE / originalSpriteHeight * originalSpriteWidth); // Lebar sesuai rasio
    
                int drawX = playerTileScreenX + (TILE_SIZE - drawWidth) / 2;  // Tengah horizontal
                int drawY = playerTileScreenY + (TILE_SIZE - drawHeight);     // Rata bawah
    
                g.drawImage(spriteFrame, drawX, drawY, drawWidth, drawHeight, this);
            } else {
                // Fallback jika dimensi sprite pemain tidak valid
                g.setColor(Color.BLUE); // Warna placeholder berbeda untuk pemain
                g.fillRect(playerTileScreenX, playerTileScreenY, TILE_SIZE, TILE_SIZE);
                g.setColor(Color.BLACK);
                g.drawString("DIM?", playerTileScreenX + 5, playerTileScreenY + 15); // Pesan jika dimensi asli 0
            }
        } else {
            // Fallback jika spriteFrame pemain null
            g.setColor(Color.RED); 
            g.fillRect(playerTileScreenX, playerTileScreenY, TILE_SIZE, TILE_SIZE);
            g.setColor(Color.BLACK);
            g.drawRect(playerTileScreenX, playerTileScreenY, TILE_SIZE, TILE_SIZE);
            g.drawString("NO SPRITE", playerTileScreenX + 5, playerTileScreenY + 15); // Pesan jika frame null
        }
    
        // Menggambar nama item yang dipilih di atas pemain
        Item selectedItem = player.getSelectedItem();
        if (selectedItem != null) {
            String itemName = selectedItem.getName();
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics(); // Sebaiknya FontMetrics untuk font yang akan digunakan untuk teks
            // Jika ingin menggunakan font yang sama dengan DIALOGUE_TEXT_FONT misalnya:
            // g.setFont(DIALOGUE_TEXT_FONT); // Set font sebelum mengambil FontMetrics jika beda
            // fm = g.getFontMetrics();

            int stringWidth = fm.stringWidth(itemName);
            
            // Menghitung posisi Y untuk teks item.
            // Kita ingin teks berada di atas sprite yang telah digambar (yang bagian bawahnya rata dengan tile).
            // Jadi, kita ambil posisi Y atas dari tile, lalu kurangi sedikit.
            // Posisi Y atas dari tile di layar adalah playerTileScreenY.
            int textY = playerTileScreenY - 5; // 5 piksel di atas tile

            // Jika ingin teks tepat di atas kepala sprite yang mungkin lebih pendek dari TILE_SIZE:
            // int spriteActualDrawY = playerTileScreenY + (TILE_SIZE - (int) ((double) TILE_SIZE / player.spriteHeightPlayer * player.spriteHeightPlayer));
            // int textY = spriteActualDrawY - 5; 
            // Namun, player.spriteHeightPlayer bisa jadi tidak ada, maka gunakan player.spriteHeightPlayer
            // Perbaikan untuk textY berdasarkan asumsi drawHeight = TILE_SIZE untuk sprite:
            // Posisi atas sprite adalah playerTileScreenY + (TILE_SIZE - TILE_SIZE) = playerTileScreenY
            // Jika sprite rata bawah, posisi Y atasnya adalah playerTileScreenY + (TILE_SIZE - drawHeight)
            // dimana drawHeight = TILE_SIZE, jadi Y atas sprite = playerTileScreenY.
            // Jadi, playerTileScreenY - 5 sudah benar untuk menempatkannya di atas tile.

            g.drawString(itemName, playerTileScreenX + (TILE_SIZE - stringWidth) / 2, textY);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameController == null || farmModel == null) return;

        int keyCode = e.getKeyCode();

        // Priority for modal-like UI states
        if (farmModel.getCurrentGameState() == GameState.CHEAT_INPUT) {
            handleCheatTyping(e); // Pass full event for char typing
            repaint();
            return;
        }
        if (farmModel.getCurrentGameState() == GameState.WORLD_MAP_SELECTION) {
            handleWorldMapSelectionInput(keyCode); // Pass only keyCode for navigation
            repaint();
            return;
        }
        if (farmModel.getCurrentGameState() == GameState.STORE_UI) { // Check before MAIN_MENU or IN_GAME
            handleStoreInput(keyCode);
            repaint();
            return; 
        }
        if (farmModel.getCurrentGameState() == GameState.SHIPPING_BIN) { // Check before MAIN_MENU or IN_GAME
            handleShippingBinInput(keyCode);
            repaint(); 
            return;     
        }
        if (farmModel.getCurrentGameState() == GameState.NPC_DIALOGUE) { // Use isNpcDialogueActive or GameState
             if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_X || keyCode == KeyEvent.VK_E) {
                // This should ideally be handled by GameController to manage dialogue flow and state
                // For now, directly closing here as a simple mechanism
                isNpcDialogueActive = false; // This flag might become redundant if GameState is the primary driver
                farmModel.setCurrentGameState(GameState.IN_GAME); // Or previous state if more complex
                repaint();
            }
            return;
        }

        // Non-modal UI states or main game states
        if (farmModel.getCurrentGameState() == GameState.MAIN_MENU) {
            handleMainMenuInput(keyCode);
            repaint();
            return;
        }

        if (farmModel.getCurrentGameState() == GameState.END_OF_DAY_SUMMARY) {
            if (keyCode == KeyEvent.VK_ENTER) {
                farmModel.setCurrentGameState(GameState.IN_GAME);
                startGameTimer(); 
            }
            repaint(); 
            return;
        }

        // IN_GAME actions below - only if current state is IN_GAME
        if (farmModel.getCurrentGameState() == GameState.IN_GAME) {
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
                case KeyEvent.VK_E: 
                    actionTaken = tryGeneralAction();
                    break;
                case KeyEvent.VK_F: 
                    actionTaken = gameController.requestEatSelectedItem();
                    break;
                case KeyEvent.VK_T: 
                    openStoreDialog(); 
                    actionTaken = true; 
                    break;
                case KeyEvent.VK_B: 
                    actionTaken = tryOpenShippingBinDialog();
                    break;
                case KeyEvent.VK_C:
                    farmModel.setCurrentGameState(GameState.CHEAT_INPUT);
                    cheatInputString = ""; 
                    setGeneralGameMessage("Cheat mode activated. Enter code.", false);
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
                case KeyEvent.VK_X:
                    gameController.handleChatRequest();
                    actionTaken = true;
                    break;
                case KeyEvent.VK_G:
                    System.out.println("G key pressed - Attempting Gift");
                    gameController.handleGiftRequest();
                    actionTaken = true;
                    break;
                case KeyEvent.VK_L:
                    gameController.requestNormalSleep();
                    actionTaken = true;
                    break;
                case KeyEvent.VK_P:
                    if (gameController != null) {
                        gameController.handleProposeRequest();
                        actionTaken = true;
                    }
                    break;
                case KeyEvent.VK_M:
                    if(gameController != null){
                        gameController.handleMarryRequest();
                        actionTaken = true;
                    }
                    break;
                case KeyEvent.VK_K:
                    if(gameController != null){
                        gameController.handleCookRequest();
                        actionTaken = true;
                    }
                    break;
                case KeyEvent.VK_V:
                    System.out.println("V key pressed - Attempting to Watch TV");
                    gameController.requestWatchTV();
                    actionTaken = true;
                    break;
                case KeyEvent.VK_I:
                    System.out.println("I key pressed - Viewing Player Info");
                    gameController.requestViewPlayerInfo();
                    actionTaken = true;
                    break;
                case KeyEvent.VK_O:
                    System.out.println("O key pressed - Viewing Statistics");
                    gameController.requestShowStatistics();
                    actionTaken = true;
                    break;
                // Removed VK_H case for showWorldMapSelectionDialog()
            }

            if (actionTaken) {
                repaint();
            }
        }
    }

    private void handleCheatTyping(KeyEvent e) { // Changed parameter to KeyEvent e
        if (farmModel.getCurrentGameState() != GameState.CHEAT_INPUT) return;
        int keyCode = e.getKeyCode(); // Get keyCode from e

        if (keyCode == KeyEvent.VK_ENTER) {
            if (!cheatInputString.trim().isEmpty()) {
                processCheatCode(cheatInputString.trim());
        } else {
                setGeneralGameMessage("No cheat code entered.", true);
            }
            farmModel.setCurrentGameState(GameState.IN_GAME); // Return to game
            cheatInputString = ""; // Clear for next time
        } else if (keyCode == KeyEvent.VK_ESCAPE) {
            farmModel.setCurrentGameState(GameState.IN_GAME);
            setGeneralGameMessage("Cheat input cancelled.", false);
            cheatInputString = "";
        } else if (keyCode == KeyEvent.VK_BACK_SPACE) {
            if (!cheatInputString.isEmpty()) {
                cheatInputString = cheatInputString.substring(0, cheatInputString.length() - 1);
            }
        } else {
            char typedChar = e.getKeyChar(); // Use e.getKeyChar() directly
            
            if (Character.isLetterOrDigit(typedChar) || typedChar == ' ') {
                 if (cheatInputString.length() < 50) { // Limit length
                    cheatInputString += Character.toLowerCase(typedChar); // Store as lowercase
                }
            }
             // If using VK_ codes for letters/numbers (more verbose but explicit)
            // Example: if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) cheatInputString += Character.toLowerCase((char)keyCode);
            // else if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9) cheatInputString += (char)keyCode;
            // else if (keyCode == KeyEvent.VK_SPACE) cheatInputString += ' ';
        }
    }

    // Renamed and modified from handleCheatInput to processCheatCode
    private void processCheatCode(String cheatCode) {
        // String cheat = JOptionPane.showInputDialog(this, "Enter cheat code (type 'help' for list of cheats):");
        // if (cheat == null || cheat.trim().isEmpty()) {
        //     return;
        // }
        // String[] parts = cheat.trim().toLowerCase().split("\\s+");

        String[] parts = cheatCode.toLowerCase().split("\\s+"); // Already lowercase from input
        String command = parts[0];

        boolean success = false;
        String feedbackMessage = "";

        if (command.equals("help")) {
            showCheatsHelp(); // This still uses JOptionPane, will need to be converted later
            feedbackMessage = "Help dialog displayed."; // Placeholder feedback
            // Success isn't really applicable here in the same way
        } else if (command.equals("weather")) {
            if (parts.length > 1) {
                String weatherType = parts[1];
                Weather newWeather = null;
                if (weatherType.equals("sunny")) newWeather = Weather.SUNNY;
                else if (weatherType.equals("rainy")) newWeather = Weather.RAINY;

                if (newWeather != null) {
                    farmModel.getCurrentTime().setWeather(newWeather);
                    feedbackMessage = "Weather changed to " + newWeather.toString();
                    success = true;
                } else {
                    feedbackMessage = "Invalid weather type. Use 'sunny' or 'rainy'.";
                }
            } else {
                feedbackMessage = "Usage: weather [sunny|rainy]";
            }
        } else if (command.equals("season")) {
            if (parts.length > 1) {
                String seasonType = parts[1].toUpperCase();
                Season newSeason = null;
                try { newSeason = Season.valueOf(seasonType); } catch (IllegalArgumentException ex) {}
                if (newSeason != null && newSeason != Season.ANY) {
                    farmModel.getCurrentTime().setSeason(newSeason);
                    feedbackMessage = "Season changed to " + newSeason.toString();
                    success = true;
                } else {
                    feedbackMessage = "Invalid season. Use SPRING, SUMMER, FALL, or WINTER.";
                }
            } else {
                feedbackMessage = "Usage: season [SPRING|SUMMER|FALL|WINTER]";
            }
        } else if (command.equals("time")) {
            if (parts.length == 3) {
                try {
                    int hour = Integer.parseInt(parts[1]);
                    int minute = Integer.parseInt(parts[2]);
                    if (gameController.requestSetTime(hour, minute)) {
                        feedbackMessage = "Time changed to " + String.format("%02d:%02d", hour, minute);
                        success = true;
                    } else {
                        feedbackMessage = "Invalid time values. Hour (0-23), Minute (0-59).";
                    }
                } catch (NumberFormatException ex) {
                    feedbackMessage = "Invalid number format for time. Usage: time HH MM";
                }
            } else {
                feedbackMessage = "Usage: time HH MM (e.g., time 8 0 for 8:00 AM, time 22 30 for 10:30 PM)";
            }
        } else if (command.equals("fishdebug")) {
            // ... (fishdebug logic still uses JOptionPane, needs conversion later)
            showFishDebugDialog(); // Assuming this method encapsulates the JOptionPane for fishdebug
            feedbackMessage = "Fish debug info displayed.";
        } else if (command.equals("gold")) {
            if (parts.length > 1) {
                try {
                    int amount = Integer.parseInt(parts[1]);
                    farmModel.getPlayer().addGold(amount); // addGold handles positive/negative
                    feedbackMessage = (amount >= 0 ? "Added " : "Removed ") + Math.abs(amount) + " gold. New total: " + farmModel.getPlayer().getGold() + "G";
                    success = true;
                } catch (NumberFormatException ex) {
                    feedbackMessage = "Invalid gold amount.";
                }
            } else {
                 feedbackMessage = "Usage: gold [amount]";
            }
        } else {
            feedbackMessage = "Unknown cheat code: " + cheatCode;
        }

        setGeneralGameMessage(feedbackMessage, !success && !command.equals("help") && !command.equals("fishdebug"));
        if (success) repaint(); // Repaint if a game state affecting visual changed (weather, season, time, gold in HUD)
    }

    private void showFishDebugDialog() { // Extracted for clarity, still uses JOptionPane
        StringBuilder debugMessage = new StringBuilder("<html><body>");
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

            JEditorPane editorPane = new JEditorPane("text/html", debugMessage.toString());
            editorPane.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new Dimension(600, 400)); 
            JOptionPane.showMessageDialog(this, scrollPane, "Fish Debug Information", JOptionPane.INFORMATION_MESSAGE);
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
        if (farmModel == null || farmModel.getPlayer() == null || farmModel.getCurrentGameState() != GameState.IN_GAME) {
            return;
        }
        Player player = farmModel.getPlayer();
        int keyCode = e.getKeyCode();
    
        // Hanya set isMoving ke false jika tombol yang dilepas adalah tombol gerakan
        if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP ||
            keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN ||
            keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT ||
            keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
    
            // Cek apakah ada tombol gerakan lain yang MASIH ditekan.
            // Ini agak rumit tanpa melacak status semua tombol.
            // Pendekatan sederhana: jika salah satu dilepas, anggap berhenti sementara.
            // Pembaruan isMoving akan terjadi lagi jika tombol lain ditekan di keyPressed.
            player.setMoving(false);
            repaint(); // Penting untuk update ke frame diam
        }
    }

    /**
     * Menampilkan dialog informasi akhir hari (misalnya, karena pingsan atau tidur normal).
     * Kini menggunakan panel in-game.
     * @param eventMessage Pesan utama kejadian (misal, "Kamu pingsan!" atau "Kamu tidur nyenyak.").
     * @param income Pendapatan dari penjualan di hari sebelumnya.
     * @param newDayInfo Informasi tentang hari baru (Tanggal, Musim, Cuaca).
     */
    public void showEndOfDayMessage(String eventMessage, int income, String newDayInfo) {
        this.endOfDayEventMessage = eventMessage;
        this.endOfDayIncome = income;
        this.endOfDayNewDayInfo = newDayInfo;
        
        if (farmModel != null) {
            farmModel.setCurrentGameState(GameState.END_OF_DAY_SUMMARY);
            // Consider stopping game timer here if it's not already stopped by controller logic for EOD
            // stopGameTimer(); // Usually GameController handles stopping timer before calling this for sleep/passout
        } else {
            // Fallback or error handling if farmModel is null
            System.err.println("GamePanel: farmModel is null, cannot show EndOfDaySummaryUI properly.");
            // As a last resort, could show JOptionPane here, but indicates a deeper issue.
            // JOptionPane.showMessageDialog(this, eventMessage + "\nIncome: " + income + "\n" + newDayInfo, "Akhir Hari", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        repaint(); 
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
        String currentMapName = currentMap.getName();

        // Ensure worldMapDestinations is initialized if null, or clear if already exists
        if (this.worldMapDestinations == null) {
            this.worldMapDestinations = new ArrayList<>();
        } else {
            this.worldMapDestinations.clear();
        }

        for (com.spakborhills.model.Enum.LocationType locType : com.spakborhills.model.Enum.LocationType.values()) {
            boolean isCurrentMapType = locType.toString().equalsIgnoreCase(currentMapName);
            if (currentMap instanceof FarmMap && locType == com.spakborhills.model.Enum.LocationType.FARM) {
                 isCurrentMapType = true;
            }
            if (currentMap instanceof Store && locType == com.spakborhills.model.Enum.LocationType.STORE) {
                isCurrentMapType = true;
            }

            if (locType != com.spakborhills.model.Enum.LocationType.POND && !isCurrentMapType) {
                this.worldMapDestinations.add(locType.toString());
            }
        }

        if (this.worldMapDestinations.isEmpty()) {
            if (!(currentMap instanceof FarmMap) && !this.worldMapDestinations.contains(com.spakborhills.model.Enum.LocationType.FARM.toString())) {
                 boolean farmAlreadyExcludedAsCurrent = com.spakborhills.model.Enum.LocationType.FARM.toString().equalsIgnoreCase(currentMapName);
                 if (!farmAlreadyExcludedAsCurrent) {
                    this.worldMapDestinations.add(com.spakborhills.model.Enum.LocationType.FARM.toString());
                 }
            }
            if (this.worldMapDestinations.isEmpty()) {
                 setGeneralGameMessage("No other locations available to visit from here.", false);
                 return;
            }
        }

        // Instead of JOptionPane, set the game state to show the new UI
        if (!this.worldMapDestinations.isEmpty()) {
            this.currentWorldMapSelectionIndex = 0;
            farmModel.setCurrentGameState(GameState.WORLD_MAP_SELECTION);
            repaint();
        } else {
             // This case should ideally be caught by the isEmpty check above and show general message
            setGeneralGameMessage("No destinations loaded for map selection.", true);
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
    public void showNPCDialogue(NPC npc, String dialogue) { // Parameter diubah
        if (npc == null) {
            System.err.println("GamePanel.showNPCDialogue: Objek NPC null.");
            this.isNpcDialogueActive = false; // Jangan tampilkan dialog jika NPC null
            this.currentInteractingNPC = null;
            repaint();
            return;
        }

        this.currentInteractingNPC = npc; // Simpan objek NPC yang aktif
        this.currentNpcName = npc.getName(); // Ambil nama dari objek NPC
        this.currentNpcDialogue = dialogue;
        this.isNpcDialogueActive = true;

        // ... (sisa kode untuk update dimensi dialog box, sama seperti sebelumnya)
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        if (panelWidth == 0 || panelHeight == 0) { 
            panelWidth = getPreferredSize().width;
            panelHeight = getPreferredSize().height;
        }

        int dialogueBoxWidth = panelWidth * 3 / 4;
        int dialogueBoxHeight = panelHeight / 3;
        int dialogueBoxX = (panelWidth - dialogueBoxWidth) / 2;
        int dialogueBoxY = panelHeight - dialogueBoxHeight - 20; 
        
        npcDialogueBox = new Rectangle(dialogueBoxX, dialogueBoxY, dialogueBoxWidth, dialogueBoxHeight);
        
        System.out.println("GamePanel: Activating NPC Dialogue - Name: " + this.currentNpcName + ", Dialogue: " + this.currentNpcDialogue); //
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
    // public void stopGameTimer() {
    //     if (gameTimer != null && gameTimer.isRunning()) {
    //         gameTimer.stop();
    //         System.out.println("Game Timer stopped.");
    //     }
    // }

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

    @Deprecated
    // private void drawNpcDialogue(Graphics g) {
    //     if (!isNpcDialogueActive) {
    //         return;
    //     }

    //     Graphics2D g2d = (Graphics2D) g.create(); // Create a copy to not affect other drawings

    //     // 1. Draw Dialogue Box Background
    //     g2d.setColor(new Color(0, 0, 0, 200)); // Semi-transparent black
    //     g2d.fill(npcDialogueBox);
    //     g2d.setColor(Color.WHITE);
    //     g2d.draw(npcDialogueBox);

    //     // 2. Draw NPC Portrait (Placeholder)
    //     int portraitX = npcDialogueBox.x + DIALOGUE_PADDING;
    //     int portraitY = npcDialogueBox.y + DIALOGUE_PADDING;
    //     g2d.drawImage(npcPortraitPlaceholder, portraitX, portraitY, PORTRAIT_SIZE, PORTRAIT_SIZE, this);
    //     g2d.setColor(Color.GRAY);
    //     g2d.drawRect(portraitX,portraitY, PORTRAIT_SIZE, PORTRAIT_SIZE);


    //     // 3. Draw NPC Name
    //     g2d.setFont(DIALOGUE_NAME_FONT);
    //     FontMetrics nameFm = g2d.getFontMetrics();
    //     int nameX = portraitX + PORTRAIT_SIZE + DIALOGUE_PADDING;
    //     int nameY = npcDialogueBox.y + DIALOGUE_PADDING + nameFm.getAscent();
    //     g2d.setColor(Color.YELLOW); // Or any color for the name
    //     g2d.drawString(currentNpcName + " says:", nameX, nameY);

    //     // 4. Draw Dialogue Text (with basic word wrapping)
    //     g2d.setFont(DIALOGUE_TEXT_FONT);
    //     g2d.setColor(Color.WHITE);
    //     FontMetrics textFm = g2d.getFontMetrics();
    //     int textBlockStartX = npcDialogueBox.x + DIALOGUE_PADDING + PORTRAIT_SIZE + DIALOGUE_PADDING;
    //     int availableTextWidth = (npcDialogueBox.x + npcDialogueBox.width - DIALOGUE_PADDING) - textBlockStartX;
        
    //     List<String> lines = new ArrayList<>();
    //     String[] words = currentNpcDialogue.split(" ");
    //     StringBuilder currentLine = new StringBuilder();

    //     for (String word : words) {
    //         if (textFm.stringWidth(currentLine.toString() + word) < availableTextWidth) {
    //             currentLine.append(word).append(" ");
    //         } else {
    //             lines.add(currentLine.toString().trim());
    //             currentLine = new StringBuilder(word + " ");
    //         }
    //     }
    //     lines.add(currentLine.toString().trim()); // Add the last line

    //     int lineY = nameY + DIALOGUE_PADDING;
    //     for (String line : lines) {
    //         if (lineY + textFm.getHeight() > npcDialogueBox.y + npcDialogueBox.height - DIALOGUE_PADDING) { // Check bounds
    //             g2d.drawString("...", textBlockStartX, lineY); // Indicate more text if it overflows
    //             break;
    //         }
    //         g2d.drawString(line, textBlockStartX, lineY);
    //         lineY += textFm.getHeight();
    //     }


    //     // 5. Draw "Press Enter to continue" prompt
    //     g2d.setFont(DIALOGUE_TEXT_FONT.deriveFont(Font.ITALIC));
    //     String continuePrompt = "Press ENTER to continue...";
    //     int promptWidth = textFm.stringWidth(continuePrompt); // Use textFm from DIALOGUE_TEXT_FONT
    //     int promptX = npcDialogueBox.x + npcDialogueBox.width - promptWidth - DIALOGUE_PADDING;
    //     int promptY = npcDialogueBox.y + npcDialogueBox.height - DIALOGUE_PADDING;
    //     g2d.setColor(Color.LIGHT_GRAY);
    //     g2d.drawString(continuePrompt, promptX, promptY);

    //     g2d.dispose();
    // }

    private void drawNpcDialogue(Graphics g) {
        if (!isNpcDialogueActive) { 
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setColor(new Color(0, 0, 0, 200)); 
        g2d.fill(npcDialogueBox);
        g2d.setColor(Color.WHITE);
        g2d.draw(npcDialogueBox);

        Image npcPortraitToDraw = null;
        int actualPortraitWidthDrawn = PORTRAIT_SIZE; 
        int actualPortraitHeightDrawn = PORTRAIT_SIZE;
        String npcNameToDisplay = (this.currentNpcName != null) ? this.currentNpcName : "???"; // Fallback name

        if (this.currentInteractingNPC != null) { 
            npcPortraitToDraw = this.currentInteractingNPC.getDefaultPortrait();
            npcNameToDisplay = this.currentInteractingNPC.getName(); // Gunakan nama dari objek NPC
            if (npcPortraitToDraw != null) { 
                actualPortraitWidthDrawn = this.currentInteractingNPC.portraitWidth;
                actualPortraitHeightDrawn = this.currentInteractingNPC.portraitHeight;
            }
        }
        
        int portraitX = npcDialogueBox.x + DIALOGUE_PADDING;
        int portraitY = npcDialogueBox.y + DIALOGUE_PADDING;

        if (npcPortraitToDraw != null) {
            g2d.drawImage(npcPortraitToDraw, portraitX, portraitY,
                          actualPortraitWidthDrawn,
                          actualPortraitHeightDrawn,
                          this);
        } else {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(portraitX, portraitY, PORTRAIT_SIZE, PORTRAIT_SIZE);
            g2d.setColor(Color.BLACK);
            String initial = (npcNameToDisplay != null && !npcNameToDisplay.isEmpty()) 
                             ? npcNameToDisplay.substring(0,1) 
                             : "?"; 
            g2d.drawString(initial, portraitX + PORTRAIT_SIZE/2 - 5, portraitY + PORTRAIT_SIZE/2 + 5);
        }
        g2d.setColor(Color.GRAY); 
        g2d.drawRect(portraitX, portraitY, actualPortraitWidthDrawn, actualPortraitHeightDrawn);

        g2d.setFont(DIALOGUE_NAME_FONT);
        FontMetrics nameFm = g2d.getFontMetrics();
        int nameX = portraitX + actualPortraitWidthDrawn + DIALOGUE_PADDING;
        int nameY = npcDialogueBox.y + DIALOGUE_PADDING + nameFm.getAscent();
        g2d.setColor(Color.YELLOW); 
        g2d.drawString(npcNameToDisplay + " says:", nameX, nameY); // Gunakan npcNameToDisplay

        // ... (sisa kode untuk menggambar teks dialog dan prompt, sama seperti sebelumnya)
        g2d.setFont(DIALOGUE_TEXT_FONT);
        g2d.setColor(Color.WHITE);
        FontMetrics textFm = g2d.getFontMetrics();
        int textBlockStartX = nameX;
        int availableTextWidth = (npcDialogueBox.x + npcDialogueBox.width - DIALOGUE_PADDING) - textBlockStartX;
        
        List<String> lines = new ArrayList<>();
        String dialogueText = (currentNpcDialogue != null) ? currentNpcDialogue : " ";
        String[] words = dialogueText.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (textFm.stringWidth(currentLine.toString() + word) < availableTextWidth) {
                currentLine.append(word).append(" ");
            } else {
                lines.add(currentLine.toString().trim());
                currentLine = new StringBuilder(word + " ");
            }
        }
        if (currentLine.length() > 0) {
             lines.add(currentLine.toString().trim()); 
        }

        int lineY = nameY + DIALOGUE_PADDING;
        for (String line : lines) {
            if (lineY + textFm.getHeight() > npcDialogueBox.y + npcDialogueBox.height - DIALOGUE_PADDING - textFm.getHeight()) { 
                g2d.drawString("...", textBlockStartX, lineY); 
                break;
            }
            g2d.drawString(line, textBlockStartX, lineY);
            lineY += textFm.getHeight();
        }

        g2d.setFont(DIALOGUE_TEXT_FONT.deriveFont(Font.ITALIC));
        String continuePrompt = "Press ENTER to continue...";
        int promptWidth = textFm.stringWidth(continuePrompt); 
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

    private void handleShippingBinInput(int keyCode) {
        if (farmModel.getCurrentGameState() != GameState.SHIPPING_BIN) return;

        if (shippingBinInputMode.equals("inputting_quantity")) {
            if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9) {
                if (shippingBinQuantityInputString.length() < 3) { // Max 3 digits for quantity
                    shippingBinQuantityInputString += (char) keyCode;
                }
            } else if (keyCode == KeyEvent.VK_BACK_SPACE) {
                if (!shippingBinQuantityInputString.isEmpty()) {
                    shippingBinQuantityInputString = shippingBinQuantityInputString.substring(0, shippingBinQuantityInputString.length() - 1);
                }
            } else if (keyCode == KeyEvent.VK_ENTER) {
                if (!shippingBinQuantityInputString.isEmpty() && currentShippingBinItemForQuantity != null) {
                    try {
                        int quantity = Integer.parseInt(shippingBinQuantityInputString);
                        if (quantity > 0) {
                            gameController.requestAddItemToShippingBin(currentShippingBinItemForQuantity, quantity);
                            // Feedback will be set by controller or this method after controller call
                        }
                    } catch (NumberFormatException nfe) {
                        setShippingBinFeedback("Invalid quantity.", true);
                    }
                }
                shippingBinInputMode = "selecting_player_item"; // Go back to item selection
                shippingBinQuantityInputString = "";
                currentShippingBinItemForQuantity = null;
            } else if (keyCode == KeyEvent.VK_ESCAPE) {
                shippingBinInputMode = "selecting_player_item";
                shippingBinQuantityInputString = "";
                currentShippingBinItemForQuantity = null;
                setShippingBinFeedback("Quantity input cancelled.", false);
            }
        } else { // "selecting_player_item"
            switch (keyCode) {
                case KeyEvent.VK_UP:
                    if (currentPlayerItemSelectionIndex > 0) {
                        currentPlayerItemSelectionIndex--;
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (playerSellableItems != null && currentPlayerItemSelectionIndex < playerSellableItems.size() - 1) {
                        currentPlayerItemSelectionIndex++;
                    }
                    break;
                case KeyEvent.VK_ENTER:
                    if (playerSellableItems != null && !playerSellableItems.isEmpty() && currentPlayerItemSelectionIndex < playerSellableItems.size()) {
                        currentShippingBinItemForQuantity = playerSellableItems.get(currentPlayerItemSelectionIndex);
                        shippingBinInputMode = "inputting_quantity";
                        shippingBinQuantityInputString = "";
                        setShippingBinFeedback("Enter quantity for " + currentShippingBinItemForQuantity.getName(), false);
                    }
                    break;
                case KeyEvent.VK_ESCAPE:
                    gameController.requestCloseShippingBin();
                    break;
                 // Optional: Add keys to switch focus between player inventory and bin items if managing bin items is implemented
            }
        }
        repaint();
    }

    private void drawShippingBinUI(Graphics2D g2d) {
        if (farmModel.getCurrentGameState() != GameState.SHIPPING_BIN) return;

        // Panel Background
        g2d.setColor(SHIPPING_BIN_BG_COLOR);
        g2d.fillRect(shippingBinPanelRect.x, shippingBinPanelRect.y, shippingBinPanelRect.width, shippingBinPanelRect.height);
        g2d.setColor(SHIPPING_BIN_TEXT_COLOR);
        g2d.drawRect(shippingBinPanelRect.x, shippingBinPanelRect.y, shippingBinPanelRect.width, shippingBinPanelRect.height);

        // Title
        g2d.setFont(DIALOG_FONT); // Re-use dialog font for title
        String title = "Shipping Bin";
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(title);
        g2d.drawString(title, shippingBinPanelRect.x + (shippingBinPanelRect.width - titleWidth) / 2, shippingBinPanelRect.y + 30);

        // Close Button (simple text for now)
        g2d.setFont(SHIPPING_BIN_FONT);
        g2d.drawString("[Esc] Close", shippingBinCloseButtonRect.x + 5, shippingBinCloseButtonRect.y + 20);

        // Player Inventory List (Left Side)
        g2d.setFont(SHIPPING_BIN_FONT);
        g2d.drawString("Your Items:", playerItemsListRect.x, playerItemsListRect.y - 5);
        g2d.drawRect(playerItemsListRect.x, playerItemsListRect.y, playerItemsListRect.width, playerItemsListRect.height);

        g2d.setFont(SHIPPING_BIN_ITEM_FONT);
        if (playerSellableItems != null && !playerSellableItems.isEmpty()) {
            for (int i = 0; i < playerSellableItems.size(); i++) {
                Item item = playerSellableItems.get(i);
                String itemName = item.getName();
                int quantity = farmModel.getPlayer().getInventory().getItemCount(item); // Corrected to getItemCount(item)
                String displayText = String.format("%-15s x%d", itemName, quantity);
                if (i == currentPlayerItemSelectionIndex) {
                    g2d.setColor(SHIPPING_BIN_HIGHLIGHT_COLOR);
                    g2d.fillRect(playerItemsListRect.x + 1, playerItemsListRect.y + 1 + (i * 20), playerItemsListRect.width - 2, 20);
                    g2d.setColor(Color.BLACK); // Text color on highlight
                } else {
                    g2d.setColor(SHIPPING_BIN_TEXT_COLOR);
                }
                g2d.drawString(displayText, playerItemsListRect.x + 5, playerItemsListRect.y + 15 + (i * 20));
            }
        } else {
            g2d.setColor(SHIPPING_BIN_TEXT_COLOR);
            g2d.drawString("No sellable items.", playerItemsListRect.x + 5, playerItemsListRect.y + 20);
        }

        // Items in Bin List (Right Side)
        g2d.setColor(SHIPPING_BIN_TEXT_COLOR); // Reset color
        g2d.setFont(SHIPPING_BIN_FONT);
        // int slotsUsed = (itemsInBinSession != null) ? itemsInBinSession.size() : 0; // OLD BUGGY WAY
        int slotsUsed = 0;
        if (farmModel != null && farmModel.getShippingBin() != null) {
            slotsUsed = farmModel.getShippingBin().getItems().size(); // CORRECT WAY
        }
        g2d.drawString(String.format("In Bin (Slots: %d/%d):", slotsUsed, ShippingBin.MAX_UNIQUE_SLOTS), binItemsListRect.x, binItemsListRect.y - 5);
        g2d.drawRect(binItemsListRect.x, binItemsListRect.y, binItemsListRect.width, binItemsListRect.height);

        g2d.setFont(SHIPPING_BIN_ITEM_FONT);
        if (farmModel.getShippingBin() != null && !farmModel.getShippingBin().getItems().isEmpty()) { // Check farmModel's bin
            int i = 0;
            for (Map.Entry<Item, Integer> entry : farmModel.getShippingBin().getItems().entrySet()) {
                Item item = entry.getKey();
                int quantityInBin = entry.getValue();
                String displayText = String.format("%-15s x%d", item.getName(), quantityInBin);
                g2d.setColor(SHIPPING_BIN_TEXT_COLOR);
                // Highlighting for items in bin is not implemented here, could be added if needed
                g2d.drawString(displayText, binItemsListRect.x + 5, binItemsListRect.y + 15 + (i * 20));
                i++;
            }
        } else {
            g2d.setColor(SHIPPING_BIN_TEXT_COLOR);
            g2d.drawString("Bin is empty.", binItemsListRect.x + 5, binItemsListRect.y + 20);
        }
        
        // Quantity Input Mode
        if (shippingBinInputMode.equals("inputting_quantity") && currentShippingBinItemForQuantity != null) {
            g2d.setColor(SHIPPING_BIN_TEXT_COLOR);
            g2d.setFont(SHIPPING_BIN_FONT);
            String prompt = "Qty for " + currentShippingBinItemForQuantity.getName() + ": " + shippingBinQuantityInputString + "_";
            g2d.fillRect(shippingBinQuantityRect.x, shippingBinQuantityRect.y, shippingBinQuantityRect.width, shippingBinQuantityRect.height); // Background for input
            g2d.setColor(Color.BLACK);
            g2d.drawString(prompt, shippingBinQuantityRect.x + 5, shippingBinQuantityRect.y + 20);
        }

        // Feedback Message
        if (shippingBinFeedbackMessage != null && !shippingBinFeedbackMessage.isEmpty()) {
            g2d.setFont(SHIPPING_BIN_FONT);
            g2d.setColor(shippingBinFeedbackColor);
            FontMetrics fmFeedback = g2d.getFontMetrics();
            int msgWidth = fmFeedback.stringWidth(shippingBinFeedbackMessage);
            g2d.drawString(shippingBinFeedbackMessage, shippingBinPanelRect.x + (shippingBinPanelRect.width - msgWidth) / 2, shippingBinPanelRect.y + shippingBinPanelRect.height - 10);
        }
    }

    private void setShippingBinFeedback(String message, boolean isError) {
        shippingBinFeedbackMessage = message;
        shippingBinFeedbackColor = isError ? Color.RED : SHIPPING_BIN_TEXT_COLOR;
        shippingBinFeedbackTimer.restart();
        repaint();
    }

    // Method to be called by GameController to open the UI
    public void openShippingBinUI() {
        if (farmModel.getShippingBin() == null) {
            System.err.println("GamePanel: ShippingBin model is null. Cannot open UI.");
            return;
        }
        playerSellableItems = getSellableItemsFromInventory();
        // itemsInBinSession is not strictly needed if we draw directly from shippingBin.getItems()
        // For now, let's keep it simple and draw directly in drawShippingBinUI.
        // If itemsInBinSession was for modification, then it's different.
        // The current draw logic iterates farmModel.getShippingBin().getItems().entrySet()
        // So, itemsInBinSession variable in GamePanel might be redundant for display if always a fresh copy.

        currentPlayerItemSelectionIndex = 0;
        shippingBinInputMode = "selecting_player_item";
        shippingBinQuantityInputString = "";
        currentShippingBinItemForQuantity = null;
        setShippingBinFeedback("Select an item to add to the bin. [Enter] to set quantity.", false);
        // farmModel.setCurrentGameState(GameState.SHIPPING_BIN); // Controller should set this
        repaint();
    }

    // Method to be called by GameController to close the UI
    public void closeShippingBinUI() {
        // farmModel.setCurrentGameState(GameState.IN_GAME); // Controller should set this
        shippingBinFeedbackMessage = ""; // Clear any lingering messages
        // Player items and bin items will be naturally cleared or reloaded on next open
        repaint();
    }

    // Helper to get sellable items from player's inventory
    private List<Item> getSellableItemsFromInventory() {
        List<Item> sellable = new ArrayList<>();
        if (farmModel.getPlayer() != null && farmModel.getPlayer().getInventory() != null) {
            for (Map.Entry<Item, Integer> entry : farmModel.getPlayer().getInventory().getItems().entrySet()) {
                Item item = entry.getKey();
                // Filter out non-sellable types like Equipment
                if (item instanceof Crop || item instanceof Fish || item instanceof Food || item instanceof MiscItem) {
                    if (entry.getValue() > 0) { // Only if player has some
                        // We add a representation of the item type, actual quantity is fetched during display
                        // Or, create new Item objects with full details if needed for other logic
                        sellable.add(item); 
                    }
                }
            }
        }
        // Sort alphabetically for easier navigation
        sellable.sort((i1, i2) -> i1.getName().compareToIgnoreCase(i2.getName()));
        return sellable;
    }

    // Call this when GameController confirms an item was added to bin
    public void itemAddedToBinSuccessfully(Item item, int quantityAdded) {
        // Refresh itemsInBinSession from the model - NO LONGER NEEDED if drawing direct
        // itemsInBinSession = new ArrayList<>(farmModel.getShippingBin().getItems().entrySet());
        // Refresh player's sellable items as quantity might have changed
        playerSellableItems = getSellableItemsFromInventory();
        
        // Reset selection and quantity input
        shippingBinInputMode = "selecting_player_item";
        shippingBinQuantityInputString = "";
        currentShippingBinItemForQuantity = null;
        
        setShippingBinFeedback(quantityAdded + " " + item.getName() + " added to bin.", false);
        
        // Adjust selection if the list size changed and selection is now out of bounds
        if (currentPlayerItemSelectionIndex >= playerSellableItems.size() && !playerSellableItems.isEmpty()) {
            currentPlayerItemSelectionIndex = playerSellableItems.size() - 1;
        } else if (playerSellableItems.isEmpty()){
            currentPlayerItemSelectionIndex = 0;
        }
        repaint();
    }

    public void shippingBinActionFailed(String errorMessage) {
        setShippingBinFeedback(errorMessage, true);
        // Potentially revert quantity input mode if error occurred there
        shippingBinInputMode = "selecting_player_item";
        shippingBinQuantityInputString = "";
        currentShippingBinItemForQuantity = null;
        repaint();
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
                actionProcessed = gameController.requestFish();
            }
        } else if (currentItem instanceof Seed) {
            actionProcessed = gameController.requestPlantSeedAtPlayerPosition();
        } else if (currentItem instanceof Food) {
            // Eating is handled by 'F' key now, 'E' won't trigger eating for Food.
            // System.out.println("Food item selected: " + currentItem.getName() + ". Press 'F' to eat.");
        }

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
            setGeneralGameMessage("Store system not ready.", true);
            return;
        }

        Player player = farmModel.getPlayer();
        MapArea currentMap = player.getCurrentMap();

        boolean isInStoreLocation = false;
        if (currentMap != null) {
            if (currentMap instanceof com.spakborhills.model.Store) { // Assuming Store is a MapArea type
                isInStoreLocation = true;
            } 
        }

        if (!isInStoreLocation) {
            setGeneralGameMessage("You must be in the Store to access it.", true);
            return;
        }

        this.storeItemsForDisplay = gameController.getStoreItemsForDisplay();
        if (this.storeItemsForDisplay == null || this.storeItemsForDisplay.isEmpty()) {
            setGeneralGameMessage("The store is currently empty.", false);
            return;
        }
        this.currentStoreItemSelectionIndex = 0;
        this.currentBuyQuantity = 1;
        this.storeInputMode = "selecting_item";
        // farmModel.setCurrentGameState(GameState.STORE_UI); // This should be done by controller or here
        this.isStoreUiActive = true; // This flag should be linked to GameState.STORE_UI
        farmModel.setCurrentGameState(GameState.STORE_UI); // Explicitly set game state
        repaint();
    }

    private boolean tryOpenShippingBinDialog() {
        Player player = farmModel.getPlayer();
        if (!(player.getCurrentMap() instanceof FarmMap)) {
            return false; // Can only use shipping bin from FarmMap
        }

        FarmMap farmMap = (FarmMap) player.getCurrentMap();
        int playerX = player.getCurrentTileX();
        int playerY = player.getCurrentTileY();

        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}}; // N, S, W, E
        boolean adjacentToBin = false;
        for (int[] dir : directions) {
            Tile adjacentTile = farmMap.getTile(playerX + dir[0], playerY + dir[1]);
            if (adjacentTile != null && adjacentTile.getAssociatedObject() instanceof ShippingBinObject) {
                adjacentToBin = true;
                break;
            }
        }

        if (adjacentToBin) {
            if (farmModel.getShippingBin().canSellToday()) {
                gameController.requestOpenShippingBin(); // This will set GameState to SHIPPING_BIN
                return true;
            } else {
                setGeneralGameMessage("You have already used the shipping bin today.", false);
                return false; 
            }
        } else {
            // Optional: setGeneralGameMessage("You need to be next to the Shipping Bin to use it.", true);
            return false;
        }
    }

    private void drawEndOfDaySummaryUI(Graphics2D g2d) {
        if (farmModel.getCurrentGameState() != GameState.END_OF_DAY_SUMMARY) {
            return;
        }

        // Panel Background
        g2d.setColor(END_OF_DAY_BG_COLOR);
        g2d.fill(endOfDayPanelRect);
        g2d.setColor(END_OF_DAY_TEXT_COLOR.brighter());
        g2d.draw(endOfDayPanelRect);

        int currentY = endOfDayPanelRect.y + 40;
        int textX = endOfDayPanelRect.x + 30;
        int textWidth = endOfDayPanelRect.width - 60;

        // Title
        g2d.setFont(END_OF_DAY_FONT_TITLE);
        g2d.setColor(END_OF_DAY_TEXT_COLOR);
        String title = "End of Day";
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(title);
        g2d.drawString(title, endOfDayPanelRect.x + (endOfDayPanelRect.width - titleWidth) / 2, currentY);
        currentY += fmTitle.getHeight() + 20;

        // Event Message
        g2d.setFont(END_OF_DAY_FONT_TEXT);
        List<String> eventLines = getWrappedText(endOfDayEventMessage, textWidth, g2d.getFontMetrics());
        for (String line : eventLines) {
            g2d.drawString(line, textX, currentY);
            currentY += g2d.getFontMetrics().getHeight();
        }
        currentY += 10; // Spacing

        // Income
        String incomeText;
        if (endOfDayIncome > 0) {
            incomeText = "You earned " + endOfDayIncome + " G from sales.";
        } else {
            incomeText = "No income from sales today.";
        }
        List<String> incomeLines = getWrappedText(incomeText, textWidth, g2d.getFontMetrics());
        for (String line : incomeLines) {
            g2d.drawString(line, textX, currentY);
            currentY += g2d.getFontMetrics().getHeight();
        }
        currentY += 20; // More spacing

        // New Day Info
        List<String> newDayLines = getWrappedText(endOfDayNewDayInfo, textWidth, g2d.getFontMetrics());
        for (String line : newDayLines) {
            g2d.drawString(line, textX, currentY);
            currentY += g2d.getFontMetrics().getHeight();
        }
        currentY += 30; // Spacing before prompt

        // Prompt to continue
        g2d.setFont(END_OF_DAY_FONT_TEXT.deriveFont(Font.ITALIC));
        String continuePrompt = "Press Enter to start the new day...";
        FontMetrics fmPrompt = g2d.getFontMetrics();
        int promptWidth = fmPrompt.stringWidth(continuePrompt);
        g2d.drawString(continuePrompt, endOfDayPanelRect.x + (endOfDayPanelRect.width - promptWidth) / 2, endOfDayPanelRect.y + endOfDayPanelRect.height - 30);
    }

    // Helper for basic word wrapping (can be more sophisticated)
    private List<String> getWrappedText(String text, int maxWidth, FontMetrics fm) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty() || fm == null) return lines;

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            if (fm.stringWidth(currentLine.toString() + word) < maxWidth) {
                currentLine.append(word).append(" ");
            } else {
                lines.add(currentLine.toString().trim());
                currentLine = new StringBuilder(word + " ");
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString().trim());
        }
        return lines;
    }

    private void drawWorldMapSelectionUI(Graphics2D g2d) {
        if (farmModel.getCurrentGameState() != GameState.WORLD_MAP_SELECTION || worldMapDestinations == null) {
            return;
        }

        // Panel Background
        g2d.setColor(WORLD_MAP_BG_COLOR);
        g2d.fill(worldMapPanelRect);
        g2d.setColor(WORLD_MAP_TEXT_COLOR.brighter());
        g2d.draw(worldMapPanelRect);

        int currentY = worldMapPanelRect.y + 40;
        int textX = worldMapPanelRect.x + 30;

        // Title
        g2d.setFont(WORLD_MAP_FONT_TITLE);
        g2d.setColor(WORLD_MAP_TEXT_COLOR);
        String title = "Pilih Tujuan";
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(title);
        g2d.drawString(title, worldMapPanelRect.x + (worldMapPanelRect.width - titleWidth) / 2, currentY);
        currentY += fmTitle.getHeight() + 25; // Spacing

        // Destination List
        g2d.setFont(WORLD_MAP_FONT_ITEM);
        FontMetrics fmItem = g2d.getFontMetrics();
        int itemLineHeight = fmItem.getHeight() + 10; // Spacing between items

        for (int i = 0; i < worldMapDestinations.size(); i++) {
            // Adjusted condition to allow more items before cutting off for instructions
            // Check if drawing the current item would overlap with the instruction text area
            int instructionTextY = worldMapPanelRect.y + worldMapPanelRect.height - 25 - fmItem.getDescent(); // Approximate top of instruction text
            if (currentY + fmItem.getAscent() > instructionTextY - itemLineHeight) { // If next line starts to overlap or gets too close
                g2d.drawString("...", textX, currentY);
                break;
            }
            String destName = worldMapDestinations.get(i);
            if (i == currentWorldMapSelectionIndex) {
                g2d.setColor(WORLD_MAP_HIGHLIGHT_COLOR);
                g2d.drawString("> " + destName, textX, currentY);
                g2d.setColor(WORLD_MAP_TEXT_COLOR);
            } else {
                g2d.drawString("  " + destName, textX, currentY);
            }
            currentY += itemLineHeight;
        }

        // Instructions
        g2d.setFont(WORLD_MAP_FONT_ITEM.deriveFont(Font.ITALIC, 18f));
        String instructions = "[Up/Down] Pilih  [Enter] Pergi  [Esc] Batal";
        FontMetrics fmInstructions = g2d.getFontMetrics();
        int instructionsWidth = fmInstructions.stringWidth(instructions);
        g2d.drawString(instructions, worldMapPanelRect.x + (worldMapPanelRect.width - instructionsWidth) / 2, worldMapPanelRect.y + worldMapPanelRect.height - 25);
    }

    private void handleWorldMapSelectionInput(int keyCode) {
        if (farmModel.getCurrentGameState() != GameState.WORLD_MAP_SELECTION || worldMapDestinations == null || worldMapDestinations.isEmpty()) {
            return;
        }

        switch (keyCode) {
            case KeyEvent.VK_UP:
                currentWorldMapSelectionIndex--;
                if (currentWorldMapSelectionIndex < 0) {
                    currentWorldMapSelectionIndex = worldMapDestinations.size() - 1;
                }
                break;
            case KeyEvent.VK_DOWN:
                currentWorldMapSelectionIndex++;
                if (currentWorldMapSelectionIndex >= worldMapDestinations.size()) {
                    currentWorldMapSelectionIndex = 0;
                }
                break;
            case KeyEvent.VK_ENTER:
                if (currentWorldMapSelectionIndex >= 0 && currentWorldMapSelectionIndex < worldMapDestinations.size()) {
                    String chosenDestination = worldMapDestinations.get(currentWorldMapSelectionIndex);
                    try {
                        com.spakborhills.model.Enum.LocationType destinationEnum =
                                com.spakborhills.model.Enum.LocationType.valueOf(chosenDestination.toUpperCase());
                        
                        // GameController will handle changing state back to IN_GAME after successful visit
                        gameController.requestVisit(destinationEnum); 
                        // UI will close automatically when state changes
                    } catch (IllegalArgumentException ex) {
                        System.err.println("GamePanel: Invalid destination string chosen from UI: " + chosenDestination + " Error: " + ex.getMessage());
                        setGeneralGameMessage("Error: Invalid location '" + chosenDestination + "' selected.", true);
                        // Optionally, revert to IN_GAME state if selection is fundamentally broken
                        // farmModel.setCurrentGameState(GameState.IN_GAME);
                    }
                }
                break;
            case KeyEvent.VK_ESCAPE:
                farmModel.setCurrentGameState(GameState.IN_GAME);
                // No specific message needed for cancel, just return to game
                break;
        }
        repaint(); // Repaint to reflect selection changes or UI closing
    }
}
