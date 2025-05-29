package com.spakborhills.model.NPC;

import java.util.Arrays;

import com.spakborhills.model.Enum.ItemCategory;
import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Player;
public class Emily extends NPC {
    private static final String EMILY_SPRITESHEET_PATH = "/assets/sprites/npc/emily_tile.png";
    
    // Untuk sprite di peta
    private static final int DEFAULT_SPRITE_X = 0;     
    private static final int DEFAULT_SPRITE_Y = 0;    
    private static final int SPRITE_WIDTH = 32;        
    private static final int SPRITE_HEIGHT = 32;       

    // Untuk potret di dialog 
    private static final int DEFAULT_PORTRAIT_X = 68; 
    private static final int DEFAULT_PORTRAIT_Y = 195;   
    private static final int PORTRAIT_WIDTH = 55;     
    private static final int PORTRAIT_HEIGHT = 61;    
    private static final String EMILY_PORTRAIT_PATH = "/assets/portraits/npc/emily.png";

    public Emily() {
        super("Emily",
        LocationType.STORE,
        true,
        EMILY_SPRITESHEET_PATH,
        DEFAULT_SPRITE_X, DEFAULT_SPRITE_Y, SPRITE_WIDTH, SPRITE_HEIGHT,
        DEFAULT_PORTRAIT_X, DEFAULT_PORTRAIT_Y, PORTRAIT_WIDTH, PORTRAIT_HEIGHT,
        EMILY_PORTRAIT_PATH);
        this.likedItems.addAll(Arrays.asList("Catfish", "Salmon", "Sardine"));
        this.hatedItems.addAll(Arrays.asList("Coal", "Wood"));
    }
    
    @Override
    public int checkGiftPreference(Item item){
        if(item.getCategory() == ItemCategory.SEED){
            return 25;
        }

        if(this.likedItems.contains(item.getName())){
            return 20;
        }

        return 0;
    }

    @Override
    public String getDialogue(Player player) {
        if (player != null) {
            return "Oh, hai " + player.getName() + "! Selamat datang di restoran. Apa ada yang bisa kubantu? Atau mungkin... kamu punya bibit baru untuk kebunku?";
        }
        return "Oh, hai! Selamat datang di restoran. Apa ada yang bisa kubantu? Atau mungkin... kamu punya bibit baru untuk kebunku?";
    }

    @Override
    public String reactToGift(Item item, Player player) {
        if (item == null || item.getName() == null) {
            return "Untukku? Apa ini?";
        }
        int preference = checkGiftPreference(item); // Emily has custom checkGiftPreference logic
        String playerName = (player != null) ? player.getName() : "Pelanggan";

        if (preference == 25) { // Loved (Seeds for Emily)
            return "Oh, " + item.getName() + "! Bibit baru! Terima kasih banyak, " + playerName + ", ini akan sangat membantu kebunku!";
        }
        if (preference == 20) { // Liked
            return "Sebuah " + item.getName() + ", ya? Manis sekali, terima kasih, " + playerName + "!";
        }
        return "Terima kasih untuk " + item.getName() + "-nya, " + playerName + ".";
    }
}
