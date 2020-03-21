package jemu.system.vz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jemu.core.cpu.Z80;
import jemu.core.device.Device;
import jemu.core.device.DeviceMapping;

public class VzFloppyDevice extends Device {
	private static final Logger log = LoggerFactory.getLogger(VzFloppyDevice.class);
	private static final String DEVICE_ID = "VZ Floppy Device";

	public static final int OUT_PORT_MASK = 0b11110000; // 0xf0
	public static final int OUT_PORT_TEST = 0b00010000; // 0x10-0x1f
	public static final int PORT_LATCH = 0x10;
	public static final int PORT_DATA = 0x11;
	public static final int PORT_POLLING = 0x12;
	public static final int PORT_WRITE_PROTECT_STATUS = 0x13;

	private VZ vz;
	private byte[][][][] sectors;
	private int actualStepperMotorPhase;
	private int drive;
	private boolean[] writeProtected;
	private boolean writeDataFlag;
	private boolean writeRequestFlag;
	private int track;
	private int sector;
	private int cell;

	public VzFloppyDevice(VZ vz) {
		super(DEVICE_ID);
		this.vz = vz;
		this.sectors = new byte[2][40][16][128];
		this.actualStepperMotorPhase = 1;
		this.drive = 0;
		this.writeProtected = new boolean[] { false, false };
		this.writeDataFlag = false;
		this.writeRequestFlag = false;
		this.track = 79;
		this.sector = 0;
		this.cell = 0;
	}

	public void register(Z80 z80) {
		z80.addOutputDeviceMapping(new DeviceMapping(this, OUT_PORT_MASK, OUT_PORT_TEST));
		z80.addInputDeviceMapping(new DeviceMapping(this, OUT_PORT_MASK, OUT_PORT_TEST));
	}

	// -------- manage output --

	// latch (0x10) | d (0x11) | poll (0x12) | ro (0x13)
	// dn wd wr nnnn | read data | poll | read wp
	@Override
	public void writePort(int port, int value) {
		if ((port & 0xff) == PORT_LATCH) {
			int newDrive = 0;
			if ((value & 0x10) == 0) {
				newDrive = 1;
			}
			if ((value & 0x80) == 0) {
				newDrive = 2;
			}
			if (newDrive > 0) {
				this.drive = newDrive-1;
			}
			writeDataFlag = (value & 0x20) != 0;
			writeRequestFlag = (value & 0x40) == 0;
			processStepperMotor(value & 0x0f);
			log(newDrive, writeDataFlag, writeRequestFlag, (value & 0x0f), false, false, false);
			return;
		}
	}

	private void log(int enableDrive, boolean writeData, boolean writeRequest, int stepper, boolean readData, boolean poll,
			boolean readWriteProtected) {
		String result = 
				String.format("%2s %2s %2s %4s | %s | %s        | %s",
						enableDrive > 0 ? "d" + enableDrive : "..",
						writeData ? "wd" : "..",
						writeRequest ? "wr" : "..",
						toBinary(stepper, 4).replaceAll("0", "."),	
						readData ? "read data" : "         ",
						poll ? "poll" : "    ",
						readWriteProtected ? "read writeProtect" : ""
				);
	}

	private void processStepperMotor(int phase) {
		if (phase == 0) {
			return;
		}
		if (phase == 1 || phase == 2 || phase == 4 || phase == 8) {
			int vDown = ((actualStepperMotorPhase & 0x01) * 8) + (actualStepperMotorPhase / 2);
			int vUp = (actualStepperMotorPhase / 8) + ((actualStepperMotorPhase * 2) & 0x0f);
			if (vDown == phase && track > 0) {
				track--;
			} else if (vUp == phase && track < 79) {
				track++;
			}
			this.actualStepperMotorPhase = phase;
		}
	}

	boolean poll = false;
	
	@Override
	public int readPort(int port) {
		int p = port & 0xff;
		switch (p) {
		case PORT_DATA:
			log(0, false,false, 0, true, false, false);
			return (sector * 128 + cell) % 255; // sectors[drive][track/2][sector][cell];
		case PORT_POLLING:
			cell = (cell + 1) % 128;
			if (cell == 0) {
				sector = (sector + 1) % 16;
			}
			log(0, false,false, 0, false, true, false);
			poll = !poll;
			return 0x00;
		case PORT_WRITE_PROTECT_STATUS:
			log(0, false,false, 0, false, false, false);
			return writeProtected[drive] ? 1 : 0;
		default:
			return 0;
		}
	}

	private String toBinary(int i, int n) {
		return String.format("%" + n + "s", Integer.toBinaryString(i)).replaceAll(" ", "0");
	}
}
