package jemu.system.cpc;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.InputStream;

import jemu.core.cpu.Processor;
import jemu.core.device.Computer;
import jemu.core.device.DeviceMapping;
import jemu.core.device.crtc.Basic6845;
import jemu.core.device.io.PPI8255;
import jemu.core.device.memory.Memory;
import jemu.core.device.sound.AY_3_8910;
import jemu.ui.Display;
import jemu.util.diss.Disassembler;
import jemu.util.diss.DissZ80;

/**
 * Title:        JEMU
 * Description:  The Java Emulation Platform
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class CPC extends Computer {

  // Conversion of CPC BDIR & BC1 values 0..3 to PSG values
  protected static final int[] PSG_VALUES = new int[] {
    AY_3_8910.BC2_MASK,
    AY_3_8910.BC2_MASK | AY_3_8910.BC1_MASK,
    AY_3_8910.BC2_MASK | AY_3_8910.BDIR_MASK,
    AY_3_8910.BC2_MASK | AY_3_8910.BDIR_MASK | AY_3_8910.BC1_MASK
  };

  // Port mappings
  protected static final int PSG_PORT_A = -1;
  protected static final int PPI_PORT_B = -2;
  protected static final int PPI_PORT_C = -3;

  protected static final int CYCLES_PER_SECOND = 1000000;
  protected static final int AUDIO_TEST        = 0x40000000;
  
  protected Z80 z80 = new Z80(CYCLES_PER_SECOND);
  protected CPCMemory memory = new CPCMemory(CPCMemory.TYPE_64K);
  protected Basic6845 crtc = new Basic6845();
  protected GateArray gateArray = new GateArray(this);
  protected PPI8255 ppi = new PPI8255();
  protected AY_3_8910 psg = new AY_3_8910();
  protected Keyboard keyboard = new Keyboard();
  protected Disassembler disassembler = new DissZ80();
  protected int audioAdd = psg.getSoundPlayer().getClockAdder(AUDIO_TEST,CYCLES_PER_SECOND);
  protected int audioCount = 0;

  public CPC() {
    super("CPC464");
    z80.setMemoryDevice(memory);
    z80.addOutputDeviceMapping(new DeviceMapping(memory,0x2000,0x0000));    // ROM Select
    z80.setInterruptDevice(gateArray);
    z80.addOutputDeviceMapping(new DeviceMapping(gateArray,0xc000,0x4000)); // All GA functions
    z80.setCycleDevice(this);
    crtc.setRegisterSelectMask(0x0100,0x0000);
    crtc.setCRTCListener(gateArray);
    z80.addOutputDeviceMapping(new DeviceMapping(crtc,0x4000,0x0000));
    ppi.setPortMasks(0x0100,0x0100,0x0200,0x0200);
    ppi.setReadDevice(PPI8255.PORT_B,this,PPI_PORT_B);
    ppi.setWriteDevice(PPI8255.PORT_C,this,PPI_PORT_C);
    ppi.setReadDevice(PPI8255.PORT_A,psg,0);
    ppi.setWriteDevice(PPI8255.PORT_A,psg,0);
    psg.setReadDevice(AY_3_8910.PORT_A,this,PSG_PORT_A);
    psg.setClockSpeed(CYCLES_PER_SECOND);
    psg.getSoundPlayer().play();
    z80.addOutputDeviceMapping(new DeviceMapping(ppi,0x0800,0x0000));
    z80.addInputDeviceMapping(new DeviceMapping(ppi,0x0800,0x0000));
    setBasePath("cpc");
  }

  public void initialise() {
    memory.setLowerROM(getFile(romPath + "CPCLOW.ROM", 16384));
    memory.setUpperROM(0,getFile(romPath + "CPCBASIC.ROM", 16384));
    gateArray.setMemory(memory.getMemory());
    super.initialise();
  }

	public String getKeyboardImage() {
		return "/jemu/ui/cpc/cpc464_keyboard.png";
	}
  public void cycle() {
    gateArray.cycle();
    if ((audioCount += audioAdd) >= AUDIO_TEST) {
      psg.writeAudio();
      audioCount -= AUDIO_TEST;
    }
  }

  public void setDisplay(Display value) {
    super.setDisplay(value);
    gateArray.setDisplay(value);
  }

  public void setFrameSkip(int value) {
    super.setFrameSkip(value);
    gateArray.setRendering(value == 0);
  }

  public void vSync() {
    if (frameSkip == 0)
      display.updateImage(true);
    syncProcessor();
  }

  public Memory getMemory() {
    return memory;
  }

  public Processor getProcessor() {
    return z80;
  }

  public Dimension getDisplaySize(boolean large) {
    return gateArray.getDisplaySize(large);
  }
  
  public Dimension getDisplayScale(boolean large) {
    return large ? Display.SCALE_1x2 : Display.SCALE_1;
  }
  
  public void setLarge(boolean value) {
    gateArray.setHalfSize(!value);
  }

  public Disassembler getDisassembler() {
    return disassembler;
  }

  public int readPort(int port) {
    int result;
    switch(port) {
      case PPI_PORT_B:
        result = 0x5e | (crtc.isVSync() ? 0x01 : 0);
        break;

      case PSG_PORT_A:
        result = keyboard.readSelectedRow();
        break;

      default:
        throw new RuntimeException("Unexpected Port Read: " + port);
    }
    return result;
  }

  public void writePort(int port, int value) {
    switch(port) {

      case PPI_PORT_C:
        psg.setBDIR_BC2_BC1(PSG_VALUES[value >> 6],ppi.readOutput(PPI8255.PORT_A));
        keyboard.setSelectedRow(value & 0x0f);
        break;

      default:
        throw new RuntimeException("Unexpected Port Write: " + port + " with " + value);
    }
  }

  public void processKeyEvent(KeyEvent e) {
    if (e.getID() == KeyEvent.KEY_PRESSED)
      keyboard.keyPressed(e.getKeyCode());
    else if (e.getID() == KeyEvent.KEY_RELEASED)
      keyboard.keyReleased(e.getKeyCode());
  }

  protected static final byte[] SNA_HEADER = "MV - SNA".getBytes();
  protected static final int CRTC_FLAG_VSYNC_ACTIVE                 = 0x01;
  protected static final int CRTC_FLAG_HSYNC_ACTIVE                 = 0x02;
  protected static final int CRTC_FLAG_HDISP_ACTIVE                 = 0x04;
  protected static final int CRTC_FLAG_VDISP_ACTIVE                 = 0x08;
  protected static final int CRTC_FLAG_HTOT_REACHED                 = 0x10;
  protected static final int CRTC_FLAG_VTOT_REACHED                 = 0x20;
  protected static final int CRTC_FLAG_MAXIMUM_RASTER_COUNT_REACHED = 0x40;

  protected static final int SNAPSHOT_ID      = 0x0000;
  protected static final int VERSION          = 0x0010;
  protected static final int AF               = 0x0011;
  protected static final int BC               = 0x0013;
  protected static final int DE               = 0x0015;
  protected static final int HL               = 0x0017;
  protected static final int R                = 0x0019;
  protected static final int I                = 0x001a;
  protected static final int IFF1             = 0x001b;
  protected static final int IFF2             = 0x001c;
  protected static final int IX               = 0x001d;
  protected static final int IY               = 0x001f;
  protected static final int SP               = 0x0021;
  protected static final int PC               = 0x0023;
  protected static final int IM               = 0x0025;
  protected static final int AF1              = 0x0026;
  protected static final int BC1              = 0x0028;
  protected static final int DE1              = 0x002a;
  protected static final int HL1              = 0x002c;

  protected static final int GA_PEN           = 0x002e;
  protected static final int GA_INKS          = 0x002f;
  protected static final int GA_ROM           = 0x0040;
  protected static final int GA_RAM           = 0x0041;

  protected static final int CRTC_REG         = 0x0042;
  protected static final int CRTC_REGS        = 0x0043;

  protected static final int UPPER_ROM        = 0x0055;
  protected static final int PPI_A            = 0x0056;
  protected static final int PPI_B            = 0x0057;
  protected static final int PPI_C            = 0x0058;
  protected static final int PPI_CONTROL      = 0x0059;

  protected static final int PSG_REG          = 0x005a;
  protected static final int PSG_REGS         = 0x005b;

  protected static final int MEM_SIZE         = 0x006b;

  protected static final int CPC_TYPE         = 0x006c;

  protected static final int VER_INT_BLOCK    = 0x006d;
  protected static final int VER_MODES        = 0x006e;

  protected static final int HEADER_SIZE      = 0x0100;

  public void loadVzFile(String name) throws Exception {
    InputStream in = openFile(name);
    try {
      byte[] header = new byte[HEADER_SIZE];
      readStream(in,header,0,HEADER_SIZE);

      z80.setAF(getWord(header,AF));
      z80.setBC(getWord(header,BC));
      z80.setDE(getWord(header,DE));
      z80.setHL(getWord(header,HL));
      z80.setR(header[R]);
      z80.setI(header[I]);
      z80.setIFF1(header[IFF1] != 0);
      z80.setIFF2(header[IFF2] != 0);
      z80.setIX(getWord(header,IX));
      z80.setIY(getWord(header,IY));
      z80.setSP(getWord(header,SP));
      z80.setPC(getWord(header,PC));
      z80.setIM(header[IM]);
      z80.setAF1(getWord(header,AF1));
      z80.setBC1(getWord(header,BC1));
      z80.setDE1(getWord(header,DE1));
      z80.setHL1(getWord(header,HL1));

      gateArray.setSelectedInk(header[GA_PEN]);
      for (int i = 0; i < 0x11; i++)
        gateArray.setInk(i,header[GA_INKS + i]);

      gateArray.setModeAndROMEnable(memory,header[GA_ROM]);
      memory.setRAMBank(header[GA_RAM]);

      crtc.setSelectedRegister(header[CRTC_REG]);
      for (int i = 0; i < 18; i++)
        crtc.setRegister(i,header[CRTC_REGS + i]);

      ppi.setControl(header[PPI_CONTROL] & 0xff | 0x80);
      ppi.setOutputValue(PPI8255.PORT_A,header[PPI_A] & 0xff);
      ppi.setOutputValue(PPI8255.PORT_B,header[PPI_B] & 0xff);
      int portC = header[PPI_C] & 0xff;
      ppi.setOutputValue(PPI8255.PORT_C,portC);

      psg.setBDIR_BC2_BC1(PSG_VALUES[portC >> 6],ppi.readOutput(PPI8255.PORT_A));

      psg.setSelectedRegister(header[PSG_REG]);
      for (int i = 0; i < 14; i++)
        psg.setRegister(i,header[PSG_REGS + i] & 0xff);

      int memSize = (header[MEM_SIZE] & 0xff) * 1024;
      memSize = Math.min(memSize,65536);
      byte[] mem = memory.getMemory();
      readStream(in,mem,0,memSize);

    } finally {
      in.close();
    }
  }

  public void displayLostFocus() {
    keyboard.reset();
  }

}