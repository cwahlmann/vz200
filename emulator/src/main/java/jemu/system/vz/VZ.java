package jemu.system.vz;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jemu.core.cpu.Processor;
import jemu.core.cpu.Z80;
import jemu.core.device.Computer;
import jemu.core.device.memory.Memory;
import jemu.core.device.sound.SoundPlayer;
import jemu.core.device.sound.SoundUtil;
import jemu.ui.Display;
import jemu.util.assembler.z80.Assembler;
import jemu.util.diss.Disassembler;
import jemu.util.diss.DissZ80;

/**
 * @author Christian
 *
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
	protected Z80 z80 = new Z80(cyclesPerSecond); // This is different for VZ-200 and VZ-300
	protected VZMemory memory = new VZMemory(this);
	protected int cycles = 0;
	protected int frameFlyback = 0x80;
	protected int vdcLatch = 0x00;
	protected SimpleRenderer renderer = new FullRenderer(memory); 
	protected Keyboard keyboard = new Keyboard();
	protected Disassembler disassembler = new DissZ80();
	protected SoundPlayer player = SoundUtil.getSoundPlayer(441, false);
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

	public VZ() {
		super("VZ200");
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
		z80.setCyclesPerSecond(cyclesPerSecond);
		cyclesPerFrame = CYCLES_PER_SCAN * scansPerFrame;
		cyclesToFlyback = cyclesPerFrame - (CYCLES_PER_SCAN * scansOfFlyback);
		audioAdd = player.getClockAdder(AUDIO_TEST, cyclesPerSecond);
		z80.setMemoryDevice(this);
		z80.setCycleDevice(this);
		z80.setInterruptDevice(this);
		
		player.setFormat(SoundUtil.UPCM8);
		setBasePath("vz");
		this.printer = new VzPrinterDevice();
		this.printer.register(z80);
		this.tapeDevice = new VZTapeDevice(this);
		this.tapeDevice.register(z80);
		this.loaderDevice = new VZLoaderDevice(this);
		this.loaderDevice.register(z80);
		this.audioDevice = new VzAudioDevice(this);
		this.audioDevice.register(z80);
	}

	public void initialise() {
		memory.setMemory(0, getFile(romPath + "VZBAS" + (vz200 ? "12" : "20") + ".ROM", 16384));
		SimpleRenderer.setFontData(getFile(romPath + "VZ.CHR", 768));
		super.initialise();
		this.setVolume(127);
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
			if (frameSkip == 0)
				renderer.renderScreen(memory);
			syncProcessor();
		}
		this.tapeDevice.cycle();
		soundUpdate += audioAdd;
		if ((soundUpdate & AUDIO_TEST) != 0) {
			soundUpdate -= AUDIO_TEST;
			player.writeMono(soundLevel);
		}

		if (frameSkip == 0)
			renderer.cycle();
	}

	public void setInterrupt(int mask) {
		z80.clearInterrupt(1);
	}

	public int readByte(int address) {
		if (address >= 0x7000 || address < 0x6800) {
			return memory.readByte(address);
		}
		int lsb = address & 0xff;
		if (lsb == 0xfe || lsb == 0xfd || lsb == 0xfb || lsb == 0xf7 || lsb == 0xef || lsb == 0xdf || lsb == 0xbf
				|| lsb == 0x7f) {
			return frameFlyback | (keyboard.readByte(address) & 0x7f);
		}
		return vdcLatch & 0x40; // cassette input
	}

	public int getVdcLatch() {
		return vdcLatch;
	}

	public int writeByte(int address, int value) {
		if (address >= 0x7800)
			return memory.writeByte(address, value);
		else if (address >= 0x7000)
			return renderer.setData(memory.writeByte(address, value));
		else if (address >= 0x6800) {

			// check sound
			boolean audiolatch1 = (vdcLatch & 0x20) != 0;
			boolean audiolatch2 = (vdcLatch & 0x01) != 0;
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
	}

	public int getVolume() {
		return this.player.getVolume();
	}

	public void processKeyEvent(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_ESCAPE) {
			reset();
			return;
		}
		if (e.getExtendedKeyCode() == 0x010000d6) { // ö
			keyCode = KeyEvent.VK_PLUS;
		} else if (e.getExtendedKeyCode() == 0x010000c4) { // ä
			keyCode = KeyEvent.VK_NUMBER_SIGN;
		} else if (e.getExtendedKeyCode() == 0x010000df) { // ß
			keyCode = KeyEvent.VK_MINUS;
		}
		if (e.getID() == KeyEvent.KEY_PRESSED)
			keyboard.keyPressed(keyCode);
		else if (e.getID() == KeyEvent.KEY_RELEASED)
			keyboard.keyReleased(keyCode);
	}

	protected void vzFileException(String error) throws Exception {
		if (error == null)
			error = "Bad VZ File format";
		throw new Exception(error);
	}

	public void loadSourceFile(InputStream is) throws Exception {
		VzBasicLoader loader = new VzBasicLoader(getMemory());
		loader.loadBasFile(is);
	}

	public void loadBinaryFile(InputStream is) throws Exception {
		try {
			byte[] header = new byte[24];
			int len = is.read(header);
			if (len != 24) /* || !"VZF0".equals(new String(header,0,4))) - Doesn't test this */
				vzFileException(null);

			int type = header[21] & 0xff;
			int start = (header[22] & 0xff) + 256 * (header[23] & 0xff);
			int address = start;

			int read;
			do {
				read = is.read();
				if (read != -1) {
					memory.writeByte(address, read);
					address = (address + 1) & 0xffff;
				}
			} while (read != -1);
			is.close();

			if (type == 0xf0) {
				memory.writeByte(0x78a4, header[22]);
				memory.writeByte(0x78a5, header[23]);
				memory.writeByte(0x78f9, address);
				memory.writeByte(0x78fa, address >> 8);
				// Insert RUN<RETURN> in keyboard
			} else if (type == 0xf1)
				z80.setPC(start);
		} finally {
			is.close();
		}
	}

	class HexFileAddress {
		int start;
		int address;

		HexFileAddress(int start, int address) {
			this.start = start;
			this.address = address;
		}
	}

	public void loadHexFile(InputStream is) throws Exception {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			HexFileAddress hfa = new HexFileAddress(-1, 0x7ae9);
			reader.lines().forEach(line -> {
				int indexAddress = line.indexOf(':');
				String bytes = line.trim();
				if (indexAddress > 0) {
					String startString = line.substring(0, indexAddress).trim();
					hfa.address = Integer.valueOf(startString, 16);
					System.out.println(String.format("Adress = %04x", hfa.address));
					if (hfa.start < 0) {
						hfa.start = hfa.address;
					}
					bytes = line.substring(indexAddress + 1).trim();
				}
				for (String b : bytes.split(" ")) {
					memory.writeByte(hfa.address, Integer.valueOf(b.trim(), 16).intValue());
					System.out.println(b);
					hfa.address = (hfa.address + 1) & 0xffff;
				}
			});
			if (hfa.start < 0) {
				hfa.start = hfa.address;
			}
			log.info(String.format("Start at %04x...", hfa.start));
			z80.setPC(hfa.start);
		}
	}

	public String loadAsmFile(String name, Boolean autorun) throws Exception {
		Assembler asm = new Assembler(getMemory());
		String result = asm.assemble(Paths.get(name));
		log.info(String.format("Start at %04x...", asm.getRunAddress()));
		if (autorun) {
			z80.setPC(asm.getRunAddress());
		}
		return result;
	}

	@Override
	public String loadAsmFile(InputStream is, Boolean autorun) throws Exception {
		Assembler asm = new Assembler(getMemory());
		String result = asm.assemble(is);
		log.info(String.format("Start at %04x...", asm.getRunAddress()));
		if (autorun) {
			z80.setPC(asm.getRunAddress());
		}
		return result;
	}

	@Override
	public String loadAsmZip(InputStream is, Boolean autorun) throws Exception {
		Assembler asm = new Assembler(getMemory());
		String result = asm.assembleZipStream(is);
		log.info(String.format("Start at %04x...", asm.getRunAddress()));
		if (autorun) {
			z80.setPC(asm.getRunAddress());
		}
		return result;
	}

	public void saveFile(String name, String range, Boolean autorun) throws Exception {
		try (FileOutputStream os = new FileOutputStream(name)) {
			saveFile(os, range, autorun);
		}
	}

	public void saveFile(OutputStream os, String range, Boolean autorun) throws Exception {
		int type = autorun ? 0xf1 : 0xf0;
		int endOfBasicPointer;
		int startOfBasicPointer;
		if (StringUtils.isEmpty(range)) {
			endOfBasicPointer = memory.readWord(SYSTEM_BASIC_END);
			startOfBasicPointer = memory.readWord(SYSTEM_BASIC_START);
		} else {
			startOfBasicPointer = Integer.valueOf(range.split("-")[0], 16);
			endOfBasicPointer = Integer.valueOf(range.split("-")[1], 16);
		}
		byte[] header = new byte[24];
		header[21] = (byte) type;
		header[22] = (byte) (startOfBasicPointer & 0xff);
		header[23] = (byte) ((startOfBasicPointer >> 8) & 0xff);
		header[0] = 'V';
		header[1] = 'Z';
		header[2] = 'F';
		header[3] = '0';
		for (int i = 4; i < 21; i++) {
			header[i] = (i - 4 < name.length()) ? (byte) name.charAt(i - 4) : 0;
		}
		os.write(header);
		for (int address = startOfBasicPointer; address < endOfBasicPointer; address++) {
			os.write(memory.readByte(address));
		}
	}

	public String disassemble(int startAdress, int endAdress) {
		StringBuilder result = new StringBuilder();
		DissZ80 da = new DissZ80();
		int[] address = new int[] { startAdress };
		while (address[0] <= endAdress) {
			int a0 = address[0];
			String asm = da.disassemble(memory, address);
			int a1 = address[0];
			String bytes = "";
			for (int a = a0; a < a1; a++) {
				bytes = bytes + String.format("%02x ", readByte(a));
			}
			while (bytes.length() < 12) {
				bytes = bytes + " ";
			}
			result.append(String.format("%04x: ", a0)).append(bytes).append(asm).append("\n");
		}
		return result.toString();
	}

	public List<String> flushPrinter() {
		return printer.flush();
	}

	public void setDisplay(Display value) {
		super.setDisplay(value);
		renderer.setDisplay(value);
	}

	public Dimension getDisplaySize(boolean large) {
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
		for (int i = 0; i < 32; i++) {
			writeByte(0x71e0 + i, 0x60);
		}
		printAt(16 - s.length() / 2, 15, s, true);
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
}