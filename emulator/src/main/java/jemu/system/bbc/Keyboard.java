/*
 * Keyboard.java
 *
 * Created on 24 July 2006, 20:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.system.bbc;

import java.awt.event.*;
import jemu.core.device.keyboard.*;
import jemu.core.device.io.*;

/**
 *
 * @author Richard
 */
public class Keyboard extends MatrixKeyboard {
  
  protected static final int[] KEY_MAP = {
    // Row 0 - SHIFT, CTRL and links
    KeyEvent.VK_SHIFT, KeyEvent.VK_CONTROL, -1, -1, -1,
    -1, -1, -1, -1, -1,
    
    // Row 1
    KeyEvent.VK_Q, KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_F4,
    KeyEvent.VK_8, KeyEvent.VK_F7, KeyEvent.VK_MINUS, KeyEvent.VK_EQUALS, KeyEvent.VK_LEFT,
    
    // Row 2
    KeyEvent.VK_F10, KeyEvent.VK_W, KeyEvent.VK_E, KeyEvent.VK_T, KeyEvent.VK_7,
    KeyEvent.VK_I, KeyEvent.VK_9, KeyEvent.VK_0, KeyEvent.VK_NUMBER_SIGN, KeyEvent.VK_DOWN,
    
    // Row 3
    KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_D, KeyEvent.VK_R, KeyEvent.VK_6,
    KeyEvent.VK_U, KeyEvent.VK_O, KeyEvent.VK_P, KeyEvent.VK_OPEN_BRACKET, KeyEvent.VK_UP,
    
    // Row 4
    KeyEvent.VK_CAPS_LOCK, KeyEvent.VK_A, KeyEvent.VK_X, KeyEvent.VK_F, KeyEvent.VK_Y,
    KeyEvent.VK_J, KeyEvent.VK_K, KeyEvent.VK_BACK_QUOTE, KeyEvent.VK_QUOTE, KeyEvent.VK_ENTER,
    
    // Row 5
    KeyEvent.VK_SCROLL_LOCK, KeyEvent.VK_S, KeyEvent.VK_C, KeyEvent.VK_G, KeyEvent.VK_H,
    KeyEvent.VK_N, KeyEvent.VK_L, KeyEvent.VK_SEMICOLON, KeyEvent.VK_CLOSE_BRACKET, KeyEvent.VK_BACK_SPACE,
    
    // Row 6
    KeyEvent.VK_TAB, KeyEvent.VK_Z, KeyEvent.VK_SPACE, KeyEvent.VK_V, KeyEvent.VK_B,
    KeyEvent.VK_M, KeyEvent.VK_COMMA, KeyEvent.VK_PERIOD, KeyEvent.VK_SLASH, KeyEvent.VK_ALT,
    
    // Row 7
    KeyEvent.VK_ESCAPE, KeyEvent.VK_F1, KeyEvent.VK_F2, KeyEvent.VK_F3, KeyEvent.VK_F5,
    KeyEvent.VK_F6, KeyEvent.VK_F8, KeyEvent.VK_F9, KeyEvent.VK_BACK_SLASH, KeyEvent.VK_RIGHT
      
  };
  
  protected R6522 via;
  protected int down[] = new int[16];    // Number of pressed keys in each column
  protected int selCol = 0;
  protected int selRow = 0;
  
  /** Creates a new instance of Keyboard */
  public Keyboard(R6522 via) {
    super("BBC Keyboard",10,8);
    this.via = via;
    addKeyMappings(KEY_MAP);
    reset();
    for (int col = 0; col < 16; col++)
      down[col] = 0;
  }
  
  protected void keyChanged(int col, int row, int oldValue, int newValue) {
    if (row != 0) {  // SHIFT, CTRL and links don't cause an interrupt
      if (oldValue == 0)
        down[col]++;
      else if (newValue == 0)
        down[col]--;
    }
    //System.out.println("Key pressed: " + oldValue + ", " + newValue + ", " + down);
  }
  
  public void setColumnAndRow(int col, int row) {
    //System.out.println("Test Key: " + col + ", " + row);
    selCol = col;
    selRow = row;
    via.setCA2(down[selCol] != 0);
  }
  
  public boolean isKeyPressed() {
    return isKeyPressed(selCol,selRow);
  }
  
  public void cycle() {
    setColumnAndRow((selCol + 1) & 0x0f,selRow);
  }

}
