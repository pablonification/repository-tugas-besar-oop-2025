package com.spakborhills.model.NPC;

import java.util.Arrays;

import com.spakborhills.model.Enum.ItemCategory;
import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Item.Item;

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
    public void interact(Player player) {
        System.out.println(this.getName() + ": Oh, hai " + player.getName() + "! Selamat datang di restoran. Apa ada yang bisa kubantu? Atau mungkin... kamu punya bibit baru untuk kebunku?");
        System.out.println("Ini interact Emily");
    }
}
