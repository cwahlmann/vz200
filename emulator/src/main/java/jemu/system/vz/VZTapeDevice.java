package jemu.system.vz;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jemu.core.cpu.Z80;
import jemu.core.device.Device;
import jemu.core.device.DeviceMapping;

public class VZTapeDevice extends Device {
	private static final Logger log = LoggerFactory.getLogger(VZTapeDevice.class);
	private static final String DEVICE_ID = "VZ Tape Device";

	public static enum Mode {
		idle, record, play
	}

	public static final int IN_PORT_MASK = 0xfe;
	public static final int IN_PORT_TEST = 0xfe;
	public static final int OUT_PORT_MASK = 0xfe;
	public static final int OUT_PORT_TEST = 0xfe;
	public static final int COMMAND_STOP = 0x00;
	public static final int COMMAND_PLAY = 0x01;
	public static final int COMMAND_RECORD = 0x10;
	public static final int QUERY_POS_H = 0x01;
	public static final int QUERY_POS_L = 0x02;

	public static final int OUTPUT_MASK = 0x06;
	public static final int INPUT_MASK = 0x40;
	public static final int SOUND_MASK = 0x21;
	public static final int SOUND_MASK_1 = 0x01;
	public static final int SOUND_MASK_2 = 0x20;
	public static final int LATCH = 0x6800;

	private int value = 0;
	private static final int maxCount = Integer.MAX_VALUE - 1;
	private int count = 1;
	private Mode mode;

	private VZ vz;
	private VZTape tapeSlot;
	private String tapeName;
	private int slot;

	public VZTapeDevice(VZ vz) {
		super(DEVICE_ID);
		this.vz = vz;
		this.slot = 0;
		this.tapeName = "default";
		loadSlot();
		this.mode = Mode.idle;
	}

	public void register(Z80 z80) {
		z80.addInputDeviceMapping(new DeviceMapping(this, IN_PORT_MASK, IN_PORT_TEST));
		z80.addOutputDeviceMapping(new DeviceMapping(this, OUT_PORT_MASK, OUT_PORT_TEST));
	}

	private void loadSlot() {
		// path
		Path path = getTapePath();
		if (!path.toFile().exists()) {
			this.tapeSlot = new VZTape();
			return;
		}
		this.tapeSlot = new VZTape();
		try (InputStream is = new FileInputStream(path.toFile())) {
			this.tapeSlot.read(is);
		} catch (Exception e) {
			log.error("unable to read tape slot {} at path {}", slot, path.toString());
		}
	}
	
	private void saveSlot() {
		// path
		Path path = getTapePath();
		try (OutputStream os = new FileOutputStream(path.toFile())) {
			this.tapeSlot.write(os);
		} catch (Exception e) {
			log.error("unable to read tape slot {} at path {}", slot, path.toString());
		}
	}

	private Path getTapePath() {
		Path path = Paths.get(System.getProperty("user.home"), "vz200", "tape", tapeName);
		if (!path.toFile().exists()) {
			path.toFile().mkdirs();
		}
		return path.resolve(String.format("tape_%05d.data", slot));
	}
	// -------- manage output --

	@Override
	public void writePort(int port, int value) {
		int p = port & 0xff;
		if (p == 0xfe) {
			switch (value) {
			case COMMAND_STOP:
				stop();
				break;
			case COMMAND_PLAY:
				play();
				break;
			case COMMAND_RECORD:
				record();
				break;
			default:
			}
			return;
		}
		slot(value);
	}

	@Override
	public int readPort(int port) {
		int p = port & 0xff;
		if (p == 0xfe) {
			return slot & 0xff;
		}
		return (slot / 256) & 0xff;
	}

	//

	public int slot() {
		return slot;
	}
	
	public void slot(int slot) {
		stop();
		this.slot = slot;
		log.info("rewind tape to slot {}", slot);
		loadSlot();
	}
	
	public VZTapeDevice tapeSlot(VZTape tape) {
		this.tapeSlot = tape;
		return this;
	}

	public VZTape tapeSlot() {
		return tapeSlot;
	}

	public void changeTape(String tapeName) {
		stop();
		log.info("change tape to [{}]", tapeName);
		this.tapeName = tapeName;
		this.slot = 0;
		loadSlot();
	}
	
	public String getTapeName() {
		return this.tapeName;
	}
	
	public void stop() {
		if (this.mode == Mode.idle) {
			return;
		}
		this.mode = Mode.idle;
		saveSlot();
		slot++;
		loadSlot();
		log.info("Stop tape at slot [{}]", slot);
	}

	public void play() {
		stop();
		this.countUpTime = 0;
		this.countDownTime = 0;
		this.countPause = 15000000;
		this.mode = Mode.play;
		log.info("Started tape at slot [{}]", slot);
	}

	public void record() {
		stop();
		this.tapeSlot = new VZTape();
		this.mode = Mode.record;
		this.count = 0;
		this.recordingStarted = false;
		log.info("Start RECORD for tape at slot [{}]", slot);
	}

	public void cycle() {
		if (tapeSlot == null) {
			return;
		}
		switch (mode) {
		case record:
			handleRecord();
			return;
		case play:
			handlePlay();
			return;
		case idle:
			return;
		default:
		}
	}

	private boolean recordingStarted;
	private int upTime;
	private int downTime;

	private void handleRecord() {
		int l = vz.getVdcLatch();
		int value = l & OUTPUT_MASK;

		this.count++;
		if (this.count >= maxCount) {
			this.count = 0;
		}

		if (this.value != 0 && value == 0) {
			// handle UP -> DOWN count
			vz.writeByte(LATCH, l & (0xff - SOUND_MASK_1) | SOUND_MASK_2);

			recordingStarted = true;
			upTime = count;
			this.value = value;
			this.count = 0;

		} else if (this.value == 0 && value != 0) {
			// handle DOWN -> UP
			vz.writeByte(LATCH, l & (0xff - SOUND_MASK_2) | SOUND_MASK_1);

			downTime = count;
			this.value = value;
			this.count = 0;
			if (recordingStarted) {
				tapeSlot.write(Pair.of(upTime, downTime));
			}
		}
	}

	private int countUpTime;
	private int countDownTime;
	private int countPause;

	private void handlePlay() {
		if (countPause > 0) {
			countPause--;
			if (countPause == 0) {
				Pair<Integer, Integer> v = tapeSlot.read();
				if (v == null) {
					stop();
					return;
				}
				countUpTime = v.getLeft();
				countDownTime = v.getRight();
				// first up!
				doUp();
			}
			return;
		}
		if (countUpTime > 0) {
			countUpTime--;
			if (countUpTime == 0) {
				doDown();
			}
			return;
		}
		countDownTime--;
		if (countDownTime == 0) {
			doUp();

			Pair<Integer, Integer> v = tapeSlot.read();
			if (v == null) {
				stop();
				return;
			}
			countUpTime = v.getLeft();
			countDownTime = v.getRight();
		}
	}

	private void doDown() {
		int l = vz.getVdcLatch();
		vz.writeByte(LATCH, ((l | INPUT_MASK) & (0xff - SOUND_MASK_2)) | SOUND_MASK_1);
	}

	private void doUp() {
		int l = vz.getVdcLatch();
		vz.writeByte(LATCH, (l & (0xff - INPUT_MASK) & (0xff - SOUND_MASK_1)) | SOUND_MASK_2);
	}
}
