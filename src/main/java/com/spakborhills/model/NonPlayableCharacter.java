package com.spakborhills.model;

public class NonPlayableCharacter {
    private String name;
    
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

    @Override
    public String toString() {
        return "NonPlayableCharacter{" +
               "name='" + name + "'" +
               '}';
    }
} 