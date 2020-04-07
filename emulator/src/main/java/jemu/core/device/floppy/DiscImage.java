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
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public abstract class DiscImage {
  
  public abstract byte[] readSector(int cylinder, int head, int c, int h, int r, int n);
  
  public abstract int getSectorCount(int cylinder, int head);
  
  public abstract int[] getSectorID(int cylinder, int head, int index);
  
}
