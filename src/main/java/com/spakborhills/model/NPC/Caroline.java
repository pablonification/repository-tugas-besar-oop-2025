package com.spakborhills.model.NPC;

import java.util.Arrays;

import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Player;

public class Caroline extends NPC {
    public Caroline() {
        super("Caroline", LocationType.NPC_HOME, false);
        this.lovedItems.addAll(Arrays.asList("Findewood", "Coal"));
        this.likedItems.addAll(Arrays.asList("Potato", "Wheat"));
        this.hatedItems.add("Hot Pepper");
    }

    @Override
    public void interact(Player player) {
        System.out.println(this.getName() + ": Oh, halo, " + player.getName() + "! Butuh bahan dasar atau tertarik melihat hasil kerajinan daur ulangku?");
        System.out.println("Ini interact Caroline");
    }
}