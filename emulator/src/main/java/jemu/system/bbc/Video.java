/*
 * Video.java
 *
 * Created on 24 July 2006, 18:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.system.bbc;

import java.awt.*;
import jemu.core.device.crtc.*;
import jemu.core.device.io.*;
import jemu.core.device.memory.*;
import jemu.core.renderer.*;

/**
 *
 * @author Richard
 */
public class Video extends MonitorRenderer implements CRTCListener {
  
    
  protected static final int[] ADDERS = { 0x4000, 0x6000, 0x3000, 0x5800 };
  protected static final int[] INKS   = {
    0x000000, 0xff0000, 0x00ff00, 0xffff00, 0x0000ff, 0xff00ff, 0x00ffff, 0xffffff
  };
  protected static final int[] BLANK_8 = { 0, 0, 0, 0, 0, 0, 0, 0 };

  // Cursor mask rotate bits
  protected static final byte[] CURSOR_MASKS = { 0x00, 0x0C, 0x02, 0x0e, 0x01, 0x0d, 0x03, 0x0f };
  
  protected static final Rectangle IMAGE_DOUBLED = new Rectangle(0,0,768,270);
  
  protected BBC bbc;
  protected Basic6845 crtc;
  protected R6522 sysVIA;
  protected SAA505x saa;
  protected byte[] memory;
  
  protected int[] pixels;
  
  protected byte[][][] fullMaps = new byte[8][][];     // Each contains 8 pixels
  protected byte[][][] halfMaps = new byte[8][][];
  
  // For smaller run-time, this may be reduced to use only the to 8 bits
  // Bits 15 and 14 represent C1 and C0 from IC32
  protected int[] maTranslate = new int[0x10000];       // For 4 different ULA adder settings
  protected int[][] palette = new int[2][16];           // Current palette
  
  protected byte[][] map;                               // Current Mode map
  protected int[] pal = palette[0];                     // Current palette
  
  protected int addMA = 0;
  protected int crtcMask = 0;                           // Cycle Mask for CRTC (1MHz or 2MHz)
  protected int cycleCount = 0;                         // Cycle Count for crtc
  protected boolean teletext = false;
  protected int data;                                   // Data read from memory on CRTC cycle
  protected int index = 0;                              // Current display index
  protected int scanStart = 0;
  protected int rowAdd = 768;                           // Number of byte between scans
  protected boolean rendering = false;                  // Stops rendering if frames need to be skipped
  protected Rectangle imageRect;                        // Image Rectangle for rendering
  protected boolean large = true;                       // Full Size Display?
  protected int pixPerCycle = 4;                        // Number of pixels per cycle
  protected int selectedMap = 0;                        // Currently used map
  protected int selCursorMask = 0;                      // Selected Cursor bits
  protected int cursorMask  = 0;                        // Cursor bits
  
  /** Creates a new instance of Video */
  public Video(BBC bbc) {
    super("BBC Video Emulation");
    setCycleFrequency(2000000);
    setVerticalAdjust(5);
    this.bbc = bbc;
    saa = bbc.saa;
    crtc = bbc.crtc;
    sysVIA = bbc.sysVIA;
    crtc.setCRTCListener(this);
    for (int c1c0 = 0; c1c0 < 4; c1c0++) {                     // add represents c0 and c1 from IC32 latch
      int top = c1c0 << 14;
      for (int ma = 0; ma < 0x4000; ma++) {
        int adder = (ma & 0x1000) == 0 ? 0 : ADDERS[c1c0];     // MA12 high, use adders
        int sum = ((ma << 3) + adder) & 0x7800;                // Outputs S1 .. S4 of IC39 - S4 used in both modes
        if ((ma & 0x2000) != 0)                                // MA13 - Teletext addressing mode
          maTranslate[top + ma] = (sum & 0x4000) | 0x3c00 |    // Bit 14 = S4, 13 .. 10 tied high, 
            (ma & 0x3ff);                                      // other from MA (1K)
        else
          maTranslate[top + ma] = sum | ((ma << 3) & 0x7f8);   // Bits 14 ..11 from adder, Bits 10..3 from MA, RA added later
      }
    }
    
    for (int rate = 0; rate < 4; rate++) {
      fullMaps[rate] = new byte[512][8];
      fullMaps[rate + 4] = new byte[256][8];
      int steps = 8 >> rate;
      for (int i = 0; i < 256; i++) {
        int data = i;
        int count = 0;
        for (int pix = 0; pix < 16; pix++) {
          byte pal = getPaletteIndex(data);
          if (pix < 8) {
            fullMaps[rate][i][pix] = pal;
            fullMaps[rate + 4][i][pix] = pal;
          }
          else
            fullMaps[rate][i + 256][pix - 8] = pal;
          if (++count == steps) {
            count = 0;
            data = (data << 1) | 0x01;
          }
        }
      }
    }
    for (int rate = 0; rate < 8; rate++) {
      byte[][] map = fullMaps[rate];
      halfMaps[rate] = new byte[map.length][4];
      for (int i = 0; i < map.length; i++) {
        for (int pix = 0; pix < 4; pix++)
          halfMaps[rate][i][pix] = map[i][pix << 1];
      }
    }
    map = large ? fullMaps[0] : halfMaps[0];
    setLarge(false);
  }
  
  public void setLarge(boolean value) {
    if (large != value) {
      large = value;
      index = scanStart = 0;  // Ensure no array out of bounds
      pixPerCycle = large ? 8 : 4;
      map = large ? fullMaps[selectedMap] : halfMaps[selectedMap];
      if (large)
        saa.setCharacterSize(16,20);
      else
        saa.setCharacterSize(8,20);
    }
  }
  
  public void setMemory(byte[] value) {
    memory = value;
  }
  
  protected byte getPaletteIndex(int data) {
    return (byte)(((data & 0x80) >> 4)  | ((data & 0x20) >> 3) |
      ((data & 0x08) >> 2) | ((data & 0x02) >> 1));
  }
  
  public void setPixels(int[] value) {
    pixels = value;
  }
  
  public void cycle() {
    if ((cycleCount & crtcMask) == 0) {
      cursorMask >>= 1;
      crtc.cycle();
      if (rendering) {
        int ma = crtc.ma + addMA;
        if ((ma & 0x2000) == 0)
          data = memory[maTranslate[ma] + (crtc.ra & 0x07)] & 0xff;
        else if ((cycleCount & 0x01) == 0)
          saa.setCharacter(data = memory[maTranslate[ma]] & 0xff);
        else
          data = memory[maTranslate[ma]] & 0xff;
      }
    }
    if (rendering && monitorLine >= 0 && monitorLine < 270 && hPos >= 0x94000 && hPos < 0x394000) {
      if (teletext) {
        if ((cycleCount & 0x01) == 0)
          saa.setPixels(pixels,index,0,pixPerCycle);
        else
          saa.setPixels(pixels,index,pixPerCycle,pixPerCycle << 1);
      }
      else if (crtc.hDisp) {
        if ((crtc.ra & 0x08) == 0) {
          byte[] pals = map[data];
          for (int pix = 0; pix < pixPerCycle; pix++)
            pixels[index + pix] = pal[pals[pix]];
          data |= 256;
        }
        else
          System.arraycopy(BLANK_8,0,pixels,index,pixPerCycle);
      }
      else
        System.arraycopy(BLANK_8,0,pixels,index,pixPerCycle);
      if ((cursorMask & 0x01) != 0)
        for (int i = 0; i < 8; i++)
          pixels[index + i] ^= 0xffffff;
      index += pixPerCycle;
    }
    cycleCount++;
    clock();
  }
  
  protected long lastCycles;
  
  public void vSyncStart() {
    sysVIA.setCA1(true);
    saa.setDEW(true);
    super.vSyncStart();
    //System.out.print(" VCC: " + crtc.getVCC() + ", VLC: " + crtc.getVLC() + ", HCC: " + crtc.getHCC() +
    //    " Cycles: " + (bbc.cpu.getCycles() - lastCycles));
    //lastCycles = bbc.cpu.getCycles();
  }
  
  public void vSyncEnd() {
    saa.setDEW(false);
    super.vSyncEnd();
    sysVIA.setCA1(false);
  }
  
  public void vSync(boolean interlace) {
    if (rendering)
      imageRect = large && rowAdd == 768 ? IMAGE_DOUBLED : null;
    if (large && crtc.isInterlaceVideo()) {
      scanStart = (crtc.getFrame() ^ 0x01) * 768;
      rowAdd = 768 * 2;
    }
    else {
      scanStart = 0;
      rowAdd = pixPerCycle * 768 / 8;
    }
    index = scanStart;
    bbc.vSync();
  }
  
  public void vDispStart() {
    //System.out.print(" " + monitorLine);
  }
  
  public void hDispEnd() {
    saa.setLOSE(false);
  }
  
  public void hDispStart() {
    //System.out.print(" LOSE: VCC=" + crtc.getVCC() + ", VLC=" + crtc.getVLC());
    saa.setCRS((crtc.ra & 0x01) != 0);
    saa.setLOSE(true);
  }
  
  public final void hSync() {
    if (monitorLine >= 0 && monitorLine < 270) {
      index = scanStart;
      scanStart += rowAdd;
    }
  }
  
  int cw;
  
  public void writePort(int port, int value) {
    if (port == 0) {
      // Control write
      // Bit 0 = flash colour
      // Bits 2,3 ULA shift rate (8, 4, 2 or 1)
      // Bit 4 = CRTC clock rate
      // Bit 7 - master cursor size
      // Bits 5 and 6 - cursor width in bytes
      teletext = (value & 0x02) != 0;
      crtcMask = (~value >> 4) & 0x01;
      selectedMap = (value >> 2) & 0x07;
      map = large ? fullMaps[selectedMap] : halfMaps[selectedMap];
      pal = palette[value & 0x01];
      selCursorMask = CURSOR_MASKS[(value >> 5) & 0x07];
    }
    else {
      // Palette write
      int rgb = (value & 0x07) ^ 0x07;
      int log = value >> 4;
      palette[0][log] = INKS[rgb];
      palette[1][log] = (value & 0x08) == 0 ? INKS[rgb]: INKS[rgb ^ 0x07];
    }
  }
  
  public void setRendering(boolean value) {
    rendering = value;
  }
  
  public final void setAddMA(int value) {
    addMA = value;
  }
  
  public Rectangle getImageRect() {
    return imageRect;
  }
  
  public final void cursor() {
    cursorMask = selCursorMask;
  }
  
}
