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
     * Penempatan House dan Pond di-randomize dengan pertimbangan ukuran peta.
     * Shipping Bin ditempatkan dengan prioritas di sebelah kanan rumah.
     */
    private void placeInitialDeployedObjects() {
        Random random = new Random();

        // Margin dari tepi peta untuk memastikan ruang yang cukup
        int edgeMargin = 4;
        
        // A. Tempatkan House (6x6) secara acak dengan margin yang cukup
        House playerHouse = new House();
        boolean housePlaced = false;
        int houseX = 0;
        int houseY = 0;
        
        // Coba sebanyak kali yang diperlukan untuk menempatkan rumah dengan posisi yang baik
        int houseAttempts = 0;
        while (!housePlaced && houseAttempts < 50) {
            // Tempatkan rumah dengan margin yang cukup dari tepi
            houseX = random.nextInt(DEFAULT_WIDTH - playerHouse.getWidth() - (2 * edgeMargin)) + edgeMargin;
            houseY = random.nextInt(DEFAULT_HEIGHT - playerHouse.getHeight() - (2 * edgeMargin)) + edgeMargin;
            
            // Pastikan juga tidak terlalu dekat dengan entry point
            boolean tooCloseToEntry = false;
            for (Point entry : entryPoints) {
                int entryDistance = Math.abs(entry.x - houseX) + Math.abs(entry.y - houseY);
                if (entryDistance < 5) { // Minimal 5 tile dari entry point
                    tooCloseToEntry = true;
                    break;
                }
            }
            
            if (!tooCloseToEntry && placeObject(playerHouse, houseX, houseY)) {
                housePlaced = true;
                System.out.println("Rumah berhasil ditempatkan di (" + houseX + "," + houseY + ")");
                // Set player spawn di dekat rumah
                playerSpawnX = houseX - 1;  // Sedikit di sebelah kiri rumah
                playerSpawnY = houseY + playerHouse.getHeight(); // Di depan rumah
            }
            houseAttempts++;
        }
        
        if (!housePlaced) {
            // Fallback jika tidak berhasil setelah banyak percobaan
            System.err.println("PERINGATAN: Tidak bisa menempatkan rumah setelah banyak percobaan. Menggunakan posisi default.");
            houseX = edgeMargin;
            houseY = edgeMargin;
            if (placeObject(playerHouse, houseX, houseY)) {
                System.out.println("Rumah ditempatkan di posisi default (" + houseX + "," + houseY + ")");
            } else {
                System.err.println("KRITIS: Bahkan posisi default untuk rumah gagal!");
            }
        }

        // B. Tempatkan Shipping Bin (3x2) dengan prioritas posisi yang baik
        ShippingBinObject shippingBin = new ShippingBinObject();
        boolean binPlaced = false;
        
        // Coba beberapa posisi dengan prioritas:
        // 1. Di sebelah kanan rumah
        // 2. Di sebelah kiri rumah
        // 3. Di bawah rumah
        // 4. Pilihan lain yang valid
        
        // Posisi 1: Sebelah kanan rumah (prioritas tertinggi)
        int binX = houseX + playerHouse.getWidth() + 1;
        int binY = houseY;
        if (isAreaAvailable(binX, binY, shippingBin.getWidth(), shippingBin.getHeight()) && 
            binX + shippingBin.getWidth() < DEFAULT_WIDTH - edgeMargin) {
            placeObject(shippingBin, binX, binY);
            System.out.println("Shipping Bin ditempatkan di kanan rumah (" + binX + "," + binY + ")");
            binPlaced = true;
        } 
        
        // Posisi 2: Sebelah kiri rumah
        if (!binPlaced) {
            binX = houseX - shippingBin.getWidth() - 1;
            binY = houseY;
            if (binX >= edgeMargin && isAreaAvailable(binX, binY, shippingBin.getWidth(), shippingBin.getHeight())) {
                placeObject(shippingBin, binX, binY);
                System.out.println("Shipping Bin ditempatkan di kiri rumah (" + binX + "," + binY + ")");
                binPlaced = true;
            }
        }
        
        // Posisi 3: Di bawah rumah
        if (!binPlaced) {
            binX = houseX;
            binY = houseY + playerHouse.getHeight() + 1;
            if (binY + shippingBin.getHeight() < DEFAULT_HEIGHT - edgeMargin && 
                isAreaAvailable(binX, binY, shippingBin.getWidth(), shippingBin.getHeight())) {
                placeObject(shippingBin, binX, binY);
                System.out.println("Shipping Bin ditempatkan di bawah rumah (" + binX + "," + binY + ")");
                binPlaced = true;
            }
        }
        
        // Posisi 4: Pencarian dengan spiral dari rumah untuk menemukan spot yang tersedia
        if (!binPlaced) {
            System.out.println("Mencari lokasi alternatif untuk Shipping Bin...");
            int maxRadius = 10; // Radius maksimum pencarian dari rumah
            int houseCenterX = houseX + playerHouse.getWidth() / 2;
            int houseCenterY = houseY + playerHouse.getHeight() / 2;
            
            for (int radius = 2; radius <= maxRadius && !binPlaced; radius++) {
                for (int offsetY = -radius; offsetY <= radius && !binPlaced; offsetY++) {
                    for (int offsetX = -radius; offsetX <= radius && !binPlaced; offsetX++) {
                        // Hanya periksa titik-titik di tepi kotak dengan radius tertentu
                        if (Math.abs(offsetX) == radius || Math.abs(offsetY) == radius) {
                            binX = houseCenterX + offsetX - (shippingBin.getWidth() / 2);
                            binY = houseCenterY + offsetY - (shippingBin.getHeight() / 2);
                            
                            // Pastikan dalam batas peta dan tidak tumpang tindih
                            if (binX >= edgeMargin && binY >= edgeMargin && 
                                binX + shippingBin.getWidth() < DEFAULT_WIDTH - edgeMargin &&
                                binY + shippingBin.getHeight() < DEFAULT_HEIGHT - edgeMargin &&
                                isAreaAvailable(binX, binY, shippingBin.getWidth(), shippingBin.getHeight())) {
                                
                                placeObject(shippingBin, binX, binY);
                                System.out.println("Shipping Bin ditempatkan di lokasi alternatif (" + binX + "," + binY + ")");
                                binPlaced = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        if (!binPlaced) {
            System.err.println("KRITIS: Tidak bisa menempatkan Shipping Bin di manapun! Coba di lokasi sembarang yang valid.");
            // Percobaan terakhir di lokasi random yang valid
            int binAttempts = 0;
            while (!binPlaced && binAttempts < 100) {
                binX = random.nextInt(DEFAULT_WIDTH - shippingBin.getWidth() - (2 * edgeMargin)) + edgeMargin;
                binY = random.nextInt(DEFAULT_HEIGHT - shippingBin.getHeight() - (2 * edgeMargin)) + edgeMargin;
                
                if (isAreaAvailable(binX, binY, shippingBin.getWidth(), shippingBin.getHeight())) {
                    placeObject(shippingBin, binX, binY);
                    System.out.println("Shipping Bin ditempatkan di lokasi random (" + binX + "," + binY + ")");
                    binPlaced = true;
                }
                binAttempts++;
            }
        }

        // C. Tempatkan Pond (4x3) secara acak, hindari rumah dan shipping bin
        Pond farmPond = new Pond();
        boolean pondPlaced = false;
        int maxAttempts = 100;
        int attempts = 0;
        
        while (!pondPlaced && attempts < maxAttempts) {
            int pondX = random.nextInt(DEFAULT_WIDTH - farmPond.getWidth() - (2 * edgeMargin)) + edgeMargin;
            int pondY = random.nextInt(DEFAULT_HEIGHT - farmPond.getHeight() - (2 * edgeMargin)) + edgeMargin;
            
            // Hindari penempatan terlalu dekat dengan rumah dan shipping bin
            int distanceToHouse = Math.abs(pondX - houseX) + Math.abs(pondY - houseY);
            int distanceToBin = Math.abs(pondX - binX) + Math.abs(pondY - binY);
            
            // Pastikan kolam tidak terlalu dekat dengan objek lain (minimal 3 tile)
            if (distanceToHouse > 3 && distanceToBin > 3 && isAreaAvailable(pondX, pondY, farmPond.getWidth(), farmPond.getHeight())) {
                placeObject(farmPond, pondX, pondY);
                System.out.println("Pond berhasil ditempatkan di (" + pondX + "," + pondY + ")");
                pondPlaced = true;
            }
            attempts++;
        }
        
        if (!pondPlaced) {
            System.err.println("PERINGATAN: Gagal menempatkan Pond setelah " + maxAttempts + " percobaan. Mungkin peta terlalu penuh.");
            // Coba di lokasi sembarang yang valid tanpa batasan jarak
            attempts = 0;
            while (!pondPlaced && attempts < 50) {
                int pondX = random.nextInt(DEFAULT_WIDTH - farmPond.getWidth() - 2) + 1;
                int pondY = random.nextInt(DEFAULT_HEIGHT - farmPond.getHeight() - 2) + 1;
                
                if (isAreaAvailable(pondX, pondY, farmPond.getWidth(), farmPond.getHeight())) {
                    placeObject(farmPond, pondX, pondY);
                    System.out.println("Pond ditempatkan di lokasi alternatif (" + pondX + "," + pondY + ")");
                    pondPlaced = true;
                }
                attempts++;
            }
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