package de.dreierschach.vz200ui.util;

import org.apache.commons.lang3.StringUtils;

public class StringObject {
    private String value;

    public StringObject() {
        this.value = "";
    }

    public StringObject(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void append(String value) {
        this.value += value;
    }

    public void clear() {
        this.value = "";
    }

    public boolean isEmpty() {
        return StringUtils.isEmpty(this.value);
    }
}
