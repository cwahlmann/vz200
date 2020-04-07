package jemu.core.device;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
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