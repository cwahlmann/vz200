package jemu.core.device.sound;

import jemu.core.device.*;

/**
 * Title:        JEMU
 * Description:  The Java Emulation Platform
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class AY_3_8910 extends SoundDevice {

  public static final int BDIR_MASK = 0x04;
  public static final int BC2_MASK  = 0x02;
  public static final int BC1_MASK  = 0x01;

  public static final int PORT_A     = 0;
  public static final int PORT_B     = 1;

  // Possible states
  protected static final int INACTIVE = 0;
  protected static final int LATCH    = 1;
  protected static final int READ     = 2;
  protected static final int WRITE    = 3;

  protected static final int[] STATES = {
    INACTIVE, LATCH, INACTIVE, READ, LATCH, INACTIVE, WRITE, LATCH
  };
  
  // Registers
  protected static final int AFINE       = 0;
  protected static final int ACOARSE     = 1;
  protected static final int BFINE       = 2;
  protected static final int BCOARSE     = 3;
  protected static final int CFINE       = 4;
  protected static final int CCOARSE     = 5;
  protected static final int NOISEPERIOD = 6;
  protected static final int ENABLE      = 7;
  protected static final int AVOL        = 8;
  protected static final int BVOL        = 9;
  protected static final int CVOL        = 10;
  protected static final int EFINE       = 11;
  protected static final int ECOARSE     = 12;
  protected static final int ESHAPE      = 13;
  protected static final int REG_PORTA   = 14;
  protected static final int REG_PORTB   = 15;
  
  // Bits of ENABLE register
  protected static final int ENABLE_A    = 0x01;
  protected static final int ENABLE_B    = 0x02;
  protected static final int ENABLE_C    = 0x04;
  protected static final int NOISE_A     = 0x08;
  protected static final int NOISE_B     = 0x10;
  protected static final int NOISE_C     = 0x20;
  protected static final int PORT_A_OUT  = 0x40;
  protected static final int PORT_B_OUT  = 0x80;
  
  protected static final int NOISE_ALL = NOISE_A | NOISE_B | NOISE_C;
  
  // Sound Channels (inc Noise and Envelope)
  protected static final int A        = 0;
  protected static final int B        = 1;
  protected static final int C        = 2;
  protected static final int NOISE    = 3;
  protected static final int ENVELOPE = 4;

  protected int step = 0x8000;
  protected int[] regs = new int[16];
  protected int selReg = 0;
  protected int bdirBC2BC1 = 0;
  protected int state = INACTIVE;
  protected int clockSpeed = 1000000;
  protected IOPort[] ports = new IOPort[] {
    new IOPort(IOPort.READ), new IOPort(IOPort.READ)
  };
  protected int[] envelope = new int[3];  // Channels A, B and C
  protected int[] output   = new int[4];  // A, B, C and Noise
  protected int[] count    = new int[5];  // A, B, C, Noise and Envelope counters
  protected int[] period   = new int[5];  // A, B, C, Noise and Envelope
  protected int[] volume   = new int[5];  // A, B, C, Noise and Envelope (Vol[3] not used)
  protected int outN, random = 1;
  protected int countEnv, hold, alternate, attack, holding;
  
  protected int updateStep;

  public AY_3_8910() {
    super("AY-3-8910/2/3 Programmable Sound Generator");
    setClockSpeed(1000000);
    player = SoundUtil.getSoundPlayer(true);
    player.setFormat(SoundUtil.UPCM8);
  }
  
  public void setClockSpeed(int value) {
    clockSpeed = value;
    updateStep = 5790; //(int)(((long)step * 8L * (long)audio.getSampleRate()) / (long)clockSpeed);
    output[NOISE] = 0xff;
    for (int i = A; i <= ENVELOPE; i++)
      period[i] = count[i] = updateStep;
    period[ENVELOPE] = 0;
    count[NOISE] = 0x7fffffff;
  }

  public void setSelectedRegister(int value) {
    selReg = value & 0x0f;
  }

  public void setBDIR_BC2_BC1(int value, int dataValue) {
    if (bdirBC2BC1 != value) {
      bdirBC2BC1 = value;
      state = STATES[bdirBC2BC1];
      writePort(0,dataValue);
    }
  }

  public int readPort(int port) {
    return state == READ ? readRegister(selReg) : 0xff;
  }

  public void writePort(int port, int value) {
    switch(state) {
      case LATCH: selReg = value & 0x0f;              break;
      case WRITE: setRegister(selReg,value);          break;
    }
  }

  public int getRegister(int index) {
    return regs[index];
  }

  public int readRegister(int index) {
    return index < REG_PORTA ? regs[index] : ports[index - REG_PORTA].read();
  }

  public void setRegister(int index, int value) {
    if (regs[index] != value) {
      if (index < REG_PORTA) {
        if (index == ESHAPE || regs[index] != value) {
          regs[index] = value;
          switch(index) {
            case ACOARSE:
            case BCOARSE:
            case CCOARSE:
            case AFINE:
            case BFINE:
            case CFINE: {
              index >>= 1;
              int val = (((regs[(index << 1) + 1] &0x0f) << 8) | regs[index << 1]) * updateStep;
              int last = period[index];
              period[index] = val = val < 0x8000 ? 0x8000 : val;
              int newCount = count[index] - (val - last);
              count[index] = newCount < 1 ? 1 : newCount;
              break;
            }
            
            case NOISEPERIOD: {
              int val  = (value & 0x1f) * updateStep;
              int last = period[NOISE];
              period[NOISE] = val = val == 0 ? updateStep : val;
              int newCount = count[NOISE] - (val - last);
              count[NOISE] = newCount < 1 ? 1 : newCount;
              break;
            }
            
            case ENABLE: break;
            
            case AVOL:
            case BVOL:
            case CVOL: {
              volume[index - AVOL] = (value & 0x10) == 0 ? value & 0x0f : volume[ENVELOPE];
              break;
            }
            
            case EFINE:
            case ECOARSE: {
              int val = (((regs[ECOARSE] << 8) | regs[EFINE]) * updateStep) << 1;
              int last = period[ENVELOPE];
              period[ENVELOPE] = val;
              int newCount = count[ENVELOPE] - (val - last);
              count[ENVELOPE] = newCount < 1 ? 1 : newCount;
              break;
            }
            
            case ESHAPE: {
              attack = (value & 0x04) == 0 ? 0 : 0x0f;
              if ((value & 0x08) == 0) {
                hold = 1;
                alternate = attack;
              }
              else {
                hold = value & 0x01;
                alternate = value & 0x02;
              }
              count[ENVELOPE] = period[ENVELOPE];
              countEnv = 0x0f;
              holding = 0;
              int vol = volume[ENVELOPE] = attack ^ 0x0f;
              if ((regs[AVOL] & 0x10) != 0) volume[A] = vol;
              if ((regs[BVOL] & 0x10) != 0) volume[B] = vol;
              if ((regs[CVOL] & 0x10) != 0) volume[C] = vol;
              break;
            }
          }
        }
      }
      else
        ports[index - REG_PORTA].write(value);
    }
  }
  
  public void writeAudio() {
    int enable = regs[ENABLE];
    if ((enable & ENABLE_A) != 0) {
      if (count[A] <= step) count[A] += step;
      output[A] = 1;
    }
    if ((enable & ENABLE_B) != 0) {
      if (count[B] <= step) count[B] += step;
      output[B] = 1;
    }
    if ((enable & ENABLE_C) != 0) {
      if (count[C] <= step) count[C] += step;
      output[C] = 1;
    }
    outN = output[NOISE] | enable;
    if ((enable & NOISE_ALL) == NOISE_ALL) { // false if All disabled
      if (count[NOISE] <= step)
        count[NOISE] += step;
    }
    // output Sound bytes
    int[] cnt = new int[3];
    int left = step;
    do {
      int add = count[NOISE] < left ? count[NOISE] : left;
      
      for (int chan = A; chan <= C; chan++) {
        int chcnt = count[chan];
        if ((outN & (NOISE_A << chan)) != 0) {
          int val = output[chan] == 0 ? cnt[chan] : cnt[chan] + chcnt;
          if ((chcnt -= add) <= 0) {
            int p = period[chan];
            while (true) {
              if ((chcnt += p) > 0) {
                if ((output[chan] ^= 0x01) != 0)
                  val += p - chcnt;
                break;
              }
              val += p;
              if ((chcnt += p) > 0) {
                if (output[chan] == 0)
                  val -= chcnt;
                break;
              }
            }
          }
          else if (output[chan] != 0)
            val -= chcnt;
          cnt[chan] = val;
        }
        else {
          if ((chcnt -= add) <= 0) {
            int p = period[chan];
            while (true) {
              if ((chcnt += p) > 0) {
                output[chan] ^= 0x01;
                break;
              }
              if ((chcnt += p) > 0)
                break;
            }
          }
        }
        count[chan] = chcnt;
      }
      
      if ((count[NOISE] -= add) <= 0) {
        int val = random + 1;
        if ((val & 0x02) != 0)
          outN = (output[NOISE] ^= 0xff) | enable;
        random = (random & 0x01) == 0 ? random << 1 : (random ^ 0x28000) << 1;
        count[NOISE] += period[NOISE];
      }
 
      left -= add;
    } while (left > 0);
    
    if (holding == 0 && period[ENVELOPE] != 0) {
      if ((count[ENVELOPE] -= step) <= 0) {
        int ce = countEnv;
        int p = period[ENVELOPE];
        do {
          ce--;
        } while((count[ENVELOPE] += p) <= 0);
        
        if (ce < 0) {
          if (hold != 0) {
            if (alternate != 0)
              attack ^= 0x0f;
            holding = 1;
            ce = 0;
          }
          else {
            if (alternate != 0 && (ce & 0x10) != 0)
              attack ^= 0x0f;
            ce &= 0x0f;
          }
        }
        countEnv = ce;
        int vol = volume[ENVELOPE] = ce ^ attack;
        if ((regs[AVOL] & 0x10) != 0) volume[A] = vol;
        if ((regs[BVOL] & 0x10) != 0) volume[B] = vol;
        if ((regs[CVOL] & 0x10) != 0) volume[C] = vol;
      }
    }
    
    int a = volume[A] * cnt[A] >> 15;
    int b = volume[B] * cnt[B] >> 15;
    int c = volume[C] * cnt[C] >> 15;
    player.writeStereo(a + b,b + c);
  }

  public void setReadDevice(int port, Device device, int readPort) {
    ports[port].setInputDevice(device,readPort);
  }

  public void setWriteDevice(int port, Device device, int writePort) {
    ports[port].setOutputDevice(device,writePort);
  }

}