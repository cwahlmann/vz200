package jemu.core.device.io;

import jemu.core.*;
import jemu.core.device.*;

/**
 * Title:        JEMU
 * Description:  The Java Emulation Platform
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class PPI8255 extends Device {

  public static final int PORT_A       = 0;
  public static final int PORT_B       = 1;
  public static final int PORT_C       = 2;
  public static final int PORT_CONTROL = 3;

  protected int portLowMask  = 0x01;
  protected int portLowTest  = 0x01;
  protected int portHighMask = 0x02;
  protected int portHighTest = 0x02;

  protected IOPort[] ports = new IOPort[] {
    new IOPort(IOPort.READ), new IOPort(IOPort.READ), new IOPort(IOPort.READ),
    new IOPort(IOPort.WRITE)
  };

  public PPI8255() {
    super("8255 PPI");
  }

  public void setPortMasks(int lowMask, int lowTest, int highMask, int highTest) {
    portLowMask = lowMask;
    portLowTest = lowTest;
    portHighMask = highMask;
    portHighTest = highTest;
  }

  public int readPort(int port) {
    int selPort = (port & portHighMask) == portHighTest ? 2 : 0;
    if ((port & portLowMask) == portLowTest)
      selPort++;
    int result = ports[selPort].read();
    return result;
  }

  public void writePort(int port, int value) {
    int selPort = (port & portHighMask) == portHighTest ? 2 : 0;
    if ((port & portLowMask) == portLowTest)
      selPort++;
    if (selPort == PORT_CONTROL) {
      if ((value & 0x80) == 0) {    // Bit Set/Reset
        IOPort ioPort = ports[PORT_C];
        int mask = 1 << ((value >> 1) & 0x07);

        if ((value & 0x01) == 0)    // Reset Bit
          ioPort.write(ioPort.readOutput() & (mask ^ 0xff));
        else                        // Set Bit
          ioPort.write(ioPort.readOutput() | mask);
      }

      else
        setControl(value);
    }
    else
      ports[selPort].write(value);
  }

  public void setReadDevice(int port, Device device, int readPort) {
    ports[port].setInputDevice(device,readPort);
  }

  public void setWriteDevice(int port, Device device, int writePort) {
    ports[port].setOutputDevice(device,writePort);
  }

  protected String readWrite(int port, int mask) {
    return (ports[port].getPortMode() & mask) == IOPort.READ ? "read" : "write";
  }

  public int readOutput(int port) {
    return ports[port].readOutput();
  }

  public String toString() {
    return super.toString() + ": Port A = " + readWrite(PORT_A,0xff) + ", Port B = " +
      readWrite(PORT_B,0xff) + ", Port C (Upper) = " + readWrite(PORT_C,0xf0) +
      ", Port C (Lower) = " + readWrite(PORT_C,0x0f);
  }

  public void setOutputValue(int port, int value) {
    ports[port].setOutput(value);
  }

  public void setControl(int value) {
    ports[PORT_CONTROL].setOutput(value);
    ports[PORT_A].setPortMode((value & 0x10) != 0 ? IOPort.READ : IOPort.WRITE);
    ports[PORT_B].setPortMode((value & 0x02) != 0 ? IOPort.READ : IOPort.WRITE);
    int mode = (value & 0x08) != 0 ? 0 : 0xf0;
    if ((value & 0x01) == 0)
      ports[PORT_C].setPortMode(mode | 0x0f);
    else
      ports[PORT_C].setPortMode(mode);
    ports[PORT_A].write(0);
    ports[PORT_B].write(0);
    ports[PORT_C].write(0);
  }

}