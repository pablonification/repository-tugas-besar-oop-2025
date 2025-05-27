/*
 *  class Equipment extends Item {
    - toolType: String
    + use(player: Player, target: Object): boolean
    + getToolType(): String
  }
 */
package com.spakborhills.model.Item;

import com.spakborhills.model.Enum.ItemCategory;
import com.spakborhills.model.Player;


public class Equipment extends Item {
    private String toolType;

    public Equipment(String name, String toolType) {
        super(name, ItemCategory.EQUIPMENT, 0, 0);
        this.toolType = toolType;
    }

    @Override
    public boolean use(Player player, Object target) {
        // Action seperti nyangkul, nyiram, dll akan dipicu oleh input pemain dan dihanle oleh Player/Controller
        // yang meriksa getToolType() dari item ini
        System.out.println("Menggunakan " + getName() + "...");
        return true;
    }
    /**
     * Mendapatkan tipe spesifik dari peralatan ini.
     * @return String yang mengidentifikasi tipe alat (e.g., "Hoe", "WateringCan").
     */
    public String getToolType() {
        return toolType;
    }
    
    @Override
    public Item cloneItem() {
        return new Equipment(this.getName(), this.getToolType());
    }
}
