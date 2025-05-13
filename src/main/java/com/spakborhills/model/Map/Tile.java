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
                return false;
            }
            plantedSeed = seed;
            growthDays = 0;
            type = TileType.PLANTED;
            isWatered = false;
            daysSinceLastWatered = 0;
            return true;
        }
        return false;
    }

    public boolean canBeWateredInternalCheck(){
        return this.type == TileType.TILLED || this.type == TileType.PLANTED;
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
            this.setType(TileType.TILLABLE);

            if (cropBase instanceof Crop){
                List<Item> harvestedItems = new ArrayList<>();
                for (int i = 0; i < quantity; i++){
                    harvestedItems.add(new Crop(cropBase.getName(), cropBase.getBuyPrice(), cropBase.getSellPrice()));
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
        if(!canBeWateredInternalCheck() || this.isWatered || weather == Weather.RAINY){
            return false;
        }
        if(weather == Weather.SUNNY && this.daysSinceLastWatered >= WATERING_INTERVAL_HOT_WEATHER -1){
            return true;
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
        // Siram otomatis jika hujan
        if(weather == Weather.RAINY && canBeWateredInternalCheck()){
            isWatered = true;
            daysSinceLastWatered = 0;
        }

        // Update pertumbuhan tanaman
        if(this.type == TileType.PLANTED && this.plantedSeed != null){
            if(this.plantedSeed.getTargetSeason() != Season.ANY && this.plantedSeed.getTargetSeason() != currentSeason){
                System.out.println("Tanaman " + plantedSeed.getName() + " di Tile (" + this.hashCode() % 1000 + ") mati karena perubahan musim.");
                this.setType(TileType.TILLABLE);
                return;
            }

            if(this.isWatered){
                if(!isHarvestable()){
                    this.growthDays++;
                }
                this.daysSinceLastWatered = 0;
            } else {
                this.daysSinceLastWatered++;
            }
        } else {
            this.daysSinceLastWatered = 0;
        }
        this.isWatered = false;
    }
    
    /**
     * Mengasosiasikan DeployedObject dengan tile ini.
     * Mengubah tipe tile menjadi DEPLOYED_OBJECT.
     * @param obj Objek yang akan ditempatkan.
     * @return true jika berhasil ditempatkan, false jika tile sudah ditempati atau tidak valid.
     */
    public boolean associateObject(DeployedObject obj){
        if(this.type == TileType.DEPLOYED_OBJECT || this.associatedObject != null){
            System.err.println("Tile (" + this.hashCode() % 1000 + ") sudah ditempati oleh objek lain.");
            return false;
        }
        if(this.plantedSeed != null){
            System.err.println("Tidak bisa menempatkan objek di atas tanaman!");
        }
        this.associatedObject = obj;
        this.setType(TileType.DEPLOYED_OBJECT);
        System.out.println("Objek '" + obj.getName() + "' ditempatkan di Tile (" + this.hashCode() % 1000 + ").");
        return true;
    }

    /**
     * Menghapus asosiasi DeployedObject dari tile ini.
     * Mengubah tipe tile kembali menjadi TILLABLE.
     */
    public void removeAssociatedObject(){
        if(this.associatedObject != null){
            System.out.println("Objek '" + this.associatedObject.getName() + "' dihapus dari Tile (" + this.hashCode() % 1000 + ").");
            this.associatedObject = null;
            this.setType(TileType.TILLABLE);
        }
    }

}
