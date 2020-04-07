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

/**
  * This is a generic Input/Output port, as used by the 8255 PPI and then
  * AY-3-891x series PSG.
  */
public class IOPort extends Device {

  public static final int READ  = 0x00;
  public static final int WRITE = 0xff;

  protected Device inputDevice = null;
  protected int inputPort = 0;
  protected Device outputDevice = null;
  protected int outputPort = 0;
  protected int readMask  = 0xff;
  protected int writeMask = 0x00;
  protected int input = 0xff;
  protected int output = 0xff;
  protected boolean latched = false;

  public IOPort() {
    this(READ);
  }

  public IOPort(int mode) {
    super("Input/Output Port");
    setPortMode(mode);
  }

  public void setInputDevice(Device device, int port) {
    inputDevice = device;
    inputPort = port;
  }

  public void setOutputDevice(Device device, int port) {
    outputDevice = device;
    outputPort = port;
  }

  public void setPortMode(int mask) {
    setPortMode(mask ^ 0xff,mask);
  }
  
  public void setPortMode(int readMask, int writeMask) {
    this.writeMask = writeMask;
    this.readMask = readMask;
  }

  public int getPortMode() {
    return writeMask;
  }

  public int readPort(int port) {
    return read();
  }

  public void writePort(int port, int value) {
    write(value);
  }

  public int read() {
    if (!latched && inputDevice != null)
      input = inputDevice.readPort(inputPort);
    return (output & (readMask ^ 0xff)) | (input & readMask);
  }
  
  public void latch() {
    input = inputDevice == null ? 0xff : inputDevice.readPort(inputPort);
  }

  public void write(int value) {
    output = value;
    if (inputDevice == null)
      input = value;
    if (outputDevice != null && writeMask != 0x00)
      outputDevice.writePort(outputPort,(value & writeMask) | (input & (writeMask ^ 0xff)));
  }

  public void setOutput(int value) {
    output = value;
  }

  public int getOutput() {
    return output;
  }

  public int readOutput() {
    return output & writeMask;
  }
  
  public void setInput(int value) {
    input = value;
  }
  
  public int getInput() {
    return input;
  }
  
  public void setLatched(boolean value) {
    latched = value;
  }
  
  public boolean isLatched() {
    return latched;
  }

}