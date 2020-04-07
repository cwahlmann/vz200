package jemu.rest.dto;

public class KeyboardInput {
    private String value;

    public KeyboardInput() {
        this.value = "";
    }

    public static KeyboardInput of(String value) {
        return new KeyboardInput().withValue(value);
    }

    public String getValue() {
        return value;
    }

    public KeyboardInput withValue(String input) {
        this.value = value;
        return this;
    }
}
