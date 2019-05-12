/*
 * VideoULA.java
 *
 * Created on 31 August 2006, 17:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.system.spectrum;

import java.awt.*;
import jemu.core.renderer.*;
import jemu.core.*;

/**
 *
 * @author Richard
 */
public class VideoULA extends Renderer {
  
  protected static Dimension IMAGE_SIZE = new Dimension(256 + 16,192 + 16);
  protected static final int NORMAL_MASK = 0xc0c0c0;
  protected static final int BRIGHT_MASK = 0xffffff;
  
  protected static int[] PALETTE = {
    0x000000, 0x0000ff, 0xff0000,  0xff00ff, 0x00ff00, 0x00ffff, 0xffff00, 0xffffff
  };
  
  protected ByteRenderer[] NO_REND     = new ByteRenderer[34];
  protected ByteRenderer[] BORDER_REND = new ByteRenderer[34];
  protected ByteRenderer[] SCREEN_REND = new ByteRenderer[34];
  
  protected int[][][] maps = new int[2][65536][8];  // One map for each flash state
  
  protected Spectrum spectrum;
  protected Memory memory;
  protected byte[] mem;
  protected byte col = 0;             // column 
  protected int[][] map = maps[0];
  protected int addr = 0x4000;
  protected int attr = 0x5800;
  protected int offset = 0;
  protected int row = -8;
  protected int border = 0;           // Border colour
  protected ByteRenderer[] rend;      // Renderers for current line
  protected boolean rendering;
  
  /** Creates a new instance of VideoULA */
  public VideoULA(Spectrum spectrum) {
    super("ZX Spectrum Video ULA");
    this.spectrum = spectrum;
    ByteRenderer nothing = new ByteRenderer();
    ByteRenderer border = new BorderRenderer();
    ByteRenderer display = new DisplayRenderer();
    for (int col = 0; col < 34; col++) {
      NO_REND[col]     = nothing;
      BORDER_REND[col] = border;
      SCREEN_REND[col] = col > 0 && col < 33 ? display : border;
    }
    for (int attr = 0; attr < 256; attr++) {
      int mask = (attr & 0x40) != 0 ? BRIGHT_MASK : NORMAL_MASK;
      int fg = PALETTE[attr & 0x07] & mask;
      int bg = PALETTE[(attr & 0x38) >> 3] & mask;
      boolean flash = (attr & 0x80) != 0;
      for (int val = 0; val < 256; val++) {
        int index = (attr << 8) + val;
        for (int pix = 0; pix < 8; pix++) {
          mask = (val & (0x80 >> pix));
          maps[0][index][pix] = mask == 0 ? bg : fg;
          maps[1][index][pix] = flash ? (mask == 0 ? fg : bg) : (mask == 0 ? bg : fg);
        }
      }
    }
    reset();
  }
  
  public void reset() {
    row = -8;
    col = 0;
    addr = 0x4000;
    attr = 0x5800;
    offset = 0;
    rend = BORDER_REND;
  }
  
  public void setBorder(int value) {
    border = PALETTE[value] & NORMAL_MASK;
  }
  
  public void setRendering(boolean value) {
    rendering = value;
  }
  
  public Dimension getDisplaySize(boolean large) {
    return IMAGE_SIZE;
  }
  
  public void setMemory(Memory value) {
    memory = value;
    mem = memory.getMemory();  // This includes the ROM
  }
  
  public int readByte(int addr) {
    // TODO: Contention - The ZX cuts the clock to the CPU. /WAIT is tied high!
    return memory.readByte(addr);
  }
  
  public int writeByte(int addr, int value) {
    // TODO: Contention
    return memory.writeByte(addr,value);
  }
  
  public void cycle() {
    if (col < 34)
      rend[col].render();
    if (++col == 56) {
      col = 0;
      if (++row == 304) {
        offset = 0;
        row = -8;
      }
      else if (row > 0 && row < 192) {
        if ((row & 0x07) == 0) {
          if ((row & 0x3f) != 0)
            addr -= 0x700;
        }
        else {
          addr += 0x100 - 32;
          attr -= 32;
        }
      }
      else if (row == 248) {
        spectrum.vSync();
        spectrum.z80.setInterrupt(1);
        addr = 0x4000;
        attr = 0x5800;
      }
      else if (row == 250)
        spectrum.z80.clearInterrupt(1);
      rend = row >= 200 || !rendering ? NO_REND : (row >= 0 && row < 192 ? SCREEN_REND : BORDER_REND);
    }
  }
  
  public int readPort(int port) {
    return row >= 0 && row < 192 && col > 0 && col < 33 ? mem[addr] : 0xff;
  }
  
  protected class ByteRenderer {
    public void render() { }
  }
  
  protected class BorderRenderer extends ByteRenderer {
    public void render() {
      // 8 pixels of border colour
      for (int i = 0; i < 8; i++)
        pixels[offset++] = border;
    }
  }
  
  protected class DisplayRenderer extends ByteRenderer {
    public void render() {
      int disp = (mem[addr] & 0xff) | ((mem[attr] & 0xff) << 8);
      System.arraycopy(map[disp],0,pixels,offset,8);
      offset += 8;
      addr++;
      attr++;
    }
  }

}
