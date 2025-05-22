package com.spakborhills.model.NPC;

import java.util.Arrays;

import com.spakborhills.model.Enum.ItemCategory;
import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Player;
public class Emily extends NPC {
    public Emily() {
        super("Emily", LocationType.STORE, false);
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
        // Emily's checkGiftPreference currently returns 0 for neutral/hated, 
        // so we'll add a generic neutral response if not loved/liked.
        // If hatedItems were explicitly checked for -25, we could add a hated response.
        // For now, if not 25 or 20, it's considered neutral by this reaction logic.
        return "Terima kasih untuk " + item.getName() + "-nya, " + playerName + ".";
    }
}
