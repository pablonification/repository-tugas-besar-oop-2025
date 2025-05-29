package com.spakborhills.view;

import com.spakborhills.model.Farm; 
import com.spakborhills.controller.GameController; 
import com.spakborhills.model.Player;
import com.spakborhills.model.Map.FarmMap;
import com.spakborhills.model.Map.WorldMap;
import com.spakborhills.model.NPC.NPC;
import com.spakborhills.model.Store;
import com.spakborhills.model.Util.EndGameStatistics;
import com.spakborhills.model.Util.GameTime;
import com.spakborhills.model.Util.PriceList;
import com.spakborhills.model.Util.Recipe;
import com.spakborhills.model.Util.ShippingBin;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Enum.Gender;
import com.spakborhills.Main; 
import com.spakborhills.util.SaveLoadManager; 
import com.spakborhills.data.SaveData; 
import com.spakborhills.model.Object.House; 

import javax.swing.*;
import java.awt.*; 
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import java.util.Collections;

public class GameFrame extends JFrame {

    private MainMenuPanel mainMenuPanel;
    private GamePanel gamePanel;
    private GameController gameController;
    private Farm farm; 
    private CardLayout cardLayout;
    private JPanel mainPanelContainer; 

    private static final String MAIN_MENU_PANEL_KEY = "MainMenu";
    private static final String GAME_PANEL_KEY = "GamePanel";

    public GameFrame() {
        setTitle("Spakbor Hills");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);

        mainPanelContainer = new JPanel();
        cardLayout = new CardLayout();
        mainPanelContainer.setLayout(cardLayout);

        mainMenuPanel = new MainMenuPanel(this);
        mainPanelContainer.add(mainMenuPanel, MAIN_MENU_PANEL_KEY);

        gamePanel = new GamePanel(null, gameController, this, dynamicTileSize, dynamicInfoPanelHeight);
        mainPanelContainer.add(gamePanel, GAME_PANEL_KEY); 

        add(mainPanelContainer);

        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();
        
        if (device.isFullScreenSupported()) {
            try {
                device.setFullScreenWindow(this);
            } catch (Exception e) {
                System.err.println("Error attempting to set full-screen mode: " + e.getMessage());
                fallbackToWindowedMode();
            }
        } else {
            System.err.println("Full-screen mode is not supported on this device.");
            fallbackToWindowedMode();
        }
        showMainMenu();
    }

    private void fallbackToWindowedMode() {
        setUndecorated(false); 
        pack();
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setVisible(true);
    }

        // Di dalam GameFrame, sebelum membuat GamePanel
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenHeight = screenSize.height;
    
        // Target persentase tinggi layar untuk jendela game
        double targetHeightRatio = 0.85; 
        int targetWindowHeight = (int) (screenHeight * targetHeightRatio);
    
        // Nilai referensi dari desain
        int originalViewportTilesHeight = 10; 
        double originalInfoPanelHeightToTileRatio = 100.0 / 96.0; 
    
        // Lakukan kalkulasi awal
        int calculatedTileSize = (int) (targetWindowHeight / (originalViewportTilesHeight + originalInfoPanelHeightToTileRatio));
    
        // Batasi (clamp) hasil kalkulasi tersebut ke dalam suatu rentang
        int dynamicTileSize = Math.max(32, Math.min(calculatedTileSize, 128));
    
        int dynamicInfoPanelHeight = (int) (originalInfoPanelHeightToTileRatio * dynamicTileSize);
    
    public void showMainMenu() {
        cardLayout.show(mainPanelContainer, MAIN_MENU_PANEL_KEY);
        mainMenuPanel.requestFocusInWindow();

        if (gamePanel != null) {
            System.out.println("GameFrame.showMainMenu: Requesting GamePanel to play menu music.");
            gamePanel.playMenuMusic(); 
        }
    }

    public void showGamePanel() {
        if (gamePanel != null) {
            cardLayout.show(mainPanelContainer, GAME_PANEL_KEY);
            gamePanel.requestFocusInWindow();
        } else {
            System.err.println("GamePanel not initialized before showing!");
        }
    }

    public void onMainMenuSelection(int option) {
        switch (option) {
            case MainMenuPanel.START_GAME:
                promptForPlayerAndFarmInfoAndStartGame();
                break;
            case MainMenuPanel.LOAD_GAME:
                loadGameAndStart();
                break;
            case MainMenuPanel.HELP:
                displayHelp();
                mainMenuPanel.requestFocusInWindow(); 
                break;
            case MainMenuPanel.CREDITS:
                displayCredits();
                mainMenuPanel.requestFocusInWindow(); 
                break;
            case MainMenuPanel.MANAGE_SAVES:
                manageSaves();
                mainMenuPanel.requestFocusInWindow(); 
                break;
            case MainMenuPanel.EXIT:
                System.exit(0);
                break;
        }
    }

    /**
     * Load a game from a selected save file
     */
    private void loadGameAndStart() {
        SaveLoadManager saveLoadManager = SaveLoadManager.getInstance();
        List<SaveLoadManager.SaveSlot> saveSlots = saveLoadManager.getSaveSlots();
        
        if (saveSlots.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No save files found.", "Load Game", JOptionPane.INFORMATION_MESSAGE);
            showMainMenu();
            return;
        }
        
        // Buat JComboBox dropdown dari save files
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
        
        // Panel buat dialog
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Select a save file to load:"), BorderLayout.NORTH);
        panel.add(saveComboBox, BorderLayout.CENTER);
        
        // Show the dialog
        int result = JOptionPane.showConfirmDialog(
            this, 
            panel, 
            "Load Game", 
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result != JOptionPane.OK_OPTION) {
            // User cancelled
            showMainMenu();
            return;
        }
        
        SaveLoadManager.SaveSlot selectedSave = (SaveLoadManager.SaveSlot) saveComboBox.getSelectedItem();
        if (selectedSave == null) {
            // No save selected
            showMainMenu();
            return;
        }
        
        SaveData saveData = saveLoadManager.loadGame(selectedSave.getFileName());

        if (saveData != null) {
            Map<String, Item> itemRegistry = Main.setupItemRegistry();
            if (itemRegistry == null || itemRegistry.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Error: Item Registry setup failed! Cannot load game.", "Load Error", JOptionPane.ERROR_MESSAGE);
                showMainMenu();
                return;
            }

            FarmMap farmMap = new FarmMap(false); 
            GameTime gameTime = new GameTime(); 
            ShippingBin shippingBin = new ShippingBin(); 
            Store store = new Store(); 
            WorldMap worldMap = new WorldMap("Spakbor Hills World", store);
            List<NPC> npcList = Main.setupNPCs(); 
            List<Recipe> recipeList = Main.setupRecipes();
            PriceList priceList = Main.setupPriceList();
            
            String playerSpritesheetPath = "/assets/sprites/player/main_char.png"; 
            int playerSpriteWidth = 16;
            int playerSpriteHeight = 32;

            Player player = new Player("Loading...", Gender.MALE, "Loading Farm...", farmMap, 0, 0, 
                                       itemRegistry, playerSpritesheetPath, playerSpriteWidth, playerSpriteHeight);
            EndGameStatistics statistics = new EndGameStatistics(new ArrayList<>(), player);

            House playerHouse = new House(); 
            this.farm = new Farm("Loading Farm...", player, farmMap, worldMap, store, npcList, recipeList, 
                                 gameTime, shippingBin, statistics, priceList, itemRegistry, playerHouse);
            
            saveLoadManager.applySaveDataToGame(saveData, this.farm, this.farm.getPlayer(), this.farm.getCurrentTime());
            
            this.gameController = new GameController(this.farm);

            // Setup GamePanel
            if (this.gamePanel != null) {
                this.gamePanel.stopMusic();
                mainPanelContainer.remove(this.gamePanel);
            }
            gamePanel = new GamePanel(this.farm, this.gameController, this, dynamicTileSize, dynamicInfoPanelHeight);
            mainPanelContainer.add(gamePanel, GAME_PANEL_KEY);
            
            if (this.gameController != null) {
                this.gameController.setGamePanel(gamePanel);
            }

            if (gamePanel != null) {
                gamePanel.startGame(); 
            }
            showGamePanel();
            
            if (this.gameController != null && this.farm.getPlayer().getCurrentMap() != null) {
                this.gameController.ensureSafePlayerSpawn();
            }

        } else {
            JOptionPane.showMessageDialog(this, "Failed to load selected save file.", "Load Error", JOptionPane.ERROR_MESSAGE);
            showMainMenu();
        }
    }

    private void promptForPlayerAndFarmInfoAndStartGame() {
        String playerName = JOptionPane.showInputDialog(this, "Enter your name:", "Player Setup", JOptionPane.PLAIN_MESSAGE);
        if (playerName == null) { // User cancelled
            mainMenuPanel.requestFocusInWindow();
            return;
        }
        if (playerName.trim().isEmpty()) {
            playerName = "Hero"; 
        }

        Object[] genderOptions = {Gender.MALE, Gender.FEMALE};
        Gender playerGender = (Gender) JOptionPane.showInputDialog(
                this,
                "Select your gender:",
                "Player Setup",
                JOptionPane.PLAIN_MESSAGE,
                null,
                genderOptions,
                Gender.MALE
        );
        if (playerGender == null) { // User cancelled
            mainMenuPanel.requestFocusInWindow();
            return;
        }
        
        String farmName = JOptionPane.showInputDialog(this, "Enter your farm's name:", "Farm Setup", JOptionPane.PLAIN_MESSAGE);
        if (farmName == null) { // User cancelled
            mainMenuPanel.requestFocusInWindow();
            return;
        }
        if (farmName.trim().isEmpty()) {
            farmName = playerName + "'s Farm";
        }
        
        Map<String, Item> itemRegistry = Main.setupItemRegistry();
        if (itemRegistry == null || itemRegistry.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Error: Item Registry setup failed! Cannot start game.", "Initialization Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        List<String> favoriteItemOptions = new ArrayList<>();
        
        favoriteItemOptions.add("Parsnip Seeds");
        favoriteItemOptions.add("Cauliflower Seeds");
        favoriteItemOptions.add("Potato Seeds");
        favoriteItemOptions.add("Wheat Seeds");
        favoriteItemOptions.add("Blueberry Seeds");
        favoriteItemOptions.add("Tomato Seeds");
        favoriteItemOptions.add("Hot Pepper Seeds");
        favoriteItemOptions.add("Melon Seeds");
        favoriteItemOptions.add("Cranberry Seeds");
        favoriteItemOptions.add("Pumpkin Seeds");
        favoriteItemOptions.add("Grape Seeds");
        
        // Crops
        favoriteItemOptions.add("Parsnip");
        favoriteItemOptions.add("Cauliflower");
        favoriteItemOptions.add("Potato");
        favoriteItemOptions.add("Wheat");
        favoriteItemOptions.add("Blueberry");
        favoriteItemOptions.add("Tomato");
        favoriteItemOptions.add("Hot Pepper");
        favoriteItemOptions.add("Melon");
        favoriteItemOptions.add("Cranberry");
        favoriteItemOptions.add("Pumpkin");
        favoriteItemOptions.add("Grape");
        
        // Fish - Common
        favoriteItemOptions.add("Bullhead");
        favoriteItemOptions.add("Carp");
        favoriteItemOptions.add("Chub");
        
        // Fish - Regular
        favoriteItemOptions.add("Largemouth Bass");
        favoriteItemOptions.add("Rainbow Trout");
        favoriteItemOptions.add("Sturgeon");
        favoriteItemOptions.add("Midnight Carp");
        favoriteItemOptions.add("Flounder");
        favoriteItemOptions.add("Halibut");
        favoriteItemOptions.add("Octopus");
        favoriteItemOptions.add("Pufferfish");
        favoriteItemOptions.add("Sardine");
        favoriteItemOptions.add("Super Cucumber");
        favoriteItemOptions.add("Catfish");
        favoriteItemOptions.add("Salmon");
        
        // Fish - Legendary
        favoriteItemOptions.add("Angler");
        favoriteItemOptions.add("Crimsonfish");
        favoriteItemOptions.add("Glacierfish");
        favoriteItemOptions.add("Legend");
        
        // Food
        favoriteItemOptions.add("Fish n' Chips");
        favoriteItemOptions.add("Baguette");
        favoriteItemOptions.add("Sashimi");
        favoriteItemOptions.add("Fugu");
        favoriteItemOptions.add("Wine");
        favoriteItemOptions.add("Pumpkin Pie");
        favoriteItemOptions.add("Veggie Soup");
        favoriteItemOptions.add("Fish Stew");
        favoriteItemOptions.add("Spakbor Salad");
        favoriteItemOptions.add("Fish Sandwich");
        favoriteItemOptions.add("The Legends of Spakbor");
        favoriteItemOptions.add("Cooked Pig's Head");
        
        // Equipment
        favoriteItemOptions.add("Hoe");
        favoriteItemOptions.add("Watering Can");
        favoriteItemOptions.add("Pickaxe");
        favoriteItemOptions.add("Fishing Rod");
        
        // Miscellaneous Items
        favoriteItemOptions.add("Coal");
        favoriteItemOptions.add("Firewood");
        favoriteItemOptions.add("Egg");
        favoriteItemOptions.add("Eggplant");
        favoriteItemOptions.add("Proposal Ring");
        favoriteItemOptions.add("Koran");
        
        // Sorting
        Collections.sort(favoriteItemOptions);
        
        // Show dialog
        String favoriteItemName = (String) JOptionPane.showInputDialog(
                this,
                "Choose your favorite item:",
                "Player Favorite Item",
                JOptionPane.PLAIN_MESSAGE,
                null,
                favoriteItemOptions.toArray(),
                favoriteItemOptions.get(0)
        );
        
        if (favoriteItemName == null) { // User cancelled
            favoriteItemName = ""; // Default to empty string
        }

        initializeGame(playerName, playerGender, farmName, favoriteItemName);
        if (this.farm != null && this.gameController != null) {
            if (this.gamePanel != null) { 
                this.gamePanel.stopMusic(); 
                mainPanelContainer.remove(this.gamePanel);
            }

            gamePanel = new GamePanel(this.farm, this.gameController, this, dynamicTileSize, dynamicInfoPanelHeight);
            mainPanelContainer.add(gamePanel, GAME_PANEL_KEY);
            
            if (this.gameController != null) { 
                this.gameController.setGamePanel(gamePanel);
            }
            
            if (gamePanel != null) { 
                gamePanel.startGame(); 
            }
            showGamePanel();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to initialize game components.", "Error", JOptionPane.ERROR_MESSAGE);
            showMainMenu(); 
        }
    }

    private void initializeGame(String playerName, Gender playerGender, String farmName, String favoriteItemName) {
        Map<String, Item> itemRegistry = Main.setupItemRegistry();
        if (itemRegistry == null || itemRegistry.isEmpty()) {
            System.err.println("ERROR: Failed to setup Item Registry. Cannot start game.");
            JOptionPane.showMessageDialog(this, "Error: Item Registry setup failed! Cannot start game.", "Initialization Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        FarmMap farmMap = new FarmMap();
        GameTime gameTime = new GameTime();
        ShippingBin shippingBin = new ShippingBin();
        Store store = new Store();
        WorldMap worldMap = new WorldMap("Spakbor Hills World", store);
        List<NPC> npcList = Main.setupNPCs();
        List<Recipe> recipeList = Main.setupRecipes();
        PriceList priceList = Main.setupPriceList();

        String playerSpritesheetPath = "/assets/sprites/player/main_char.png";
        int playerSpriteWidth = 16;
        int playerSpriteHeight = 32;

        Player newPlayer = new Player(playerName, playerGender, farmName, farmMap, 5, 5,
                                   itemRegistry, playerSpritesheetPath,
                                   playerSpriteWidth, playerSpriteHeight);
        
        newPlayer.setFavoriteItemName(favoriteItemName);
        
        EndGameStatistics statistics = new EndGameStatistics(new ArrayList<>(), newPlayer);

        
        House newPlayerHouse = new House(); 
        this.farm = new Farm(
            farmName, newPlayer, farmMap, worldMap, store,
            npcList, recipeList, gameTime, shippingBin, statistics, priceList,
            itemRegistry, newPlayerHouse
        );

        this.gameController = new GameController(this.farm);
        
    }

    private void displayHelp() {
        JOptionPane.showMessageDialog(this,
            "Spakbor Hills - A Farming Adventure Game!\n\n" +
            "Objective: Become a successful farmer and achieve milestones!\n\n" +
            "Controls (In-Game):\n" +
            "- WASD/Arrows: Move\n" +
            "- E: Interact/Use Tool/Harvest\n" +
            "- F: Eat Selected Item\n" +
            "- T: Open Store (In-Game UI)\n" +
            "- B: Open Shipping Bin (In-Game UI)\n" +
            "- 1, 2: Cycle Inventory\n" +
            "- X: Chat with NPC\n" +
            "- G: Gift to NPC\n" +
            "- L: Sleep\n" +
            "- K: Cook\n" +
            "- V: Watch TV\n" +
            "- I: View Player Info\n" +
            "- O: View Current Progress\n" +
            "- C: Open Cheat Menu (In-Game UI)\n" +
            "- ESC: Pause/Open In-Game Menu (if implemented)\n\n" +
            "Main Menu Controls:\n" +
            "- UP/W: Navigate Up\n" +
            "- DOWN/S: Navigate Down\n" +
            "- ENTER/E: Select Option\n" +
            "- ESC: Exit Game (from main menu)",
            "Help", JOptionPane.INFORMATION_MESSAGE);
    }

    private void displayCredits() {
        JOptionPane.showMessageDialog(this,
            "Spakbor Hills - Created by Kelompok 2\n\n"+
            "PixelMix Font by: Andrew Tyler (https://www.dafont.com/pixelmix.font)\n"+ // Assuming this is the source, adjust if not
            "Game assets from: https://www.spriters-resource.com/\n"+
            "Developed with Java Swing.",
            "Credits", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Display the manage saves dialog
     */
    private void manageSaves() {
        SaveLoadManager saveLoadManager = SaveLoadManager.getInstance();
        List<SaveLoadManager.SaveSlot> saveSlots = saveLoadManager.getSaveSlots();
        
        if (saveSlots.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No save files found.", "Manage Saves", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        boolean keepManaging = true;
        while (keepManaging) {
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
            
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panel.add(new JLabel("Select a save file to manage:"), BorderLayout.NORTH);
            panel.add(saveComboBox, BorderLayout.CENTER);
            
            int result = JOptionPane.showConfirmDialog(
                this, 
                panel, 
                "Manage Saves", 
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (result != JOptionPane.OK_OPTION) {
                // User cancelled atau closed dialog
                keepManaging = false;
            } else {
                SaveLoadManager.SaveSlot selectedSave = (SaveLoadManager.SaveSlot) saveComboBox.getSelectedItem();
                
                if (selectedSave != null) {
                    // Tampilkan opsi untuk menghapus save
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
                        options[1] // Default Cancel
                    );
                    
                    if (action == 0) { // Delete
                        int confirm = JOptionPane.showConfirmDialog(
                            this,
                            "Are you sure you want to delete " + selectedSave.getFileName() + "?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                        );
                        
                        if (confirm == JOptionPane.YES_OPTION) {
                            boolean deleted = saveLoadManager.deleteSave(selectedSave.getFileName());
                            if (deleted) {
                                JOptionPane.showMessageDialog(this,
                                    "Save file deleted successfully.",
                                    "Delete Save",
                                    JOptionPane.INFORMATION_MESSAGE
                                );
                                saveSlots = saveLoadManager.getSaveSlots();
                                if (saveSlots.isEmpty()) {
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
                        // Cancel atau dialog closed
                        keepManaging = false;
                    }
                } else {
                    keepManaging = false;
                }
            }
        }
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    public GameController getGameController() {
        return gameController;
    }
} 