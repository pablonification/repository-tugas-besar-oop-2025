package com.spakborhills.model.Object;

/**
 * Merepresentasikan Kolam (Pond) di peta.
 * Ukuran default 4x3 sesuai spesifikasi Halaman 21.
 * Tempat pemain bisa memancing.
 */
public class Pond extends DeployedObject {

    private static final int DEFAULT_POND_WIDTH = 4;  
    private static final int DEFAULT_POND_HEIGHT = 3; 

    public Pond() {
        super("Kolam", DEFAULT_POND_WIDTH, DEFAULT_POND_HEIGHT);
    }
}
