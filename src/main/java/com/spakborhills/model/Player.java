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
import com.spakborhills.model.Enum.TileType;
import com.spakborhills.model.Enum.Weather;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Enum.Season;
// import com.spakborhills.model.Item.Crop;
import com.spakborhills.model.Item.EdibleItem;
import com.spakborhills.model.Item.Seed;
// import com.spakborhills.model.Item.Equipment; // Pastikan ada
import com.spakborhills.model.Item.ProposalRing;
import com.spakborhills.model.NPC.NPC; // Pastikan ada
import com.spakborhills.model.Map.MapArea;
import com.spakborhills.model.Map.Tile;
import com.spakborhills.model.Util.GameTime;
// import com.spakborhills.model.Util.GameTime; // Anda mungkin perlu ini di Controller
import com.spakborhills.model.Util.Inventory;
import com.spakborhills.model.Util.Recipe; // Pastikan ada
import com.spakborhills.model.Util.ShippingBin; // Pastikan ada
import com.spakborhills.model.Item.Equipment; // Pastikan import Equipment ada

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
    private Item selectedItem; // Ditambahkan

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
        this.selectedItem = null; // Inisialisasi selectedItem

        // Inisialisasi inventory dengan item default (Halaman 23)
        if (itemRegistry != null) {
            Item parsnipSeeds = itemRegistry.get("Parsnip Seeds");
            Item hoe = itemRegistry.get("Hoe");
            Item wateringCan = itemRegistry.get("Watering Can");
            Item pickaxe = itemRegistry.get("Pickaxe");
            Item fishingRod = itemRegistry.get("Fishing Rod");

            if (parsnipSeeds != null) this.inventory.addItem(parsnipSeeds, 15); else System.err.println("PERINGATAN: Parsnip Seeds tidak ditemukan di registry.");
            if (hoe != null) {
                this.inventory.addItem(hoe, 1);
                if (this.selectedItem == null) { // Set Hoe sebagai default jika belum ada
                    this.selectedItem = hoe;
                }
            } else System.err.println("PERINGATAN: Hoe tidak ditemukan di registry.");
            if (wateringCan != null) {
                this.inventory.addItem(wateringCan, 1);
                if (this.selectedItem == null || !(this.selectedItem instanceof Equipment && ((Equipment)this.selectedItem).getToolType().equals("WateringCan"))) { // Prioritaskan WateringCan jika ada
                    // Atau jika selectedItem bukan watering can, ganti dengan watering can
                    // Jika ingin Hoe jadi default utama, tukar logika ini atau set selectedItem ke hoe setelah semua item ditambah
                     if (this.selectedItem == null && hoe == null) { // Jika Hoe tidak ada dan selectedItem masih null
                        this.selectedItem = wateringCan;
                    } else if (hoe != null && this.selectedItem == hoe) { 
                        // Jika selectedItem sudah Hoe, biarkan. Jika ingin selalu ganti ke WateringCan jika ada, hapus kondisi ini.
                        // Untuk sekarang, jika ada Hoe dan Watering Can, Hoe akan jadi selected default.
                        // Jika ingin WC jadi default, maka set this.selectedItem = wateringCan di sini.
                        // Saya akan set Hoe sebagai default jika ada, baru WC jika Hoe tidak ada.
                    } else if (hoe == null) { // Jika Hoe tidak ada, jadikan WC sebagai default
                         this.selectedItem = wateringCan;
                    }
                }
            } else System.err.println("PERINGATAN: Watering Can tidak ditemukan di registry.");
            if (pickaxe != null) {
                this.inventory.addItem(pickaxe, 1);
                 if (this.selectedItem == null && hoe == null && wateringCan == null) { // Jika belum ada tool terpilih
                    this.selectedItem = pickaxe;
                }
            } else System.err.println("PERINGATAN: Pickaxe tidak ditemukan di registry.");
            if (fishingRod != null) {
                this.inventory.addItem(fishingRod, 1);
                if (this.selectedItem == null && hoe == null && wateringCan == null && pickaxe == null) { // Jika belum ada tool terpilih
                    this.selectedItem = fishingRod;
                }
            } else System.err.println("PERINGATAN: Fishing Rod tidak ditemukan di registry.");
            
            // Fallback: jika setelah semua item default dicek dan selectedItem masih null (misal semua item tool gagal di-load)
            // coba ambil item pertama dari inventory jika ada.
            if (this.selectedItem == null && !this.inventory.getItems().isEmpty()) {
                for (Item itemInInventory : this.inventory.getItems().keySet()) {
                    if (itemInInventory instanceof Equipment) {
                        this.selectedItem = itemInInventory;
                        break;
                    }
                }
                // Jika masih null, ambil item apapun
                if (this.selectedItem == null) {
                     this.selectedItem = this.inventory.getItems().keySet().iterator().next();
                }
            }


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
    public Item getSelectedItem() { return selectedItem; } // Ditambahkan

    // --- Setters ---
    public void setPartner(NPC partner) { this.partner = partner; }
    public void setPosition(int x, int y) { this.currentTileX = x; this.currentTileY = y; }
    public void setCurrentMap(MapArea map) { this.currentMap = map; }
    public void setFavoriteItemName(String itemName) { this.favoriteItemName = itemName; }
    public void setSelectedItem(Item item) { // Ditambahkan
        // Opsional: Cek apakah item ada di inventory sebelum di-set
        // if (inventory.hasItem(item, 1) || item == null) { // item bisa null jika ingin "unequip"
             this.selectedItem = item;
        // } else {
        //     System.err.println("Error: Mencoba memilih item yang tidak ada di inventory.");
        // }
    }

    // --- Pengubah State ---
    public void changeEnergy(int amount) {
        long newEnergy = (long)this.energy + amount; 
        if (newEnergy > MAX_ENERGY) {
            this.energy = MAX_ENERGY;
        } else if (newEnergy < MIN_ENERGY) {
            this.energy = MIN_ENERGY;
        } else {
            this.energy = (int)newEnergy;
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
        // Validasi dasar
        if (targetTile == null) {
            System.out.println("Tidak ada tile target untuk dicangkul.");
            return false;
        }

        // Periksa apakah pemain memiliki Hoe dan sedang dipilih
        boolean hasHoe = false;
        if (selectedItem instanceof Equipment) {
            Equipment currentTool = (Equipment) selectedItem;
            if (currentTool.getToolType().equalsIgnoreCase("Hoe")) {
                hasHoe = true;
            }
        }

        if (!hasHoe) {
            System.out.println("Kamu membutuhkan Hoe untuk mencangkul tanah dan Hoe harus dipilih.");
            // Bisa juga cek inventory.hasTool("Hoe") jika selectedItem bukan Hoe tapi ada di inv
            return false;
        }
        
        // Periksa apakah tile bisa dicangkul
        if (!targetTile.canBeTilled()) {
            System.out.println("Tile ini tidak bisa dicangkul.");
            // (Misalnya sudah dicangkul, ada objek, atau tipe yang tidak bisa dicangkul)
            return false;
        }

        // Lakukan aksi pencangkulan (mengubah tipe Tile)
        targetTile.setType(TileType.TILLED);
        System.out.println("Tanah berhasil dicangkul!");
        return true; // Berhasil
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
        boolean hasPickaxe = false;
        if (selectedItem instanceof Equipment) {
            Equipment currentTool = (Equipment) selectedItem;
            if (currentTool.getToolType().equalsIgnoreCase("Pickaxe")) {
                hasPickaxe = true;
            }
        }

        if (!hasPickaxe) {
            System.out.println("[Player.recoverLand] Action failed: Pickaxe is not selected. Selected: " + (selectedItem != null ? selectedItem.getName() : "None"));
            return false;
        }

        if (targetTile == null || !targetTile.canBeRecovered()) {
            System.out.println("[Player.recoverLand] Action failed: Nothing to recover or tile is null. Tile type: " + (targetTile != null ? targetTile.getType() : "null"));
            return false;
        }
        targetTile.setType(TileType.TILLABLE);
        System.out.println("[Player.recoverLand] Action success: Land recovered.");
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
     * @param currentTime Objek GameTime untuk mendapatkan musim saat ini.
     * @return true jika penanaman berhasil, false jika gagal.
     */
    public boolean plant(Seed seedToPlant, Tile targetTile, GameTime currentTime) {
        if (seedToPlant == null) {
            System.out.println("[Player.plant] Error: Seed to plant is null.");
            return false;
        }
        if (targetTile == null) {
            System.out.println("[Player.plant] Error: Target tile is null.");
            return false;
        }
        if (currentTime == null) {
            System.out.println("[Player.plant] Error: GameTime (currentTime) is null.");
            return false;
        }

        if (targetTile.getType() != TileType.TILLED) { 
            System.out.println("[Player.plant] Action failed: Tile is not tilled. Tile type: " + targetTile.getType());
            return false;
        }

        if (targetTile.getPlantedSeed() != null) {
            System.out.println("[Player.plant] Action failed: Tile already has a plant.");
            return false;
        }

        // Corrected season check using getTargetSeason() and direct comparison
        Season currentSeasonValue = currentTime.getCurrentSeason();
        Season seedTargetSeasonValue = seedToPlant.getTargetSeason();
        if (seedTargetSeasonValue != Season.ANY && seedTargetSeasonValue != currentSeasonValue) {
            System.out.println("[Player.plant] Action failed: " + seedToPlant.getName() + " cannot be planted in " + currentSeasonValue + ". Required season: " + seedTargetSeasonValue + ".");
            return false;
        }

        if (inventory.hasItem(seedToPlant, 1)) {
            // Menggunakan metode setPlantedSeed dari Tile.java yang menerima Seed dan Season
            boolean plantSuccess = targetTile.setPlantedSeed(seedToPlant, currentSeasonValue); 
            if (plantSuccess) {
                inventory.removeItem(seedToPlant, 1);
                System.out.println("[Player.plant] Action success: " + seedToPlant.getName() + " planted.");
                return true;
            } else {
                // Pesan error spesifik dari Tile.setPlantedSeed() jika ada, atau pesan ini jika gagal karena alasan lain di sana
                System.out.println("[Player.plant] Action failed: Tile.setPlantedSeed returned false (check Tile's internal logic, e.g. season check there too).");
                return false;
            }
        } else {
            System.out.println("[Player.plant] Action failed: Player does not have " + seedToPlant.getName() + ".");
            return false;
        }
    }

    /**
     * Mencoba menyiram Tile target. Membutuhkan Watering Can.
     * Mengasumsikan Controller menyediakan objek Tile target yang benar.
     * Biaya energi (-5) ditangani oleh Controller.
     * Biaya waktu (5 menit) ditangani oleh Controller.
     *
     * @param targetTile Objek Tile yang akan disiram.
     * @return true jika berhasil menyiram, false jika gagal.
     */
    public boolean water(Tile targetTile, Weather currentWeather) { // currentWeather tidak digunakan di sini karena Tile.markAsWatered() tidak memerlukannya secara langsung
                                                                    // Tile.updateDaily() yang akan menghandle efek hujan
        if (targetTile == null) {
            System.out.println("[Player.water] Error: Target tile is null.");
            return false;
        }

        boolean hasWateringCan = false;
        if (selectedItem instanceof Equipment) {
            Equipment currentTool = (Equipment) selectedItem;
            String actualToolType = currentTool.getToolType(); // Get the tool type
            System.out.println("[Player.water] Debug: Selected item is Equipment. Name: " + currentTool.getName() + ", Actual ToolType: '" + actualToolType + "'"); 
            
            if (actualToolType != null && actualToolType.equalsIgnoreCase("WateringCan")) {
                hasWateringCan = true;
            } else {
                System.out.println("[Player.water] Debug: ToolType mismatch or null. Expected 'WateringCan' (case-insensitive), got '" + actualToolType + "'. Comparison result: " + (actualToolType != null && actualToolType.equalsIgnoreCase("WateringCan")) );
            }
        } else if (selectedItem != null) {
            System.out.println("[Player.water] Debug: Selected item '" + selectedItem.getName() + "' is NOT an instance of Equipment. It is: " + selectedItem.getClass().getName());
        } else {
            System.out.println("[Player.water] Debug: selectedItem is null.");
        }

        if (!hasWateringCan) {
            System.out.println("[Player.water] Action failed because hasWateringCan is false. (Original log message was: Watering Can is not selected or current item is not Equipment). Selected: " + (selectedItem != null ? selectedItem.getName() : "None"));
            return false;
        }

        if (!targetTile.canBeWateredInternalCheck()) {
            System.out.println("[Player.water] Action failed: Tile cannot be watered (not TILLED or PLANTED). Tile type: " + targetTile.getType());
            return false;
        }
        
        if (targetTile.isWatered()) {
            System.out.println("[Player.water] Info: Tile is already watered.");
            return false; 
        }

        targetTile.markAsWatered();
        System.out.println("[Player.water] Action success: Tile watered!");
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
        // TODO: Implementasi lebih detail terkait item spesifik dari tanaman
        if (targetTile == null || !targetTile.isHarvestable() || targetTile.getPlantedSeed() == null) {
            System.out.println("Player.harvest: Tidak ada yang bisa dipanen atau tile tidak valid.");
            return false;
        }
        // Diasumsikan Tile.harvest() akan mengembalikan daftar Item hasil panen atau null/kosong jika gagal
        List<Item> harvestedProduce = targetTile.processHarvest(itemRegistry); // processHarvest HARUS mereset tile

        if (harvestedProduce != null && !harvestedProduce.isEmpty()) {
            boolean allAdded = true;
            for(Item item : harvestedProduce){
                if (item != null) { // Pastikan item tidak null sebelum menambahkannya
                    this.inventory.addItem(item, 1); // Untuk sekarang, asumsikan setiap hasil panen adalah 1 unit
                    System.out.println("Player memanen: " + item.getName());
                } else {
                    allAdded = false; // Tandai jika ada item null dalam hasil panen
                }
            }
            if (!allAdded) {
                System.err.println("Peringatan: Beberapa item hasil panen null dan tidak ditambahkan.");
            }
            // Tidak ada biaya energi eksplisit untuk panen di spesifikasi,
            // namun controller akan tetap memanggil changeEnergy(-5) sebagai contoh.
            // Jika ingin panen gratis energi, hapus changeEnergy di controller atau buat nol.
            return true;
        } else {
            System.out.println("Player.harvest: Gagal memanen atau tidak ada hasil dari tile.");
            return false;
        }
    }

    /**
     * Pemain mencoba memakan item yang dipilih.
     * Hanya item yang mengimplementasikan EdibleItem yang bisa dimakan.
     * Memulihkan energi dan mengurangi item dari inventory.
     * @param itemToEat Item yang akan dimakan.
     * @return true jika item berhasil dimakan, false jika tidak.
     */
    public boolean eat(Item itemToEat) {
        if (itemToEat == null) {
            System.out.println("Player.eat: Tidak ada item yang dipilih untuk dimakan.");
            return false;
        }

        if (itemToEat instanceof EdibleItem) {
            // Cek dulu apakah pemain punya item tersebut sebelum mencoba mengurangi
            if (!inventory.hasItem(itemToEat, 1)) {
                System.out.println("Player.eat: Pemain tidak memiliki " + itemToEat.getName() + " untuk dimakan.");
                return false; // Seharusnya tidak terjadi jika itemToEat adalah selectedItem yang valid dari inventory
            }

            EdibleItem edible = (EdibleItem) itemToEat;
            int energyRestored = edible.getEnergyRestore();

            if (this.energy >= MAX_ENERGY && energyRestored > 0) {
                System.out.println("Player.eat: Energi sudah penuh, tidak bisa makan " + itemToEat.getName() + " untuk memulihkan energi.");
                // Pertimbangkan apakah tetap mengonsumsi item jika energi sudah penuh.
                // Untuk saat ini, jika energi penuh dan item memberi energi positif, makan dibatalkan untuk hemat item.
                // Jika item memberi energi negatif (poison?), mungkin tetap dikonsumsi.
                // Untuk simplicity, kita batalkan jika energi penuh & restore positif.
                return false;
            }
            
            // Hapus item dari inventory DULU, baru tambah energi.
            // Ini mencegah situasi di mana energi bertambah tapi item gagal dihapus (meskipun kecil kemungkinannya di sini).
            boolean removed = inventory.removeItem(itemToEat, 1);
            if (removed) {
                changeEnergy(energyRestored);
                System.out.println(this.name + " memakan " + itemToEat.getName() + ". Energi pulih: " + energyRestored + ". Energi sekarang: " + this.energy);
                // Jika item adalah Equipment setelah dimakan (misal potion dengan efek sementara),
                // selectedItem mungkin perlu di-clear atau diganti.
                // Untuk EdibleItem biasa (Crop, Food), selectedItem akan menjadi null jika stack habis,
                // dan Player/Controller harus menangani pemilihan item berikutnya.
                // Jika itemToEat adalah selectedItem dan jumlahnya jadi 0, selectedItem harus di-update.
                // Logic ini sebaiknya ada di Controller atau setelah pemanggilan eat()
                if (inventory.getItemCount(itemToEat) == 0 && itemToEat.equals(this.selectedItem)) {
                    // Jika item yang dimakan adalah selectedItem dan habis, selectedItem jadi null
                    // Controller akan perlu logika untuk memilih item berikutnya atau handle selectedItem null.
                    // this.setSelectedItem(null); // Player tidak seharusnya mengatur selected itemnya sendiri secara langsung berdasarkan aksi ini.
                                              // Controller yang harusnya mengelola selectedItem.
                }
                return true;
            } else {
                // Ini seharusnya tidak terjadi jika hasItem(itemToEat, 1) true.
                System.err.println("Player.eat: Gagal menghapus " + itemToEat.getName() + " dari inventory meskipun awalnya terdeteksi.");
                return false;
            }
        } else {
            System.out.println("Player.eat: " + itemToEat.getName() + " tidak bisa dimakan.");
            return false;
        }
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
        changeEnergy(-10); // hanya untuk testing

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
        if (!inventory.hasTool("Fishing Rod")) {
            System.out.println("Kamu butuh pancing (Fishing Rod)!");
            return;
        }
        changeEnergy(-5); // hanya untuk testing
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
        
        if (npcTarget == null || ring == null) {
            System.out.println("Target NPC atau cincin tidak valid untuk melamar.");
            return false;
        }

        // Panggil ProposalRing.use() untuk efek awal / pesan (meski saat ini hanya validasi dasar)
        // Sebenarnya, Player.propose yang harusnya memegang semua logika inti.
        // ring.use(this, npcTarget); // Ini hanya print pesan, tidak krusial untuk logika di sini

        System.out.println(this.getName() + " mengeluarkan Proposal Ring dan mencoba melamar " + npcTarget.getName() + "...");

        if (!npcTarget.isBachelor()) {
            System.out.println(npcTarget.getName() + " tidak bisa dilamar (bukan bachelor/bachelorette).");
            changeEnergy(-5); // Misal, energi tetap berkurang untuk usaha
            return false;
        }

        // Asumsi proposal memerlukan poin hati maksimal untuk bachelor/bachelorette
        if (npcTarget.getHeartPoints() < npcTarget.getMaxHeartPoints()) {
            System.out.println("Sayangnya, " + npcTarget.getName() + " merasa hubungan kalian belum cukup dekat untuk lamaran.");
            changeEnergy(-10); // Energi berkurang lebih banyak jika ditolak karena hati
            return false;
        }

        // Lamaran berhasil!
        System.out.println(npcTarget.getName() + " menerima lamaranmu dengan bahagia!");
        npcTarget.setRelationshipStatus(RelationshipStatus.FIANCE);
        this.setPartner(npcTarget); // Pemain kini punya tunangan
        changeEnergy(-15); // Energi berkurang untuk lamaran sukses
        // Hapus ProposalRing jika itemnya consumable, atau biarkan jika reusable
        // Berdasarkan spek Hal 27, ProposalRing tidak hilang.
        // inventory.removeItem(ring, 1); 
        return true;
    }

    /**
     * Mencoba menikahi seorang NPC (harus tunangan).
     * Time skip (seharian + skip ke 22:00) ditangani oleh Controller.
     *
     * @param npcTarget NPC yang akan dinikahi.
     * @return true jika kondisi pernikahan terpenuhi di awal, false jika gagal.
     */
    public boolean marry(NPC npcTarget) {
        if (npcTarget == null ) {
            System.out.println("Target NPC tidak valid untuk menikah.");
            return false;
        }
        if (this.partner != npcTarget || npcTarget.getRelationshipStatus() != RelationshipStatus.FIANCE) {
            System.out.println("Kamu hanya bisa menikahi tunanganmu saat ini, " + (this.partner != null ? this.partner.getName() : "belum ada") + ".");
            if (npcTarget.getRelationshipStatus() != RelationshipStatus.FIANCE) {
                 System.out.println(npcTarget.getName() + " belum menjadi tunanganmu.");
            }
            return false;
        }
        
        System.out.println("Hari pernikahan dengan " + npcTarget.getName() + " telah tiba! Selamat!");
        npcTarget.setRelationshipStatus(RelationshipStatus.SPOUSE);
        // Energi dan waktu diatur oleh GameController/Farm.
        return true; 
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
        if (npcTarget == null || itemToGift == null) {
            System.out.println("NPC target or item to gift cannot be null.");
            return false;
        }
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
     * Mencoba menjual item ke Shipping Bin.
     * Memeriksa apakah pemain memiliki item yang cukup.
     * Tidak langsung mengubah gold pemain (itu terjadi di akhir hari).
     *
     * @param itemToSell  Item yang akan dijual.
     * @param quantity    Jumlah item yang akan dijual.
     * @param shippingBin Objek ShippingBin.
     * @param currentDay  Hari saat ini dalam game, untuk validasi canSellToday.
     * @return true jika item berhasil ditaruh di bin, false jika gagal.
     */
    public boolean sellItemToBin(Item itemToSell, int quantity, ShippingBin shippingBin, int currentDay) {
        if (itemToSell == null || quantity <= 0) {
            System.err.println(this.name + " mencoba menjual item tidak valid atau kuantitas nol.");
            return false;
        }
        if (shippingBin == null) {
            System.err.println("ShippingBin tidak boleh null untuk menjual item.");
             return false;
         }

        // Validasi apakah bisa menjual hari ini - Panggil canSellToday() tanpa argumen
        if (!shippingBin.canSellToday()) { // Removed currentDay argument
            System.out.println(this.name + ": Sudah melakukan penjualan via Shipping Bin hari ini. Coba lagi besok.");
             return false;
         }

        if (!this.inventory.hasItem(itemToSell, quantity)) {
            System.out.println(this.name + " tidak punya cukup " + itemToSell.getName() + " (" + quantity + ") untuk dijual. Hanya punya: " + this.inventory.getItemCount(itemToSell));
            return false;
        }

        // Coba hapus dari inventory dulu
        if (this.inventory.removeItem(itemToSell, quantity)) {
            // Jika berhasil dihapus dari inventory, coba tambahkan ke bin
            if (shippingBin.addItem(itemToSell, quantity)) {
                System.out.println(this.name + " menaruh " + quantity + " " + itemToSell.getName() + " ke Shipping Bin.");
                // Tidak ada pengurangan energi atau perubahan gold di sini
                // Efek waktu 15 menit akan ditangani Controller jika relevan
             return true;
         } else {
                // Gagal menambahkan ke bin (misal, bin penuh slot unik), kembalikan item ke inventory
                this.inventory.addItem(itemToSell, quantity); // Rollback
                System.out.println(this.name + ": Gagal menaruh item ke Shipping Bin (mungkin penuh slot unik). Item dikembalikan ke inventory.");
                return false;
            }
        } else {
            // Seharusnya tidak sampai sini jika hasItem() dan logika removeItem() benar
            System.err.println(this.name + ": Gagal menghapus " + itemToSell.getName() + " dari inventory meskipun pengecekan awal berhasil.");
             return false;
         }
    } 

    // Anda bisa menambahkan metode helper lain di sini jika diperlukan
    // Misalnya:
    // private Tile getFacingTile() { ... }
    // private NPC getNearbyNPC() { ... }

} 