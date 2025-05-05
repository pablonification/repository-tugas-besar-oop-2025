// --- WorldMap.java (Stub) ---
package com.spakborhills.model.Map;

import com.spakborhills.model.Enum.TileType;
import java.awt.Dimension;

public class WorldMap implements MapArea {
    private final String name = "Dunia Luar";
    private final Tile defaultTile = new Tile(TileType.TILLABLE); // Tile default

    @Override public String getName() { return name; }
    @Override public boolean isWithinBounds(int x, int y) { return true; } // Stub: selalu di dalam batas
    @Override public boolean isOccupied(int x, int y) { return false; } // Stub: selalu bisa dilewati
    @Override public Tile getTile(int x, int y) { return defaultTile; } // Stub: kembalikan tile default
    @Override public Dimension getSize() { return new Dimension(100, 100); } // Stub: ukuran dunia luar
    // @Override public boolean placeObject(DeployedObject obj, int x, int y) { return true; } // Stub: selalu berhasil
}