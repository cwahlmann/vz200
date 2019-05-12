/*
 * Memory.java
 *
 * Created on 30 August 2006, 16:10
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.system.spectrum;

import jemu.core.device.memory.*;

/**
 *
 * @author Richard
 */
public class Memory extends DynamicMemory {
  
  public static final int ROM = 0;
  public static final int RAM = 1;
  
  /** Creates a new instance of Memory */
  public Memory() {
    super("ZX Spectrum Memory",2);
    getMem(ROM,0x4000);
    getMem(RAM,0xc000);
  }
  
  public int readByte(int addr) {
    return mem[addr] & 0xff;
  }
  
  public int writeByte(int addr, int value) {
    if (addr >= 0x4000)
      mem[addr] = (byte)value;
    return value & 0xff;
  }
  
  public void setROM(byte[] data) {
    System.arraycopy(data,0,mem,0,Math.min(data.length,0x4000));
  }
}
