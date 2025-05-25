package com.spakborhills.view.entity;

import com.spakborhills.controller.PlayerController;
import com.spakborhills.model.Enum.Direction;
import com.spakborhills.model.Player;
import com.spakborhills.view.main.GamePanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PlayerView {

    public final int screenX;
    public final int screenY;
    Player player;
    PlayerController controller;

    GamePanel gp;

    public PlayerView(GamePanel gamePanel, Player player, PlayerController controller) {
        this.gp = gamePanel;
        screenX = (gp.screenWidth - gp.tileSize) / 2;
        screenY = (gp.screenHeight - gp.tileSize) / 2;
        this.player = player;
        this.controller = controller;
        System.out.println(player == null);
    }

    public void update() {
        controller.updatePlayer();
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = null;
        switch (player.direction) {
            case Direction.NORTH:
                if (player.spriteNumber == 1) image = player.up1;
                else image = player.up2;
                break;
            case Direction.SOUTH:
                if (player.spriteNumber == 1) image = player.down1;
                else image = player.down2;
                break;
            case Direction.WEST:
                if (player.spriteNumber == 1) image = player.left1;
                else image = player.left2;
                break;
            case Direction.EAST:
                if (player.spriteNumber == 1) image = player.right1;
                else image = player.right2;
                break;
        }

        g2.drawImage(image, screenX, screenY, null);
    }
}
