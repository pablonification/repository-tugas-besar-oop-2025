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
import java.util.ArrayList;
import java.awt.Image; // Impor Image
import java.awt.image.BufferedImage; // Impor BufferedImage
import javax.imageio.ImageIO; // Impor ImageIO
import java.io.IOException; // Impor IOException

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
import com.spakborhills.model.Item.Fish;
import com.spakborhills.model.NPC.NPC; // Pastikan ada
import com.spakborhills.model.Map.MapArea;
import com.spakborhills.model.Map.Tile;
import com.spakborhills.model.Map.FarmMap; // Impor FarmMap
import com.spakborhills.model.Util.GameTime;
// import com.spakborhills.model.Util.GameTime; // Anda mungkin perlu ini di Controller
import com.spakborhills.model.Util.Inventory;
import com.spakborhills.model.Util.Recipe; // Pastikan ada
import com.spakborhills.model.Util.ShippingBin; // Pastikan ada
import com.spakborhills.model.Item.Equipment; // Pastikan import Equipment ada
import com.spakborhills.model.Util.EndGameStatistics; // Pastikan import EndGameStatistics ada


public class Player {
    // --- Konstanta ---
    public static final int MAX_ENERGY = 100;
    public static final int MIN_ENERGY = -20;
    public static final int LOW_ENERGY_THRESHOLD = 10;
    private static final int DEFAULT_STARTING_GOLD = 500; 
    public static final int CHAT_MAX_DISTANCE = 1;
    public static final int CHAT_ENERGY_COST = 10;
    private static final int CHAT_TIME_ADVANCE_MINUTES = 10;
    private static final int CHAT_HEART_POINTS_GAIN = 10;

    // --- Atribut ---
    private String name;
    private Gender gender;
    private int energy;
    private String farmName;
    private int gold;
    private final Inventory inventory;
    private MapArea currentMap;
    private int currentTileX;
    private int currentTileY;
    private NPC partner; // null jika single
    private String favoriteItemName;
    private Item selectedItem; // Ditambahkan
    private int engagementDay;
    private List<String> unlockedRecipes; // Added for save/load

    // Atribut untuk sprite dan animasi pemain
    private String spritesheetPathPlayer; // Ganti nama agar tidak konflik dengan field NPC jika ada subclassing
    private transient BufferedImage fullSpritesheetPlayer;
    public int spriteWidthPlayer, spriteHeightPlayer; // Dimensi asli SATU frame

    private Direction currentDirection;
    private boolean isMoving;
    private int animationFrame;
    private int animationCounter;
    private static final int ANIMATION_SPEED = 10; // Ganti frame setiap X panggilan updateAnimation
    private static final int WALKING_FRAMES = 4; // Jumlah frame animasi berjalan per arah (misal, kiri-kanan)

    // Anda perlu mendefinisikan Y offset di spritesheet untuk setiap arah
    // Ini SANGAT tergantung layout spritesheet Anda. Anggap saja:
    // Baris 0: Menghadap Bawah (SOUTH)
    // Baris 1: Menghadap Atas (NORTH)
    // Baris 2: Menghadap Kiri (WEST)
    // Baris 3: Menghadap Kanan (EAST)
    // Frame 0 = Diam, Frame 1 = Kaki Kiri, Frame 2 = Kaki Kanan (atau sebaliknya)
    // Jika spritesheet Anda punya lebih banyak frame per animasi (misal 4), sesuaikan WALKING_FRAMES
    // dan logika di getCurrentSpriteFrame.
    // Ini adalah contoh, Anda HARUS menyesuaikannya dengan spritesheet Anda.
    private int spriteSheetRowDown = 0;    // Y-offset di spritesheet untuk menghadap bawah (kali spriteHeightPlayer)
    private int spriteSheetRowUp = 2;      // Y-offset di spritesheet untuk menghadap atas
    private int spriteSheetRowLeft = 3;    // Y-offset di spritesheet untuk menghadap kiri
    private int spriteSheetRowRight = 1;   // Y-offset di spritesheet untuk menghadap kanan
    // Jika setiap baris HANYA berisi frame animasi untuk satu arah, maka Y offsetnya adalah `baris_ke * spriteHeightPlayer`

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
    public Player(String name, Gender gender, String farmName, MapArea startMap, int startX, int startY,
                  Map<String, Item> itemRegistry, String spritesheetPath,
                  int spriteWidth, int spriteHeight) { // Hanya perlu lebar & tinggi satu frame
        this.name = name;
        this.gender = gender;
        this.farmName = farmName;
        this.energy = MAX_ENERGY;
        this.gold = DEFAULT_STARTING_GOLD;
        this.inventory = new Inventory();
        this.currentMap = startMap;
        this.currentTileX = startX;
        this.currentTileY = startY;
        this.favoriteItemName = "";
        this.partner = null;
        this.selectedItem = null;
        this.engagementDay = -1;

        // Inisialisasi sprite
        this.spritesheetPathPlayer = spritesheetPath;
        this.spriteWidthPlayer = spriteWidth;   // Lebar SATU frame
        this.spriteHeightPlayer = spriteHeight; // Tinggi SATU frame
        loadSpritesheet();

        this.currentDirection = Direction.SOUTH; // Arah hadap awal
        this.isMoving = false;
        this.animationFrame = 0; // Frame diam
        this.animationCounter = 0;

        // Inisialisasi inventory dengan item default (Halaman 23)
        if (itemRegistry != null) {
            Item parsnipSeeds = itemRegistry.get("Parsnip Seeds");
            // Khusus debug gift aja
            // Legend fish untuk MayorTadi


            // Testing Mayor Tadi
            Item legendFish = itemRegistry.get("Legend");
            Item anglerFish = itemRegistry.get("Angler Fish");
            Item crimsonFish = itemRegistry.get("Crimson Fish");
            Item glacierFish = itemRegistry.get("Glacier Fish");



            Item proposalRing = itemRegistry.get("Proposal Ring");

            // Caroline
            Item coal = itemRegistry.get("Coal");
            Item firewood = itemRegistry.get("Firewood");
            Item potato = itemRegistry.get("Potato");
            Item wheat = itemRegistry.get("Wheat");
            Item hotPepper = itemRegistry.get("Hot Pepper");

            if(coal != null) this.inventory.addItem(coal, 10); else System.err.println("PERINGATAN: Coal tidak ditemukan di registry.");
            if(firewood != null) this.inventory.addItem(firewood, 10); else System.err.println("PERINGATAN: Firewood tidak ditemukan di registry.");
            if(potato != null) this.inventory.addItem(potato, 10); else System.err.println("PERINGATAN: Potato tidak ditemukan di registry.");
            if(wheat != null) this.inventory.addItem(wheat, 10); else System.err.println("PERINGATAN: Wheat tidak ditemukan di registry.");
            if(hotPepper != null) this.inventory.addItem(hotPepper, 10); else System.err.println("PERINGATAN: Hot Pepper tidak ditemukan di registry.");

            // Perry
            Item cranberry = itemRegistry.get("Cranberry");
            Item blueberry = itemRegistry.get("Blueberry");
            Item wine = itemRegistry.get("Wine");

            if(cranberry != null) this.inventory.addItem(cranberry, 10); else System.err.println("PERINGATAN: Cranberry tidak ditemukan di registry.");
            if(blueberry != null) this.inventory.addItem(blueberry, 10); else System.err.println("PERINGATAN: Blueberry tidak ditemukan di registry.");
            if(wine != null) this.inventory.addItem(wine, 10); else System.err.println("PERINGATAN: Wine tidak ditemukan di registry.");

            // Dasco
            // Item theLegendOf = itemRegistry.get("The Legend of the Legend");

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
            // if (coal != null) this.inventory.addItem(coal, 10); else System.err.println("PERINGATAN: Coal tidak ditemukan di registry.");
            // if (firewood != null) this.inventory.addItem(firewood, 10); else System.err.println("PERINGATAN: Firewood tidak ditemukan di registry.");
            if (legendFish != null) this.inventory.addItem(legendFish, 2); else System.err.println("PERINGATAN: Legend Fish tidak ditemukan di registry.");
            if (anglerFish != null) this.inventory.addItem(anglerFish, 1); else System.err.println("PERINGATAN: Angler Fish tidak ditemukan di registry.");
            if (crimsonFish != null) this.inventory.addItem(crimsonFish, 1); else System.err.println("PERINGATAN: Crimson Fish tidak ditemukan di registry.");
            if (glacierFish != null) this.inventory.addItem(glacierFish, 1); else System.err.println("PERINGATAN: Glacier Fish tidak ditemukan di registry.");

            if (proposalRing != null) this.inventory.addItem(proposalRing, 1); else System.err.println("PERINGATAN: Proposal Ring tidak ditemukan di registry.");
            // if (potato != null) this.inventory.addItem(potato, 10); else System.err.println("PERINGATAN: Potato tidak ditemukan di registry.");
            // if (stone != null) this.inventory.addItem(stone, 10); else System.err.println("PERINGATAN: Stone tidak ditemukan di registry.");
            // if (wheat != null) this.inventory.addItem(wheat, 10); else System.err.println("PERINGATAN: Wheat tidak ditemukan di registry.");
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

    private void loadSpritesheet() {
        try {
            if (this.spritesheetPathPlayer != null && !this.spritesheetPathPlayer.isEmpty()) {
                this.fullSpritesheetPlayer = ImageIO.read(getClass().getResourceAsStream(this.spritesheetPathPlayer));
                if (this.fullSpritesheetPlayer == null) {
                    System.err.println("Gagal memuat spritesheet Pemain: " + this.name + " dari path: " + this.spritesheetPathPlayer);
                } else {
                    System.out.println("Berhasil memuat spritesheet Pemain " + this.name);
                }
            }
        } catch (IOException e) {
            System.err.println("Error saat memuat spritesheet untuk Pemain " + this.name + ": " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Error path spritesheet tidak valid untuk Pemain " + this.name + ": " + e.getMessage());
        }
    }

    public Image getCurrentSpriteFrame() {
        if (this.fullSpritesheetPlayer == null) {
            if (this.spritesheetPathPlayer != null && !this.spritesheetPathPlayer.isEmpty()) {
                loadSpritesheet();
            }
            if (this.fullSpritesheetPlayer == null) {
                 System.err.println("Player " + name + ": fullSpritesheetPlayer null di getCurrentSpriteFrame.");
                return null;
            }
        }
    
        int spriteSheetRowPixelY; 
        int spriteSheetColPixelX; 
    
        switch (currentDirection) {
            case NORTH:
                spriteSheetRowPixelY = spriteSheetRowUp * spriteHeightPlayer;
                break;
            case SOUTH:
            default: 
                spriteSheetRowPixelY = spriteSheetRowDown * spriteHeightPlayer;
                break;
            case WEST:
                spriteSheetRowPixelY = spriteSheetRowLeft * spriteHeightPlayer;
                break;
            case EAST:
                spriteSheetRowPixelY = spriteSheetRowRight * spriteHeightPlayer;
                break;
        }
    
        if (isMoving) {
            switch (animationFrame) { 
                case 0: // Kaki Kiri
                    spriteSheetColPixelX = 1 * spriteWidthPlayer;
                    break;
                case 1: // Diam (posisi tengah)
                    spriteSheetColPixelX = 0 * spriteWidthPlayer; // Gunakan kolom 0 untuk diam
                    break;
                case 2: // Kaki Kanan - INI YANG DIPERBAIKI
                    spriteSheetColPixelX = 3 * spriteWidthPlayer; // Gunakan kolom 3 untuk kaki kanan
                    break;
                case 3: // Diam lagi (kembali ke tengah)
                default: 
                    spriteSheetColPixelX = 0 * spriteWidthPlayer; // Kembali ke diam
                    break;
            }
        } else {
            // Player diam, gunakan frame idle (kolom 0)
            spriteSheetColPixelX = 0 * spriteWidthPlayer;
        }
        
        // Validasi batas (tetap sama)
        if (spriteSheetColPixelX < 0 || spriteSheetRowPixelY < 0 ||
            spriteSheetColPixelX + spriteWidthPlayer > fullSpritesheetPlayer.getWidth() ||
            spriteSheetRowPixelY + spriteHeightPlayer > fullSpritesheetPlayer.getHeight()) {
            System.err.println("Player " + name + ": Koordinat/dimensi sprite di luar batas!" +
                               " Dir: " + currentDirection + ", Moving: " + isMoving + ", AnimFrameIdx: " + animationFrame +
                               ", Xpx: " + spriteSheetColPixelX + ", Ypx: " + spriteSheetRowPixelY +
                               ", W: " + spriteWidthPlayer + ", H: " + spriteHeightPlayer +
                               ", Sheet: " + fullSpritesheetPlayer.getWidth() + "x" + fullSpritesheetPlayer.getHeight());
            return this.fullSpritesheetPlayer.getSubimage(0, spriteSheetRowDown * spriteHeightPlayer, spriteWidthPlayer, spriteHeightPlayer); 
        }
    
        try {
            return this.fullSpritesheetPlayer.getSubimage(spriteSheetColPixelX, spriteSheetRowPixelY, spriteWidthPlayer, spriteHeightPlayer);
        } catch (Exception e) {
            System.err.println("Player " + name + ": Exception saat getSubimage: " + e.getMessage());
            e.printStackTrace();
            return this.fullSpritesheetPlayer.getSubimage(0, spriteSheetRowDown * spriteHeightPlayer, spriteWidthPlayer, spriteHeightPlayer);
        }
    }
    

    public void updateAnimation() {
        if (isMoving) {
            animationCounter++;
            if (animationCounter >= ANIMATION_SPEED) {
                animationCounter = 0;
                animationFrame++;
                if (animationFrame >= WALKING_FRAMES) {
                    animationFrame = 0;
                }
                // DEBUG: Lihat perubahan animationFrame
                System.out.println("Player.updateAnimation: isMoving=true, new animationFrame = " + animationFrame);
            }
        } else {
            if (animationFrame != 0 || animationCounter != 0) { // Hanya print jika ada perubahan
                System.out.println("Player.updateAnimation: isMoving=false, resetting animationFrame to 0");
            }
            animationFrame = 0; 
            animationCounter = 0; 
        }
    }

    // Getter dan Setter untuk animasi (dipanggil dari GamePanel/GameController)
    public void setMoving(boolean moving) {
        isMoving = moving;
        if (!moving) { // Jika berhenti bergerak, reset ke frame diam
            animationFrame = 0;
            animationCounter = 0;
        }
    }
    public void setCurrentDirection(Direction direction) { //
        if (this.currentDirection != direction) {
            this.currentDirection = direction;
            this.animationFrame = 0; 
            this.animationCounter = 0;
        } else if (!isMoving) { // Jika arah sama tapi berhenti bergerak, reset animasi ke idle
             this.animationFrame = 0;
             this.animationCounter = 0;
        }
    }
    public Direction getCurrentDirection() {
        return currentDirection;
    }
    public boolean isMoving() {
        return isMoving;
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
    public Item getSelectedItem() { return selectedItem; }
    public int getEngagementDay() { return engagementDay; }

    // Added getter and setter for unlockedRecipes
    public List<String> getUnlockedRecipes() {
        if (this.unlockedRecipes == null) { // Initialize if null to prevent NullPointerExceptions
            this.unlockedRecipes = new ArrayList<>();
        }
        return unlockedRecipes;
    }

    public void setUnlockedRecipes(List<String> unlockedRecipes) {
        this.unlockedRecipes = unlockedRecipes;
    }

    public void unlockRecipe(String recipeId) { // Helper method to add a recipe
        if (this.unlockedRecipes == null) {
            this.unlockedRecipes = new ArrayList<>();
        }
        if (!this.unlockedRecipes.contains(recipeId)) {
            this.unlockedRecipes.add(recipeId);
        }
    }

    // --- Setters ---
    public void setName(String name) {
        this.name = name;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }

    public void setPartner(NPC partner) { this.partner = partner; }
    public void setPosition(int x, int y) { this.currentTileX = x; this.currentTileY = y; }
    public void setCurrentMap(MapArea map) { this.currentMap = map; }
    public void setFavoriteItemName(String itemName) { this.favoriteItemName = itemName; }
    public void setSelectedItem(Item item) {
        this.selectedItem = item;
    }
    public void setEngagementDay(int engagementDay) { this.engagementDay = engagementDay; }

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
        }
    }

    public void setGold(int gold) {
        if (gold >= 0) {
            this.gold = gold;
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

    public void setEnergy(int energy) {
        this.energy = Math.max(MIN_ENERGY, Math.min(MAX_ENERGY, energy));
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

        setCurrentDirection(direction);

        switch (direction) {
            case NORTH: nextY--; break;
            case SOUTH: nextY++; break;
            case EAST:  nextX++; break;
            case WEST:  nextX--; break;
        }

        if (currentMap == null || !currentMap.isWithinBounds(nextX, nextY)) {
            System.out.println("Tidak bisa bergerak ke luar batas map.");
            setMoving(false);
            return false;
        }
        if (currentMap.isOccupied(nextX, nextY)) {
            System.out.println("Jalan terhalang.");
            setMoving(false);
            return false;
        }

        Tile destinationTile = currentMap.getTile(nextX, nextY);
        if (destinationTile != null && destinationTile.getType() == TileType.WATER) {
            System.out.println("Player cannot move onto WATER tile.");
            return false;
        }

        if (currentMap.isOccupied(nextX, nextY)) {
            System.out.println("Jalan terhalang.");
            setMoving(false);
            return false;
        }

        currentTileX = nextX;
        currentTileY = nextY;
        setMoving(true);
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
        if (targetTile == null) {
            System.out.println("Tidak ada tile target untuk dicangkul.");
            return false;
        }

        boolean hasHoe = false;
        if (selectedItem instanceof Equipment) {
            Equipment currentTool = (Equipment) selectedItem;
            if (currentTool.getToolType().equalsIgnoreCase("Hoe")) {
                hasHoe = true;
            }
        }

        if (!hasHoe) {
            System.out.println("Kamu membutuhkan Hoe untuk mencangkul tanah dan Hoe harus dipilih.");
            return false;
        }
        
        if (!targetTile.canBeTilled()) {
            System.out.println("Tile ini tidak bisa dicangkul.");
            return false;
        }

        targetTile.setType(TileType.TILLED);
        System.out.println("Tanah berhasil dicangkul!");
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

        Season currentSeasonValue = currentTime.getCurrentSeason();
        Season seedTargetSeasonValue = seedToPlant.getTargetSeason();
        if (seedTargetSeasonValue != Season.ANY && seedTargetSeasonValue != currentSeasonValue) {
            System.out.println("[Player.plant] Action failed: " + seedToPlant.getName() + " cannot be planted in " + currentSeasonValue + ". Required season: " + seedTargetSeasonValue + ".");
            return false;
        }

        if (inventory.hasItem(seedToPlant, 1)) {
            boolean plantSuccess = targetTile.setPlantedSeed(seedToPlant, currentSeasonValue); 
            if (plantSuccess) {
                inventory.removeItem(seedToPlant, 1);
                System.out.println("[Player.plant] Action success: " + seedToPlant.getName() + " planted.");
                return true;
            } else {
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
    public boolean water(Tile targetTile, Weather currentWeather) {
        if (targetTile == null) {
            System.out.println("[Player.water] Error: Target tile is null.");
            return false;
        }

        boolean hasWateringCan = false;
        if (selectedItem instanceof Equipment) {
            Equipment currentTool = (Equipment) selectedItem;
            String actualToolType = currentTool.getToolType();
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
     * @param statistics Referensi ke EndGameStatistics untuk mencatat panen.
     * @return true jika panen berhasil, false jika gagal.
     */
    public boolean harvest(Tile targetTile, Map<String, Item> itemRegistry, EndGameStatistics statistics) {
        if (targetTile == null || !targetTile.isHarvestable() || targetTile.getPlantedSeed() == null) {
            System.out.println("Player.harvest: Tidak ada yang bisa dipanen atau tile tidak valid.");
            return false;
        }
        List<Item> harvestedProduce = targetTile.processHarvest(itemRegistry);

        if (harvestedProduce != null && !harvestedProduce.isEmpty()) {
            boolean allAdded = true;
            for(Item item : harvestedProduce){
                if (item != null) {
                    this.inventory.addItem(item, 1);
                    System.out.println("Player memanen: " + item.getName());
                    if (statistics != null) {
                        statistics.recordHarvest(item.getName(), 1);
                    }
                } else {
                    allAdded = false;
                }
            }
            if (!allAdded) {
                System.err.println("Peringatan: Beberapa item hasil panen null dan tidak ditambahkan.");
            }
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
            if (!inventory.hasItem(itemToEat, 1)) {
                System.out.println("Player.eat: Pemain tidak memiliki " + itemToEat.getName() + " untuk dimakan.");
                return false;
            }

            EdibleItem edible = (EdibleItem) itemToEat;
            int energyRestored = edible.getEnergyRestore();

            if (this.energy >= MAX_ENERGY && energyRestored > 0) {
                System.out.println("Player.eat: Energi sudah penuh, tidak bisa makan " + itemToEat.getName() + " untuk memulihkan energi.");
                return false;
            }
            
            boolean removed = inventory.removeItem(itemToEat, 1);
            if (removed) {
                changeEnergy(energyRestored);
                System.out.println(this.name + " memakan " + itemToEat.getName() + ". Energi pulih: " + energyRestored + ". Energi sekarang: " + this.energy);
                if (inventory.getItemCount(itemToEat) == 0 && itemToEat.equals(this.selectedItem)) {
                }
                return true;
            } else {
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
        boolean applyPenalty = energyBeforeSleep < LOW_ENERGY_THRESHOLD;

        if (applyPenalty) {
            targetEnergy = MAX_ENERGY / 2;
            System.out.println("Kamu tidur dengan energi rendah... Energi hanya pulih setengah.");
        } else {
            targetEnergy = MAX_ENERGY;
            System.out.println("Kamu tidur nyenyak. Energi pulih sepenuhnya.");
        }

        if (usedBonusBed) {
            System.out.println("Menggunakan tempat tidur bonus! (Logika spesifik akan diimplementasikan dengan fitur furnitur)");
        }

        if (targetEnergy > MAX_ENERGY) {
            targetEnergy = MAX_ENERGY;
        }
        if (targetEnergy < MIN_ENERGY) {
            targetEnergy = MIN_ENERGY;
        }
        this.energy = targetEnergy;
    }

    /**
     * Mencoba memulai proses memasak berdasarkan resep dan bahan bakar.
     * Metode ini akan mengurangi bahan dan bahan bakar dari inventory jika valid.
     * Pengurangan energi dan pemajuan waktu (termasuk 1 jam pasif) akan dihandle oleh Controller.
     *
     * @param recipe Resep yang akan dimasak.
     * @param fuelItem Item bahan bakar yang digunakan (Coal atau Firewood).
     * @param itemRegistry Untuk mendapatkan objek Item dari nama.
     * @return String berisi nama item hasil jika persiapan berhasil, atau pesan error jika gagal.
     */
    public String cook(Recipe recipe, Item fuelItem, Map<String, Item> itemRegistry) {
        if (recipe == null) {
            return "Resep tidak valid.";
        }
        if (fuelItem == null) {
            return "Bahan bakar tidak valid.";
        }
        if (itemRegistry == null) {
            return "Item registry tidak tersedia.";
        }

        String fuelName = fuelItem.getName();
        if (!fuelName.equals("Coal") && !fuelName.equals("Firewood")) {
            return "Bahan bakar tidak valid. Gunakan Coal atau Firewood.";
        }
        if (!inventory.hasItem(fuelItem, 1)) {
            return "Kamu tidak punya " + fuelName + " yang cukup.";
        }

        List<Item> fishItemsToRemove = new ArrayList<>();
        int anyFishRequired = 0;
        int anyFishFound = 0;

        for (Map.Entry<String, Integer> entry : recipe.getIngredients().entrySet()) {
            String ingredientName = entry.getKey();
            int requiredQty = entry.getValue();

            if (ingredientName.equals("Any Fish")) {
                anyFishRequired = requiredQty;
                for (Map.Entry<Item, Integer> invEntry : inventory.getItems().entrySet()) {
                    if (invEntry.getKey() instanceof Fish) {
                        anyFishFound += invEntry.getValue();
                    }
                }
                if (anyFishFound < anyFishRequired) {
                    return "Kamu kekurangan bahan: " + anyFishRequired + " Any Fish (kamu punya " + anyFishFound + ").";
                }
            } else {
                Item ingredient = itemRegistry.get(ingredientName);
                if (ingredient == null) {
                return "Bahan '" + ingredientName + "' tidak dikenal dalam sistem.";
            }
            if (!inventory.hasItem(ingredient, requiredQty)) {
                return "Kamu kekurangan bahan: " + requiredQty + " " + ingredientName + ".";
                }
            }
        }

        inventory.removeItem(fuelItem, 1);

        for (Map.Entry<String, Integer> entry : recipe.getIngredients().entrySet()) {
            String ingredientName = entry.getKey();
            int requiredQty = entry.getValue();

            if (ingredientName.equals("Any Fish")) {
                int fishRemovedCount = 0;
                List<Item> currentInventoryKeys = new ArrayList<>(inventory.getItems().keySet());
                for (Item invItem : currentInventoryKeys) {
                    if (invItem instanceof Fish) {
                        int countInStack = inventory.getItemCount(invItem);
                        int canRemoveFromStack = Math.min(countInStack, requiredQty - fishRemovedCount);
                        
                        inventory.removeItem(invItem, canRemoveFromStack);
                        fishRemovedCount += canRemoveFromStack;
                        
                        if (fishRemovedCount >= requiredQty) {
                            break; 
                        }
                    }
                }
            } else {
                Item ingredient = itemRegistry.get(ingredientName);
            inventory.removeItem(ingredient, entry.getValue());
            }
        }

        return recipe.getResultItemName();
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
        changeEnergy(-5);
        System.out.println("Kamu mulai memancing...");
    }

    /**
     * Mencoba melamar seorang NPC. Membutuhkan Proposal Ring.
     *
     * @param npcTarget NPC yang akan dilamar.
     * @param ring Item ProposalRing (untuk konfirmasi item dipegang).
     * @param currentTotalDaysPlayed Total hari yang telah dimainkan, untuk mencatat tanggal pertunangan.
     * @return String pesan error jika gagal, atau null jika berhasil.
     */
    public String propose(NPC npcTarget, ProposalRing ring, int currentTotalDaysPlayed) {
        if (npcTarget == null || ring == null) {
            return "Target NPC atau cincin tidak valid untuk melamar.";
        }

        if (this.partner != null && this.partner.getRelationshipStatus() == RelationshipStatus.SPOUSE) {
            return "Kamu sudah menikah dengan " + this.partner.getName() + "! Tidak bisa melamar siapapun lagi.";
        }

        if (this.partner != null && this.partner.getRelationshipStatus() == RelationshipStatus.FIANCE) {
            if (this.partner == npcTarget) {
                return "Kamu sudah bertunangan dengan " + npcTarget.getName() + ".";
            } else {
                return "Kamu sudah bertunangan dengan " + this.partner.getName() + " dan tidak bisa melamar " + npcTarget.getName() + ".";
            }
        }
        
        if (!npcTarget.isBachelor()) {
            return npcTarget.getName() + " tidak bisa dilamar (bukan bachelor/bachelorette).";
        }

        if (npcTarget.getHeartPoints() < npcTarget.getMaxHeartPoints()) {
            return "Sayangnya, " + npcTarget.getName() + " merasa hubungan kalian belum cukup dekat untuk lamaran. (Hati: " + npcTarget.getHeartPoints() + "/" + npcTarget.getMaxHeartPoints() + ")";
        }

        npcTarget.setRelationshipStatus(RelationshipStatus.FIANCE);
        this.setPartner(npcTarget); 
        this.setEngagementDay(currentTotalDaysPlayed); 
        
        return null;
    }

    /**
     * Mencoba menikahi seorang NPC (harus tunangan).
     * Time skip (seharian + skip ke 22:00) ditangani oleh Controller.
     *
     * @param npcTarget NPC yang akan dinikahi.
     * @return true jika kondisi pernikahan terpenuhi di awal, false jika gagal.
     */
    public String marry(NPC npcTarget, int currentTotalDaysPlayed) { 
        if (npcTarget == null ) {
            return "Target NPC tidak valid untuk menikah.";
        }

        if (this.partner != null && this.partner == npcTarget && this.partner.getRelationshipStatus() == RelationshipStatus.SPOUSE) {
            return "Kamu sudah menikah dengan " + npcTarget.getName() + "!";
        }

        if (this.partner != null && this.partner.getRelationshipStatus() == RelationshipStatus.SPOUSE && this.partner != npcTarget) {
            return "Kamu sudah menikah dengan " + this.partner.getName() + " dan tidak bisa menikah dengan " + npcTarget.getName() + ".";
        }
        
        if (this.partner == null) {
            return "Kamu belum bertunangan dengan siapapun. Lamar dulu " + npcTarget.getName() + "!";
        }
        
        if (this.partner != npcTarget) {
            return npcTarget.getName() + " bukanlah tunanganmu saat ini. Tunanganmu adalah " + this.partner.getName() + ".";
        }

        if (npcTarget.getRelationshipStatus() != RelationshipStatus.FIANCE) {
             return "Status hubunganmu dengan " + npcTarget.getName() + " adalah " + npcTarget.getRelationshipStatus() + ". Kamu hanya bisa menikahi seorang FIANCE.";
        }

        if (this.engagementDay < 0 || currentTotalDaysPlayed <= this.engagementDay) {
            return "Kamu harus menunggu setidaknya satu hari setelah bertunangan untuk menikah. ";
        }
        
        npcTarget.setRelationshipStatus(RelationshipStatus.SPOUSE);
        
        return null;
    }

    /**
     * Menonton TV (jika diimplementasikan sebagai BONUS).
     * Biaya energi (-5) ditangani oleh Controller.
     * Biaya waktu (15 menit) ditangani oleh Controller.
     * @return Cuaca yang diprediksi untuk besok (atau null jika TV tidak tersedia).
     */
    public Weather watchTV() {
        System.out.println("Kamu menonton TV...");
        return null;
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
     * Attempts to chat with the specified NPC.
     * Checks energy, distance, and updates game state if successful.
     * This method assumes the GameController has identified a potential NPC target
     * and provides the MapArea instance where the NPC is currently located.
     *
     * @param npcTarget The NPC to attempt to chat with.
     * @param gameTime  The current GameTime object to advance time.
     * @param npcActualMap The MapArea instance where the npcTarget is currently located.
     * @return true if chat was successful, false otherwise.
     */
    public boolean chat(NPC npcTarget, GameTime gameTime, MapArea npcActualMap) {
        if (npcTarget == null) {
            System.out.println("Tidak ada NPC yang ditargetkan untuk diajak bicara.");
            return false;
        }
         if (npcActualMap == null) {
            System.out.println("Informasi map NPC tidak tersedia untuk chat.");
            return false;
        }
        if (this.currentMap == null) {
            System.out.println(this.name + " tidak berada di map yang valid untuk chat.");
            return false;
        }

        if (this.energy < CHAT_ENERGY_COST) {
            System.out.println(this.name + " tidak punya cukup energi untuk berbicara (butuh " + CHAT_ENERGY_COST + ", punya " + this.energy + ").");
            return false;
        }

        if (this.currentMap != npcActualMap) {
            System.out.println(this.name + " tidak berada di map yang sama dengan " + npcTarget.getName() + ".");
            System.out.println("Player di: " + this.currentMap.getName() + 
                               ", " + npcTarget.getName() + " berada di: " + npcActualMap.getName());
            return false;
        }
        
        int distance = Math.abs(this.currentTileX - npcTarget.getCurrentTileX()) + 
                       Math.abs(this.currentTileY - npcTarget.getCurrentTileY());

        if (distance > CHAT_MAX_DISTANCE) {
            System.out.println(this.name + " terlalu jauh dari " + npcTarget.getName() + " untuk berbicara. Jarak: " + distance + ", Maks: " + CHAT_MAX_DISTANCE);
            return false;
        }

        this.changeEnergy(-CHAT_ENERGY_COST);
        if (gameTime != null) {
            gameTime.advance(CHAT_TIME_ADVANCE_MINUTES);
        } else {
            System.err.println("Player.chat: GameTime is null, waktu tidak dimajukan.");
        }
        
        npcTarget.addHeartPoints(CHAT_HEART_POINTS_GAIN);
        
        return true;
    }

    /**
     * Gifts an item to the specified NPC.
     * Checks energy, proximity, and item validity. Updates game state if successful.
     * @param npcTarget The NPC to gift to.
     * @param itemToGift The Item to gift.
     * @param gameTime The current GameTime.
     * @param npcActualMap The MapArea instance where the npcTarget is located.
     * @return true if gifting was successful, false otherwise.
     */
    public boolean gift(NPC npcTarget, Item itemToGift, GameTime gameTime, MapArea npcActualMap) {
        if (npcTarget == null) {
            System.out.println("Tidak ada NPC yang ditargetkan untuk diberi hadiah.");
            return false;
        }
        if (itemToGift == null) {
            System.out.println("Tidak ada item yang dipilih untuk diberikan.");
            return false;
        }
        if (npcActualMap == null) {
            System.out.println("Informasi map NPC tidak tersedia untuk memberi hadiah.");
            return false;
        }
        if (this.currentMap == null) {
            System.out.println(this.name + " tidak berada di map yang valid.");
            return false;
        }

        final int GIFT_ENERGY_COST = 5;
        final int GIFT_TIME_ADVANCE_MINUTES = 10;
        final int GIFT_MAX_DISTANCE = 1;

        if (this.energy < GIFT_ENERGY_COST) {
            System.out.println(this.name + " tidak punya cukup energi untuk memberi hadiah (butuh " + GIFT_ENERGY_COST + ", punya " + this.energy + ").");
            return false;
        }

        if (this.currentMap != npcActualMap) {
            System.out.println(this.name + " tidak berada di map yang sama dengan " + npcTarget.getName() + " untuk memberi hadiah.");
            return false;
        }

        int distance = Math.abs(this.currentTileX - npcTarget.getCurrentTileX()) +
                       Math.abs(this.currentTileY - npcTarget.getCurrentTileY());

        if (distance > GIFT_MAX_DISTANCE) {
            System.out.println(this.name + " terlalu jauh dari " + npcTarget.getName() + " untuk memberi hadiah.");
            return false;
        }

        if (!this.inventory.hasItem(itemToGift, 1)) {
            System.out.println(this.name + " tidak memiliki " + itemToGift.getName() + " untuk diberikan.");
            return false;
        }

        this.changeEnergy(-GIFT_ENERGY_COST);
        if (gameTime != null) {
            gameTime.advance(GIFT_TIME_ADVANCE_MINUTES);
        } else {
            System.err.println("Player.gift: GameTime is null, waktu tidak dimajukan.");
        }

        this.inventory.removeItem(itemToGift, 1);
        int heartChange = npcTarget.checkGiftPreference(itemToGift);
        npcTarget.addHeartPoints(heartChange);

        System.out.println(this.name + " memberikan " + itemToGift.getName() + " kepada " + npcTarget.getName() + ".");
        System.out.println(npcTarget.getName() + " bereaksi... (Hati berubah: " + heartChange + ", Total Hati: " + npcTarget.getHeartPoints() + ")");
        
        if (this.selectedItem != null && this.selectedItem.equals(itemToGift) && this.inventory.getItemCount(itemToGift) == 0) {
            setSelectedItem(null);
        }

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

        if (!shippingBin.canSellToday()) {
            System.out.println(this.name + ": Sudah melakukan penjualan via Shipping Bin hari ini. Coba lagi besok.");
             return false;
         }

        if (!this.inventory.hasItem(itemToSell, quantity)) {
            System.out.println(this.name + " tidak punya cukup " + itemToSell.getName() + " (" + quantity + ") untuk dijual. Hanya punya: " + this.inventory.getItemCount(itemToSell));
            return false;
        }

        if (this.inventory.removeItem(itemToSell, quantity)) {
            if (shippingBin.addItem(itemToSell, quantity)) {
                System.out.println(this.name + " menaruh " + quantity + " " + itemToSell.getName() + " ke Shipping Bin.");
             return true;
         } else {
                this.inventory.addItem(itemToSell, quantity);
                System.out.println(this.name + ": Gagal menaruh item ke Shipping Bin (mungkin penuh slot unik). Item dikembalikan ke inventory.");
                return false;
            }
        } else {
            System.err.println(this.name + ": Gagal menghapus " + itemToSell.getName() + " dari inventory meskipun pengecekan awal berhasil.");
             return false;
         }
    } 

    /**
     * Menangani kondisi pemain pingsan.
     * Energi diatur setengah, hari diproses, dan pendapatan dari hari itu dikembalikan.
     * @param farm Referensi ke Farm model untuk memproses hari berikutnya.
     * @return int pendapatan dari penjualan pada hari berikutnya.
     */
    public int passOut(Farm farm) {
        System.out.println(getName() + " pingsan karena kelelahan!");
        
        int targetEnergy = MAX_ENERGY / 2;
        int currentEnergy = getEnergy();
        changeEnergy(targetEnergy - currentEnergy);

        int income = farm.forceSleepAndProcessNextDay(); 
        System.out.println("Bangun keesokan harinya dengan energi: " + getEnergy() + ".");
        return income;
    }

    /**
     * Memeriksa apakah posisi pemain saat ini berada di salah satu entry/exit point
     * pada peta saat ini.
     * @return true jika pemain berada di entry point map saat ini, false jika tidak atau map tidak punya entry points.
     */
    public boolean isOnEntryPoint() {
        if (this.currentMap == null || this.currentMap.getEntryPoints() == null) {
            return false;
        }
        List<Point> entryPoints = this.currentMap.getEntryPoints();
        Point playerPosition = new Point(this.currentTileX, this.currentTileY);
        for (Point entryPoint : entryPoints) {
            if (entryPoint.equals(playerPosition)) {
                return true;
            }
        }
        return false;
    } 

} 