package com.spakborhills.model.tile;

import com.spakborhills.model.Util.Utility;
import com.spakborhills.view.main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@SuppressWarnings({"DataFlowIssue", "CallToPrintStackTrace"})
public class TileManager {
    public static final int NO_OF_TILES = 10;
    public static final int MAP_SIZE = 48;
    GamePanel gp;

    public Tile[] tiles;
    public int[][] mapTileNum;

    public TileManager(GamePanel gp) {
        this.gp = gp;
        tiles = new Tile[NO_OF_TILES];
        mapTileNum = new int[MAP_SIZE][MAP_SIZE];
        getTileImage();
        loadMap("/map/farmV2.txt");
    }

    public void loadMap(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int col = 0;
            int row = 0;
            while(col < gp.maxWorldCol && row < gp.maxWorldRow) {
                String line = br.readLine();

                while(col < gp.maxWorldCol) {
                    String[] numbers = line.split(" ");
                    int num = Integer.parseInt(numbers[col]);
                    mapTileNum[col][row] = num;
                    col++;
                }

                if(col == gp.maxWorldCol) {
                    col = 0;
                    row ++;
                }
            }
            br.close();
        }catch (Exception e) {
            e.printStackTrace();
        }

        for (int[] a : mapTileNum) {
            for(int b : a) {
                System.out.print(b + " ");
            }
            System.out.println();
        }
    }

    public void getTileImage() {
        setup(0, "grass00", false);
        setup(1, "water01", true);
        setup(2, "wall", true);
        setup(3, "tree", true);
        setup(4, "grass00", true);
        setup(5, "grass01", false);

    }

    public void setup(int index, String imageName, boolean collision) {
        try{
            tiles[index] = new Tile();
            tiles[index].image = ImageIO.read(getClass().getResourceAsStream("/tiles/" + imageName + ".png"));
            tiles[index].image = Utility.scaleImage(tiles[index].image, gp.tileSize, gp.tileSize);
            tiles[index].collision = collision;
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2) {
        int worldCol = 0;
        int worldRow = 0;

        while (worldCol < gp.maxWorldCol && worldRow < gp.maxWorldRow) {
            int tileNum = mapTileNum[worldCol][worldRow];

            int worldX = worldCol * gp.tileSize;
            int worldY = worldRow * gp.tileSize;
            int screenX = worldX - gp.worldX + gp.screenX;
            int screenY = worldY - gp.worldY + gp.screenY;

            if(worldX + gp.tileSize > gp.worldX - gp.screenX
                    && worldX - gp.tileSize < gp.worldX + gp.screenX
                    && worldY + gp.tileSize > gp.worldY - gp.screenY
                    && worldY - gp.tileSize < gp.worldY + gp.screenY
            ) {
                g2.drawImage(tiles[tileNum].image, screenX, screenY, null);
            }

            worldCol++;

            if(worldCol == gp.maxWorldCol) {
                worldCol = 0;
                worldRow++;
            }
        }
    }
}
