package com.spakborhills.controller;

import com.spakborhills.model.Enum.Direction;
import com.spakborhills.model.Player;
import com.spakborhills.view.main.GamePanel;

public class PlayerController {
    GamePanel gp;
    Player player;

    public int standCounter = 0;

    public PlayerController(GamePanel gp, Player player) {
        this.gp = gp;
        this.player = player;
    }

    public void updatePlayer() {
        if (gp.keyHandler.directionKeyPressed) {

            if(gp.keyHandler.upPressed) {
                player.setDirection(Direction.NORTH);
            }
            else if(gp.keyHandler.downPressed) {
                player.setDirection(Direction.SOUTH);
            }
            else if(gp.keyHandler.leftPressed) {
                player.setDirection(Direction.WEST);
            }
            else {
                player.setDirection(Direction.EAST);
            }

            player.walk();
        } else {
            standCounter++;
            if(standCounter > 20) {
                standCounter = 0;
                player.spriteNumber = 1;
            }
        }
    }
}
