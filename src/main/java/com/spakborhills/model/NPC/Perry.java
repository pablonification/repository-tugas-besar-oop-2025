package com.spakborhills.model.NPC;

import java.util.Arrays;

import com.spakborhills.model.Enum.ItemCategory;
import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Player;

public class Perry extends NPC {
    private static final String PERRY_SPRITESHEET_PATH = "/assets/sprites/npc/perry.png";
    // CONTOH KOORDINAT DAN DIMENSI (HARUS DISESUAIKAN DENGAN SPRITESHEET ANDA!)
    // Untuk sprite di peta (misal, frame pertama menghadap ke bawah)
    private static final int DEFAULT_SPRITE_X = 1;     // Koordinat X frame di spritesheet
    private static final int DEFAULT_SPRITE_Y = 1;     // Koordinat Y frame di spritesheet
    private static final int SPRITE_WIDTH = 14;        // Lebar satu frame sprite
    private static final int SPRITE_HEIGHT = 31;       // Tinggi satu frame sprite

    // Untuk potret di dialog (misal, potret netral pertama)
    private static final int DEFAULT_PORTRAIT_X = 64; // Koordinat X potret di spritesheet
    private static final int DEFAULT_PORTRAIT_Y = 0;   // Koordinat Y potret di spritesheet
    private static final int PORTRAIT_WIDTH = 65;      // Lebar satu potret
    private static final int PORTRAIT_HEIGHT = 64;     // Tinggi satu potret
    public Perry() {
        super("Perry", LocationType.PERRY_HOME, false,
        PERRY_SPRITESHEET_PATH,
        DEFAULT_SPRITE_X, DEFAULT_SPRITE_Y, SPRITE_WIDTH, SPRITE_HEIGHT,
        DEFAULT_PORTRAIT_X, DEFAULT_PORTRAIT_Y, PORTRAIT_WIDTH, PORTRAIT_HEIGHT);
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
        int preference = checkGiftPreference(item); // Perry has custom logic (hates all fish)
        String playerName = (player != null) ? player.getName() : "...";

        if (preference == 25) { // Loved
            return "Wow, " + item.getName() + "! Ini... ini sangat bagus! Terima kasih, " + playerName + "!";
        }
        if (preference == 20) { // Liked
            return "Oh, " + item.getName() + ". Lumayan. Terima kasih, " + playerName + ".";
        }
        if (preference == -25) { // Hated (e.g., Fish for Perry)
            return "Ugh, " + item.getName() + "... Aku tidak suka ini, " + playerName + ". Tolong jauhkan.";
        }
        // Neutral
        return "Untukku? " + item.getName() + "... Terima kasih.";
    }
}
