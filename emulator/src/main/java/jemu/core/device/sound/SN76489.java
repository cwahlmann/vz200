/*
 * SN76489.java
 *
 * Created on 4 August 2006, 15:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.core.device.sound;

import jemu.core.*;
import jemu.core.device.*;

/**
 *
 * @author Richard
 */
public class SN76489 extends SoundDevice {
  
  // For some unknown reason the SN76489 numbers everything internally and in the datasheet
  // from 0 as the MSB to n as the LSB. Weird TI. Consequently, everything here assumes
  // that programming of registers is with D0 .. D7 on the SN connected to bits D7 .. D0 of the
  // data bus.
  
  public static final int[] LOG_VOLUME = { 63, 59, 55, 50, 46, 42, 38, 34,  29, 25, 21, 17, 13, 8, 4, 0 };
  
  public static final int TONE3_FREQUENCY = 0;
  public static final int TONE3_VOLUME    = 1;
  public static final int TONE2_FREQUENCY = 2;
  public static final int TONE2_VOLUME    = 3;
  public static final int TONE1_FREQUENCY = 4;
  public static final int TONE1_VOLUME    = 5;
  public static final int NOISE_CONTROL   = 6;
  public static final int NOISE_VOLUME    = 7;
  
  protected static final int FIRST_BYTE = 0x80;
  
  // BBC Uses a 4MHz clock
  
  protected int lastTone = 2;         // Last tone register set: 0 .. 2 equate to 3 .. 1
  protected int[] tone  = new int[4]; // Tone periods for each channel
  protected int[] count = new int[4]; // Count for each channel
  protected int[] flip  = new int[4]; // Flip-flop state for each channel
  protected int[] vol   = new int[4]; // Logical volume for each channel
  protected int[] out   = new int[4]; // Output values
  
  protected int clockSpeed = 4000000;
  protected int cycles = 0;
  protected boolean whiteNoise = false;
  protected int noiseRate = 0;          
  protected int noiseCycles = 0;
  protected int noiseShift = 0;
  protected boolean noiseShift4 = true;
  protected boolean noiseShift8 = true;
  protected int shiftReg = 0x4000;
   
  /** Creates a new instance of SN76489 */
  public SN76489() {
    super("Texas Instruments SN76489 Sound Generator");
    player = SoundUtil.getSoundPlayer(false);
    player.setFormat(SoundUtil.UPCM8);
    reset();
  }
  
  public void reset() {
    for (int i = 0; i < 3; i++) {
      tone[i] = count[i] = 0;
      vol[i] = 63;
    }
    shiftReg = 0x8000;
  }
  
  public void setClockSpeed(int value) {
    clockSpeed = value;
  }
  
  public void writePort(int port, int value) {
    // Only one port
    if ((value & FIRST_BYTE) != 0) {
      switch((value >> 4) & 0x07) {
        case TONE3_FREQUENCY: lastTone = 0; tone[0] = (tone[0] & 0x3f0) | (value & 0x0f);   break;
        case TONE3_VOLUME:    vol[0] = LOG_VOLUME[value & 0x0f]; out[0] = flip[0] * vol[0]; break;
        case TONE2_FREQUENCY: lastTone = 1; tone[1] = (tone[1] & 0x3f0) | (value & 0x0f);   break;
        case TONE2_VOLUME:    vol[1] = LOG_VOLUME[value & 0x0f]; out[1] = flip[1] * vol[1]; break;
        case TONE1_FREQUENCY: lastTone = 2; tone[2] = (tone[2] & 0x3f0) | (value & 0x0f);   break;
        case TONE1_VOLUME:    vol[2] = LOG_VOLUME[value & 0x0f]; out[2] = flip[2] * vol[2]; break;
        case NOISE_CONTROL:   noiseRate = value & 0x03; whiteNoise = (value & 0x04) != 0;
                              shiftReg = 0x4000; break;
        case NOISE_VOLUME:    vol[3] = LOG_VOLUME[value & 0x0f]; out[3] = flip[3] * vol[3]; break;
      }
    }
    else
      tone[lastTone] = (tone[lastTone] & 0x0f) | ((value & 0x3f) << 4);
    //System.out.println("Sound Write: " + Util.hex((byte)value) + ", Tone3=" + tone[0] + 
    //  ", Tone2=" + tone[2] + ", Tone1=" + tone[1] + ", noiseRate=" + noiseRate + ", white=" + whiteNoise);
  }
  
  protected final void noiseShift() {
    int bit = shiftReg & 0x01;
    if (whiteNoise) {
      flip[3] = bit ^ 0x01;
      shiftReg >>= 1;
      shiftReg |= (bit ^ (shiftReg & 0x01)) << 14;
    }
    else {
      flip[3] = bit;
      shiftReg = (shiftReg >> 1) | (bit << 14);
    }
    out[3] = flip[3] * vol[3];
  }
  
  public final void cycle(int add) {
    // Not using the normal cycle method
    if ((cycles += add) >= 16) {                               // Divide by 16
      // Do channel 3 (0) separately since this has Noise control
      if ((count[0] = (count[0] - 1) & 0x3ff) == 0) {
        count[0] = tone[0];
        out[0] = vol[0] * (flip[0] ^= 0x01);
        if (noiseRate == 3 && flip[0] == 1)                    // Flip the flip-flop output 
          noiseShift();
      }
      for (int i = 1; i < 3; i++)
        if ((count[i] = (count[i] - 1) & 0x3ff) == 0) {
          count[i] = tone[i];
          out[i] = vol[i] * (flip[i] ^= 0x01);                 // Flip the flip-flop output
        }
      cycles -= 16;
      if (++noiseCycles == 32) {                               // Further divide the above 16 by 32 to give 512 cycles
        noiseCycles = 0;
        if (noiseRate == 0)                                    // Noise rate 512
          noiseShift();
        if (noiseShift4 = !noiseShift4) {                      // Every 1024 cycles
          if ((noiseShift8 = !noiseShift8) && noiseRate == 2)  // Noise rate 2048
            noiseShift();
          else if (noiseRate == 1)                             // Noise rate 1024
            noiseShift();
        }
      }
    }
  }
  
  public final void writeAudio() {
    player.writeMono(out[0] + out[1] + out[2] + out[3]);
  }
  
}
