/*
 * SoundPlayer.java
 *
 * Created on 4 August 2006, 17:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.core.device.sound;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public abstract class SoundPlayer {     // Probably should be an interface, but how efficient are they?
  
  protected int format = SoundUtil.ULAW;
  
  public int getClockAdder(int test, int cyclesPerSecond) {
    return (int)((long)test * (long)getSampleRate() / (long)cyclesPerSecond);
  }

  public abstract int getVolume();
  
  public abstract void setVolume(int volume);
  
  public abstract int getSampleRate();

  public abstract void writeMono(int value);
  
  public abstract void writeStereo(int a, int b);
  
  public abstract void play();
  
  public abstract void stop();
  
  public abstract void sync();

  public void setFormat(int value) {
    format = value;
  }
  
  public int getFormat() {
    return format;
  }
  
}
