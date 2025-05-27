package com.spakborhills.model.Item;

public class WateringCan extends Equipment {

    public WateringCan() {
        super("Watering Can", "WateringCan"); // name and toolType (Removed extra space from toolType)
        // Deskripsi bisa ditambahkan jika diperlukan, atau di-handle oleh UI berdasarkan nama/tipe
    }

    // Implementasi spesifik untuk WateringCan bisa ditambahkan di sini jika ada.
    // Metode use() diwarisi dari Equipment. Perilaku spesifik saat use()
    // kemungkinan akan di-handle di GameController berdasarkan getToolType().
} 