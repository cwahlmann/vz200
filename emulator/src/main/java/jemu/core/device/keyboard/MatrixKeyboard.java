package jemu.core.device.keyboard;

import java.util.*;
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

public class MatrixKeyboard extends Device {

  protected int[] pressMap = new int[0x800];

  // Each key has a count of Java keys pressed for that key
  protected int[][] keyMap;

  // Each Integer Java key is mapped in this table to a mapping array
  protected Hashtable mappings = new Hashtable();

  public MatrixKeyboard(String type, int cols, int rows) {
    super("Matrix Keyboard");
    keyMap = new int[rows][cols];
  }

  public void reset() {
    for (int row = 0; row < keyMap.length; row++) {
      int[] keyRow = keyMap[row];
      for (int col = 0; col < keyRow.length; col++)
        setKeyMap(col,row,0);
    }
  }

  protected void setKeyMap(int col, int row, int value) {
    int oldValue = keyMap[row][col];
    if (oldValue != value) {
      keyMap[row][col] = value;
      keyChanged(col,row,oldValue,value);
    }
  }

  public boolean isKeyPressed(int col, int row) {
    return row >= 0 && row < keyMap.length &&
      col >= 0 && col < keyMap[row].length && keyMap[row][col] != 0;
  }

  protected void keyChanged(int col, int row, int oldValue, int newValue) { }

  public void addKeyMapping(int key, int col, int row) {
    KeyMapping mapping = (KeyMapping)mappings.get(new Integer(key));
    if (mapping == null) {
      mapping = new KeyMapping();
      mappings.put(new Integer(key),mapping);
    }
    mapping.addMapping(col,row);
  }
  
  public void addKeyMappings(int[] map) {
    int cols = keyMap[0].length;
    for (int i = 0; i < map.length; i++)
      if (map[i] != -1)
        addKeyMapping(map[i],i % cols,i / cols);
  }
  
  public void addKeyMappings(int[][] map) {
    for (int row = 0; row < map.length; row++) {
      for (int col = 0; col < map[row].length; col++)
        if (map[row][col] != -1)
          addKeyMapping(map[row][col],col,row);
    }
  }

  public void removeKeyMapping(int key, int col, int row) {
    KeyMapping mapping = (KeyMapping)mappings.get(new Integer(key));
    if (mapping != null) {
      mapping.removeMapping(col,row);
      if (mapping.getCount() == 0)
        mappings.remove(mapping);
    }
  }

  public void keyPressed(int key) {
    keyChange(key,1);
  }

  public void keyReleased(int key) {
    keyChange(key,-1);
  }
  
  protected void keyChange(int key, int step) {
//    System.out.println("key=" + Util.hex((short)key));
    int mask = 0x01 << (key & 0x1f);
    int offs = key / 32;
    if (step != 1 || (pressMap[offs] & mask) == 0) {
      KeyMapping mapping = (KeyMapping)mappings.get(new Integer(key));
      if (mapping != null)
        mapping.keyChange(step);
      if (step == 1)
        pressMap[offs] |= mask;
      else
        pressMap[offs] &= mask ^ 0xffffffff;
    }
  }

  protected class KeyMapping {
    protected int[] cols = new int[0];
    protected int[] rows = new int[0];

    protected void addMapping(int col, int row) {
      cols = Util.arrayInsert(cols,cols.length,1,col);
      rows = Util.arrayInsert(rows,rows.length,1,row);
    }

    protected void removeMapping(int col, int row) {
      for (int i = 0; i < cols.length; i++) {
        if (cols[i] == col && rows[i] == row) {
          cols = Util.arrayDelete(cols,i,1);
          rows = Util.arrayDelete(rows,i,1);
          break;
        }
      }
    }

    protected int getCount() {
      return cols.length;
    }

    protected void keyChange(int step) {
      for (int i = 0; i < cols.length; i++) {
        int col = cols[i];
        int row = rows[i];
        setKeyMap(col,row,Math.max(0,keyMap[row][col] + step));
      }
    }

  }

}