/*
 *   class TileType <<enumeration>> { 
    TILLABLE
    TILLED
    PLANTED
    WATERED
    DEPLOYED_OBJECT 
  }
 */
package com.spakborhills.model.Enum;

public enum TileType {
    GRASS,
    TILLABLE,
    TILLED,
    PLANTED,
    OBSTACLE,
    WATER,
    ENTRY_POINT,
    DEPLOYED_OBJECT,
    WOOD_FLOOR,    // For Mayor Tadi's Home (example)
    STONE_FLOOR,   // For Caroline's Home (example)
    CARPET_FLOOR,  // For Perry's Home (example)
    LUXURY_FLOOR,  // For Dasco's Home (example)
    DIRT_FLOOR,    // For Abigail's Home (example)
    WALL           // Added for house interior walls and other impassable barriers
}
