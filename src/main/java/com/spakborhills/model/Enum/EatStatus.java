package com.spakborhills.model.Enum;

public enum EatStatus {
    SUCCESS,        // Successfully ate the item
    NOT_EDIBLE,     // Item is not an instance of EdibleItem
    ENERGY_FULL,    // Player's energy is already max or would exceed max without benefit
    CANNOT_EAT_NOW, // General failure (e.g., player state prevents eating)
    ITEM_NOT_FOUND  // Selected item was null or not in inventory (should ideally be caught earlier)
} 