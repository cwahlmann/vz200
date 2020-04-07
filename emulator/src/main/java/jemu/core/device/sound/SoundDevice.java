/*
 * SoundDevice.java
 *
 * Created on 4 August 2006, 17:09
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.core.device.sound;

import jemu.core.device.*;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class SoundDevice extends Device {
  
  protected SoundPlayer player;
  
  /** Creates a new instance of SoundDevice */
  public SoundDevice(String name) {
    super(name);
  }
  
  public SoundPlayer getSoundPlayer() {
    return player;
  }
  
}
