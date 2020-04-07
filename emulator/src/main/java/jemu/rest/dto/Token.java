package jemu.rest.dto;

public class Token {
    private String value;

    public String getValue() {
        return value;
    }

    public Token withValue(String value) {
        this.value = value;
        return this;
    }
}
