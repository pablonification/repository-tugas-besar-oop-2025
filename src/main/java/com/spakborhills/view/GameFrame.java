package com.spakborhills.view;

// import com.spakborhills.model.Map.FarmMap; // No longer needed directly
import com.spakborhills.model.Farm; // Import Farm
import com.spakborhills.controller.GameController; // Import GameController
// import com.spakborhills.controller.GameController;

import javax.swing.*;
import java.awt.*; // Import full AWT package for GraphicsDevice and GraphicsEnvironment

public class GameFrame extends JFrame {

    private GamePanel gamePanel;
    private GameController gameController; // Add GameController field

    public GameFrame(Farm farm, GameController gameController) { // Modify constructor
        this.gameController = gameController; // Store GameController
        setTitle("Spakbor Hills");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // setResizable(false); // Not needed for full-screen

        // Pass both farm and gameController to GamePanel
        gamePanel = new GamePanel(farm, this.gameController);
        add(gamePanel);

        // Set GamePanel reference in GameController after GamePanel is created
        if (this.gameController != null) {
            this.gameController.setGamePanel(gamePanel);
        }

        // Configure for full-screen
        setUndecorated(true); // Remove window borders and title bar
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();
        
        if (device.isFullScreenSupported()) {
            try {
                device.setFullScreenWindow(this);
            } catch (Exception e) {
                System.err.println("Error attempting to set full-screen mode: " + e.getMessage());
                // Fallback to windowed mode if full-screen fails
                pack();
                setLocationRelativeTo(null);
                setVisible(true);
            }
        } else {
            System.err.println("Full-screen mode is not supported on this device.");
            // Fallback to windowed mode if full-screen is not supported
            pack();
            setLocationRelativeTo(null);
        setVisible(true);
        }
        
        // Ensure GamePanel can receive focus immediately
        gamePanel.requestFocusInWindow(); 
    }

    // Later, we might initialize with a GameController or the full Farm model
    /*
    public GameFrame(GameController controller, FarmMap farmMap) {
        this.gameController = controller;
        // ...
        gamePanel = new GamePanel(farmMap); // Or pass controller to panel
        // ...
    }
    */

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    // It might be useful to have a getter for the controller too
    public GameController getGameController() {
        return gameController;
    }
} 