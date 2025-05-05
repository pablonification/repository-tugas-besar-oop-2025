package com.spakborhills.model.NPC;

import java.util.Arrays;

import com.spakborhills.model.Enum.ItemCategory;
import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Player;

public class Perry extends NPC {
    public Perry() {
        super("Perry", LocationType.NPC_HOME, false);
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
    public void interact(Player player) {
        System.out.println("Ini interact Perry");
        System.out.println(this.getName() + ": Oh... h-halo, " + player.getName() + ". Maaf, aku sedang mencoba fokus menulis...");
    }
    
    
}
