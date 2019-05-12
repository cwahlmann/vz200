package jemu.system.zx;

import java.awt.event.*;
import jemu.core.*;
import jemu.core.device.keyboard.*;

/**
 * Title:        JEMU
 * Description:  The Java Emulation Platform
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class Keyboard extends MatrixKeyboard {

  protected static final int[] KEY_MAP = {
    KeyEvent.VK_SHIFT, KeyEvent.VK_Z, KeyEvent.VK_X, KeyEvent.VK_C, KeyEvent.VK_V,
    KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D, KeyEvent.VK_F, KeyEvent.VK_G,
    KeyEvent.VK_Q, KeyEvent.VK_W, KeyEvent.VK_E, KeyEvent.VK_R, KeyEvent.VK_T,
    KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5,
    KeyEvent.VK_0, KeyEvent.VK_9, KeyEvent.VK_8, KeyEvent.VK_7, KeyEvent.VK_6,
    KeyEvent.VK_P, KeyEvent.VK_O, KeyEvent.VK_I, KeyEvent.VK_U, KeyEvent.VK_Y,
    KeyEvent.VK_ENTER, KeyEvent.VK_L, KeyEvent.VK_K, KeyEvent.VK_J, KeyEvent.VK_H,
    KeyEvent.VK_SPACE, KeyEvent.VK_CONTROL, KeyEvent.VK_M, KeyEvent.VK_N, KeyEvent.VK_B
  };

  protected int[] bytes = new int[8];

  public Keyboard() {
    super("ZX Keyboard",6,8);
    for (int i = 0; i < KEY_MAP.length; i++)
      if (KEY_MAP[i] != -1)
        addKeyMapping(KEY_MAP[i],i % 5,i / 5);

    // For MS JVM, these keys have different codes
    addKeyMapping(0xbe,1,7);  // VK_PERIOD = 0xbe

    // Backspace mapped to SHIFT+0
    addShifted(KeyEvent.VK_BACK_SPACE,0,4);

    // Escape mapped to SHIFT+SPACE
    addShifted(KeyEvent.VK_ESCAPE,0,7);

    // Cursor keys
    addShifted(KeyEvent.VK_LEFT,4,3);
    addShifted(KeyEvent.VK_RIGHT,2,4);
    addShifted(KeyEvent.VK_UP,3,4);
    addShifted(KeyEvent.VK_DOWN,4,4);
    
    // Comma and full stop
    addControl(KeyEvent.VK_COMMA,3,7);
    addControl(KeyEvent.VK_PERIOD,2,7);
    reset();
  }

  public void reset() {
    for (int i = 0; i < bytes.length; i++)
      bytes[i] = 0xff;
    super.reset();
  }

  protected void addShifted(int key, int col, int row) {
    addKeyMapping(key,0,0);     // SHIFT (Caps Shift)
    addKeyMapping(key,col,row);
  }
  
  protected void addControl(int key, int col, int row) {
    addKeyMapping(key,1,7);    // CONTROL (Symbol Shift)
    addKeyMapping(key,col,row);
  }

  protected void keyChanged(int col, int row, int oldValue, int newValue) {
//    System.out.println("keyChanged: " + col + ", " + row + " " + oldValue + " => " + newValue);
    if (oldValue == 0) {
      if (newValue != 0)
        bytes[row] &= (0x01 << col) ^ 0xff;
    }
    else if (newValue == 0)
      bytes[row] |= (0x01 << col);
//    System.out.println("Bytes[" + row + "] = " + Util.hex((byte)bytes[row]));
  }

  public int readPort(int port) {
    int result = 0xff;
    int mask = 0x0100;
    for (int row = 0; row < 8; row++) {
      if ((port & mask) == 0)
        result &= bytes[row];
      mask <<= 1;
    }
    return result;
  }

}