package jemu.core.device.memory;

import jemu.core.device.Device;

/**
 * Title: JEMU Description: The Java Emulation Platform Copyright: Copyright (c)
 * 2002 Company:
 * 
 * @author
 * @version 1.0
 */

public abstract class Memory extends Device {

	public Memory(String type) {
		super(type);
	}

	public int readByte(int address, Object config) {
		return readByte(address);
	}

	public void writeByte(int address, int value, Object config) {
		writeByte(address, value);
	}

	public int readWord(int address, Object config) {
		return readWord(address);
	}

	public void writeWord(int address, int value, Object config) {
		writeWord(address, value);
	}
}