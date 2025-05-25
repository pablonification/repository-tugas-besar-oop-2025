package com.spakborhills.view.main;

import com.spakborhills.model.Enum.GameState;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.security.Key;

public class KeyHandler implements KeyListener {
    public boolean upPressed, downPressed, leftPressed, rightPressed = false;
    public boolean directionKeyPressed = false;
    GamePanel gp;

    public KeyHandler(GamePanel gp) {
        this.gp = gp;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        switch (gp.gameState) {
            case PLAY -> handlePlay(code);
            case PAUSE -> handlePause(code);
            case TITLE -> handleTitle(code);
            case DIALOGUE -> handleDialogue(code);
            case CHARACTER_STATE -> handleCharacter(code);
        }
    }

    private void handleDialogue(int code) {
        // TODO: implement
    }

    private void handleTitle(int code) {
        //TODO: implement this
    }

    private void handlePause(int code) {
        //TODO: implement this
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if(code == KeyEvent.VK_W) {
            upPressed = false;
        }

        if(code == KeyEvent.VK_A) {
            leftPressed = false;
        }

        if(code == KeyEvent.VK_S) {
            downPressed = false;
        }

        if(code == KeyEvent.VK_D) {
            rightPressed = false;
        }

        directionKeyPressed = upPressed || leftPressed || downPressed || rightPressed;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public void handlePlay(int code) {
        if(code == KeyEvent.VK_W) {
            upPressed = true;
        }

        if(code == KeyEvent.VK_A) {
            leftPressed = true;
        }

        if(code == KeyEvent.VK_S) {
            downPressed = true;
        }

        if(code == KeyEvent.VK_D) {
            rightPressed = true;
        }

        if(code == KeyEvent.VK_C) {
            gp.gameState = GameState.CHARACTER_STATE;
        }

        directionKeyPressed = upPressed || leftPressed || downPressed || rightPressed;
    }
    public void handleCharacter(int code) {
        if(code == KeyEvent.VK_C) {
            gp.gameState = GameState.PLAY;
        }

        if(code == KeyEvent.VK_W) {
            gp.ui.slotRow--;
            if(gp.ui.slotRow < 0) gp.ui.slotRow = 0;
        }

        if(code == KeyEvent.VK_A) {
            gp.ui.slotCol--;
            if(gp.ui.slotCol < 0) gp.ui.slotCol = 0;
        }

        if(code == KeyEvent.VK_S) {
            gp.ui.slotRow++;
            if(gp.ui.slotRow >= gp.ui.MAX_ROW) gp.ui.slotRow = gp.ui.MAX_ROW - 1;
        }

        if(code == KeyEvent.VK_D) {
            gp.ui.slotCol++;
            if(gp.ui.slotCol >= gp.ui.MAX_COL) gp.ui.slotCol = gp.ui.MAX_COL - 1;
        }
    }
}
