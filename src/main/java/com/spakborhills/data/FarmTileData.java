package com.spakborhills.data;

import java.io.Serializable;

public class FarmTileData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String cropId; // ID tanaman yang ditanam, null jika tidak ada
    private int growthStage; // Tahap pertumbuhan tanaman
    private boolean isWatered; // Apakah tile sudah disiram
    // Tambahkan properti lain jika ada, misalnya status pupuk, dll.

    public FarmTileData() {
    }

    // Contoh konstruktor dengan parameter
    public FarmTileData(String cropId, int growthStage, boolean isWatered) {
        this.cropId = cropId;
        this.growthStage = growthStage;
        this.isWatered = isWatered;
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
} 