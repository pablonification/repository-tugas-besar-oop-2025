package com.spakborhills.model.NPC;

import java.util.Arrays;

import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Player;
import com.spakborhills.model.Item.Item;

public class Abigail extends NPC {
    private static final String ABIGAIL_SPRITESHEET_PATH = "/assets/sprites/npc/abigail.png";
    // CONTOH KOORDINAT DAN DIMENSI (HARUS DISESUAIKAN DENGAN SPRITESHEET ANDA!)
    // Untuk sprite di peta (misal, frame pertama menghadap ke bawah)
    private static final int DEFAULT_SPRITE_X = 1;     // Koordinat X frame di spritesheet
    private static final int DEFAULT_SPRITE_Y = 5;     // Koordinat Y frame di spritesheet
    private static final int SPRITE_WIDTH = 14;        // Lebar satu frame sprite
    private static final int SPRITE_HEIGHT = 27;       // Tinggi satu frame sprite

    // Untuk potret di dialog (misal, potret netral pertama)
    private static final int DEFAULT_PORTRAIT_X = 68; // Koordinat X potret di spritesheet
    private static final int DEFAULT_PORTRAIT_Y = 135;   // Koordinat Y potret di spritesheet
    private static final int PORTRAIT_WIDTH = 53;      // Lebar satu potret
    private static final int PORTRAIT_HEIGHT = 57;     // Tinggi satu potret
    
    public Abigail() {
        super("Abigail",
              LocationType.ABIGAIL_HOME,
              false,
              ABIGAIL_SPRITESHEET_PATH,
              DEFAULT_SPRITE_X, DEFAULT_SPRITE_Y, SPRITE_WIDTH, SPRITE_HEIGHT,
              DEFAULT_PORTRAIT_X, DEFAULT_PORTRAIT_Y, PORTRAIT_WIDTH, PORTRAIT_HEIGHT);

        this.lovedItems.addAll(Arrays.asList("Blueberry", "Melon", "Pumpkin", "Grape", "Cranberry"));
        this.likedItems.addAll(Arrays.asList("Baguette", "Pumpkin Pie", "Wine"));
        this.hatedItems.addAll(Arrays.asList("Hot Pepper", "Cauliflower", "Parsnip", "Wheat"));
    }

    @Override
    public String getDialogue(Player player) {
        if (player != null) {
            return "Hei, " + player.getName() + "! Siap untuk petualangan hari ini? Aku baru saja makan buah untuk energi!";
        }
        return "Hei, aku baru saja makan buah untuk energi!";
    }

    @Override
    public String reactToGift(Item item, Player player) {
        if (item == null || item.getName() == null) {
            return "Uh, apa ini?";
        }
        int preference = checkGiftPreference(item);
        String playerName = (player != null) ? player.getName() : "Petualang";

        if (preference == 25) { // Loved
            return "Wow, " + item.getName() + "! Ini kesukaanku! Terima kasih, " + playerName + "!";
        }
        if (preference == 20) { // Liked
            return item.getName() + "? Keren! Makasih, " + playerName + ".";
        }
        if (preference == -25) { // Hated
            return "Um... " + item.getName() + "? Aku tidak begitu suka ini, " + playerName + ". Tapi makasih sudah berusaha.";
        }
        // Neutral
        return "Oh, " + item.getName() + ". Oke, makasih ya, " + playerName + ".";
    }
}
