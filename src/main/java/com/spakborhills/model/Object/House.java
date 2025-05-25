package com.spakborhills.model.Object;

public class House extends DeployedObject {
    public static final int DEFAULT_HOUSE_WIDTH = 6;
    public static final int DEFAULT_HOUSE_HEIGHT = 6;

    public House(){
        super("Rumah", DEFAULT_HOUSE_WIDTH, DEFAULT_HOUSE_HEIGHT);
    }

    public House(String name){
        super(name, DEFAULT_HOUSE_WIDTH, DEFAULT_HOUSE_HEIGHT);
    }

    // .... implement it later
}
