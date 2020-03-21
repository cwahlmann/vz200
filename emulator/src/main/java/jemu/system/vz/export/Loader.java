package jemu.system.vz.export;

import jemu.core.device.memory.Memory;
import jemu.rest.VzSource;

import java.io.IOException;

public abstract class Loader<T extends Loader> {
    public static final int ADR = 0x7ae9;
    public static final int BASIC_START = 0x78a4;
    public static final int BASIC_END = 0x78f9;

    protected Memory memory;
    private String name;
    private int startAddress;
    private int endAddress;
    private boolean autorun;

    public Loader(Memory memory) {
        this.name = "DEFAULT";
        this.memory = memory;
        this.startAddress = -1;
        this.endAddress = -1;
        this.autorun = false;
    }

    public String getName() {
        return name;
    }

    public T withName(String name) {
        this.name = name;
        return (T)this;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public T withStartAddress(int startAddress) {
        this.startAddress = startAddress;
        return (T)this;
    }

    public int getEndAddress() {
        return endAddress;
    }

    public T withEndAddress(int endAddress) {
        this.endAddress = endAddress;
        return (T)this;
    }

    public boolean isAutorun() {
        return autorun;
    }

    public T withAutorun(boolean autorun) {
        this.autorun = autorun;
        return (T)this;
    }

    public abstract void importData(VzSource source);
    public abstract VzSource exportData();
}
