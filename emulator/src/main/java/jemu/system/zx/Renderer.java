package jemu.system.zx;

import jemu.core.device.*;
import jemu.core.device.memory.*;
import jemu.core.renderer.*;

/**
 * Title:        JEMU
 * Description:  The Java Emulation Platform
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class Renderer extends MonitorRenderer {

  public static final int white = 0xefefef;
  public static final int black = 0x000000;

  public static final int SCR_ROWS    = 216;
  public static final int SCR_START   = 20;

  protected Memory memory;
  protected int col = 0;
  protected int scan = 0;
  protected int pairs = 0;
  protected int data = 0;
  protected int offs = 0;
  protected int hsync = 0;  // Count to HSYNC
  protected boolean scanReset = false;
  protected boolean frameSkip = false;
  protected boolean zx81 = false;
  protected boolean nmiEnabled = false;
  protected boolean nmiRequired = false;
  protected int nmiCount = 0;
  protected ZX zx;
  protected Z80 z80;

  protected static int[] pixdata = new int[0x800];
  static {
    for (int i = 0; i < 0x100; i++) {
      pixdata[i]         = (i & 0x02) == 0 ? white : black;
      pixdata[i + 0x400] = (i & 0x01) == 0 ? white : black;
      pixdata[i + 0x100] = (i & 0x08) == 0 ? white : black;
      pixdata[i + 0x500] = (i & 0x04) == 0 ? white : black;
      pixdata[i + 0x200] = (i & 0x20) == 0 ? white : black;
      pixdata[i + 0x600] = (i & 0x10) == 0 ? white : black;
      pixdata[i + 0x300] = (i & 0x80) == 0 ? white : black;
      pixdata[i + 0x700] = (i & 0x40) == 0 ? white : black;
    }
  }

  public Renderer(ZX zx, Z80 z80, Memory memory) {
    super("ZX Renderer");
    this.zx = zx;
    this.z80 = z80;
    this.memory = memory;
    setCycleFrequency(z80.getCyclesPerSecond());
  }

  public void charByte(int base, int ch) {
    data = memory.readByte(base + scan + ((ch & 0x3f) << 3));
    if ((ch & 0x80) != 0)
      data ^= 0xff;
    pairs = 0x400;
  }

  public void hiResByte(int addr) {
    data = memory.readByte(addr);
    pairs = 0x400;
  }

  public void cycle() {
    debug = zx.getMode() != ZX.RUN;
    //if (pairs != 0)
    //  System.out.print(" row=" + monitorLine + ", col=" + col + ", pairs=" + pairs);
    if (monitorLine >= SCR_START && monitorLine < SCR_ROWS + SCR_START && !frameSkip) {
      if (col >= 0 && col < 160) {
        if (pairs == 0)
          pixels[offs++] = pixels[offs++] = white;
        else {
          pairs -= 0x100;
          pixels[offs++] = pixdata[pairs + data];
          pixels[offs++] = pixdata[pairs + 0x400 + data];
        }
      }
    }
    else if (pairs != 0)
      pairs -= 0x100;
    col++;
    clock();
    if (hsync > 0) {
      hsync--;
      if (hsync == 15 && !inVSync)
        hSyncStart();
      if (hsync == 0) {
        scan = scanReset ? 0 : ++scan & 0x07;
        if (!inVSync)
          hSyncEnd();
      }
    }

    if (++nmiCount == 192) {
      nmiRequired = nmiEnabled;
      if (nmiEnabled && !inVSync) {
        hSyncStart();
        hsync = 15;
      }
    }
    else if (nmiCount == 207) {
      nmiCount = 0;
      nmiRequired = false;
    }
    if (nmiRequired)
      z80.setNMIPending(true);
  }

  public void hSync() {
    if (zx.getMode() != ZX.RUN)
      System.out.println("hSync(): col=" + col + ", row=" + monitorLine + ", vPos=" + vPos);
    if (monitorLine >= SCR_START && monitorLine < SCR_ROWS + SCR_START && !frameSkip)
      for (int i = Math.max(0,col + 1); i < 160; i++)
          pixels[offs++] = pixels[offs++] = 0xffffff00; //black;
    col = -40 + (hPos * 3) / (adder * 2);
  }

  public void setInterrupt(int mask) {
    debug = zx.getMode() != ZX.RUN;
    // Next screen line
    hsync = 11 + 15;  // Interrupt line goes low on next /M1 cycle
  }

  public void vSync(boolean interlace) {
    offs = 0;
    zx.vsync();
  }

  public int readPort(int port) {
    vSyncStart();
    //hSyncStart();  // TODO: Does this happen??? HiRes Tetris doesn't like it!
    scanReset = true;
    scan = 0;
    //nmiCount = 0;
    return 0xff;
  }

  public void writePort(int port, int value) {
    // Turns off the VSYNC
    scanReset = false;
    vSyncEnd();
    //hSyncEnd();
    if (zx81)
      if ((port & 0x01) == 0) {
        nmiEnabled = true;
        nmiRequired = nmiCount >= 192;
        if (nmiRequired && hsync == 0)
          hsync = 15;
        if (nmiRequired)
          z80.setNMIPending(true);
      }
      else if ((port & 0x02) == 0)
        nmiEnabled = nmiRequired = false;
  }

  public void setFrameSkip(boolean value) {
    frameSkip = value;
    if (value)
      pairs = 0;
  }

  public void nmi() {
    debug = zx.getMode() != ZX.RUN;
    nmiRequired = false;
    if (z80.isInHalt())
      z80.cycle(207 - nmiCount + 1);
    z80.nmi();
  }

  public void setZX81(boolean value) {
    zx81 = value;
  }

}