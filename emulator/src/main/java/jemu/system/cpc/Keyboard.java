package jemu.system.cpc;

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
    // Row 0
    KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_NUMPAD9,
    KeyEvent.VK_NUMPAD6, KeyEvent.VK_NUMPAD3, KeyEvent.VK_END, KeyEvent.VK_DECIMAL,

    // Row 1
    KeyEvent.VK_LEFT, KeyEvent.VK_ALT, KeyEvent.VK_NUMPAD7, KeyEvent.VK_NUMPAD8,
    KeyEvent.VK_NUMPAD5, KeyEvent.VK_NUMPAD1, KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD0,

    // Row 2
    KeyEvent.VK_BACK_SLASH, KeyEvent.VK_ALT_GRAPH, KeyEvent.VK_ENTER,
    KeyEvent.VK_CLOSE_BRACKET, KeyEvent.VK_NUMPAD4, KeyEvent.VK_SHIFT,
    KeyEvent.VK_BACK_QUOTE, KeyEvent.VK_CONTROL,

    // Row 3
    KeyEvent.VK_EQUALS, KeyEvent.VK_MINUS, KeyEvent.VK_OPEN_BRACKET, KeyEvent.VK_P,
    KeyEvent.VK_QUOTE, KeyEvent.VK_SEMICOLON, KeyEvent.VK_SLASH, KeyEvent.VK_PERIOD,

    // Row 4
    KeyEvent.VK_0, KeyEvent.VK_9, KeyEvent.VK_O, KeyEvent.VK_I,
    KeyEvent.VK_L, KeyEvent.VK_K, KeyEvent.VK_M, KeyEvent.VK_COMMA,

    // Row 5
    KeyEvent.VK_8, KeyEvent.VK_7, KeyEvent.VK_U, KeyEvent.VK_Y,
    KeyEvent.VK_H, KeyEvent.VK_J, KeyEvent.VK_N, KeyEvent.VK_SPACE,

    // Row 6
    KeyEvent.VK_6, KeyEvent.VK_5, KeyEvent.VK_R, KeyEvent.VK_T,
    KeyEvent.VK_G, KeyEvent.VK_F, KeyEvent.VK_B, KeyEvent.VK_V,

    // Row 7
    KeyEvent.VK_4, KeyEvent.VK_3, KeyEvent.VK_E, KeyEvent.VK_W,
    KeyEvent.VK_S, KeyEvent.VK_D, KeyEvent.VK_C, KeyEvent.VK_X,

    // Row 8
    KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_ESCAPE, KeyEvent.VK_Q,
    KeyEvent.VK_TAB, KeyEvent.VK_A, KeyEvent.VK_CAPS_LOCK, KeyEvent.VK_Z,

    // Row 9
    -1, -1, -1, -1, -1, -1, -1, KeyEvent.VK_BACK_SPACE
  };

  protected int[] bytes = new int[16];
  protected int row = 0;

  public Keyboard() {
    super("CPC Keyboard",8,10);
    for (int i = 0; i < bytes.length; i++)
      bytes[i] = 0xff;
    addKeyMappings(KEY_MAP);

    // For MS JVM, these keys have different codes
    addKeyMapping(0xba,5,3);  // VK_SEMICOLON = 0xba
    addKeyMapping(0xbc,7,4);  // VK_COMMA = 0xbc
    addKeyMapping(0xbd,1,3);  // VK_MINUS = 0xbd
    addKeyMapping(0xbe,7,3);  // VK_PERIOD = 0xbe
    addKeyMapping(0xdb,2,3);  // VK_OPEN_BRACKET = 0xdb
    addKeyMapping(0xdd,3,2);  // VK_CLOSE_BRACKET = 0xdd
    reset();
  }

  protected void keyChanged(int col, int row, int oldValue, int newValue) {
    if (oldValue == 0) {
      if (newValue != 0)
        bytes[row] &= (0x01 << col) ^ 0xff;
    }
    else if (newValue == 0)
      bytes[row] |= (0x01 << col);
  }

  public void setSelectedRow(int value) {
    row = value;
  }

  public int readSelectedRow() {
    return bytes[row];
  }

}