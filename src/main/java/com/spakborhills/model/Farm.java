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
    + getNPCs(): List<NPC>
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

import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Enum.RelationshipStatus;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.NPC.NPC;
import com.spakborhills.model.Map.FarmMap;
import com.spakborhills.model.Map.MapArea;
import com.spakborhills.model.Map.WorldMap;
import com.spakborhills.model.Store;
import com.spakborhills.model.Util.GameTime;
import com.spakborhills.model.Util.PriceList;
import com.spakborhills.model.Util.Recipe;
import com.spakborhills.model.Util.ShippingBin;
import com.spakborhills.model.Util.EndGameStatistics;


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
    public Farm(String name, Player player, FarmMap farmMap, WorldMap worldMap, Store store, List<NPC> npcs, List<Recipe> recipes, GameTime gameTime, ShippingBin shippingBin, EndGameStatistics statistics, PriceList priceList, Map<String, Item> itemRegistry) {
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

    public List<NPC> getNPCs() {
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

    public void nextDay() {
      System.out.println("\n-- Malam tiba, memproses akhir hari --");

      // Proses penjualan ShippingBin
      // Asumsi processSales mengembalikan total pendapatan dan mengupdate statistik internal
      int income = shippingBin.processSales(this.statistics, this.priceList, this.gameTime.getCurrentDay(), this.gameTime.getCurrentSeason());
      player.addGold(income);
      shippingBin.clearBin(); // Reset ShippingBin untuk hari berikutnya
      if(income > 0) {
        System.out.println("Kamu mendapatkan " + income + " gold dari penjualan kemarin.");
      } else {
        System.out.println("Tidak ada penjualan dari Shipping Bin kemarin.");
      }

      // Majukan waktu game ke next day
      gameTime.nextDay();
      System.out.println("--- Memulai Hari Baru ---");
      System.out.println("Hari ke-" + gameTime.getCurrentDay() + ", Musim " + gameTime.getCurrentSeason() + ", Cuaca: " + gameTime.getCurrentWeather());

      // Update pertumbuhan tanaman
      if(farmMap != null) {
        farmMap.updateDailyTiles(gameTime.getCurrentWeather(), gameTime.getCurrentSeason());
        System.out.println("Tanaman di kebun berhasil tumbuh...");
      }

      // 4. Reset Status Harian Lainnya (jika ada)
        // Contoh: reset batas bicara/hadiah NPC per hari (logika ini mungkin ada di Controller/NPC)

      // 5. Update statistik game
      if (statistics != null) {
        statistics.incrementDay(gameTime.getCurrentSeason());
      }

      // 6. (Bonus) Update Pasar jika fitur Free Market diimplementasikan
        // market.updatePrices();


        System.out.println("--- Hari baru telah dimulai ---");
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
          case NPC_HOME:
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
