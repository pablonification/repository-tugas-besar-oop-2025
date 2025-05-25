package com.spakborhills.view.main;

import com.spakborhills.model.Player;
import com.spakborhills.model.tile.TileManager;
import com.spakborhills.view.entity.PlayerView;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable {

    public static final int ONE_SECOND = 1_000_000_000;

    // screen settings
    final int originalTileSize = 16;
    final int scale = 3;

    public final int tileSize = originalTileSize * scale;

    // SCREEN
    public final int maxScreenCol = 16;
    public final int maxScreenRow = 16;
    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;

    // WORLD
    public final int maxWorldCol = 48;
    public final int maxWorldRow = 48;

    //ENTITY & PLAYER
    public PlayerView player = new PlayerView(this);

    int speed = 4;

    //FPS
    int fps = 60;

    //SYSTEM
    public KeyHandler keyHandler = new KeyHandler();
    public CollisionChecker collisionChecker = new CollisionChecker(this);
    Thread gameThread = null;
    TileManager tileManager = new TileManager(this);

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyHandler);
        this.setFocusable(true);
    }

    // UI THREAD
    public void startGameThread() {
//        System.out.println("CALLED");
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void update() {
//        System.out.println("Called");
        player.update();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);


        Graphics2D g2 = (Graphics2D) g;

        tileManager.draw(g2);

        player.draw(g2);

        g2.dispose();
    }

    @Override
    public void run() {


        double drawInterval = (double) ONE_SECOND / fps; // in ns, for maximum precision

        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        // debugging purpose
//        long timer = 0;
//        long drawCount = 0;
        //-------------------

        while (gameThread != null) {
            currentTime = System.nanoTime();
//            timer += currentTime - lastTime;
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;
            if(delta >= 1) {
                update();

                repaint();
                delta--;
//                drawCount++;
            }

//            if(timer >= ONE_SECOND) {
//                System.out.println("FPS: " + drawCount);
//                drawCount = 0;
//                timer = 0;
//            }

        }
    }
}
