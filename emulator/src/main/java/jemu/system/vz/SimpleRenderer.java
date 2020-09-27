package jemu.system.vz;

import jemu.core.renderer.Renderer;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public abstract class SimpleRenderer extends Renderer {

    protected static final int WIDTH = 256;
    protected static final int HEIGHT = 192;

    protected static final int BLACK = 0xff000000;
    protected static final int GREEN = 0xff00e000;
    protected static final int YELLOW = 0xffd0ff00;
    protected static final int BLUE = 0xff0000ff;
    protected static final int RED = 0xffff0000;
    protected static final int BUFF = 0xffe0e090;
    protected static final int CYAN = 0xff00ffa0;
    protected static final int MAGENTA = 0xffff00ff;
    protected static final int ORANGE = 0xfff07000;
    protected static final int DK_GREEN = 0xff004000;
    protected static final int BR_GREEN = 0xff00e018;
    protected static final int DK_ORANGE = 0xff401000;
    protected static final int BR_ORANGE = 0xffffc418;

    protected static final int[] TEXT_FG = {GREEN, YELLOW, BLUE, RED, BUFF, CYAN, MAGENTA, ORANGE};

    protected static final int[] GREEN_GR = {GREEN, YELLOW, BLUE, RED};
    protected static final int[] BUFF_GR = {BUFF, CYAN, ORANGE, MAGENTA};

    protected static final int[] MONO_GREEN_GR = {BR_GREEN, DK_GREEN};
    protected static final int[] MONO_BUFF_GR = {BR_ORANGE, DK_ORANGE};

    protected static int[] greenText = new int[0x6000];
    protected static int[] orangeText = new int[0x6000];

    protected static int[] greenLowresGraphics = new int[0x400];
    protected static int[] buffLowresGraphics = new int[0x400];
    protected static int[] greenGraphics = new int[0x800];
    protected static int[] buffGraphics = new int[0x800];

    protected static int[] greenMonoGraphics = new int[0x400];
    protected static int[] buffMonoGraphics = new int[0x400];
    protected static int[] greenMonoHiresGraphics = new int[0x800];
    protected static int[] buffMonoHiresGraphics = new int[0x800];

    static {
        int offs = 0;
        for (int i = 0; i < 127; i++) {
            for (int pix = 0; pix < 2; pix++) {
                int colour = ((i << (pix << 1)) & 0x0c) >> 2;
                greenLowresGraphics[offs] = greenLowresGraphics[offs + 1] = greenLowresGraphics[offs + 2] =
                greenLowresGraphics[offs + 3] = GREEN_GR[colour];
                buffLowresGraphics[offs] = buffLowresGraphics[offs + 1] = buffLowresGraphics[offs + 2] =
                buffLowresGraphics[offs + 3] = BUFF_GR[colour];
                offs += 4;
            }
        }
        offs = 0;
        for (int i = 0; i < 256; i++) {
            for (int pix = 0; pix < 4; pix++) {
                int colour = ((i << (pix << 1)) & 0xc0) >> 6;
                greenGraphics[offs + 1] = greenGraphics[offs] = GREEN_GR[colour];
                buffGraphics[offs + 1] = buffGraphics[offs] = BUFF_GR[colour];
                offs += 2;
            }
        }

        offs = 0;
        for (int i = 0; i < 128; i++) {
            int m = 0x08;
            for (int pix = 0; pix < 4; pix++) {
                int colour = (i & m) == 0 ? 0 : 1;
                greenMonoGraphics[offs] = greenMonoGraphics[offs + 1] = MONO_GREEN_GR[colour];
                buffMonoGraphics[offs] = buffMonoGraphics[offs + 1] = MONO_BUFF_GR[colour];
                offs += 2;
                m = m >> 1;
            }
        }
        offs = 0;
        for (int i = 0; i < 256; i++) {
            int m = 0x80;
            for (int pix = 0; pix < 8; pix++) {
                int colour = (i & m) == 0 ? 0 : 1;
                greenMonoHiresGraphics[offs] = MONO_GREEN_GR[colour];
                buffMonoHiresGraphics[offs] = MONO_BUFF_GR[colour];
                offs++;
                m = m >> 1;
            }
        }
    }

    enum Mode {
        //@formatter:off
        TEXT_GREEN(256, 192, 1,12, GREEN, BLACK, greenText),
        TEXT_BUFF(256, 192, 1, 12, ORANGE, BLACK, orangeText),
        GFX_GREEN(128, 64, 2, 1, GREEN, -1, greenGraphics),
        GFX_BUFF(128, 64, 2,1, BUFF, -1, buffGraphics),

        ULTRA_0_GREEN(64, 64,2, 1, GREEN, -1, greenLowresGraphics),
        ULTRA_1_GREEN(128, 64,1, 1, BR_GREEN, BR_GREEN, greenMonoGraphics),
        ULTRA_2_GREEN(128, 64,2, 1, GREEN, -1, greenGraphics),
        ULTRA_3_GREEN(128, 96,1, 1, BR_GREEN, BR_GREEN, greenMonoGraphics),
        ULTRA_4_GREEN(128, 96,2, 1, GREEN, -1, greenGraphics),
        ULTRA_5_GREEN(128, 192,1, 1, BR_GREEN, BR_GREEN, greenMonoGraphics),
        ULTRA_6_GREEN(128, 192,2, 1, GREEN, -1, greenGraphics),
        ULTRA_7_GREEN(256, 192,1, 1, BR_GREEN, BR_GREEN, greenMonoHiresGraphics),

        ULTRA_0_BUFF(64, 64,2, 1, BUFF, -1, buffLowresGraphics),
        ULTRA_1_BUFF(128, 64,1, 1, BR_ORANGE, BR_ORANGE, buffMonoGraphics),
        ULTRA_2_BUFF(128, 64,2, 1, BUFF, -1, buffGraphics),
        ULTRA_3_BUFF(128, 96,1, 1, BR_ORANGE, BR_ORANGE, buffMonoGraphics),
        ULTRA_4_BUFF(128, 96,2, 1, BUFF, -1, buffGraphics),
        ULTRA_5_BUFF(128, 192,1, 1, BR_ORANGE, BR_ORANGE, buffMonoGraphics),
        ULTRA_6_BUFF(128, 192,2, 1, BUFF, -1, buffGraphics),
        ULTRA_7_BUFF(256, 192,1, 1, BR_ORANGE, BR_ORANGE, buffMonoHiresGraphics)
        ;
        //@formatter:on

        private final int width, height;
        private final int bitsPerPixel;
        private final int uniqueLines;
        private final int border;
        private final int borderMask;
        private final int[] pixelMap;

        private final int pixelWidth, pixelHeight;
        private final int pixelPerByte;
        private final int pixelPerScan;
        private final int bitsPerScan;
        private final int mapMult;
        private final int bytesPerLine;

        Mode(int width, int height, int bitsPerPixel, int uniqueLines, int border, int borderMask, int[] pixelMap) {
            this.width = width;
            this.height = height;
            this.bitsPerPixel = bitsPerPixel;
            this.uniqueLines = uniqueLines;
            this.border = border;
            this.borderMask = borderMask;
            this.pixelMap = pixelMap;

            this.pixelWidth = WIDTH / this.width;
            this.pixelHeight = HEIGHT / this.height;
            this.pixelPerByte = 8 / this.bitsPerPixel;
            this.pixelPerScan = 8 / pixelWidth;
            this.bitsPerScan = pixelPerScan * this.bitsPerPixel;
            this.mapMult = 8 * this.uniqueLines;
            this.bytesPerLine = 4 * bitsPerScan;
        }

        public int getBytesPerLine() {
            return bytesPerLine;
        }

        public int getMapMult() {
            return mapMult;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getBitsPerPixel() {
            return bitsPerPixel;
        }

        public int getUniqueLines() {
            return uniqueLines;
        }

        public int getBorder() {
            return border;
        }

        public int getBorderMask() {
            return borderMask;
        }

        public int[] getPixelMap() {
            return pixelMap;
        }

        public int getPixelWidth() {
            return pixelWidth;
        }

        public int getPixelHeight() {
            return pixelHeight;
        }

        public int getPixelPerByte() {
            return pixelPerByte;
        }

        public int getPixelPerScan() {
            return pixelPerScan;
        }

        public int getBitsPerScan() {
            return bitsPerScan;
        }
    }

    //@formatter:off
    private Mode ultraModes[] = {Mode.ULTRA_0_GREEN,
                                 Mode.ULTRA_1_GREEN,
                                 Mode.ULTRA_2_GREEN,
                                 Mode.ULTRA_3_GREEN,
                                 Mode.ULTRA_4_GREEN,
                                 Mode.ULTRA_5_GREEN,
                                 Mode.ULTRA_6_GREEN,
                                 Mode.ULTRA_7_GREEN,

                                 Mode.ULTRA_0_BUFF,
                                 Mode.ULTRA_1_BUFF,
                                 Mode.ULTRA_2_BUFF,
                                 Mode.ULTRA_3_BUFF,
                                 Mode.ULTRA_4_BUFF,
                                 Mode.ULTRA_5_BUFF,
                                 Mode.ULTRA_6_BUFF,
                                 Mode.ULTRA_7_BUFF
    };
    //@formatter:on

    protected int vdcLatch = 0x00;
    protected int port32 = 8;
    protected Mode mode = Mode.TEXT_GREEN;

    public SimpleRenderer() {
        super("VZ Simple Screen Renderer");
    }

    protected SimpleRenderer(String type) {
        super(type);
    }

    abstract public void setVerticalAdjustment(int value);

    public static void setFontData(byte[] data) {
        int offs = 0;
        for (int i = 0; i < WIDTH; i++) {
            int fg = TEXT_FG[(i >> 4) & 0x07];
            for (int y = 0; y < 12; y++) {
                int value;
                if (i >= 128) {
                    int bits = (y < 6 ? i >> 2 : i) & 0x03;
                    value = (bits & 0x01) != 0 ? 0x0f : 0x00;
                    if ((bits & 0x02) != 0) {
                        value |= 0xf0;
                    }
                } else if (i < 64) {
                    value = data[i * 12 + y] & 0xff;
                } else {
                    value = (data[(i & 0x3f) * 12 + y] & 0xff) ^ 0xff;
                }
                for (int x = 0; x < 8; x++) {
                    if (i >= 128)
                    //            greenText[offs] = orangeText[offs] = (value & 0x01) != 0 ? fg : BLACK;
                    {
                        greenText[offs] = orangeText[offs] = (value & 0x80) != 0 ? fg : BLACK;
                    } else {
                        //            greenText[offs] = (value & 0x01) == 0 ? DK_GREEN : BR_GREEN;
                        //            orangeText[offs] = (value & 0x01) == 0 ? DK_ORANGE : BR_ORANGE;
                        greenText[offs] = (value & 0x80) == 0 ? DK_GREEN : BR_GREEN;
                        orangeText[offs] = (value & 0x80) == 0 ? DK_ORANGE : BR_ORANGE;
                    }
                    offs++;
                    value <<= 1;
                }
            }
        }
    }

    public void setVDCLatch(int value) {
        this.vdcLatch = value;
        setGfxMode();
    }

    public void setPort32(int value) {
        port32 = value;
        setGfxMode();
    }

    private void setGfxMode() {
        switch (vdcLatch & 0x18) {
            case 0x00:
                mode = Mode.TEXT_GREEN;
                break;
            case 0x08:
                mode = ultraModes[(port32 >> 2) & 0x07]; // bits 2-4 -> bits 0-2
                //                mode = Mode.GFX_GREEN;
                break;
            case 0x10:
                mode = Mode.TEXT_BUFF;
                break;
            case 0x18:
                mode = ultraModes[((port32 >> 2) & 0x07) + 8]; // bits 2-4 -> bits 0-2
                //                mode = Mode.GFX_BUFF;
                break;
        }
    }

    public abstract void renderScreen(VZMemory memory);

    public Dimension getDisplaySize() {
        return new Dimension(WIDTH, HEIGHT);
    }

    public int setData(int value) {
        return value;
    }

    public BufferedImage getImage() {
        return getDisplay().getImage();
    }
}
