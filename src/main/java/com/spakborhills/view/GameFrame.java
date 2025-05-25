package com.spakborhills.view;

// import com.spakborhills.model.Map.FarmMap; // No longer needed directly
import com.spakborhills.model.Farm; // Import Farm
import com.spakborhills.controller.GameController; // Import GameController
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
import com.spakborhills.Main; // Required for setup methods

import javax.swing.*;
import java.awt.*; // Import full AWT package for GraphicsDevice and GraphicsEnvironment
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameFrame extends JFrame {

    private MainMenuPanel mainMenuPanel;
    private GamePanel gamePanel;
    private GameController gameController;
    private Farm farm; // Store the farm instance
    private CardLayout cardLayout;
    private JPanel mainPanelContainer; // Panel to hold mainMenuPanel and gamePanel

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

        // Initialize GamePanel here with nulls for now, primarily for its music system.
        // It will be replaced with a fully initialized one when the game starts.
        gamePanel = new GamePanel(null, null, this);
        mainPanelContainer.add(gamePanel, GAME_PANEL_KEY); 
        // Note: GameController will be set later when it's created.

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
        setUndecorated(false); // Show decorations if not full screen
        pack();
        setSize(1280, 720); // Default window size
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void showMainMenu() {
        cardLayout.show(mainPanelContainer, MAIN_MENU_PANEL_KEY);
        mainMenuPanel.requestFocusInWindow();

        // If GamePanel exists (e.g., returning to main menu from game),
        // tell it to play the menu music and stop any in-game music.
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
                // Placeholder for Load Game functionality
                JOptionPane.showMessageDialog(this, "Load Game functionality is not yet implemented.", "Load Game", JOptionPane.INFORMATION_MESSAGE);
                mainMenuPanel.requestFocusInWindow(); // Return focus
                break;
            case MainMenuPanel.HELP:
                displayHelp();
                mainMenuPanel.requestFocusInWindow(); // Return focus
                break;
            case MainMenuPanel.CREDITS:
                displayCredits();
                mainMenuPanel.requestFocusInWindow(); // Return focus
                break;
            case MainMenuPanel.EXIT:
                System.exit(0);
                break;
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

        initializeGame(playerName, playerGender, farmName);
        if (this.farm != null && this.gameController != null) {
            // Remove the old "headless" gamePanel if it exists from the container
            if (this.gamePanel != null) { // gamePanel here is the one from the constructor
                this.gamePanel.stopMusic(); // Stop music on the old panel
                mainPanelContainer.remove(this.gamePanel);
            }
            // Create and add the new GamePanel with proper Farm and GameController
            gamePanel = new GamePanel(this.farm, this.gameController, this);
            mainPanelContainer.add(gamePanel, GAME_PANEL_KEY);
            
            if (this.gameController != null) { // gameController should be non-null here
                this.gameController.setGamePanel(gamePanel);
            }
            
            // GamePanel is created and its dependencies are set.
            // Now, tell GamePanel to start the game logic (sets its state to IN_GAME, starts timers etc.)
            if (gamePanel != null) { // Ensure gamePanel is not null before calling startGame
                gamePanel.startGame(); 
            }
            showGamePanel(); // Then, make the GamePanel visible via CardLayout
        } else {
            JOptionPane.showMessageDialog(this, "Failed to initialize game components.", "Error", JOptionPane.ERROR_MESSAGE);
            showMainMenu(); // Go back to main menu
        }
    }

    private void initializeGame(String playerName, Gender playerGender, String farmName) {
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
        EndGameStatistics statistics = new EndGameStatistics(new ArrayList<>(), newPlayer);

        this.farm = new Farm(
            farmName, newPlayer, farmMap, worldMap, store,
            npcList, recipeList, gameTime, shippingBin, statistics, priceList,
            itemRegistry
        );

        this.gameController = new GameController(this.farm);
        // The GamePanel will be created and set to controller in promptForPlayerAndFarmInfoAndStartGame after farm and controller are ready
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

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    public GameController getGameController() {
        return gameController;
    }
} 