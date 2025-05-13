package com.spakborhills.model.Object;

import java.awt.Dimension;

/**
 * Kelas abstrak dasar untuk semua objek yang dapat ditempatkan di peta
 * dan memiliki dimensi fisik (lebar dan tinggi).
 * Contoh: Rumah, Kolam, Peti Pengiriman.
 */
public abstract class DeployedObject {
    protected String name;
    protected int width;
    protected int height;

    /**
     * Konstruktor untuk DeployedObject.
     *
     * @param name   Nama objek (misalnya, "Rumah Pemain", "Kolam").
     * @param width  Lebar objek dalam tile.
     * @param height Tinggi objek dalam tile.
     */
    public DeployedObject(String name, int width, int height){
        if(name == null || name.isBlank()){
            throw new IllegalArgumentException("Nama DeployedObject tidak boleh kosong");
        }
        if (width <= 0 || height <= 0){
            throw new IllegalArgumentException("Lebar dan tinggi DeployedObject tidak boleh negatif!");
        }
        this.name = name;
        this.width = width;
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Mengembalikan ukuran objek sebagai objek Dimension.
     * Sesuai diagram kelas.
     * @return Dimension yang berisi lebar dan tinggi objek.
     */
    public Dimension getSize(){
        return new Dimension(this.width, this.height);
    }

    @Override
    public String toString(){
        return name + " (" + width + "x" + height + ")";
    }

}
