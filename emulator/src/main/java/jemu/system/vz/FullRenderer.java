/*
 * FullRenderer.java
 *
 * Created on 12 June 2006, 16:12
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.system.vz;

import jemu.core.cpu.Z80;
import jemu.core.device.DeviceMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class FullRenderer extends SimpleRenderer {
    private static final Logger log = LoggerFactory.getLogger(FullRenderer.class);

    protected static final int BORDER = 16;  // Must be divisible by 8
    protected static final int WIDTH = 256 + BORDER * 2;
    protected static final int HEIGHT = 192 + BORDER * 2;
    protected static final int CWIDTH = WIDTH / 8;  // Width in chars  (2)
    protected static final int CBORDER = BORDER / 8; // Border in chars (2)

    protected static final int XOFFSET = -8;   // Number of char widths (4T) from HSYNC to displayed
    protected static final int YOFFSET = -48;
    protected static final int LINE_LENGTH = 32;
    protected static final int XDISPEND = CBORDER + LINE_LENGTH;
    protected static final int YDISPEND = BORDER + 192;
    protected static final int CYCLE_ADJUST = 206;

    protected byte[][] mem;
    protected int data;
    protected int y = YOFFSET;
    protected int x = 0;
    protected int line = 0;
    protected int crow = 0;
    protected int offset = 0;
    protected int count = 0;
    protected int addr = 0x7000;
    protected int bitOffset = 0;
    protected int lineStart = 0x7000;
    protected int vertAdjust = YOFFSET;
    protected int usedBorder;
    protected int usedMask;
    protected int snowMask = 0x03;

    /**
     * Creates a new instance of FullRenderer
     */
    public FullRenderer(VZMemory memory) {
        super("Acurate VZ Renderer");
        mem = memory.getGfxMemory();
    }

    public void register(Z80 z80) {
        z80.addOutputDeviceMapping(new DeviceMapping(this, 0xf0, 0x20));
    }

    @Override
    public void writePort(int port, int value) {
        setPort32(value);
    }

    // This is 1 for a VZ-300. FS stops one line earlier
    public void setVerticalAdjustment(int value) {
        y = vertAdjust = YOFFSET + value;
    }

    public void renderScreen(VZMemory memory) {
        offset = 0;
        x = -BORDER;
        y = vertAdjust;
        addr = lineStart = 0x7000;
        bitOffset = 8 - mode.getBitsPerScan();
        crow = 0;
        line = 0;
        count = CYCLE_ADJUST;
        display.updateImage(true);
    }

    public void cycle() {
        if ((++count & 3) == 0) {
            if (y >= 0 && y < HEIGHT && x >= 0) {
                if (x < CWIDTH) {
                    if (y >= BORDER && y < YDISPEND && x >= CBORDER && x < XDISPEND) {
                        int ch = data * mode.getMapMult() + crow;
                        try {
                            System.arraycopy(mode.getPixelMap(), ch, pixels, offset, 8);
                        } catch (Exception e) {
                            log.error("ERROR rendering screen at x={}, y={}, data={}, offset={}, char={}", x, y, data,
                                      offset, ch, e);
                        }
                        offset += 8;
                        usedBorder = mode.getBorder();
                        usedMask = mode.getBorderMask();
                        bitOffset -= mode.getBitsPerScan();
                        if (bitOffset < 0) {
                            bitOffset = 8 - mode.getBitsPerScan();
                            addr++;
                        }
                    } else {
                        for (int i = 0; i < 8; i++) {
                            pixels[offset++] = usedBorder & usedMask;
                        }
                    }
                    data = getData(addr, bitOffset);
                }
            }
            if (count == VZ.CYCLES_PER_SCAN) {
                count = 0;
                // x = -pixelPerByte * mode.getBytesPerCycle();
                x = XOFFSET;
                if (y++ >= BORDER) {
                    // if ((cline -= mode.getMapMult()) <= 0) {
                    //     cline = 24;
                    line++;
                    if (line >= mode.getPixelHeight()) {
                        line = 0;
                        if ((crow += 8) >= mode.getMapMult()) {
                            crow = 0;
                            lineStart += mode.getBytesPerLine();
                        }
                    }
                    bitOffset = 8 - mode.getBitsPerScan();
                    addr = lineStart;
                }
                if (y >= BORDER && y < YDISPEND) {
                    usedMask = mode.getBorderMask();
                }
            } else {
                x++;
            }
        }
    }

    private static int[] mask = {0x01, 0x03, 0x07, 0x0f, 0x1f, 0x3f, 0x7f, 0xff};

    private int getData(int addr, int bitOffset) {
        int a = (addr - 0x7000) & 0x7ff;
        int b = ((addr - 0x7000) & 0x3800) >> 11;
        int value = bitOffset == 0 ? mem[b][a] : mem[b][a] >> bitOffset;
        return value & mask[mode.getBitsPerScan() - 1];
    }

    public Dimension getDisplaySize() {
        return new Dimension(WIDTH, HEIGHT);
    }

    public void setSnow(boolean value) {
        snowMask = value ? 0x03 : 0;
    }

    public boolean isSnow() {
        return snowMask != 0;
    }

    public int setData(int value) {
        if ((count & snowMask) == 3) {
            data = value & mask[mode.getBitsPerScan() - 1];
        }
        return value;

    }

}
