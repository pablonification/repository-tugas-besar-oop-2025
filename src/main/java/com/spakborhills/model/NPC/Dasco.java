package com.spakborhills.model.NPC;

import java.util.Arrays;

import com.spakborhills.model.Enum.LocationType;

public class Dasco extends NPC {
    public Dasco() {
        super("Dasco", LocationType.NPC_HOME, false);
        this.lovedItems.addAll(Arrays.asList("The Legends of Spakbor", "Cooked Pig's Head", "Wine", "Fugu", "Spakbor Salad"));
        this.likedItems.addAll(Arrays.asList("Fish Sandwich", "Fish Stew", "Baguette", "Fish n’ Chips"));
        this.hatedItems.addAll(Arrays.asList("Legend", "Grape", "Cauliflower", "Wheat", "Pufferfish", "Salmon"));
    }
    
    @Override
    public void interact(Player player) {
        System.out.println(this.getName() + ": Selamat datang di Kasino Spakbor Hills, " + player.getName() + ". Cari hiburan mewah? Atau mau mencoba keberuntunganmu?");
        System.out.println("Ini interact Dasco");
    }
}
