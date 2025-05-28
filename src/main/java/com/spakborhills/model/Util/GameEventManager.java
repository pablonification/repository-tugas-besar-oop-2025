package com.spakborhills.model.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GameEventManager implements the Observer pattern for game events.
 * It allows objects to register as listeners for specific event types
 * and dispatches events to the appropriate listeners.
 */
public class GameEventManager {
    
    // Singleton instance
    private static GameEventManager instance;
    
    // Map of event types to lists of listeners
    private Map<GameEventType, List<GameEventListener>> listeners;
    
    /**
     * Private constructor for singleton pattern
     */
    private GameEventManager() {
        listeners = new HashMap<>();
        // Initialize lists for all event types
        for (GameEventType eventType : GameEventType.values()) {
            listeners.put(eventType, new ArrayList<>());
        }
    }
    
    /**
     * Gets the singleton instance of the GameEventManager
     * 
     * @return The GameEventManager instance
     */
    public static synchronized GameEventManager getInstance() {
        if (instance == null) {
            instance = new GameEventManager();
        }
        return instance;
    }
    
    /**
     * Adds a listener for a specific event type
     * 
     * @param eventType The event type to listen for
     * @param listener The listener to add
     */
    public void addEventListener(GameEventType eventType, GameEventListener listener) {
        List<GameEventListener> eventListeners = listeners.get(eventType);
        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener);
        }
    }
    
    /**
     * Adds a listener for multiple event types
     * 
     * @param eventTypes The event types to listen for
     * @param listener The listener to add
     */
    public void addEventListener(GameEventType[] eventTypes, GameEventListener listener) {
        for (GameEventType eventType : eventTypes) {
            addEventListener(eventType, listener);
        }
    }
    
    /**
     * Removes a listener for a specific event type
     * 
     * @param eventType The event type
     * @param listener The listener to remove
     */
    public void removeEventListener(GameEventType eventType, GameEventListener listener) {
        List<GameEventListener> eventListeners = listeners.get(eventType);
        eventListeners.remove(listener);
    }
    
    /**
     * Removes a listener from all event types
     * 
     * @param listener The listener to remove
     */
    public void removeEventListener(GameEventListener listener) {
        for (GameEventType eventType : GameEventType.values()) {
            removeEventListener(eventType, listener);
        }
    }
    
    /**
     * Fires an event to all registered listeners for that event type
     * 
     * @param event The event to fire
     */
    public void fireEvent(GameEvent event) {
        List<GameEventListener> eventListeners = listeners.get(event.getEventType());
        for (GameEventListener listener : eventListeners) {
            listener.onGameEvent(event);
        }
    }
    
    /**
     * Convenience method to fire an event with a type, source, and data
     * 
     * @param eventType The type of event
     * @param source The source of the event
     * @param data The event data
     */
    public void fireEvent(GameEventType eventType, Object source, Object data) {
        fireEvent(new GameEvent(eventType, source, data));
    }
    
    /**
     * Convenience method to fire an event with a type and source
     * 
     * @param eventType The type of event
     * @param source The source of the event
     */
    public void fireEvent(GameEventType eventType, Object source) {
        fireEvent(new GameEvent(eventType, source));
    }
} 