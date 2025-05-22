package com.spakborhills.model.NPC;

import java.util.Arrays;

import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Player;
import com.spakborhills.model.Item.Item;

public class Abigail extends NPC {
    public Abigail() {
        super("Abigail", LocationType.ABIGAIL_HOME, false);
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
