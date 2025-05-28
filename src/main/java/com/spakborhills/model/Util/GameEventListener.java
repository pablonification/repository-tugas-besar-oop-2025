package com.spakborhills.model.Util;

/**
 * Interface for observers in the Observer pattern.
 * Classes that want to receive game events must implement this interface.
 */
public interface GameEventListener {
    
    /**
     * Called when a game event occurs
     * 
     * @param event The event that occurred
     */
    void onGameEvent(GameEvent event);
} 