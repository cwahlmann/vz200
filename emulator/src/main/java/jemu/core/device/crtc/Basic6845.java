package jemu.core.device.crtc;

import jemu.core.*;
import jemu.core.device.*;

/**
 * Title:        JEMU
 * Description:  The Java Emulation Platform
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class Basic6845 extends CRTC {

  protected final int EVENT_HSYNC_START = 0x01;
  protected final int EVENT_HDISP_END   = 0x02;
  protected final int EVENT_HDISP_START = 0x04;
  protected final int EVENT_VSYNC_START = 0x08;
  protected final int EVENT_VSYNC_END   = 0x10;
  
  protected static final int[] CURSOR_FLASH_MASKS = { 0x40, 0x00, 0x10, 0x20 };

  public int ma;
  public int ra;
  protected int hCC;
  protected int vCC;
  protected int hCCMask = 0x7f;

  protected int[] reg = new int[32];
  protected int[] orig = new int[32];
  protected int[] rdMask;
  protected int[] wrMask;

  protected int[] eventMask = new int[256];

  protected int registerSelectMask = 0x01;
  protected int registerSelectTest = 0x00;

  protected int selReg;

  protected int hChars = 1;
  protected int hSyncStart, hDispEnd, hSyncWidth, hSyncCount, vSyncWidth, vSyncCount;
  protected boolean inHSync = false;
  protected boolean inVSync = false;
  public boolean hDisp = true;
  public boolean vDisp = true;
  protected boolean interlace = false;
  protected int interlaceVideo = 0;  // 0 for normal, 1 for interlace sync & video
  protected int scanAdd = 1;         // 2 for interlace sync & video
  protected int maxRaster = 0;       // Register 9 | interlaceVideo
  protected int frame = 0;           // Toggles between 0 and 1 for interlace mode
  protected int maBase = 0;          // Base address for current character line
  protected int maScreen = 0;        // Base address at start of screen
  protected int vtAdj = 0;           // Vertical total adjust
  protected int halfR0 = 0;          // Used for VSync in interlace odd frames
  protected int hDispDelay = 0;      // HDISP delay
  protected int cursorMA = 0;        // Cursor position
  protected int cursorStart = 0;     // Cursor start
  protected int cursorEnd   = 0;     // Cursor end
  protected boolean cursor = false;  // Is cursor on?
  protected int cursorCount = 0;     // Cursor flash counter
  protected int cursorFlash = 0;     // Cursor flash mask
  protected int cursorDelay = 0;     // Cursor delay programmed in register 8
  protected int cursorWait  = 0;     // Delay counter

  public Basic6845() {
    super("Basic 6845");
    rdMask = wrMask = new int[32];
    for (int i = 0; i < 32; i++)
      rdMask[i] = wrMask[i] = 0xff;
    reset();
  }

  public void reset() {
    selReg = hCC = 0;
    ma = maBase;
    hSyncWidth = hSyncCount = 0;
    inHSync = false;
    for (int i = 0; i < eventMask.length; i++)
      eventMask[i] = 0;
    reg[0] = 0x3f & wrMask[0];
    setEvents();
  }
  
  public void setWriteMask(int reg, int mask) {
    wrMask[reg] = mask;
  }

  public void cycle() {
    if (hCC == reg[0]) {
      hCC = 0;
      scanStart();
      ma = maBase;
    }
    else {
      hCC = (hCC + 1) & hCCMask;
      ma = (maBase + hCC) & 0x3fff;
    }
    if (inHSync) {
      hSyncCount = (hSyncCount + 1) & 0x0f;
      if (hSyncCount == hSyncWidth) {
        inHSync = false;
        listener.hSyncEnd();
      }
    }
    int mask = eventMask[hCC];
    if (mask != 0) {
      if ((mask & EVENT_VSYNC_START) != 0) {
        eventMask[hCC] &= ~EVENT_VSYNC_START;
        inVSync = true;
        listener.vSyncStart();
      }
      else if ((mask & EVENT_VSYNC_END) != 0) {
        eventMask[hCC] &= ~EVENT_VSYNC_END;
        inVSync = false;
        listener.vSyncEnd();
      }
      if ((mask & EVENT_HSYNC_START) != 0) {
        inHSync = true;
        hSyncCount = 0;
        listener.hSyncStart();
      }
      if (vDisp) {
        if ((mask & EVENT_HDISP_START) != 0) {
          hDisp = true;
          listener.hDispStart(); 
        }
        if ((mask & EVENT_HDISP_END) != 0) {
          hDisp = false;
          listener.hDispEnd();
        }
      }
    }
    if (cursor) {
      if (cursorWait > 0) {
        if (--cursorWait == 0)
          listener.cursor();
      }
      else if (ma == cursorMA && ra >= cursorStart && ra <= cursorEnd && hDisp) {
        if ((cursorWait = cursorDelay) == 0)
          listener.cursor();
      }
    };
  }
  
  protected void newFrame() {
    vCC = 0;
    frame = interlace ? frame ^ 0x01 : 0;
    ra = frame & interlaceVideo;
    vDisp = reg[6] != 0;
    ma = maBase = maScreen;
    listener.vDispStart();
    checkVSync();
    cursorCount = (cursorCount + 1) | 0x40;       // 0x40 is for always on
    cursor = (cursorCount & cursorFlash) != 0 && cursorDelay != 0x03;
  }
  
  protected void checkVSync() {
    if (vCC == reg[7] && !inVSync) {
      vSyncCount = 0;
      if (interlace && (frame == 0))
        eventMask[halfR0] |= EVENT_VSYNC_START;
      else {
        inVSync = true;
        listener.vSyncStart();
      }
        //System.out.println("vSync Start: reg7=" + reg[7]);
    }
  }

  protected void scanStart() {
    //System.out.print(Integer.toString(vCC) + ":" + ra + " ");
    if (inVSync && (vSyncCount = (vSyncCount + 1) & 0x0f) == vSyncWidth) {
      if (interlace && (frame == 0))
        eventMask[halfR0] |= EVENT_VSYNC_END;
      else {
        inVSync = false;
        listener.vSyncEnd();
      }
    }
    if (vtAdj > 0 && --vtAdj == 0)
      newFrame();
    else if ((ra | interlaceVideo) == maxRaster) {
      if (vCC == reg[4] && vtAdj == 0) {
        vtAdj = reg[5];
        if (interlace && frame == 0)
          vtAdj++;
        if (vtAdj == 0) {
          newFrame();
          return;
        }
      }
      vCC = (vCC + 1) & 0x7f;
      maBase = (maBase + reg[1]) & 0x3fff;
      checkVSync();
      if (vCC == reg[6])
        vDisp = false;
      ra = frame & interlaceVideo;
    }
    else
      ra = (ra + scanAdd) & 0x1f;
  }

  public void writePort(int port, int value) {
    if ((port & registerSelectMask) == registerSelectTest)
      selReg = value & 0x1f;
    else
      setRegister(selReg,value);
  }

  public void setRegister(int index, int value) {
    //System.out.println("Reg " + index + " = " + Util.hex((byte)value));
    orig[index] = value & 0xff;
    value &= wrMask[index];
    if (reg[index] != value) {
      reg[index] = value;
      switch(index) {
        case 0:
        case 1:
        case 2: setEvents(); break;
        case 3: setReg3(value); setEvents(); break;
        case 8: setReg8(value); break;
        case 9: maxRaster = value | interlaceVideo; break;
        case 10: cursorStart = value & 0x1f; cursorFlash = CURSOR_FLASH_MASKS[(value >> 5) & 0x03]; break;
        case 11: cursorEnd = value & 0x1f; break;
        case 12:
        case 13: maScreen = (reg[13] + (reg[12] << 8)) & 0x3fff; break;
        case 14:
        case 15: cursorMA = (reg[15] + (reg[14] << 8)) & 0x3fff; break;
      }
    }
    //if (index == 10 || index == 11)
    //  System.out.println("Cursor: " + cursorStart + " - " + cursorEnd);
  }

  protected void setEvents() {
    eventMask[hSyncStart] &= ~EVENT_HSYNC_START;
    eventMask[hDispDelay] &= ~EVENT_HDISP_START;
    eventMask[hDispEnd] &= ~EVENT_HDISP_END;
    hChars = reg[0] + 1;
    halfR0 = hChars >> 1;
    hSyncStart = reg[2];
    hDispDelay = (reg[8] >> 4) & 0x03;
    hDispEnd = reg[1] + hDispDelay;
    eventMask[hSyncStart] |= EVENT_HSYNC_START;
    eventMask[hDispDelay] |= EVENT_HDISP_START;
    eventMask[hDispEnd] |= EVENT_HDISP_END;
  }

  protected void setReg3(int value) {
    hSyncWidth = value & 0x0f;
    vSyncWidth = (value >> 4) & 0x0f;
  }
  
  protected void setReg8(int value) {
    interlace = (value & 0x01) != 0;
    interlaceVideo = (value & 0x03) == 3 ? 1 : 0;
    scanAdd = interlaceVideo + 1;
    maxRaster = reg[9] | interlaceVideo;
    cursorDelay = (value >> 6) & 0x03;
    setEvents();
  }

  public void setRegisterSelectMask(int mask, int test) {
    registerSelectMask = mask;
    registerSelectTest = test;
  }

  public int getHCC() {
    return hCC;
  }

  public boolean isVDisp() {
    return vDisp;
  }

  public boolean isVSync() {
    return inVSync;
  }

  public boolean isHSync() {
    return inHSync;
  }

  public boolean isHDisp() {
    return hDisp;
  }

  public int getMA() {
    return ma;
  }
  
  public int getRA() {
    return ra;
  }
  
  public int getScreenMA() {
    return maScreen;
  }
  
  public int getVCC() {
    return vCC;
  }
  
  public int getVLC() {
    return ra;
  }

  public int getReg(int index) {
    return reg[index];
  }

  public void setSelectedRegister(int value) {
    selReg = value & 0x1f;
  }
  
  public boolean isInterlace() {
    return interlace;
  }
  
  public boolean isInterlaceVideo() {
    return interlaceVideo == 1;
  }
  
  public int getFrame() {
    return frame;
  }
  
}