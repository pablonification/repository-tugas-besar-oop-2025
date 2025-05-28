/*
 *   class Farm {
    - name: String
    - player: Player
    - farmMap: FarmMap
    - worldMap: WorldMap ' Added reference to world map areas
    - store: Store ' Added reference to store
    - npcs: List<NPC> ' List of all NPCs
    - recipes: List<Recipe> ' List of all Recipes
    - gameTime: GameTime
    - shippingBin: ShippingBin
    - statistics: EndGameStatistics ' Changed from StatisticsTracker
    - priceList: PriceList ' Added PriceList
    + getName(): String
    + getPlayer(): Player
    + getFarmMap(): FarmMap
    + getWorldMap(): WorldMap
    + getStore(): Store
    + getNpcs(): List<NPC>
    + findNPC(name: String): Optional<NPC>
    + getRecipes(): List<Recipe>
    + findRecipe(name: String): Recipe
    + getCurrentTime(): GameTime
    + getShippingBin(): ShippingBin
    + getStatistics(): EndGameStatistics
    + getPriceList(): PriceList
    + nextDay(): void ' Central logic for day change
    + checkEndConditions(): boolean
    + getMapArea(type: LocationType): MapArea ' Get specific map based on type
  }
 */

package com.spakborhills.model;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.ArrayList;

import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Enum.RelationshipStatus;
import com.spakborhills.model.Enum.GameState;
import com.spakborhills.model.Enum.Weather;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.NPC.NPC;
import com.spakborhills.model.Map.FarmMap;
import com.spakborhills.model.Map.MapArea;
import com.spakborhills.model.Map.WorldMap;
// import com.spakborhills.model.Store;
import com.spakborhills.model.Util.GameTime;
import com.spakborhills.model.Util.PriceList;
import com.spakborhills.model.Util.Recipe;
import com.spakborhills.model.Util.ShippingBin;
import com.spakborhills.model.Util.EndGameStatistics;
import com.spakborhills.model.Object.House;


public class Farm {
    private String name;
    private Player player;
    private FarmMap farmMap;
    private WorldMap worldMap;
    private Store store;
    private List<NPC> npcs;
    private List<Recipe> recipes;
    private GameTime gameTime;
    private ShippingBin shippingBin;
    private EndGameStatistics statistics;
    private PriceList priceList;
    private Map<String, Item> itemRegistry;
    private GameState currentGameState;
    private Weather currentWeather;
    private List<String> achievedMilestones;
    private House house;
    
/**
     * Konstruktor untuk Farm. Menginisialisasi seluruh state dunia game.
     * Objek-objek dependensi (player, maps, npcs, dll.) harus sudah dibuat sebelumnya.
     *
     * @param name        Nama Farm yang dipilih pemain.
     * @param player      Objek Player yang sudah diinisialisasi.
     * @param farmMap     Objek FarmMap yang sudah diinisialisasi.
     * @param worldMap    Objek WorldMap yang sudah diinisialisasi.
     * @param store       Objek Store yang sudah diinisialisasi.
     * @param npcs        List berisi semua objek NPC dalam game.
     * @param recipes     List berisi semua objek Recipe dalam game.
     * @param gameTime    Objek GameTime yang sudah diinisialisasi.
     * @param shippingBin Objek ShippingBin yang sudah diinisialisasi.
     * @param statistics  Objek EndGameStatistics yang sudah diinisialisasi.
     * @param priceList   Objek PriceList yang sudah diinisialisasi.
     */
    public Farm(String name, Player player, FarmMap farmMap, WorldMap worldMap, Store store, List<NPC> npcs, List<Recipe> recipes, GameTime gameTime, ShippingBin shippingBin, EndGameStatistics statistics, PriceList priceList, Map<String, Item> itemRegistry, House house) {
      this.name = name;
      this.player = player;
      this.farmMap = farmMap;
      this.worldMap = worldMap;
      this.store = store;
      this.npcs = List.copyOf(npcs); // immutable
      this.recipes = List.copyOf(recipes); // immutable
      this.gameTime = gameTime;
      this.shippingBin = shippingBin;
      this.statistics = statistics;
      this.priceList = priceList;
      this.itemRegistry = itemRegistry;
      this.currentGameState = GameState.MAIN_MENU;
      this.currentWeather = Weather.SUNNY;
      this.achievedMilestones = new ArrayList<>();
      this.house = house;
      System.out.println("Selamat datang di Kebun '" + name + "'!");
    }

    public String getName() {
      return name;
    }

    public Player getPlayer() {
      return player;
    }

    public FarmMap getFarmMap() {
      return farmMap;
    }

    public WorldMap getWorldMap() {
      return worldMap;
    }

    public Store getStore() {
      return store;
    }

    public List<NPC> getNpcs() {
      return npcs;
    }

    public List<Recipe> getRecipes() {
      return recipes;
    }

    public GameTime getCurrentTime() {
      return gameTime;
    }

    public ShippingBin getShippingBin() {
      return shippingBin;
    }

    public EndGameStatistics getStatistics() {
      return statistics;
    }

    public PriceList getPriceList() {
      return priceList;
    }
    
    public Map<String, Item> getItemRegistry() {
        return itemRegistry;
    }

    public GameState getCurrentGameState() {
        return currentGameState;
    }

    public void setCurrentGameState(GameState gameState) {
        this.currentGameState = gameState;
        System.out.println("GameState changed to: " + gameState);
    }

    public Weather getCurrentWeather() {
        return currentWeather;
    }

    public void setCurrentWeather(Weather currentWeather) {
        this.currentWeather = currentWeather;
    }

    public List<String> getAchievedMilestones() {
        if (this.achievedMilestones == null) {
            this.achievedMilestones = new ArrayList<>();
        }
        return achievedMilestones;
    }

    public void setAchievedMilestones(List<String> achievedMilestones) {
        this.achievedMilestones = achievedMilestones;
    }

    public House getHouse() {
        return house;
    }

    public void setHouse(House house) {
        this.house = house;
    }

    /**
     * Mencari NPC berdasarkan nama (tidak case-sensitive).
     * @param npcName Nama NPC yang dicari.
     * @return Optional berisi NPC jika ditemukan, Optional kosong jika tidak.
     */
    public Optional<NPC> findNPC(String npcName) {
      if(npcName == null || npcName.isBlank()) {
        return Optional.empty();
      }
      return npcs.stream()
        .filter(npc -> npcName.equalsIgnoreCase(npc.getName()))
        .findFirst();
    }
    
    /**
     * Mencari Resep berdasarkan nama (tidak case-sensitive).
     * @param recipeName Nama Resep yang dicari.
     * @return Optional berisi Resep jika ditemukan, Optional kosong jika tidak.
     */
    public Optional<Recipe> findRecipe(String recipeName) {
      if(recipeName == null || recipeName.isBlank()) {
        return Optional.empty();
      }
      return recipes.stream()
        .filter(recipe -> recipeName.equalsIgnoreCase(recipe.getName()))
        .findFirst();
    }

    /**
     * Memproses semua logika akhir hari, termasuk penjualan dari ShippingBin,
     * pembaruan GameTime, pertumbuhan tanaman, dan pembaruan statistik.
     * @return int jumlah gold yang didapatkan dari penjualan ShippingBin.
     */
    public int nextDay() {
      System.out.println("\n-- Malam tiba, memproses akhir hari --");

      int income = 0;
      if (shippingBin != null && statistics != null && priceList != null && gameTime != null && player != null) {
          income = shippingBin.processSales(this.statistics, this.priceList, this.gameTime.getCurrentDay(), this.gameTime.getCurrentSeason());
      player.addGold(income);
      shippingBin.clearBin(); // Reset ShippingBin untuk hari berikutnya
      } else {
          System.err.println("Farm.nextDay: Salah satu komponen (shippingBin, statistics, priceList, gameTime, player) adalah null. Penjualan dilewati.");
      }

      // Majukan waktu game ke next day
      if (gameTime != null) {
      gameTime.nextDay();
      System.out.println("--- Memulai Hari Baru ---");
      System.out.println("Hari ke-" + gameTime.getCurrentDay() + ", Musim " + gameTime.getCurrentSeason() + ", Cuaca: " + gameTime.getCurrentWeather());
      } else {
          System.err.println("Farm.nextDay: gameTime is null. Tidak bisa melanjutkan hari.");
          return income; // Kembalikan income sejauh ini jika gameTime null
      }

      // Update pertumbuhan tanaman
      if(farmMap != null && gameTime != null) { // gameTime check lagi untuk keamanan
        farmMap.updateDailyTiles(gameTime.getCurrentWeather(), gameTime.getCurrentSeason());
        System.out.println("Tanaman di kebun berhasil tumbuh...");
      }

      // Update statistik game
      if (statistics != null && gameTime != null) {
        statistics.incrementDay(gameTime.getCurrentSeason());
      }

        System.out.println("--- Hari baru telah dimulai ---");
      return income;
    }

    /**
     * Memaksa pemain tidur (misalnya karena pingsan), memproses hari berikutnya,
     * dan memulihkan energi pemain dengan penalti.
     * Logika pemulihan energi dan pesan pingsan kini ditangani di Player.passOut().
     * @return int jumlah gold yang didapatkan dari penjualan ShippingBin pada hari berikutnya.
     */
    public int forceSleepAndProcessNextDay() {
        return nextDay(); // Memproses hari berikutnya dan mengembalikan pendapatan
    }

/**
     * Memeriksa apakah salah satu kondisi milestone End Game (Halaman 34) sudah tercapai.
     * @return true jika salah satu milestone tercapai, false jika belum.
     */
    public boolean checkEndConditions() {
      // Milestone 1: Gold >=17209g
      boolean goldMet = player.getGold() >= 17209;

      // Milestone 2: Player sudah menikah (punya partner berstatus SPOUSE)
      boolean marriedMet = player.getPartner() != null && player.getPartner().getRelationshipStatus() == RelationshipStatus.SPOUSE;

      if(goldMet) {
        System.out.println("Milestone Tercapai: Kekayaan Melimpah (Gold >= 17209g)!");
      }
      if(marriedMet){
        System.out.println("Milestone Tercapai: Menikah!");
      }
      return goldMet || marriedMet;
    }
    
/**
     * Mendapatkan objek MapArea berdasarkan tipe lokasi.
     * Berguna untuk Controller saat menangani perpindahan pemain (visit).
     * Catatan: Ini mungkin perlu disesuaikan tergantung bagaimana WorldMap dan sub-lokasinya diimplementasikan.
     *
     * @param type Tipe lokasi yang diinginkan.
     * @return Objek MapArea yang sesuai, atau null jika tidak ditemukan/tidak relevan.
     */
    public MapArea getMapArea(LocationType type) {
      switch (type) {
          case FARM:
              return this.farmMap;
          case STORE:
              return this.worldMap.getSpecificArea(LocationType.STORE);
          case FOREST_RIVER:
          case MOUNTAIN_LAKE:
          case OCEAN:
          case MAYOR_TADI_HOME:
          case CAROLINE_HOME:
          case PERRY_HOME:
          case DASCO_HOME:
          case ABIGAIL_HOME:
              return this.worldMap.getSpecificArea(type);
          case POND:
              System.out.println("Pond berada di dalam FarmMap.");
              return this.farmMap;
          default:
              System.err.println("Tipe MapArea tidak dikenal: " + type);
              return null;
      }
  }
}
