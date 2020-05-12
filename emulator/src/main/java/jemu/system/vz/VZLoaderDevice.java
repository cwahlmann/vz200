package jemu.system.vz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class VZLoaderDevice extends Device {
	private static final Logger log = LoggerFactory.getLogger(VZLoaderDevice.class);
	private static final String DEVICE_ID = "VZ Loader Device";

	public static enum Mode {
		idle, record, play
	}

	public static final int OUT_PORT_MASK = 0b11111110; // 0xfe
	public static final int OUT_PORT_TEST = 0b11111100; // 0xfc
	public static final int COMMAND_LOAD = 0b11111100;
	public static final int COMMAND_SAVE = 0b11111101;
	public static final int FIRST_WRITABLE_VZ = 100; // 0x60;

	private final VZ vz;
	private final VzDirectory vzDirectory;

	public VZLoaderDevice(VZ vz, VzDirectory vzDirectory) {
		super(DEVICE_ID);
		this.vz = vz;
		this.vzDirectory = vzDirectory;
	}

	public void register(Z80 z80) {
		z80.addOutputDeviceMapping(new DeviceMapping(this, OUT_PORT_MASK, OUT_PORT_TEST));
	}

	// -------- manage output --

	@Override
	public void writePort(int port, int value) {
		int p = port & 0xff;
		if (p == COMMAND_LOAD) {
			try {
				String filename = VzDirectory.getFilename(value);
				log.info("Load program [{}] from [{}]", value, filename);
				vz.importVzFileToMemory(filename);
				vz.alert(String.format("vz-program #%03d loaded", value));
			} catch (Exception e) {
				vz.alert(String.format("error loading vz-program #%03d", value));
				log.error("Unable to load program [{}]", value, e);
			}
			return;
		} else if (p == COMMAND_SAVE) {
			if (value < FIRST_WRITABLE_VZ) {
				vz.alert(String.format("vz-program #%03d is readonly", value));
				log.error("Program slot [{}] is readonly", value);
				return;
			}
			try {
				String filename = VzDirectory.getFilename(value);
				log.info("Save program [{}] to [{}]", value, filename);
				vz.saveVzFile(filename, "", false);
				vz.alert(String.format("vz-program #%03d saved", value));
			} catch (Exception e) {
				vz.alert(String.format("error saving vz-program #%03d", value));
				log.error("Unable to save program [{}]", value, e);
			}
		}

	}
}
