package jemu.system.vz;

import java.net.InetAddress;
import java.net.UnknownHostException;

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

public class VzIpDevice extends Device {
	private static Logger log = LoggerFactory.getLogger(VzIpDevice.class);
	
	private static final String TYPE = "VZ IP Device";
	public static final int PORT_MASK_DATA = 255;
	public static final int PORT_TEST_DATA = 250;

	public VzIpDevice() {
		super(TYPE);
		startIpAddressThread();
	}

	public void register(Z80 z80) {
		z80.addOutputDeviceMapping(new DeviceMapping(this, PORT_MASK_DATA, PORT_TEST_DATA));
		z80.addInputDeviceMapping(new DeviceMapping(this, PORT_MASK_DATA, PORT_TEST_DATA));
	}

	private int[] ipAddress = new int[] { 0, 0, 0, 0 };
	private int index = 0;

	public void startIpAddressThread() {
		new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					byte[] ip = InetAddress.getLocalHost().getAddress();
					for (int i = 0; i < 4; i++) {
						ipAddress[i] = (int) (ip[i] & 0xff);
					}
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					return;
				} catch (UnknownHostException e) {
					log.error("error to determine ip-address of localhost");
				}
			}
		}).start();
	}

	@Override
	public int readPort(int port) {
		int result = ipAddress[index];
		index = (index + 1) & 3;		
		return result;
	}

	@Override
	public void writePort(int port, int value) {
		index = value & 3;		
	}
}
