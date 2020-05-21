package jemu.system.vz;

import jemu.config.Constants;
import jemu.config.JemuConfiguration;
import jemu.core.cpu.Processor;
import jemu.core.cpu.Z80;
import jemu.core.device.Computer;
import jemu.core.device.memory.Memory;
import jemu.core.device.sound.JavaSound;
import jemu.core.device.sound.SoundPlayer;
import jemu.core.device.sound.SoundUtil;
import jemu.rest.dto.VzSource;
import jemu.rest.security.SecurityService;
import jemu.system.vz.export.VzFileLoader;
import jemu.ui.Display;
import jemu.util.assembler.z80.Assembler;
import jemu.util.diss.Disassembler;
import jemu.util.diss.DissZ80;
import jemu.util.vz.KeyboardController;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

@Component(jemu.system.vz.VZ.BEAN_ID)
public class VZ extends Computer {
    public static final String BEAN_ID = "jemu.system.vz.VZ";

    private static final Logger log = LoggerFactory.getLogger(VZ.class);

    private static final int SYSTEM_BASIC_START = 0x78a4;
    private static final int SYSTEM_BASIC_END = 0x78f9;
    protected static final int CYCLES_PER_SEC_VZ200 = 3579500;
    protected static final int CYCLES_PER_SEC_VZ300 = 3546900;
    protected static final int CYCLES_PER_SCAN = 228;

    protected static final int AUDIO_TEST = 0x40000000;
    protected static final int AUDIO_RESYNC_FRAMES = 50;

    private static int SOUND_LEVEL = 240;
    private int soundLevel = 0;

    protected boolean vz200;
    protected int cyclesPerSecond = CYCLES_PER_SEC_VZ200;
    protected Z80 z80; // This is different for VZ-200 and VZ-300
    protected VZMemory memory;
    protected int cycles = 0;
    protected int frameFlyback = 0x80;
    protected int vdcLatch = 0x00;
    protected SimpleRenderer renderer;
    protected Keyboard keyboard;
    protected Disassembler disassembler;
    protected SoundPlayer player;
    protected VzPrinterDevice printer;
    protected int soundBit = 1;
    protected int soundUpdate = 0;
    protected int audioAdd;
    protected int syncCnt = AUDIO_RESYNC_FRAMES;
    protected int scansOfFlyback;
    protected int scansPerFrame;
    protected int cyclesPerFrame;
    protected int cyclesToFlyback;
    protected VZTapeDevice tapeDevice;
    protected VZLoaderDevice loaderDevice;
    protected VzAudioDevice audioDevice;
    protected VzIpDevice ipDevice;
    private SecurityDevice securityDevice;
    private final JemuConfiguration config;
    private final KeyboardController keyboardController;
    private final SecurityService securityService;

    @Autowired
    public VZ(JemuConfiguration config, VzDirectory vzDirectory, KeyboardController keyboardController,
              SecurityService securityService) {
        super("VZ200");
        this.config = config;

        this.securityService = securityService;

        this.memory = new VZMemory(true); // with 16k Expansion
        this.renderer = new FullRenderer(memory);

        keyboard = new Keyboard();
        disassembler = new DissZ80();
        player = SoundUtil.getSoundPlayer(441, false);

        this.keyboardController = keyboardController.withKeyboard(keyboard);

        vz200 = true;
        if (vz200) {
            cyclesPerSecond = CYCLES_PER_SEC_VZ200;
            scansPerFrame = 312;
            scansOfFlyback = 57;
            renderer.setVerticalAdjustment(0);
        } else {
            cyclesPerSecond = CYCLES_PER_SEC_VZ300;
            scansPerFrame = 310;
            scansOfFlyback = 56;
            renderer.setVerticalAdjustment(1);
        }
        z80 = new Z80(cyclesPerSecond);
        cyclesPerFrame = CYCLES_PER_SCAN * scansPerFrame;
        cyclesToFlyback = cyclesPerFrame - (CYCLES_PER_SCAN * scansOfFlyback);
        audioAdd = player.getClockAdder(AUDIO_TEST, cyclesPerSecond - JavaSound.SAMPLE_RATE);

        z80.setMemoryDevice(this);
        z80.setCycleDevice(this);
        z80.setInterruptDevice(this);

        player.setFormat(SoundUtil.UPCM8);
        setBasePath("vz");
        this.printer = new VzPrinterDevice();
        this.printer.register(z80);
        this.tapeDevice = new VZTapeDevice(this);
        this.tapeDevice.register(z80);
        this.loaderDevice = new VZLoaderDevice(this, vzDirectory);
        this.loaderDevice.register(z80);
        this.audioDevice = new VzAudioDevice(this);
        this.audioDevice.register(z80);
        this.ipDevice = new VzIpDevice();
        this.ipDevice.register(z80);
        this.securityDevice = new SecurityDevice(this, securityService);
        this.securityDevice.register(z80);
    }

    public void initialise() {
        memory.setMemory(0, getFile(romPath + "VZBAS" + (vz200 ? "12" : "20") + ".ROM", 16384));
        if (config.getBoolean(Constants.ENABLE_DOS_ROM, false)) {
            memory.setMemory(0x4000, getFile(romPath + "vzdos.rom", 8192));
            new VzFloppyDevice(this).register(z80);
        }
        SimpleRenderer.setFontData(getFile(romPath + "VZ.CHR", 768));
        super.initialise();
        this.setVolume(config.getInt(Constants.SOUND_VOLUME));
    }

    @Override
    public void softReset() {
        keyboardController.clearCache();
        z80.reset();
    }

    public String getKeyboardImage() {
        return "/jemu/ui/vz/keyboard.png";
    }

    public VZTapeDevice getTapeDevice() {
        return tapeDevice;
    }

    public Memory getMemory() {
        return memory;
    }

    public Processor getProcessor() {
        return z80;
    }

    public Keyboard getKeyboard() {
        return keyboard;
    }

    public void cycle() {
        if (++cycles == cyclesToFlyback) {
            if (--syncCnt == 0) {
                player.sync();
                syncCnt = AUDIO_RESYNC_FRAMES;
            }
            frameFlyback = 0x00;
            z80.setInterrupt(1);
        } else if (cycles == cyclesPerFrame) {
            cycles = 0;
            frameFlyback = 0x80;
            if (frameSkip == 0) {
                renderer.renderScreen(memory);
            }
            syncProcessor();
        }
        this.tapeDevice.cycle();
        soundUpdate += audioAdd;
        if ((soundUpdate & AUDIO_TEST) != 0) {
            soundUpdate -= AUDIO_TEST;
            player.writeMono(soundLevel);
        }

        if (frameSkip == 0) {
            renderer.cycle();
        }
    }

    public void setInterrupt(int mask) {
        z80.clearInterrupt(1);
    }

    public int readByte(int address) {
        if (address >= 0x7000 || address < 0x6800) {
            return memory.readByte(address);
        }
        int lsb = address & 0xff;
        if (lsb == 0xfe || lsb == 0xfd || lsb == 0xfb || lsb == 0xf7 || lsb == 0xef || lsb == 0xdf || lsb == 0xbf ||
            lsb == 0x7f) {
            return frameFlyback | (keyboard.readByte(address) & 0x7f);
        }
        return vdcLatch & 0x40; // cassette input
    }

    public int getVdcLatch() {
        return vdcLatch;
    }

    public int writeByte(int address, int value) {
        if (address >= 0x7800) {
            return memory.writeByte(address, value);
        } else if (address >= 0x7000) {
            return renderer.setData(memory.writeByte(address, value));
        } else if (address >= 0x6800) {
            // check sound
            boolean audiolatch1 = (value & 0x20) != 0;
            boolean audiolatch2 = (value & 0x01) != 0;
            if (audiolatch1 && !audiolatch2) {
                soundLevel = SOUND_LEVEL;
            } else if (audiolatch2 && !audiolatch1) {
                soundLevel = 0;
            } else {
                soundLevel = SOUND_LEVEL / 2;
            }

            vdcLatch = value;
            renderer.setVDCLatch(value);
        }
        return value & 0xff;
    }

    public void setVolume(int volume) {
        this.player.setVolume(volume);
        this.config.set(Constants.SOUND_VOLUME, volume);
        this.config.persist();
    }

    public int getVolume() {
        return this.player.getVolume();
    }

    public void processKeyEvent(KeyEvent e) {
        log.debug(String.format("processKeyEvent - Key pressed: %s", e.toString()));
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_ESCAPE) {
            softReset();
            return;
        }
        if (e.getExtendedKeyCode() == 0x010000d6) { // ö
            keyCode = KeyEvent.VK_PLUS;
        } else if (e.getExtendedKeyCode() == 0x010000c4) { // ä
            keyCode = KeyEvent.VK_NUMBER_SIGN;
        } else if (e.getExtendedKeyCode() == 0x010000df) { // ß
            keyCode = KeyEvent.VK_MINUS;
        }
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            keyboard.keyPressed(keyCode);
        } else if (e.getID() == KeyEvent.KEY_RELEASED) {
            keyboard.keyReleased(keyCode);
        }
    }

    public void loadVzFile(InputStream is) throws IOException {
        byte[] bytes = IOUtils.toByteArray(is);
        VzFileLoader loader = new VzFileLoader(memory);
        VzSource source = new VzSource().withSource(Base64.getEncoder().encodeToString(bytes));
        loader.importData(source);
        if (loader.isAutorun()) {
            z80.setPC(loader.getStartAddress());
        }
    }

    public void saveVzFile(String name, String range, Boolean autorun) throws Exception {
        try (FileOutputStream os = new FileOutputStream(name)) {
            saveVzFile(os, range, autorun);
        }
    }

    public void saveVzFile(OutputStream os, String range, Boolean autorun) throws IOException {
        VzFileLoader loader = new VzFileLoader(memory).withAutorun(autorun);
        if (!StringUtils.isEmpty(range)) {
            loader.withStartAddress(Integer.valueOf(range.split("-")[0], 16))
                  .withEndAddress(Integer.valueOf(range.split("-")[1], 16));
        }
        VzSource source = loader.exportData();
        os.write(Base64.getDecoder().decode(source.getSource().getBytes(StandardCharsets.UTF_8)));
    }

    public List<String> flushPrinter() {
        return printer.flush();
    }

    public void setDisplay(Display value) {
        super.setDisplay(value);
        renderer.setDisplay(value);
    }

    public Dimension getDisplaySize() {
        return renderer.getDisplaySize();
    }

    public Disassembler getDisassembler() {
        return disassembler;
    }

    public void emulate(int mode) {
        player.play();
        super.emulate(mode);
        player.stop();
    }

    public void displayLostFocus() {
        keyboard.reset();
    }

    public void alert(String s) {
        String[] lines = s.split("\n");
        for (int l = 0; l < lines.length; l++) {
            int y = 16 - lines.length + l;
            String line = lines[l];
            for (int i = 0; i < 32; i++) {
                writeByte(0x7000 + y * 32 + i, 0x60);
            }
            printAt(16 - line.length() / 2, y, line, true);
        }
    }

    public void printAt(int x, int y, String s, boolean inverse) {
        int adr = 0x7000 + x + y * 32;
        for (int i = 0; i < s.length(); i++) {
            int c = s.toUpperCase().charAt(i);
            if (c >= 0x40 && c < 0x60) {
                c = c - 0x40;
            }
            writeByte(adr + i, inverse ? c | 0x40 : c);
        }
    }

    public void exportScreenshot(OutputStream out) throws IOException {
        ImageIO.write(renderer.getImage(), "png", out);
    }
}