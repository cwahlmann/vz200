package jemu.system.vz;

import jemu.core.device.memory.Memory;

/**
 * Title: JEMU Description: The Java Emulation Platform Copyright: Copyright (c)
 * 2002 Company:
 * 
 * @author
 * @version 1.0
 */

public class VZMemory extends Memory {

	protected VZ vz;
	protected byte[] mem = new byte[65536];

	public VZMemory(VZ vz) {
		super("VZ Memory");
		this.vz = vz;
	}

	public int readByte(int address) {
		return mem[address] & 0xff;
	}

	public int writeByte(int address, int value) {
		mem[address] = (byte) value;
		return value & 0xff;
	}

	public int readWord(int address) {
		int l = mem[address] & 0xff;
		int h = mem[address + 1] & 0xff;
		return l | (h << 8);
	}

	public int writeWord(int address, int value) {
		int l = value & 0xff;
		int h = value >> 8;
		mem[address] = (byte) l;
		mem[address + 1] = (byte) h;
		return value & 0xffff;
	}

	public void setMemory(int address, byte[] data) {
		System.arraycopy(data, 0, mem, address, data.length);
	}

	public byte[] getMemory() {
		return mem;
	}

}