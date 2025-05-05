package com.spakborhills.model.NPC;

import java.util.Arrays;

import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Player;

public class Abigail extends NPC {
    public Abigail() {
        super("Abigail", LocationType.NPC_HOME, false);
        this.lovedItems.addAll(Arrays.asList("Blueberry", "Melon", "Pumpkin", "Grape", "Cranberry"));
        this.likedItems.addAll(Arrays.asList("Baguette", "Pumpkin Pie", "Wine"));
        this.hatedItems.addAll(Arrays.asList("Hot Pepper", "Cauliflower", "Parsnip", "Wheat"));
    }

    @Override
    public void interact(Player player) {
        System.out.println(this.getName() + ": Hei, " + player.getName() + "! Siap untuk petualangan hari ini? Aku baru saja makan buah untuk energi!");
        System.out.println(this.getName() + ": Jangan coba-coba bawakan aku sayuran ya, " + player.getName() + "!");
        System.out.println("Ini interact Abigail");
    }
}
