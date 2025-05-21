package com.spakborhills.model.Map;

import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Enum.TileType; // Untuk tile default
import com.spakborhills.model.Object.DeployedObject; // Jika ada objek di world map
import com.spakborhills.model.Store; 

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Merepresentasikan area di luar kebun pemain (World Map).
 * Dapat berisi berbagai lokasi seperti Forest River, Mountain Lake, Ocean, dan Store.
 * Implementasi ini menganggap WorldMap sebagai sebuah konsep yang bisa
 * mengarahkan ke MapArea spesifik atau memiliki representasi tile generik.
 */
public class WorldMap implements MapArea {

    private final String name; 
    private final Map<LocationType, MapArea> subLocations; 
    private final Tile[][] genericTiles; 
    private static final int GENERIC_WIDTH = 20; 
    private static final int GENERIC_HEIGHT = 20;
    private final List<Point> entryPoints;

    /**
     * Konstruktor untuk WorldMap.
     * @param name Nama umum untuk WorldMap ini.
     * @param storeInstance Objek Store yang akan menjadi bagian dari WorldMap.
     */
    public WorldMap(String name, Store storeInstance) {
        this.name = (name == null || name.isBlank()) ? "World" : name;
        this.subLocations = new HashMap<>();
        this.entryPoints = new ArrayList<>();

        if (storeInstance != null) {
            this.subLocations.put(LocationType.STORE, storeInstance);
        }
        // Tambahkan MapArea lain jika ada (misal, peta khusus untuk Hutan, Danau)
        // this.subLocations.put(LocationType.FOREST_RIVER, new ForestRiverMap());

        // Inisialisasi genericTiles untuk area yang tidak punya map detail
        this.genericTiles = new Tile[GENERIC_HEIGHT][GENERIC_WIDTH];
        for (int y = 0; y < GENERIC_HEIGHT; y++) {
            for (int x = 0; x < GENERIC_WIDTH; x++) {
                genericTiles[y][x] = new Tile(TileType.GRASS); // Default to GRASS for world map areas
            }
        }
        defineEntryPoints(); // Define default entry points for the generic world map
        System.out.println("WorldMap '" + this.name + "' berhasil dibuat dengan entry points.");
    }

    private void defineEntryPoints() {
        // Define some default entry points on the edges of the generic WorldMap grid
        // These points, when stepped on, would trigger the world map selection dialog.
        // Example: Mid-points of each edge
        if (GENERIC_WIDTH > 0 && GENERIC_HEIGHT > 0) {
            // Top edge, middle
            addEntryPoint(new Point(GENERIC_WIDTH / 2, 0));
            // Bottom edge, middle
            addEntryPoint(new Point(GENERIC_WIDTH / 2, GENERIC_HEIGHT - 1));
            // Left edge, middle
            addEntryPoint(new Point(0, GENERIC_HEIGHT / 2));
            // Right edge, middle
            addEntryPoint(new Point(GENERIC_WIDTH - 1, GENERIC_HEIGHT / 2));
        }
        // Mark these tiles as ENTRY_POINT type
        for (Point p : this.entryPoints) {
            if (isWithinBounds(p.x, p.y)) {
                Tile tile = getTile(p.x, p.y);
                if (tile != null) { // Should not be null if within bounds and initialized
                    tile.setType(TileType.ENTRY_POINT);
                }
            }
        }
    }

    private void addEntryPoint(Point point) {
        // Check for duplicates and ensure it's within bounds (though defineEntryPoints should ensure this)
        if (!this.entryPoints.contains(point) && isWithinBounds(point.x, point.y)) {
            this.entryPoints.add(point);
        }
    }

    @Override
    public List<Point> getEntryPoints() {
        return new ArrayList<>(this.entryPoints); // Return a copy
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Dimension getSize() {
        return new Dimension(GENERIC_WIDTH, GENERIC_HEIGHT);
    }

    @Override
    public Tile getTile(int x, int y) {
        if (isWithinBounds(x, y)) {
            return genericTiles[y][x];
        }
        return null; 
    }

    @Override
    public boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < GENERIC_WIDTH && y >= 0 && y < GENERIC_HEIGHT;
    }

    @Override
    public boolean isOccupied(int x, int y) {
        if (!isWithinBounds(x, y)) {
            return true;
        }
        Tile tile = getTile(x,y);
        return tile != null && tile.getAssociatedObject() != null;
    }

    /**
     * Menempatkan objek di area generik WorldMap.
     * Untuk menempatkan objek di sub-lokasi (seperti Store), panggil placeObject
     * pada objek MapArea sub-lokasi tersebut.
     */
    @Override
    public boolean placeObject(DeployedObject obj, int x, int y) {
        if (obj == null) return false;

        // Validasi area penempatan di genericTiles
        for (int i = 0; i < obj.getHeight(); i++) {
            for (int j = 0; j < obj.getWidth(); j++) {
                int targetX = x + j;
                int targetY = y + i;
                if (!isWithinBounds(targetX, targetY) || isOccupied(targetX, targetY)) {
                    System.err.println("Tidak bisa menempatkan " + obj.getName() + " di WorldMap generik (" + targetX + "," + targetY + "): area tidak valid/terisi.");
                    return false;
                }
            }
        }

        // Tempatkan objek dan update tile yang terlibat di genericTiles
        for (int i = 0; i < obj.getHeight(); i++) {
            for (int j = 0; j < obj.getWidth(); j++) {
                Tile currentTile = getTile(x + j, y + i);
                if (currentTile != null) {
                    currentTile.associateObject(obj);
                }
            }
        }
        System.out.println(obj.getName() + " ditempatkan di WorldMap generik pada (" + x + "," + y + ").");
        return true;
    }

    @Override
    public DeployedObject getObjectAt(int x, int y) {
        if (isWithinBounds(x, y)) {
            Tile tile = getTile(x, y);
            if (tile != null) {
                return tile.getAssociatedObject();
            }
        }
        return null;
    }

    /**
     * Mendapatkan MapArea spesifik dari dalam WorldMap berdasarkan LocationType.
     * @param type Tipe lokasi yang dicari (misalnya STORE, FOREST_RIVER).
     * @return Objek MapArea jika ada, atau WorldMap ini sendiri jika tipe merujuk ke area generik.
     */
    public MapArea getSpecificArea(LocationType type) {
        return subLocations.getOrDefault(type, this);
    }
}
