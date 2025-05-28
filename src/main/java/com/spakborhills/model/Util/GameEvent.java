package com.spakborhills.model.Util;

/**
 * GameEvent class represents various events that can occur in the game.
 * Used as part of the Observer pattern implementation.
 */
public class GameEvent {
    
    /**
     * The type of event that occurred
     */
    private GameEventType eventType;
    
    /**
     * Optional data associated with the event
     */
    private Object data;
    
    /**
     * The source object that generated the event
     */
    private Object source;
    
    /**
     * Creates a new GameEvent with the specified type, source, and data
     * 
     * @param eventType The type of event
     * @param source The object that generated the event
     * @param data Optional data associated with the event
     */
    public GameEvent(GameEventType eventType, Object source, Object data) {
        this.eventType = eventType;
        this.source = source;
        this.data = data;
    }
    
    /**
     * Creates a new GameEvent with the specified type and source
     * 
     * @param eventType The type of event
     * @param source The object that generated the event
     */
    public GameEvent(GameEventType eventType, Object source) {
        this(eventType, source, null);
    }
    
    /**
     * Gets the type of this event
     * 
     * @return The event type
     */
    public GameEventType getEventType() {
        return eventType;
    }
    
    /**
     * Gets the source object that generated this event
     * 
     * @return The source object
     */
    public Object getSource() {
        return source;
    }
    
    /**
     * Gets the data associated with this event
     * 
     * @return The event data, or null if no data was provided
     */
    public Object getData() {
        return data;
    }
    
    /**
     * Returns a string representation of this event
     */
    @Override
    public String toString() {
        return "GameEvent[type=" + eventType + ", source=" + source + ", data=" + data + "]";
    }
} 