/*
 * BBC.java
 *
 * Created on 16 July 2006, 16:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.system.bbc;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.JPanel;

import jemu.core.*;
import jemu.core.cpu.*;
import jemu.core.device.*;
import jemu.core.device.crtc.*;
import jemu.core.device.floppy.*;
import jemu.core.device.io.*;
import jemu.core.device.memory.*;
import jemu.core.device.sound.*;
import jemu.ui.*;
import jemu.util.diss.*;

/**
 *
 * @author Richard
 */
public class BBC extends Computer {
  
  protected static Dimension HALF_DISPLAY_SIZE = new Dimension(384,270);
  protected static Dimension FULL_DISPLAY_SIZE = new Dimension(768,540);
  
  protected static final int CYCLES_PER_SECOND = 2000000;
  protected static final int AUDIO_TEST        = 0x40000000;
  
  protected static final int SYS_VIA_PORT_A = 0;
  protected static final int SYS_VIA_PORT_B = 1;
  
  protected static final int SYS_VIA_INT_MASK  = 0x00001;
  protected static final int USER_VIA_INT_MASK = 0x00002;
  protected static final int FDC_INT_MASK      = 0x10000;  // NMI
  
  protected static final int KEYBOARD_WRITE_ENABLE = 0x08;
  protected static final int SOUND_WRITE_ENABLE    = 0x01;

  protected MC6502 cpu = new MC6502(CYCLES_PER_SECOND);
  protected BBCMemory memory = new BBCMemory();
  protected Basic6845 crtc = new Basic6845();
  protected SAA505x saa = new SAA505x();
  protected R6522 sysVIA = new R6522();
  protected R6522 userVIA = new R6522();
  protected Video video = new Video(this);
  protected SN76489 psg = new SN76489();
  protected I8271 fdc = new I8271();
  protected Keyboard keyboard = new Keyboard(sysVIA);
  protected Disassembler disassembler = new Diss6502();
  protected int latchState = 0x00;
  protected int fdcControl = 0x01;
  protected boolean oddCycle = false;
  protected boolean oddFrame = false;
  protected int audioCount = 0;
  protected int audioAdd = psg.getSoundPlayer().getClockAdder(AUDIO_TEST,CYCLES_PER_SECOND >> 1);
  
  /** Creates a new instance of BBC */
  public BBC(JPanel applet, String name) {
    super(applet,name);
    cpu.setMemoryDevice(this);
    cpu.setCycleDevice(this);
    sysVIA.getPort(R6522.PORT_A).setInputDevice(this,SYS_VIA_PORT_A);
    sysVIA.getPort(R6522.PORT_A).setOutputDevice(this,SYS_VIA_PORT_A);
    sysVIA.getPort(R6522.PORT_B).setOutputDevice(this,SYS_VIA_PORT_B);
    sysVIA.setInterruptDevice(cpu,SYS_VIA_INT_MASK);
    userVIA.setInterruptDevice(cpu,USER_VIA_INT_MASK);
    fdc.setInterruptDevice(cpu,FDC_INT_MASK);
    fdc.setDrive(0,new Drive(2));
    crtc.setCRTCListener(video);
    psg.setClockSpeed(CYCLES_PER_SECOND * 2);
    setBasePath("bbc");
  }
  
  public void initialise() {
    memory.setOSROM(getFile(romPath + "os12.rom",0x4000));
    memory.loadROM(0x0f,getFile(romPath + "basic2.rom",0x4000));
    memory.loadROM(0x0e,getFile(romPath + "wdfs.rom",0x4000));
    video.setMemory(memory.getMemory());
    saa.setCharacterROM(getFile(romPath + "SAA5050.fnt",0x360));
    fdc.getDrive(0).setDisc(3,new BBCDiscImage("Revs",getFile(filePath + "Revs.zip")));
    psg.getSoundPlayer().play();
    super.initialise();
  }
  
  public void reset() {
    //keyboard.reset();
    sysVIA.reset();
    userVIA.reset();
    fdc.reset();
    //crtc.reset();
    psg.reset();
    super.reset();
  }
  
  public void loadVzFile(String name) throws Exception {
    fdc.getDrive(0).setDisc(3,new BBCDiscImage(name,getFile(name)));
  }

  public Memory getMemory() {
    return memory;
  }
  
  public void cycle() {
    video.cycle();
    if (oddCycle = !oddCycle) {
      if ((latchState & KEYBOARD_WRITE_ENABLE) != 0)
        keyboard.cycle();
      sysVIA.cycle();
      userVIA.cycle();
      fdc.cycle();
      psg.cycle(4);
      if ((audioCount += audioAdd) >= AUDIO_TEST) {
        //System.out.println("Audio Out:  " + cpu.getCycles());
        psg.writeAudio();
        audioCount -= AUDIO_TEST;
      }
    }
  }
  
  public void setFrameSkip(int value) {
    super.setFrameSkip(value);
    video.setRendering(value == 0);
  }
  
  long lastCycles;

  public void vSync() {
    if (frameSkip == 0)
      updateDisplay(true);
    syncProcessor();
    lastCycles = cpu.getCycles();
  }
  
  public void updateDisplay(boolean wait) {
    display.setSourceRect(video.getImageRect());
    display.updateImage(wait);
  }

  public final int readByte(int addr) {
    if (addr >= 0xfc00 && addr < 0xff00) {
      if (addr >= 0xfe00) {
        if (oddCycle)
          cpu.cycle();
        cpu.cycle();
        switch(addr & 0xe0) {
          case 0x00: {
            if (addr < 0xfe08)
              return crtc.readPort(addr & 0x07);
            else if (addr < 0xfe10) {
              //System.out.println("ACIA read " + Util.hex((short)addr)); // 6850 ACIA
              return 0x7f;
            }  
            else
              System.out.println("Serial ULA read " + Util.hex((short)addr)); // Serial ULA
            break;
          }
          case 0x20: {
            return 0xff;       // Video ULA and ROM Select not readable
          }
          
          case 0x40: return sysVIA.readPort(addr & 0x0f);
         
          case 0x60: //System.out.println("User VIA read " + Util.hex((short)addr) + ", PC=" + Util.hex((short)cpu.getProgramCounter()));
                     return userVIA.readPort(addr & 0x0f);
          
          case 0x80: return fdc.readPort(addr & 0x07);
          
          case 0xa0: {
            System.out.println("Econet read " + Util.hex((short)addr)); // Econet
            break;
          }
          
          case 0xc0: {
            //System.out.println("ADC read " + Util.hex((short)addr));
            break;
          }
          
          default: {     // 0xfee0..0xfeff - Tube reads
            System.out.println("Tube read " + Util.hex((short)addr));
            return 0;
          }
        }
      }
      return 0xff;
    }
    return memory.readByte(addr);
  }
  
  public final int writeByte(int addr, int value) {
    if (addr >= 0xfc00 && addr < 0xff00) {
      if (addr >= 0xfe00) {
        if (oddCycle)
          cpu.cycle();
        cpu.cycle();
        switch(addr & 0xe0) {
          case 0x00: {  // CRTC, ACIA, SERPROC, INTOFF/STATID
            if (addr < 0xfe08)
              crtc.writePort(addr & 0x07,value);
            else if (addr < 0xfe10)
              System.out.println("ACIA write " + Util.hex((short)addr) + "=" + Util.hex((byte)value));
            else
              System.out.println("Serial ULA write " + Util.hex((short)addr) + "=" + Util.hex((byte)value));
            break;
          }
          
          case 0x20: {
            if (addr < 0xfe30)
              video.writePort(addr & 0x01,value);
            else
              memory.selectROM(value & 0x0f);
            break;
          }
          
          case 0x40: //if ((addr & 0x0f) == 14)
                     //  System.out.println("System VIA write " + Util.hex((short)addr) + "=" + Util.hex((byte)value));
                     sysVIA.writePort(addr & 0x0f,value); break;

          case 0x60: //System.out.println("User VIA write " + Util.hex((short)addr) + "=" + Util.hex((byte)value));
                     userVIA.writePort(addr & 0x0f,value); break;

          case 0x80: fdc.writePort(addr & 0x07,value); break;
          
          case 0xa0: {
            System.out.println("Econet write " + Util.hex((short)addr) + "=" + Util.hex((byte)value));
            break;            
          }
          
          case 0xc0: {
            //System.out.println("ADC write " + Util.hex((short)addr) + "=" + Util.hex((byte)value));
            break;
          }
          
          default: {     // 0xfee0..0xfeff - Tube write
            System.out.println("Tube write " + Util.hex((short)addr) + "=" + Util.hex((byte)value));
            break;
          }
        }
      }
      return value & 0xff;
    }
    return memory.writeByte(addr,value);
  }
  
  public int readPort(int port) {
    if (port == SYS_VIA_PORT_A) {
      //System.out.println("Key read: " + Util.hex((short)cpu.getProgramCounter()));
      return keyboard.isKeyPressed() ? 0xff : 0x7f;
    }
    return 0xff;
  }
  
  public void writePort(int port, int value) {
    if (port == SYS_VIA_PORT_A) {
//      System.out.println("Keyboard value: " + Util.hex((byte)value) + ": " + Util.hex((byte)sysVIA.getPort(0).getPortMode()));
      if ((latchState & KEYBOARD_WRITE_ENABLE) == 0)
        keyboard.setColumnAndRow(value & 0x0f, (value >> 4) & 0x07);
      //if ((latchState & SOUND_WRITE_ENABLE) == 0)
        //psg.writePort(0,value);
    }
    else {
      int bit = value & 0x07;
      boolean set = (value & 0x08) != 0;
      //System.out.println("Sys VIA Port B Write: " + bit + " = " + set);
      // TODO: Some bits may need to be processed
      if (set)
        latchState |= 0x01 << bit;
      else
        latchState &= ~(0x01 << bit);
      video.setAddMA((latchState & 0x30) << 10);
      if ((latchState & SOUND_WRITE_ENABLE) == 0)
        psg.writePort(0,sysVIA.getPort(R6522.PORT_A).getOutput());
    }
  }

  public Processor getProcessor() {
    return cpu;
  }
  
  public void setDisplay(Display display) {
    super.setDisplay(display);
    video.setPixels(display.getPixels());
  }

  public Dimension getDisplaySize(boolean large) {
    return large ? FULL_DISPLAY_SIZE : HALF_DISPLAY_SIZE;
  }
  
  public Dimension getDisplayScale(boolean large) {
    return Display.SCALE_1;
  }
  
  public void processKeyEvent(KeyEvent e) {
    if (mode == RUN) {
      if (e.getID() == KeyEvent.KEY_PRESSED) {
        keyboard.keyPressed(e.getKeyCode());
        if (e.getKeyCode() == KeyEvent.VK_F12)
          reset();
      }
      else if (e.getID() == KeyEvent.KEY_RELEASED)
        keyboard.keyReleased(e.getKeyCode());
    }
  }

  public Disassembler getDisassembler() {
    return disassembler;
  }
  
  public void setLarge(boolean value) {
    video.setLarge(value);
  }

}
