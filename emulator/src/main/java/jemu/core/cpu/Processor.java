package jemu.core.cpu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jemu.core.Util;
import jemu.core.device.Device;
import jemu.core.device.DeviceMapping;

/**
 * Title: JEMU Description: The Java Emulation Platform Copyright: Copyright (c)
 * 2002 Company:
 * 
 * @author
 * @version 1.0
 */

public abstract class Processor extends Device {
	private final static Logger log = LoggerFactory.getLogger(Processor.class);
	
	// Memory for processor
	protected Device memory;

	// Input Devices
	protected DeviceMapping[] inputDevice = new DeviceMapping[0];

	// Output Devices
	protected DeviceMapping[] outputDevice = new DeviceMapping[0];

	// Cycle devices
	protected Device cycleDevice = null;

	// Interrupt device
	protected Device interruptDevice = null;

	// Interrupt mask
	protected int interruptPending = 0;

	// Total number of cycles executed
	protected long cycles = 0;

	// Cycles per second of CPU
	protected long cyclesPerSecond;

	// Processor stopped
	protected boolean stopped = false;

	protected boolean paused = false;

	public Processor(String type, long cyclesPerSecond) {
		super(type);
		this.cyclesPerSecond = cyclesPerSecond;
	}

	public void cycle(int count) {
		if (cycleDevice == null)
			cycles += count;
		else
			for (; count > 0; count--) {
				cycles++;
				cycleDevice.cycle();
			}
	}

	public void reset() {
		for (DeviceMapping mapping : outputDevice) {
			mapping.reset();
		}
		interruptPending = 0;
	}

	public long getCycles() {
		return cycles;
	}

	public synchronized void pause() {
		log.info("pause processor...");
		this.paused = true;
	}

	public synchronized void resume() {
		log.info("resume processor...");
		this.paused = false;
	}

	public abstract void step();

	public abstract void stepOver();

	public void run() {
		stopped = false;
		do {
			if (!paused) {
				step();
			} else {
				try {
				Thread.sleep(200);
				} catch (InterruptedException e) {
					log.warn("Stopping processer because of Thread interrupt!");
					stopped = true;
				}
			}
		} while (!stopped);
	}

	public void runTo(int address) {
		stopped = false;
		do {
			step();
		} while (!stopped && getProgramCounter() != address);
	}

	public synchronized void stop() {
		stopped = true;
	}

	public final int readWord(int addr) {
		return readByte(addr) + (readByte((addr + 1) & 0xffff) << 8);
	}

	public final int writeWord(int addr, int value) {
		writeByte(addr, value);
		writeByte((addr + 1) & 0xffff, value >> 8);
		return addr & 0xffff;
	}

	public final int readByte(int address) {
		return memory.readByte(address);
	}

	public final int writeByte(int address, int value) {
		return memory.writeByte(address, value);
	}

	public final int in(int port) {
		int result = 0xff;
		for (int i = 0; i < inputDevice.length; i++)
			result &= inputDevice[i].readPort(port);
		return result;
	}

	public final void out(int port, int value) {
		for (int i = 0; i < outputDevice.length; i++)
			outputDevice[i].writePort(port, value);
	}

	public final void setMemoryDevice(Device value) {
		memory = value;
	}

	public final Device getMemoryDevice() {
		return memory;
	}

	public final void addInputDeviceMapping(DeviceMapping value) {
		inputDevice = (DeviceMapping[]) Util.arrayInsert(inputDevice, inputDevice.length, 1, value);
	}

	public final void removeInputDeviceMapping(DeviceMapping value) {
		inputDevice = (DeviceMapping[]) Util.arrayDeleteElement(inputDevice, value);
	}

	public final void addOutputDeviceMapping(DeviceMapping value) {
		outputDevice = (DeviceMapping[]) Util.arrayInsert(outputDevice, outputDevice.length, 1, value);
	}

	public final void removeOutputDeviceMapping(DeviceMapping value) {
		outputDevice = (DeviceMapping[]) Util.arrayDeleteElement(outputDevice, value);
	}

	public final void setCycleDevice(Device value) {
		cycleDevice = value;
	}

	public final void setInterruptDevice(Device value) {
		interruptDevice = value;
	}

	public void setInterrupt(int mask) {
		interruptPending |= mask;
	}

	public void clearInterrupt(int mask) {
		interruptPending &= ~mask;
	}

	public abstract String getState();

	public abstract String[] getRegisterNames();

	public abstract int getRegisterBits(int index);

	public abstract int getRegisterValue(int index);

	public abstract int getProgramCounter();

	/**
	 * Used to return a label for flags
	 */
	public String getRegisterFormat(int index) {
		return null;
	}

	public long getCyclesPerSecond() {
		return cyclesPerSecond;
	}

	public void setCyclesPerSecond(long value) {
		cyclesPerSecond = value;
	}

}