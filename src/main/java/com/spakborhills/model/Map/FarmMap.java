package com.spakborhills.model.Map;

import com.spakborhills.model.Enum.TileType;
import com.spakborhills.model.Enum.Weather;
import com.spakborhills.model.Enum.Season; 
import com.spakborhills.model.Object.DeployedObject;
import com.spakborhills.model.Object.House; // Contoh DeployedObject
import com.spakborhills.model.Object.Pond;   // Contoh DeployedObject
import com.spakborhills.model.Object.ShippingBinObject; // Contoh DeployedObject

import java.awt.Point;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;

/**
 * Merepresentasikan peta kebun pemain (Farm Map).
 * Berisi grid Tile dan mengelola objek yang ditempatkan (DeployedObject).
 * Mengimplementasikan interface MapArea.
 * Berdasarkan spesifikasi Halaman 21-22 dan diagram kelas.
 */
public class FarmMap implements MapArea{
    private static final int DEFAULT_WIDTH = 32;
    private static final int DEFAULT_HEIGHT = 32;
    private final String name = "Farm";
    private final Tile[][] tiles;
    private final Map<Point, DeployedObject> deployedObjectsMap;
    private final java.util.List<Point> entryPoints; // Daftar EntryPoint
    private int playerSpawnX; // Added for default spawn X
    private int playerSpawnY; // Added for default spawn Y

    /**
     * Default constructor for new games, initializes with default objects and entry points.
     */
    public FarmMap() {
        this(true); // Initialize with defaults
    }

    /**
     * Konstruktor untuk FarmMap.
     * Menginisialisasi grid Tile, mendefinisikan entry point, 
     * dan secara kondisional menempatkan objek awal.
     * @param initializeDefaults Jika true, tempatkan objek awal (House, Pond, dll.). Jika false, jangan tempatkan.
     */
    public FarmMap(boolean initializeDefaults) {
        this.tiles = new Tile[DEFAULT_HEIGHT][DEFAULT_WIDTH];
        this.deployedObjectsMap = new HashMap<>();
        this.entryPoints = new java.util.ArrayList<>();

        this.playerSpawnX = DEFAULT_WIDTH / 2; 
        this.playerSpawnY = DEFAULT_HEIGHT / 2;

        // Always initialize all tiles as TILLABLE first
        for (int y = 0; y < DEFAULT_HEIGHT; y++) {
            for (int x = 0; x < DEFAULT_WIDTH; x++) {
                tiles[y][x] = new Tile(TileType.TILLABLE);
            }
        }

        // Define entry points first - always need to do this
        defineEntryPoints();

        // Only place house, pond, etc when initializing defaults
        if (initializeDefaults) {
            placeInitialDeployedObjects();
        }

        System.out.println("FarmMap '" + name + "' berhasil dibuat. Initial defaults placed: " + initializeDefaults);
    } 

    /**
     * Menempatkan objek-objek awal (House, Pond, Shipping Bin) di FarmMap.
     * Penempatan House dan Pond di-randomize.
     * Shipping Bin selalu berjarak 1 petak dari rumah.
     */
    private void placeInitialDeployedObjects() {
        Random random = new Random();

        // A. Tempatkan House (6x6) secara acak
        House playerHouse = new House(); // Asumsi konstruktor default ada
        boolean housePlaced = false;
        while (!housePlaced) {
            // Batasi area penempatan agar tidak terlalu mepet pinggir
            int houseX = random.nextInt(DEFAULT_WIDTH - playerHouse.getWidth() - 2) + 1; // +1 agar tidak di kolom 0
            int houseY = random.nextInt(DEFAULT_HEIGHT - playerHouse.getHeight() - 2) + 1; // +1 agar tidak di baris 0
            if (placeObject(playerHouse, houseX, houseY)) {
                housePlaced = true;
                System.out.println("Rumah ditempatkan di (" + houseX + "," + houseY + ")");

                // B. Tempatkan Shipping Bin (3x2) 1 petak dari rumah (Halaman 21)
                // Coba tempatkan di sebelah kanan rumah
                ShippingBinObject shippingBin = new ShippingBinObject(); // Asumsi konstruktor default
                int binX = houseX + playerHouse.getWidth() + 1; // 1 petak di kanan rumah
                int binY = houseY; // Sejajar dengan bagian atas rumah (bisa disesuaikan)
                // Pastikan bin masih dalam batas peta
                if (isAreaAvailable(binX, binY, shippingBin.getWidth(), shippingBin.getHeight())) {
                    placeObject(shippingBin, binX, binY);
                    System.out.println("Shipping Bin ditempatkan di (" + binX + "," + binY + ")");
                } else {
                    // Coba di kiri, atas, atau bawah jika kanan tidak bisa (logika bisa lebih kompleks)
                    System.err.println("PERINGATAN: Tidak bisa menempatkan Shipping Bin di sebelah kanan rumah. Perlu logika penempatan alternatif.");
                }
            }
        }


        // C. Tempatkan Pond (4x3) secara acak, pastikan tidak tumpang tindih dengan House/Bin
        Pond farmPond = new Pond(); // Asumsi konstruktor default ada
        boolean pondPlaced = false;
        int maxAttempts = 100; // Hindari infinite loop jika peta terlalu penuh
        int attempts = 0;
        while (!pondPlaced && attempts < maxAttempts) {
            int pondX = random.nextInt(DEFAULT_WIDTH - farmPond.getWidth());
            int pondY = random.nextInt(DEFAULT_HEIGHT - farmPond.getHeight());
            if (placeObject(farmPond, pondX, pondY)) {
                pondPlaced = true;
                System.out.println("Pond ditempatkan di (" + pondX + "," + pondY + ")");
            }
            attempts++;
        }
        if (!pondPlaced) {
            System.err.println("PERINGATAN: Gagal menempatkan Pond setelah " + maxAttempts + " percobaan. Peta mungkin terlalu penuh.");
        }
    }

    /**
     * Defines entry points at the north, east, south, and west edges of the map.
     * It's important this runs before placeInitialDeployedObjects and is always called
     * regardless of initializeDefaults value.
     */
    private void defineEntryPoints() {
        // Entry Point Utara (tengah atas)
        int northX = DEFAULT_WIDTH / 2;
        int northY = 0;
        if (isWithinBounds(northX, northY)) {
            tiles[northY][northX].setType(TileType.ENTRY_POINT);
            entryPoints.add(new Point(northX, northY));
            System.out.println("Entry Point Utara ditambahkan di (" + northX + "," + northY + ")");
        }

        // Entry Point Timur (tengah kanan)
        int eastX = DEFAULT_WIDTH - 1;
        int eastY = DEFAULT_HEIGHT / 2;
        if (isWithinBounds(eastX, eastY)) {
            tiles[eastY][eastX].setType(TileType.ENTRY_POINT);
            entryPoints.add(new Point(eastX, eastY));
            System.out.println("Entry Point Timur ditambahkan di (" + eastX + "," + eastY + ")");
        }

        // Entry Point Selatan (tengah bawah)
        int southX = DEFAULT_WIDTH / 2;
        int southY = DEFAULT_HEIGHT - 1;
        if (isWithinBounds(southX, southY)) {
            tiles[southY][southX].setType(TileType.ENTRY_POINT);
            entryPoints.add(new Point(southX, southY));
            System.out.println("Entry Point Selatan ditambahkan di (" + southX + "," + southY + ")");
        }

        // Entry Point Barat (tengah kiri)
        int westX = 0;
        int westY = DEFAULT_HEIGHT / 2;
        if (isWithinBounds(westX, westY)) {
            tiles[westY][westX].setType(TileType.ENTRY_POINT);
            entryPoints.add(new Point(westX, westY));
            System.out.println("Entry Point Barat ditambahkan di (" + westX + "," + westY + ")");
        }
    }
    
    /**
     * Mengembalikan daftar koordinat (Point) dari semua entry/exit point di map ini.
     * @return List<Point> dari entry points.
     */
    public java.util.List<Point> getEntryPoints() {
        return java.util.Collections.unmodifiableList(this.entryPoints);
    }

    // Added getters for player spawn coordinates
    public int getPlayerSpawnX() {
        return playerSpawnX;
    }

    public int getPlayerSpawnY() {
        return playerSpawnY;
    }

    /**
     * Helper untuk memeriksa apakah area tertentu tersedia untuk penempatan objek.
     */
    private boolean isAreaAvailable(int startX, int startY, int width, int height) {
        for (int y = startY; y < startY + height; y++) {
            for (int x = startX; x < startX + width; x++) {
                if (!isWithinBounds(x, y) || isOccupied(x, y) || tiles[y][x].getType() == TileType.ENTRY_POINT) { // Check for ENTRY_POINT
                    return false; // Area tidak valid, sudah terisi, atau merupakan entry point
                }
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Mengembalikan ukuran peta (lebar dan tinggi).
     * Sesuai diagram kelas MapArea.
     * @return Objek Dimension yang berisi lebar dan tinggi peta.
     */
    @Override
    public Dimension getSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public Tile getTile(int x, int y) {
        if (isWithinBounds(x, y)) {
            return tiles[y][x];
        }
        return null; 
    }

    @Override
    public Tile[][] getTiles() {
        return this.tiles;
    }

    /**
     * Memeriksa apakah koordinat (x,y) berada dalam batas peta.
     * Sesuai diagram kelas MapArea.
     */
    @Override
    public boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < DEFAULT_WIDTH && y >= 0 && y < DEFAULT_HEIGHT;
    }

    /**
     * Memeriksa apakah tile di koordinat (x,y) ditempati (tidak bisa dilewati).
     * Tile dianggap ditempati jika tipenya DEPLOYED_OBJECT atau ada objek di deployedObjectsMap
     * yang mencakup koordinat tersebut.
     * Sesuai diagram kelas MapArea.
     */
    @Override
    public boolean isOccupied(int x, int y) {
        if (!isWithinBounds(x, y)) {
            return true; // Di luar batas dianggap ditempati/tidak bisa diakses
        }
        // Cek dulu tipe tile dasarnya, apakah memang tidak bisa dilewati (misal Tembok, Air)
        Tile currentTile = tiles[y][x];
        if (currentTile.getType() == TileType.WALL || 
            currentTile.getType() == TileType.WATER) { // WATER is generally impassable
            // Tambahkan TileType lain yang tidak bisa dilewati di sini jika ada (misal, MOUNTAIN)
            return true;
        }

        // Kemudian cek apakah ada DeployedObject di lokasi tersebut
        DeployedObject obj = getObjectAt(x, y);
        if (obj != null) {
            // Jika objeknya adalah House, maka tile TIDAK dianggap occupied (bisa dilewati)
            if (obj instanceof House) {
                return false; 
            }
            // Untuk objek lain, anggap tile occupied (tidak bisa dilewati)
            return true; 
        }
        
        // Jika tidak ada objek dan tipe tile-nya sendiri tidak menghalangi, maka tidak occupied
        return false;
    }

    /**
     * Menempatkan DeployedObject di peta pada koordinat x, y (kiri atas objek).
     * Metode ini akan memvalidasi area dan mengupdate semua Tile yang terlibat.
     * Sesuai diagram kelas MapArea.
     *
     * @param obj Objek yang akan ditempatkan.
     * @param x Koordinat x (kiri atas) untuk penempatan.
     * @param y Koordinat y (kiri atas) untuk penempatan.
     * @return true jika berhasil ditempatkan, false jika gagal.
     */
    public boolean placeObject(DeployedObject obj, int x, int y) {
        if (obj == null) {
            System.err.println("Kesalahan: Mencoba menempatkan objek null.");
            return false;
        }

        if (!isAreaAvailable(x, y, obj.getWidth(), obj.getHeight())) {
            return false;
        }

        deployedObjectsMap.put(new Point(x, y), obj);

        for (int i = 0; i < obj.getHeight(); i++) {
            for (int j = 0; j < obj.getWidth(); j++) {
                Tile currentTile = getTile(x + j, y + i);
                if (currentTile != null) {
                    // The associateObject method in Tile now handles preserving ENTRY_POINT type.
                    // It also correctly sets DEPLOYED_OBJECT for non-Pond, non-ENTRY_POINT cases.
                    currentTile.associateObject(obj);

                    // If the object is a Pond, explicitly set the TileType to WATER.
                    // This overrides the DEPLOYED_OBJECT that might have been set by associateObject
                    // if the tile wasn't an ENTRY_POINT.
                    if (obj instanceof Pond) {
                        currentTile.setType(TileType.WATER);
                    } 
                    // No specific handling for House or ShippingBinObject here regarding tile type,
                    // as associateObject (if not ENTRY_POINT) would have set it to DEPLOYED_OBJECT.
                }
            }
        }
        System.out.println(obj.getName() + " ditempatkan di FarmMap pada (" + x + "," + y + "). Tile di bawahnya disesuaikan.");
        return true;
    }

    /**
     * Mendapatkan DeployedObject yang ada di koordinat (x,y) tertentu.
     * Ini akan mencari objek yang area cakupannya meliputi (x,y).
     * Sesuai diagram kelas MapArea.
     *
     * @param x Koordinat x.
     * @param y Koordinat y.
     * @return DeployedObject jika ada, atau null jika tidak ada.
     */
    public DeployedObject getObjectAt(int x, int y) {
        if (!isWithinBounds(x, y)) {
            return null;
        }
        
        Tile tile = getTile(x,y);
        if (tile != null) {
            return tile.getAssociatedObject();
        }
        return null; 
    }

    /**
     * Mengupdate semua tile di FarmMap untuk akhir hari.
     * @param weather Cuaca saat ini.
     * @param currentSeason Musim saat ini.
     */
    public void updateDailyTiles(Weather weather, Season currentSeason) {
        for (int r = 0; r < DEFAULT_HEIGHT; r++) {
           for (int c = 0; c < DEFAULT_WIDTH; c++) {
               tiles[r][c].updateDaily(weather, currentSeason);
           }
       }
   }

    public boolean removeObject(DeployedObject objToRemove) {
        if (objToRemove == null) return false;

        Point anchorToRemove = null;
        // Cari anchor point dari objek yang mau dihapus
        for (Map.Entry<Point, DeployedObject> entry : deployedObjectsMap.entrySet()) {
            if (entry.getValue() == objToRemove) {
                anchorToRemove = entry.getKey();
                break;
            }
        }

        if (anchorToRemove != null) {
            deployedObjectsMap.remove(anchorToRemove);
            // Reset semua tile yang sebelumnya ditempati objek ini
            for (int i = 0; i < objToRemove.getHeight(); i++) {
                for (int j = 0; j < objToRemove.getWidth(); j++) {
                    Tile tile = getTile(anchorToRemove.x + j, anchorToRemove.y + i);
                    if (tile != null) {
                        tile.removeAssociatedObject(); // Tile kembali ke TILLABLE
                    }
                }
            }
            System.out.println(objToRemove.getName() + " dihapus dari peta.");
            return true;
        }
        System.err.println("Objek " + objToRemove.getName() + " tidak ditemukan untuk dihapus.");
        return false;
    }

    public Map<Point, DeployedObject> getDeployedObjectsMap() {
        return this.deployedObjectsMap;
    }

    public void clearAllDeployedObjects() {
        // Create a list of objects to remove to avoid ConcurrentModificationException
        java.util.List<DeployedObject> objectsToRemove = new ArrayList<>(deployedObjectsMap.values());
        for (DeployedObject obj : objectsToRemove) {
            removeObject(obj); // removeObject already handles resetting tiles
        }
        // Sanity check, though removeObject should handle it.
        // deployedObjectsMap.clear(); // This would orphan tiles if removeObject wasn't thorough
        System.out.println("All deployed objects cleared from FarmMap.");
    }

}