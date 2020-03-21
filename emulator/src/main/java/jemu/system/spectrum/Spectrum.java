/*
 * Spectrum.java
 *
 * Created on 30 August 2006, 15:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.system.spectrum;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.InputStream;

import jemu.core.cpu.Processor;
import jemu.core.cpu.Z80;
import jemu.core.device.Computer;
import jemu.core.device.DeviceMapping;
import jemu.core.device.keyboard.MatrixKeyboard;
import jemu.core.device.sound.SoundPlayer;
import jemu.core.device.sound.SoundUtil;
import jemu.ui.Display;
import jemu.util.diss.Disassembler;
import jemu.util.diss.DissZ80;

/**
 *
 * @author Richard
 */
public class Spectrum extends Computer {
  
  protected static final int CYCLES_PER_SECOND = 3500000;
  protected static final int AUDIO_TEST        = 0x40000000;
  
  protected Z80 z80 = new Z80(CYCLES_PER_SECOND);
  protected Memory memory = new Memory();
  protected VideoULA video = new VideoULA(this);
  protected Disassembler disassembler = new DissZ80();
  protected MatrixKeyboard keyboard = new jemu.system.zx.Keyboard();
  protected int cycles = 0;
  protected SoundPlayer player = SoundUtil.getSoundPlayer(false);
  protected byte soundByte = 0;
  protected int soundUpdate = 0;
  protected int audioAdd = player.getClockAdder(AUDIO_TEST, CYCLES_PER_SECOND / 4);
  
  protected static final byte RLE_TAG = (byte)0xed;

  /** Creates a new instance of Spectrum */
  public Spectrum() {
    super("Spectrum");
    video.setMemory(memory);
    z80.setMemoryDevice(video);
    z80.setCycleDevice(this);
    z80.addInputDeviceMapping(new DeviceMapping(this,0x0001,0x0000));
    z80.addInputDeviceMapping(new DeviceMapping(video,0x0001,0x0001));
    z80.addOutputDeviceMapping(new DeviceMapping(this,0x0001,0x0000));
    setBasePath("spectrum");
    player.play();
  }
  
  public void initialise() {
    memory.setROM(getFile(romPath + "ZX48.ROM", 16384));
    super.initialise();
  }
  
  public void setFrameSkip(int value) {
    super.setFrameSkip(value);
    video.setRendering(value == 0);
  }

  protected void vSync() {
    if (frameSkip == 0)
      display.updateImage(true);
    syncProcessor();
  }
  
  public void cycle() {
    if ((cycles++ & 0x03) == 0) {
      video.cycle();
      soundUpdate += audioAdd;
      if ((soundUpdate & AUDIO_TEST) != 0) {
        soundUpdate -= AUDIO_TEST;
        player.writeMono(soundByte);
      }
    }
  }
  
  public Processor getProcessor() {
    return z80;
  }
  
  public Memory getMemory() {
    return memory;
  }
  
  public Disassembler getDisassembler() {
    return disassembler;
  }

  public Dimension getDisplaySize() {
    return video.getDisplaySize(true);
  }

  public void setDisplay(Display value) {
    super.setDisplay(value);
    video.setDisplay(value);
  }

  public int readPort(int port) {
    //System.out.println("read port: " + Util.hex((short)port));
    return keyboard.readPort(port);
  }

  public void writePort(int port, int value) {
    //System.out.println("Write port: " + Util.hex((short)port) + ", " + Util.hex((byte)value));
    soundByte = (value & 0x10) == 0 ? (byte)0x7f : (byte)0;
    video.setBorder(value & 0x07);
  }

  public void processKeyEvent(KeyEvent e) {
    if (mode == RUN) {
      if (e.getID() == KeyEvent.KEY_PRESSED) {
        keyboard.keyPressed(e.getKeyCode());
      }
      else if (e.getID() == KeyEvent.KEY_RELEASED)
        keyboard.keyReleased(e.getKeyCode());
    }
  }

	public void loadVzFile(String name) throws Exception {
    InputStream in = openFile(name);
    
    try {
      byte[] header = new byte[30];
      byte[] buff = new byte[65536];

      readStream(in,header,0,30);

      z80.setAF(getWordBE(header,0));
      z80.setBC(getWord(header,2));
      z80.setHL(getWord(header,4));
      z80.setPC(getWord(header,6));
      z80.setSP(getWord(header,8));
      z80.setI(header[10] & 0xff);
      int brhc = header[12] & 0xff;
      z80.setR((header[11] | ((brhc & 0x01) << 7)) & 0xff);

      video.setBorder(brhc == 0xff ? 0 : (brhc >> 1) & 0x07);

      boolean compressed = (brhc & 0x20) != 0;
      z80.setDE(getWord(header,13));
      z80.setBC1(getWord(header,15));
      z80.setDE1(getWord(header,17));
      z80.setHL1(getWord(header,19));
      z80.setAF1(getWordBE(header,21));

      z80.setIY(getWord(header,23));
      z80.setIX(getWord(header,25));
      z80.setIFF1(header[27] != 0);
      z80.setIFF2(header[28] != 0);
      int im = header[29] & 0x03;
      z80.setIM(im == 3 ? 2 : im);

      byte[] mem = memory.getMemory();
      if (z80.getPC() == 0) {
        int type = in.read();
        if (in.read() != 0 || (type != 23 && type != 54 && type != 58))
          throw new Exception("Unsupported Extended Z80 Snapshot Version");
        byte[] hdr = new byte[type];
        readStream(in,hdr,0,type);
        z80.setPC(getWord(hdr,0));
        for (int blk = 0; blk < 3; blk++) {
          readStream(in,hdr,0,3);
          int size = getWord(hdr,0);
          int addr = 0xc000;
          switch(hdr[2]) {
            case 4:  addr = 0x8000; break;
            case 5:  addr = 0xc000; break;
            case 8:  addr = 0x4000; break;
            default: throw new Exception("Illegal page: " + hdr[2]);
          }
          if (size == 0xffff)
            readStream(in,mem,addr,0x4000);
          else
            decompress(buff,mem,addr,readStream(in,buff,0,size));
        }
      }
      else if ((brhc & 0x20) != 0)
        decompress(buff,mem,0x4000,readStream(in,buff,0,65536,false));
      else
        in.read(mem,0x4000,0xc000);
    } finally {
      in.close();
    }
  }
  
  protected void decompress(byte[] buff, byte[] mem, int addr, int size) {
    int ofs = 0;
    byte data;
    while (addr < 0x10000 && ofs < size) {
      data = buff[ofs++];
      if (data != RLE_TAG)
        mem[addr++] = data;
      else {
        data = buff[ofs];
        if (data != RLE_TAG)
          mem[addr++] = RLE_TAG;
        else {
          ofs++;
          int count = buff[ofs++] & 0xff;
          data = buff[ofs++];
          while (count-- > 0)
            mem[addr++] = data;
        }
      }
    }
  }

}