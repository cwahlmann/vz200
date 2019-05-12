/*
 * DiscImage.java
 *
 * Created on 29 July 2006, 11:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.core.device.floppy;

/**
 *
 * @author Richard
 */
public abstract class DiscImage {
  
  public abstract byte[] readSector(int cylinder, int head, int c, int h, int r, int n);
  
  public abstract int getSectorCount(int cylinder, int head);
  
  public abstract int[] getSectorID(int cylinder, int head, int index);
  
}
