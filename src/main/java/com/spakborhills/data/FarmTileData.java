package com.spakborhills.data;

import java.io.Serializable;

public class FarmTileData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String tileType; // New: String representation of TileType enum
    private String cropId; // ID tanaman yang ditanam, null jika tidak ada. Renamed from seedId for clarity with JSON example.
    private int growthStage; // Tahap pertumbuhan tanaman
    private boolean isWatered; // Apakah tile sudah disiram hari ini
    private int lastWateredDay; // New: Menyimpan hari terakhir tile disiram
    // Tambahkan properti lain jika ada, misalnya status pupuk, dll.

    public FarmTileData() {
    }

    // Contoh konstruktor dengan parameter
    public FarmTileData(String tileType, String cropId, int growthStage, boolean isWatered, int lastWateredDay) {
        this.tileType = tileType;
        this.cropId = cropId;
        this.growthStage = growthStage;
        this.isWatered = isWatered;
        this.lastWateredDay = lastWateredDay;
    }

    public String getTileType() {
        return tileType;
    }

    public void setTileType(String tileType) {
        this.tileType = tileType;
    }

    public String getCropId() {
        return cropId;
    }

    public void setCropId(String cropId) {
        this.cropId = cropId;
    }

    public int getGrowthStage() {
        return growthStage;
    }

    public void setGrowthStage(int growthStage) {
        this.growthStage = growthStage;
    }

    public boolean isWatered() {
        return isWatered;
    }

    public void setWatered(boolean watered) {
        isWatered = watered;
    }

    public int getLastWateredDay() {
        return lastWateredDay;
    }

    public void setLastWateredDay(int lastWateredDay) {
        this.lastWateredDay = lastWateredDay;
    }
} 