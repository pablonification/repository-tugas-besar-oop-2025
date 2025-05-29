package com.spakborhills.model.NPC;

import java.util.Arrays;

import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Player;
import com.spakborhills.model.Item.Item;

public class Dasco extends NPC {
    private static final String DASCO_SPRITESHEET_PATH = "/assets/sprites/npc/dasco_tile.png";

    // Untuk sprite di peta
    private static final int DEFAULT_SPRITE_X = 0;     
    private static final int DEFAULT_SPRITE_Y = 0;     
    private static final int SPRITE_WIDTH = 32;        
    private static final int SPRITE_HEIGHT = 32;       

    // Untuk potret di dialog 
    private static final int DEFAULT_PORTRAIT_X = 66; 
    private static final int DEFAULT_PORTRAIT_Y = 0;   
    private static final int PORTRAIT_WIDTH = 65;      
    private static final int PORTRAIT_HEIGHT = 64;     
    private static final String DASCO_PORTRAIT_PATH = "/assets/portraits/npc/dasco.png";

    public Dasco() {
        super("Dasco",
        LocationType.DASCO_HOME,
        true,
        DASCO_SPRITESHEET_PATH,
        DEFAULT_SPRITE_X, DEFAULT_SPRITE_Y, SPRITE_WIDTH, SPRITE_HEIGHT,
        DEFAULT_PORTRAIT_X, DEFAULT_PORTRAIT_Y, PORTRAIT_WIDTH, PORTRAIT_HEIGHT,
        DASCO_PORTRAIT_PATH); 
        this.lovedItems.addAll(Arrays.asList("The Legends of Spakbor", "Cooked Pig's Head", "Wine", "Fugu", "Spakbor Salad"));
        this.likedItems.addAll(Arrays.asList("Fish Sandwich", "Fish Stew", "Baguette", "Fish n' Chips"));
        this.hatedItems.addAll(Arrays.asList("Legend", "Grape", "Cauliflower", "Wheat", "Pufferfish", "Salmon"));
    }
    
    @Override
    public String getDialogue(Player player) {
        if (player != null) {
            return "Selamat datang di Kasino Spakbor Hills, " + player.getName() + ". Cari hiburan mewah? Atau mau mencoba keberuntunganmu?";
        }
        return "Selamat datang di Kasino Spakbor Hills! Cari hiburan mewah? Atau mau mencoba keberuntunganmu?";
    }

    @Override
    public String reactToGift(Item item, Player player) {
        if (item == null || item.getName() == null) {
            return "Apa maksudmu dengan ini?";
        }
        int preference = checkGiftPreference(item);
        String playerName = (player != null) ? player.getName() : "Sobat";

        if (preference == 25) { // Loved
            return "Ah, " + item.getName() + "! Luar biasa! Ini baru namanya hadiah, " + playerName + "!";
        }
        if (preference == 20) { // Liked
            return "Tidak buruk, " + playerName + ". " + item.getName() + " ini cukup berkelas.";
        }
        if (preference == -25) { // Hated
            return "Coba lagi, " + playerName + ". " + item.getName() + " ini... bukan seleraku.";
        }
        // Neutral
        return "Sebuah " + item.getName() + "? Baiklah, terima kasih, " + playerName + ".";
    }
}
