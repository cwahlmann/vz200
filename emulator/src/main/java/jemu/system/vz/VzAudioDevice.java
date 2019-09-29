package jemu.system.vz;

import jemu.core.cpu.Z80;
import jemu.core.device.Device;
import jemu.core.device.DeviceMapping;

public class VzAudioDevice extends Device {
	private static final String TYPE = "VZ Audio Device";
	public static final int PORT_MASK_DATA = 0xff;
	public static final int PORT_TEST_DATA = 0xfb;

	private VZ vz;

	public VzAudioDevice(VZ vz) {
		super(TYPE);
		this.vz = vz;
	}

	public void register(Z80 z80) {
		z80.addOutputDeviceMapping(new DeviceMapping(this, PORT_MASK_DATA, PORT_TEST_DATA));
		z80.addInputDeviceMapping(new DeviceMapping(this, PORT_MASK_DATA, PORT_TEST_DATA));
	}

	@Override
	public int readPort(int port) {
		return vz.getVolume();
	}

	@Override
	public void writePort(int port, int value) {
		vz.setVolume(value);
	}
}
