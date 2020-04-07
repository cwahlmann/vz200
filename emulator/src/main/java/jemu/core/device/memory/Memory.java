package jemu.core.device.memory;

import jemu.core.device.Device;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
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