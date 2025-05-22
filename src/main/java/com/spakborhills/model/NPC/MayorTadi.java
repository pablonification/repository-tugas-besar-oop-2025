package com.spakborhills.model.NPC;

// import java.lang.reflect.Array;
import java.util.Arrays;

import com.spakborhills.model.Enum.LocationType;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Player;

/*
 * Atribut Nilai
name: Mayor Tadi
lovedItems: Legend
likedItems: Angler, Crimsonfish, Glacierfish
hatedItems: Seluruh item yang bukan merupakan lovedItems dan likedItems


 */
public class MayorTadi extends NPC {
    public MayorTadi() {
        super("Mayor Tadi", LocationType.MAYOR_TADI_HOME, false);
        this.lovedItems.add("Legend");
        this.likedItems.addAll(Arrays.asList("Angler", "Crimsonfish", "Glacierfish"));
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
        if (item == null || item.getName() == null) return -25; // If item is null, consider it hated.
        if(lovedItems.contains(item.getName())){
            return 25;
        }
        else if(likedItems.contains(item.getName())){
            return 20;
        }
        return -25; // All other items are hated by Mayor Tadi
    }

    @Override
    public String getDialogue(Player player) {
        // Example dialogue, can be made more complex (e.g., based on heart points, time of day)
        if (player != null) {
            return "Selamat datang di Spakbor Hills, " + player.getName() + "! Ada yang bisa saya bantu hari ini?";
        }
        return "Selamat datang di Spakbor Hills! Ada yang bisa saya bantu?";
    }

    @Override
    public String reactToGift(Item item, Player player) {
        if (item == null || item.getName() == null) {
            return "Apa ini? Saya tidak mengerti.";
        }
        int preference = checkGiftPreference(item);
        if (preference == 25) { // Loved
            return "Wow, " + item.getName() + "! Ini luar biasa, terima kasih banyak, " + player.getName() + "!";
        }
        if (preference == 20) { // Liked
            return "Oh, sebuah " + item.getName() + ". Terima kasih, ini sangat berarti.";
        }
        // Hated (since Mayor Tadi hates everything not loved/liked)
        return "Saya tidak yakin apa yang harus saya lakukan dengan " + item.getName() + " ini, tapi... terima kasih?"; 
    }
}
