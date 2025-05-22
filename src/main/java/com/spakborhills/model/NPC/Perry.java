package com.spakborhills.model.NPC;

import java.util.Arrays;

import com.spakborhills.model.Enum.ItemCategory;
import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Player;

public class Perry extends NPC {
    public Perry() {
        super("Perry", LocationType.PERRY_HOME, false);
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
