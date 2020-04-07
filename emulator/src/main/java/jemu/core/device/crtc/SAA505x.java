/*
 * SAA505x.java
 *
 * Created on 22 July 2006, 19:09
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.core.device.crtc;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class SAA505x extends CRTC {
   
  protected static final int[] RGB = {
    0x000000, 0xff0000, 0x00ff00, 0xffff00, 0x0000ff, 0xff00ff, 0x00ffff, 0xffffff
  }; 
  protected static final int FLASH_ON_TIME = 37;
  protected static final int FLASH_OFF_TIME = 13;
  
  protected static final byte[] BLANK_ROW = { 0, 0, 0, 0, 0, 0 };
  
  protected byte[][][] romData     = new byte[96][10][6];  // Original data
  protected byte[][][] displayData = new byte[96 * 3][][]; // Converted data
  
  protected int width       = 12;
  protected int height      = 20;
  protected int heightShift = 1;
  
  protected int current   = 0;                // Current display character
  protected int previous  = 0;                // The previous character
  protected int prev2     = 0;                // The one before
  protected int prev3     = 0;                // The one before that (3 chars delay)
  protected int graphics  = 0;                // Graphics 0 = text, 96 = solid, 192 = separated
  protected int separated = 96;               // 967 for solid, 192 for separated
  protected int flashMask = 0xff;             // Mask to zero character when flash on
  protected int flashCount = FLASH_ON_TIME;   // Counter for flashing
  protected int flashOn = 0xff;               // Mask to use for frame
  protected int last = 0;                     // Last graphics character
  protected int held = 0;                     // Held graphics character
  protected boolean hold = false;             // Hold graphics characters?
  protected int row = 0;                      // Character row counter
  protected int singleRow = 0;                // Single height row
  protected int doubleRow = 0;                // Double height row
  protected int rowAdd = 0;                   // Amount to add to row (CRS)
  protected boolean doubleNow  = false;       // Double height now?
  protected boolean doubleThis = false;       // Had double-height this row?
  protected boolean doubleLast = false;       // Last row was first row of double-height?
  
  protected int[] inks = { RGB[0], RGB[7], 0x7f7f7f };  // Default colours
  protected boolean lose = false;             // Display window
  protected boolean newLose = false;          // Next value for LOSE
  protected boolean dew = false;              // Display enable window
  protected int crs = 0;                      // Set to 0 when CRS high, 1 when CRS low
  protected boolean delay = true;             // Delay character output by 2us (actually should be 2.6)
  protected int loseDelay = 0;                // Delay for LOSE signal
  
  /** Creates a new instance of SAA505x */
  public SAA505x() {
    super("SAA505x");
  }
  
  protected final void mix() {
    inks[2] = (inks[0] & 0x7f7f7f) + (inks[1] & 0x7f7f7f);  // This only works because they are full bright colours
  }
  
  public void setCharacterSize(int width, int height) {
    if ((width == 6 || width == 8 || width == 12 || width == 16) && (height == 10 || height == 20))
    {
      this.width = width;
      this.height = height;
      heightShift = height == 10 ? 0 : 1;
    }
    else
      throw new RuntimeException("Width must be 6,8,12 or 16 and height must be 10 or 20");
    setDisplayData();
  }
  
  protected byte getPix(int ch, int y, int x) {
    return x >= 0 && x < 6 && y >= 0 && y < 10 ? romData[ch][y][x] : 0;
  }
  
  public void setCharacterROM(byte[] data) {
    // Data should be 0x360 long
    int index = 0;
    for (int ch = 0; ch < 96; ch++) {
      for (int row = 0; row < 9; row++) {
        for (int pix = 0; pix < 6; pix++) {
          byte val = (byte)(((data[index] & 0x1f) >> (5 - pix)) & 0x01);
          romData[ch][row][pix] = val;
        }
        index++;
      }
      romData[ch][9] = BLANK_ROW;
    }
    setDisplayData();
  }
  
  protected byte[] expandTo(byte[] source, int width) {
    byte[] result;
    if (source.length == width)
      result = source;
    else {
      result = new byte[width];
      int index = 0;
      int count = 0;
      for (int i = 0; i < width; i++) {
        result[i] = source[index];
        count += source.length;
        if (count >= width) {
          index++;
          count -= width;
        }
      }
    }
    return result;
  }
   
  protected void setDisplayData() {
    System.out.println("setDisplayData: " + width + "x" + height);
    for (int ch = 0; ch < 96; ch++) {
      // TODO: Smaller fonts could be anti-aliased for better quality - Use 12x20 and scale down!!!
      if (width == 6 && height == 10)              // 6 x 10
        displayData[ch] = romData[ch];
      else {
        displayData[ch] = new byte[height][];
        if (height == 10) {                        // 8 x 10, 12 x 10, 16 x 10
          for (int row = 0; row < 10; row++)
            displayData[ch][row] = expandTo(romData[ch][row],width);
        }
        else {                                     // 6 x 20, 8 x 20, 12 x 20, 16 x 20 - First create as 12
          // As per documentation - Even rows do row before, odd do row after
          for (int row = 0; row < 10; row++) {
            displayData[ch][row * 2] = new byte[12];
            displayData[ch][row * 2 + 1] = new byte[12];
            for (int pix = 0; pix < 6; pix++) {
              // Even rows
              byte pixel = getPix(ch,row,pix);
              byte left = pixel, right = pixel;
              if (pixel == 0) {
                // Even row - use row before
                if (getPix(ch,row,pix - 1) == 1 && getPix(ch,row - 1,pix - 1) == 0)
                  left = getPix(ch,row - 1,pix);
                if (getPix(ch,row,pix + 1) == 1 && getPix(ch,row -1,pix + 1) == 0)
                  right = getPix(ch,row - 1,pix);
              }
              displayData[ch][row * 2][pix * 2] = left;
              displayData[ch][row * 2][pix * 2 + 1] = right;
              // Odd 
              left = right = pixel;
              if (pixel == 0) {
                // Odd row - use row after
                if (getPix(ch,row,pix - 1) == 1 && getPix(ch,row + 1,pix - 1) == 0)
                  left = getPix(ch,row + 1,pix);
                if (getPix(ch,row,pix + 1) == 1 && getPix(ch,row + 1,pix + 1) == 0)
                  right = getPix(ch,row + 1,pix);
              }
              displayData[ch][row * 2 + 1][pix * 2] = left;
              displayData[ch][row * 2 + 1][pix * 2 + 1] = right;
            }
          }
          
          if (width == 16)
            for (int row = 0; row < 20; row++)
              displayData[ch][row] = expandTo(displayData[ch][row],16);
          
          else if (width != 12) {
            // Shrink to 6 wide
            for (int row = 0; row < 20; row++) {
              byte[] data = new byte[6];
              for (int pix = 0; pix < 6; pix++) {
                int pixel = displayData[ch][row][pix * 2] + displayData[ch][row][pix * 2 + 1];
                data[pix] = (byte)(pixel == 0 ? 0 : 3 - pixel);  // 1 is full, 2 is half
              }
              displayData[ch][row] = expandTo(data,width);
            }
          }
        }
      }
    }
    // Create graphics data
    for (int ch = 0; ch < 96; ch++) {
      if (ch >= 32 && ch < 64)
        displayData[ch + 96] = displayData[ch + 192] = displayData[ch];
      else {
        byte[][] solid = displayData[ch + 96] = new byte[height][];
        byte[][] sep   = displayData[ch + 192] = new byte[height][];
        int bits = (ch & 0x1f) | ((ch & 0x40) >> 1);  // Get the 6 graphics bits
        byte[] pixels = new byte[6];
        int y = 0;       
        for (int row = 0; row < 3; row++) {
          pixels[0] = pixels[1] = pixels[2] = (byte)(bits & 0x01);
          bits >>= 1;
          pixels[3] = pixels[4] = pixels[5] = (byte)(bits & 0x01);
          bits >>= 1;
          solid[y] = expandTo(pixels,width);
          pixels[0] = pixels[3] = 0;
          sep[y++] = expandTo(pixels,width);
          if (height == 20) {
            solid[y] = solid[y - 1];
            sep[y] = sep[y - 1];
            y++;
          }
          int count = row == 1 ? 3 : 2;  // Number of extra rows
          while (count-- > 0) {
            solid[y] = solid[y - 1];
            sep[y] = count == 0 ? expandTo(BLANK_ROW,width) : sep[y - 1];
            y++;
            if (height == 20) {
              solid[y] = solid[y - 1];
              sep[y] = sep[y - 1];
              y++;
            }
          }
        }
      }
    }
  }
  
  public final void setCharacter(int value) {
    if (!delay)
      previous = value & 0x7f;
    else if (loseDelay != 0)
      if (--loseDelay == 0)
        toggleLose();
    if (lose) {
      if (previous < 32) {
        current = hold ? held : 0;
        switch(previous) {
          case 1:  inks[1] = RGB[1]; graphics = 0; mix(); break;
          case 2:  inks[1] = RGB[2]; graphics = 0; mix(); break;
          case 3:  inks[1] = RGB[3]; graphics = 0; mix(); break;
          case 4:  inks[1] = RGB[4]; graphics = 0; mix(); break;
          case 5:  inks[1] = RGB[5]; graphics = 0; mix(); break;
          case 6:  inks[1] = RGB[6]; graphics = 0; mix(); break;
          case 7:  inks[1] = RGB[7]; graphics = 0; mix(); break; 

          case 8:  flashMask = flashOn;                    break;
          case 9:  flashMask = 0xff;                       break;

          case 12: row = (singleRow << heightShift) + rowAdd; doubleNow = false; break;
          case 13: row = doubleRow;              doubleThis = doubleNow = true;  break;

          case 17: inks[1] = RGB[1]; graphics = separated; mix(); break;
          case 18: inks[1] = RGB[2]; graphics = separated; mix(); break;
          case 19: inks[1] = RGB[3]; graphics = separated; mix(); break;
          case 20: inks[1] = RGB[4]; graphics = separated; mix(); break;
          case 21: inks[1] = RGB[5]; graphics = separated; mix(); break;
          case 22: inks[1] = RGB[6]; graphics = separated; mix(); break;
          case 23: inks[1] = RGB[7]; graphics = separated; mix(); break;

          case 24: inks[2] = inks[1] = inks[0]; break; // Conceal (is this correct? Will work unless code 28 is after it)

          case 25: separated = 96;  if (graphics != 0) graphics = separated; break;
          case 26: separated = 192; if (graphics != 0) graphics = separated; break;

          case 28: inks[0] = RGB[0]; mix();         break;
          case 29: inks[2] = inks[0] = inks[1];     break;

          case 30: hold = true;                     break;
          case 31: hold = false;                    break;
        }
      }
      else {
        current = ((previous - 32) & flashMask) + graphics;
        if (graphics != 0 && (current & 0x20) != 0)
          held = current;
      }
    }
    if (delay) {
      previous = prev2;
      prev2 = prev3;
      prev3 = value & 0x7f;
    }
  }
  
  public final int setPixels(int[] pixels, int offset) {
    byte[] data = displayData[current][row];
    for (int i = 0; i < width; i++)
      pixels[offset++] = inks[data[i]];
    return offset;
  }
  
  public final void setPixels(int[] pixels, int offset, int start, int end) {
    byte[] data = displayData[current][row];
    for (int i = start; i < end; i++)
      pixels[offset++] = inks[data[i]];
  }
  
  /**
   * The LOSE signal on the SAA resets the control code flip-flops at the start of each display line.
   * This also disables output when set low (after 2.6us - not currently implemented).
   */
  public final void setLOSE(boolean value) {
    if (newLose != value) {
      newLose = value;
      if (delay) loseDelay = 3;
      else toggleLose();
    }
  }
  
  protected final void toggleLose() {
    if (lose = newLose) {
      // Clear control-codes
      inks[0] = RGB[0];
      inks[1] = RGB[7];
      graphics = held = 0;
      separated = 96;
      flashMask = 0xff;
      doubleNow = false;
    }
    else {
      current = 0;
      if (++singleRow == 10) {
        singleRow = 0;
        row = rowAdd;
        doubleLast = doubleThis && !doubleLast;
        doubleRow = doubleLast ? 5 << heightShift : 0;
        doubleThis = false;
      }
      else {
        if (height == 10)
          doubleRow += singleRow & 0x01;
        else
          doubleRow++;
        row = (singleRow << heightShift) + rowAdd;
      }
    }
  }
  
  /**
   * The DEW signal is used to reset the internal row address counter and for flash timing.
   */
  public final void setDEW(boolean value) {
    if (dew != value) {
      if (!(dew = value)) {
        row = singleRow = doubleRow = 0;
        doubleLast = doubleThis = doubleNow = false;
        if (--flashCount == 0) {
          flashOn = flashOn ^ 0xff;
          flashCount = flashOn == 0 ? FLASH_OFF_TIME : FLASH_ON_TIME;
        }
      }
    }
  }
  
  public final void setCRS(boolean value) {
    crs = value ? 1 : 0;
    rowAdd = height == 10 ? 0 : crs;
    if (!doubleNow) row = (singleRow << heightShift) + rowAdd;
  }
  
}
