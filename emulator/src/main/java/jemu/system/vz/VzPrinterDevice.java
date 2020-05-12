package jemu.system.vz;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

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

public class VzPrinterDevice extends Device {
	private static final Logger log = LoggerFactory.getLogger(VzPrinterDevice.class);
	private static final String TYPE = "VZ Printer Device";
	public static final int PORT_MASK_DATA = 0xff;
	public static final int PORT_TEST_DATA = 0x0e;
	public static final int PORT_MASK_BUSY = 0xff;
	public static final int PORT_TEST_BUSY = 0x00;
	public static final int CARRIAGE_RETURN = 0x0d;
	public static final int LINEFEED = 0x0a;

	private Deque<String> printedLines;
	private StringBuilder actualLine;

	public VzPrinterDevice() {
		super(TYPE);
		printedLines = new ConcurrentLinkedDeque<>();
		actualLine = new StringBuilder();
	}

	public void register(Z80 z80) {
		z80.addOutputDeviceMapping(new DeviceMapping(this, PORT_MASK_DATA, PORT_TEST_DATA));
		z80.addInputDeviceMapping(new DeviceMapping(this, PORT_MASK_BUSY, PORT_TEST_BUSY));
	}

	@Override
	public int readPort(int port) {
		return 0; // NOT busy
	}

	@Override
	public void writePort(int port, int value) {
		if (value == LINEFEED) {
			newLine();
		} else if (value == CARRIAGE_RETURN) {
			// ignore CR
		} else {
			actualLine.append((char) value);
		}
	}

	private void newLine() {
		printedLines.addLast(actualLine.toString());
		actualLine = new StringBuilder();
	}

	public String readLine() {
		return printedLines.poll();
	}

	public boolean hasData() {
		return !printedLines.isEmpty();
	}

	public List<String> flush() {
		if (!actualLine.toString().isEmpty()) {
			newLine();
		}
		List<String> result = new ArrayList<>();
		String line = "";
		while (Objects.nonNull(line)) {
			line = readLine();
			if (Objects.nonNull(line)) {
				result.add(line);
			}
		}
		return result;
	}
}
