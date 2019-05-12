package jemu.system.vz;

import java.awt.*;
import jemu.core.device.*;
import jemu.core.renderer.*;

/**
 * Title:        JEMU
 * Description:  The Java Emulation Platform
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class SimpleRenderer extends Renderer {
  
  protected static final int WIDTH     = 256;
  protected static final int HEIGHT    = 192;

  protected static final int BLACK     = 0xff000000;
  protected static final int GREEN     = 0xff00e000;
  protected static final int YELLOW    = 0xffd0ff00;
  protected static final int BLUE      = 0xff0000ff;
  protected static final int RED       = 0xffff0000;
  protected static final int BUFF      = 0xffe0e090;
  protected static final int CYAN      = 0xff00ffa0;
  protected static final int MAGENTA   = 0xffff00ff;
  protected static final int ORANGE    = 0xfff07000;
  protected static final int DK_GREEN  = 0xff004000;
  protected static final int BR_GREEN  = 0xff00e018;
  protected static final int DK_ORANGE = 0xff401000;
  protected static final int BR_ORANGE = 0xffffc418;

  protected static final int[] TEXT_FG =
    { GREEN, YELLOW, BLUE, RED, BUFF, CYAN, MAGENTA, ORANGE };

  protected static final int[] GREEN_GR = { GREEN, YELLOW, BLUE, RED };
  protected static final int[] BUFF_GR = { BUFF, CYAN, ORANGE, MAGENTA };

  protected static int[] greenText  = new int[0x6000];
  protected static int[] orangeText = new int[0x6000];

  protected static int[] greenGraphics = new int[0x800];
  protected static int[] buffGraphics = new int[0x800];
  static {
    int offs = 0;
    for (int i = 0; i < 256; i++) {
      for (int pix = 0; pix < 4; pix++) {
        int colour = ((i << (pix << 1)) & 0xc0) >> 6;
        greenGraphics[offs + 1] = greenGraphics[offs] = GREEN_GR[colour];
        buffGraphics[offs + 1] = buffGraphics[offs] = BUFF_GR[colour];
        offs += 2;
      }
    }
  };

  protected int vdcLatch = 0x00;
  protected int mapMult = 96;
  protected int[] pixelMap = greenText;
  protected int border = GREEN;
  protected int borderMask = BLACK;

  public SimpleRenderer() {
    super("VZ Simple Screen Renderer");
  }
  
  protected SimpleRenderer(String type) {
    super(type);
  }
  
  public void setVerticalAdjustment(int value) { }  // Does nothing here, used in FullRenderer

  public static void setFontData(byte[] data) {
    int offs = 0;
    for (int i = 0; i < WIDTH; i++) {
      int fg = TEXT_FG[(i >> 4) & 0x07];
      for (int y = 0; y < 12; y++) {
        int value;
        if (i >= 128) {
          int bits = (y < 6 ? i >> 2 : i) & 0x03;
          value = (bits & 0x02) != 0 ? 0x0f : 0x00;
          if ((bits & 0x01) != 0)
            value |= 0xf0;
        }
        else if (i < 64)
          value = data[i * 12 + y] & 0xff;
        else
          value = (data[(i & 0x3f) * 12 + y] & 0xff) ^ 0xff;
        for (int x = 0; x < 8; x++) {
          if (i >= 128)
//            greenText[offs] = orangeText[offs] = (value & 0x01) != 0 ? fg : BLACK;
        	  greenText[offs] = orangeText[offs] = (value & 0x80) != 0 ? fg : BLACK;
          else {
//            greenText[offs] = (value & 0x01) == 0 ? DK_GREEN : BR_GREEN;
//            orangeText[offs] = (value & 0x01) == 0 ? DK_ORANGE : BR_ORANGE;
            greenText[offs] = (value & 0x80) == 0 ? DK_GREEN : BR_GREEN;
            orangeText[offs] = (value & 0x80) == 0 ? DK_ORANGE : BR_ORANGE;
          }
          offs++;
          value <<= 1;
        }
      }
    }
  }

  public void setVDCLatch(int value) {
    switch(value & 0x18) {
      case 0x00: pixelMap = greenText;     border = GREEN; borderMask = BLACK; setMapMult(96); break;
      case 0x08: pixelMap = greenGraphics; border = GREEN; borderMask = -1;    setMapMult(8);  break;
      case 0x10: pixelMap = orangeText;    border = BUFF;  borderMask = BLACK; setMapMult(96); break;
      case 0x18: pixelMap = buffGraphics;  border = BUFF;  borderMask = -1;    setMapMult(8);  break;
    }
  }
  
  public void setMapMult(int value) {
    mapMult = value;
  }

  public void renderScreen(VZMemory memory) {
    // TODO: Render Border etc.
    int offs = 0;
    int width = display.getImageWidth();
    byte[] mem = memory.getMemory();
    int addr = 0x7000;
    if (mapMult == 8) {
      for (int y = 0; y < 64; y++) {
        int start = offs;
        for (int x = 0; x < 32; x++) {
          int pix = (mem[addr++] & 0xff) * mapMult;
          int dst = offs;
          for (int row = 0; row < 3; row++) {
//            for (int i = 0; i < 8; i++)
//              pixels[dst + i] = pixelMap[pix++];
            System.arraycopy(pixelMap,pix,pixels,dst,8);
            dst += width;
          }
          offs += 8;
        }
        offs = start + width * 3;
      }
    }
    else {
      for (int y = 0; y < 16; y++) {
        int start = offs;
        for (int x = 0; x < 32; x++) {
          int ch = (mem[addr++] & 0xff) * mapMult;  // 12 * 8
          int dst = offs;
          for (int row = 0; row < 12; row++) {
            System.arraycopy(pixelMap,ch,pixels,dst,8);
            ch += 8;
            dst += width;
          }
          offs += 8;
        }
        offs = start + width * 12;
      }
    }
    display.updateImage(true);
  }
  
  public Dimension getDisplaySize() {
    return new Dimension(WIDTH,HEIGHT);
  }
  
  public int setData(int value) {
    return value;
  }

}