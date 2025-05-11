package com.spakborhills.model.tile;

import com.spakborhills.model.Util.Utility;
import com.spakborhills.view.main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

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
        generateMap();
//        loadMap("/map/farmV2.txt");
    }

    public void generateMap() {
        // house & shipping bin generation;
        Random random = new Random();
        int houseX = random.nextInt(22) + 9;
        int houseY = random.nextInt(25) + 9;

        /*
        * Rectangle below specify the allocated space for the house & shipping bin + 1 tile
        * additional space in every side to prevent pond getting side by side with house & shipping bin
        * (it's not specified in the specs, but I think it better be that way)
        * */
        Rectangle house = new Rectangle(houseX - 1, houseY - 1, 12, 8 );

        Rectangle pond = new Rectangle();
        pond.width = 4;
        pond.height = 3;
        do {
            pond.x = (random.nextInt(28) + 9);
            pond.y = (random.nextInt(27) + 9);
        } while (house.intersects(pond));

        house.x++;
        house.y++;
        house.width -= 2;
        house.height -= 2;

        System.out.println("House(" + house.x + ", " + house.y + ')');
        System.out.println("Pond(" + pond.x + ", " + pond.y + ')');

        for(int row = 0; row < 8; ++row) {
            for(int col = 0; col < 48; ++col) {
                mapTileNum[col][row] = 2;
            }
        }

        for(int row = 8; row < 40; ++row) {
            for(int col = 0; col < 48; ++col) {
                if(col < 8) {
                    mapTileNum[col][row] = 2;
                }
                else if(col < 40) {
                    if(pond.contains(col,row)) {
                        mapTileNum[col][row] = 1;
                    }

                    else if(house.contains(col, row)) {
                        if(col - house.x < 6) {
                            mapTileNum[col][row] = 2;
                        }
                        else if(col - house.x > 6 && row - house.y < 2) {
                            mapTileNum[col][row] = 3;
                        }
                        else {
                            mapTileNum[col][row] = 0;
                        }
                    }

                    else {
                        mapTileNum[col][row] = 0;
                    }

                }
                else {
                    mapTileNum[col][row] = 2;
                }
            }
        }

        for(int row = 40; row < 48; ++row) {
            for(int col = 0; col < 48; ++col) {
                mapTileNum[col][row] = 2;
            }
        }

    }

    public void loadMap(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int col = 0;
            int row = 0;
            while(row < gp.maxWorldRow) {
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

//        for (int[] a : mapTileNum) {
//            for(int b : a) {
//                System.out.print(b + " ");
//            }
//            System.out.println();
//        }
    }

    public void getTileImage() {
        setup(0, "grass00", false);
        setup(1, "water01", true);
        setup(2, "wall", true);
        setup(3, "tree", true);
        setup(4, "grass00", false);
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
            int screenX = worldX - gp.player.worldX + gp.player.screenX;
            int screenY = worldY - gp.player.worldY + gp.player.screenY;

            if(worldX + gp.tileSize > gp.player.worldX - gp.player.screenX
                    && worldX - gp.tileSize < gp.player.worldX + gp.player.screenX
                    && worldY + gp.tileSize > gp.player.worldY - gp.player.screenY
                    && worldY - gp.tileSize < gp.player.worldY + gp.player.screenY
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
