package jemu.util.diss;

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

public abstract class Disassembler {

  protected Object config;
  protected int addrMask = 0xffff;

  protected int nextAddress(int[] address) {
    int result = address[0];
    address[0] = (result + 1) & addrMask;
    return result;
  }

  public abstract String disassemble(Memory memory, int[] address);

  public String disassemble(Memory memory, int[] address, boolean showAddr,
    int dataPos)
  {
    int start = address[0];
    String result = Util.hex((short)start) + ": " + disassemble(memory,address);
    if (dataPos != 0) {
      while (result.length() < dataPos)
        result += " ";
      for (int i = start; i != address[0]; i = (i + 1) & 0xffff) {
        if (i != start)
          result += " ";
        result += Util.hex((byte)memory.readByte(i,config));
      }
    }
    return result;
  }

  public void setMemoryConfiguration(Object value) {
    config = value;
  }

  public Object getMemoryConfiguration() {
    return config;
  }
}