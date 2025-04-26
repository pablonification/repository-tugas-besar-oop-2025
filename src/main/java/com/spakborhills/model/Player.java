/*
class Player {
  ' --- Constants ---
  + {static} final int MAX_ENERGY = 100
  + {static} final int MIN_ENERGY = -20
  + {static} final int LOW_ENERGY_THRESHOLD = 10 ' Added constant

  ' --- Attributes ---
  - name: String
  - gender: Gender
  - energy: int
  - farmName: String
  - gold: int
  - inventory: Inventory
  - currentMap: MapArea
  - currentTileX: int
  - currentTileY: int
  - partner: NPC
  - favoriteItemName: String

  ' --- Constructor (Implicit dependency on ItemRegistry not shown in diagram) ---
  + Player(name: String, gender: Gender, farmName: String, startMap: MapArea, startX: int, startY: int, itemRegistry: ItemRegistry)

  ' --- Getters ---
  + getName(): String
  + getGender(): Gender
  + getEnergy(): int
  + getGold(): int
  + getInventory(): Inventory
  + getCurrentMap(): MapArea
  + getPosition(): Point ' Returns Point(currentTileX, currentTileY)
  + getPartner(): NPC
  + getFavoriteItemName(): String

  ' --- Setters / State Changers ---
  + changeEnergy(amt: int): void ' Clamps energy, pass-out check delegated
  + addGold(amt: int): void
  + spendGold(amt: int): boolean
  + setPartner(n: NPC): void
  + setPosition(x: int, y: int): void ' Separate from setCurrentMap
  + setCurrentMap(m: MapArea): void ' Separate from setPosition
  + setFavoriteItemName(name: String): void

  ' --- Action Methods (Many delegate complex logic/state changes to Controller) ---
  + move(dir: Direction): boolean ' Checks bounds & obstacles, updates position
  + till(targetTile: Tile): boolean ' Checks tool & tile, calls tile.till(). Costs delegated.
  + recoverLand(targetTile: Tile): boolean ' Checks tool & tile, calls tile.recover(). Costs delegated.
  + plant(seedToPlant: Seed, targetTile: Tile): boolean ' Calls seed.use(), removes seed if success. Costs delegated.
  + water(targetTile: Tile): boolean ' Checks tool & tile, calls tile.water(). Costs delegated.
  + harvest(targetTile: Tile, itemRegistry: ItemRegistry): boolean ' Calls tile.harvest(), adds items to inventory. Costs delegated. ' Changed return, added param
  + eat(itemToEat: Item): boolean ' Checks instanceof EdibleItem, calls item.use(), removes item if success. Time cost delegated. ' Parameter changed to Item
  + sleep(energyBeforeSleep: int, usedBonusBed: boolean): void ' Sets energy based on penalty/bonus. Time skip delegated. ' Changed signature & return
  + cook(recipe: Recipe, fuelItem: Item, itemRegistry: ItemRegistry): boolean ' Checks & consumes ingredients/fuel. Passive time/item addition delegated. ' Added params
  + fish(fishingLocation: LocationType): void ' Checks tool. RNG & item addition delegated. ' Changed return type
  + propose(npcTarget: NPC, ring: ProposalRing): boolean ' Calls ring.use(). Core logic (checks, status change, costs) delegated. ' Added param
  + marry(npcTarget: NPC): boolean ' Checks conditions. Time skip & status change delegated.
  + watchTV(): Weather ' Placeholder. Costs delegated. Needs bonus impl.
  + visit(destinationMap: MapArea, entryX: int, entryY: int): boolean ' Updates map & position. Costs delegated. ' Changed params
  + chat(npcTarget: NPC): boolean ' Calls npc.interact(), adds heart points. Costs delegated.
  + gift(npcTarget: NPC, itemToGift: Item): boolean ' Checks preference, adds points, removes item. Costs delegated.
  + sellItemToBin(itemToSell: Item, quantity: int, shippingBin: ShippingBin): boolean ' Adds item to bin, removes from inventory. Time cost delegated. ' Added param
}
 */

 package com.spakborhills.model; // Pastikan sesuai struktur Anda

import java.util.List;
import java.util.Map;
import java.awt.Point;

// Import kelas/enum lain yang dibutuhkan (Pastikan path benar!)
import com.spakborhills.model.Enum.Gender;
import com.spakborhills.model.Enum.Direction;
import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Enum.RelationshipStatus;
import com.spakborhills.model.Enum.Weather;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Item.Crop;
import com.spakborhills.model.Item.EdibleItem;
import com.spakborhills.model.Item.Seed;
import com.spakborhills.model.Item.Equipment; // Pastikan ada
import com.spakborhills.model.Item.ProposalRing;
import com.spakborhills.model.NPC.NPC; // Pastikan ada
import com.spakborhills.model.Map.MapArea;
import com.spakborhills.model.Map.Tile;
// import com.spakborhills.model.Util.GameTime; // Anda mungkin perlu ini di Controller
import com.spakborhills.model.Util.Inventory;
import com.spakborhills.model.Util.Recipe; // Pastikan ada
import com.spakborhills.model.Util.ShippingBin; // Pastikan ada

public class Player {
    // --- Konstanta ---
    public static final int MAX_ENERGY = 100;
    public static final int MIN_ENERGY = -20;
    public static final int LOW_ENERGY_THRESHOLD = 10;
    private static final int DEFAULT_STARTING_GOLD = 500; 

    // --- Atribut ---
    private final String name;
    private final Gender gender;
    private int energy;
    private final String farmName;
    private NPC partner; // null jika single
    private int gold;
    private final Inventory inventory;
    private MapArea currentMap;
    private int currentTileX;
    private int currentTileY;
    private String favoriteItemName;

    /**
     * Konstruktor untuk kelas Player.
     * Menginisialisasi state pemain di awal permainan.
     *
     * @param name          Nama pemain yang dipilih.
     * @param gender        Jenis kelamin pemain yang dipilih.
     * @param farmName      Nama kebun pemain yang dipilih.
     * @param startMap      Map awal tempat pemain memulai (biasanya FarmMap).
     * @param startX        Koordinat X awal.
     * @param startY        Koordinat Y awal.
     * @param itemRegistry  Sebuah Map yang merepresentasikan registri item (String nama -> objek Item).
     */
    public Player(String name, Gender gender, String farmName, MapArea startMap, int startX, int startY, Map<String, Item> itemRegistry) {
        this.name = name;
        this.gender = gender;
        this.farmName = farmName;
        this.energy = MAX_ENERGY;
        this.gold = DEFAULT_STARTING_GOLD;
        this.inventory = new Inventory(); // Buat inventory baru
        this.currentMap = startMap;
        this.currentTileX = startX;
        this.currentTileY = startY;
        this.favoriteItemName = ""; // Default kosong
        this.partner = null; // Mulai single

        // Inisialisasi inventory dengan item default (Halaman 23)
        if (itemRegistry != null) {
            Item parsnipSeeds = itemRegistry.get("Parsnip Seeds");
            Item hoe = itemRegistry.get("Hoe");
            Item wateringCan = itemRegistry.get("Watering Can");
            Item pickaxe = itemRegistry.get("Pickaxe");
            Item fishingRod = itemRegistry.get("Fishing Rod");

            if (parsnipSeeds != null) this.inventory.addItem(parsnipSeeds, 15); else System.err.println("PERINGATAN: Parsnip Seeds tidak ditemukan di registry.");
            if (hoe != null) this.inventory.addItem(hoe, 1); else System.err.println("PERINGATAN: Hoe tidak ditemukan di registry.");
            if (wateringCan != null) this.inventory.addItem(wateringCan, 1); else System.err.println("PERINGATAN: Watering Can tidak ditemukan di registry.");
            if (pickaxe != null) this.inventory.addItem(pickaxe, 1); else System.err.println("PERINGATAN: Pickaxe tidak ditemukan di registry.");
            if (fishingRod != null) this.inventory.addItem(fishingRod, 1); else System.err.println("PERINGATAN: Fishing Rod tidak ditemukan di registry.");

        } else {
            System.err.println("PERINGATAN: ItemRegistry (Map) null. Inventory tidak diinisialisasi.");
        }
    }

    // --- Getters ---
    public String getName() { return name; }
    public Gender getGender() { return gender; }
    public int getEnergy() { return energy; }
    public String getFarmName() { return farmName; }
    public NPC getPartner() { return partner; }
    public int getGold() { return gold; }
    public Inventory getInventory() { return inventory; }
    public MapArea getCurrentMap() { return currentMap; }
    public int getCurrentTileX() { return currentTileX; }
    public int getCurrentTileY() { return currentTileY; }
    public Point getPosition() { return new Point(currentTileX, currentTileY); }
    public String getFavoriteItemName() { return favoriteItemName; }

    // --- Setters ---
    public void setPartner(NPC partner) { this.partner = partner; }
    public void setPosition(int x, int y) { this.currentTileX = x; this.currentTileY = y; }
    public void setCurrentMap(MapArea map) { this.currentMap = map; }
    public void setFavoriteItemName(String itemName) { this.favoriteItemName = itemName; }

    // --- Pengubah State ---
    public void changeEnergy(int amount) {
        this.energy += amount;
        if (this.energy > MAX_ENERGY) {
            this.energy = MAX_ENERGY;
        } else if (this.energy < MIN_ENERGY) {
            this.energy = MIN_ENERGY;
        }
    }

    public void addGold(int amount) {
        if (amount > 0) {
            this.gold += amount;
        } else {
            System.out.println("Jumlah gold yang ditambahkan harus positif.");
        }
    }

    public boolean spendGold(int amount) {
        if (amount <= 0) {
             System.out.println("Jumlah gold yang dibelanjakan harus positif.");
             return false;
        }
        if (this.gold >= amount) {
            this.gold -= amount;
            return true;
        } else {
            System.out.println("Gold tidak cukup.");
            return false;
        }
    }

    // --- Metode Aksi Inti (Implementasi berdasarkan Spesifikasi Hal 25-28) ---
    // Catatan: Metode ini sering mengembalikan boolean untuk sukses/gagal.
    // Metode ini mengasumsikan Controller menyediakan objek target yang benar.
    // Biaya Energi/Waktu diterapkan oleh Controller *setelah* aksi berhasil.

    /**
     * Mencoba memindahkan pemain satu petak ke arah yang ditentukan.
     * Memeriksa batas peta dan halangan.
     *
     * @param direction Arah untuk bergerak.
     * @return true jika perpindahan berhasil, false jika gagal.
     */
    public boolean move(Direction direction) {
        int nextX = currentTileX;
        int nextY = currentTileY;

        switch (direction) {
            case NORTH: nextY--; break;
            case SOUTH: nextY++; break;
            case EAST:  nextX++; break;
            case WEST:  nextX--; break;
        }

        if (currentMap == null || !currentMap.isWithinBounds(nextX, nextY)) {
            System.out.println("Tidak bisa bergerak ke luar batas map.");
            return false;
        }
        if (currentMap.isOccupied(nextX, nextY)) {
            System.out.println("Jalan terhalang.");
            return false;
        }

        currentTileX = nextX;
        currentTileY = nextY;
        return true;
    }

    /**
     * Mencoba mencangkul Tile target. Membutuhkan Hoe.
     * Mengasumsikan Controller menyediakan objek Tile target yang benar.
     * Biaya energi (-5) ditangani oleh Controller.
     * Biaya waktu (5 menit) ditangani oleh Controller.
     *
     * @param targetTile Objek Tile yang akan dicangkul.
     * @return true jika pencangkulan berhasil, false jika gagal.
     */
    public boolean till(Tile targetTile) {
        if (!inventory.hasTool("Hoe")) {
            System.out.println("Kamu butuh cangkul (Hoe)!");
            return false;
        }
        if (targetTile == null || !targetTile.canBeTilled()) {
            System.out.println("Tanah ini tidak bisa dicangkul.");
            return false;
        }
        targetTile.till();
        System.out.println("Kamu mencangkul tanah.");
        return true;
    }

    /**
     * Mencoba mengembalikan tanah yang sudah dicangkul/ditanami menjadi tanah biasa. Membutuhkan Pickaxe.
     * Mengasumsikan Controller menyediakan objek Tile target yang benar.
     * Biaya energi (-5) ditangani oleh Controller.
     * Biaya waktu (5 menit) ditangani oleh Controller.
     *
     * @param targetTile Objek Tile yang akan dipulihkan.
     * @return true jika pemulihan berhasil, false jika gagal.
     */
    public boolean recoverLand(Tile targetTile) {
        if (!inventory.hasTool("Pickaxe")) {
            System.out.println("Kamu butuh beliung (Pickaxe)!");
            return false;
        }
        if (targetTile == null || !targetTile.canBeRecovered()) {
            System.out.println("Tidak ada yang bisa dipulihkan di sini.");
            return false;
        }
        targetTile.recover();
        System.out.println("Kamu memulihkan tanah.");
        return true;
    }

    /**
     * Mencoba menanam benih pada Tile target.
     * Mengasumsikan Controller menyediakan objek Tile target dan Seed yang benar.
     * Mengasumsikan Controller sudah memeriksa kecocokan musim.
     * Biaya energi (-5) ditangani oleh Controller.
     * Biaya waktu (5 menit) ditangani oleh Controller.
     *
     * @param seedToPlant Item Seed yang akan ditanam.
     * @param targetTile Objek Tile tempat menanam.
     * @return true jika penanaman berhasil, false jika gagal.
     */
    public boolean plant(Seed seedToPlant, Tile targetTile) {
        if (seedToPlant == null) return false;
        if (!inventory.hasItem(seedToPlant, 1)) {
            System.out.println("Kamu tidak punya benih " + seedToPlant.getName() + ".");
            return false;
        }
        boolean success = seedToPlant.use(this, targetTile);
        if (success) {
            inventory.removeItem(seedToPlant, 1);
        }
        return success;
    }

    /**
     * Mencoba menyiram Tile target. Membutuhkan Watering Can.
     * Mengasumsikan Controller menyediakan objek Tile target yang benar.
     * Biaya energi (-5) ditangani oleh Controller.
     * Biaya waktu (5 menit) ditangani oleh Controller.
     *
     * @param targetTile Objek Tile yang akan disiram.
     * @return true jika penyiraman berhasil, false jika gagal.
     */
    public boolean water(Tile targetTile) {
        if (!inventory.hasTool("Watering Can")) {
            System.out.println("Kamu butuh penyiram tanaman (Watering Can)!");
            return false;
        }
        if (targetTile == null || !targetTile.canBeWatered()) {
            System.out.println("Tidak perlu menyiram petak ini.");
            return false;
        }
        targetTile.water();
        System.out.println("Kamu menyiram tanaman.");
        return true;
    }

    /**
     * Mencoba memanen hasil tanaman dari Tile target.
     * Mengasumsikan Controller menyediakan objek Tile target yang benar.
     * Biaya energi (-5) ditangani oleh Controller.
     * Biaya waktu (5 menit) ditangani oleh Controller.
     *
     * @param targetTile Objek Tile tempat memanen.
     * @param itemRegistry Referensi ke database item untuk mendapatkan objek Crop.
     * @return true jika panen berhasil, false jika gagal.
     */
    public boolean harvest(Tile targetTile, Map<String, Item> itemRegistry) {
        if (targetTile == null || !targetTile.isHarvestable()) {
            System.out.println("Tidak ada yang bisa dipanen di sini.");
            return false;
        }
        List<Item> harvestedItems = targetTile.harvest(itemRegistry);
        if (harvestedItems != null && !harvestedItems.isEmpty()) {
            for (Item crop : harvestedItems) {
                inventory.addItem(crop, 1);
            }
            System.out.println("Kamu memanen " + harvestedItems.get(0).getName() + "!");
            return true;
        } else {
            System.out.println("Gagal memanen.");
            return false;
        }
    }

    /**
     * Mencoba memakan sebuah Item (yang harusnya EdibleItem).
     * Mengasumsikan Controller menyediakan objek Item yang benar.
     * Biaya waktu (5 menit) ditangani oleh Controller.
     *
     * @param itemToEat Item yang akan dikonsumsi.
     * @return true jika berhasil dimakan, false jika gagal.
     */
    public boolean eat(Item itemToEat) {
        if (itemToEat == null) return false;
        if (!(itemToEat instanceof EdibleItem)) {
            System.out.println(itemToEat.getName() + " tidak bisa dimakan.");
            return false;
        }
        if (!inventory.hasItem(itemToEat, 1)) {
            System.out.println("Kamu tidak punya " + itemToEat.getName() + ".");
            return false;
        }
        boolean success = itemToEat.use(this, null);
        if (success) {
            inventory.removeItem(itemToEat, 1);
        }
        return success;
    }

    /**
     * Memulai aksi tidur. Utamanya memulihkan energi.
     * Time skip ditangani oleh Controller.
     * Logika pemulihan energi berdasarkan Halaman 26 (penalti) dan bonus.
     *
     * @param energyBeforeSleep Energi pemain tepat sebelum tidur.
     * @param usedBonusBed Apakah tempat tidur bonus digunakan.
     */
    public void sleep(int energyBeforeSleep, boolean usedBonusBed) {
        int targetEnergy;
        boolean applyPenalty = energyBeforeSleep <= 0;

        if (applyPenalty) {
            targetEnergy = MAX_ENERGY / 2;
            System.out.println("Kamu tidur kelelahan... Energi pulih setengah.");
        } else {
            targetEnergy = MAX_ENERGY;
            System.out.println("Kamu tidur nyenyak. Energi pulih sepenuhnya.");
        }

        if (energyBeforeSleep == 0) {
            targetEnergy += 10;
             System.out.println("Bonus energi +10!");
        }

        if (usedBonusBed && energyBeforeSleep < LOW_ENERGY_THRESHOLD) {
            targetEnergy *= 2;
            System.out.println("Bonus tempat tidur aktif! Pemulihan energi digandakan.");
        }

        if (targetEnergy > MAX_ENERGY) targetEnergy = MAX_ENERGY;
        if (targetEnergy < MIN_ENERGY) targetEnergy = MIN_ENERGY;

        this.energy = targetEnergy;
    }

    /**
     * Mencoba memasak resep menggunakan bahan bakar.
     * Membutuhkan pengecekan bahan dan bahan bakar di inventory.
     * Biaya energi (-10 per percobaan) ditangani oleh Controller.
     * Biaya waktu (1 jam pasif) ditangani oleh Controller.
     *
     * @param recipe Resep yang akan dimasak.
     * @param fuelItem Item bahan bakar (Coal atau Firewood) yang digunakan.
     * @param itemRegistry Untuk mendapatkan objek Makanan hasil.
     * @return true jika persiapan memasak valid (bahan/bahan bakar ada), false jika gagal.
     *         Penambahan item hasil terjadi setelah waktu pasif.
     */
    public boolean cook(Recipe recipe, Item fuelItem, Map<String, Item> itemRegistry) {
        if (recipe == null || fuelItem == null || itemRegistry == null) return false;

        if (!fuelItem.getName().equals("Coal") && !fuelItem.getName().equals("Firewood")) {
            System.out.println("Bahan bakar tidak valid.");
            return false;
        }
        if (!inventory.hasItem(fuelItem, 1)) {
            System.out.println("Kamu tidak punya " + fuelItem.getName() + ".");
            return false;
        }

        for (Map.Entry<String, Integer> entry : recipe.getIngredients().entrySet()) {
            String ingredientName = entry.getKey();
            int requiredQty = entry.getValue();
            Item ingredient = itemRegistry.get(ingredientName);
            if (ingredient == null || !inventory.hasItem(ingredient, requiredQty)) {
                System.out.println("Kamu kekurangan bahan: " + requiredQty + " " + ingredientName);
                return false;
            }
        }

        inventory.removeItem(fuelItem, 1);
        for (Map.Entry<String, Integer> entry : recipe.getIngredients().entrySet()) {
            inventory.removeItem(itemRegistry.get(entry.getKey()), entry.getValue());
        }

        System.out.println("Kamu mulai memasak " + recipe.getResultItemName() + "...");
        return true;
    }

    /**
     * Memulai aksi memancing di lokasi saat ini.
     * Biaya energi (-5 per percobaan) ditangani oleh Controller.
     * Biaya waktu (15 menit setup + waktu RNG) ditangani oleh Controller.
     *
     * @param fishingLocation Tipe lokasi tempat pemain memancing.
     */
    public void fish(LocationType fishingLocation) {
        if (!inventory.hasTool("FishingRod")) {
            System.out.println("Kamu butuh pancing (Fishing Rod)!");
            return;
        }
        System.out.println("Kamu mulai memancing...");
    }

    /**
     * Mencoba melamar seorang NPC. Membutuhkan Proposal Ring.
     * Biaya energi (-10 jika diterima, -20 jika ditolak) ditangani oleh Controller.
     * Biaya waktu (1 jam) ditangani oleh Controller.
     *
     * @param npcTarget NPC yang akan dilamar.
     * @param ring Item ProposalRing.
     * @return true jika percobaan lamaran dilakukan (cincin ada), false jika gagal.
     *         Logika sukses/gagal sebenarnya kompleks dan kemungkinan di Controller.
     */
    public boolean propose(NPC npcTarget, ProposalRing ring) {
        if (npcTarget == null || ring == null) return false;
        boolean canUse = ring.use(this, npcTarget);
        if (!canUse) return false;
        return true; // Percobaan dilakukan
    }

    /**
     * Mencoba menikahi seorang NPC (harus tunangan).
     * Time skip (seharian + skip ke 22:00) ditangani oleh Controller.
     *
     * @param npcTarget NPC yang akan dinikahi.
     * @return true jika kondisi pernikahan terpenuhi di awal, false jika gagal.
     */
    public boolean marry(NPC npcTarget) {
        if (npcTarget == null || this.partner != npcTarget || npcTarget.getRelationshipStatus() != RelationshipStatus.FIANCE) {
            System.out.println("Kamu hanya bisa menikahi tunanganmu.");
            return false;
        }
        System.out.println("Hari pernikahan dengan " + npcTarget.getName() + "!");
        return true; // Kondisi terpenuhi
    }

    /**
     * Menonton TV (jika diimplementasikan sebagai BONUS).
     * Biaya energi (-5) ditangani oleh Controller.
     * Biaya waktu (15 menit) ditangani oleh Controller.
     * @return Cuaca yang diprediksi untuk besok (atau null jika TV tidak tersedia).
     */
    public Weather watchTV() {
        System.out.println("Kamu menonton TV...");
        return null; // Placeholder
    }

    /**
     * Memindahkan pemain ke area peta yang berbeda (lokasi World Map).
     * Biaya energi (-10) ditangani oleh Controller.
     * Biaya waktu (15 menit) ditangani oleh Controller.
     *
     * @param destinationMap Objek MapArea tujuan.
     * @param entryX Koordinat X tujuan di peta baru.
     * @param entryY Koordinat Y tujuan di peta baru.
     * @return true (aksi mengunjungi selalu mungkin secara konseptual).
     */
    public boolean visit(MapArea destinationMap, int entryX, int entryY) {
        if (destinationMap == null) return false;
        System.out.println("Kamu pergi ke " + destinationMap.getName() + "...");
        this.currentMap = destinationMap;
        this.currentTileX = entryX;
        this.currentTileY = entryY;
        return true;
    }

    /**
     * Berbicara dengan seorang NPC.
     * Mengasumsikan Controller menyediakan target NPC.
     * Biaya energi (-10) ditangani oleh Controller.
     * Biaya waktu (10 menit atau berdasarkan dialog) ditangani oleh Controller.
     *
     * @param npcTarget NPC yang diajak bicara.
     * @return true (aksi berbicara selalu mungkin jika NPC bisa ditarget).
     */
    public boolean chat(NPC npcTarget) {
        if (npcTarget == null) return false;
        npcTarget.interact(this); // NPC menampilkan dialog
        npcTarget.addHeartPoints(10); // Player menambahkan poin
        return true;
    }

    /**
     * Memberikan hadiah item kepada seorang NPC.
     * Mengasumsikan Controller menyediakan target NPC dan Item.
     * Biaya energi (-5) ditangani oleh Controller.
     * Biaya waktu (10 menit) ditangani oleh Controller.
     *
     * @param npcTarget NPC yang diberi hadiah.
     * @param itemToGift Item yang diberikan.
     * @return true jika pemberian hadiah berhasil (item ada), false jika gagal.
     */
    public boolean gift(NPC npcTarget, Item itemToGift) {
        if (npcTarget == null || itemToGift == null) return false;
        if (!inventory.hasItem(itemToGift, 1)) {
            System.out.println("Kamu tidak punya " + itemToGift.getName() + " untuk diberikan.");
            return false;
        }
        int pointsChange = npcTarget.checkGiftPreference(itemToGift);
        npcTarget.addHeartPoints(pointsChange);
        System.out.println("Kamu memberikan " + itemToGift.getName() + " kepada " + npcTarget.getName() + ".");
        inventory.removeItem(itemToGift, 1);
        return true;
    }

    /**
     * Menambahkan item ke shipping bin untuk dijual.
     * Mengasumsikan Controller menyediakan Item dan kuantitas.
     * Biaya waktu (15 menit setelah selesai) ditangani oleh Controller.
     *
     * @param itemToSell Item yang dimasukkan ke bin.
     * @param quantity Jumlah yang dimasukkan ke bin.
     * @param shippingBin Objek ShippingBin.
     * @return true jika item berhasil ditambahkan, false jika gagal.
     */
    public boolean sellItemToBin(Item itemToSell, int quantity, ShippingBin shippingBin) {
         // Validasi input dasar
         if (itemToSell == null || quantity <= 0 || shippingBin == null) {
             System.out.println("Input tidak valid untuk menjual item.");
             return false;
         }

         // 1. Periksa apakah pemain memiliki cukup item
         if (!inventory.hasItem(itemToSell, quantity)) {
              System.out.println("Kamu tidak punya cukup " + itemToSell.getName() + " untuk dijual.");
             return false;
         }

         // 2. Coba tambahkan item ke Shipping Bin
         // Mengasumsikan metode addItem di ShippingBin mengembalikan boolean
         // (true jika berhasil, false jika gagal, misal karena bin penuh untuk item unik itu)
         boolean added = shippingBin.addItem(itemToSell, quantity);

         if (added) {
             // 3. Jika berhasil ditambahkan ke bin, hapus dari inventory pemain
             inventory.removeItem(itemToSell, quantity);
             System.out.println("Kamu memasukkan " + quantity + " " + itemToSell.getName() + " ke Shipping Bin.");
             // Controller akan menangani biaya waktu setelah pemain selesai sesi penjualan.
             return true;
         } else {
             // Pesan error jika gagal ditambahkan (misalnya, bin penuh)
             System.out.println("Tidak bisa menambahkan " + itemToSell.getName() + " ke Shipping Bin. Mungkin sudah penuh?");
             return false;
         }
    } 

    // Anda bisa menambahkan metode helper lain di sini jika diperlukan
    // Misalnya:
    // private Tile getFacingTile() { ... }
    // private NPC getNearbyNPC() { ... }

} 