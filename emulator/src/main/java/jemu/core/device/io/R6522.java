/*
 * R6522.java
 *
 * Created on 17 July 2006, 20:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.core.device.io;

import jemu.core.*;
import jemu.core.device.*;

/**
 *
 * @author Richard
 */
public class R6522 extends Device {
  
  public static final int PORT_A       = 0;
  public static final int PORT_B       = 1;
  
  protected static final int INT_CA2   = 0x01;
  protected static final int INT_CA1   = 0x02;
  protected static final int INT_SHIFT = 0x04;
  protected static final int INT_CB2   = 0x08;
  protected static final int INT_CB1   = 0x10;
  protected static final int INT_T2    = 0x20;
  protected static final int INT_T1    = 0x40;
  protected static final int INT_ANY   = 0x80;
  protected static final int INT_SET   = 0x80;
  
  protected static final int CA1_POSITIVE    = 0x01;  // Negative edge if 0, Positive if 1
  
  protected static final int CA2_OUTPUT      = 0x08;  // Input if 0, Output if 1
  protected static final int CA2_POSITIVE    = 0x04;  // Negative if 0, Positive if 1
  protected static final int CA2_CLEAR       = CA2_OUTPUT | 0x02;
  protected static final int CA2_INDEPENDENT = 0x02;
  
  protected static final int T1_FREE_RUN     = 0x40;  // 0 for one-shot, 1 for free-run
  protected static final int T1_PB7          = 0x80;  // 1 to toggle PB7
  protected static final int T2_PB6          = 0x20;  // Count T2 on PB6 pulses

  protected IOPort[] ports = new IOPort[] { new IOPort(IOPort.READ), new IOPort(IOPort.READ) };
  
  protected int t1c;  // Timer 1 counter
  protected int t1l;  // Timer 1 latch
  protected int t2c;  // Timer 2 counter
  protected int t2l;  // Timer 2 latch
  protected int sr;   // Shift Register
  protected int acr;  // Auxillary Control Register
  protected int pcr;  // Peripheral Control Register
  protected int ifr;  // Interrupt Flag Register
  protected int ier;  // Interrupt Enable Register
  
  protected boolean ca1;   // CA1 signal
  protected boolean ca2;   // CA2 signal
  protected boolean cb1;   // CB1 signal
  protected boolean cb2;   // CB2 signal
  protected boolean nint;  // /INT signal
  
  protected Device interruptDevice;
  protected int interruptMask;
  
  protected boolean t1int = false;
  protected boolean t2int = false;
  
  /**
   * Creates a new instance of R6522
   */
  public R6522() {
    super("R6522 VIA");
    reset();
  }
  
  public void reset() {
    ports[PORT_A].setPortMode(IOPort.READ);
    ports[PORT_B].setPortMode(IOPort.READ);
    ier = INT_SET;
  }
  
  public void setInterruptDevice(Device device, int mask) {
    interruptDevice = device;
    interruptMask = mask;
  }
  
  public int readPort(int port) {
    // RS0 .. RS3 in port
    switch(port & 0x0f) {
      case 0:  return ports[PORT_B].read();               // IRB
      
      case 1:  if ((pcr & CA2_CLEAR) != CA2_INDEPENDENT)
                 setIFR(ifr & ~(INT_CA2 | INT_CA1));
               else
                 setIFR(ifr & ~INT_CA1);
      case 15: return ports[PORT_A].read();               // IRA
      
      case 2:  return ports[PORT_B].getPortMode();        // DDRB
      
      case 3:  return ports[PORT_A].getPortMode();        // DDRA
      
      case 4:  setIFR(ifr & ~INT_T1); return t1c & 0xff;  // T1C-L
               
      case 5:  return t1c >> 8;                           // T1C-H
      
      case 6:  return t1l & 0xff;                         // T1L-L
      
      case 7:  return t1l >> 8;                           // T1L-H
      
      case 8:  setIFR(ifr & ~INT_T2); return t2c & 0xff;  // T2C-L
               
      case 9:  return t2c >> 8;                           // T2C-H
      
      case 10: return sr;                                 // Shift Register
      
      case 11: return acr;                                // Auxillary Control Register
      
      case 12: return pcr;                                // Peripheral Control Register
      
      case 13: return ifr;                                // Interrupt Flag Register
      
      default: return ier;                                // Interrupt Enable Register
    }
  }

  public void writePort(int port, int value) {
    // RS0 .. RS3 in port
    switch(port & 0x0f) {
      case 0:  ports[PORT_B].write(value);                                     break;  // ORB
      case 1:  if ((pcr & CA2_CLEAR) != CA2_INDEPENDENT)
                 setIFR(ifr & ~(INT_CA2 | INT_CA1));
               else
                 setIFR(ifr & ~INT_CA1);
      case 15: ports[PORT_A].write(value);                                     break;  // ORA
      case 2:  ports[PORT_B].setPortMode(value);                               break;  // DDRB
      case 3:  ports[PORT_A].setPortMode(value);                               break;  // DDRA
      case 4:
      case 6:  t1l = (t1l & 0xff00 | value);                                   break;  // T1L-L
      case 5:  setIFR(ifr & ~INT_T1); t1c = t1l = (t1l & 0xff) | (value << 8);
               if ((acr & T1_PB7) != 0)                                        // TODO: Does this happen in Free-run
                 ports[PORT_B].write(ports[PORT_B].getOutput() & 0x7f);
               t1int = true;                                                   break;  // T1C-H
      case 7:  setIFR(ifr & ~INT_T1); t1l = (t1l & 0xff) | (value << 8);       break;  // T1L-H
      case 8:  t2l = value;                                                    break;  // T2L-L
      case 9:  setIFR(ifr & ~INT_T2); t2c = t2l | (value << 8); t2int = true;  break;  // T2C-H
      case 10: sr = value;                                                     break;  // Shift Register
      case 11: acr = value;                                                    break;  // Auxilary Control
      case 12: pcr = value;                                                    break;  // Peripheral Control
      case 13: setIFR(ifr & ~(value & 0x7f));                                  break;  // Interrupt Flags
      case 14: {                                        
        if ((value & 0x80) != 0) ier |= value; else ier &= ~(value & 0x7f);
        setIFR(ifr);                                                           break;  // Interrupt Enable
      }
    }
  }
  
  protected void setIFR(int value) {
    //System.out.println("setIFR: " + Util.hex((byte)value) + ", IER=" + Util.hex((byte)ier));
    int oldInt = ifr & 0x80;
    if ((value & ier & 0x7f) != 0) {
      ifr = value | 0x80;
      if (oldInt == 0 && interruptDevice != null)
        interruptDevice.setInterrupt(interruptMask);
    } else {
      ifr = value & 0x7f;
      if (oldInt != 0 && interruptDevice != null)
        interruptDevice.clearInterrupt(interruptMask);
    }
  }
  
  public IOPort getPort(int port) {
    return ports[port];
  }
  
  public void setCA1(boolean value) {
    if (ca1 != value) {
      ca1 = value;
      if (((pcr & CA1_POSITIVE) != 0) == value)
        setIFR(ifr | INT_CA1);
    }
  }
  
  public void setCA2(boolean value) {
    if (ca2 != value) {
      ca2 = value;
      if ((pcr & CA2_OUTPUT) == 0 && ((pcr & CA2_POSITIVE) != 0) == value)
        setIFR(ifr | INT_CA2);
//      System.out.println("ca2 toggle: " + value + " ifr=" + ifr + ", pcr=" + Util.hex((byte)pcr));
    }
  }
  
  public void cycle() {
    // Timer 1 count down
    if (t1c == 0xffff) {
      if ((acr & T1_FREE_RUN) != 0) {
        t1c = t1l;  // Copy latch
        if ((acr & T1_PB7) != 0)
          ports[PORT_B].write(ports[PORT_B].getOutput() ^ 0x80);
        setIFR(ifr | INT_T1);
        //System.out.println("Free run timer 1");
      }
      else if (t1int) {
        if ((acr & T1_PB7) != 0)
          ports[PORT_B].write(ports[PORT_B].getOutput() | 0x80);
        setIFR(ifr | INT_T1);
        t1int = false;
        //System.out.println("One shot timer 1");
      }
      else
        t1c = 0xfffe;
      // TODO: shift etc
    }
    else
      t1c = (t1c - 1) & 0xffff;
    
    // Timer 2
    if ((acr & T2_PB6) == 0) {
      if (t2int && t2c == 0) {
        setIFR(ifr | INT_T2);
        t2int = false;
      }
      t2c = (t2c - 1) & 0xffff;
    }
  }
}
