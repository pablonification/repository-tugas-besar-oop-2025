package com.spakborhills.model.Util;

/**
 * Enum representing the different types of game events that can occur.
 * Used as part of the Observer pattern implementation.
 */
public enum GameEventType {
    // Player-related events
    PLAYER_MOVED,
    PLAYER_ENERGY_CHANGED,
    PLAYER_GOLD_CHANGED,
    PLAYER_ITEM_USED,
    PLAYER_ITEM_ADDED,
    PLAYER_ITEM_REMOVED,
    
    // Farm-related events
    CROP_PLANTED,
    CROP_WATERED,
    CROP_HARVESTED,
    TILE_TILLED,
    TILE_RECOVERED,
    
    // Time-related events
    DAY_ENDED,
    DAY_STARTED,
    SEASON_CHANGED,
    WEATHER_CHANGED,
    
    // NPC-related events
    NPC_CHAT,
    NPC_GIFT,
    NPC_RELATIONSHIP_CHANGED,
    PLAYER_MARRIED,
    
    // Economy-related events
    ITEM_PURCHASED,
    ITEM_SOLD,
    SHIPPING_BIN_ITEM_ADDED,
    SHIPPING_BIN_PROCESSED,
    
    // Game state events
    GAME_SAVED,
    GAME_LOADED,
    GAME_ENDED,
    MILESTONE_ACHIEVED
} 