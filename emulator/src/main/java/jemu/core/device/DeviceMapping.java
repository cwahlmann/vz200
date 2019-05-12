package jemu.core.device;

import jemu.core.*;

/**
 * Title:        JEMU
 * Description:  The Java Emulation Platform
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
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

  public int readPort(int port) {
    return (port & mask) == test ? device.readPort(port) : -1;
  }

  public void writePort(int port, int value) {
    if ((port & mask) == test)
      device.writePort(port,value);
  }
}