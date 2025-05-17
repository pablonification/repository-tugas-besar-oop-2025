package com.spakborhills.view;

// import com.spakborhills.model.Map.FarmMap; // No longer needed directly
import com.spakborhills.model.Farm; // Import Farm
import com.spakborhills.controller.GameController; // Import GameController
// import com.spakborhills.controller.GameController;

import javax.swing.*;
// import java.awt.*;

public class GameFrame extends JFrame {

    private GamePanel gamePanel;
    private GameController gameController; // Add GameController field

    public GameFrame(Farm farm, GameController gameController) { // Modify constructor
        this.gameController = gameController; // Store GameController
        setTitle("Spakbor Hills");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Pass both farm and gameController to GamePanel
        gamePanel = new GamePanel(farm, this.gameController);
        add(gamePanel);

        pack(); // Sizes the frame based on its components' preferred sizes
        setLocationRelativeTo(null); // Center the window on the screen
        setVisible(true);
        
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