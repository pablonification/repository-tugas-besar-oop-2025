package com.spakborhills.model.Map;

import com.spakborhills.model.Item.Seed;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Item.Crop;
import com.spakborhills.model.Enum.TileType;
import com.spakborhills.model.Enum.Weather;
import com.spakborhills.model.Enum.Season;
import com.spakborhills.model.Object.DeployedObject;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class Tile {
    private TileType type;
    private boolean isWatered;
    private int daysSinceLastWatered;
    private Seed plantedSeed;
    private int growthDays;
    private DeployedObject associatedObject;
    private static final int WATERING_INTERVAL_HOT_WEATHER = 2;
    private static final int MAX_DAYS_WITHOUT_WATER_BEFORE_DEATH = 3;

    public Tile(TileType type) {
        this.type = type;
        this.isWatered = false;
        this.daysSinceLastWatered = 0;
        this.plantedSeed = null;
        this.growthDays = 0;
        this.associatedObject = null;
    }

    // Getters
    public TileType getType(){
        return type;
    }

    public boolean isWatered(){
        return isWatered;
    }
    
    public Seed getPlantedSeed(){
        return plantedSeed;
    }

    public int getGrowthDays(){
        return growthDays;
    }
    
    public DeployedObject getAssociatedObject(){
        return associatedObject;
    }

    // Added for SaveLoadManager
    public int getLastWateredDay() {
        return daysSinceLastWatered; 
    }

    public void setLastWateredDay(int day) {
        this.daysSinceLastWatered = day;
    }

    public void clearWatered() {
        this.isWatered = false;
        }

    // Setters
    public void setType(TileType newType){
        this.type = newType;
        if (newType == TileType.TILLABLE || newType == TileType.TILLED){
            isWatered = false;
            daysSinceLastWatered = 0;
            plantedSeed = null;
            growthDays = 0;
        }
    }

    public void markAsWatered(){
        if (canBeWateredInternalCheck()){
            this.isWatered = true;
            this.daysSinceLastWatered = 0;
        }
    }

    /**
     * Menanam benih di tile ini. Dipanggil oleh Player.plant().
     * Diagram memiliki plant(s: Seed): void, ini adalah implementasi internalnya.
     * @param seed Benih yang akan ditanam.
     * @param currentSeason Musim saat ini untuk validasi.
     * @return true jika berhasil menanam, false jika gagal.
     */
    public boolean setPlantedSeed(Seed seed, Season currentSeason){
        if (this.type == TileType.TILLED && this.plantedSeed == null && seed != null){
            if(seed.getTargetSeason() != Season.ANY && seed.getTargetSeason() != currentSeason){
                System.err.println("GAGAL TANAM: Benih " + seed.getName() + " tidak cocok untuk musim " + currentSeason);
                return false;
            }
            this.plantedSeed = seed;
            this.growthDays = 0; 
            this.type = TileType.PLANTED;
            this.isWatered = false; 
            this.daysSinceLastWatered = 0;
            System.out.println("Berhasil menanam " + seed.getName() + " di Tile (" + this.hashCode() % 1000 + ").");
            return true;
        }
        System.err.println("GAGAL TANAM: Kondisi tile tidak memungkinkan untuk menanam " + (seed != null ? seed.getName() : "benih null") + ". Tile type: " + this.type + ", plantedSeed: " + (this.plantedSeed != null ? this.plantedSeed.getName() : "null"));
        return false;
    }

    /**
     * Sets the planted seed and type directly, intended for loading game state.
     * This method bypasses normal planting logic like growth day reset or season checks.
     */
    public void setPlantedSeedForLoad(Seed seed) {
        this.plantedSeed = seed;
        if (seed != null) {
            this.type = TileType.PLANTED;
        }
    }

    public boolean canBeWateredInternalCheck(){
        return this.type == TileType.PLANTED;
    }
    
    /**
     * Memproses panen dari tile ini. Dipanggil oleh Player.harvest().
     * @param itemRegistry Registry item untuk membuat objek Crop.
     * @return List berisi Item hasil panen, atau null jika tidak ada yang bisa dipanen.
     */
    public List<Item> processHarvest(Map<String, Item> itemRegistry){
        if (isHarvestable()){
            String cropName = plantedSeed.getCropYieldName();
            int quantity = plantedSeed.getQuantityPerHarvest();
            Item cropBase = itemRegistry.get(cropName);

            this.setType(TileType.TILLED);

            if (cropBase instanceof Crop){
                List<Item> harvestedItems = new ArrayList<>();
                for (int i = 0; i < quantity; i++){
                    harvestedItems.add(cropBase);
                }
                return harvestedItems;
            } else {
                System.err.println("PERINGATAN!\n (Tile.processHarvest): Crop '" + cropName + "' tidak ditemukan/valid di registry.");
                return Collections.emptyList();
            }
        }
        return null;
    }

    public boolean canBeTilled(){
        return this.type  == TileType.TILLABLE && this.plantedSeed == null;
    }

    public boolean isHarvestable(){
        return this.type == TileType.PLANTED && this.plantedSeed != null && this.growthDays >= this.plantedSeed.getDaysToHarvest();
    }

    public boolean needsWatering(Weather weather){
        if (this.isWatered || weather == Weather.RAINY) {
            return false;
        }
        return this.type == TileType.PLANTED;
    }

    public boolean canBeRecovered(){
        return this.type == TileType.TILLED || this.type == TileType.PLANTED;
    }

    /**
     * Mengupdate kondisi tile di akhir hari (pertumbuhan, status air).
     * Diagram memiliki incrementGrowth, resetWaterCounter, incrementDaysSinceWatered.
     * Metode ini mengintegrasikan logika tersebut.
     * @param weather Cuaca hari ini.
     * @param currentSeason Musim saat ini.
     */
    public void updateDaily(Weather weather, Season currentSeason){
        boolean effectivelyWateredForGrowth = this.isWatered;

        if (weather == Weather.RAINY && this.type == TileType.PLANTED) {
            effectivelyWateredForGrowth = true; 
        }

        if (this.type == TileType.PLANTED && this.plantedSeed != null) {
            if (this.plantedSeed.getTargetSeason() != Season.ANY && this.plantedSeed.getTargetSeason() != currentSeason) {
                System.out.println("Tanaman " + plantedSeed.getName() + " di Tile (" + this.hashCode() % 1000 + ") mati karena perubahan musim.");
                this.setType(TileType.TILLABLE); 
                this.isWatered = false; 
                return;
            }

            if (effectivelyWateredForGrowth) {
                if (!isHarvestable()) { 
                    this.growthDays++;
                    System.out.println("Tanaman " + plantedSeed.getName() + " di Tile (" + this.hashCode() % 1000 + ") tumbuh: day " + 
                        this.growthDays + "/" + plantedSeed.getDaysToHarvest() + " (watered)");
                } else {
                    System.out.println("Tanaman " + plantedSeed.getName() + " di Tile (" + this.hashCode() % 1000 + 
                        ") sudah siap panen: day " + this.growthDays + "/" + plantedSeed.getDaysToHarvest());
                }
                this.daysSinceLastWatered = 0; 
            } else {
                this.daysSinceLastWatered++;
                System.out.println("Tanaman " + plantedSeed.getName() + " di Tile (" + this.hashCode() % 1000 + ") tidak disiram hari ini. DaysSinceLastWatered: " + this.daysSinceLastWatered + ", Cuaca: " + weather);

                if (weather == Weather.SUNNY && this.daysSinceLastWatered >= WATERING_INTERVAL_HOT_WEATHER) {
                    System.out.println("Tanaman " + plantedSeed.getName() + " di Tile (" + this.hashCode() % 1000 + ") mati karena tidak disiram selama " + this.daysSinceLastWatered + " hari saat cuaca panas.");
                    this.setType(TileType.TILLED);
                    this.isWatered = false; 
                    return; 
                }
                
                if (this.daysSinceLastWatered > MAX_DAYS_WITHOUT_WATER_BEFORE_DEATH) {
                    System.out.println("Tanaman " + plantedSeed.getName() + " di Tile (" + this.hashCode() % 1000 + ") mati karena tidak disiram terlalu lama (" + this.daysSinceLastWatered + " hari).");
                    this.setType(TileType.TILLED);
                    this.isWatered = false;
                    return; 
                }
            }
        } else {
            this.daysSinceLastWatered = 0;
        }
        this.isWatered = false;
    }
    
    /**
     * Menghubungkan objek DeployedObject dengan tile ini.
     * Jika tile bukan ENTRY_POINT, tipenya akan diubah menjadi DEPLOYED_OBJECT.
     * @param obj Objek yang akan dihubungkan.
     * @return true jika berhasil, false jika gagal.
     */
    public boolean associateObject(DeployedObject obj) {
        if (this.associatedObject != null) {
            System.err.println("Tile (" + this.hashCode() % 1000 + ") sudah ditempati oleh objek lain.");
            return false;
        }
        
        if (this.plantedSeed != null) {
            System.err.println("Tidak bisa menempatkan objek di atas tanaman!");
            return false;
        }

        this.associatedObject = obj;
        
        if (this.type != TileType.ENTRY_POINT) {
            if (!(obj instanceof com.spakborhills.model.Object.Pond)) {
                this.setType(TileType.DEPLOYED_OBJECT);
            }
        }
        
        System.out.println("Objek '" + obj.getName() + "' ditempatkan di Tile (" + this.hashCode() % 1000 + ").");
        return true;
    }

    /**
     * Menghapus asosiasi DeployedObject dari tile ini.
     * Mengubah tipe tile kembali menjadi TILLABLE kecuali untuk ENTRY_POINT.
     */
    public void removeAssociatedObject() {
        if (this.associatedObject != null) {
            System.out.println("Objek '" + this.associatedObject.getName() + "' dihapus dari Tile (" + this.hashCode() % 1000 + ").");
            this.associatedObject = null;
            
            if (this.type != TileType.ENTRY_POINT) {
                this.setType(TileType.TILLABLE);
            }
        }
    }

    public void setGrowthDays(int growthDays) { 
        if (this.plantedSeed != null) { 
            this.growthDays = Math.max(0, growthDays); 
        }
    }

}
