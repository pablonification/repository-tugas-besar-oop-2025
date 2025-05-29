package com.spakborhills.model.Object;

import com.spakborhills.model.Item.Furniture;
import java.util.ArrayList;
import java.util.List;

public class House extends DeployedObject {
    public static final int DEFAULT_HOUSE_WIDTH = 6;
    public static final int DEFAULT_HOUSE_HEIGHT = 6;
    private List<Furniture> furnitures;

    public House(){
        super("Rumah", DEFAULT_HOUSE_WIDTH, DEFAULT_HOUSE_HEIGHT);
        this.furnitures = new ArrayList<>();
    }

    public House(String name){
        super(name, DEFAULT_HOUSE_WIDTH, DEFAULT_HOUSE_HEIGHT);
        this.furnitures = new ArrayList<>();
    }

    public List<Furniture> getFurnitures() {
        return furnitures;
    }

    public void addFurniture(Furniture furniture) {
        if (this.furnitures == null) {
            this.furnitures = new ArrayList<>();
        }
        this.furnitures.add(furniture);
    }

    public void clearFurniture() {
        if (this.furnitures != null) {
            this.furnitures.clear();
        }
    }
}
