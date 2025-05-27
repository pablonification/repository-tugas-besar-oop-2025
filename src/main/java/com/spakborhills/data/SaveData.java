package com.spakborhills.data;

import java.io.Serializable;
import java.util.List; // Ditambahkan untuk placedObjects
import java.util.Map; // Diubah untuk farmTiles agar lebih fleksibel dengan koordinat
// Import kelas-kelas lain yang mungkin dibutuhkan nanti
// import com.spakborhills.model.Player;
// import com.spakborhills.model.FarmModel;
// import com.spakborhills.model.TimeService;

public class SaveData implements Serializable {
    private static final long serialVersionUID = 1L; // Untuk kontrol versi serialisasi

    // Data Player
    private int playerX;
    private int playerY;
    private String currentMapId;
    private int playerMoney;
    private int playerEnergy;
    private InventoryData playerInventory; // Diaktifkan

    // Data Waktu
    private int currentDay;
    private int currentHour;
    private String currentSeason;
    private int currentYear;

    // Data Pertanian
    // Menggunakan Map untuk farmTiles agar bisa menyimpan berdasarkan koordinat (misal "x,y")
    // Ini lebih fleksibel daripada array 2D jika ukuran farm bisa berubah atau tidak semua tile perlu disimpan.
    // Jika farm selalu berukuran tetap dan semua tile disimpan, FarmTileData[][] juga bisa.
    private Map<String, FarmTileData> farmTiles; // Diaktifkan dan diubah tipe datanya
    // private List<PlacedObjectData> placedObjects; // Masih di-comment, akan dibuat jika diperlukan nanti

    // Konstruktor kosong untuk deserialisasi
    public SaveData() {
    }

    // Getter dan Setter untuk semua field
    // Contoh:
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

    public InventoryData getPlayerInventory() {
        return playerInventory;
    }

    public void setPlayerInventory(InventoryData playerInventory) {
        this.playerInventory = playerInventory;
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

    public Map<String, FarmTileData> getFarmTiles() {
        return farmTiles;
    }

    public void setFarmTiles(Map<String, FarmTileData> farmTiles) {
        this.farmTiles = farmTiles;
    }

    // TODO: Tambahkan getter dan setter untuk placedObjects jika sudah dibuat
    
} 