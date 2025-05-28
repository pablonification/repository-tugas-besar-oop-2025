package com.spakborhills.model.Util;

/**
 * Example implementation of GameEventListener that logs events to the console.
 * This demonstrates how to use the Observer pattern.
 */
public class GameEventLogger implements GameEventListener {
    
    private boolean logToConsole;
    
    /**
     * Creates a new GameEventLogger
     * 
     * @param logToConsole Whether to log events to the console
     */
    public GameEventLogger(boolean logToConsole) {
        this.logToConsole = logToConsole;
        
        // Register for all event types
        GameEventManager eventManager = GameEventManager.getInstance();
        for (GameEventType eventType : GameEventType.values()) {
            eventManager.addEventListener(eventType, this);
        }
    }
    
    /**
     * Sets whether to log events to the console
     * 
     * @param logToConsole true to log events, false otherwise
     */
    public void setLogToConsole(boolean logToConsole) {
        this.logToConsole = logToConsole;
    }
    
    /**
     * Handles game events by logging them to the console
     */
    @Override
    public void onGameEvent(GameEvent event) {
        if (logToConsole) {
            System.out.println("[EVENT] " + event.toString());
        }
    }
    
    /**
     * Unregisters this logger from all events
     */
    public void unregister() {
        GameEventManager.getInstance().removeEventListener(this);
    }
} 