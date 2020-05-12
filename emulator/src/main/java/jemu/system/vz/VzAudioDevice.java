package jemu.system.vz;

import jemu.core.cpu.Z80;
import jemu.core.device.Device;
import jemu.core.device.DeviceMapping;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

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
