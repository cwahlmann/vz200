package jemu.rest.dto;

import jemu.system.vz.VZTapeDevice;

public class TapeInfo {
    private String name;
    private int position;
    private int positionCount;
    private VZTapeDevice.Mode mode;

    public TapeInfo(String name, int position, int positionCount, VZTapeDevice.Mode mode) {
        this.name = name;
        this.position = position;
        this.positionCount = positionCount;
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

    public VZTapeDevice.Mode getMode() {
        return mode;
    }
}
