package com.spakborhills.data;

import java.io.Serializable;
import java.util.List; // Ditambahkan untuk placedObjects
import java.util.Map; // Diubah untuk farmTiles agar lebih fleksibel dengan koordinat
// Import kelas-kelas lain yang mungkin dibutuhkan nanti
// import com.spakborhills.model.Player;
// import com.spakborhills.model.FarmModel;
// import com.spakborhills.model.TimeService;

// Added imports for new data structures
import com.spakborhills.model.Enum.Gender;
import com.spakborhills.model.Enum.RelationshipStatus;
import com.spakborhills.model.NPC.NPC;

public class SaveData implements Serializable {
    private static final long serialVersionUID = 1L; // Untuk kontrol versi serialisasi

    // Data Player
    private String playerName; // New
    private Gender playerGender; // New
    private int playerX;
    private int playerY;
    private String currentMapId;
    private int playerMoney;
    private int playerEnergy;
    private String playerFarmName; // New
    private PartnerData playerPartner; // New
    private InventoryData playerInventory; // Diaktifkan
    private List<String> unlockedRecipes; // New

    // Data Waktu & Dunia Game
    private int currentDay;
    private int currentHour;
    private int currentMinute; // New
    private String currentSeason;
    private int currentYear;
    private String currentWeather; // New
    // Menggunakan Map untuk farmTiles agar bisa menyimpan berdasarkan koordinat (misal "x,y")
    // Ini lebih fleksibel daripada array 2D jika ukuran farm bisa berubah atau tidak semua tile perlu disimpan.
    // Jika farm selalu berukuran tetap dan semua tile disimpan, FarmTileData[][] juga bisa.
    private Map<String, FarmTileData> farmTiles; // Diaktifkan dan diubah tipe datanya
    private List<ShippingBinItemData> shippingBinContents; // New
    // private List<PlacedObjectData> placedObjects; // Masih di-comment, akan dibuat jika diperlukan nanti

    // Data NPC
    private Map<String, NpcData> npcDataMap; // New

    // Data Progres & Event
    private List<String> milestonesAchieved; // New

    // Data Bonus
    private BonusData bonusData; //New

    // New field for farm deployed objects
    private List<PlacedObjectData> farmDeployedObjects;

    // Konstruktor kosong untuk deserialisasi
    public SaveData() {
    }

    // Getter dan Setter untuk semua field
    // Contoh:
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Gender getPlayerGender() {
        return playerGender;
    }

    public void setPlayerGender(Gender playerGender) {
        this.playerGender = playerGender;
    }

    public int getPlayerX() {
        return playerX;
    }

    public void setPlayerX(int playerX) {
        this.playerX = playerX;
    }

    public int getPlayerY() {
        return playerY;
    }

    public void setPlayerY(int playerY) {
        this.playerY = playerY;
    }

    public String getCurrentMapId() {
        return currentMapId;
    }

    public void setCurrentMapId(String currentMapId) {
        this.currentMapId = currentMapId;
    }

    public int getPlayerMoney() {
        return playerMoney;
    }

    public void setPlayerMoney(int playerMoney) {
        this.playerMoney = playerMoney;
    }

    public int getPlayerEnergy() {
        return playerEnergy;
    }

    public void setPlayerEnergy(int playerEnergy) {
        this.playerEnergy = playerEnergy;
    }

    public String getPlayerFarmName() {
        return playerFarmName;
    }

    public void setPlayerFarmName(String playerFarmName) {
        this.playerFarmName = playerFarmName;
    }

    public PartnerData getPlayerPartner() {
        return playerPartner;
    }

    public void setPlayerPartner(PartnerData playerPartner) {
        this.playerPartner = playerPartner;
    }

    public InventoryData getPlayerInventory() {
        return playerInventory;
    }

    public void setPlayerInventory(InventoryData playerInventory) {
        this.playerInventory = playerInventory;
    }

    public List<String> getUnlockedRecipes() {
        return unlockedRecipes;
    }

    public void setUnlockedRecipes(List<String> unlockedRecipes) {
        this.unlockedRecipes = unlockedRecipes;
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(int currentDay) {
        this.currentDay = currentDay;
    }

    public int getCurrentHour() {
        return currentHour;
    }

    public void setCurrentHour(int currentHour) {
        this.currentHour = currentHour;
    }

    public int getCurrentMinute() {
        return currentMinute;
    }

    public void setCurrentMinute(int currentMinute) {
        this.currentMinute = currentMinute;
    }

    public String getCurrentSeason() {
        return currentSeason;
    }

    public void setCurrentSeason(String currentSeason) {
        this.currentSeason = currentSeason;
    }

    public int getCurrentYear() {
        return currentYear;
    }

    public void setCurrentYear(int currentYear) {
        this.currentYear = currentYear;
    }

    public String getCurrentWeather() {
        return currentWeather;
    }

    public void setCurrentWeather(String currentWeather) {
        this.currentWeather = currentWeather;
    }

    public Map<String, FarmTileData> getFarmTiles() {
        return farmTiles;
    }

    public void setFarmTiles(Map<String, FarmTileData> farmTiles) {
        this.farmTiles = farmTiles;
    }

    public List<ShippingBinItemData> getShippingBinContents() {
        return shippingBinContents;
    }

    public void setShippingBinContents(List<ShippingBinItemData> shippingBinContents) {
        this.shippingBinContents = shippingBinContents;
    }

    public Map<String, NpcData> getNpcDataMap() {
        return npcDataMap;
    }

    public void setNpcDataMap(Map<String, NpcData> npcDataMap) {
        this.npcDataMap = npcDataMap;
    }

    public List<String> getMilestonesAchieved() {
        return milestonesAchieved;
    }

    public void setMilestonesAchieved(List<String> milestonesAchieved) {
        this.milestonesAchieved = milestonesAchieved;
    }

    public BonusData getBonusData() {
        return bonusData;
    }

    public void setBonusData(BonusData bonusData) {
        this.bonusData = bonusData;
    }

    public List<PlacedObjectData> getFarmDeployedObjects() {
        return farmDeployedObjects;
    }

    public void setFarmDeployedObjects(List<PlacedObjectData> farmDeployedObjects) {
        this.farmDeployedObjects = farmDeployedObjects;
    }

    // Inner classes for structured data

    public static class PartnerData implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private RelationshipStatus status;

        public PartnerData(String name, RelationshipStatus status) {
            this.name = name;
            this.status = status;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public RelationshipStatus getStatus() {
            return status;
        }

        public void setStatus(RelationshipStatus status) {
            this.status = status;
        }
    }

    public static class NpcData implements Serializable {
        private static final long serialVersionUID = 1L;
        private int heartPoints;
        private RelationshipStatus relationshipStatus;

        public NpcData(int heartPoints, RelationshipStatus relationshipStatus) {
            this.heartPoints = heartPoints;
            this.relationshipStatus = relationshipStatus;
        }

        public int getHeartPoints() {
            return heartPoints;
        }

        public void setHeartPoints(int heartPoints) {
            this.heartPoints = heartPoints;
        }

        public RelationshipStatus getRelationshipStatus() {
            return relationshipStatus;
        }

        public void setRelationshipStatus(RelationshipStatus relationshipStatus) {
            this.relationshipStatus = relationshipStatus;
        }
    }

    public static class ShippingBinItemData implements Serializable {
        private static final long serialVersionUID = 1L;
        private String itemId;
        private int quantity;

        public ShippingBinItemData(String itemId, int quantity) {
            this.itemId = itemId;
            this.quantity = quantity;
        }

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
    
    public static class BonusData implements Serializable {
        private static final long serialVersionUID = 1L;
        private List<FurnitureData> houseFurniture;
        // Add other bonus data fields here if needed

        public BonusData() {
            // Default constructor
        }

        public List<FurnitureData> getHouseFurniture() {
            return houseFurniture;
        }

        public void setHouseFurniture(List<FurnitureData> houseFurniture) {
            this.houseFurniture = houseFurniture;
        }
    }

    public static class FurnitureData implements Serializable {
        private static final long serialVersionUID = 1L;
        private String itemId;
        private int x;
        private int y;

        public FurnitureData(String itemId, int x, int y) {
            this.itemId = itemId;
            this.x = x;
            this.y = y;
        }

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }
    }

    // Inner class for Placed Object Data
    public static class PlacedObjectData implements Serializable {
        private static final long serialVersionUID = 1L;
        private String objectName;      // To identify the object, e.g., "Rumah", "Pond"
        private String objectClassType; // Full class name, e.g., "com.spakborhills.model.Object.House"
        private int x;                  // Anchor X coordinate
        private int y;                  // Anchor Y coordinate
        // Width and height might not be needed if they are fixed by type or re-queried upon creation
        // For now, let's assume they can be inferred or are fixed by the class type

        public PlacedObjectData(String objectName, String objectClassType, int x, int y) {
            this.objectName = objectName;
            this.objectClassType = objectClassType;
            this.x = x;
            this.y = y;
        }

        public String getObjectName() {
            return objectName;
        }

        public void setObjectName(String objectName) {
            this.objectName = objectName;
        }

        public String getObjectClassType() {
            return objectClassType;
        }

        public void setObjectClassType(String objectClassType) {
            this.objectClassType = objectClassType;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }
    }

    // TODO: Tambahkan getter dan setter untuk placedObjects jika sudah dibuat
    
} 