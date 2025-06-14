package com.spakborhills.model.Object;

/**
 * Merepresentasikan objek fisik Shipping Bin di peta.
 * Ukuran default 3x2 sesuai spesifikasi Halaman 21.
 * Pemain berinteraksi dengan ini untuk menjual item.
 */
public class ShippingBinObject extends DeployedObject {

    private static final int DEFAULT_BIN_WIDTH = 3;  // Lebar
    private static final int DEFAULT_BIN_HEIGHT = 2; // Tinggi

    public ShippingBinObject() {
        super("Shipping Bin", DEFAULT_BIN_WIDTH, DEFAULT_BIN_HEIGHT);
    }
}
