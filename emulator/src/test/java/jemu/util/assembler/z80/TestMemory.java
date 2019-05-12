package jemu.util.assembler.z80;

import jemu.core.device.memory.Memory;

public class TestMemory extends Memory {

	private int[] data = new int[2048];
	
	public TestMemory() {
		super("Testmemory");
		clear();
	}

	public final void clear() {
		for (int i=0; i<data.length; i++) {
			data[i] = 0;
		}
	}
	
	@Override
	public int readByte(int address) {
		return readByte(address, null);
	}
	
	@Override
	public int readByte(int address, Object config) {
		return data[wrapAddress(address)];
	}

	@Override
	public int writeByte(int address, int value) {
		writeByte(address, value, null);
		return value;
	}
	
	@Override
	public void writeByte(int address, int value, Object config) {
		data[wrapAddress(address)] = value & 0xff;
	}
	
	@Override
	public int readWord(int address) {
		return readWord(address, null);
	}

	@Override
	public int readWord(int address, Object config) {
		return data[wrapAddress(address)] + (data[wrapAddress(address+1)] << 8) ;
	}

	@Override
	public int writeWord(int address, int value) {
		writeWord(address, value, null);
		return value;
	}
	
	@Override
	public void writeWord(int address, int value, Object config) {
		data[wrapAddress(address)] = value & 0xff;
		data[wrapAddress(address)+1] = (value >> 8) & 0xff;
	}
	
	private int wrapAddress(int a) {
		int w = a % data.length;
		return w;
	}
	
}
