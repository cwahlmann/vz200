/*
 * BBCMemory.java
 *
 * Created on 16 July 2006, 17:19
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.system.bbc;

import jemu.core.device.memory.*;

 /**
  * Actual memory mapping from I/O map performed by BBC.
  *
  * Memory is allocated in 16K blocks.
  */
public class BBCMemory extends DynamicMemory {
  
  protected int[] readMap = new int[4];
  protected int[] writeMap = new int[4];
  
  protected static final int BASE_RAM    = 0;
  protected static final int BASE_OS_ROM = 3;
  protected static final int BASE_ROM    = BASE_OS_ROM + 1;
  protected static final int LAST_ROM    = BASE_ROM + 15;
  
  public BBCMemory() {
    super("BBC Memory",LAST_ROM + 1);
    for (int i = BASE_RAM; i < BASE_OS_ROM; i++)
      getMem(i,0x4000);
    for (int i = 0; i < 3; i++)
      readMap[i] = writeMap[i] = baseAddr[BASE_RAM + i];
  }
  
  public int readByte(int address) {
    return mem[readMap[address >> 14] + (address & 0x3fff)] & 0xff;
  }

  public int writeByte(int address, int value) {
    mem[writeMap[address >> 14] + (address & 0x3fff)] = (byte)value;
    return value & 0xff;
  }
  
  public void setOSROM(byte[] value) {
    int start = getMem(BASE_OS_ROM,0x4000);
    System.arraycopy(value,0,mem,start,Math.min(value.length,0x4000));
    readMap[3] = start;
  }
  
  public void loadROM(int slot, byte[] value) {
    int start = getMem(BASE_ROM + (slot & 0x0f),0x4000);
    System.arraycopy(value,0,mem,start,Math.min(value.length,0x4000));
  }
  
  public void selectROM(int slot) {
    int base = baseAddr[BASE_ROM + slot];
    readMap[2] = base == -1 ? baseAddr[BASE_RAM + 2] : base;
  }

}