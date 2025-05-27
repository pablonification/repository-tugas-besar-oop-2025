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
        // This needs to be calculated or stored differently if an absolute day is needed.
        // For now, assuming daysSinceLastWatered can be used or adapted.
        // If GameTime.getCurrentDay() - daysSinceLastWatered is needed, GameTime instance would be required here.
        // For simplicity with current structure, just returning daysSinceLastWatered.
        // A more robust solution would be to store the actual game day it was last watered.
        // Let's assume for now SaveLoadManager wants the *actual day index* it was last watered.
        // This requires a change in how Tile stores this. For now, placeholder.
        // To properly implement, Tile would need a field: private int actualLastWateredDay;
        // and markAsWatered() would set it. GameTime would be needed in markAsWatered().
        // For the current structure, we will return daysSinceLastWatered as a proxy.
        // It will be the responsibility of SaveLoadManager to interpret this if needed, or for Tile to be refactored.
        // REFECTOR CANDIDATE: Store actualLastWateredDay in Tile.
        // For now, returning daysSinceLastWatered as per current structure.
        return daysSinceLastWatered; 
    }

    public void setLastWateredDay(int day) {
        // This setter implies 'day' is the actual game day it was last watered.
        // To make this work with daysSinceLastWatered, we'd need current game day.
        // If 'day' is actually the intended value for daysSinceLastWatered from save file:
        this.daysSinceLastWatered = day;
        // If 'day' is the actual game day, and Tile needs to calculate daysSinceLastWatered from it,
        // then Tile needs access to GameTime.getCurrentDay() upon loading.
        // For now, directly setting daysSinceLastWatered based on the assumption that the
        // save file stores this relative counter or an equivalent that is passed as 'day'.
    }

    public void clearWatered() {
        this.isWatered = false;
        // Optionally, if daysSinceLastWatered should indicate it wasn't watered today:
        // this.daysSinceLastWatered = 1; // Or based on game logic if it implies it missed one day of watering.
        // For now, just clearing the flag.
    }

    // Setters
    public void setType(TileType newType){
        this.type = newType;
        // Jika tipe berubah, reset state lain
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
            this.growthDays = 0; // Reset growth days on new plant
            this.type = TileType.PLANTED;
            this.isWatered = false; // New plant needs water
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
        // Removed the automatic setting of tile type to TILLED when seed is null
        // This now preserves the tile type that was set from the saved data
        
        // isWatered and daysSinceLastWatered will be set separately by the loading logic
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

            // Reset state tile setelah panen
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
        if (this.isWatered || weather == Weather.RAINY) { // Already watered or raining? No need.
            return false;
        }
        // Only planted tiles need watering
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
        // Determine if the tile is effectively watered for today's growth calculation.
        // 'this.isWatered' here reflects if it was manually watered by the player before nextDay() was called.
        boolean effectivelyWateredForGrowth = this.isWatered;

        // Only planted tiles can benefit from rain
        if (weather == Weather.RAINY && this.type == TileType.PLANTED) {
            effectivelyWateredForGrowth = true; // Rain makes it watered for today's growth regardless of prior manual watering.
        }

        // Update pertumbuhan tanaman
        if (this.type == TileType.PLANTED && this.plantedSeed != null) {
            // Check for season mismatch first
            if (this.plantedSeed.getTargetSeason() != Season.ANY && this.plantedSeed.getTargetSeason() != currentSeason) {
                System.out.println("Tanaman " + plantedSeed.getName() + " di Tile (" + this.hashCode() % 1000 + ") mati karena perubahan musim.");
                this.setType(TileType.TILLABLE); // Reset tile
                this.isWatered = false; // Ensure isWatered is also reset before returning
                return;
            }

            // Now, apply growth logic based on whether it was effectively watered
            if (effectivelyWateredForGrowth) {
                if (!isHarvestable()) { // Only increment growthDays if not already harvestable
                    this.growthDays++;
                }
                this.daysSinceLastWatered = 0; // Reset counter because it got water
            } else {
                // Was not watered by player before nextDay() AND it did not rain today (implicitly weather == Weather.SUNNY)
                this.daysSinceLastWatered++;
                System.out.println("Tanaman " + plantedSeed.getName() + " di Tile (" + this.hashCode() % 1000 + ") tidak disiram hari ini. DaysSinceLastWatered: " + this.daysSinceLastWatered + ", Cuaca: " + weather);

                // Check for death due to hot weather rule first
                if (weather == Weather.SUNNY && this.daysSinceLastWatered >= WATERING_INTERVAL_HOT_WEATHER) {
                    System.out.println("Tanaman " + plantedSeed.getName() + " di Tile (" + this.hashCode() % 1000 + ") mati karena tidak disiram selama " + this.daysSinceLastWatered + " hari saat cuaca panas.");
                    this.setType(TileType.TILLED); // Kembali jadi tanah olahan kering
                    this.isWatered = false; 
                    return; 
                }
                
                // If not dead from hot weather rule, check general death rule
                if (this.daysSinceLastWatered > MAX_DAYS_WITHOUT_WATER_BEFORE_DEATH) {
                    System.out.println("Tanaman " + plantedSeed.getName() + " di Tile (" + this.hashCode() % 1000 + ") mati karena tidak disiram terlalu lama (" + this.daysSinceLastWatered + " hari).");
                    this.setType(TileType.TILLED); // Kembali jadi tanah olahan kering
                    this.isWatered = false;
                    return; 
                }
            }
        } else {
            // Not a planted tile, or no seed, so reset watering counters.
            this.daysSinceLastWatered = 0;
        }

        // CRUCIAL: Reset the 'isWatered' flag for the *next* day cycle.
        // This flag indicates if the player manually watered it on *their* turn.
        // It should be false at the start of the next player's turn, unless they water it again.
        this.isWatered = false;
    }
    
    /**
     * Menghubungkan objek DeployedObject dengan tile ini.
     * Jika tile bukan ENTRY_POINT, tipenya akan diubah menjadi DEPLOYED_OBJECT.
     * @param obj Objek yang akan dihubungkan.
     * @return true jika berhasil, false jika gagal.
     */
    public boolean associateObject(DeployedObject obj) {
        // Check if tile already has an object assigned or if planting exists
        if (this.associatedObject != null) {
            System.err.println("Tile (" + this.hashCode() % 1000 + ") sudah ditempati oleh objek lain.");
            return false;
        }
        
        if (this.plantedSeed != null) {
            System.err.println("Tidak bisa menempatkan objek di atas tanaman!");
            return false;
        }

        // Associate the object
        this.associatedObject = obj;
        
        // Don't change tile type if it's an ENTRY_POINT
        if (this.type != TileType.ENTRY_POINT) {
            // Let FarmMap.placeObject handle specific types like WATER for Ponds
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
            
            // Don't change the type back to TILLABLE if it's an ENTRY_POINT
            if (this.type != TileType.ENTRY_POINT) {
                this.setType(TileType.TILLABLE);
            }
        }
    }

    public void setGrowthDays(int growthDays) { // Added for loading
        if (this.plantedSeed != null) { // Only makes sense if there's a seed
            this.growthDays = Math.max(0, growthDays); // Basic validation
        }
    }

}
