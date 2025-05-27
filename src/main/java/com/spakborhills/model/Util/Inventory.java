/*
 *   class Inventory {
    - items: Map<Item, Integer>
    + addItem(item: Item, qty: int): void
    + removeItem(item: Item, qty: int): boolean
    + getItemCount(item: Item): int
    + hasItem(item: Item, qty: int): boolean
    + getItems(): Map<Item, Integer>
  }
 */

package com.spakborhills.model.Util;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import com.spakborhills.model.Item.Item;
import com.spakborhills.model.Item.Equipment;


/**
 * Merepresentasikan inventory pemain, menyimpan koleksi item dan jumlahnya.
 * Tidak ada batasan kapasitas item unik atau jumlah total.
 * Berdasarkan spesifikasi Halaman 23 dan kebutuhan dari kelas Player.
 */
public class Inventory {
  private final Map<Item, Integer> items;

/**
     * Konstruktor untuk Inventory.
     * Membuat inventory kosong saat pertama kali diinisialisasi.
     */
    public Inventory(){
      this.items = new HashMap<>();
    }

/**
     * Menambahkan sejumlah item tertentu ke dalam inventory.
     * Jika item sudah ada, kuantitasnya akan ditambahkan.
     * Jika belum ada, item baru akan ditambahkan.
     *
     * @param item     Objek Item yang akan ditambahkan.
     * @param quantity Jumlah item yang akan ditambahkan (harus > 0).
     */
    public void addItem(Item item, int quantity){
      if(item == null || quantity <= 0){
        return;
      }
      long currentQuantity = items.getOrDefault(item, 0); // Get as int, then cast for sum
      long newQuantityLong = currentQuantity + quantity;
      
      int finalQuantity;
      if (newQuantityLong > Integer.MAX_VALUE) {
        finalQuantity = Integer.MAX_VALUE;
      } else if (newQuantityLong < 0 && quantity > 0 && currentQuantity >=0 ) { // Check for overflow to negative specifically
        finalQuantity = Integer.MAX_VALUE; 
      } else if (newQuantityLong < 0) { // If it's still negative (e.g. current was already negative due to bad data, or quantity was huge negative)
        finalQuantity = 0; // Cap at 0 if it becomes negative by other means (should ideally not happen)
      }else {
        finalQuantity = (int)newQuantityLong;
      }
      this.items.put(item, finalQuantity);
    }

/**
     * Menghapus sejumlah item tertentu dari inventory.
     *
     * @param item     Objek Item yang akan dihapus.
     * @param quantity Jumlah item yang akan dihapus (harus > 0).
     * @return true jika item berhasil dihapus (ada cukup kuantitas), false jika gagal.
     */
    public boolean removeItem(Item item, int quantity){
      if(item == null || quantity <= 0){
        // throw new IllegalArgumentException("Item atau kuantitas tidak valid");
        return false;
      }
      
      int currentQuantity = items.getOrDefault(item, 0);

      // Periksa apakah ada cukup kuantitas untuk dihapus
      if(currentQuantity < quantity){
        return false;
      } else if(currentQuantity == quantity){
        items.remove(item);
      } else {
        items.put(item, currentQuantity - quantity);
      }
      return true;
    }

/**
     * Memeriksa apakah inventory memiliki setidaknya sejumlah item tertentu.
     *
     * @param item     Objek Item yang diperiksa.
     * @param quantity Jumlah minimum yang harus dimiliki (harus > 0).
     * @return true jika item ada dengan kuantitas yang cukup, false jika tidak.
     */
    public boolean hasItem(Item item, int quantity){
      if(item == null || quantity <= 0){
        // throw new IllegalArgumentException("Item atau kuantitas tidak valid");
        return false;
      } 
      return this.items.getOrDefault(item, 0) >= quantity;
    }

/**
     * Mendapatkan jumlah (kuantitas) dari item tertentu dalam inventory.
     *
     * @param item Objek Item yang ingin diketahui jumlahnya.
     * @return Jumlah item tersebut, atau 0 jika item tidak ada atau null.
     */
    public int getItemCount(Item item){
      if(item == null){
        // throw new IllegalArgumentException("Item tidak valid");
        return 0;
      }
      return this.items.getOrDefault(item, 0);
    }

/**
     * Memeriksa apakah inventory memiliki setidaknya satu buah peralatan (Equipment)
     * dengan tipe tertentu (misalnya, "Hoe", "Pickaxe").
     *
     * @param toolType String yang merepresentasikan tipe alat (tidak case-sensitive).
     * @return true jika alat dengan tipe tersebut ditemukan, false jika tidak.
     */
    public boolean hasTool(String toolType){
      if(toolType == null){
        // throw new IllegalArgumentException("Tipe alat tidak valid");
        return false;
      }
      for (Item item : this.items.keySet()){
        if(item instanceof Equipment && ((Equipment) item).getToolType().equalsIgnoreCase(toolType)){
          if(this.items.get(item) > 0){
            return true;
          }
        }
      }
      return false;
    }

/**
     * Mendapatkan representasi Map dari seluruh item dalam inventory.
     * Mengembalikan view yang tidak bisa dimodifikasi untuk melindungi state internal.
     *
     * @return Map<Item, Integer> yang unmodifiable berisi item dan kuantitasnya.
     */
    public Map<Item, Integer> getItems(){
      return Collections.unmodifiableMap(this.items);
    }

    public void clear() { // Added for loading save data
        this.items.clear();
    }

// Anda bisa menambahkan metode lain jika perlu, misalnya:
    // - getTotalItemCount(): Mengembalikan jumlah total semua item.
    // - isEmpty(): Memeriksa apakah inventory kosong.
    // - toString(): Untuk representasi string inventory (berguna untuk debugging/CLI).
    @Override
    public String toString(){
      if(items.isEmpty()){
        return "Inventory kosong";
      }
      StringBuilder sb = new StringBuilder("Isi Inventory:\n");
      for(Map.Entry<Item, Integer> entry : items.entrySet()){
        sb.append("- ").append(entry.getKey().getName())
        .append(" x ").append(entry.getValue()).append("\n");
      }
      return sb.toString();
    }

}