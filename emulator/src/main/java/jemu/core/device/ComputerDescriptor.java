/*
 * ComputerDescriptor.java
 *
 * Created on 5 August 2006, 21:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.core.device;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class ComputerDescriptor {
  
  public String key, name, className;
  public boolean shown;
  
  /** Creates a new instance of ComputerDescriptor */
  public ComputerDescriptor(String key, String name, String className, boolean shown) {
    this.key = key;
    this.name = name;
    this.className = className;
    this.shown = shown;
  }
  
  public String toString() {
    return name;
  }
  
}
