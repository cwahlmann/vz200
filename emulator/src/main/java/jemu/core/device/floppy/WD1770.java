/*
 * WD1770.java
 *
 * Created on 27 July 2006, 20:45
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
public class WD1770 extends Device {
  
  public static final int MOTOR_ON_DELAY   =  500000;
  public static final int MOTOR_OFF_DELAY  = 1500000;
  
  public static final int MOTOR_ON      = 0x80;
  public static final int WRITE_PROTECT = 0x40;
  public static final int SPIN_UP       = 0x20;
  public static final int DELETED_DATA  = 0x20;
  public static final int NOT_FOUND     = 0x10;
  public static final int CRC_ERROR     = 0x08;
  public static final int TRACK_ZERO    = 0x04;
  public static final int LOST_DATA     = 0x04;
  public static final int INDEX         = 0x02;
  public static final int DATA_REQUEST  = 0x02;
  public static final int BUSY          = 0x01;
  
  public static final int NO_COMMAND    = 0;
  public static final int EXECUTE       = 1;
  public static final int SETTLE        = 2;
  public static final int SPIN_DOWN     = 3;
  
  protected int status;
  protected int track;
  protected int sector;
  protected int data;
  
  protected int command;             // Current command
  protected int count;               // Current count
  protected int count2;              // Next count
  protected int count3;              // Third count
  protected int mode = NO_COMMAND;
  protected int currentTrack = 0;
  protected int direction = -1;
  protected int stepRate;
  protected int[] stepRates = { 6000, 12000, 20000, 30000 };
  protected int settleTime = 30000;
  
  protected Device interruptDevice;
  protected int interruptMask;
  protected Device dataRequestDevice;
  protected int dataRequestMask;
  
  /** Creates a new instance of WD1770 */
  public WD1770() {
    super("WD1770 Floppy Controller");
  }
  
  public void setInterruptDevice(Device device, int mask) {
    interruptDevice = device;
    interruptMask = mask;
  }
  
  public void setDataRequestDevice(Device device, int mask) {
    dataRequestDevice = device;
    dataRequestMask = mask;
  }
  
  protected void interrupt(boolean set) {
    if (interruptDevice != null) {
      if (set)
        interruptDevice.setInterrupt(interruptMask);
      else
        interruptDevice.clearInterrupt(interruptMask);
    }
  }
  
  protected void dataRequest(boolean set) {
    if (dataRequestDevice != null) {
      if (set)
        dataRequestDevice.setInterrupt(dataRequestMask);
      else
        dataRequestDevice.clearInterrupt(dataRequestMask);
    }
  }
  
  protected void spinup(int command) {
    status &= ~SPIN_UP;
    if ((command & 0x08) == 0 && (status & MOTOR_ON) == 0) {
      count = MOTOR_ON_DELAY;
      mode = SPIN_UP;
    }
    else
      mode = EXECUTE;
  }
  
  protected void seek(int command, int track) {
    System.out.println("Seek: " + track + ", current=" + currentTrack);
    spinup(command);
    stepRate = stepRates[command & 0x03];
    int diff = currentTrack - track;
    count2 = (diff < 0 ? -diff : diff) * stepRate;
    if (track < 0) currentTrack = 0;
    else if (track > 255) currentTrack = 255;
    else currentTrack = track;
    if ((command & 0x04) != 0)
      count2 += settleTime;
  }
  
  public int readPort(int port) {
    //System.out.println("FDC Read Port: " + port + ", status=" + Util.hex((byte)status));
    switch(port) {
      case 0: interrupt(false); return status;
      case 1: return track;
      case 2: return sector;
      case 3: dataRequest(false); return data;
    }
    return 0xff;
  }
  
  public void writePort(int port, int value) {
    System.out.println("FDC: Write port: " + port + " = " + Util.hex((byte)value));
    switch(port) {
      case 0: if ((status & BUSY) == 0 || (value & 0xf0) == 0xd0) command(value); break;
      case 1: if ((status & BUSY) == 0) track = value;                            break;
      case 2: if ((status & BUSY) == 0) sector = value;                           break;
      case 3: break;  // Data write
    }
  }
  
  protected void command(int value) {
    System.out.println("FDC Command: " + Util.hex((byte)value));
    command = value;
    status |= BUSY;
    switch(command & 0xf0) {
      case 0x00: seek(command,0);                                break; // Restore
      case 0x10: seek(command,data);                             break; // Seek
      case 0x20:
      case 0x30: seek(command,currentTrack + direction);         break; // Step
      case 0x40:
      case 0x50: direction = 1; seek(command,currentTrack +
                   (currentTrack > 76 ? -1 : 1));                break; // Step out
      case 0x60: 
      case 0x70: direction = -1; seek(command,currentTrack - 1); break; // Step in
    }
    if (mode == EXECUTE) {
      count = count2;
      count2 = 0;
    }
    System.out.println("Command started: mode=" + mode + ", count=" + count + ", count2=" + count2 +
      ", status=" + Util.hex((byte)status));
    ((Processor)interruptDevice).stop();
  }
  
  public void reset() {
    status = track = sector = data = 0;
  }
  
  public void cycle() {
    if (mode != NO_COMMAND && --count == 0) {
      System.out.println("Count done, mode=" + mode + ", status=" + Util.hex((byte)status));
      if (mode == SPIN_UP) {
        mode = EXECUTE;
        status |= SPIN_UP | MOTOR_ON;
        count = count2;
        count2 = 0;
      }
      else if (mode == EXECUTE) {
        status &= ~BUSY;
        if (command < 0x80) {
          if (currentTrack == 0) status &= ~TRACK_ZERO; else status |= TRACK_ZERO;
          if (command < 0x30 || (command < 0x80 && (command & 0x08) != 0))
            track = currentTrack;
        }
        interrupt(true);
        mode = SPIN_DOWN;
        count = MOTOR_OFF_DELAY;
      }
      else if (mode == SPIN_DOWN) {
        mode = NO_COMMAND;
        status &= ~MOTOR_ON;
      }
      System.out.println("New Mode: " + mode + ", count=" + count + ", status=" + Util.hex((byte)status));
    }
  }
  
}
