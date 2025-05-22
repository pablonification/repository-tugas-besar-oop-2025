package com.spakborhills.model.NPC;

import java.util.Arrays;

import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Player;
import com.spakborhills.model.Item.Item;

public class Caroline extends NPC {
    public Caroline() {
        super("Caroline", LocationType.CAROLINE_HOME, false);
        this.lovedItems.addAll(Arrays.asList("Firewood", "Coal"));
        this.likedItems.addAll(Arrays.asList("Potato", "Wheat"));
        this.hatedItems.add("Hot Pepper");
    }

    @Override
    public String getDialogue(Player player) {
        if (player != null) {
            return "Oh, halo, " + player.getName() + "! Butuh bahan dasar atau tertarik melihat hasil kerajinan daur ulangku?";
        }
        return "Halo! Tertarik dengan kerajinan atau butuh bahan?";
    }

    @Override
    public String reactToGift(Item item, Player player) {
        if (item == null || item.getName() == null) {
            return "Hm? Kamu memberiku apa?";
        }
        int preference = checkGiftPreference(item);
        String playerName = (player != null) ? player.getName() : "";

        if (preference == 25) { // Loved
            return "Untukku? Sebuah " + item.getName() + "? Terima kasih banyak, " + playerName + "! Ini sangat berguna!";
        }
        if (preference == 20) { // Liked
            return "Oh, " + item.getName() + ". Ini bagus, terima kasih ya, " + playerName + ".";
        }
        if (preference == -25) { // Hated
            return "Maaf, " + playerName + ", tapi aku tidak begitu suka " + item.getName() + ".";
        }
        // Neutral
        return "Terima kasih untuk " + item.getName() + "-nya, " + playerName + ".";
    }
}