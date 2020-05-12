package jemu.core.device;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class DeviceMapping {

  protected Device device;
  protected int mask;
  protected int test;

  public DeviceMapping(Device device, int mask, int test) {
    this.device = device;
    this.mask = mask;
    this.test = test;
  }

  public void reset() {
	  this.device.reset();
  }
  
  public int readPort(int port) {
    return (port & mask) == test ? device.readPort(port) : -1;
  }

  public void writePort(int port, int value) {
    if ((port & mask) == test)
      device.writePort(port,value);
  }
}