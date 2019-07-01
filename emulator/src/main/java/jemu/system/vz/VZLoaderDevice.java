package jemu.system.vz;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jemu.core.cpu.Z80;
import jemu.core.device.Device;
import jemu.core.device.DeviceMapping;

public class VZLoaderDevice extends Device {
	private static final Logger log = LoggerFactory.getLogger(VZLoaderDevice.class);
	private static final String DEVICE_ID = "VZ Loader Device";

	public static enum Mode {
		idle, record, play
	}

	public static final int OUT_PORT_MASK = 0b11111110; // 0xfe
	public static final int OUT_PORT_TEST = 0b11111100; // 0xfa
	public static final int COMMAND_LOAD = 0b11111100;
	public static final int COMMAND_SAVE = 0b11111101;
	public static final int FIRST_WRITABLE_VZ = 100; // 0x60;

	private VZ vz;

	public VZLoaderDevice(VZ vz) {
		super(DEVICE_ID);
		this.vz = vz;
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
				String filename = getFilename(value);
				log.info("Load program [{}] from [{}]", value, filename);
				vz.loadBinaryFile(filename);
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
				String filename = getFilename(value);
				log.info("Save program [{}] to [{}]", value, filename);
				vz.saveFile(filename);
				vz.alert(String.format("vz-program #%03d saved", value));
			} catch (Exception e) {
				vz.alert(String.format("error saving vz-program #%03d", value));
				log.error("Unable to save program [{}]", value, e);
			}
		}

	}

	private String getFilename(int value) throws IOException {
		String dir = System.getProperty("user.home") + "/vz200/vz";
		Files.createDirectories(Paths.get(dir));
		return dir + "/" + String.format("vzfile_%03d.vz", value);
	}
}
