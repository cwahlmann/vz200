package jemu.system.zx;

import jemu.core.device.memory.*;

/**
 * Title:        JEMU
 * Description:  The Java Emulation Platform
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class ZXMemory extends Memory {

  protected byte[] mem = new byte[32768];
  protected int limit;

  /**
   * @param size Size in K
   */
  public ZXMemory(int size) {
    super("ZX Memory");
    limit = 0x4000 + size * 0x400;
  }

  public int readByte(int address) {
    return mem[address & 0x7fff] & 0xff;
  }

  public int writeByte(int address, int value) {
    address &= 0x7fff;
    if (address >= 0x4000 && address < limit)
      mem[address] = (byte)value;
    return value & 0xff;
  }

  public void setMemory(int address, byte[] data) {
    System.arraycopy(data,0,mem,address,data.length);
  }

  public byte[] getMemory() {
    return mem;
  }

}