package jemu.core.device;

/**
 * Title: JEMU Description: The Java Emulation Platform Copyright: Copyright (c)
 * 2002 Company:
 * 
 * @author
 * @version 1.0
 */

public abstract class Device {

	protected String type;

	public Device(String type) {
		this.type = type;
	}

	public int readByte(int address) {
		return -1;
	}

	public int writeByte(int address, int value) {
		return value & 0xff;
	}

	public int readWord(int address) {
		return -1;
	}

	public int writeWord(int address, int value) {
		return value & 0xffff;
	}

	public int readPort(int port) {
		return -1;
	}

	public void writePort(int port, int value) {
	}

	public void cycle() {
	}

	public void reset() {
	}

	public void setInterrupt(int mask) {
	}

	public void clearInterrupt(int mask) {
	}

	public String toString() {
		return type;
	}

}