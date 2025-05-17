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
import com.spakborhills.model.Util.PriceList; // For getting item prices in store

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent; // Import KeyEvent
import java.awt.event.KeyListener; // Import KeyListener
import java.util.List;

public class GamePanel extends JPanel implements KeyListener { // Implement KeyListener

    private static final int TILE_SIZE = 32;
    private static final int INFO_PANEL_HEIGHT = 50; // Height for player info display
    private Farm farmModel;
    private GameController gameController;

    public GamePanel(Farm farmModel, GameController gameController) {
        this.farmModel = farmModel;
        this.gameController = gameController;

        setPreferredSize(new Dimension(farmModel.getFarmMap().getSize().width * TILE_SIZE, 
                                       farmModel.getFarmMap().getSize().height * TILE_SIZE + INFO_PANEL_HEIGHT));
        setBackground(Color.GRAY);
        addKeyListener(this);
        setFocusable(true); // Important to receive key events
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
        String info = String.format("Name: %s | Energy: %d | Gold: %d G",
                                    player.getName(), player.getEnergy(), player.getGold());
        g.drawString(info, 10, 20);
        String timeInfo = String.format("Time: %s | Day: %d | Season: %s | Weather: %s",
                                    farmModel.getCurrentTime().getTimeString(),
                                    farmModel.getCurrentTime().getCurrentDay(),
                                    farmModel.getCurrentTime().getCurrentSeason(),
                                    farmModel.getCurrentTime().getCurrentWeather());
        g.drawString(timeInfo, 10, 40);
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
                            // Simple green for planted, could be more detailed
                            tileColor = Color.GREEN.darker(); 
                            if (tile.isWatered()) {
                                tileColor = new Color(0,100,0); // Darker, wet green
                            }
                            if (tile.isHarvestable()) {
                                tileColor = Color.YELLOW; // Ready to harvest
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
            case KeyEvent.VK_E: // Action key for Tilling/Planting
                // Try tilling first
                boolean tilled = gameController.requestTillLandAtPlayerPosition();
                if (tilled) {
                    actionTaken = true;
                } else {
                    // If tilling didn't happen (e.g., tile not tillable, or already tilled), try planting
                    boolean planted = gameController.requestPlantSeedAtPlayerPosition();
                    if (planted) {
                        actionTaken = true;
                    }
                }
                break;
            case KeyEvent.VK_P: // 'P' for Open Store (Changed from 'S' to avoid conflict with move South)
                openStoreDialog();
                actionTaken = true; // Repaint to show any changes like gold
                break;
        }

        if (actionTaken) {
            repaint(); // Repaint the panel if an action was taken that might change the state
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

    @Override
    public void keyReleased(KeyEvent e) {
        // Not used
    }
} 