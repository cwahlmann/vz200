package de.dreierschach.vz200ui.service;

public class KeyboardInput {
    private String value;

    public String getValue() {
        return value;
    }

    public KeyboardInput withValue(String value) {
        this.value = value;
        return this;
    }
}
