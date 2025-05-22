package com.spakborhills.model;

public class NonPlayableCharacter {
    private String name;
    // Future attributes: dialogue, schedule, relationship points, gift preferences

    public NonPlayableCharacter(String name) {
        if (name == null || name.trim().isEmpty()) {
            this.name = "Mysterious Figure";
        } else {
            this.name = name;
        }
    }

    public String getName() {
        return name;
    }

    // Future methods: interact(), receiveGift(), etc.

    @Override
    public String toString() {
        return "NonPlayableCharacter{" +
               "name='" + name + "'" +
               '}';
    }
} 