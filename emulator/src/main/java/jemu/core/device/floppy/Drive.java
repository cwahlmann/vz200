/*
 * Drive.java
 *
 * Created on 28 July 2006, 18:07
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.core.device.floppy;

import jemu.core.device.*;

/**
 *
 * @author Richard
 */
public class Drive extends Device {
  
  protected boolean ready = true;
  protected boolean writeProtected = true;
  protected int cylinder = 0;
  protected int head     = 0;
  protected int sides    = 1;
  protected DiscImage[] discs  = new DiscImage[2];  // Possible one for each head
  
  /** Creates a new instance of Drive */
  public Drive(int sides) {
    super((sides == 1 ? "Single" : "Double") + "-Sided Floppy Drive");
  }
  
  public void setDisc(int heads, DiscImage value) {
    if ((heads & 0x01) != 0)
      discs[0] = value;
    if ((heads & 0x02) != 0)
      discs[1] = value;
  }
  
  public boolean isReady() {
    return ready;
  }
  
  public boolean isWriteProtected() {
    return writeProtected;
  }
  
  public void setCylinder(int value) {
    cylinder = value;
  }
  
  public int getCylinder() {
    return cylinder;
  }
  
  public void setHead(int value) {
    head = value & (sides - 1);
  }
  
  public int getHead() {
    return head;
  }
  
  public int getSides() {
    return sides;
  }
  
  public int getSectorCount() {
    return discs[head] == null ? 0 : discs[head].getSectorCount(cylinder,head);
  }
  
  public byte[] getSector(int c, int h, int r, int n) {
    return discs[head] == null ? null : discs[head].readSector(cylinder,head,c,h,r,n);
  }
  
}
