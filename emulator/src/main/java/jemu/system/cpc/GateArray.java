package jemu.system.cpc;

import java.awt.*;
import jemu.core.*;
import jemu.core.device.*;
import jemu.core.device.crtc.*;
import jemu.core.renderer.*;

/**
 * Title:        JEMU
 * Description:  The Java Emulation Platform
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class GateArray extends MonitorRenderer implements CRTCListener {

  protected static final int HOFFSET  = 0x0b0000 - 0x800;    // Offset of monitor (half pixel adjust)
  protected static final int HOFFSEND = HOFFSET + 0x300000;  // End of offset

  protected static Dimension HALF_DISPLAY_SIZE = new Dimension(384,270);
  protected static Dimension FULL_DISPLAY_SIZE = new Dimension(768,270);

  protected CPC cpc;
  protected Z80 z80;
  protected Basic6845 crtc;
  protected int r52;
  protected int vSyncInt = 0;
  protected int interruptMask;
  protected int hSyncCount;
  protected int vSyncCount = 0;
  protected int screenMode = -1;
  protected int newMode = 0;
  protected boolean inHSync = false;
  protected boolean outHSync = false;
  protected int[] inks = new int[33];  // Ink 32 is black (for Sync renderer);
  protected byte[] fullMap;
  protected byte[] halfMap;
  protected int offset = 0;
  protected int scanStart = 0;
  protected boolean scanStarted;
  protected Renderer borderRenderer;
  protected Renderer syncRenderer;
  protected Renderer defRenderer;
  protected Renderer startRenderer;
  protected Renderer endRenderer;
  protected Renderer renderer;
  protected int endPix;
  protected int selInk = 0;
  protected boolean render = true;
  protected boolean rendering = true;
  protected byte[] memory;
  protected int[] palTranslate = new int[4096];
  protected boolean halfSize = false;

  protected static final int[] maTranslate = new int[65536];

  protected static final int[] inkTranslate = {
    0x666, 0x666, 0xF06, 0xFF6, 0x006, 0x0F6, 0x606, 0x6F6,
    0x0F6, 0xFF6, 0xFF0, 0xFFF, 0x0F0, 0x0FF, 0x6F0, 0x6FF,
    0x006, 0xF06, 0xF00, 0xF0F, 0x000, 0x00F, 0x600, 0x60F,
    0x066, 0xF66, 0xF60, 0xF6F, 0x060, 0x06F, 0x660, 0x66F
  };

  protected static final byte[][] fullMaps = new byte[4][];
  protected static final byte[][] halfMaps = new byte[4][];
  static {
    for (int mode = 0; mode < 4; mode++) {
      byte[] full = new byte[65536 * 16];
      byte[] half = new byte[65536 * 8];
      fullMaps[mode] = full;
      halfMaps[mode] = half;
      for (int i = 0; i < 65536 * 8;) {
        int b1 = (i >> 3) & 0xff;
        int b2 = (i >> 11) & 0xff;
        decodeHalf(half,i,mode,b1);
        decodeFull(full,i * 2,mode,b1);
        i += 4;
        decodeHalf(half,i,mode,b2);
        decodeFull(full,i * 2,mode,b2);
        i += 4;
      }
    }
    for (int i = 0; i < maTranslate.length; i++) {
      int j = i << 1;
      maTranslate[i] = (j & 0x7fe) | ((j & 0x6000) << 1);
    }
  }

  protected static final void decodeHalf(byte[] map, int offs, int mode, int b) {
    switch(mode) {
      case 0:
        map[offs] = map[offs + 1] = (byte)
          (((b & 0x80) >> 7) | ((b & 0x08) >> 2) | ((b & 0x20) >> 3) | ((b & 0x02) << 2));
        map[offs + 2] = map[offs + 3] = (byte)
          (((b & 0x40) >> 6) | ((b & 0x04) >> 1) | ((b & 0x10) >> 2) | ((b & 0x01) << 3));
        break;

      case 1:
        map[offs++] = (byte)(((b & 0x80) >> 7) | ((b & 0x08) >> 2));
        map[offs++] = (byte)(((b & 0x40) >> 6) | ((b & 0x04) >> 1));
        map[offs++] = (byte)(((b & 0x20) >> 5) | (b & 0x02));
        map[offs]   = (byte)(((b & 0x10) >> 4) | ((b & 0x01) << 1));
        break;

      case 2:
        map[offs++] = (byte)((b & 0x80) >> 7);
        map[offs++] = (byte)((b & 0x20) >> 5);
        map[offs++] = (byte)((b & 0x08) >> 3);
        map[offs]   = (byte)((b & 0x02) >> 1);
        break;

      case 3:
        break;
    }
  }

  protected static final void decodeFull(byte[] map, int offs, int mode, int b) {
    switch(mode) {
      case 0:
        map[offs] = map[offs + 1] = map[offs + 2] = map[offs + 3] = (byte)
          (((b & 0x80) >> 7) | ((b & 0x08) >> 2) | ((b & 0x20) >> 3) | ((b & 0x02) << 2));
        map[offs + 4] = map[offs + 5] = map[offs + 6] = map[offs + 7] = (byte)
          (((b & 0x40) >> 6) | ((b & 0x04) >> 1) | ((b & 0x10) >> 2) | ((b & 0x01) << 3));
        break;

      case 1:
        map[offs]     = map[offs + 1] = (byte)(((b & 0x80) >> 7) | ((b & 0x08) >> 2));
        map[offs + 2] = map[offs + 3] = (byte)(((b & 0x40) >> 6) | ((b & 0x04) >> 1));
        map[offs + 4] = map[offs + 5] = (byte)(((b & 0x20) >> 5) | (b & 0x02));
        map[offs + 6] = map[offs + 7] = (byte)(((b & 0x10) >> 4) | ((b & 0x01) << 1));
        break;

      case 2:
        map[offs++] = (byte)((b & 0x80) >> 7);
        map[offs++] = (byte)((b & 0x40) >> 6);
        map[offs++] = (byte)((b & 0x20) >> 5);
        map[offs++] = (byte)((b & 0x10) >> 4);
        map[offs++] = (byte)((b & 0x08) >> 3);
        map[offs++] = (byte)((b & 0x04) >> 2);
        map[offs++] = (byte)((b & 0x02) >> 1);
        map[offs]   = (byte)(b & 0x01);
        break;

      case 3:
        break;
    }
  }

  public GateArray(CPC cpc) {
    super("Amstrad Gate Array");

    setCycleFrequency(1000000);  // 1MHz
    this.cpc = cpc;
    z80 = cpc.z80;
    crtc = cpc.crtc;
    reset();

    for (int i = 0; i < 4096; i++) {
      int b = i & 0x00f;
      int r = (i & 0x0f0) >> 4;
      int g = (i & 0xf00) >> 8;
      g = g * 255 / 15;
      r = r * 255 / 15;
      b = b * 255 / 15;
      palTranslate[i] = 0xff000000 + (r << 16) + (g << 8) + b;
    }
    setHalfSize(true);
  }

  public void setHalfSize(boolean value) {
    if (halfSize != value || defRenderer == null) {
      halfSize = value;
      defRenderer = halfSize ? (Renderer)new HalfRenderer() : new FullRenderer();
      startRenderer = halfSize ? (Renderer)new HalfStartRenderer() : new FullStartRenderer();
      endRenderer = halfSize ? (Renderer)new HalfEndRenderer() : new FullEndRenderer();
      borderRenderer = new BorderRenderer(16,halfSize ? 8 : 16);
      syncRenderer = new BorderRenderer(32,halfSize ? 8 : 16);
      renderer = borderRenderer;
    }
  }

  public void reset() {
    r52 = 0;
    setScreenMode(0);
    for (int i = 0; i < 33; i++)
      inks[i] = 0xff000000;
    inks[0x10] = 0xff808080;
  }

  public void setSelectedInk(int value) {
    selInk = (value & 0x1f) < 0x10 ? value & 0x0f : 0x10;
  }

  public void setInk(int index, int value) {
    inks[index] = palTranslate[inkTranslate[value & 0x1f]];
  }

  public void writePort(int port, int value) {
    if ((value & 0x80) == 0) {
      if ((value & 0x40) == 0) {
        value &= 0x1f;
        selInk = value < 0x10 ? value : 0x10;
      }
      else
        inks[selInk] = palTranslate[inkTranslate[value & 0x1f]];
    }
    else {
      CPCMemory memory = (CPCMemory)cpc.getMemory();
      if ((value & 0x40) == 0)
        setModeAndROMEnable(memory,value);
      else
        memory.setRAMBank(value);
    }
  }

  public void setModeAndROMEnable(CPCMemory memory, int value) {
    memory.setLowerEnabled((value & 0x04) == 0);
    memory.setUpperEnabled((value & 0x08) == 0);
    if ((value & 0x10) != 0) {
      r52 = 0;
      setInterruptMask(interruptMask & 0x70);
    }
    newMode = value & 0x03;
  }

  protected void setScreenMode(int mode) {
    screenMode = mode;
    fullMap = fullMaps[mode];
    halfMap = halfMaps[mode];
  }

  public void setInterruptMask(int value) {
    interruptMask = value;
    if (interruptMask != 0)
      z80.setInterrupt(1);
    else
      z80.clearInterrupt(1);
  }

  public void setInterrupt(int mask) {
    r52 &= 0x1f;
    setInterruptMask(interruptMask & 0x70);
  }

  public void cycle() {
    //debug = cpc.getMode() != CPC.RUN;
    if (scanStarted) {
      if (hPos < HOFFSEND)
        renderer.render();
      else {
        endRenderer.render();
        render = scanStarted = false;
      }
    }
    else if (render && hPos >= HOFFSET) {
      startRenderer.render();
      scanStarted = true;
    }

    crtc.cycle();
    if (inHSync) {
      hSyncCount++;
      if (hSyncCount == 2) {
        outHSync = true;
        super.hSyncStart();
      }
      else if (hSyncCount == 7)
        endHSync();
    }
    clock();
  }

  // TODO: WinAPE does some weird stuff here to suit Overflow Preview 3
  protected void endHSync() {
    if (outHSync)
      super.hSyncEnd();
    outHSync = false;
    if (screenMode != newMode)
      setScreenMode(newMode);
  }

  public void hSyncStart() {
    hSyncCount = 0;
    inHSync = true;
    renderer = syncRenderer;
  }

  public void hSync() {
    if (render = rendering && (monitorLine >= 0 && monitorLine < 270)) {
      offset = scanStart;
      scanStart += halfSize ? 384 : 768;
    }
    if (vSyncCount > 0) {
      if (--vSyncCount == 0)
        super.vSyncEnd();
    }
    scanStarted = false;
  }

  public void hSyncEnd() {
    debug = cpc.getMode() != CPC.RUN;
    endHSync();
    if (++r52 == 52) {
      r52 = 0;
      setInterruptMask(interruptMask | 0x80);
    }
    if (vSyncInt > 0 && --vSyncInt == 0) {
      if (r52 >= 32)
        setInterruptMask(interruptMask | 0x80);
      r52 = 0;
    }
    renderer = vSyncCount > 0 ? syncRenderer :
      (crtc.isVDisp() && crtc.isHDisp() ? defRenderer : borderRenderer);
  }

  public void hDispEnd() {
    renderer = vSyncCount > 0 || crtc.isHSync() ? syncRenderer : borderRenderer;
  }

  public void hDispStart() {
    renderer = vSyncCount > 0 || crtc.isHSync() ? syncRenderer :
      (crtc.isVDisp() ? defRenderer : borderRenderer);
  }

  public void vSyncStart() {
    super.vSyncStart();
    vSyncCount = 32;
    renderer = syncRenderer;
    vSyncInt = 2;
  }

  public void vSyncEnd() { }

  public void vSync(boolean interlace) {
    scanStart = offset = 0;
    cpc.vSync();
  }
  
  public void vDispStart() { }

  public void setMemory(byte[] value) {
    memory = value;
  }

  public void setRendering(boolean value) {
    rendering = value;
    if (!rendering)
      render = scanStarted = false;
  }

  public Dimension getDisplaySize(boolean large) {
    return large ? FULL_DISPLAY_SIZE : HALF_DISPLAY_SIZE;
  }
  
  public void cursor() { }  // Not used on CPC

  protected abstract class Renderer {
    public void render() { }
  }

  protected class BorderRenderer extends Renderer {

    protected int ink;
    protected int width;

    public BorderRenderer(int ink, int width) {
      this.ink = ink;
      this.width = width;
    }

    public void render() {
      int pix = inks[ink];
      for (int i = 0; i < width; i++)
        pixels[offset++] = pix;
    }
  }

  protected class HalfRenderer extends Renderer {

    public void render() {
      int addr = maTranslate[crtc.getMA()] + ((crtc.getRA() & 0x07) << 11);
      int val = ((memory[addr] & 0xff) << 3) + ((memory[addr + 1] & 0xff) << 11);  // Base always even
      for (int i = 0; i < 8; i++)
        pixels[offset++] = inks[halfMap[val++]];
    }

  }

  protected class FullRenderer extends Renderer {

    public void render() {
      int addr = maTranslate[crtc.getMA()] + ((crtc.getRA() & 0x07) << 11);
      int val = ((memory[addr] & 0xff) << 4) + ((memory[addr + 1] & 0xff) << 12);  // Base always even
      for (int i = 0; i < 16; i++)
        pixels[offset++] = inks[fullMap[val++]];
    }

  }
  
  protected class HalfStartRenderer extends Renderer {
    
    public void render() {
      endPix = 8 - (((hPos - HOFFSET) >> 13) & 0x07);
      if (renderer == borderRenderer) {
        int pix = inks[16];
        for (int i = endPix; i < 8; i++)
          pixels[offset++] = pix;
      }
      else if (renderer == defRenderer) {
        int addr = maTranslate[crtc.getMA()] + ((crtc.getRA() & 0x07) << 11);
        int val = ((memory[addr] & 0xff) << 3) + ((memory[addr + 1] & 0xff) << 11) + endPix;  // Base always even
        for (int i = endPix; i < 8; i++)
          pixels[offset++] = inks[halfMap[val++]];
      }
      else {
        int pix = inks[32];
        for (int i = endPix; i < 8; i++)
          pixels[offset++] = pix;
      }
    }
    
  }
  
  protected class HalfEndRenderer extends Renderer {
    
    public void render() {
      if (renderer == borderRenderer) {
        int pix = inks[16];
        for (int i = 0; i < endPix; i++)
          pixels[offset++] = pix;
      }
      else if (renderer == defRenderer) {
        int addr = maTranslate[crtc.getMA()] + ((crtc.getRA() & 0x07) << 11);
        int val = ((memory[addr] & 0xff) << 3) + ((memory[addr + 1] & 0xff) << 11);  // Base always even
        for (int i = 0; i < endPix; i++)
          pixels[offset++] = inks[halfMap[val++]];
      }
      else {
        int pix = inks[32];
        for (int i = 0; i < endPix; i++)
          pixels[offset++] = pix;
      }
    }
    
  }
  
  protected class FullStartRenderer extends Renderer {
    
    public void render() {
      endPix = 16 - (((hPos - HOFFSET) >> 12) & 0x0f);
      if (renderer == borderRenderer) {
        int pix = inks[16];
        for (int i = endPix; i < 16; i++)
          pixels[offset++] = pix;
      }
      else if (renderer == defRenderer) {
        int addr = maTranslate[crtc.getMA()] + ((crtc.getRA() & 0x07) << 11);
        int val = ((memory[addr] & 0xff) << 4) + ((memory[addr + 1] & 0xff) << 12) + endPix;  // Base always even
        for (int i = endPix; i < 16; i++)
          pixels[offset++] = inks[fullMap[val++]];
      }
      else {
        int pix = inks[32];
        for (int i = endPix; i < 16; i++)
          pixels[offset++] = pix;
      }
    }
    
  }

  protected class FullEndRenderer extends Renderer {
    
    public void render() {
      if (renderer == borderRenderer) {
        int pix = inks[16];
        for (int i = 0; i < endPix; i++)
          pixels[offset++] = pix;
      }
      else if (renderer == defRenderer) {
        int addr = maTranslate[crtc.getMA()] + ((crtc.getRA() & 0x07) << 11);
        int val = ((memory[addr] & 0xff) << 4) + ((memory[addr + 1] & 0xff) << 12);  // Base always even
        for (int i = 0; i < endPix; i++)
          pixels[offset++] = inks[fullMap[val++]];
      }
      else {
        int pix = inks[32];
        for (int i = 0; i < endPix; i++)
          pixels[offset++] = pix;
      }
    }
    
  }

}