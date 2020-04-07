package jemu.system.vz;

import java.awt.event.KeyEvent;

import jemu.core.device.keyboard.MatrixKeyboard;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class Keyboard extends MatrixKeyboard {

  protected static final int[] KEY_MAP = {
    KeyEvent.VK_T, KeyEvent.VK_W, -1,					KeyEvent.VK_E, 		KeyEvent.VK_Q, KeyEvent.VK_R,
    KeyEvent.VK_G, KeyEvent.VK_S, KeyEvent.VK_CONTROL, 	KeyEvent.VK_D, 		KeyEvent.VK_A, KeyEvent.VK_F,
    KeyEvent.VK_B, KeyEvent.VK_X, KeyEvent.VK_SHIFT, 	KeyEvent.VK_C, 		KeyEvent.VK_Z, KeyEvent.VK_V,
    KeyEvent.VK_5, KeyEvent.VK_2, -1, 					KeyEvent.VK_3, 		KeyEvent.VK_1, KeyEvent.VK_4,
    KeyEvent.VK_N, KeyEvent.VK_PERIOD, -1, 				KeyEvent.VK_COMMA, 	KeyEvent.VK_SPACE, KeyEvent.VK_M,
    KeyEvent.VK_6, KeyEvent.VK_9, KeyEvent.VK_MINUS, 
    //0x2D, 				
    KeyEvent.VK_8, 		KeyEvent.VK_0, KeyEvent.VK_7,
    KeyEvent.VK_Y, KeyEvent.VK_O, KeyEvent.VK_ENTER, KeyEvent.VK_I, KeyEvent.VK_P, KeyEvent.VK_U,
    KeyEvent.VK_H, KeyEvent.VK_L, KeyEvent.VK_NUMBER_SIGN, KeyEvent.VK_K, KeyEvent.VK_PLUS, KeyEvent.VK_J
  };

//  protected static final int[] KEY_MAP = {
//		    KeyEvent.VK_T, KeyEvent.VK_W, -1,					KeyEvent.VK_E, 		KeyEvent.VK_Q, KeyEvent.VK_R,
//		    KeyEvent.VK_G, KeyEvent.VK_S, KeyEvent.VK_CONTROL, 	KeyEvent.VK_D, 		KeyEvent.VK_A, KeyEvent.VK_F,
//		    KeyEvent.VK_B, KeyEvent.VK_X, KeyEvent.VK_SHIFT, 	KeyEvent.VK_C, 		KeyEvent.VK_Z, KeyEvent.VK_V,
//		    KeyEvent.VK_5, KeyEvent.VK_2, -1, 					KeyEvent.VK_3, 		KeyEvent.VK_1, KeyEvent.VK_4,
//		    KeyEvent.VK_N, KeyEvent.VK_PERIOD, -1, 				KeyEvent.VK_COMMA, 	KeyEvent.VK_SPACE, KeyEvent.VK_M,
//		    KeyEvent.VK_6, KeyEvent.VK_9, 0x2D, 				KeyEvent.VK_8, 		KeyEvent.VK_0, KeyEvent.VK_7,
//		    KeyEvent.VK_Y, KeyEvent.VK_O, 						KeyEvent.VK_ENTER, 	KeyEvent.VK_I, KeyEvent.VK_P, KeyEvent.VK_U,
//		    KeyEvent.VK_H, KeyEvent.VK_L, KeyEvent.VK_BACK_QUOTE, KeyEvent.VK_K, KeyEvent.VK_SEMICOLON, KeyEvent.VK_J
//		  };

  protected int[] bytes = { 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff };

  public Keyboard() {
    super("VZ Keyboard",6,8);
    addKeyMappings(KEY_MAP);
    addCursor(KeyEvent.VK_LEFT,5,4);
    addCursor(KeyEvent.VK_RIGHT,3,4);
    addCursor(KeyEvent.VK_UP,1,4);
    addCursor(KeyEvent.VK_DOWN,4,4);

    // For MS JVM, these keys have different codes
    addKeyMapping(0xba,4,7);  // VK_SEMICOLON = 0xba
    addKeyMapping(0xbc,3,4);  // VK_COMMA = 0xbc
    addKeyMapping(0xbd,2,5);  // VK_MINUS = 0xbd
    addKeyMapping(0xbe,1,4);  // VK_PERIOD = 0xbe

    reset();
  }

  protected void addCursor(int key, int col, int row) {
    addKeyMapping(key,2,1);
    addKeyMapping(key,col,row);
  }

  protected void keyChanged(int col, int row, int oldValue, int newValue) {
    if (oldValue == 0) {
      if (newValue != 0)
        bytes[row] &= (0x01 << col) ^ 0x3f;
    }
    else if (newValue == 0)
      bytes[row] |= (0x01 << col);
  }

  public int readByte(int address) {
    int result = (address & 0x01) == 0 ? bytes[0] : 0x3f;
    if ((address & 0x02) == 0) result &= bytes[1];
    if ((address & 0x04) == 0) result &= bytes[2];
    if ((address & 0x08) == 0) result &= bytes[3];
    if ((address & 0x10) == 0) result &= bytes[4];
    if ((address & 0x20) == 0) result &= bytes[5];
    if ((address & 0x40) == 0) result &= bytes[6];
    if ((address & 0x80) == 0) result &= bytes[7];
    return result;
  }

}