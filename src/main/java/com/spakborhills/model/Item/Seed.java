/*
 *   class Seed extends Item {
    - daysToHarvest: int
    - targetSeason: Season
    - cropYieldName: String
    - quantityPerHarvest: int
    + use(player: Player, target: Object): boolean
    + getDaysToHarvest(): int
    + getTargetSeason(): Season
    + getCropYieldName(): String
    + getQuantityPerHarvest(): int
  }
 */
package com.spakborhills.model.Item;

import com.spakborhills.model.Enum.ItemCategory;
import com.spakborhills.model.Enum.Season;
import com.spakborhills.model.Enum.TileType;
import com.spakborhills.model.Player;
import com.spakborhills.model.Tile;


public class Seed extends Item {
    private final int daysToHarvest;
    private final Season targetSeason;
    private final String cropYieldName;
    private final int quantityPerHarvest;

    /**
     * Constructor for Seed items.
     *
     * @param name               The name of the seed (e.g., "Parsnip Seeds").
     * @param buyPrice           The price to buy the seed from the store.
     * @param daysToHarvest      Days required for the seed to mature into a crop.
     * @param targetSeason       The season(s) in which this seed can grow.
     * @param cropYieldName      The name of the Crop item produced upon harvest.
     * @param quantityPerHarvest The number of Crop items yielded per harvest action.
     */
    public Seed(String name, int buyPrice, int daysToHarvest, Season targetSeason, String cropYieldName, int quantityPerHarvest) {
        super(name, ItemCategory.SEED, buyPrice, buyPrice/2);
        this.daysToHarvest = daysToHarvest;
        this.targetSeason = targetSeason;
        this.cropYieldName = cropYieldName;
        this.quantityPerHarvest = quantityPerHarvest;
    }

    @Override
    public boolean use(Player player, Object target) {
        // Validasi target adalah Tile
        if (!(target instanceof Tile)) {
            System.out.println("ERROR: Benih hanya bisa ditanam di petak tanah!");
            return false;
        }
        Tile tile = (Tile) target;

        // Validasi tile harus sudah dibajak/Tilled
        if(tile.getType() != TileType.TILLED) {
            System.out.println("ERROR: Petak tanah harus dibajak terlebih dahulu!");
            return false;
        }

        // Lakukan penanaman pada Tile
        // Tipe Tile -> Planted dan simpan referensi ke seed ini
        boolean plantSuccess = tile.plant(this);
        if(plantSuccess) {
            System.out.println("Kamu menanam " + this.getName() + " di petak tanah.");
            // Controller akan hapus 1 seed dari inventory player setelah return true
            return true;
        } else {
            System.out.println("Penanaman gagal. Petak tanah tidak sesuai atau sudah memiliki tanaman lain.");
            return false;
        }
        
    }

    public int getDaysToHarvest() {
        return daysToHarvest;
    }

    public Season getTargetSeason() {
        return targetSeason;
    }

    public String getCropYieldName() {
        return cropYieldName;
    }

    public int getQuantityPerHarvest() {
        return quantityPerHarvest;
    }
   
}
