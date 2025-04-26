package com.spakborhills.model.NPC;

import java.lang.reflect.Array;
import java.util.Arrays;

import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Item.Item;

/*
 * Atribut Nilai
name: Mayor Tadi
lovedItems: Legend
likedItems: Angler, Crimsonfish, Glacierfish
hatedItems: Seluruh item yang bukan merupakan lovedItems dan likedItems


 */
public class MayorTadi extends NPC {
    public MayorTadi() {
        super("Mayor Tadi", LocationType.NPC_HOME, false);
        this.lovedItems.add("Legend");
        this.likedItems.addAll(Arrays.asList(("Angler", "Crimsonfish", "Glacierfish")));
        // hated items
        /*
        // Catatan: Hated items Mayor Tadi adalah "Seluruh item yang bukan merupakan
        // lovedItems dan likedItems". Ini mungkin tidak diisi secara eksplisit
        // di list `hatedItems`, tapi logikanya dihandle dalam `checkGiftPreference`
        // atau di-override jika perlu. Untuk NPC lain, isi list hatedItems di sini.
        // this.hatedItems.add("Sampah"); // Contoh jika ada item spesifik yg dibenci
         */
    }

    @Override
    public int checkGiftPreference(Item item){
        if(lovedItems.contains(item.getName())){
            return 25;
        }
        else if(likedItems.contains(item.getName())){
            return 20;
        }
        return -25;
    }

    @Override
    public void interact(Player player) {
        System.out.println(this.getName() + ": Selamat datang di Spakbor Hills! Ada yang bisa saya bantu?");
        System.out.println("Ini interact MayorTadi");
    }
}
