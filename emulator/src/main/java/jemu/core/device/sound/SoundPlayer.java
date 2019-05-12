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
 *
 * @author Richard
 */
public abstract class SoundPlayer {     // Probably should be an interface, but how efficient are they?
  
  protected int format = SoundUtil.ULAW;
  
  public int getClockAdder(int test, int cyclesPerSecond) {
    return (int)((long)test * (long)getSampleRate() / (long)cyclesPerSecond);
  }

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
