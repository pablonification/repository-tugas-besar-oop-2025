package com.spakborhills.view;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class MainMenuPanel extends JPanel {

    private BufferedImage backgroundImage;
    private String[] menuOptions = {"Start Game", "Load Game", "Help", "Credits", "Manage Saves", "Exit"};
    private int selectedOption = 0;
    private Font pixelFont;
    private Font titleFont;
    private GameFrame gameFrame;

    public static final int START_GAME = 0;
    public static final int LOAD_GAME = 1;
    public static final int HELP = 2;
    public static final int CREDITS = 3;
    public static final int MANAGE_SAVES = 4;
    public static final int EXIT = 5;


    public MainMenuPanel(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        setFocusable(true);
        setPreferredSize(new Dimension(800, 600)); // Default size, will be adapted by GameFrame

        try {
            InputStream bgStream = getClass().getResourceAsStream("/assets/menu/background.png");
            if (bgStream == null) {
                throw new IOException("Cannot find background image /assets/menu/background.png");
            }
            backgroundImage = ImageIO.read(bgStream);
        } catch (IOException e) {
            System.err.println("Error loading main menu background image: " + e.getMessage());
            e.printStackTrace();
            backgroundImage = null; // Fallback to no image
        }

        try {
            // Attempt to load PixelMix font
            InputStream fontStream = getClass().getResourceAsStream("/assets/font/PixelMix.ttf");
            if (fontStream != null) {
                pixelFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(24f);
                titleFont = pixelFont.deriveFont(Font.BOLD, 72f);
            } else {
                System.err.println("PixelMix.ttf not found in /assets/fonts/. Falling back to Monospaced.");
                pixelFont = new Font("Monospaced", Font.PLAIN, 24);
                titleFont = new Font("Monospaced", Font.BOLD, 72);
            }
        } catch (FontFormatException | IOException e) {
            System.err.println("Error loading custom font: " + e.getMessage());
            pixelFont = new Font("Monospaced", Font.PLAIN, 24); // Fallback font
            titleFont = new Font("Monospaced", Font.BOLD, 72); // Fallback font
        }


        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
                    selectedOption = (selectedOption - 1 + menuOptions.length) % menuOptions.length;
                } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
                    selectedOption = (selectedOption + 1) % menuOptions.length;
                } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_E) {
                    handleMenuSelection(selectedOption);
                } else if (keyCode == KeyEvent.VK_ESCAPE) {
                    handleMenuSelection(EXIT); // Exit on Escape
                }
                repaint();
            }
        });
    }

    private void handleMenuSelection(int option) {
        gameFrame.onMainMenuSelection(option);
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Get panel dimensions
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // Draw background image, scaled to fit panel
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, panelWidth, panelHeight, null);
        } else {
            // Fallback background color if image loading failed
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, panelWidth, panelHeight);
        }

        // Draw Title "Spakbor Hills"
        g2d.setFont(titleFont);
        FontMetrics titleFm = g2d.getFontMetrics();
        String titleText = "Spakbor Hills";
        int titleWidth = titleFm.stringWidth(titleText);
        int titleX = (panelWidth - titleWidth) / 2;
        int titleY = panelHeight / 4; // Position title higher

        // Text shadow for title
        g2d.setColor(new Color(0,0,0,150)); // Shadow color
        g2d.drawString(titleText, titleX + 5, titleY + 5);
        // Actual text for title
        g2d.setColor(Color.YELLOW); // Brighter color for title
        g2d.drawString(titleText, titleX, titleY);


        // Draw Menu Options
        g2d.setFont(pixelFont);
        FontMetrics optionFm = g2d.getFontMetrics();
        int optionStartY = titleY + titleFm.getHeight() + 60; // Start options below title

        for (int i = 0; i < menuOptions.length; i++) {
            String optionText = menuOptions[i];
            if (i == selectedOption) {
                optionText = "> " + optionText + " <";
                g2d.setColor(Color.YELLOW);
            } else {
                g2d.setColor(Color.WHITE);
            }
            int optionWidth = optionFm.stringWidth(optionText);
            int optionX = (panelWidth - optionWidth) / 2;
            int optionY = optionStartY + (i * (optionFm.getHeight() + 15)); // Add padding

            // Text shadow for options
            g2d.setColor(new Color(0,0,0,100)); // Shadow color
            g2d.drawString(optionText, optionX + 2, optionY + 2);

            // Actual text for options
            if (i == selectedOption) {
                g2d.setColor(Color.YELLOW);
            } else {
                g2d.setColor(Color.WHITE);
            }
            g2d.drawString(optionText, optionX, optionY);
        }
    }
} 