package com.spakborhills.view.entity;

import com.spakborhills.model.Util.Utility;
import com.spakborhills.model.Enum.Direction;
import com.spakborhills.view.main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

@SuppressWarnings({"DataFlowIssue", "CallToPrintStackTrace"})
public class Entity {

//    GamePanel gp;
    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2;
    public Direction direction = Direction.SOUTH;

    public boolean collisionOn = false;

    public int worldX;
    public int worldY;
    public int speed;

    public Rectangle solidArea;

//    public Entity(GamePanel gp) {
//        this.gp = gp;
//    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }



}
