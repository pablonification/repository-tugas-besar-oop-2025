package com.spakborhills.model;

// import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Enum.TileType;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Map.MapArea;
import com.spakborhills.model.Map.Tile;
import com.spakborhills.model.Object.DeployedObject; 
// import com.spakborhills.model.Player;
import com.spakborhills.model.Util.PriceList; 

import java.awt.Dimension;
// import java.awt.Point; 
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map; 

/**
 * Merepresentasikan Toko (Store) dalam game.
 * Pemain dapat membeli berbagai item di sini. Emily juga tinggal di sini.
 * Mengimplementasikan MapArea karena merupakan lokasi fisik yang bisa dikunjungi.
 */
public class Store implements MapArea{
    private final String name = "Toko Spakbor Hills";
    private final Tile[][] tiles;
    private final List<String> itemNamesForSale;
    // private Map<Point, DeployedObject> internalObjects; // Jika ada objek di dalam toko

    private static final int STORE_WIDTH = 10; // ini adjust aja nanti
    private static final int STORE_HEIGHT = 8;

    /**
     * Konstruktor untuk Store.
     * Menginisialisasi tata letak tile internal dan daftar item yang dijual.
     */
    public Store(){
        this.tiles = new Tile[STORE_HEIGHT][STORE_WIDTH];
        // this.internalObjects = new HashMap<>(); // ini kalo mau ada objek baru kaya cart

        for (int y = 0; y < STORE_HEIGHT; y++){
            for (int x = 0; x < STORE_WIDTH; x++){
                tiles[y][x] = new Tile(TileType.TILLABLE);
            }
        }
        // Tambahkan objek internal seperti konter, rak, dll. jika perlu
        // placeObject(new CounterObject(), 3, 2);

        this.itemNamesForSale = new ArrayList<>();
        initializeItemsForSale();

        System.out.println("Toko '" + name + "' berhasil dibuat.");
    }
    /**
     * Menginisialisasi daftar nama item yang dijual di toko.
     * Berdasarkan spesifikasi (Seeds, beberapa Food, Koran).
     */
    private void initializeItemsForSale(){
        itemNamesForSale.add("Parsnip Seeds");
        itemNamesForSale.add("Cauliflower Seeds");
        itemNamesForSale.add("Potato Seeds");
        itemNamesForSale.add("Wheat Seeds"); // Spring & Fall
        itemNamesForSale.add("Blueberry Seeds");
        itemNamesForSale.add("Tomato Seeds");
        itemNamesForSale.add("Hot Pepper Seeds");
        itemNamesForSale.add("Melon Seeds");
        itemNamesForSale.add("Cranberry Seeds");
        itemNamesForSale.add("Pumpkin Seeds");
        itemNamesForSale.add("Grape Seeds");

        // Food yang bisa dibeli (Halaman 31)
        itemNamesForSale.add("Fish n' Chips"); // Resep dibeli di store
        itemNamesForSale.add("Fish Sandwich"); // Resep dibeli di store
        // Makanan lain mungkin tidak dibeli langsung tapi resepnya

        // Koran (Bonus Free Market, Halaman 39)
        itemNamesForSale.add("Koran Edisi Baru"); // Nama item koran
    }

    /**
     * Mendapatkan daftar objek Item yang tersedia untuk dijual, lengkap dengan harganya.
     * Membutuhkan ItemRegistry (untuk mendapatkan objek Item) dan PriceList (untuk harga).
     *
     * @param itemRegistry Map berisi semua item dalam game (Nama -> Item).
     * @param priceList Objek PriceList untuk mendapatkan harga beli.
     * @return List berisi objek Item yang dijual di toko.
     */
    public List<Item> getAvailableItems(Map<String, Item> itemRegistry, PriceList priceList){
        if(itemRegistry == null || priceList == null){
            System.err.println("ERROR: ItemRegistry atau PriceList null di Store.getAvailableItems.");
            return Collections.emptyList();
        }
        List<Item> availableItems = new ArrayList<>();
        for (String itemName : itemNamesForSale){
            Item item = itemRegistry.get(itemName);
            if(item != null){
                availableItems.add(item);
            }
            else {
                System.err.println("WARNING: Item'" + itemName + "' yang terdaftar di toko tidak ditemukan di ItemRegistry.");
            }
        }
        return availableItems;
    }

    /**
     * Memproses aksi pembelian item oleh pemain.
     *
     * @param player Pemain yang melakukan pembelian.
     * @param itemToBuy Objek Item yang ingin dibeli.
     * @param quantity Jumlah yang ingin dibeli.
     * @param priceList Untuk mendapatkan harga beli item.
     * @return true jika pembelian berhasil, false jika gagal.
     */
    public boolean sellToPlayer(Player player, Item itemToBuy, int quantity, PriceList priceList){
        if (player == null || itemToBuy == null ||  quantity <= 0 || priceList == null){
            System.err.println("Input tidak valid untuk pembelian.");
            return false;
        }

        // Cek item dijual sama toko atau nggak
        if(!itemNamesForSale.contains(itemToBuy.getName())){
            System.out.println("Maaf, " + itemToBuy.getName() + " tidak dijual di sini.");
            return false;
        }

        // ambil harga beli
        int singleBuyPrice = itemToBuy.getBuyPrice();

        if(singleBuyPrice <= 0 && !itemToBuy.getName().equals("Koran Edisi Baru")){
            System.out.println("Maaf, " + itemToBuy.getName() + " tidak bisa dibeli (harga tidak valid).");
            return false;
        }

        int totalCost = singleBuyPrice * quantity;

        // Cek gold cukup gak
        if (!player.spendGold(totalCost)){
            return false;
        }

        // Tambahkan item ke inventory
        player.getInventory().addItem(itemToBuy, quantity);
        System.out.println("Kamu membeli " + quantity + " " + itemToBuy.getName() + " seharga " + totalCost + "g.");
        return true;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Dimension getSize() {
        return new Dimension(STORE_WIDTH, STORE_HEIGHT);
    }

    @Override
    public Tile getTile(int x, int y) {
        if (isWithinBounds(x, y)) {
            return tiles[y][x];
        }
        return null;
    }

    @Override
    public boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < STORE_WIDTH && y >= 0 && y < STORE_HEIGHT;
    }

    @Override
    public boolean isOccupied(int x, int y) {
        if (!isWithinBounds(x, y)) return true;
        Tile tile = getTile(x,y);
        return tile != null && tile.getType() == TileType.DEPLOYED_OBJECT; 
    }

    @Override
    public boolean placeObject(DeployedObject obj, int x, int y) {
        // Logika penempatan objek di dalam toko (misal, rak, konter)
        if (isAreaAvailableInternal(x, y, obj.getWidth(), obj.getHeight())) {
            // this.internalObjects.put(new Point(x,y), obj); // Jika pakai map objek internal
            for (int i = 0; i < obj.getHeight(); i++) {
                for (int j = 0; j < obj.getWidth(); j++) {
                    Tile currentTile = getTile(x + j, y + i);
                    if (currentTile != null) {
                        currentTile.associateObject(obj); // Tandai tile sebagai ditempati
                    }
                }
            }
            System.out.println(obj.getName() + " ditempatkan di dalam toko pada (" + x + "," + y + ").");
            return true;
        }
        System.err.println("Tidak bisa menempatkan " + obj.getName() + " di dalam toko: area tidak tersedia.");
        return false;
    }

    private boolean isAreaAvailableInternal(int startX, int startY, int width, int height) {
        for (int y = startY; y < startY + height; y++) {
            for (int x = startX; x < startX + width; x++) {
                if (!isWithinBounds(x, y) || isOccupied(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public DeployedObject getObjectAt(int x, int y) {
        if (isWithinBounds(x,y)) {
            Tile tile = getTile(x,y);
            if (tile != null) return tile.getAssociatedObject();
        }
        return null;
    }

}