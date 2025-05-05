// --- Store.java (Stub) ---
package com.spakborhills.model;

import com.spakborhills.model.Map.MapArea;
import com.spakborhills.model.Map.Tile;
import com.spakborhills.model.Enum.TileType;
import java.awt.Dimension;

// Bisa implement MapArea jika Store adalah lokasi fisik di peta
public class Store implements MapArea {
     private final String name = "Toko";
     private final Tile defaultTile = new Tile(TileType.TILLABLE);

    @Override public String getName() { return name; }
    @Override public Dimension getSize() { return new Dimension(5, 5); } // Ukuran stub
    @Override public boolean isWithinBounds(int x, int y) { return true; }
    @Override public boolean isOccupied(int x, int y) { return false; }
    @Override public Tile getTile(int x, int y) { return defaultTile; }
    // Metode lain Store (misal getAvailableItems) bisa ditambahkan nanti
}