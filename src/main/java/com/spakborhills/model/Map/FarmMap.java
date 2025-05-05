package com.spakborhills.model.Map;
import com.spakborhills.model.Enum.TileType;
import com.spakborhills.model.Enum.Weather; // Import Weather
import java.awt.Dimension;

// Stub


public class FarmMap implements MapArea {
    private static final int WIDTH = 32;
    private static final int HEIGHT = 32;
    private Tile[][] tiles;

    public FarmMap() {
        tiles = new Tile[HEIGHT][WIDTH];
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                tiles[y][x] = new Tile(TileType.TILLABLE); // Default tanah biasa
            }
        }
        // Nanti tambahkan penempatan House, Pond, ShippingBinObject di sini
        System.out.println("FarmMap created dengan ukuran " + WIDTH + "x" + HEIGHT);
    }

    @Override public String getName() { return "Kebun"; }

    @Override public Dimension getSize() { return new Dimension(WIDTH, HEIGHT); }

    @Override public boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }

    @Override public boolean isOccupied(int x, int y) {
        // Stub: Anggap tidak ada halangan selain batas peta
        // Implementasi nyata akan cek TileType (misal DEPLOYED_OBJECT)
        // atau objek lain di tile tersebut.
        if (!isWithinBounds(x, y)) return true; // Di luar batas dianggap occupied
        // return tiles[y][x].getType() == TileType.DEPLOYED_OBJECT; // Contoh implementasi nanti
        return false; // Stub: selalu bisa dilewati
    }

    @Override public Tile getTile(int x, int y) {
        if (isWithinBounds(x, y)) {
            return tiles[y][x];
        }
        return null; // Atau throw exception
    }

    // Metode untuk update harian semua tile (dipanggil oleh Farm.nextDay)
    public void updateDailyTiles(Weather weather) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                tiles[y][x].updateDaily(weather); // Panggil update di setiap tile
            }
        }
    }

}