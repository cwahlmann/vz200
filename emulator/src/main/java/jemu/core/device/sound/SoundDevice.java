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
 *
 * @author Richard
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
