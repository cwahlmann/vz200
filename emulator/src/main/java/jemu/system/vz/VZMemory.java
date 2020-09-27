package jemu.system.vz;

import jemu.core.cpu.Z80;
import jemu.core.device.DeviceMapping;
import jemu.core.device.memory.Memory;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class VZMemory extends Memory {

    private byte[] mem;
    private byte[][] gfxShadowMem; // 7000-7800 * 6 = 256*192, 4 colors
    private int shadowReadBank;
    private int shadowWriteBank;
    private int topMemory;

    public VZMemory(boolean with16kExpansion) {
        super("VZ Memory");
        this.topMemory = with16kExpansion ? 0xd000 : 0x9000;
        mem = new byte[this.topMemory];
        gfxShadowMem = new byte[8][0x800];
        shadowReadBank = 0;
        shadowWriteBank = 0;
        for (int i = 0; i < this.topMemory; i += 4) {
            mem[i] = 0;
            mem[i + 1] = 0;
            mem[i + 2] = -1;
            mem[i + 3] = -1;
        }
        for (int j=0; j<7; j++) {
            for (int i = 0; i < 0x800; i += 4) {
                gfxShadowMem[j][i] = 0;
                gfxShadowMem[j][i + 1] = 0;
                gfxShadowMem[j][i + 2] = -1;
                gfxShadowMem[j][i + 3] = -1;
            }
        }
    }

    public void register(Z80 z80) {
        z80.addOutputDeviceMapping(new DeviceMapping(this, 0xf0, 0x20));
    }

    @Override
    public void writePort(int port, int value) {
        withShadowReadBank(value & 0x03);
        withShadowWriteBank(value & 0x03);
    }

    public int getShadowReadBank() {
        return shadowReadBank;
    }

    public VZMemory withShadowReadBank(int shadowReadBank) {
        this.shadowReadBank = shadowReadBank;
        return this;
    }

    public int getShadowWriteBank() {
        return shadowWriteBank;
    }

    public VZMemory withShadowWriteBank(int shadowWriteBank) {
        this.shadowWriteBank = shadowWriteBank;
        return this;
    }

    public int readByte(int address) {
        if (address >= 0x7000 && address < 0x7800) {
            return gfxShadowMem[shadowReadBank & 0x07][address - 0x7000] & 0xff;
        }
        if (address < topMemory) {
            return mem[address] & 0xff;
        }
        return 0;
    }

    public int writeByte(int address, int value) {
        if (address >= 0x7000 && address < 0x7800) {
            gfxShadowMem[shadowWriteBank & 0x07][address - 0x7000] = (byte) value;
        } else if (address < topMemory) {
            mem[address] = (byte) value;
        }
        return value & 0xff;
    }

    public int readWord(int address) {
        int l = readByte(address) & 0xff;
        int h = readByte(address + 1) & 0xff;
        return l | (h << 8);
    }

    public int writeWord(int address, int value) {
        int l = value & 0xff;
        int h = value >> 8;
        writeByte(address, l);
        writeByte(address + 1, h);
        return value & 0xffff;
    }

    public void setMemory(int address, byte[] data) {
        System.arraycopy(data, 0, mem, address, data.length);
    }

    public byte[] getMemory() {
        return mem;
    }

    public byte[][] getGfxMemory() {
        return gfxShadowMem;
    }

}