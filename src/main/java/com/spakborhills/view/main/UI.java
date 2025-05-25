package com.spakborhills.view.main;

import com.spakborhills.model.Enum.GameState;
import com.spakborhills.model.Item.Item;
import org.w3c.dom.css.Rect;

import java.awt.*;

public class UI {
    GamePanel gp;
    Font arial_28;
    Graphics2D g2;

    // INVENTORY
    public final int MAX_COL = 5;
    public final int MAX_ROW = 4;
    public int slotCol = 0;
    public int slotRow = 0;

    public UI(GamePanel gp) {
        this.gp = gp;
        arial_28 = new Font("Arial", Font.PLAIN, 28);
    }

    public void draw(Graphics2D g2) {
        this.g2 = g2;
        g2.setFont(arial_28);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(Color.white);

        if(gp.gameState == GameState.PLAY) {
        }

        if(gp.gameState == GameState.CHARACTER_STATE) {
            drawInventory();
        }
    }

    private void drawInventory() {
        Rectangle frame = new Rectangle(gp.tileSize * 9, gp.tileSize, gp.tileSize * 6, gp.tileSize * 5);
        drawSubWindow(frame);

        // slot
        final int slotXStart = frame.x + 20;
        final int slotYStart = frame.y + 20;
        int slotSize = gp.tileSize + 3;

//         items
        int i = 0;
        for(Item item : gp.player.getInventory().getItems().keySet()) {
            int row = i / MAX_COL;
            int col = i % MAX_COL;

            int itemX = slotXStart + col * slotSize;
            int itemY = slotYStart + row * slotSize;

            g2.drawImage(item.image, itemX, itemY, null);

            ++i;
        }

//        cursor

        int cursorX = slotXStart + slotSize * slotCol;
        int cursorY = slotYStart + slotSize * slotRow;
        int cursorWidth = gp.tileSize;
        int cursorHeight = gp.tileSize;
        g2.setColor(Color.white);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(cursorX, cursorY, cursorWidth, cursorHeight, 10,10);
    }

    public void drawSubWindow(Rectangle rect) {
        drawSubWindow(rect.x, rect.y, rect.width, rect.height);
    }

    public void drawSubWindow(int x, int y, int width, int height) {
        // Window body
        Color irengsamar = new Color(0,0,0,210);
        g2.setColor(irengsamar);
        g2.fillRoundRect(x, y, width, height, 35, 35);

        // Window stroke
        g2.setColor(Color.white);
        g2.setStroke(new BasicStroke(5));
        g2.drawRoundRect(x + 5, y + 5, width-10, height-10, 35, 35);
    }

    private void drawEnergy() {

    }

    public int getXCenterText(String text) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return (gp.screenWidth-length)/2;
    }

    public int getXRightText(String text, int tail) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return tail - length;
    }
}
