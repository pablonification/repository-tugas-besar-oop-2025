package com.spakborhills.model.NPC;

import java.util.Arrays;

import com.spakborhills.model.Enum.ItemCategory;
import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Player;

public class Perry extends NPC {
    private static final String PERRY_SPRITESHEET_PATH = "/assets/sprites/npc/perry_tile.png";
    
    // Untuk sprite di peta
    private static final int DEFAULT_SPRITE_X = 0;     
    private static final int DEFAULT_SPRITE_Y = 0;     
    private static final int SPRITE_WIDTH = 32;        
    private static final int SPRITE_HEIGHT = 32;       

    // Untuk potret di dialog 
    private static final int DEFAULT_PORTRAIT_X = 64; 
    private static final int DEFAULT_PORTRAIT_Y = 0;   
    private static final int PORTRAIT_WIDTH = 65;      
    private static final int PORTRAIT_HEIGHT = 64;     
    private static final String PERRY_PORTRAIT_PATH = "/assets/portraits/npc/perry.png"; 

    public Perry() {
        super("Perry", LocationType.PERRY_HOME, true,
        PERRY_SPRITESHEET_PATH,
        DEFAULT_SPRITE_X, DEFAULT_SPRITE_Y, SPRITE_WIDTH, SPRITE_HEIGHT,
        DEFAULT_PORTRAIT_X, DEFAULT_PORTRAIT_Y, PORTRAIT_WIDTH, PORTRAIT_HEIGHT,
        PERRY_PORTRAIT_PATH);
        this.lovedItems.addAll(Arrays.asList("Cranberry", "Blueberry"));
        this.likedItems.add("Wine");
    }

    @Override
    public int checkGiftPreference(Item item){
        if(lovedItems.contains(item.getName())){
            return 25;
        }
        else if(likedItems.contains(item.getName())){
            return 20;
        }
        else if(item.getCategory() == ItemCategory.FISH){
            return -25;
        }
        return 0;
    }


    @Override
    public String getDialogue(Player player) {
        if (player != null) {
            return "Oh... h-halo, " + player.getName() + ". Maaf, aku sedang mencoba fokus menulis...";
        }
        return "Oh... h-halo, aku sedang mencoba fokus menulis...";
    }
    
    @Override
    public String reactToGift(Item item, Player player) {
        if (item == null || item.getName() == null) {
            return "...Apa ini?";
        }
        int preference = checkGiftPreference(item); // Perry has custom logic
        String playerName = (player != null) ? player.getName() : "...";

        if (preference == 25) { // Loved
            return "Wow, " + item.getName() + "! Ini... ini sangat bagus! Terima kasih, " + playerName + "!";
        }
        if (preference == 20) { // Liked
            return "Oh, " + item.getName() + ". Lumayan. Terima kasih, " + playerName + ".";
        }
        if (preference == -25) { // Hated
            return "Ugh, " + item.getName() + "... Aku tidak suka ini, " + playerName + ". Tolong jauhkan.";
        }
        // Neutral
        return "Untukku? " + item.getName() + "... Terima kasih.";
    }
}
