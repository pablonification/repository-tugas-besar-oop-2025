package com.spakborhills.model.Map;

import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Enum.TileType; 
import com.spakborhills.model.Object.DeployedObject;
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
    private boolean isParentMap = true; 
    private static final int NPC_HOME_WIDTH = 10; 
    private static final int NPC_HOME_HEIGHT = 10;

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

        if (isParentMap) {
        if (storeInstance != null) {
            this.subLocations.put(LocationType.STORE, storeInstance);
            }
            this.subLocations.put(LocationType.FOREST_RIVER, createForestRiverMap());
            this.subLocations.put(LocationType.MOUNTAIN_LAKE, createMountainLakeMap());
            this.subLocations.put(LocationType.OCEAN, createOceanMap());
            
            // Add specific NPC homes
            this.subLocations.put(LocationType.MAYOR_TADI_HOME, createMayorTadiHomeMap());
            this.subLocations.put(LocationType.CAROLINE_HOME, createCarolineHomeMap());
            this.subLocations.put(LocationType.PERRY_HOME, createPerryHomeMap());
            this.subLocations.put(LocationType.DASCO_HOME, createDascoHomeMap());
            this.subLocations.put(LocationType.ABIGAIL_HOME, createAbigailHomeMap());
           
        }

        // Inisialisasi genericTiles untuk area yang tidak punya map detail
        this.genericTiles = new Tile[GENERIC_HEIGHT][GENERIC_WIDTH];
        for (int y = 0; y < GENERIC_HEIGHT; y++) {
            for (int x = 0; x < GENERIC_WIDTH; x++) {
                genericTiles[y][x] = new Tile(TileType.GRASS);
            }
        }
        defineEntryPoints(); 
        System.out.println("WorldMap '" + this.name + "' berhasil dibuat dengan entry points.");
    }

    private void defineEntryPoints() {
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
        for (Point p : this.entryPoints) {
            if (isWithinBounds(p.x, p.y)) {
                Tile tile = getTile(p.x, p.y);
                if (tile != null) { 
                    tile.setType(TileType.ENTRY_POINT);
            }
        }
        }
    }

    private void addEntryPoint(Point point) {
        if (!this.entryPoints.contains(point) && isWithinBounds(point.x, point.y)) {
            this.entryPoints.add(point);
        }
    }

    @Override
    public List<Point> getEntryPoints() {
        return new ArrayList<>(this.entryPoints); 
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

    @Override
    public Tile[][] getTiles() {
        return this.genericTiles;
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
        
        forestMap.entryPoints.clear();
        forestMap.addEntryPoint(new Point(0, GENERIC_HEIGHT / 2));
        forestMap.addEntryPoint(new Point(GENERIC_WIDTH - 1, GENERIC_HEIGHT / 2));

        for (Point p : forestMap.entryPoints) {
            if (forestMap.isWithinBounds(p.x, p.y)) {
                Tile tile = forestMap.getTile(p.x, p.y);
                if (tile != null) {
                    tile.setType(TileType.ENTRY_POINT);
                }
            }
        }
        
        System.out.println("Forest River Map created with river and entry points on both sides.");
        return forestMap;
    }
    
    /**
     * Membuat peta untuk lokasi Mountain Lake.
     * Area ini memiliki danau (WATER tiles) untuk memancing ikan tertentu.
     * @return MapArea yang merepresentasikan Mountain Lake
     */
    private MapArea createMountainLakeMap() {
        WorldMap lakeMap = new WorldMap("Mountain Lake", null, false);
        
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
     * Membuat peta untuk lokasi Mayor Tadi's Home.
     * Area ini memiliki rumah-rumah NPC untuk interaksi dengan NPC.
     * @return MapArea yang merepresentasikan Mayor Tadi's Home
     */
    private MapArea createMayorTadiHomeMap() {
        WorldMap mayorHome = new WorldMap("Mayor Tadi's Home", null, false);
        mayorHome.overrideTiles(NPC_HOME_WIDTH, NPC_HOME_HEIGHT, TileType.WOOD_FLOOR); 
        System.out.println("Mayor Tadi's Home map created.");
        return mayorHome;
    }

    private MapArea createCarolineHomeMap() {
        WorldMap carolineHome = new WorldMap("Caroline's Home", null, false);
        carolineHome.overrideTiles(NPC_HOME_WIDTH, NPC_HOME_HEIGHT, TileType.STONE_FLOOR);
        System.out.println("Caroline's Home map created.");
        return carolineHome;
    }

    private MapArea createPerryHomeMap() {
        WorldMap perryHome = new WorldMap("Perry's Home", null, false);
        perryHome.overrideTiles(NPC_HOME_WIDTH, NPC_HOME_HEIGHT, TileType.CARPET_FLOOR); 
        System.out.println("Perry's Home map created.");
        return perryHome;
    }

    private MapArea createDascoHomeMap() {
        WorldMap dascoHome = new WorldMap("Dasco's Residence", null, false);
        dascoHome.overrideTiles(NPC_HOME_WIDTH, NPC_HOME_HEIGHT, TileType.LUXURY_FLOOR); 
        System.out.println("Dasco's Residence map created.");
        return dascoHome;
    }

    private MapArea createAbigailHomeMap() {
        WorldMap abigailHome = new WorldMap("Abigail's Home", null, false);
        abigailHome.overrideTiles(NPC_HOME_WIDTH, NPC_HOME_HEIGHT, TileType.DIRT_FLOOR); 
        System.out.println("Abigail's Home map created.");
        return abigailHome;
    }

    /**
     * Helper method to re-initialize genericTiles for sub-maps like NPC homes.
     * This allows NPC homes to have different default sizes and tile types.
     * @param width The width of the map.
     * @param height The height of the map.
     * @param defaultType The default TileType for this map.
     */
    private void overrideTiles(int width, int height, TileType defaultType) {
        for (int y = 0; y < GENERIC_HEIGHT; y++) { 
            for (int x = 0; x < GENERIC_WIDTH; x++) { 
                if (x < width && y < height) { 
                    this.genericTiles[y][x] = new Tile(defaultType);
                } else {
                    this.genericTiles[y][x] = new Tile(TileType.GRASS); 
                }
            }
        }
        this.entryPoints.clear();
        if (width > 0 && height > 0) {
            Point entry = new Point(width / 2, height - 1); 
            addEntryPoint(entry);
             if (isWithinBounds(entry.x, entry.y)) {
                Tile tile = getTile(entry.x, entry.y);
                if (tile != null) {
                    tile.setType(TileType.ENTRY_POINT);
                }
            }
        }
    }

    /**
     * Mendapatkan MapArea spesifik dari dalam WorldMap berdasarkan namanya.
     * Berguna untuk memuat game.
     * @param name Nama MapArea yang dicari.
     * @return Objek MapArea jika ditemukan, atau null jika tidak.
     */
    public MapArea getMapAreaByName(String name) {
        if (this.name.equalsIgnoreCase(name)) {
            return this;
        }
        for (MapArea subArea : subLocations.values()) {
            if (subArea.getName().equalsIgnoreCase(name)) {
                return subArea;
            }
        }
        return null; 
    }
}
