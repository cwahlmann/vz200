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
	private int topMemory;

	public VZMemory(boolean with16kExpansion) {
		super("VZ Memory");
		this.topMemory = with16kExpansion ? 0xd000 : 0x9000;
		mem = new byte[this.topMemory];
		for (int i = 0; i < this.topMemory; i += 4) {
			mem[i] = 0;
			mem[i + 1] = 0;
			mem[i + 2] = -1;
			mem[i + 3] = -1;
		}
	}

	public int readByte(int address) {
		if (address < topMemory) {
			return mem[address] & 0xff;			
		}
		return 0;
	}

	public int writeByte(int address, int value) {
		if (address < topMemory) {
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

}