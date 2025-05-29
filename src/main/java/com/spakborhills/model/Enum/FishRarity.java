package com.spakborhills.model.Enum;

public enum FishRarity {
    COMMON(10),
    REGULAR(5),
    LEGENDARY(25);

    private final int priceMultiplier;
    FishRarity(int multiplier) {
        this.priceMultiplier = multiplier;
    }

    public int getPriceMultiplier() {
        return priceMultiplier;
    }
}
