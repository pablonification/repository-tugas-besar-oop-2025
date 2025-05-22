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
    private boolean isParentMap = true; // Flag to prevent infinite recursion

    /**
     * Konstruktor untuk WorldMap.
     * @param name Nama umum untuk WorldMap ini.
     * @param storeInstance Objek Store yang akan menjadi bagian dari WorldMap.
     */
    public WorldMap(String name, Store storeInstance) {
        this(name, storeInstance, true);
    }
    
    /**
     * Konstruktor untuk WorldMap dengan flag untuk mencegah rekursi tak terbatas.
     * @param name Nama umum untuk WorldMap ini.
     * @param storeInstance Objek Store yang akan menjadi bagian dari WorldMap.
     * @param isParentMap Flag yang menunjukkan apakah ini peta utama (akan membuat sub-peta) atau sub-peta.
     */
    private WorldMap(String name, Store storeInstance, boolean isParentMap) {
        this.name = (name == null || name.isBlank()) ? "World" : name;
        this.subLocations = new HashMap<>();
        this.entryPoints = new ArrayList<>();
        this.isParentMap = isParentMap;

        if (storeInstance != null) {
            this.subLocations.put(LocationType.STORE, storeInstance);
            if (isParentMap) {
                this.subLocations.put(LocationType.FOREST_RIVER, createForestRiverMap());
                this.subLocations.put(LocationType.MOUNTAIN_LAKE, createMountainLakeMap());
                this.subLocations.put(LocationType.OCEAN, createOceanMap());
                this.subLocations.put(LocationType.NPC_HOME, createNPCHomeMap());
            }
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
    
    /**
     * Membuat peta untuk lokasi Forest River.
     * Area ini memiliki sungai (WATER tiles) untuk memancing ikan tertentu.
     * @return MapArea yang merepresentasikan Forest River
     */
    private MapArea createForestRiverMap() {
        WorldMap forestMap = new WorldMap("Forest River", null, false);
        
        // Override genericTiles untuk membuat peta dengan sungai
        for (int y = 0; y < GENERIC_HEIGHT; y++) {
            for (int x = 0; x < GENERIC_WIDTH; x++) {
                // Buat sungai di tengah peta (vertikal)
                if (x >= GENERIC_WIDTH/2 - 2 && x <= GENERIC_WIDTH/2 + 2) {
                    forestMap.genericTiles[y][x].setType(TileType.WATER);
                } else {
                    // Sisanya adalah rumput
                    forestMap.genericTiles[y][x].setType(TileType.GRASS);
                }
            }
        }
        
        // Tetapkan entry point di sisi untuk kembali ke farm
        forestMap.entryPoints.clear();
        forestMap.addEntryPoint(new Point(0, GENERIC_HEIGHT/2));
        for (Point p : forestMap.entryPoints) {
            if (forestMap.isWithinBounds(p.x, p.y)) {
                Tile tile = forestMap.getTile(p.x, p.y);
                if (tile != null) {
                    tile.setType(TileType.ENTRY_POINT);
                }
            }
        }
        
        System.out.println("Forest River Map created with river and entry points.");
        return forestMap;
    }
    
    /**
     * Membuat peta untuk lokasi Mountain Lake.
     * Area ini memiliki danau (WATER tiles) untuk memancing ikan tertentu.
     * @return MapArea yang merepresentasikan Mountain Lake
     */
    private MapArea createMountainLakeMap() {
        WorldMap lakeMap = new WorldMap("Mountain Lake", null, false);
        
        // Override genericTiles untuk membuat peta dengan danau
        for (int y = 0; y < GENERIC_HEIGHT; y++) {
            for (int x = 0; x < GENERIC_WIDTH; x++) {
                // Buat danau di tengah peta (bentuk oval)
                if (Math.pow((x - GENERIC_WIDTH/2), 2) + Math.pow((y - GENERIC_HEIGHT/2), 2) <= 
                    Math.pow(Math.min(GENERIC_WIDTH, GENERIC_HEIGHT)/4, 2)) {
                    lakeMap.genericTiles[y][x].setType(TileType.WATER);
                } else {
                    // Sisanya adalah rumput
                    lakeMap.genericTiles[y][x].setType(TileType.GRASS);
                }
            }
        }
        
        // Tetapkan entry point di sisi untuk kembali ke farm
        lakeMap.entryPoints.clear();
        lakeMap.addEntryPoint(new Point(0, GENERIC_HEIGHT/2));
        for (Point p : lakeMap.entryPoints) {
            if (lakeMap.isWithinBounds(p.x, p.y)) {
                Tile tile = lakeMap.getTile(p.x, p.y);
                if (tile != null) {
                    tile.setType(TileType.ENTRY_POINT);
                }
            }
        }
        
        System.out.println("Mountain Lake Map created with lake and entry points.");
        return lakeMap;
    }
    
    /**
     * Membuat peta untuk lokasi Ocean.
     * Area ini memiliki lautan (WATER tiles) untuk memancing ikan tertentu.
     * @return MapArea yang merepresentasikan Ocean
     */
    private MapArea createOceanMap() {
        WorldMap oceanMap = new WorldMap("Ocean", null, false);
        
        // Override genericTiles untuk membuat peta dengan lautan
        for (int y = 0; y < GENERIC_HEIGHT; y++) {
            for (int x = 0; x < GENERIC_WIDTH; x++) {
                // Buat daerah pantai (5 kotak dari ujung bawah)
                if (y < GENERIC_HEIGHT - 5) {
                    oceanMap.genericTiles[y][x].setType(TileType.WATER);
                } else {
                    // Sisanya adalah pantai/rumput
                    oceanMap.genericTiles[y][x].setType(TileType.GRASS);
                }
            }
        }
        
        // Tetapkan entry point di pantai untuk kembali ke farm
        oceanMap.entryPoints.clear();
        oceanMap.addEntryPoint(new Point(GENERIC_WIDTH/2, GENERIC_HEIGHT-1));
        for (Point p : oceanMap.entryPoints) {
            if (oceanMap.isWithinBounds(p.x, p.y)) {
                Tile tile = oceanMap.getTile(p.x, p.y);
                if (tile != null) {
                    tile.setType(TileType.ENTRY_POINT);
                }
            }
        }
        
        System.out.println("Ocean Map created with ocean and entry points.");
        return oceanMap;
    }
    
    /**
     * Membuat peta untuk lokasi NPC Home.
     * Area ini memiliki rumah-rumah NPC untuk interaksi dengan NPC.
     * @return MapArea yang merepresentasikan NPC Home
     */
    private MapArea createNPCHomeMap() {
        WorldMap npcMap = new WorldMap("NPC Village", null, false);
        
        // Override genericTiles untuk membuat peta dengan rumah NPC
        for (int y = 0; y < GENERIC_HEIGHT; y++) {
            for (int x = 0; x < GENERIC_WIDTH; x++) {
                // Seluruh peta adalah rumput
                npcMap.genericTiles[y][x].setType(TileType.GRASS);
            }
        }
        
        // Tetapkan beberapa area sebagai OBSTACLE untuk merepresentasikan rumah NPC
        // Rumah Mayor Tadi
        for (int y = 3; y < 8; y++) {
            for (int x = 3; x < 8; x++) {
                npcMap.genericTiles[y][x].setType(TileType.OBSTACLE);
            }
        }
        
        // Rumah Caroline
        for (int y = 3; y < 8; y++) {
            for (int x = GENERIC_WIDTH - 8; x < GENERIC_WIDTH - 3; x++) {
                npcMap.genericTiles[y][x].setType(TileType.OBSTACLE);
            }
        }
        
        // Rumah Perry
        for (int y = GENERIC_HEIGHT - 8; y < GENERIC_HEIGHT - 3; y++) {
            for (int x = 3; x < 8; x++) {
                npcMap.genericTiles[y][x].setType(TileType.OBSTACLE);
            }
        }
        
        // Rumah Dasco
        for (int y = GENERIC_HEIGHT - 8; y < GENERIC_HEIGHT - 3; y++) {
            for (int x = GENERIC_WIDTH - 8; x < GENERIC_WIDTH - 3; x++) {
                npcMap.genericTiles[y][x].setType(TileType.OBSTACLE);
            }
        }
        
        // Rumah Abigail (tengah)
        for (int y = GENERIC_HEIGHT/2 - 2; y < GENERIC_HEIGHT/2 + 3; y++) {
            for (int x = GENERIC_WIDTH/2 - 2; x < GENERIC_WIDTH/2 + 3; x++) {
                npcMap.genericTiles[y][x].setType(TileType.OBSTACLE);
            }
        }
        
        // Tetapkan entry point di sisi untuk kembali ke farm
        npcMap.entryPoints.clear();
        npcMap.addEntryPoint(new Point(GENERIC_WIDTH-1, GENERIC_HEIGHT/2));
        for (Point p : npcMap.entryPoints) {
            if (npcMap.isWithinBounds(p.x, p.y)) {
                Tile tile = npcMap.getTile(p.x, p.y);
                if (tile != null) {
                    tile.setType(TileType.ENTRY_POINT);
                }
            }
        }
        
        System.out.println("NPC Village Map created with NPC houses and entry points.");
        return npcMap;
    }
}
