package de.dreierschach.vz200ui.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TapeInfo {
    public enum Mode {
        idle, record, play, off
    }

    private String name;
    private int position;
    private int positionCount;
    private Mode mode;

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setPositionCount(int positionCount) {
        this.positionCount = positionCount;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public int getPositionCount() {
        return positionCount;
    }

}
