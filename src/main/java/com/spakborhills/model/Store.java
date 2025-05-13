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

        // Crops yang bisa dibeli (Halaman 19)
        itemNamesForSale.add("Parsnip"); // Harga Beli 50g
        itemNamesForSale.add("Cauliflower"); // Harga Beli 200g
        itemNamesForSale.add("Wheat"); // Harga Beli 50g
        itemNamesForSale.add("Blueberry"); // Harga Beli 150g
        itemNamesForSale.add("Tomato"); // Harga Beli 90g
        itemNamesForSale.add("Pumpkin"); // Harga Beli 300g
        itemNamesForSale.add("Grape"); // Harga Beli 100g
        // Crop lain seperti Potato, Hot Pepper, Melon, Cranberry punya harga beli '-' atau tidak ada,
        // jadi tidak dimasukkan sebagai item yang bisa dibeli di sini.

        // Koran (Bonus Free Market, Halaman 39)
        itemNamesForSale.add("Koran Edisi Baru"); // Nama item koran
    }

    /**
     * Mendapatkan daftar objek Item yang tersedia untuk dijual, lengkap dengan harganya
     * yang diambil dari PriceList.
     *
     * @param itemRegistry Map global berisi semua definisi item (Nama Item -> Objek Item).
     * @param priceList Objek PriceList untuk mendapatkan harga beli aktual.
     * @return List berisi objek Item yang dijual di toko. Item akan memiliki harga beli
     *         yang sudah di-set dari PriceList jika berbeda dari harga defaultnya.
     */
    public List<Item> getAvailableItemsForDisplay(Map<String, Item> itemRegistry, PriceList priceList) {
        if (itemRegistry == null || priceList == null) {
            System.err.println("Kesalahan: ItemRegistry atau PriceList null di Store.getAvailableItemsForDisplay.");
            return Collections.emptyList();
        }
        List<Item> availableItems = new ArrayList<>();
        for (String itemName : itemNamesForSale) {
            Item masterItem = itemRegistry.get(itemName); // Dapatkan template item dari registry
            if (masterItem != null) {
                // Dapatkan harga beli aktual dari PriceList
                int actualBuyPrice = priceList.getBuyPrice(itemName);

                // Jika item tidak ada di PriceList atau harga <= 0 (kecuali koran), anggap tidak dijual
                if (actualBuyPrice < 0 ) { // Harga -1 berarti tidak ditemukan di PriceList
                    System.err.println("Peringatan: Harga beli untuk '" + itemName + "' tidak ditemukan di PriceList. Item tidak ditampilkan.");
                    continue;
                }
                 if (actualBuyPrice == 0 && !itemName.equals("Koran Edisi Baru")) { // Item dengan harga 0 tidak dijual kecuali koran
                     // System.out.println("Info: Item '" + itemName + "' memiliki harga beli 0 dan tidak akan ditampilkan untuk dijual (kecuali koran).");
                     // continue; // Lewati jika harga 0 dan bukan koran
                 }


                // Buat salinan item untuk ditampilkan (opsional, tapi lebih aman)
                // atau modifikasi harga beli item master jika hanya untuk display sementara.
                // Untuk kesederhanaan, kita asumsikan objek Item dari registry bisa langsung digunakan.
                // Jika Item adalah immutable, kita perlu membuat instance baru dengan harga yang benar.
                // Untuk sekarang, kita hanya tambahkan masterItem. View akan mengambil harga dari PriceList.
                availableItems.add(masterItem);
            } else {
                System.err.println("Peringatan: Item '" + itemName + "' yang terdaftar di toko tidak ditemukan di ItemRegistry.");
            }
        }
        return availableItems;
    }

    /**
     * Memproses aksi pembelian item oleh pemain.
     *
     * @param player Pemain yang melakukan pembelian.
     * @param itemToBuy Objek Item yang ingin dibeli (sebaiknya diambil dari getAvailableItemsForDisplay).
     * @param quantity Jumlah yang ingin dibeli.
     * @param priceList Untuk mendapatkan harga beli item.
     * @param itemRegistry (Opsional, jika perlu membuat instance baru dari item yang dibeli)
     * @return true jika pembelian berhasil, false jika gagal.
     */
    public boolean sellToPlayer(Player player, Item itemToBuy, int quantity, PriceList priceList, Map<String, Item> itemRegistry) {
        if (player == null || itemToBuy == null || quantity <= 0 || priceList == null || itemRegistry == null) {
            System.out.println("Input tidak valid untuk pembelian.");
            return false;
        }

        // 1. Cek apakah item ini memang terdaftar untuk dijual
        if (!itemNamesForSale.contains(itemToBuy.getName())) {
            System.out.println("Maaf, " + itemToBuy.getName() + " tidak dijual di sini.");
            return false;
        }

        // 2. Dapatkan harga beli aktual dari PriceList
        int singleActualBuyPrice = priceList.getBuyPrice(itemToBuy.getName());

        if (singleActualBuyPrice < 0) { // Harga -1 berarti tidak ada di PriceList
            System.out.println("Maaf, harga untuk " + itemToBuy.getName() + " tidak tersedia.");
            return false;
        }
        // Item dengan harga 0 (kecuali koran) tidak seharusnya bisa dibeli
        if (singleActualBuyPrice == 0 && !itemToBuy.getName().equals("Koran Edisi Baru")) {
            System.out.println("Maaf, " + itemToBuy.getName() + " tidak dapat dibeli.");
            return false;
        }

        int totalCost = singleActualBuyPrice * quantity;

        // 3. Cek apakah pemain punya cukup gold
        if (!player.spendGold(totalCost)) {
            // Pesan "Gold tidak cukup" sudah dari player.spendGold()
            return false;
        }

        // 4. Tambahkan item ke inventory pemain
        // Penting: Ambil item master dari registry untuk ditambahkan,
        // jangan tambahkan itemToBuy langsung jika itu adalah salinan/objek sementara.
        Item masterItem = itemRegistry.get(itemToBuy.getName());
        if (masterItem != null) {
            player.getInventory().addItem(masterItem, quantity);
            System.out.println("Kamu membeli " + quantity + " " + masterItem.getName() + " seharga " + totalCost + "g.");
            return true;
        } else {
            // Seharusnya tidak terjadi jika item ada di itemNamesForSale dan getAvailableItemsForDisplay
            System.err.println("Kesalahan internal: Item master '" + itemToBuy.getName() + "' tidak ditemukan di registry saat transaksi.");
            // Kembalikan gold pemain karena transaksi gagal di tahap akhir
            player.addGold(totalCost);
            return false;
        }
    }

    @Override
    public String getName() { return this.name; }

    @Override
    public Dimension getSize() { return new Dimension(STORE_WIDTH, STORE_HEIGHT); }

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
        return tile != null && (tile.getType() == TileType.DEPLOYED_OBJECT || tile.getAssociatedObject() != null);
    }

    @Override
    public boolean placeObject(DeployedObject obj, int x, int y) {
        if (obj == null) return false;
        if (isAreaAvailableInternal(x, y, obj.getWidth(), obj.getHeight())) {
            // this.internalObjects.put(new Point(x,y), obj); // Jika pakai map objek internal
            for (int i = 0; i < obj.getHeight(); i++) {
                for (int j = 0; j < obj.getWidth(); j++) {
                    Tile currentTile = getTile(x + j, y + i);
                    if (currentTile != null) {
                        currentTile.associateObject(obj);
                    }
                }
            }
            // System.out.println(obj.getName() + " ditempatkan di dalam toko pada (" + x + "," + y + ").");
            return true;
        }
        System.err.println("Tidak bisa menempatkan " + obj.getName() + " di dalam toko: area tidak tersedia.");
        return false;
    }

    private boolean isAreaAvailableInternal(int startX, int startY, int width, int height) {
        for (int r = startY; r < startY + height; r++) {
            for (int c = startX; c < startX + width; c++) {
                if (!isWithinBounds(c, r) || isOccupied(c, r)) {
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