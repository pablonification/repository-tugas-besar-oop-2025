package com.spakborhills.view.entity;

import com.spakborhills.view.main.Direction;
import com.spakborhills.view.main.GamePanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Player extends Entity {

    public final int screenX;
    public final int screenY;

    int spriteCounter = 0;
    int spriteNumber = 1;



    public Player(GamePanel gamePanel) {
        super(gamePanel);
        screenX = gp.screenWidth/2 - gp.tileSize/2;
        screenY = gp.screenHeight/2 - gp.tileSize/2;

        worldX = 12 * gp.tileSize;
        worldY = 28 * gp.tileSize;

        solidArea = new Rectangle(8, 16, 32, 32);
        speed = 4;
        direction = Direction.DOWN;

        getPlayerImage();
    }

    public void getPlayerImage() {
        up1 = setup("/player/boy_up_1");
        up2 = setup("/player/boy_up_2");
        down1 = setup("/player/boy_down_1");
        down2 = setup("/player/boy_down_2");
        left1 = setup("/player/boy_left_1");
        left2 = setup("/player/boy_left_2");
        right1 = setup("/player/boy_right_1");
        right2 = setup("/player/boy_right_2");
    }

    public void update() {
        if(!gp.keyHandler.directionKeyPressed) return;

        if(gp.keyHandler.upPressed) {
            direction = Direction.UP;
        }

        if(gp.keyHandler.downPressed) {
            direction = Direction.DOWN;
        }

        if(gp.keyHandler.leftPressed) {
            direction = Direction.LEFT;
        }

        if(gp.keyHandler.rightPressed) {
            direction = Direction.RIGHT;
        }

        collisionOn = false;
        gp.collisionChecker.checkTile(this);

        // collision doesn't happen;
        if(!collisionOn) {
            switch (direction) {
                case UP -> worldY -= speed;
                case DOWN -> worldY += speed;
                case LEFT -> worldX -= speed;
                case RIGHT -> worldX += speed;
            }

            spriteCounter++;
            if(spriteCounter > 12) {
                if(spriteNumber == 1) spriteNumber = 2;
                else spriteNumber = 1;
                spriteCounter = 0;
            }
        }
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = null;
        switch (direction) {
            case Direction.UP:
                if (spriteNumber == 1) image = up1;
                else image = up2;
                break;
            case Direction.DOWN:
                if (spriteNumber == 1) image = down1;
                else image = down2;
                break;
            case Direction.LEFT:
                if (spriteNumber == 1) image = left1;
                else image = left2;
                break;
            case Direction.RIGHT:
                if (spriteNumber == 1) image = right1;
                else image = right2;
                break;
        }

        g2.drawImage(image, screenX, screenY, null);
    }
}
