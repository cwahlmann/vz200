/*
 * I8271.java
 *
 * Created on 28 July 2006, 13:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.core.device.floppy;

import jemu.core.*;
import jemu.core.cpu.*;
import jemu.core.device.*;

/**
 *
 * @author Richard
 */
public class I8271 extends Device {
  
  public static final int NON_DMA_DRQ  = 0x04;
  public static final int IRQ          = 0x08;
  public static final int RESULT_FULL  = 0x10;
  public static final int PARAM_FULL   = 0x20;
  public static final int COMMAND_FULL = 0x40;
  public static final int COMMAND_BUSY = 0x80;
  
  protected static final int RESULT_MASK  = ~(IRQ | RESULT_FULL);
  protected static final int DATA_MASK    = ~(IRQ | NON_DMA_DRQ);
  protected static final int COMMAND_MASK = COMMAND_BUSY | COMMAND_FULL;
  
  protected static final int READ   = 2;
  protected static final int WRITE  = 1;
  protected static final int RESULT = 0;
  
  protected int status;
  protected int param;
  protected int result;
  protected int data;
  protected int reset;
  protected int mode = 0;
  
  protected int command;       // Current command
  protected int action;        // Current action
  protected int[] params = new int[7];
  protected int pcount = 0;
  protected int pindex = 0;
  protected boolean select0;
  protected boolean select1;
  protected Drive drive;
  protected int c, h, r, n;    // Sector ID
  protected int sectors;       // Number of sectors
  protected int offset;        // Offset in sector
  protected int size;          // Size of sector
  protected byte[] buffer;     // Sector Buffer
  protected int count;         // Cycle count for command processing
 
  protected Drive[] drives = new Drive[2];
  
  protected Device interruptDevice;
  protected int interruptMask;
  
  /** Creates a new instance of I8271 */
  public I8271() {
    super("Intel 8271 Floppy Controller");
  }
  
  public void setInterruptDevice(Device device, int mask) {
    interruptDevice = device;
    interruptMask = mask;
  }
  
  public void setDrive(int index, Drive drive) {
    drives[index] = drive;
  }
  
  public Drive getDrive(int index) {
    return drives[index];
  }
  
  protected final void setStatus(int value) {
    status = value;
    if (interruptDevice != null) {
      if ((status & IRQ) == 0)
        interruptDevice.clearInterrupt(interruptMask);
      else
        interruptDevice.setInterrupt(interruptMask);
    }
  }
  
  protected final void setResult(int value, int irq) {
    result = value;
    setStatus((status | RESULT_FULL | irq) & ~COMMAND_MASK);
  }
  
  public final int readPort(int port) {
    // CS on bit 2
    if ((port & 0x04) == 0) {
      switch(port) {
        case 0:  return status;
        case 1:  setStatus(status & RESULT_MASK); int res = result; result = 0; return res;
        default: return 0xff;
      }
    }
    else {
      setStatus(status & DATA_MASK);
      return data;
    }
  }
  
  public final void writePort(int port, int value) {
    if ((port & 0x04) == 0) {
      switch(port) {
        case 0: if ((status & COMMAND_BUSY) == 0) setCommand(value);  break; // Command
        case 1: if (pindex < pcount) setParam(value);                 break; // Param
        case 2: if (reset == 1 && value == 0) reset(); reset = value; break; // Reset
      }
    }
    else {
      setStatus(status & DATA_MASK);
      data = value;
    }
  }
  
  public final void reset() {
    count = pindex = pcount = mode = 0;
  }
  
  protected final void writeSpecial(int reg, int value) {
    switch(reg) {
      case 0x17: if ((value & 0xfc) == 0xc0) mode = value; break;
    }
  }
  
  protected final int readDriveStatus() {
    int result = 0x80;
    if (select0 && drives[0] != null) {
      if (drives[0].isReady())
        result |= 0x04;
    }
    if (select1 && drives[1] != null) {
      if (drives[1].isReady())
        result |= 0x40;
    }
    return result;
  }
  
  protected final void seek(int track) {
    if (select0 && drives[0] != null)
      drives[0].setCylinder(track);
    if (select1 && drives[1] != null)
      drives[1].setCylinder(track);
    // TODO: Timing
    count = 200;
  }
  
  protected final void readWriteVerify(int track, int sector, int length) {
    // length contains both the sector size and number of sectors
    c = track;
    h = 0;
    r = sector;
    n = (length >> 5) & 0x07;
    sectors = length & 0x1f;
    drive = select1 ? drives[1] : drives[0];
    if (drive == null)
      error(0x10);
    else {
      drive.setCylinder(track);
      if (sectors == 0)
        sectors = drive.getSectorCount();
      action = READ;
      count = 50;
    }
    status &= ~COMMAND_FULL;
  }
  
  protected final void readSectorByte() {
    if (offset == 0) {
      buffer = drive.getSector(c,h,r++,n);
      if (buffer == null) {
        error(0x1e);
        return;
      }
      size = 128 << n;
      if (buffer.length < size)
        size = buffer.length;
      //System.out.println(Util.dumpBytes(buffer));
    }
    data = buffer[offset++] & 0xff;
    status |= NON_DMA_DRQ;
    count = 50;
    if (offset >= size) {
      offset = 0;
      if (--sectors == 0) {
        action = RESULT;
      }
    }
    setStatus(status | IRQ);
  }
  
  protected final void error(int error) {
    action = -error;
    count = 10;
  }
  
  protected final void setCommand(int value) {
    command = value & 0x3f;
    System.out.println("FDC Command: " + Util.hex((byte)command));
    //((Processor)interruptDevice).stop();
    pindex = 0;
    status |= COMMAND_MASK;
    if (value == 0x35) pcount = 4;
    else {
      select0 = (value & 0x40) != 0;
      select1 = (value & 0x80) != 0;
      switch(command) {
        case 0x13: pcount = 3;                     break; // Read Data - Variable/Multi
        case 0x29: pcount = 1;                     break; // Seek
        
        case 0x2c: setResult(readDriveStatus(),0); break; // Read Drive status;
                   
        case 0x3a: pcount = 2;                     break; // Write Special Register
        default:   status &= ~COMMAND_MASK;               // Error ??
      }
    }
  }
  
  protected final void setParam(int value) {
    params[pindex++] = value;
    if (pindex == pcount) {
      System.out.print("Command Params: ");
      for (int i = 0; i < pcount; i++)
        System.out.print(Util.hex((byte)params[i]) + " ");
      System.out.println();
      action = RESULT;        // Only used when count set
      switch(command) {
        case 0x13: readWriteVerify(params[0],params[1],params[2]);             break; // Read Data
        case 0x29: seek(params[0]); status &= ~COMMAND_FULL;                   break;  // Seek
        case 0x35: status &= ~COMMAND_MASK;                                    break;  // Specify does nothing useful at the moment
        case 0x3a: writeSpecial(params[0],params[1]); status &= ~COMMAND_MASK; break;
        default:   status &= ~COMMAND_MASK;
      }
      System.out.println("Status=" + Util.hex((byte)status) + ", count=" + count);
    }
  }
  
  public final void cycle() {
    if (count > 0 && --count == 0) {
      //System.out.println("FDC Count ended");
      switch(action) {
        case READ: readSectorByte();                        break;
        default:   setResult(action < 0 ? -action : 0,IRQ); break; // Ok
      }
    }
  }
  
}
