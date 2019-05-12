/*
 * BBCDiscImage.java
 *
 * Created on 29 July 2006, 11:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.system.bbc;

import jemu.core.device.floppy.*;

/**
 *
 * @author Richard
 */
public class BBCDiscImage extends DiscImage {
  
  protected String name;
  protected byte[][][] sectors;
  
  /** Creates a new instance of BBCDiscImage */
  public BBCDiscImage(String name, byte[] data) {
    // 10 sectors per track assumed for now (SSD)
    sectors = new byte[80][10][256];
    int index = 0;
    for (int c = 0; c < 80; c++) {
      for (int r = 0; r < 10; r++) {
        if (index < data.length) {
          int size = Math.min(256,data.length - index);
          System.arraycopy(data,index,sectors[c][r],0,size);
          index += size;
        }
      }
    }
  }

  public byte[] readSector(int cylinder, int head, int c, int h, int r, int n) {
    return cylinder >= sectors.length || r > 9 ? null : sectors[cylinder][r];
  }

  public int[] getSectorID(int cylinder, int head, int index) {
    return new int[] { 0, 0, 0, 0 };
  }

  public int getSectorCount(int cylinder, int head) {
    return cylinder >= sectors.length ? 0 : sectors[cylinder].length;
  }
  
}
