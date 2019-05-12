package jemu.system.zx;

/**
 * Title:        JEMU
 * Description:  The Java Emulation Platform
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class Z80 extends jemu.core.cpu.Z80 {

  protected Renderer renderer;
  protected boolean nmiPending = false;

  public Z80(long cyclesPerSecond) {
    super(cyclesPerSecond);
  }

  protected int fetchOpCode() {
    int result = super.fetchOpCode();
    if ((PC & 0x8000) != 0 && (result & 0x40) == 0) {
      if ((I & 0x40) == 0)
        renderer.charByte(I << 8, result);
      else
        renderer.hiResByte(I << 8 | (R & 0x7f) | R7);
      result = 0;
    }
    return result;
  }

  protected void executeNormal(int opcode) {
    interruptPending = (R & 0x40) == 0 ? 1 : 0;
    super.executeNormal(opcode);
    if (nmiPending) {
      nmiPending = false;
      renderer.nmi();
    }
  }

  public void setRenderer(Renderer value) {
    renderer = value;
  }

  public void setNMIPending(boolean value) {
    nmiPending = value;
//    if (value)
//      System.out.println("NMI: " + cycles);
  }

}