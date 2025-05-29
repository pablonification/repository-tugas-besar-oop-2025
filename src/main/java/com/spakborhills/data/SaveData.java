package com.spakborhills.data;

import java.io.Serializable;
import java.util.List; 
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import com.spakborhills.model.Enum.Gender;
import com.spakborhills.model.Enum.RelationshipStatus;

public class SaveData implements Serializable {
    private static final long serialVersionUID = 1L; 

    // Data Player
    private String playerName; 
    private Gender playerGender; 
    private int playerX;
    private int playerY;
    private String currentMapId;
    private int playerMoney;
    private int playerEnergy;
    private String playerFarmName; 
    private PartnerData playerPartner; 
    private InventoryData playerInventory; 
    private List<String> unlockedRecipes; 
    private String favoriteItemName;  

    // Data Waktu & Dunia Game
    private int currentDay;
    private int currentHour;
    private int currentMinute; 
    private String currentSeason;
    private int currentYear;
    private String currentWeather;

    private List<ShippingBinItemData> shippingBinContents; 

    // Data NPC
    private Map<String, NpcData> npcDataMap; 

    // Data Progres & Event
    private List<String> milestonesAchieved; 

    // Data Statistik
    private StatisticsData statisticsData;

    // Data Bonus
    private BonusData bonusData;

    private List<PlacedObjectData> farmDeployedObjects;

    // Konstruktor kosong untuk deserialisasi
    public SaveData() {
    }

    // Getter dan Setter untuk semua field
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

    public StatisticsData getStatisticsData() {
        return statisticsData;
    }

    public void setStatisticsData(StatisticsData statisticsData) {
        this.statisticsData = statisticsData;
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

    public String getFavoriteItemName() {
        return favoriteItemName;
    }

    public void setFavoriteItemName(String favoriteItemName) {
        this.favoriteItemName = favoriteItemName;
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

        public BonusData() {
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
        private String objectName;      
        private String objectClassType;
        private int x;                  
        private int y;                  

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

    // Statistics Data class to store all game statistics
    public static class StatisticsData implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private int totalIncome;
        private int totalExpenditure;
        private Map<String, Integer> seasonalIncome; 
        private Map<String, Integer> seasonalExpenditure; 
        private Map<String, Integer> daysPlayedInSeason; 
        private int totalDaysPlayed;
        
        private Map<String, Integer> chatFrequency; 
        private Map<String, Integer> giftFrequency; 
        private Map<String, Integer> visitFrequency; 
        
        private Map<String, Integer> cropsHarvestedCount; 
        private Set<String> uniqueCropsHarvested; 
        
        private Map<String, Map<String, Integer>> fishCaught; 
        private Set<String> uniqueFishCaught; 
        
        private Set<String> keyEventsOrItemsObtained; 
        
        public StatisticsData() {
            this.seasonalIncome = new HashMap<>();
            this.seasonalExpenditure = new HashMap<>();
            this.daysPlayedInSeason = new HashMap<>();
            this.chatFrequency = new HashMap<>();
            this.giftFrequency = new HashMap<>();
            this.visitFrequency = new HashMap<>();
            this.cropsHarvestedCount = new HashMap<>();
            this.uniqueCropsHarvested = new HashSet<>();
            this.fishCaught = new HashMap<>();
            this.uniqueFishCaught = new HashSet<>();
            this.keyEventsOrItemsObtained = new HashSet<>();
        }

        // Getters and setters
        public int getTotalIncome() {
            return totalIncome;
        }

        public void setTotalIncome(int totalIncome) {
            this.totalIncome = totalIncome;
        }

        public int getTotalExpenditure() {
            return totalExpenditure;
        }

        public void setTotalExpenditure(int totalExpenditure) {
            this.totalExpenditure = totalExpenditure;
        }

        public Map<String, Integer> getSeasonalIncome() {
            return seasonalIncome;
        }

        public void setSeasonalIncome(Map<String, Integer> seasonalIncome) {
            this.seasonalIncome = seasonalIncome;
        }

        public Map<String, Integer> getSeasonalExpenditure() {
            return seasonalExpenditure;
        }

        public void setSeasonalExpenditure(Map<String, Integer> seasonalExpenditure) {
            this.seasonalExpenditure = seasonalExpenditure;
        }

        public Map<String, Integer> getDaysPlayedInSeason() {
            return daysPlayedInSeason;
        }

        public void setDaysPlayedInSeason(Map<String, Integer> daysPlayedInSeason) {
            this.daysPlayedInSeason = daysPlayedInSeason;
        }

        public int getTotalDaysPlayed() {
            return totalDaysPlayed;
        }

        public void setTotalDaysPlayed(int totalDaysPlayed) {
            this.totalDaysPlayed = totalDaysPlayed;
        }

        public Map<String, Integer> getChatFrequency() {
            return chatFrequency;
        }

        public void setChatFrequency(Map<String, Integer> chatFrequency) {
            this.chatFrequency = chatFrequency;
        }

        public Map<String, Integer> getGiftFrequency() {
            return giftFrequency;
        }

        public void setGiftFrequency(Map<String, Integer> giftFrequency) {
            this.giftFrequency = giftFrequency;
        }

        public Map<String, Integer> getVisitFrequency() {
            return visitFrequency;
        }

        public void setVisitFrequency(Map<String, Integer> visitFrequency) {
            this.visitFrequency = visitFrequency;
        }

        public Map<String, Integer> getCropsHarvestedCount() {
            return cropsHarvestedCount;
        }

        public void setCropsHarvestedCount(Map<String, Integer> cropsHarvestedCount) {
            this.cropsHarvestedCount = cropsHarvestedCount;
        }

        public Set<String> getUniqueCropsHarvested() {
            return uniqueCropsHarvested;
        }

        public void setUniqueCropsHarvested(Set<String> uniqueCropsHarvested) {
            this.uniqueCropsHarvested = uniqueCropsHarvested;
        }

        public Map<String, Map<String, Integer>> getFishCaught() {
            return fishCaught;
        }

        public void setFishCaught(Map<String, Map<String, Integer>> fishCaught) {
            this.fishCaught = fishCaught;
        }

        public Set<String> getUniqueFishCaught() {
            return uniqueFishCaught;
        }

        public void setUniqueFishCaught(Set<String> uniqueFishCaught) {
            this.uniqueFishCaught = uniqueFishCaught;
        }

        public Set<String> getKeyEventsOrItemsObtained() {
            return keyEventsOrItemsObtained;
        }

        public void setKeyEventsOrItemsObtained(Set<String> keyEventsOrItemsObtained) {
            this.keyEventsOrItemsObtained = keyEventsOrItemsObtained;
        }
    }

    
} 