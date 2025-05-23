package com.spakborhills.model.Map;

import com.spakborhills.model.Enum.TileType;
import com.spakborhills.model.Object.DeployedObject;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class PlayerHouseInterior implements MapArea {
    private static final int HOUSE_INTERIOR_WIDTH = 24;
    private static final int HOUSE_INTERIOR_HEIGHT = 24;
    private final String name = "PlayerHouseInterior"; // Or "Rumah Pemain Internal"
    private final Tile[][] tiles;
    private final List<Point> entryPoints; // Entry from FarmMap to here

    public PlayerHouseInterior() {
        this.tiles = new Tile[HOUSE_INTERIOR_HEIGHT][HOUSE_INTERIOR_WIDTH];
        this.entryPoints = new ArrayList<>();

        // Initialize all tiles, e.g., as WOOD_FLOOR or similar
        for (int y = 0; y < HOUSE_INTERIOR_HEIGHT; y++) {
            for (int x = 0; x < HOUSE_INTERIOR_WIDTH; x++) {
                // Example: Make borders walls, and interior floor
                if (y == 0 || y == HOUSE_INTERIOR_HEIGHT - 1 || x == 0 || x == HOUSE_INTERIOR_WIDTH - 1) {
                    tiles[y][x] = new Tile(TileType.WALL); // Assuming WALL is a TileType
                } else {
                    tiles[y][x] = new Tile(TileType.WOOD_FLOOR); // Assuming WOOD_FLOOR is a TileType
                }
            }
        }

        // Define an entry point (e.g., where player appears when entering from FarmMap)
        // This is an example, adjust as needed. Maybe near the "bottom" of the 24x24 map.
        int entryX = HOUSE_INTERIOR_WIDTH / 2;
        int entryY = HOUSE_INTERIOR_HEIGHT - 2; // Near the bottom edge
        if (isWithinBounds(entryX, entryY) && tiles[entryY][entryX].getType() != TileType.WALL) {
            tiles[entryY][entryX].setType(TileType.ENTRY_POINT); // Or just keep as floor
            entryPoints.add(new Point(entryX, entryY));
        } else {
             // Fallback if default entry point is a wall (should not happen with current logic)
            entryPoints.add(new Point(1,1)); // Default to a safe spot
        }
        System.out.println(name + " map created.");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Dimension getSize() {
        return new Dimension(HOUSE_INTERIOR_WIDTH, HOUSE_INTERIOR_HEIGHT);
    }

    @Override
    public Tile getTile(int x, int y) {
        if (isWithinBounds(x, y)) {
            return tiles[y][x];
        }
        return null;
    }

    @Override
    public boolean isOccupied(int x, int y) {
        if (!isWithinBounds(x, y)) {
            return true; // Out of bounds is occupied
        }
        Tile tile = getTile(x,y);
        if (tile == null) return true; // Should not happen if within bounds
        
        // For now, only walls and deployed objects occupy space
        // DeployedObject check is more relevant if we place furniture
        return tile.getType() == TileType.WALL || tile.getAssociatedObject() != null;
    }

    @Override
    public boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < HOUSE_INTERIOR_WIDTH && y >= 0 && y < HOUSE_INTERIOR_HEIGHT;
    }

    @Override
    public boolean placeObject(DeployedObject obj, int x, int y) {
        // Basic furniture placement logic can be added here later
        // For now, just check bounds and if the tile is a floor tile
        if (obj == null || !isWithinBounds(x,y) || !isWithinBounds(x + obj.getWidth() -1, y + obj.getHeight() -1)) {
            return false;
        }
        for (int i = 0; i < obj.getHeight(); i++) {
            for (int j = 0; j < obj.getWidth(); j++) {
                Tile tile = getTile(x + j, y + i);
                if (tile == null || tile.getType() == TileType.WALL || tile.getAssociatedObject() != null) {
                    System.err.println("Cannot place " + obj.getName() + " at (" + (x+j) + "," + (y+i) + ") in " + getName() + ". Area occupied or not suitable.");
                    return false; // Area occupied or not a floor
                }
            }
        }
        // If area is clear, associate object with tiles
        for (int i = 0; i < obj.getHeight(); i++) {
            for (int j = 0; j < obj.getWidth(); j++) {
                getTile(x + j, y + i).associateObject(obj);
            }
        }
        System.out.println(obj.getName() + " placed in " + getName() + " at (" + x + "," + y + ")");
        return true;
    }

    @Override
    public DeployedObject getObjectAt(int x, int y) {
        if (isWithinBounds(x, y)) {
            Tile tile = getTile(x,y);
            if (tile != null) {
                return tile.getAssociatedObject();
            }
        }
        return null;
    }

    @Override
    public List<Point> getEntryPoints() {
        // Returns the point where the player should appear when entering this map
        return entryPoints;
    }
    
    // Helper to add more entry points if needed, e.g., from different doors
    public void addEntryPoint(int x, int y, TileType expectedTile){
        if(isWithinBounds(x,y) && (tiles[y][x].getType() == expectedTile || tiles[y][x].getType() == TileType.ENTRY_POINT)){
            tiles[y][x].setType(TileType.ENTRY_POINT);
            entryPoints.add(new Point(x,y));
        }
    }
} 