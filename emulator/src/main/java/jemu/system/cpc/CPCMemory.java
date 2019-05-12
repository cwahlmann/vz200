package jemu.system.cpc;

import jemu.core.*;
import jemu.core.device.memory.*;

/**
 * Title:        JEMU
 * Description:  The Java Emulation Platform
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

 /**
  * Actual memory mapping from OUT instructions performed by the Gate Array.
  *
  * Memory is allocated in 8K blocks.
  */
public class CPCMemory extends DynamicMemory {

  public static final int TYPE_64K          = 0;
  public static final int TYPE_128K         = 0x01;
  public static final int TYPE_256K         = 0x0f;
  public static final int TYPE_SILICON_DISC = 0xf0;
  public static final int TYPE_512K         = 0xff;

  protected int[] baseRAM = new int[8];
  protected int[] readMap  = new int[8];
  protected int[] writeMap = new int[8];

  protected boolean lower = false;
  protected boolean upper = false;
  protected int upperROM = 0;
  protected int bankRAM = -1;

  protected static final int BASE_RAM       = 0;
  protected static final int BASE_LOWROM    = BASE_RAM + 9;
  protected static final int BASE_UPROM     = BASE_LOWROM + 1;
  protected static final int BASE_MULTIFACE = BASE_UPROM + 16;

  public CPCMemory(int type) {
    super("CPC Memory",BASE_MULTIFACE + 1);
    setRAMType(type);  // Always happens first, first 64K always gets mapped in first
    reset();
  }

  public void reset() {
    setRAMBank(0xc0);
    upper = false;
    lower = true;
    upperROM = 0;
    remap();
  }

  public void setRAMType(int type) {
    getMem(BASE_RAM,64 * 1024);
    for (int i = BASE_RAM + 1; i < BASE_RAM + 9; i++) {
      if ((type & 0x01) != 0)
        getMem(i,64 * 1024);
      else
        freeMem(i,64 * 1024);
      type >>= 1;
    }
  }

  public void setLowerROM(byte[] data) {
    setROM(BASE_LOWROM,data);
  }

  public void setUpperROM(int rom, byte[] data) {
    setROM(BASE_UPROM + (rom & 0x0f),data);
  }

  protected void setROM(int base, byte[] data) {
    if (data == null || data.length == 0)
      freeMem(base,16 * 1024);
    else {
      base = getMem(base,16 * 1024);
      System.arraycopy(data,0,mem,base,Math.min(16 * 1024,data.length));
    }
    remap();
  }

  public void setLowerEnabled(boolean value) {
    if (lower != value) {
      lower = value;
      remap();
    }
  }

  public void setUpperEnabled(boolean value) {
    if (upper != value) {
      upper = value;
      remap();
    }
  }

  public void setUpperROM(int value) {
    value &= 0x0f;
    if (upperROM != value) {
      upperROM = value;
      remap();
    }
  }

  public void setRAMBank(int value) {
    value &= 0x3f;
    if (bankRAM != value) {
      bankRAM = value;
      remapRAM();
      remap();
    }
  }

  protected void remapRAM() {
    int bankBase = ((bankRAM & 0x38) >> 3) + BASE_RAM + 1;
    bankBase = baseAddr[bankBase];
    if (bankBase == -1) {             // 64K block not available
      bankBase = baseAddr[BASE_RAM];
      bankRAM = 0;
    }
    int base = (bankRAM & 0x07) == 2 ? bankBase : baseAddr[BASE_RAM];   // 0xc2, 0xca etc.
    for (int i = 0; i < 8; i ++)
      baseRAM[i] = base + i * 0x2000;
    if ((bankRAM & 0x05) == 0x01) {        // 0xc1, 0xc3, 0xc9, 0xcb etc.
      baseRAM[6] = bankBase + 0xc000;
      baseRAM[7] = bankBase + 0xe000;
      if ((bankRAM & 0x02) == 0x02) {      // 0xc3, 0xcb etc. Maps normal 0xc000 to 0x4000
        baseRAM[2] = base + 0xc000;
        baseRAM[3] = base + 0xe000;
      }
    }
    else if ((bankRAM & 0x04) == 0x04) {   // 0xc4, 0xc5, 0xc6, 0xc7, 0xcc, 0xcd etc.
      baseRAM[2] = bankBase + (bankRAM & 0x03) * 0x4000;
      baseRAM[3] = baseRAM[2] + 0x2000;
    }
  }

  public int getRAMBank() {
    return bankRAM | 0xc0;
  }

  public int readByte(int address) {
    return mem[readMap[address >> 13] + (address & 0x1fff)] & 0xff;
  }

  public int writeByte(int address, int value) {
    mem[writeMap[address >> 13] + (address & 0x1fff)] = (byte)value;
    return value & 0xff;
  }

  protected void remap() {
    mapRAM();
    mapROMs();
  }

  protected void mapRAM() {
    for (int i = 0; i < 8; i ++)
      readMap[i] = writeMap[i] = baseRAM[i];
  }

  protected void mapROMs() {
    // TODO: This does not support CPC Plus Cartridge or ROM Mapping
    // or multiface
    int addr;
    if (lower && (addr = baseAddr[BASE_LOWROM]) != -1) {
      readMap[0] = addr;
      readMap[1] = addr + 0x2000;
    }

    if (upper) {
      addr = baseAddr[BASE_UPROM + upperROM];
      if (addr == -1)
        addr = baseAddr[BASE_UPROM];
      if (addr != -1) {
        readMap[6] = addr;
        readMap[7] = addr + 0x2000;
      }
    }
  }

  public void writePort(int port, int value) {
    setUpperROM(value & 0x0f);
  }

  public int readByte(int address, Object config) {
    return readByte(address);
  }

}