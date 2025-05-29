package com.spakborhills.model.NPC;

import java.util.Arrays;

import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Player;

/*
 * Atribut Nilai
name: Mayor Tadi
lovedItems: Legend
likedItems: Angler, Crimsonfish, Glacierfish
hatedItems: Seluruh item yang bukan merupakan lovedItems dan likedItems


 */
public class MayorTadi extends NPC {
    private static final String MAYOR_TADI_SPRITESHEET_PATH = "/assets/sprites/npc/mayor_tadi_tile.png";

    // Untuk sprite di peta
    private static final int DEFAULT_SPRITE_X = 0;     
    private static final int DEFAULT_SPRITE_Y = 0;     
    private static final int SPRITE_WIDTH = 32;       
    private static final int SPRITE_HEIGHT = 32;       

    // Untuk potret di dialog
    private static final int DEFAULT_PORTRAIT_X = 64; 
    private static final int DEFAULT_PORTRAIT_Y = 99;   
    private static final int PORTRAIT_WIDTH = 64;      
    private static final int PORTRAIT_HEIGHT = 61;     
    private static final String MAYOR_TADI_PORTRAIT_PATH = "/assets/portraits/npc/mayor_tadi.png"; 

    public MayorTadi() {
        super("Mayor Tadi",
        LocationType.MAYOR_TADI_HOME,
        true,
        MAYOR_TADI_SPRITESHEET_PATH,
        DEFAULT_SPRITE_X, DEFAULT_SPRITE_Y, SPRITE_WIDTH, SPRITE_HEIGHT,
        DEFAULT_PORTRAIT_X, DEFAULT_PORTRAIT_Y, PORTRAIT_WIDTH, PORTRAIT_HEIGHT,
        MAYOR_TADI_PORTRAIT_PATH); // Pass the new portrait path
        this.lovedItems.add("Legend");
        this.likedItems.addAll(Arrays.asList("Angler", "Crimsonfish", "Glacierfish"));
    }

    @Override
    public int checkGiftPreference(Item item){
        if (item == null || item.getName() == null) return -25; // If item is null, consider it hated.
        if(lovedItems.contains(item.getName())){
            return 25;
        }
        else if(likedItems.contains(item.getName())){
            return 20;
        }
        return -25; // All other items are hated by Mayor Tadi
    }

    @Override
    public String getDialogue(Player player) {
        if (player != null) {
            return "Selamat datang di Spakbor Hills, " + player.getName() + "! Ada yang bisa saya bantu hari ini?";
        }
        return "Selamat datang di Spakbor Hills! Ada yang bisa saya bantu?";
    }

    @Override
    public String reactToGift(Item item, Player player) {
        if (item == null || item.getName() == null) {
            return "Apa ini? Saya tidak mengerti.";
        }
        int preference = checkGiftPreference(item);
        if (preference == 25) { // Loved
            return "Wow, " + item.getName() + "! Ini luar biasa, terima kasih banyak, " + player.getName() + "!";
        }
        if (preference == 20) { // Liked
            return "Oh, sebuah " + item.getName() + ". Terima kasih, ini sangat berarti.";
        }
        // Hated (since Mayor Tadi hates everything not loved/liked)
        return "Saya tidak yakin apa yang harus saya lakukan dengan " + item.getName() + " ini, tapi... terima kasih?"; 
    }
}
