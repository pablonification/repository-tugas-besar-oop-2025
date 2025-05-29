package com.spakborhills.view;

import com.spakborhills.model.Farm; 
import com.spakborhills.model.Map.FarmMap;
import com.spakborhills.model.Map.Tile;
import com.spakborhills.model.Enum.TileType;
import com.spakborhills.model.Player; 
import com.spakborhills.model.Enum.Direction; 
import com.spakborhills.controller.GameController; 
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Item.Seed;
import com.spakborhills.model.Item.Equipment; 
import com.spakborhills.model.Item.Crop; 
import com.spakborhills.model.Item.Fish; 
import com.spakborhills.model.Item.Food; 
import com.spakborhills.model.Item.MiscItem; 
import com.spakborhills.model.Object.ShippingBinObject;
import com.spakborhills.model.Enum.Weather; 
import com.spakborhills.model.Enum.Season; 
import com.spakborhills.model.Map.MapArea;
import com.spakborhills.model.Store; 
import com.spakborhills.model.Util.GameTime; 
import com.spakborhills.model.Enum.LocationType; 
import com.spakborhills.model.Enum.FishRarity; 
import com.spakborhills.model.NPC.NPC; 
import com.spakborhills.model.Enum.GameState; 
import com.spakborhills.model.Util.ShippingBin; 
import com.spakborhills.model.Object.DeployedObject; 
import com.spakborhills.util.SaveLoadManager; 
import javax.imageio.ImageIO; 
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent; 
import java.awt.event.ActionListener; 
import java.awt.event.KeyEvent; 
import java.awt.event.KeyListener; 
import java.awt.image.BufferedImage; 
import java.io.IOException; 
import java.util.List;
import java.util.ArrayList; 
import java.util.Map; 
import java.io.File; 
import java.io.InputStream; 
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.text.SimpleDateFormat;
import javax.swing.Timer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;

public class GamePanel extends JPanel implements KeyListener { 
    private static final int VIEWPORT_WIDTH_IN_TILES = 20;
    private static final int VIEWPORT_HEIGHT_IN_TILES = 10;
    private Farm farmModel;
    private GameController gameController;
    private final int TILE_SIZE;
    private final int INFO_PANEL_HEIGHT;
    private final Font DIALOG_FONT;
    private final Font NPC_DIALOG_FONT;
    private final double scaleFactor;

    private javax.swing.Timer gameTimer;
    private boolean statisticsShown = false;

    // Main Menu
    private String[] menuOptions = {"New Game", "Load Game", "Help", "Credits", "Manage Saves", "Exit"};
    private int currentMenuSelection = 0;
    private static final Font MENU_FONT = new Font("Arial", Font.BOLD, 30);
    private static final Font MENU_ITEM_FONT = new Font("Arial", Font.PLAIN, 24);
    private static final Color MENU_BACKGROUND_COLOR = new Color(50, 50, 100);
    private static final Color MENU_TEXT_COLOR = Color.WHITE;
    private static final Color MENU_SELECTED_TEXT_COLOR = Color.YELLOW;

    // NPC Dialog
    private boolean isNpcDialogueActive = false;
    private String currentNpcName;
    private String currentNpcDialogue;
    private Rectangle npcDialogueBox;
    private Image npcPortraitPlaceholder;
    private static final int PORTRAIT_SIZE = 80;
    private static final int DIALOGUE_PADDING = 20;
    private final Font DIALOGUE_TEXT_FONT_SCALED; 
    private final Font DIALOGUE_NAME_FONT_SCALED;

    // Store UI
    private boolean isStoreUiActive = false;
    private List<Item> storeItemsForDisplay;
    private int currentStoreItemSelectionIndex = 0;
    private int currentBuyQuantity = 1;
    private String storeInputMode = "selecting_item";
    private Rectangle storePanelRect;
    private Rectangle storeItemListRect;
    private Rectangle storeQuantityRect;
    private Rectangle storeBuyButtonRect;
    private Rectangle storeCloseButtonRect;
    private final Font STORE_FONT_SCALED;
    private final Font STORE_ITEM_FONT_SCALED;
    private static final Color STORE_BG_COLOR = new Color(0, 0, 0, 200); 
    private static final Color STORE_TEXT_COLOR = Color.WHITE;
    private static final Color STORE_HIGHLIGHT_COLOR = Color.YELLOW;
    private String storeFeedbackMessage = "";
    private Color storeFeedbackColor = STORE_TEXT_COLOR; 
    private Timer storeFeedbackTimer;

    private NPC currentInteractingNPC;
    private javax.swing.Timer animationTimer;

    // Shipping Bin UI
    private List<Item> playerSellableItems;
    private int currentPlayerItemSelectionIndex = 0;
    private String shippingBinInputMode = "selecting_player_item";
    private String shippingBinQuantityInputString = "";
    private Item currentShippingBinItemForQuantity;
    private Rectangle shippingBinPanelRect;
    private Rectangle playerItemsListRect;
    private Rectangle binItemsListRect;
    private Rectangle shippingBinQuantityRect;
    private Rectangle shippingBinCloseButtonRect;
    private static final Font SHIPPING_BIN_FONT = new Font("Arial", Font.PLAIN, 18);
    private static final Font SHIPPING_BIN_ITEM_FONT = new Font("Monospaced", Font.PLAIN, 16);
    private static final Color SHIPPING_BIN_BG_COLOR = new Color(30, 30, 70, 220);
    private static final Color SHIPPING_BIN_TEXT_COLOR = Color.WHITE;
    private static final Color SHIPPING_BIN_HIGHLIGHT_COLOR = Color.CYAN;
    private String shippingBinFeedbackMessage = "";
    private Color shippingBinFeedbackColor = SHIPPING_BIN_TEXT_COLOR;
    private Timer shippingBinFeedbackTimer;

    private String generalGameMessage = "";
    private Color generalGameMessageColor = Color.WHITE;
    private Timer generalGameMessageTimer;
    private static final Font GENERAL_MESSAGE_FONT = new Font("Arial", Font.BOLD, 22);

    // Cheat Input UI
    private String cheatInputString = "";
    private Rectangle cheatInputPanelRect;
    private static final Font CHEAT_INPUT_FONT = new Font("Monospaced", Font.PLAIN, 20);
    private static final Color CHEAT_INPUT_BG_COLOR = new Color(20, 20, 20, 230); // Very dark semi-transparent
    private static final Color CHEAT_INPUT_TEXT_COLOR = Color.GREEN;

    // End of Day Summary UI
    private String endOfDayEventMessage = "";
    private int endOfDayIncome = 0;
    private String endOfDayNewDayInfo = "";
    private Rectangle endOfDayPanelRect;
    private static final Font END_OF_DAY_FONT_TITLE = new Font("Arial", Font.BOLD, 28);
    private static final Font END_OF_DAY_FONT_TEXT = new Font("Arial", Font.PLAIN, 20);
    private static final Color END_OF_DAY_BG_COLOR = new Color(50, 50, 70, 230); // Dark blueish-purple
    private static final Color END_OF_DAY_TEXT_COLOR = Color.WHITE;

    // World Map Selection
    private List<String> worldMapDestinations;
    private int currentWorldMapSelectionIndex = 0;
    private Rectangle worldMapPanelRect;
    private final Font WORLD_MAP_FONT_TITLE; 
    private final Font WORLD_MAP_FONT_ITEM;  
    private static final Color WORLD_MAP_BG_COLOR = new Color(60, 100, 60, 220);
    private static final Color WORLD_MAP_TEXT_COLOR = Color.WHITE;
    private static final Color WORLD_MAP_HIGHLIGHT_COLOR = Color.YELLOW;

    // Tile Images
    private BufferedImage tillableImage;
    private BufferedImage tilledImage;
    private BufferedImage plantedImage;
    private BufferedImage harvestableImage;
    private BufferedImage waterImage;
    private BufferedImage grassImage; 
    private BufferedImage obstacleImage; 
    private BufferedImage plantWateredImage;
    private BufferedImage shippingBinImage; 
    private BufferedImage houseTileImage;
    private BufferedImage portalImage; 
    private BufferedImage woodFloorImage;
    private BufferedImage stoneFloorImage;
    private BufferedImage carpetFloorImage;
    private BufferedImage luxuryFloorImage;
    private BufferedImage dirtFloorImage;
    private BufferedImage wallImage;
    private BufferedImage storeTileImage;

    // Inventory View UI
    private static final int INVENTORY_COLS = 8; 
    private static final int INVENTORY_ROWS = 4;
    private static final int INVENTORY_CELL_SIZE = 64;
    private static final int INVENTORY_PADDING = 10;
    private Rectangle inventoryPanelRect;
    private Rectangle inventoryGridRect;
    private int currentInventoryCol = 0;
    private int currentInventoryRow = 0;
    private static final Font INVENTORY_FONT = new Font("Arial", Font.PLAIN, 16);
    private static final Color INVENTORY_BG_COLOR = new Color(20, 20, 40, 230);
    private static final Color INVENTORY_CELL_COLOR = new Color(50, 50, 80, 200);
    private static final Color INVENTORY_TEXT_COLOR = Color.WHITE;
    private static final Color INVENTORY_HIGHLIGHT_COLOR = Color.ORANGE;

    // Player Info View UI
    private Rectangle playerInfoPanelRect;
    private static final Font PLAYER_INFO_FONT_TITLE = new Font("Arial", Font.BOLD, 28);
    private static final Font PLAYER_INFO_FONT_TEXT = new Font("Arial", Font.PLAIN, 20);
    private static final Color PLAYER_INFO_BG_COLOR = new Color(70, 70, 100, 230);
    private static final Color PLAYER_INFO_TEXT_COLOR = Color.WHITE;

    // Statistics View UI
    private Rectangle statisticsPanelRect;
    private int statsScrollOffset = 0;
    private static final int STATS_SCROLL_AMOUNT = 24;

    private GameFrame gameFrame;

    // Audio buat Main Menu
    private Clip menuMusicClip;
    private boolean isMenuMusicPlaying = false;

    private Clip inGameMusicClip;
    private boolean isInGameMusicPlaying = false;

    private long inGameMusicPosition = 0;
    private long menuMusicPosition = 0;

    // Pause Menu UI
    private String[] pauseMenuOptions = {"Resume", "Save Game", "Exit to Main Menu", "Exit Game"};
    private int currentPauseMenuSelection = 0;
    private Rectangle pauseMenuPanelRect;
    private static final Font PAUSE_MENU_FONT_TITLE = new Font("Arial", Font.BOLD, 28);
    private static final Font PAUSE_MENU_FONT_ITEM = new Font("Arial", Font.PLAIN, 22);
    private static final Color PAUSE_MENU_BG_COLOR = new Color(0, 0, 0, 180);
    private static final Color PAUSE_MENU_TEXT_COLOR = Color.WHITE;
    private static final Color PAUSE_MENU_HIGHLIGHT_COLOR = Color.YELLOW;

    private int storeScrollOffset = 0; // track scroll position

    public GamePanel(Farm farmModel, GameController gameController, GameFrame gameFrame, int dynamicTileSize, int dynamicInfoPanelHeight) {
        this.farmModel = farmModel;
        this.gameController = gameController;
        this.gameFrame = gameFrame; 

        this.TILE_SIZE = dynamicTileSize;
        this.INFO_PANEL_HEIGHT = dynamicInfoPanelHeight;
    
        this.scaleFactor = this.TILE_SIZE / 96.0;
    
        this.DIALOG_FONT = new Font("Arial", Font.PLAIN, Math.max(12, (int)(20 * this.scaleFactor)));
        this.NPC_DIALOG_FONT = new Font("Arial", Font.PLAIN, Math.max(10, (int)(16 * this.scaleFactor)));
        this.WORLD_MAP_FONT_TITLE = new Font("Arial", Font.BOLD, Math.max(14, (int)(28 * this.scaleFactor)));
        this.WORLD_MAP_FONT_ITEM = new Font("Arial", Font.PLAIN, Math.max(10, (int)(22 * this.scaleFactor)));

        this.DIALOGUE_NAME_FONT_SCALED = new Font("Arial", Font.BOLD, Math.max(12, (int)(20 * this.scaleFactor)));
        this.DIALOGUE_TEXT_FONT_SCALED = new Font("Arial", Font.PLAIN, Math.max(10, (int)(18 * this.scaleFactor)));
        this.STORE_FONT_SCALED = new Font("Arial", Font.PLAIN, Math.max(10, (int)(18 * this.scaleFactor)));
        this.STORE_ITEM_FONT_SCALED = new Font("Monospaced", Font.PLAIN, Math.max(9, (int)(16 * this.scaleFactor)));

        UIManager.put("OptionPane.messageFont", this.DIALOG_FONT);
        UIManager.put("OptionPane.buttonFont", this.DIALOG_FONT);
        UIManager.put("TextField.font", this.DIALOG_FONT);

        setPreferredSize(new Dimension(VIEWPORT_WIDTH_IN_TILES * this.TILE_SIZE,
                                    VIEWPORT_HEIGHT_IN_TILES * this.TILE_SIZE + this.INFO_PANEL_HEIGHT));
        setBackground(Color.GRAY);
        addKeyListener(this);
        setFocusable(true);

        gameTimer = new javax.swing.Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (farmModel != null && farmModel.getCurrentTime() != null && gameController != null) {
                    if (farmModel.getCurrentGameState() == GameState.IN_GAME) {
                        if (!statisticsShown && farmModel.checkEndConditions()) {
                            System.out.println("GAME PANEL: End game condition met! Requesting stats display.");
                            gameController.requestShowStatistics(); 
                            statisticsShown = true; 
                            return; 
                        }
    
                        if (gameTimer.isRunning()) { 
                            farmModel.getCurrentTime().advance(5); 
                            if (gameController != null) {
                                gameController.checkTimeBasedPassOut(); 
                            }
                        }
                    } else if (farmModel.getCurrentGameState() == GameState.MAIN_MENU) {
                        repaint(); 
                    }
                }
            }
        });
    
        // Timer untuk animasi (100ms = 10 FPS untuk animasi smooth)
        animationTimer = new javax.swing.Timer(80, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (farmModel != null && farmModel.getPlayer() != null) {
                    if (farmModel.getCurrentGameState() == GameState.IN_GAME || 
                        farmModel.getCurrentGameState() == GameState.SHIPPING_BIN || 
                        farmModel.getCurrentGameState() == GameState.STORE_UI ||
                        farmModel.getCurrentGameState() == GameState.NPC_DIALOGUE || 
                        farmModel.getCurrentGameState() == GameState.CHEAT_INPUT) {
                        
                        // Update animasi pemain
                        farmModel.getPlayer().updateAnimation();
                        
                        // Update animasi NPC jika ada
                        MapArea currentPlayerMap = farmModel.getPlayer().getCurrentMap();
                        if (currentPlayerMap != null && farmModel.getNpcs() != null) {
                            for (NPC npc : farmModel.getNpcs()) { 
                                if (npc != null) {
                                    // Cek apakah NPC berada di peta yang sama dengan pemain
                                    MapArea npcMap = farmModel.getMapArea(npc.getHomeLocation()); // Asumsi homeLocation adalah map NPC berada
                                    if (npcMap == currentPlayerMap) {
                                        npc.updateAnimation();
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
        animationTimer.start();
    
        loadTileImages();

        // Set default font buat JOptionPane dialog
        UIManager.put("OptionPane.messageFont", DIALOG_FONT);
        UIManager.put("OptionPane.buttonFont", DIALOG_FONT);
        UIManager.put("TextField.font", DIALOG_FONT);
       
        // Initialize NPC Dialogue Box based on preferred size
        int preferredWidth = VIEWPORT_WIDTH_IN_TILES * TILE_SIZE;
        int preferredHeightTotal = VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE + INFO_PANEL_HEIGHT;

        int dialogueBoxWidth = preferredWidth * 3 / 4;
        int dialogueBoxHeight = preferredHeightTotal / 3;
        int dialogueBoxX = (preferredWidth - dialogueBoxWidth) / 2;
        int dialogueBoxY = preferredHeightTotal - dialogueBoxHeight - 20; // 20px from bottom of the entire panel
        npcDialogueBox = new Rectangle(dialogueBoxX, dialogueBoxY, dialogueBoxWidth, dialogueBoxHeight);

        try {
                npcPortraitPlaceholder = new BufferedImage(PORTRAIT_SIZE, PORTRAIT_SIZE, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = ((BufferedImage)npcPortraitPlaceholder).createGraphics();
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillRect(0, 0, PORTRAIT_SIZE, PORTRAIT_SIZE);
                g2d.setColor(Color.BLACK);
                g2d.drawString("P", PORTRAIT_SIZE/2 - 5, PORTRAIT_SIZE/2 + 5);
                g2d.dispose();
            } catch (Exception e) { 
                System.err.println("Failed to load NPC portrait placeholder: " + e.getMessage());
                npcPortraitPlaceholder = new BufferedImage(PORTRAIT_SIZE, PORTRAIT_SIZE, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = ((BufferedImage)npcPortraitPlaceholder).createGraphics();
                g2d.setColor(Color.GRAY);
                g2d.fillRect(0, 0, PORTRAIT_SIZE, PORTRAIT_SIZE);
                g2d.dispose();
            }
        
        // Initialize Store UI Rectangles
        int storePanelWidth = VIEWPORT_WIDTH_IN_TILES * TILE_SIZE * 3 / 4;
        int storePanelHeight = VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE;
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
        int shippingBinPanelWidth = VIEWPORT_WIDTH_IN_TILES * TILE_SIZE * 5 / 6; 
        int shippingBinPanelHeight = VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE * 5 / 6;
        int shippingBinPanelX = (VIEWPORT_WIDTH_IN_TILES * TILE_SIZE - shippingBinPanelWidth) / 2;
        int shippingBinPanelY = (VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE - shippingBinPanelHeight) / 2 + INFO_PANEL_HEIGHT / 2;
        shippingBinPanelRect = new Rectangle(shippingBinPanelX, shippingBinPanelY, shippingBinPanelWidth, shippingBinPanelHeight);

        int listWidth = shippingBinPanelWidth / 2 - 30;
        int listHeight = shippingBinPanelHeight - 120; 

        playerItemsListRect = new Rectangle(shippingBinPanelX + 20, shippingBinPanelY + 60, listWidth, listHeight);
        binItemsListRect = new Rectangle(shippingBinPanelX + shippingBinPanelWidth / 2 + 10, shippingBinPanelY + 60, listWidth, listHeight);
        
        shippingBinQuantityRect = new Rectangle(shippingBinPanelX + 20, shippingBinPanelY + shippingBinPanelHeight - 50, listWidth, 30);
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
        int cheatPanelWidth = VIEWPORT_WIDTH_IN_TILES * TILE_SIZE / 2;
        int cheatPanelHeight = 80; 
        int cheatPanelX = (VIEWPORT_WIDTH_IN_TILES * TILE_SIZE - cheatPanelWidth) / 2;
        int cheatPanelY = (VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE + INFO_PANEL_HEIGHT) / 2 - cheatPanelHeight - 20;
        cheatInputPanelRect = new Rectangle(cheatPanelX, cheatPanelY, cheatPanelWidth, cheatPanelHeight);

        // Initialize End of Day Summary Panel Rect
        int eodPanelWidth = VIEWPORT_WIDTH_IN_TILES * TILE_SIZE * 2 / 3;
        int eodPanelHeight = VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE / 2;
        int eodPanelX = (VIEWPORT_WIDTH_IN_TILES * TILE_SIZE - eodPanelWidth) / 2;
        int eodPanelY = (VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE + INFO_PANEL_HEIGHT - eodPanelHeight) / 2;
        endOfDayPanelRect = new Rectangle(eodPanelX, eodPanelY, eodPanelWidth, eodPanelHeight);

        // Initialize World Map Selection Panel Rect
        int wmPanelWidth = VIEWPORT_WIDTH_IN_TILES * TILE_SIZE * 2 / 3;
        int wmPanelHeight = VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE / 2;
        int wmPanelX = (VIEWPORT_WIDTH_IN_TILES * TILE_SIZE - wmPanelWidth) / 2;
        int wmPanelY = (VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE + INFO_PANEL_HEIGHT - wmPanelHeight) / 2;
        worldMapPanelRect = new Rectangle(wmPanelX, wmPanelY, wmPanelWidth, wmPanelHeight);

        // Initialize Inventory View UI Rectangles
        int invPanelWidth = INVENTORY_COLS * (INVENTORY_CELL_SIZE + INVENTORY_PADDING) + INVENTORY_PADDING * 2;
        int invPanelHeight = INVENTORY_ROWS * (INVENTORY_CELL_SIZE + INVENTORY_PADDING) + INVENTORY_PADDING * 2 + 50;
        int invPanelX = (VIEWPORT_WIDTH_IN_TILES * TILE_SIZE - invPanelWidth) / 2;
        int invPanelY = (VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE + INFO_PANEL_HEIGHT - invPanelHeight) / 2;
        inventoryPanelRect = new Rectangle(invPanelX, invPanelY, invPanelWidth, invPanelHeight);
        inventoryGridRect = new Rectangle(
            invPanelX + INVENTORY_PADDING,
            invPanelY + INVENTORY_PADDING + 30,
            INVENTORY_COLS * (INVENTORY_CELL_SIZE + INVENTORY_PADDING),
            INVENTORY_ROWS * (INVENTORY_CELL_SIZE + INVENTORY_PADDING)
        );

        // Initialize Player Info View UI Rectangles
        int piPanelWidth = VIEWPORT_WIDTH_IN_TILES * TILE_SIZE * 2 / 3;
        int piPanelHeight = VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE / 2;
        int piPanelX = (VIEWPORT_WIDTH_IN_TILES * TILE_SIZE - piPanelWidth) / 2;
        int piPanelY = (VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE + INFO_PANEL_HEIGHT - piPanelHeight) / 2;
        playerInfoPanelRect = new Rectangle(piPanelX, piPanelY, piPanelWidth, piPanelHeight);

        // Initialize Statistics View UI Rectangles and Components
        int statsPanelWidth = VIEWPORT_WIDTH_IN_TILES * TILE_SIZE * 3 / 4;
        int statsPanelHeight = VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE * 2 / 3;
        int statsPanelX = (VIEWPORT_WIDTH_IN_TILES * TILE_SIZE - statsPanelWidth) / 2;
        int statsPanelY = (VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE + INFO_PANEL_HEIGHT - statsPanelHeight) / 2;
        statisticsPanelRect = new Rectangle(statsPanelX, statsPanelY, statsPanelWidth, statsPanelHeight);

        // Initialize Pause Menu Panel Rect (centered)
        int pmPanelWidth = VIEWPORT_WIDTH_IN_TILES * TILE_SIZE / 3;
        int pmPanelHeight = VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE / 2;
        int pmPanelX = (VIEWPORT_WIDTH_IN_TILES * TILE_SIZE - pmPanelWidth) / 2;
        int pmPanelY = (VIEWPORT_HEIGHT_IN_TILES * TILE_SIZE + INFO_PANEL_HEIGHT - pmPanelHeight) / 2;
        pauseMenuPanelRect = new Rectangle(pmPanelX, pmPanelY, pmPanelWidth, pmPanelHeight);

        if (this.getLayout() == null) { 
        }
        initMenuMusic(); // Initialize menu music
        initInGameMusic(); // Initialize in-game music
        System.out.println("GamePanel Constructor: Initial GameState: " + (farmModel != null ? farmModel.getCurrentGameState() : "FarmModel is null"));
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
        } catch (IllegalArgumentException e) {
            System.err.println("Error with image path (IllegalArgumentException): " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method buat ngeset general message di screen singkat
    public void setGeneralGameMessage(String message, boolean isError) {
        generalGameMessage = message;
        generalGameMessageColor = isError ? Color.RED : Color.WHITE;
        if (generalGameMessageTimer.isRunning()) {
            generalGameMessageTimer.restart();
        } else {
            generalGameMessageTimer.start();
        }
        repaint();
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
        if (gameTimer != null) {
            gameTimer.stop();
        }
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
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (farmModel == null || farmModel.getPlayer() == null) {
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

        // Atur music
        if (currentState == GameState.MAIN_MENU) {
            playMenuMusic();
        } else if (currentState == GameState.IN_GAME || 
                   currentState == GameState.STORE_UI || 
                   currentState == GameState.SHIPPING_BIN || 
                   currentState == GameState.NPC_DIALOGUE || 
                   currentState == GameState.CHEAT_INPUT || 
                   currentState == GameState.WORLD_MAP_SELECTION || 
                   currentState == GameState.INVENTORY_VIEW || 
                   currentState == GameState.PLAYER_INFO_VIEW || 
                   currentState == GameState.STATISTICS_VIEW || 
                   currentState == GameState.END_OF_DAY_SUMMARY) {
            playInGameMusic();
        } else if (currentState == GameState.PAUSE_MENU) {
            if (inGameMusicClip != null && inGameMusicClip.isRunning()) {
                stopInGameMusicSavingPosition();
            }
            if (menuMusicClip != null && menuMusicClip.isRunning()) { 
                stopMenuMusicSavingPosition();
            }
        } else {
            if (inGameMusicClip != null && inGameMusicClip.isRunning()) {
                stopInGameMusicSavingPosition();
            }
            if (menuMusicClip != null && menuMusicClip.isRunning()) {
                stopMenuMusicSavingPosition();
            }
        }

        if (currentState == GameState.MAIN_MENU) {
            drawMainMenu(g2d);
        } else if (currentState == GameState.END_OF_DAY_SUMMARY) {
            drawCurrentMap(g2d);
            drawPlayer(g2d);
            drawNPCs(g2d); 
            drawEndOfDaySummaryUI(g2d);
        } else {
            drawCurrentMap(g2d);
            drawPlayer(g2d);
            drawNPCs(g2d); 
            drawPlayerInfo(g2d); 
            drawDayNightTint(g2d); 
            
            if (currentState == GameState.NPC_DIALOGUE) {
                drawNpcDialogue(g2d);
            } else if (currentState == GameState.STORE_UI) {
                drawStoreUI(g2d);
            } else if (currentState == GameState.SHIPPING_BIN) {
                drawShippingBinUI(g2d);
            } else if (currentState == GameState.CHEAT_INPUT) {
                drawCheatInputUI(g2d);
            } else if (currentState == GameState.WORLD_MAP_SELECTION) { 
                drawWorldMapSelectionUI(g2d);
            } else if (currentState == GameState.INVENTORY_VIEW) { 
                drawInventoryViewUI(g2d);
            } else if (currentState == GameState.PLAYER_INFO_VIEW) { 
                drawPlayerInfoUI(g2d);
            } else if (currentState == GameState.STATISTICS_VIEW) { 
                drawStatisticsUI(g2d);
            }
        }

        if (currentState == GameState.PAUSE_MENU) {
            drawPauseMenu(g2d);
        }

        drawGeneralGameMessage(g2d);

        g2d.dispose(); 
    }

    private void drawGeneralGameMessage(Graphics2D g2d) {
        if (generalGameMessage.isEmpty()) {
            return;
        }

        FontMetrics fm = g2d.getFontMetrics(GENERAL_MESSAGE_FONT);
        int messageWidth = fm.stringWidth(generalGameMessage);
        int messageHeight = fm.getHeight();

        int x = (getWidth() - messageWidth) / 2;
        int y = INFO_PANEL_HEIGHT + messageHeight + 15; 

        g2d.setColor(new Color(0, 0, 0, 180)); 
        g2d.fillRect(x - 10, y - messageHeight, messageWidth + 20, messageHeight + 10); 

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
        g2d.setColor(CHEAT_INPUT_TEXT_COLOR.darker()); 
        String promptText = "Enter Cheat Code (Esc to Cancel):";
        FontMetrics fmPrompt = g2d.getFontMetrics();
        int promptX = cheatInputPanelRect.x + 10;
        int promptY = cheatInputPanelRect.y + fmPrompt.getAscent() + 5;
        g2d.drawString(promptText, promptX, promptY);

        // Input String
        g2d.setFont(CHEAT_INPUT_FONT);
        g2d.setColor(CHEAT_INPUT_TEXT_COLOR);
        String displayInput = cheatInputString + (System.currentTimeMillis() / 500 % 2 == 0 ? "_" : "");
        int inputX = cheatInputPanelRect.x + 10;
        int inputY = cheatInputPanelRect.y + cheatInputPanelRect.height - fmPrompt.getDescent() - 10; 
        g2d.drawString("> " + displayInput, inputX, inputY);
    }

    private void drawMainMenu(Graphics g) {
        // Draw background
        g.setColor(MENU_BACKGROUND_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw Title (similar to Harvest Moon image)
        g.setFont(MENU_FONT.deriveFont(Font.BOLD, 60f)); 
        g.setColor(MENU_TEXT_COLOR);
        String title = "Spakbor Hills";
        FontMetrics fmTitle = g.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(title);
        g.drawString(title, (getWidth() - titleWidth) / 2, getHeight() / 4);

        // Draw Menu Items
        g.setFont(MENU_ITEM_FONT);
        FontMetrics fmItems = g.getFontMetrics();
        int itemHeight = fmItems.getHeight();
        int startY = getHeight() / 2; 

        for (int i = 0; i < menuOptions.length; i++) {
            String itemText = menuOptions[i];
            if (i == currentMenuSelection) {
                g.setColor(MENU_SELECTED_TEXT_COLOR);
                itemText = "> " + itemText + " <"; 
            } else {
                g.setColor(MENU_TEXT_COLOR);
            }
            int itemWidth = fmItems.stringWidth(itemText);
            g.drawString(itemText, (getWidth() - itemWidth) / 2, startY + i * (itemHeight + 15)); 
        }
    }

    private void drawDayNightTint(Graphics g) {
        if (farmModel == null || farmModel.getCurrentTime() == null) {
            return;
        }

        int currentHour = farmModel.getCurrentTime().getHour();
        Color tintColor = null;

        if (currentHour >= 22 || currentHour < 5) { 
            tintColor = new Color(0, 0, 70, 100); 
        } else if (currentHour >= 18) { 
            tintColor = new Color(200, 100, 0, 70);
        } else if (currentHour >= 5 && currentHour < 7) { 
            tintColor = new Color(255, 204, 153, 60);
        } else {
        }

        if (tintColor != null) {
            g.setColor(tintColor);
            g.fillRect(0, INFO_PANEL_HEIGHT, getWidth(), getHeight() - INFO_PANEL_HEIGHT);
        }
    }

    private void drawPlayerInfo(Graphics g) {
        if (farmModel == null || farmModel.getPlayer() == null) {
            return;
        }
        Player player = farmModel.getPlayer();
        Graphics2D g2d = (Graphics2D) g.create(); 

        // Fonts
        Font labelFont = new Font("PixelMix", Font.BOLD, 18);
        Font valueFont = new Font("PixelMix", Font.PLAIN, 18);
        if (labelFont.getFamily().equals("Dialog") || !labelFont.getFontName().toLowerCase().contains("pixelmix")) {
            labelFont = new Font("Arial", Font.BOLD, 16);
        }
        if (valueFont.getFamily().equals("Dialog") || !valueFont.getFontName().toLowerCase().contains("pixelmix")) {
            valueFont = new Font("Arial", Font.PLAIN, 16);
        }
        
        FontMetrics labelFm = g2d.getFontMetrics(labelFont);
        FontMetrics valueFm = g2d.getFontMetrics(valueFont);

        Color textColor = Color.WHITE;
        Color energyBarColor = new Color(70, 200, 70); 
        Color energyBarBgColor = new Color(50, 50, 50);

        // Layout Constants
        int padding = 8; 
        int V_SPACING_BETWEEN_ITEMS = valueFm.getHeight() + 4; 
        int H_PADDING_LABEL_VALUE = 5; 
        int H_PADDING_COLUMNS = 15; 

        int energyBarHeight = 14;
        int energyBarWidth = 100; 
        
        int totalWidth = 0;
        
        // First row width calculation
        int nameWidth = labelFm.stringWidth("Name:") + H_PADDING_LABEL_VALUE + 
                       valueFm.stringWidth(player.getName()) + H_PADDING_COLUMNS;
        int goldWidth = labelFm.stringWidth("Gold:") + H_PADDING_LABEL_VALUE + 
                       valueFm.stringWidth(String.format("%d G", player.getGold())) + H_PADDING_COLUMNS;
        int energyWidth = labelFm.stringWidth("Energy:") + H_PADDING_LABEL_VALUE + 
                         energyBarWidth + H_PADDING_LABEL_VALUE + 
                         valueFm.stringWidth(String.format("%d/%d", player.getEnergy(), Player.MAX_ENERGY)) + H_PADDING_COLUMNS;
        
        int firstRowWidth = padding + nameWidth + goldWidth + energyWidth;
        
        // Second row width calculation
        int timeWidth = labelFm.stringWidth("Time:") + H_PADDING_LABEL_VALUE + 
                       valueFm.stringWidth(farmModel.getCurrentTime().getTimeString()) + H_PADDING_COLUMNS;
        int dateSeasonWidth = labelFm.stringWidth("Date:") + H_PADDING_LABEL_VALUE + 
                       valueFm.stringWidth(String.format("Day %d, %s", farmModel.getCurrentTime().getCurrentDay(), 
                       farmModel.getCurrentTime().getCurrentSeason())) + H_PADDING_COLUMNS;
        int weatherWidth = labelFm.stringWidth("Weather:") + H_PADDING_LABEL_VALUE + 
                       valueFm.stringWidth(farmModel.getCurrentTime().getCurrentWeather().toString());
        
        int secondRowWidth = padding + timeWidth + dateSeasonWidth + weatherWidth;
        
        // Third row width (holding + items) - this is more variable based on inventory
        Item selectedItem = player.getSelectedItem();
        String selectedItemName = (selectedItem != null) ? selectedItem.getName() : "None";
        int holdingWidth = padding + labelFm.stringWidth("Holding:") + H_PADDING_LABEL_VALUE + 
                          valueFm.stringWidth(selectedItemName) + H_PADDING_COLUMNS + 10;
        
        int hotbarMinWidth = 200;
        
        int thirdRowWidth = holdingWidth + labelFm.stringWidth("Items:") + H_PADDING_LABEL_VALUE + hotbarMinWidth;
        
        totalWidth = Math.max(firstRowWidth, Math.max(secondRowWidth, thirdRowWidth));
        
        // Panel Background 
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), INFO_PANEL_HEIGHT);

        // Top Row 
        int topRowY = padding + valueFm.getAscent(); 

        // Column 1: Name
        int currentX = padding;
        g2d.setFont(labelFont);
        g2d.setColor(textColor);
        g2d.drawString("Name:", currentX, topRowY);
        currentX += labelFm.stringWidth("Name:") + H_PADDING_LABEL_VALUE;
        g2d.setFont(valueFont);
        g2d.drawString(player.getName(), currentX, topRowY);
        currentX += valueFm.stringWidth(player.getName()) + H_PADDING_COLUMNS;

        // Column 2: Gold
        g2d.setFont(labelFont);
        g2d.drawString("Gold:", currentX, topRowY);
        currentX += labelFm.stringWidth("Gold:") + H_PADDING_LABEL_VALUE;
        g2d.setFont(valueFont);
        String goldStr = String.format("%d G", player.getGold());
        g2d.drawString(goldStr, currentX, topRowY);
        currentX += valueFm.stringWidth(goldStr) + H_PADDING_COLUMNS;
        
        // Column 3: Energy (Bar + Text)
        g2d.setFont(labelFont);
        g2d.drawString("Energy:", currentX, topRowY);
        currentX += labelFm.stringWidth("Energy:") + H_PADDING_LABEL_VALUE;

        int energyBarX = currentX;
        
        int energyBarY = topRowY - valueFm.getAscent() + (valueFm.getHeight() - energyBarHeight) / 2 ;

        double energyPercent = (double) player.getEnergy() / Player.MAX_ENERGY;
        energyPercent = Math.max(0, Math.min(1, energyPercent));
        int currentEnergyWidth = (int) (energyBarWidth * energyPercent);

        g2d.setColor(energyBarBgColor);
        g2d.fillRect(energyBarX, energyBarY, energyBarWidth, energyBarHeight);
        g2d.setColor(energyBarColor);
        g2d.fillRect(energyBarX, energyBarY, currentEnergyWidth, energyBarHeight);
        g2d.setColor(textColor.darker()); 
        g2d.drawRect(energyBarX, energyBarY, energyBarWidth, energyBarHeight);
        currentX += energyBarWidth + H_PADDING_LABEL_VALUE;

        g2d.setFont(valueFont);
        String energyText = String.format("%d/%d", player.getEnergy(), Player.MAX_ENERGY);
        g2d.drawString(energyText, currentX, topRowY);
        currentX += valueFm.stringWidth(energyText) + H_PADDING_COLUMNS; 

        //  Middle Row 
        int middleRowY = topRowY + V_SPACING_BETWEEN_ITEMS;
        currentX = padding; 

        // Column 1: Time
        g2d.setFont(labelFont);
        g2d.drawString("Time:", currentX, middleRowY);
        currentX += labelFm.stringWidth("Time:") + H_PADDING_LABEL_VALUE;
        g2d.setFont(valueFont);
        String timeStr = farmModel.getCurrentTime().getTimeString();
        g2d.drawString(timeStr, currentX, middleRowY);
        currentX += valueFm.stringWidth(timeStr) + H_PADDING_COLUMNS;

        // Column 2: Date & Season
        g2d.setFont(labelFont);
        g2d.drawString("Date:", currentX, middleRowY);
        currentX += labelFm.stringWidth("Date:") + H_PADDING_LABEL_VALUE;
        g2d.setFont(valueFont);
        String dateSeasonText = String.format("Day %d, %s", farmModel.getCurrentTime().getCurrentDay(), farmModel.getCurrentTime().getCurrentSeason());
        g2d.drawString(dateSeasonText, currentX, middleRowY);
        currentX += valueFm.stringWidth(dateSeasonText) + H_PADDING_COLUMNS;

        // Column 3: Weather
        g2d.setFont(labelFont);
        g2d.drawString("Weather:", currentX, middleRowY);
        currentX += labelFm.stringWidth("Weather:") + H_PADDING_LABEL_VALUE;
        g2d.setFont(valueFont);
        g2d.drawString(farmModel.getCurrentTime().getCurrentWeather().toString(), currentX, middleRowY);

        // Bottom Row: Selected Item & Hotbar 
        int bottomRowY = middleRowY + V_SPACING_BETWEEN_ITEMS;
        currentX = padding;

        // Selected Item (Text Only)
        g2d.setFont(labelFont);
        g2d.drawString("Holding:", currentX, bottomRowY);
        currentX += labelFm.stringWidth("Holding:") + H_PADDING_LABEL_VALUE;
        
        g2d.setFont(valueFont);
        g2d.drawString(selectedItemName, currentX, bottomRowY);
        currentX += valueFm.stringWidth(selectedItemName) + H_PADDING_COLUMNS + 10; 

        int hotbarStartX = currentX; 
        int maxRemainingWidth = getWidth() - hotbarStartX - padding;
        
        if (maxRemainingWidth > labelFm.stringWidth("Items: ") + 50) { 
            g2d.setFont(labelFont);
            g2d.drawString("Items:", hotbarStartX, bottomRowY);
            hotbarStartX += labelFm.stringWidth("Items:") + H_PADDING_LABEL_VALUE;
            
            g2d.setFont(valueFont);
            if (gameController != null) {
                List<Item> allPlayerItems = gameController.getPlayerInventoryItems();
                StringBuilder hotbarDisplayString = new StringBuilder();
                int maxVisibleHotbarItems = 3; 
                int hotbarMaxWidth = maxRemainingWidth - labelFm.stringWidth("Items:") - H_PADDING_LABEL_VALUE; 

                if (allPlayerItems.isEmpty()) {
                    hotbarDisplayString.append("Empty");
                } else {
                    int selectedIdx = -1;
                    if (selectedItem != null) {
                        for (int i = 0; i < allPlayerItems.size(); i++) {
                            if (allPlayerItems.get(i).equals(selectedItem)) {
                                selectedIdx = i;
                                break;
                            }
                        }
                    }

                    int startDisplayIdx = 0;
                    int endDisplayIdx = Math.min(allPlayerItems.size(), maxVisibleHotbarItems);

                    if (allPlayerItems.size() > maxVisibleHotbarItems) {
                        if (selectedIdx != -1) {
                            startDisplayIdx = Math.max(0, selectedIdx - (maxVisibleHotbarItems - 1) / 2); 
                            endDisplayIdx = Math.min(allPlayerItems.size(), startDisplayIdx + maxVisibleHotbarItems);
                            if (endDisplayIdx - startDisplayIdx < maxVisibleHotbarItems && allPlayerItems.size() >= maxVisibleHotbarItems) {
                                endDisplayIdx = Math.min(allPlayerItems.size(), selectedIdx + (maxVisibleHotbarItems / 2) + 1);
                                startDisplayIdx = Math.max(0, endDisplayIdx - maxVisibleHotbarItems);
                            }
                        }
                    }

                    if (startDisplayIdx > 0) {
                        hotbarDisplayString.append("...");
                    }

                    for (int i = startDisplayIdx; i < endDisplayIdx; i++) {
                        if (hotbarDisplayString.length() > 0 && !hotbarDisplayString.toString().equals("...")) {
                            hotbarDisplayString.append(" | ");
                        } else if (hotbarDisplayString.length() > 0 && i > startDisplayIdx) {
                            hotbarDisplayString.append(" | ");
                        }

                        Item currentHotbarItem = allPlayerItems.get(i);
                        String itemName = currentHotbarItem.getName();
                        if (itemName.length() > 8) {
                            itemName = itemName.substring(0, 7) + ".";
                        }
                                            
                        String tempString = hotbarDisplayString.toString() + itemName;
                        if (valueFm.stringWidth(tempString) > hotbarMaxWidth && i > startDisplayIdx) {
                            hotbarDisplayString.append("...");
                            break; 
                        }
                        
                        hotbarDisplayString.append(itemName);
                    }

                    if (endDisplayIdx < allPlayerItems.size()) {
                        hotbarDisplayString.append("...");
                    }
                }
                g2d.drawString(hotbarDisplayString.toString(), hotbarStartX, bottomRowY);
            }
        }

        g2d.dispose(); 
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
                    g.setColor(Color.PINK);
                    g.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
                    continue;
                }

                Image imageToDraw = null;
                TileType type = currentTile.getType();

                if (currentMap instanceof com.spakborhills.model.Store) {
                    if (type == TileType.DEPLOYED_OBJECT) {
                        DeployedObject associatedObj = currentTile.getAssociatedObject();
                    } else if (type == TileType.WALL) {
                        imageToDraw = wallImage;
                    } else if (type == TileType.ENTRY_POINT) {
                        imageToDraw = portalImage;
                    } else {
                        imageToDraw = storeTileImage;
                    }
                }

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
                            break;
                    }
                }
                
                // Draw the selected base image
                if (imageToDraw != null) {
                    g.drawImage(imageToDraw, screenX, screenY, TILE_SIZE, TILE_SIZE, this);
                } else { 
                    g.setColor(new Color(128, 0, 128, 150));
                g.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
                    g.setColor(Color.WHITE);
                    g.drawString(type.toString().substring(0, Math.min(type.toString().length(),3)), screenX + 5, screenY + 20);
                }

                if (currentTile.isWatered() && type == TileType.TILLED) { // Only for TILLED type now
                    if (waterImage != null) {
                        g.drawImage(waterImage, screenX, screenY, TILE_SIZE, TILE_SIZE, this);
                    }
                }
            }
        }
    }

    private void drawNPCs(Graphics g) {
        Player player = farmModel.getPlayer();
        MapArea currentMap = player.getCurrentMap();
        List<NPC> allNPCs = farmModel.getNpcs();
    
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
    
            if (currentMap == npcHomeMapInstance) {
                int npcTileScreenX = npc.getCurrentTileX() * TILE_SIZE - camX;
                int npcTileScreenY = npc.getCurrentTileY() * TILE_SIZE - camY + INFO_PANEL_HEIGHT;
    
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
                        int drawWidth = (int) ((double) TILE_SIZE / originalSpriteHeight * originalSpriteWidth);
    
                        // Posisi X dan Y untuk menggambar sprite agar tengah horizontal dan rata bawah di dalam tile
                        int drawX = npcTileScreenX + (TILE_SIZE - drawWidth) / 2; 
                        int drawY = npcTileScreenY + (TILE_SIZE - drawHeight);    
    
                        g.drawImage(spriteFrame, drawX, drawY, drawWidth, drawHeight, this);
                    
                    } else {
                        // Fallback jika dimensi sprite asli tidak valid (misal 0), gambar kotak placeholder
                        g.setColor(Color.MAGENTA); 
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
    
        // Koordinat tile pemain di layar
        int playerTileScreenX = player.getCurrentTileX() * TILE_SIZE - camX;
        int playerTileScreenY = player.getCurrentTileY() * TILE_SIZE - camY + INFO_PANEL_HEIGHT;
    
        Image spriteFrame = player.getCurrentSpriteFrame();
    
        if (spriteFrame != null) {
            int originalSpriteWidth = player.spriteWidthPlayer;  
            int originalSpriteHeight = player.spriteHeightPlayer; 
    
            if (originalSpriteWidth > 0 && originalSpriteHeight > 0) {
                int drawHeight = TILE_SIZE; 
                int drawWidth = (int) ((double) TILE_SIZE / originalSpriteHeight * originalSpriteWidth);
    
                int drawX = playerTileScreenX + (TILE_SIZE - drawWidth) / 2;  
                int drawY = playerTileScreenY + (TILE_SIZE - drawHeight);     
    
                g.drawImage(spriteFrame, drawX, drawY, drawWidth, drawHeight, this);
            } else {
                g.setColor(Color.BLUE); 
                g.fillRect(playerTileScreenX, playerTileScreenY, TILE_SIZE, TILE_SIZE);
                g.setColor(Color.BLACK);
                g.drawString("DIM?", playerTileScreenX + 5, playerTileScreenY + 15); 
            }
        } else {
            g.setColor(Color.RED); 
            g.fillRect(playerTileScreenX, playerTileScreenY, TILE_SIZE, TILE_SIZE);
            g.setColor(Color.BLACK);
            g.drawRect(playerTileScreenX, playerTileScreenY, TILE_SIZE, TILE_SIZE);
            g.drawString("NO SPRITE", playerTileScreenX + 5, playerTileScreenY + 15); 
        }
    
        // Menggambar nama item yang dipilih di atas pemain
        Item selectedItem = player.getSelectedItem();
        if (selectedItem != null) {
            String itemName = selectedItem.getName();
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics(); 

            int stringWidth = fm.stringWidth(itemName);
            
            int textY = playerTileScreenY - 5;

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

        // Global Escape for Pause Menu
        if (keyCode == KeyEvent.VK_ESCAPE) {
            if (farmModel.getCurrentGameState() == GameState.IN_GAME) {
                farmModel.setCurrentGameState(GameState.PAUSE_MENU);
                stopGameTimer();
                stopInGameMusicSavingPosition(); 
                currentPauseMenuSelection = 0; 
                repaint();
                return;
            } else if (farmModel.getCurrentGameState() == GameState.PAUSE_MENU) {
                farmModel.setCurrentGameState(GameState.IN_GAME);
                startGameTimer(); 
                if (inGameMusicClip != null && !inGameMusicClip.isRunning()) {
                    inGameMusicClip.setFramePosition((int) inGameMusicPosition);
                    inGameMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                    inGameMusicClip.start();
                }
                repaint();
                return;
            }
        }

        if (farmModel.getCurrentGameState() == GameState.PAUSE_MENU) {
            handlePauseMenuInput(keyCode);
            repaint();
            return;
        }
        if (farmModel.getCurrentGameState() == GameState.CHEAT_INPUT) {
            handleCheatTyping(e); 
            repaint();
            return;
        }
        if (farmModel.getCurrentGameState() == GameState.WORLD_MAP_SELECTION) {
            handleWorldMapSelectionInput(keyCode);
                repaint();
            return;
        }
        if (farmModel.getCurrentGameState() == GameState.STORE_UI) {
            handleStoreInput(keyCode);
            repaint();
            return; 
        }
        if (farmModel.getCurrentGameState() == GameState.SHIPPING_BIN) {
            handleShippingBinInput(keyCode);
            repaint(); 
            return;     
        }
        if (farmModel.getCurrentGameState() == GameState.NPC_DIALOGUE) { 
             if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_X || keyCode == KeyEvent.VK_E) {
                isNpcDialogueActive = false; 
                farmModel.setCurrentGameState(GameState.IN_GAME); 
                playInGameMusic(); 
                repaint();
            }
            return;     
        }

        // Non-modal UI states 
        if (farmModel.getCurrentGameState() == GameState.MAIN_MENU) {
            handleMainMenuInput(keyCode);
            repaint();
            return;
        }

        if (farmModel.getCurrentGameState() == GameState.END_OF_DAY_SUMMARY) {
            if (keyCode == KeyEvent.VK_ENTER) {
                farmModel.setCurrentGameState(GameState.IN_GAME);
                playInGameMusic(); 
                startGameTimer(); 
            }
            repaint(); 
            return;
        }

        if (farmModel.getCurrentGameState() == GameState.INVENTORY_VIEW) {
            handleInventoryViewInput(keyCode);
            repaint();
            return;
        }

        if (farmModel.getCurrentGameState() == GameState.PLAYER_INFO_VIEW) {
            handlePlayerInfoViewInput(keyCode);
            repaint();
            return;
        }

        if (farmModel.getCurrentGameState() == GameState.STATISTICS_VIEW) {
            handleStatisticsViewInput(keyCode);
            repaint();
            return;
        }

        // IN_GAME actions
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
                    if (farmModel.getCurrentGameState() == GameState.IN_GAME) {
                        farmModel.setCurrentGameState(GameState.INVENTORY_VIEW);
                        currentInventoryCol = 0;
                        currentInventoryRow = 0;
                    } 
                    actionTaken = true;
                    break;
                case KeyEvent.VK_J: 
                    System.out.println("J key pressed - Viewing Player Info");
                gameController.requestViewPlayerInfo();
                    actionTaken = true;
                break;
                case KeyEvent.VK_O:
                System.out.println("O key pressed - Viewing Statistics");
                gameController.requestShowStatistics();
                    actionTaken = true;
                break;
        }

        if (actionTaken) {
                repaint();
            }
        }
    }

    private void handleCheatTyping(KeyEvent e) { 
        if (farmModel.getCurrentGameState() != GameState.CHEAT_INPUT) return;
        int keyCode = e.getKeyCode(); 

        if (keyCode == KeyEvent.VK_ENTER) {
            if (!cheatInputString.trim().isEmpty()) {
                processCheatCode(cheatInputString.trim());
        } else {
                setGeneralGameMessage("No cheat code entered.", true);
            }
            farmModel.setCurrentGameState(GameState.IN_GAME); 
            playInGameMusic(); 
            cheatInputString = ""; 
        } else if (keyCode == KeyEvent.VK_ESCAPE) {
            farmModel.setCurrentGameState(GameState.IN_GAME);
            playInGameMusic(); 
            setGeneralGameMessage("Cheat input cancelled.", false);
            cheatInputString = "";
        } else if (keyCode == KeyEvent.VK_BACK_SPACE) {
            if (!cheatInputString.isEmpty()) {
                cheatInputString = cheatInputString.substring(0, cheatInputString.length() - 1);
            }
        } else {
            char typedChar = e.getKeyChar(); 
            if (Character.isLetterOrDigit(typedChar) || typedChar == ' ') {
                 if (cheatInputString.length() < 50) { 
                    cheatInputString += Character.toLowerCase(typedChar); 
                }
            }
        }
    }

    private void processCheatCode(String cheatCode) {
        String[] parts = cheatCode.toLowerCase().split("\\s+"); 
        String command = parts[0];

        boolean success = false;
        String feedbackMessage = "";

        if (command.equals("help")) {
            showCheatsHelp(); 
            feedbackMessage = "Help dialog displayed."; 
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
            showFishDebugDialog(); 
            feedbackMessage = "Fish debug info displayed.";
        } else if (command.equals("gold")) {
            if (parts.length > 1) {
                try {
                    int amount = Integer.parseInt(parts[1]);
                    farmModel.getPlayer().addGold(amount); 
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
        if (success) repaint();
    }

    private void showFishDebugDialog() {
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
                        if (fish.getRarity() != FishRarity.COMMON) { 
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
    
            player.setMoving(false);
            repaint();
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
        } else {
            System.err.println("GamePanel: farmModel is null, cannot show EndOfDaySummaryUI properly.");
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

        if (!this.worldMapDestinations.isEmpty()) {
            this.currentWorldMapSelectionIndex = 0;
            farmModel.setCurrentGameState(GameState.WORLD_MAP_SELECTION);
            repaint();
                } else {
            setGeneralGameMessage("No destinations loaded for map selection.", true);
        }
    }

    /**
     * Displays a message to the player using a JOptionPane dialog.
     * @param message The message to display.
     */
    public void displayMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Game Message", JOptionPane.INFORMATION_MESSAGE);
    }

    public void updatePlayerInfoPanel() {
        repaint(0, 0, getWidth(), INFO_PANEL_HEIGHT);
    }
    
    public void updateGameRender() {
        repaint();
    }

    /**
     * Displays a dialogue message from an NPC.
     * @param npcName The name of the NPC speaking.
     * @param dialogue The dialogue text.
     */
    public void showNPCDialogue(NPC npc, String dialogue) {
        if (npc == null) {
            System.err.println("GamePanel.showNPCDialogue: Objek NPC null.");
            this.isNpcDialogueActive = false;
            this.currentInteractingNPC = null;
            repaint();
            return;
        }

        this.currentInteractingNPC = npc; 
        this.currentNpcName = npc.getName(); 
        this.currentNpcDialogue = dialogue;
        this.isNpcDialogueActive = true;
        if (farmModel != null) { 
            farmModel.setCurrentGameState(GameState.NPC_DIALOGUE); 
        }

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
     * @deprecated This method uses JOptionPane and is obsolete. Statistics are now shown via an in-game panel triggered by GameState.STATISTICS_VIEW.
     */
    @Deprecated
    public void showStatisticsDialog(String statisticsSummary) {
        System.err.println("GamePanel.showStatisticsDialog() was called. This method is deprecated. Statistics should be displayed via the in-game UI panel.");
        
        if (farmModel != null && gameController != null) {
            setGeneralGameMessage("Note: Statistics display uses new in-game panel.", false);
            gameController.requestShowStatistics(); 
        } else {
            System.err.println("CRITICAL: Deprecated showStatisticsDialog called, but cannot redirect to new system (farmModel or gameController is null). Statistics will not be shown via this path.");
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
        textArea.setFont(NPC_DIALOG_FONT);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 250));

        JOptionPane.showMessageDialog(this,
                scrollPane,
                "Player Information",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleMainMenuInput(int keyCode) {
        if (farmModel.getCurrentGameState() != GameState.MAIN_MENU) return;

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
    }

    private void selectMainMenuItem() {
        String selectedOption = menuOptions[currentMenuSelection];
        System.out.println("Main Menu item selected: " + selectedOption);
        switch (selectedOption) {
            case "New Game":
                gameFrame.onMainMenuSelection(0);
                break;
            case "Load Game":
                gameFrame.onMainMenuSelection(1);
                break;
            case "Help":
                gameFrame.onMainMenuSelection(2);
                break;
            case "Credits":
                gameFrame.onMainMenuSelection(3);
                break;
            case "Manage Saves":
                showManageSavesDialog();
                break;
            case "Exit":
                gameFrame.onMainMenuSelection(4);
                break;
        }
    }

    private void drawNpcDialogue(Graphics g) {
        if (!isNpcDialogueActive) { 
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create();

        int scaledDialoguePadding = (int)(DIALOGUE_PADDING * this.scaleFactor);
        if (scaledDialoguePadding < 5) scaledDialoguePadding = 5; 

        g2d.setColor(new Color(0, 0, 0, 200)); 
        g2d.fill(npcDialogueBox);
        g2d.setColor(Color.WHITE);
        g2d.draw(npcDialogueBox);

        Image npcPortraitToDraw = null;
        int actualPortraitWidthDrawn = (int)(PORTRAIT_SIZE * this.scaleFactor); 
        int actualPortraitHeightDrawn = (int)(PORTRAIT_SIZE * this.scaleFactor);
        String npcNameToDisplay = (this.currentNpcName != null) ? this.currentNpcName : "???";

        if (this.currentInteractingNPC != null) { 
            npcPortraitToDraw = this.currentInteractingNPC.getDefaultPortrait();
            npcNameToDisplay = this.currentInteractingNPC.getName();
            if (npcPortraitToDraw != null) { 
                actualPortraitWidthDrawn = this.currentInteractingNPC.portraitWidth; 
                actualPortraitHeightDrawn = this.currentInteractingNPC.portraitHeight;
            }
        }
        
        int portraitX = npcDialogueBox.x + scaledDialoguePadding;
        int portraitY = npcDialogueBox.y + scaledDialoguePadding;

        if (npcPortraitToDraw != null) {
            g2d.drawImage(npcPortraitToDraw, portraitX, portraitY,
                          actualPortraitWidthDrawn, 
                          actualPortraitHeightDrawn,
                          this);
        } else {
            int placeholderSize = (int)(PORTRAIT_SIZE * this.scaleFactor);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(portraitX, portraitY, placeholderSize, placeholderSize);
            g2d.setColor(Color.BLACK);
            String initial = (npcNameToDisplay != null && !npcNameToDisplay.isEmpty()) 
                             ? npcNameToDisplay.substring(0,1) 
                             : "?"; 
            Font tempFont = new Font("Arial", Font.BOLD, (int)(placeholderSize * 0.6)); 
            FontMetrics tempFm = g2d.getFontMetrics(tempFont);
            g2d.setFont(tempFont);
            g2d.drawString(initial, portraitX + (placeholderSize - tempFm.stringWidth(initial)) / 2, portraitY + (placeholderSize + tempFm.getAscent()) / 2 - tempFm.getDescent()/2);
        }
        
        g2d.setColor(Color.GRAY); 
        g2d.drawRect(portraitX, portraitY, actualPortraitWidthDrawn, actualPortraitHeightDrawn);

        g2d.setFont(this.DIALOGUE_NAME_FONT_SCALED); 
        FontMetrics nameFm = g2d.getFontMetrics();
        int nameX = portraitX + actualPortraitWidthDrawn + scaledDialoguePadding;
        int nameY = npcDialogueBox.y + scaledDialoguePadding + nameFm.getAscent();
        g2d.setColor(Color.YELLOW); 
        g2d.drawString(npcNameToDisplay + " says:", nameX, nameY);

        if (this.currentInteractingNPC != null) {
            int currentHP = this.currentInteractingNPC.getHeartPoints();
            int maxHP = this.currentInteractingNPC.getMaxHeartPoints(); 
            
            String heartString = String.format("Affection: %d / %d", currentHP, maxHP);
            Font scaledNpcDialogFont = this.NPC_DIALOG_FONT.deriveFont(Font.ITALIC, Math.max(8f, (float)(16f * this.scaleFactor))); 
            g2d.setFont(scaledNpcDialogFont);
            g2d.setColor(Color.PINK);
            int heartY = nameY + nameFm.getHeight() + (int)(2 * this.scaleFactor); 
            g2d.drawString(heartString, nameX, heartY);
        }

        g2d.setFont(this.DIALOGUE_TEXT_FONT_SCALED); 
        g2d.setColor(Color.WHITE);
        FontMetrics textFm = g2d.getFontMetrics();
        int textBlockStartX = nameX;
        int textBlockStartY = nameY + nameFm.getHeight() + scaledDialoguePadding; 
        if (this.currentInteractingNPC != null) { 
            Font scaledNpcDialogFontForHeight = this.NPC_DIALOG_FONT.deriveFont(Font.ITALIC, Math.max(8f, (float)(16f * this.scaleFactor)));
            textBlockStartY += g2d.getFontMetrics(scaledNpcDialogFontForHeight).getHeight() + (int)(2*this.scaleFactor);
        }

        int availableTextWidth = (npcDialogueBox.x + npcDialogueBox.width - scaledDialoguePadding) - textBlockStartX;
        
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

        int lineY = textBlockStartY; 
        for (String line : lines) {
            if (lineY + textFm.getHeight() > npcDialogueBox.y + npcDialogueBox.height - scaledDialoguePadding - textFm.getHeight()) { 
                g2d.drawString("...", textBlockStartX, lineY); 
                break;
            }
            g2d.drawString(line, textBlockStartX, lineY);
            lineY += textFm.getHeight();
        }

        Font scaledItalicDialogueTextFont = this.DIALOGUE_TEXT_FONT_SCALED.deriveFont(Font.ITALIC);
        g2d.setFont(scaledItalicDialogueTextFont);
        FontMetrics promptFm = g2d.getFontMetrics(); 
        String continuePrompt = "Press ENTER to continue...";
        int promptWidth = promptFm.stringWidth(continuePrompt); 
        int promptX = npcDialogueBox.x + npcDialogueBox.width - promptWidth - scaledDialoguePadding;
        int promptY = npcDialogueBox.y + npcDialogueBox.height - scaledDialoguePadding;
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString(continuePrompt, promptX, promptY);

        g2d.dispose();
    }

    private void handleStoreInput(int keyCode) {
        if (farmModel.getCurrentGameState() != GameState.STORE_UI) {
            return;
        }

        if (storeInputMode.equals("selecting_item")) {
            switch (keyCode) {
                case KeyEvent.VK_UP:
                    if (currentStoreItemSelectionIndex > 0) {
                        currentStoreItemSelectionIndex--;
                        adjustStoreScrollOffset();
                    } else {
                        currentStoreItemSelectionIndex = storeItemsForDisplay.size() - 1; 
                        storeScrollOffset = Math.max(0, storeItemsForDisplay.size() - getVisibleStoreItemCount());
                    }
                    storeFeedbackMessage = ""; 
                    break;
                case KeyEvent.VK_DOWN:
                    if (currentStoreItemSelectionIndex < storeItemsForDisplay.size() - 1) {
                        currentStoreItemSelectionIndex++;
                        adjustStoreScrollOffset();
                    } else {
                        currentStoreItemSelectionIndex = 0; 
                        storeScrollOffset = 0;
                    }
                    storeFeedbackMessage = ""; 
                    break;
                case KeyEvent.VK_PAGE_UP:
                    int visibleCount = getVisibleStoreItemCount();
                    currentStoreItemSelectionIndex = Math.max(0, currentStoreItemSelectionIndex - visibleCount);
                    adjustStoreScrollOffset();
                    storeFeedbackMessage = "";
                    break;
                case KeyEvent.VK_PAGE_DOWN:
                    visibleCount = getVisibleStoreItemCount();
                    currentStoreItemSelectionIndex = Math.min(storeItemsForDisplay.size() - 1, currentStoreItemSelectionIndex + visibleCount);
                    adjustStoreScrollOffset();
                    storeFeedbackMessage = "";
                    break;
                case KeyEvent.VK_ENTER:
                case KeyEvent.VK_E: 
                    if (storeItemsForDisplay != null && !storeItemsForDisplay.isEmpty()) {
                        storeInputMode = "inputting_quantity";
                        currentBuyQuantity = 1; 
                        quantityInputString = "1"; 
                        storeFeedbackMessage = ""; 
                    }
                    break;
                case KeyEvent.VK_ESCAPE:
                case KeyEvent.VK_T: 
                    isStoreUiActive = false;
                    storeFeedbackMessage = ""; 
                    if (farmModel != null) farmModel.setCurrentGameState(GameState.IN_GAME); 
                    this.requestFocusInWindow(); 
                    break;
            }
        } else if (storeInputMode.equals("inputting_quantity")) {
            switch (keyCode) {
                case KeyEvent.VK_UP:
                    currentBuyQuantity++;
                    if (currentBuyQuantity > 999) currentBuyQuantity = 999; 
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
                case KeyEvent.VK_E: 
                    if (storeItemsForDisplay != null && !storeItemsForDisplay.isEmpty()) {
                        Item selectedItem = storeItemsForDisplay.get(currentStoreItemSelectionIndex);
                        String buyAttemptMessage = gameController.requestBuyItemAndGetMessage(selectedItem.getName(), currentBuyQuantity);
                        
                        boolean success = !buyAttemptMessage.toLowerCase().contains("gagal") && 
                                          !buyAttemptMessage.toLowerCase().contains("tidak cukup") && 
                                          !buyAttemptMessage.toLowerCase().contains("tidak tersedia") &&
                                          !buyAttemptMessage.toLowerCase().contains("tidak dapat dibeli");

                        if (success) {
                            setStoreFeedback(buyAttemptMessage, false); 
                            storeItemsForDisplay = gameController.getStoreItemsForDisplay(); 
                            if (storeItemsForDisplay == null || storeItemsForDisplay.isEmpty()) {
                                isStoreUiActive = false; 
                                if (farmModel != null) farmModel.setCurrentGameState(GameState.IN_GAME);
                                this.requestFocusInWindow(); 
                            } else {
                                currentStoreItemSelectionIndex = Math.min(currentStoreItemSelectionIndex, storeItemsForDisplay.size() - 1);
                                if (currentStoreItemSelectionIndex <0) currentStoreItemSelectionIndex = 0;
                            }
                        } else {
                            setStoreFeedback(buyAttemptMessage, true); 
                        }
                        storeInputMode = "selecting_item"; 
                    }
                    break;
                case KeyEvent.VK_BACK_SPACE: 
                    storeInputMode = "selecting_item";
                    storeFeedbackMessage = "";
                    break;
                case KeyEvent.VK_ESCAPE:
                    isStoreUiActive = false;
                    storeFeedbackMessage = "";
                    if (farmModel != null) farmModel.setCurrentGameState(GameState.IN_GAME);
                    this.requestFocusInWindow(); 
                    break;
                
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
        repaint(); 
    }

    private String quantityInputString = ""; 
    private void updateQuantityInput(int digit) {
        if (storeInputMode.equals("inputting_quantity")) {
            if (quantityInputString.length() < 3) { 
                quantityInputString += digit;
                try {
                    currentBuyQuantity = Integer.parseInt(quantityInputString);
                    if (currentBuyQuantity == 0 && quantityInputString.length() > 0) currentBuyQuantity = 1; 
                    if (currentBuyQuantity > 999) currentBuyQuantity = 999;

                } catch (NumberFormatException e) {
                    quantityInputString = ""; 
                    currentBuyQuantity = 1;
                }
            }
            if (digit != -1) {
                if (currentBuyQuantity > 0 && currentBuyQuantity <100 && quantityInputString.length() <2 ) { 
                    currentBuyQuantity = currentBuyQuantity * 10 + digit;
                } else {
                    currentBuyQuantity = digit; 
                }
                if (currentBuyQuantity == 0) currentBuyQuantity =1; 
                if (currentBuyQuantity > 999) currentBuyQuantity = 999; 
                quantityInputString = String.valueOf(currentBuyQuantity); 
            }
        }
    }

    private void drawStoreUI(Graphics2D g2d) {
        if (!isStoreUiActive || storeItemsForDisplay == null) return;

        g2d.setColor(STORE_BG_COLOR);
        g2d.fillRect(storePanelRect.x, storePanelRect.y, storePanelRect.width, storePanelRect.height);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(storePanelRect.x, storePanelRect.y, storePanelRect.width, storePanelRect.height);

        g2d.setFont(this.DIALOGUE_NAME_FONT_SCALED); 
        g2d.setColor(STORE_TEXT_COLOR);
        String title = "Toko Spakbor Hills";
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(title);
        g2d.drawString(title, storePanelRect.x + (storePanelRect.width - titleWidth) / 2, storePanelRect.y + (int)(30 * this.scaleFactor));

        g2d.setFont(this.STORE_FONT_SCALED);
        g2d.setColor(STORE_TEXT_COLOR);
        g2d.drawString("[Esc] Tutup", storeCloseButtonRect.x + (int)(5 * this.scaleFactor), storeCloseButtonRect.y + fmTitle.getAscent() + (int)(5*this.scaleFactor) );


        g2d.setFont(this.STORE_ITEM_FONT_SCALED); 
        FontMetrics fmItem = g2d.getFontMetrics(); 
        int itemY = storeItemListRect.y + fmItem.getAscent(); 
        int itemLineHeight = fmItem.getHeight() + Math.max(1, (int)(2 * this.scaleFactor)); 

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

        int bottomReservedSpace = (itemLineHeight * 3) + 20; 

        int visibleItemCount = getVisibleStoreItemCount();
        int maxY = storeItemListRect.y + storeItemListRect.height - bottomReservedSpace;
        
        // Draw scroll indicators
        if (storeItemsForDisplay != null && storeItemsForDisplay.size() > visibleItemCount) {
            // Up indicator
            if (storeScrollOffset > 0) {
                g2d.setColor(STORE_HIGHLIGHT_COLOR);
                g2d.drawString("↑", storeItemListRect.x + storeItemListRect.width - 20, storeItemListRect.y + itemLineHeight);
            }
            
            // Down indicator
            if (storeScrollOffset + visibleItemCount < storeItemsForDisplay.size()) {
                g2d.setColor(STORE_HIGHLIGHT_COLOR);
                g2d.drawString("↓", storeItemListRect.x + storeItemListRect.width - 20, maxY - itemLineHeight);
            }
            
            // Draw scrollbar
            int scrollbarHeight = maxY - (storeItemListRect.y + itemLineHeight * 2);
            int thumbHeight = Math.max(30, scrollbarHeight * visibleItemCount / storeItemsForDisplay.size());
            int thumbY = storeItemListRect.y + itemLineHeight * 2 + 
                        (scrollbarHeight - thumbHeight) * storeScrollOffset / 
                        Math.max(1, storeItemsForDisplay.size() - visibleItemCount);
            
            // Draw track
            g2d.setColor(new Color(80, 80, 80, 100));
            g2d.fillRect(storeItemListRect.x + storeItemListRect.width - 10, 
                        storeItemListRect.y + itemLineHeight * 2, 
                        5, scrollbarHeight);
            
            // Draw thumb
            g2d.setColor(new Color(180, 180, 180, 180));
            g2d.fillRect(storeItemListRect.x + storeItemListRect.width - 10, 
                        thumbY, 5, thumbHeight);
        }

        if (storeItemsForDisplay != null && !storeItemsForDisplay.isEmpty()) {
            int endIndex = Math.min(storeScrollOffset + visibleItemCount, storeItemsForDisplay.size());
            
            for (int i = storeScrollOffset; i < endIndex; i++) {
                if (itemY > maxY) {
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
        
        FontMetrics bottomFontMetrics = g2d.getFontMetrics(this.STORE_FONT_SCALED);
        int bottomTextHeight = bottomFontMetrics.getHeight();
        int goldTextY = storePanelRect.y + storePanelRect.height - 20;
        String goldText = "Gold: " + farmModel.getPlayer().getGold() + " G";

        // Posisi untuk feedback message, di atas Gold
        int feedbackTextY = goldTextY - bottomTextHeight - 5; 

        if (storeItemsForDisplay != null && !storeItemsForDisplay.isEmpty() && currentStoreItemSelectionIndex < storeItemsForDisplay.size()) {
            if (storeInputMode.equals("inputting_quantity")) {
                Item selectedItem = storeItemsForDisplay.get(currentStoreItemSelectionIndex);
                g2d.setFont(this.STORE_FONT_SCALED);
                g2d.setColor(STORE_TEXT_COLOR);
                String promptText1 = "Beli " + selectedItem.getName() + "? Jumlah: " + currentBuyQuantity;
                String promptText2 = "([Up]/[Down] Ubah Jumlah)";
                int quantityPromptX = storePanelRect.x + Math.max(10, (int)(20 * this.scaleFactor));
                
                // Posisi Y untuk prompt kuantitas, di atas feedback atau Gold
                int quantityPromptY1 = feedbackTextY - bottomTextHeight - 5;
                int quantityPromptY2 = feedbackTextY;

                g2d.drawString(promptText1, quantityPromptX, quantityPromptY1);
                g2d.drawString(promptText2, quantityPromptX, quantityPromptY2);

                g2d.setColor(STORE_HIGHLIGHT_COLOR);
                g2d.drawString("[E/Enter] Beli", storeBuyButtonRect.x, quantityPromptY1);
                g2d.setColor(STORE_TEXT_COLOR);
                g2d.drawString("[Esc/Bksp] Batal", storeBuyButtonRect.x, quantityPromptY2);
            } else {
                g2d.setFont(this.STORE_FONT_SCALED);
                g2d.setColor(STORE_TEXT_COLOR);
                String instructionText = "([Up]/[Down] Pilih Item, [PgUp]/[PgDn] Page, [E/Enter] Pilih)";
                FontMetrics instructionFm = g2d.getFontMetrics();
                g2d.drawString(instructionText, storePanelRect.x + Math.max(10, (int)(20 * this.scaleFactor)), feedbackTextY - bottomTextHeight -5 ); 
            }
        } else if (storeItemsForDisplay == null || storeItemsForDisplay.isEmpty()) {
             g2d.setFont(this.STORE_FONT_SCALED);
             g2d.setColor(Color.YELLOW);
             String emptyStoreMsg = "Toko sedang kosong!";
             FontMetrics msgFm = g2d.getFontMetrics();
             g2d.drawString(emptyStoreMsg, storePanelRect.x + (storePanelRect.width - msgFm.stringWidth(emptyStoreMsg))/2, storeItemListRect.y + storeItemListRect.height / 2 );
        }

        // Gambar feedback message jika ada
        if (storeFeedbackMessage != null && !storeFeedbackMessage.isEmpty()) {
            g2d.setFont(this.STORE_FONT_SCALED);
            g2d.setColor(storeFeedbackColor);
            FontMetrics feedbackFm = g2d.getFontMetrics();
            int feedbackX = storePanelRect.x + Math.max(10, (int)(20 * this.scaleFactor)); 
            g2d.drawString(storeFeedbackMessage, feedbackX, feedbackTextY);
        }
        
        // Gambar Gold terakhir agar selalu di atas jika ada overlap
        g2d.setFont(this.STORE_FONT_SCALED);
        g2d.setColor(STORE_TEXT_COLOR);
        g2d.drawString(goldText, storePanelRect.x + Math.max(10, (int)(20 * this.scaleFactor)), goldTextY);
    }

    // Metode baru untuk mengatur feedback di UI Toko
    private void setStoreFeedback(String message, boolean isError) {
        this.storeFeedbackMessage = message;
        this.storeFeedbackColor = isError ? Color.RED : new Color(144, 238, 144);
        if (storeFeedbackTimer.isRunning()) {
            storeFeedbackTimer.restart();
        } else {
            storeFeedbackTimer.start();
        }
        repaint(); 
    }

    private void handleShippingBinInput(int keyCode) {
        if (farmModel.getCurrentGameState() != GameState.SHIPPING_BIN) {
            return;
        }

        if (shippingBinInputMode.equals("inputting_quantity")) {
            if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9) {
                if (shippingBinQuantityInputString.length() < 3) {
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
                        }
                    } catch (NumberFormatException nfe) {
                        setShippingBinFeedback("Invalid quantity.", true);
                    }
                }
                shippingBinInputMode = "selecting_player_item";
                shippingBinQuantityInputString = "";
                currentShippingBinItemForQuantity = null;
            } else if (keyCode == KeyEvent.VK_ESCAPE) {
                shippingBinInputMode = "selecting_player_item";
                shippingBinQuantityInputString = "";
                currentShippingBinItemForQuantity = null;
                setShippingBinFeedback("Quantity input cancelled.", false);
            }
        } else { 
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
        g2d.setFont(DIALOG_FONT); 
        String title = "Shipping Bin";
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(title);
        g2d.drawString(title, shippingBinPanelRect.x + (shippingBinPanelRect.width - titleWidth) / 2, shippingBinPanelRect.y + 30);

        // Close Button 
        g2d.setFont(SHIPPING_BIN_FONT);
        g2d.drawString("[Esc] Close", shippingBinCloseButtonRect.x + 5, shippingBinCloseButtonRect.y + 20);

        // Player Inventory List 
        g2d.setFont(SHIPPING_BIN_FONT);
        g2d.drawString("Your Items:", playerItemsListRect.x, playerItemsListRect.y - 5);
        g2d.drawRect(playerItemsListRect.x, playerItemsListRect.y, playerItemsListRect.width, playerItemsListRect.height);

        g2d.setFont(SHIPPING_BIN_ITEM_FONT);
        if (playerSellableItems != null && !playerSellableItems.isEmpty()) {
            for (int i = 0; i < playerSellableItems.size(); i++) {
                Item item = playerSellableItems.get(i);
                String itemName = item.getName();
                int quantity = farmModel.getPlayer().getInventory().getItemCount(item);
                String displayText = String.format("%-15s x%d", itemName, quantity);
                if (i == currentPlayerItemSelectionIndex) {
                    g2d.setColor(SHIPPING_BIN_HIGHLIGHT_COLOR);
                    g2d.fillRect(playerItemsListRect.x + 1, playerItemsListRect.y + 1 + (i * 20), playerItemsListRect.width - 2, 20);
                    g2d.setColor(Color.BLACK); 
                } else {
                    g2d.setColor(SHIPPING_BIN_TEXT_COLOR);
                }
                g2d.drawString(displayText, playerItemsListRect.x + 5, playerItemsListRect.y + 15 + (i * 20));
            }
        } else {
            g2d.setColor(SHIPPING_BIN_TEXT_COLOR);
            g2d.drawString("No sellable items.", playerItemsListRect.x + 5, playerItemsListRect.y + 20);
        }

        // Items in Bin List
        g2d.setColor(SHIPPING_BIN_TEXT_COLOR); 
        g2d.setFont(SHIPPING_BIN_FONT);
        int slotsUsed = 0;
        if (farmModel != null && farmModel.getShippingBin() != null) {
            slotsUsed = farmModel.getShippingBin().getItems().size(); 
        }
        g2d.drawString(String.format("In Bin (Slots: %d/%d):", slotsUsed, ShippingBin.MAX_UNIQUE_SLOTS), binItemsListRect.x, binItemsListRect.y - 5);
        g2d.drawRect(binItemsListRect.x, binItemsListRect.y, binItemsListRect.width, binItemsListRect.height);

        g2d.setFont(SHIPPING_BIN_ITEM_FONT);
        if (farmModel.getShippingBin() != null && !farmModel.getShippingBin().getItems().isEmpty()) { 
            int i = 0;
            for (Map.Entry<Item, Integer> entry : farmModel.getShippingBin().getItems().entrySet()) {
                Item item = entry.getKey();
                int quantityInBin = entry.getValue();
                String displayText = String.format("%-15s x%d", item.getName(), quantityInBin);
                g2d.setColor(SHIPPING_BIN_TEXT_COLOR);
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
            g2d.fillRect(shippingBinQuantityRect.x, shippingBinQuantityRect.y, shippingBinQuantityRect.width, shippingBinQuantityRect.height);
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
        
        currentPlayerItemSelectionIndex = 0;
        shippingBinInputMode = "selecting_player_item";
        shippingBinQuantityInputString = "";
        currentShippingBinItemForQuantity = null;
        setShippingBinFeedback("Select an item to add to the bin. [Enter] to set quantity.", false);
        repaint();
    }

    // Method to be called by GameController to close the UI
    public void closeShippingBinUI() {
        shippingBinFeedbackMessage = ""; 
        repaint();
    }

    // Helper to get sellable items from player's inventory
    private List<Item> getSellableItemsFromInventory() {
        List<Item> sellable = new ArrayList<>();
        if (farmModel.getPlayer() != null && farmModel.getPlayer().getInventory() != null) {
            for (Map.Entry<Item, Integer> entry : farmModel.getPlayer().getInventory().getItems().entrySet()) {
                Item item = entry.getKey();
                if (item instanceof Crop || item instanceof Fish || item instanceof Food || item instanceof MiscItem) {
                    if (entry.getValue() > 0) {
                        sellable.add(item); 
                    }
                }
            }
        }
        // Sort alphabetically
        sellable.sort((i1, i2) -> i1.getName().compareToIgnoreCase(i2.getName()));
        return sellable;
    }

    // Call this when GameController confirms an item was added to bin
    public void itemAddedToBinSuccessfully(Item item, int quantityAdded) {
        playerSellableItems = getSellableItemsFromInventory();
        
        shippingBinInputMode = "selecting_player_item";
        shippingBinQuantityInputString = "";
        currentShippingBinItemForQuantity = null;
        
        setShippingBinFeedback(quantityAdded + " " + item.getName() + " added to bin.", false);
        
        if (currentPlayerItemSelectionIndex >= playerSellableItems.size() && !playerSellableItems.isEmpty()) {
            currentPlayerItemSelectionIndex = playerSellableItems.size() - 1;
        } else if (playerSellableItems.isEmpty()){
            currentPlayerItemSelectionIndex = 0;
        }
        repaint();
    }

    public void shippingBinActionFailed(String errorMessage) {
        setShippingBinFeedback(errorMessage, true);
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
        }

        if (actionProcessed) {
            return true;
        } else {
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
            if (currentMap instanceof com.spakborhills.model.Store) {
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
        this.storeScrollOffset = 0; 
        this.isStoreUiActive = true; 
        farmModel.setCurrentGameState(GameState.STORE_UI); 
        repaint();
    }

    private boolean tryOpenShippingBinDialog() {
        Player player = farmModel.getPlayer();
        if (!(player.getCurrentMap() instanceof FarmMap)) {
            return false; 
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
                gameController.requestOpenShippingBin(); 
                return true;
            } else {
                setGeneralGameMessage("You have already used the shipping bin today.", false);
                return false; 
            }
        } else {
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
        currentY += 10;

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
        currentY += 20;

        // New Day Info
        List<String> newDayLines = getWrappedText(endOfDayNewDayInfo, textWidth, g2d.getFontMetrics());
        for (String line : newDayLines) {
            g2d.drawString(line, textX, currentY);
            currentY += g2d.getFontMetrics().getHeight();
        }
        currentY += 30; 

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

        int currentY = worldMapPanelRect.y + (int)(40 * this.scaleFactor); 
        int textX = worldMapPanelRect.x + (int)(30 * this.scaleFactor); 

        // Title
        g2d.setFont(this.WORLD_MAP_FONT_TITLE); 
        g2d.setColor(WORLD_MAP_TEXT_COLOR);
        String title = "Pilih Tujuan";
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(title);
        g2d.drawString(title, worldMapPanelRect.x + (worldMapPanelRect.width - titleWidth) / 2, currentY);
        currentY += fmTitle.getHeight() + (int)(25 * this.scaleFactor); 

        // Destination List
        g2d.setFont(this.WORLD_MAP_FONT_ITEM);
        FontMetrics fmItem = g2d.getFontMetrics();
        int scaledItemVerticalPadding = Math.max(2, (int)(5 * this.scaleFactor));
        int itemLineHeight = fmItem.getHeight() + scaledItemVerticalPadding;

        // Calculate available space
        float baseInstructionFontSize = 18f;
        float scaledInstructionFontSize = Math.max(9f, (float)(baseInstructionFontSize * this.scaleFactor));
        Font instructionFont = this.WORLD_MAP_FONT_ITEM.deriveFont(Font.ITALIC, scaledInstructionFontSize);
        FontMetrics fmInstructions = g2d.getFontMetrics(instructionFont);
        
        int baseInstructionBottomOffset = 25;
        int scaledInstructionBottomOffset = Math.max(12, (int)(baseInstructionBottomOffset * this.scaleFactor));
        int instructionBaselineY = worldMapPanelRect.y + worldMapPanelRect.height - scaledInstructionBottomOffset;
        
        int listBottomBoundaryY = instructionBaselineY - fmInstructions.getHeight() - (int)(5 * this.scaleFactor); 

        for (int i = 0; i < worldMapDestinations.size(); i++) {
            if (currentY + fmItem.getDescent() > listBottomBoundaryY) {
                 if (i > 0) { 
                    int ellipsisY = currentY;
                    if ( (currentY - itemLineHeight + fmItem.getDescent()) <= listBottomBoundaryY ) {
                        ellipsisY = currentY - itemLineHeight;
                    }
                     if (ellipsisY < listBottomBoundaryY + fmItem.getAscent()) { 
                        g2d.setFont(this.WORLD_MAP_FONT_ITEM); 
                        g2d.setColor(WORLD_MAP_TEXT_COLOR);    
                        g2d.drawString("...", textX, ellipsisY);
                     }
                 }
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
        g2d.setFont(instructionFont); 
        g2d.setColor(WORLD_MAP_TEXT_COLOR);
        String instructions = "[Up/Down] Pilih  [Enter] Pergi  [Esc] Batal";
        int instructionsWidth = fmInstructions.stringWidth(instructions);
        g2d.drawString(instructions, worldMapPanelRect.x + (worldMapPanelRect.width - instructionsWidth) / 2, instructionBaselineY);
    }

    private void handleWorldMapSelectionInput(int keyCode) {
        if (farmModel.getCurrentGameState() != GameState.WORLD_MAP_SELECTION) return;

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
                                com.spakborhills.model.Enum.LocationType.valueOf(chosenDestination.toUpperCase().replace(" ", "_")); // Ensure format matches enum
                        
                        boolean travelSuccess = gameController.requestVisit(destinationEnum); 
                        if (travelSuccess) {
                            playInGameMusic(); // Explicitly play if IN_GAME is set by controller
                        } else {
                            setGeneralGameMessage("Cannot travel to " + chosenDestination + " at this time.", true);
                            if (farmModel.getCurrentGameState() == GameState.IN_GAME) {
                                playInGameMusic();
                            }
                        }
                    } catch (IllegalArgumentException ex) {
                        System.err.println("GamePanel: Invalid destination string chosen from UI: " + chosenDestination + " Error: " + ex.getMessage());
                        setGeneralGameMessage("Error: Invalid location '" + chosenDestination + "' selected.", true);
                    }
                }
                break;
            case KeyEvent.VK_ESCAPE:
                farmModel.setCurrentGameState(GameState.IN_GAME);
                playInGameMusic(); 
                break;
        }
        repaint();
    }

    public void startGame() { 
        if (farmModel != null && farmModel.getCurrentTime() != null) {
            farmModel.setCurrentGameState(GameState.IN_GAME); 
        }
        loadTileImages(); 
        requestFocusInWindow();
        startAllTimers(); 
    }

    // New method to draw the inventory view UI
    private void drawInventoryViewUI(Graphics2D g2d) {
        if (farmModel.getCurrentGameState() != GameState.INVENTORY_VIEW) {
            return;
        }

        // Panel Background
        g2d.setColor(INVENTORY_BG_COLOR);
        g2d.fill(inventoryPanelRect);
        g2d.setColor(INVENTORY_TEXT_COLOR.brighter());
        g2d.draw(inventoryPanelRect);

        // Title
        g2d.setFont(DIALOG_FONT); 
        g2d.setColor(INVENTORY_TEXT_COLOR);
        String title = "Inventory";
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(title);
        g2d.drawString(title, inventoryPanelRect.x + (inventoryPanelRect.width - titleWidth) / 2, inventoryPanelRect.y + 25);

        // Instructions
        g2d.setFont(INVENTORY_FONT.deriveFont(Font.ITALIC));
        String instructions = "[Arrows] Navigate | [Enter/E] Select Item | [Esc/I] Close";
        FontMetrics fmInstructions = g2d.getFontMetrics();
        int instructionsWidth = fmInstructions.stringWidth(instructions);
        g2d.drawString(instructions, inventoryPanelRect.x + (inventoryPanelRect.width - instructionsWidth) / 2, inventoryPanelRect.y + inventoryPanelRect.height - 15);


        // Draw Inventory Grid
        List<Item> playerItems = gameController.getPlayerInventoryItems(); 

        for (int row = 0; row < INVENTORY_ROWS; row++) {
            for (int col = 0; col < INVENTORY_COLS; col++) {
                int cellX = inventoryGridRect.x + col * (INVENTORY_CELL_SIZE + INVENTORY_PADDING);
                int cellY = inventoryGridRect.y + row * (INVENTORY_CELL_SIZE + INVENTORY_PADDING);

                g2d.setColor(INVENTORY_CELL_COLOR);
                g2d.fillRect(cellX, cellY, INVENTORY_CELL_SIZE, INVENTORY_CELL_SIZE);
                g2d.setColor(INVENTORY_TEXT_COLOR.darker());
                g2d.drawRect(cellX, cellY, INVENTORY_CELL_SIZE, INVENTORY_CELL_SIZE);

                int itemIndex = row * INVENTORY_COLS + col;
                if (playerItems != null && itemIndex < playerItems.size()) {
                    Item item = playerItems.get(itemIndex);
                    if (item != null) {
                        g2d.setFont(INVENTORY_FONT);
                        g2d.setColor(INVENTORY_TEXT_COLOR);
                        String itemNameAbbrev = item.getName().length() > 7 ? item.getName().substring(0, 6) + "." : item.getName();
                        FontMetrics itemFm = g2d.getFontMetrics();
                        g2d.drawString(itemNameAbbrev, cellX + 5, cellY + itemFm.getAscent() + 5);

                        // Draw quantity
                        int quantity = farmModel.getPlayer().getInventory().getItemCount(item);
                         if (quantity > 0) {
                            g2d.setFont(INVENTORY_FONT.deriveFont(Font.BOLD));
                            String qtyStr = "x" + quantity;
                            FontMetrics qtyFm = g2d.getFontMetrics();
                            g2d.drawString(qtyStr, cellX + INVENTORY_CELL_SIZE - qtyFm.stringWidth(qtyStr) - 5, cellY + INVENTORY_CELL_SIZE - qtyFm.getDescent() - 5);
                        }
                    }
                }

                // Highlight selected cell
                if (row == currentInventoryRow && col == currentInventoryCol) {
                    g2d.setColor(INVENTORY_HIGHLIGHT_COLOR);
                    g2d.setStroke(new BasicStroke(2)); 
                    g2d.drawRect(cellX, cellY, INVENTORY_CELL_SIZE, INVENTORY_CELL_SIZE);
                    g2d.setStroke(new BasicStroke(1)); 
                }
            }
        }
    }

    // New method to handle input for the inventory view
    private void handleInventoryViewInput(int keyCode) {
        if (farmModel.getCurrentGameState() != GameState.INVENTORY_VIEW) {
            return;
        }

        switch (keyCode) {
            case KeyEvent.VK_UP:
                if (currentInventoryRow > 0) currentInventoryRow--;
                else currentInventoryRow = INVENTORY_ROWS - 1; 
                break;
            case KeyEvent.VK_DOWN:
                if (currentInventoryRow < INVENTORY_ROWS - 1) currentInventoryRow++;
                else currentInventoryRow = 0; 
                break;
            case KeyEvent.VK_LEFT:
                if (currentInventoryCol > 0) currentInventoryCol--;
                else currentInventoryCol = INVENTORY_COLS - 1; 
                break;
            case KeyEvent.VK_RIGHT:
                if (currentInventoryCol < INVENTORY_COLS - 1) currentInventoryCol++;
                else currentInventoryCol = 0; 
                break;
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_E: // Select item
                List<Item> playerItems = gameController.getPlayerInventoryItems();
                int selectedItemIndex = currentInventoryRow * INVENTORY_COLS + currentInventoryCol;
                if (playerItems != null && selectedItemIndex < playerItems.size()) {
                    Item itemToSelect = playerItems.get(selectedItemIndex);
                    if (itemToSelect != null) {
                        gameController.setSelectedItem(itemToSelect); 
                        setGeneralGameMessage(itemToSelect.getName() + " selected.", false);
                    }
                }
                farmModel.setCurrentGameState(GameState.IN_GAME); 
                break;
            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_I: 
                farmModel.setCurrentGameState(GameState.IN_GAME);
                playInGameMusic(); 
                break;
        }
    }

    // New method to draw the Player Info UI
    private void drawPlayerInfoUI(Graphics2D g2d) {
        if (farmModel.getCurrentGameState() != GameState.PLAYER_INFO_VIEW) {
            return;
        }

        // Panel Background
        g2d.setColor(PLAYER_INFO_BG_COLOR);
        g2d.fill(playerInfoPanelRect);
        g2d.setColor(PLAYER_INFO_TEXT_COLOR.brighter());
        g2d.draw(playerInfoPanelRect);

        int currentY = playerInfoPanelRect.y + 40;
        int textX = playerInfoPanelRect.x + 30;
        int textWidth = playerInfoPanelRect.width - 60;

        // Title
        g2d.setFont(PLAYER_INFO_FONT_TITLE);
        g2d.setColor(PLAYER_INFO_TEXT_COLOR);
        String title = "Player Information";
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleWidthVal = fmTitle.stringWidth(title);
        g2d.drawString(title, playerInfoPanelRect.x + (playerInfoPanelRect.width - titleWidthVal) / 2, currentY);
        currentY += fmTitle.getHeight() + 20;

        g2d.setFont(PLAYER_INFO_FONT_TEXT);
        if (farmModel != null && farmModel.getPlayer() != null) {
            Player player = farmModel.getPlayer();
            List<String> infoLines = new ArrayList<>();
            infoLines.add("Name: " + player.getName());
            infoLines.add("Gender: " + player.getGender().toString());
            infoLines.add("Energy: " + player.getEnergy() + "/" + Player.MAX_ENERGY);
            
            // Add favorite item display
            String favoriteItem = player.getFavoriteItemName();
            if (favoriteItem != null && !favoriteItem.trim().isEmpty()) {
                infoLines.add("Favorite Item: " + favoriteItem);
            } else {
                infoLines.add("Favorite Item: None");
            }
            
            infoLines.add("Gold: " + player.getGold() + " G");
            String partnerName = "None";
            NPC partner = player.getPartner();
            if (partner != null) {
                partnerName = partner.getName() + " (" + partner.getRelationshipStatus().toString() + ")";
            }
            infoLines.add("Partner: " + partnerName);

            for (String line : infoLines) {
                List<String> wrappedLines = getWrappedText(line, textWidth, g2d.getFontMetrics());
                for (String wrappedLine : wrappedLines) {
                    g2d.drawString(wrappedLine, textX, currentY);
                    currentY += g2d.getFontMetrics().getHeight();
                }
            }
        }
        currentY += 20;

        // Prompt to close
        g2d.setFont(PLAYER_INFO_FONT_TEXT.deriveFont(Font.ITALIC));
        String continuePrompt = "Press J or Esc to close...";
        FontMetrics fmPrompt = g2d.getFontMetrics();
        int promptWidth = fmPrompt.stringWidth(continuePrompt);
        g2d.drawString(continuePrompt, playerInfoPanelRect.x + (playerInfoPanelRect.width - promptWidth) / 2, playerInfoPanelRect.y + playerInfoPanelRect.height - 30);
    }

    // New method to draw the Statistics UI
    private void drawStatisticsUI(Graphics2D g2d) {
        if (farmModel.getCurrentGameState() != GameState.STATISTICS_VIEW) {
            return;
        }

        if (gameController != null) {
            gameController.refreshStatisticsData();
        }

        String currentSummaryText;
        if (farmModel != null && farmModel.getStatistics() != null) {
            String summaryFromModel = farmModel.getStatistics().getSummary();
            if (summaryFromModel == null) {
                currentSummaryText = "Statistics summary is null. Unable to display data.";
            } else if (summaryFromModel.trim().isEmpty()) {
                currentSummaryText = "No statistics data to display at the moment.";
            } else {
                currentSummaryText = summaryFromModel;
            }
        } else {
            currentSummaryText = "Error: Statistics data is not available (FarmModel or Statistics object is null).";
        }

        // Panel Background - Using PLAYER_INFO_BG_COLOR for consistency
        g2d.setColor(PLAYER_INFO_BG_COLOR);
        g2d.fill(statisticsPanelRect);
        g2d.setColor(PLAYER_INFO_TEXT_COLOR.brighter());
        g2d.draw(statisticsPanelRect);

        int textX = statisticsPanelRect.x + 30;
        int textWidth = statisticsPanelRect.width - 60;
        int textAreaHeight = statisticsPanelRect.height - 100;
 
        // Title - Using PLAYER_INFO_FONT_TITLE and PLAYER_INFO_TEXT_COLOR
        g2d.setFont(PLAYER_INFO_FONT_TITLE); 
        g2d.setColor(PLAYER_INFO_TEXT_COLOR);
        String title = "Game Statistics";
        if (statisticsShown) { 
            title = "End Game Statistics";
        }
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleWidthVal = fmTitle.stringWidth(title);
        g2d.drawString(title, statisticsPanelRect.x + (statisticsPanelRect.width - titleWidthVal) / 2, statisticsPanelRect.y + 40);

        // Calculate visible area for content based on scroll position
        int initialY = statisticsPanelRect.y + 80;
        int currentY = initialY - statsScrollOffset;

        // Statistics Text - Using PLAYER_INFO_FONT_TEXT and PLAYER_INFO_TEXT_COLOR
        g2d.setFont(PLAYER_INFO_FONT_TEXT);
        g2d.setColor(PLAYER_INFO_TEXT_COLOR);

        // Create clipping region for text area to prevent overflow outside panel
        Shape originalClip = g2d.getClip();
        g2d.setClip(statisticsPanelRect.x + 10, initialY, statisticsPanelRect.width - 20, textAreaHeight);

        FontMetrics fmText = g2d.getFontMetrics();
        String[] lines = currentSummaryText.split("\n"); 

        for (String line : lines) {
            List<String> wrappedLines = getWrappedText(line, textWidth, fmText);
            for (String wrappedLine : wrappedLines) {
                if (currentY >= initialY - fmText.getHeight() && currentY <= initialY + textAreaHeight) {
                    g2d.drawString(wrappedLine, textX, currentY);
                }
                currentY += fmText.getHeight();
            }
        }
        
        // Restore original clip
        g2d.setClip(originalClip);
        
        // Draw scrollbar if content exceeds visible area
        int totalContentHeight = lines.length * fmText.getHeight() * 2; 
        if (totalContentHeight > textAreaHeight) {
            int scrollBarWidth = 8;
            int scrollBarX = statisticsPanelRect.x + statisticsPanelRect.width - scrollBarWidth - 10;
            int scrollBarHeight = statisticsPanelRect.height - 100;
            
            // Background track
            g2d.setColor(new Color(0, 0, 0, 80));
            g2d.fillRect(scrollBarX, initialY, scrollBarWidth, scrollBarHeight);
            
            // Calculate thumb position and size
            float contentRatio = (float)textAreaHeight / totalContentHeight;
            int thumbHeight = Math.max(30, (int)(scrollBarHeight * contentRatio));
            int maxScrollOffset = totalContentHeight - textAreaHeight;
            float scrollRatio = maxScrollOffset > 0 ? (float)statsScrollOffset / maxScrollOffset : 0;
            int thumbY = initialY + (int)(scrollRatio * (scrollBarHeight - thumbHeight));
            
            // Draw thumb
            g2d.setColor(new Color(255, 255, 255, 180));
            g2d.fillRect(scrollBarX, thumbY, scrollBarWidth, thumbHeight);
        }

        // Prompt to close - Styled like Player Info
        g2d.setFont(PLAYER_INFO_FONT_TEXT.deriveFont(Font.ITALIC));
        g2d.setColor(PLAYER_INFO_TEXT_COLOR); 
        String continuePrompt = "Press O or Esc to close, ↑↓ to scroll...";
        FontMetrics fmPrompt = g2d.getFontMetrics();
        int promptWidth = fmPrompt.stringWidth(continuePrompt);
        g2d.drawString(continuePrompt, statisticsPanelRect.x + (statisticsPanelRect.width - promptWidth) / 2, statisticsPanelRect.y + statisticsPanelRect.height - 30);
    }

    // New method to handle input for Statistics view
    private void handleStatisticsViewInput(int keyCode) {
        if (farmModel.getCurrentGameState() != GameState.STATISTICS_VIEW) {
            return;
        }
        
        switch (keyCode) {
            case KeyEvent.VK_O:
            case KeyEvent.VK_ESCAPE:
                statsScrollOffset = 0;
                farmModel.setCurrentGameState(GameState.IN_GAME);
                playInGameMusic(); 
                break;
            case KeyEvent.VK_R:
                if (gameController != null) {
                    gameController.refreshStatisticsData();
                    repaint();
                }
                break;
            case KeyEvent.VK_UP:
                statsScrollOffset = Math.max(0, statsScrollOffset - STATS_SCROLL_AMOUNT);
                repaint();
                break;
            case KeyEvent.VK_DOWN:
                statsScrollOffset += STATS_SCROLL_AMOUNT;
                repaint();
                break;
        }
    }

    private void initMenuMusic() {
        System.out.println("DEBUG: initMenuMusic() method CALLED."); 
        try {
            AudioInputStream audioInputStream = null;
            InputStream resourceStream = getClass().getResourceAsStream("/assets/menu/music.wav");
            if (resourceStream != null) {
                System.out.println("Attempting to load menu music from resources...");
                audioInputStream = AudioSystem.getAudioInputStream(resourceStream); 
            } else {
                System.out.println("Menu music resource not found, trying absolute path...");
                File audioFile = new File("G:\\codebro\\coding_itb\\tubesoop\\repository-tugas-besar-oop-2025\\src\\main\\resources\\assets\\menu\\music.wav");
                if (audioFile.exists()) {
                    audioInputStream = AudioSystem.getAudioInputStream(audioFile);
                } else {
                    System.err.println("Menu music file not found at absolute path either: " + audioFile.getAbsolutePath());
                    return;
                }
            }

            menuMusicClip = AudioSystem.getClip();
            menuMusicClip.open(audioInputStream);
            menuMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            System.out.println("Menu music loaded successfully.");

        } catch (UnsupportedAudioFileException e) {
            System.err.println("Error: Menu music file format not supported. " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error: Could not read menu music file. " + e.getMessage());
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            System.err.println("Error: Audio line for menu music unavailable. " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) { 
            System.err.println("An unexpected error occurred while loading menu music: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void playMenuMusic() { 
        stopInGameMusicSavingPosition(); 

        if (menuMusicClip != null) {
            if (!menuMusicClip.isRunning()) {
                try {
                    menuMusicClip.setFramePosition((int) menuMusicPosition); 
                    menuMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                    menuMusicClip.start();
                } catch (Exception e) {
                    
                }
            }
            this.isMenuMusicPlaying = menuMusicClip.isRunning(); 
        } else {
            this.isMenuMusicPlaying = false;
        }
    }

    public void stopMenuMusic() { 
        System.out.println("Attempting to stop MENU music.");
        if (menuMusicClip != null) {
            if (menuMusicClip.isRunning()) {
                menuMusicClip.stop(); 
                System.out.println("  stopMenuMusic: Stopped menuMusicClip.");
            }
            menuMusicPosition = 0; 
            isMenuMusicPlaying = false;
        } else {
            isMenuMusicPlaying = false;
            System.err.println("  stopMenuMusic: menuMusicClip is NULL.");
        }
    }

    // Added for in-game music
    private void initInGameMusic() {
        System.out.println("DEBUG: initInGameMusic() method CALLED.");
        try {
            AudioInputStream audioInputStream = null;
            InputStream resourceStream = getClass().getResourceAsStream("/assets/menu/ingame.wav"); 
            if (resourceStream != null) {
                System.out.println("Attempting to load in-game music from resources...");
                audioInputStream = AudioSystem.getAudioInputStream(resourceStream);
            } else {
                System.out.println("In-game music resource not found, trying absolute path...");
                File audioFile = new File("G:/codebro/coding_itb/tubesoop/repository-tugas-besar-oop-2025/src/main/resources/assets/menu/ingame.wav");
                if (audioFile.exists()) {
                    audioInputStream = AudioSystem.getAudioInputStream(audioFile);
                } else {
                    System.err.println("In-game music file not found at absolute path either: " + audioFile.getAbsolutePath());
                    return;
                }
            }

            inGameMusicClip = AudioSystem.getClip();
            inGameMusicClip.open(audioInputStream);
            inGameMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            System.out.println("In-game music loaded successfully.");

        } catch (UnsupportedAudioFileException e) {
            System.err.println("Error: In-game music file format not supported. " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error: Could not read in-game music file. " + e.getMessage());
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            System.err.println("Error: Audio line for in-game music unavailable. " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while loading in-game music: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void playInGameMusic() { 
        stopMenuMusicSavingPosition(); 

        if (inGameMusicClip != null) {
            if (!inGameMusicClip.isRunning()) {
                try {
                    inGameMusicClip.setFramePosition((int) inGameMusicPosition);
                    inGameMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                    inGameMusicClip.start();
                } catch (Exception e) {
                    
                }
            }
            this.isInGameMusicPlaying = inGameMusicClip.isRunning();
        } else {
            this.isInGameMusicPlaying = false;
        }
    }

    // Added for in-game music
    public void stopInGameMusic() { 
        if (inGameMusicClip != null) {
            if (inGameMusicClip.isRunning()) {
                inGameMusicClip.stop(); 
            }
            inGameMusicPosition = 0; 
            isInGameMusicPlaying = false;
        } else {
            isInGameMusicPlaying = false;
        }
    }

    // Add this new method
    public void stopMusic() {
        stopMenuMusic();
        stopInGameMusic();
    }

    // New method to draw the Pause Menu UI
    private void drawPauseMenu(Graphics2D g2d) {
        if (farmModel.getCurrentGameState() != GameState.PAUSE_MENU) {
            return;
        }

        // Panel Background (covers the whole game area, not just the menu box, to dim the background)
        g2d.setColor(PAUSE_MENU_BG_COLOR);
        g2d.fillRect(0, 0, getWidth(), getHeight()); 

        // Menu Box
        g2d.setColor(new Color(30,30,70, 230)); 
        g2d.fill(pauseMenuPanelRect);
        g2d.setColor(PAUSE_MENU_TEXT_COLOR.brighter());
        g2d.draw(pauseMenuPanelRect);


        int currentY = pauseMenuPanelRect.y + 40;
        int textX = pauseMenuPanelRect.x + 30;

        // Title
        g2d.setFont(PAUSE_MENU_FONT_TITLE);
        g2d.setColor(PAUSE_MENU_TEXT_COLOR);
        String title = "Paused";
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(title);
        g2d.drawString(title, pauseMenuPanelRect.x + (pauseMenuPanelRect.width - titleWidth) / 2, currentY);
        currentY += fmTitle.getHeight() + 25; 

        // Menu Items
        g2d.setFont(PAUSE_MENU_FONT_ITEM);
        FontMetrics fmItem = g2d.getFontMetrics();
        int itemLineHeight = fmItem.getHeight() + 10; 

        for (int i = 0; i < pauseMenuOptions.length; i++) {
            String itemName = pauseMenuOptions[i];
            if (i == currentPauseMenuSelection) {
                g2d.setColor(PAUSE_MENU_HIGHLIGHT_COLOR);
                g2d.drawString("> " + itemName, textX, currentY);
                g2d.setColor(PAUSE_MENU_TEXT_COLOR);
            } else {
                g2d.drawString("  " + itemName, textX, currentY);
            }
            currentY += itemLineHeight;
        }
    }

    // New method to handle input for the Pause Menu
    private void handlePauseMenuInput(int keyCode) {
        if (farmModel.getCurrentGameState() != GameState.PAUSE_MENU) return;

        switch (keyCode) {
            case KeyEvent.VK_UP:
                currentPauseMenuSelection--;
                if (currentPauseMenuSelection < 0) {
                    currentPauseMenuSelection = pauseMenuOptions.length - 1;
                }
                break;
            case KeyEvent.VK_DOWN:
                currentPauseMenuSelection++;
                if (currentPauseMenuSelection >= pauseMenuOptions.length) {
                    currentPauseMenuSelection = 0;
                }
                break;
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_E: 
                String selectedOption = pauseMenuOptions[currentPauseMenuSelection];
                switch (selectedOption) {
                    case "Resume":
                        farmModel.setCurrentGameState(GameState.IN_GAME);
                        startGameTimer(); 
                        if (inGameMusicClip != null && !inGameMusicClip.isRunning()) {
                            inGameMusicClip.setFramePosition((int) inGameMusicPosition);
                            inGameMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                            inGameMusicClip.start();
                        }
                        break;
                    case "Save Game":
                        if (gameController != null) {
                            String[] options = {"Quick Save", "Save As", "Overwrite Existing Save", "Cancel"};
                            int choice = JOptionPane.showOptionDialog(
                                this,
                                "Choose Save Option",
                                "Save Game",
                                JOptionPane.DEFAULT_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                options,
                                options[0]
                            );
                            
                            if (choice == 0) { // Quick Save
                                gameController.saveGame();
                                setGeneralGameMessage("Game Saved!", false);
                            } else if (choice == 1) { // Save As
                                String saveFileName = JOptionPane.showInputDialog(
                                    this,
                                    "Enter save file name (without extension):\n(Special characters will be replaced with underscores)",
                                    "Save Game As",
                                    JOptionPane.PLAIN_MESSAGE
                                );
                                
                                if (saveFileName != null && !saveFileName.trim().isEmpty()) {
                                    String actualFileName = gameController.saveGameAs(saveFileName);
                                    if (actualFileName != null) {
                                        JOptionPane.showMessageDialog(
                                            this,
                                            "Game saved successfully as:\n" + actualFileName + "\n\n" +
                                            "Note: The filename may have been modified to ensure compatibility.",
                                            "Save Complete",
                                            JOptionPane.INFORMATION_MESSAGE
                                        );
                                        setGeneralGameMessage("Game Saved as: " + actualFileName, false);
                                    } else {
                                        setGeneralGameMessage("Error: Failed to save game.", true);
                                    }
                                }
                            } else if (choice == 2) {
                                List<SaveLoadManager.SaveSlot> saveSlots = gameController.getSaveSlots();
                                
                                if (saveSlots.isEmpty()) {
                                    JOptionPane.showMessageDialog(
                                        this,
                                        "No save files found to overwrite.",
                                        "Overwrite Save",
                                        JOptionPane.INFORMATION_MESSAGE
                                    );
                                } else {
                                    JComboBox<SaveLoadManager.SaveSlot> saveComboBox = new JComboBox<>();
                                    DefaultComboBoxModel<SaveLoadManager.SaveSlot> comboModel = new DefaultComboBoxModel<>();
                                    
                                    for (SaveLoadManager.SaveSlot save : saveSlots) {
                                        comboModel.addElement(save);
                                    }
                                    
                                    saveComboBox.setModel(comboModel);
                                    saveComboBox.setRenderer(new DefaultListCellRenderer() {
                                        @Override
                                        public Component getListCellRendererComponent(JList<?> list, Object value, 
                                                int index, boolean isSelected, boolean cellHasFocus) {
                                            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                                            if (value instanceof SaveLoadManager.SaveSlot) {
                                                SaveLoadManager.SaveSlot save = (SaveLoadManager.SaveSlot) value;
                                                setText(String.format("%s - %s's %s - %s Day %d, Year %d",
                                                    save.getFileName(),
                                                    save.getPlayerName(),
                                                    save.getFarmName(),
                                                    save.getSeason(),
                                                    save.getDay(),
                                                    save.getYear()
                                                ));
                                            }
                                            return this;
                                        }
                                    });
                                    
                                    // Create a panel for the dialog
                                    JPanel panel = new JPanel(new BorderLayout(10, 10));
                                    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                                    panel.add(new JLabel("Select a save file to overwrite:"), BorderLayout.NORTH);
                                    panel.add(saveComboBox, BorderLayout.CENTER);
                                    
                                    // Show the dialog
                                    int result = JOptionPane.showConfirmDialog(
                                        this, 
                                        panel, 
                                        "Overwrite Save", 
                                        JOptionPane.OK_CANCEL_OPTION,
                                        JOptionPane.QUESTION_MESSAGE
                                    );
                                    
                                    if (result == JOptionPane.OK_OPTION) {
                                        SaveLoadManager.SaveSlot selectedSave = 
                                            (SaveLoadManager.SaveSlot) saveComboBox.getSelectedItem();
                                        
                                        if (selectedSave != null) {
                                            // Confirm overwrite
                                            int confirm = JOptionPane.showConfirmDialog(
                                                this,
                                                "Are you sure you want to overwrite:\n" + 
                                                selectedSave.getFileName() + "\n" +
                                                "Player: " + selectedSave.getPlayerName() + "\n" +
                                                "Farm: " + selectedSave.getFarmName() + "\n" +
                                                "Date: " + selectedSave.getSeason() + " Day " + selectedSave.getDay() + ", Year " + selectedSave.getYear(),
                                                "Confirm Overwrite",
                                                JOptionPane.YES_NO_OPTION,
                                                JOptionPane.WARNING_MESSAGE
                                            );
                                            
                                            if (confirm == JOptionPane.YES_OPTION) {
                                                // Use the dedicated method for overwriting saves
                                                String actualFileName = gameController.overwriteSaveFile(selectedSave.getFileName());
                                                if (actualFileName != null) {
                                                    setGeneralGameMessage("Game Saved (Overwritten): " + actualFileName, false);
                                                } else {
                                                    setGeneralGameMessage("Error: Failed to overwrite save.", true);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    case "Manage Saves":
                        showManageSavesDialog();
                        break;
                    case "Exit to Main Menu":
                        stopInGameMusicSavingPosition(); 
                        inGameMusicPosition = 0;
                        
                        if (menuMusicClip != null && menuMusicClip.isRunning()) { 
                            menuMusicClip.stop();
                        }
                        menuMusicPosition = 0;   
                        isMenuMusicPlaying = false; 
                        
                        gameFrame.showMainMenu();
                        break;
                    case "Exit Game":
                        System.out.println("Exiting Spakbor Hills via pause menu.");
                        System.exit(0);
                        break;
                }
                break;
        }
    }

    // New method to stop in-game music, saving its position
    private void stopInGameMusicSavingPosition() {
        if (this.inGameMusicClip != null && this.inGameMusicClip.isRunning()) {
            this.inGameMusicPosition = this.inGameMusicClip.getFramePosition();
            this.inGameMusicClip.stop();
        }
        this.isInGameMusicPlaying = false;
    }

    // New method to stop menu music, saving its position
    private void stopMenuMusicSavingPosition() {
        if (this.menuMusicClip != null && this.menuMusicClip.isRunning()) {
            this.menuMusicPosition = this.menuMusicClip.getFramePosition();
            this.menuMusicClip.stop();
        }
        this.isMenuMusicPlaying = false; 
    }

    /**
     * Shows a dialog to manage save files (view, delete)
     * This can be called from the pause menu or elsewhere
     */
    public void showManageSavesDialog() {
        if (gameController == null) {
            setGeneralGameMessage("Error: Cannot access save system.", true);
            return;
        }
        
        List<SaveLoadManager.SaveSlot> saves = gameController.getSaveSlots();
        if (saves.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No save files found.", "Manage Saves", JOptionPane.INFORMATION_MESSAGE);
        } else {
            boolean keepManaging = true;
            while (keepManaging) {
                // Create a JComboBox dropdown with save files
                JComboBox<SaveLoadManager.SaveSlot> saveComboBox = new JComboBox<>();
                DefaultComboBoxModel<SaveLoadManager.SaveSlot> comboModel = new DefaultComboBoxModel<>();
                
                for (SaveLoadManager.SaveSlot save : saves) {
                    comboModel.addElement(save);
                }
                
                saveComboBox.setModel(comboModel);
                saveComboBox.setRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, 
                            int index, boolean isSelected, boolean cellHasFocus) {
                        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        if (value instanceof SaveLoadManager.SaveSlot) {
                            SaveLoadManager.SaveSlot save = (SaveLoadManager.SaveSlot) value;
                            setText(String.format("%s - %s's %s - %s Day %d, Year %d",
                                save.getFileName(),
                                save.getPlayerName(),
                                save.getFarmName(),
                                save.getSeason(),
                                save.getDay(),
                                save.getYear()
                            ));
                        }
                        return this;
                    }
                });
                
                // Create a panel for the dialog
                JPanel panel = new JPanel(new BorderLayout(10, 10));
                panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                panel.add(new JLabel("Select a save file to manage:"), BorderLayout.NORTH);
                panel.add(saveComboBox, BorderLayout.CENTER);
                
                // Show the dialog
                int result = JOptionPane.showConfirmDialog(
                    this, 
                    panel, 
                    "Manage Saves", 
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (result != JOptionPane.OK_OPTION) {
                    keepManaging = false;
                } else {
                    SaveLoadManager.SaveSlot selectedSave = (SaveLoadManager.SaveSlot) saveComboBox.getSelectedItem();
                    
                    if (selectedSave != null) {
                        String[] options = {"Delete", "Cancel"};
                        int action = JOptionPane.showOptionDialog(
                            this,
                            "Do you want to delete the selected save file?\n" + 
                            "Filename: " + selectedSave.getFileName() + "\n" +
                            "Player: " + selectedSave.getPlayerName() + "\n" +
                            "Farm: " + selectedSave.getFarmName() + "\n" +
                            "Date: " + selectedSave.getSeason() + " Day " + selectedSave.getDay() + ", Year " + selectedSave.getYear() + "\n" +
                            "Last Modified: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(selectedSave.getLastModified()),
                            "Delete Save",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            options,
                            options[1] 
                        );
                        
                        if (action == 0) { 
                            int confirm = JOptionPane.showConfirmDialog(
                                this,
                                "Are you sure you want to delete " + selectedSave.getFileName() + "?",
                                "Confirm Delete",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE
                            );
                            
                            if (confirm == JOptionPane.YES_OPTION) {
                                boolean deleted = gameController.deleteSaveFile(selectedSave.getFileName());
                                if (deleted) {
                                    JOptionPane.showMessageDialog(this,
                                        "Save file deleted successfully.",
                                        "Delete Save",
                                        JOptionPane.INFORMATION_MESSAGE
                                    );
                                    saves = gameController.getSaveSlots();
                                    if (saves.isEmpty()) {
                                        JOptionPane.showMessageDialog(this, "No more save files available.", "Manage Saves", JOptionPane.INFORMATION_MESSAGE);
                                        keepManaging = false;
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(this,
                                        "Failed to delete save file.",
                                        "Delete Save",
                                        JOptionPane.ERROR_MESSAGE
                                    );
                                }
                            }
                        } else {
                            keepManaging = false;
                        }
                    } else {
                        keepManaging = false;
                    }
                }
            }
        }
    }

    // Helper method to calculate how many items can be shown in the store
    private int getVisibleStoreItemCount() {
        if (storeItemListRect == null) return 5; 
        Graphics2D g2d = (Graphics2D) getGraphics();
        if (g2d == null) return 5; 
        
        g2d.setFont(this.STORE_ITEM_FONT_SCALED);
        FontMetrics fm = g2d.getFontMetrics();
        int itemLineHeight = fm.getHeight() + Math.max(1, (int)(2 * this.scaleFactor));
        
        int bottomReservedSpace = (itemLineHeight * 3) + 20;
        
        int visibleHeight = storeItemListRect.height - bottomReservedSpace;
        int visibleCount = visibleHeight / itemLineHeight;
        
        g2d.dispose();
        return Math.max(1, visibleCount - 2);
    }

    // Helper method to adjust scroll offset based on selection
    private void adjustStoreScrollOffset() {
        int visibleCount = getVisibleStoreItemCount();
        
        if (currentStoreItemSelectionIndex >= storeScrollOffset + visibleCount) {
            storeScrollOffset = currentStoreItemSelectionIndex - visibleCount + 1;
        }
        else if (currentStoreItemSelectionIndex < storeScrollOffset) {
            storeScrollOffset = currentStoreItemSelectionIndex;
        }
        
        storeScrollOffset = Math.max(0, Math.min(storeScrollOffset, Math.max(0, storeItemsForDisplay.size() - visibleCount)));
    }

    // New method to handle input for Player Info view
    private void handlePlayerInfoViewInput(int keyCode) {
        if (farmModel.getCurrentGameState() != GameState.PLAYER_INFO_VIEW) {
            return;
        }
        if (keyCode == KeyEvent.VK_J || keyCode == KeyEvent.VK_ESCAPE) {
            farmModel.setCurrentGameState(GameState.IN_GAME);
            playInGameMusic(); 
        }
    }
}
