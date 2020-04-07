package jemu.system.vz;

import com.fasterxml.jackson.databind.ObjectMapper;
import jemu.core.cpu.Z80;
import jemu.core.device.Device;
import jemu.core.device.DeviceMapping;
import jemu.exception.JemuException;
import jemu.rest.dto.TapeInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

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

    public static final int OUTPUT_MASK = 0x06;
    public static final int INPUT_MASK = 0x40;
    public static final int SOUND_MASK_1 = 0x01;
    public static final int SOUND_MASK_2 = 0x20;
    public static final int LATCH = 0x6800;

    private int value = 0;
    private static final int maxCount = 65535;
    private int count = 1;
    private Mode mode;

    private VZ vz;
    private VzTape tape;
    private String tapeName;
    private boolean unsavedChanges;

    private ObjectMapper mapper = new ObjectMapper();

    public VZTapeDevice(VZ vz) {
        super(DEVICE_ID);
        this.vz = vz;
        mode = Mode.idle;
        reset();
    }

    @Override
    public void reset() {
        if (this.mode == Mode.record) {
            saveTape();
        }
        this.tapeName = "default";
        this.mode = Mode.idle;
        this.unsavedChanges = false;
        loadTape();
    }

    public void register(Z80 z80) {
        z80.addInputDeviceMapping(new DeviceMapping(this, IN_PORT_MASK, IN_PORT_TEST));
        z80.addOutputDeviceMapping(new DeviceMapping(this, OUT_PORT_MASK, OUT_PORT_TEST));
    }

    private VzTapeSlot tapeSlot() {
        return tape.getSlot();
    }

    // -------- load / save --

    private void loadTape() {
        // path
        Path path = getTapePath(tapeName);
        if (!path.toFile().exists()) {
            this.tape = new VzTape();
            return;
        }

        try {
            this.tape = mapper.readValue(path.toFile(), VzTape.class);
        } catch (Exception e) {
            log.error("unable to read tape at path {}", path.toString(), e);
        }

    }

    private void saveTape() {
        // path
        Path path = getTapePath(tapeName);
        try {
            mapper.writeValue(path.toFile(), this.tape);
        } catch (Exception e) {
            log.error("unable to read tape at path {}", path.toString());
        }
    }

    // -------- manage ports --

    /**
     * Listens to Ports 0xfe und 0xff
     * <p>
     * - Port 0xfe: set state (0 = stop, 1 = read, 2 = write)
     * - Port 0xff: set slot position
     *
     * @param port
     * @param value
     */
    @Override
    public void writePort(int port, int value) {
        int p = port & 0xff;
        if (p == 0xfe) {
            switch (value) {
                case COMMAND_STOP:
                    stop();
                    vz.alert(String.format("stopped tape at #%05d", tape.getPosition()));
                    break;
                case COMMAND_PLAY:
                    vz.alert(String.format("play tape at #%05d", tape.getPosition()));
                    play();
                    break;
                case COMMAND_RECORD:
                    vz.alert(String.format("record tape at #%05d", tape.getPosition()));
                    record();
                    break;
                default:
            }
            return;
        }
        vz.alert(String.format("rewind to #%05d", value));
        setPosition(value);
    }

    /**
     * Listens to Ports 0xfe und 0xff
     * <p>
     * - Port 0xfe: return state (0 = stopped, 1 = read, 2 = write)
     * - Port 0xff: return slot position
     *
     * @param port
     * @return
     */
    @Override
    public int readPort(int port) {
        int p = port & 0xff;
        if (p == 0xfe) {
            switch (mode) {
                case play:
                    return 1;
                case record:
                    return 16;
                case idle:
                default:
                    return 0;
            }
        }
        return tape.getPosition() & 0xff;
    }

    // -------- controls --

    public List<TapeInfo> readTapeInfos() {
        List<String> tapenames = new ArrayList<>();
        try {
            Files.newDirectoryStream(getTapeDir(), "*.json")
                 .forEach(path -> tapenames.add(path.getFileName().toString()));
        } catch (IOException e) {
            throw new JemuException("error reading path directory", e);
        }
        return tapenames.stream().filter(name -> name.matches("tape_.+\\.json"))
                        .map(filename -> filename.substring(5, filename.length() - 5)).map(this::readTapeInfo)
                        .collect(Collectors.toList());
    }

    public TapeInfo readTapeInfo(String tapename) {
        try {
            Path path = getTapePath(tapename);
            VzTape tape = mapper.readValue(path.toFile(), VzTape.class);
            return new TapeInfo(tapename, tape.getPosition(), tape.getSize(), Mode.idle);
        } catch (Exception e) {
            throw new JemuException(String.format("error reading tape with name %s", tapename), e);
        }
    }

    public VzTape createTape(String tapename) {
        VzTape tape = new VzTape();
        Path path = VZTapeDevice.getTapePath(tapename);
        try {
            mapper.writeValue(path.toFile(), tape);
        } catch (Exception e) {
            throw new JemuException(String.format("unable to create tape file %s", path.toString()));
        }
        return tape;
    }

    public void deleteTape(String tapename) {
        Path path = VZTapeDevice.getTapePath(tapename);
        if (!path.toFile().delete()) {
            throw new JemuException(String.format("unable to delete tape file %s", path.toString()));
        }
    }

    public Mode getMode() {
        return mode;
    }

    public static Path getTapePath(String tapeName) {
        Path path = getTapeDir();
        if (!path.toFile().exists()) {
            path.toFile().mkdirs();
        }
        return path.resolve("tape_" + tapeName + ".json");
    }

    public static Path getTapeDir() {
        return Paths.get(System.getProperty("user.home"), "vz200", "tape");
    }

    /**
     * returns current slot position
     *
     * @return slot position
     */
    public int getPosition() {
        return tape.getPosition();
    }

    /**
     * set slot position
     *
     * @param slot
     */
    public void setPosition(int slot) {
        stop();
        tape.setPosition(slot);
        log.info("rewind tape to slot {}", slot);
    }

    /**
     * save current tape and insert another
     *
     * @param tapeName
     */
    public void changeTape(String tapeName) {
        stop();
        log.info("change tape to [{}]", tapeName);
        this.tapeName = tapeName;
        loadTape();
    }

    /**
     * get name of current tape
     *
     * @return
     */
    public String getTapeName() {
        return this.tapeName;
    }

    public int getSlotsSize() {
        return tape.getSize();
    }

    public void stop() {
        if (this.mode == Mode.idle) {
            return;
        }
        this.mode = Mode.idle;
        saveTape();
        tape.nextPosition();
        log.info("Stop tape at slot [{}]", tape.getPosition());
    }

    public void play() {
        stop();
        this.countUpTime = 0;
        this.countDownTime = 0;
        this.countPause = 15000000;
        this.mode = Mode.play;
        log.info("Started tape at slot [{}]", tape.getPosition());
    }

    public void record() {
        stop();
        tapeSlot().clear();
        this.mode = Mode.record;
        this.count = 0;
        this.recordingStarted = false;
        log.info("Start RECORD for tape at slot [{}]", tape.getPosition());
    }

    // -------- tape emulator --

    /**
     * processor cycle hook: liest und schreibt das output-Latch, je nach Status des Tape-Devices
     * <p>
     * - record: lauscht auf Signale des Tape-Ausgangs; schneidet sie mit und routet sie auf den Lautsprecher
     * zum Mithören
     * - read: routet die gespeicherten, mitgeschnittenen Signale auf den Tape-Eingang und auf den Lautsprecher zum
     * Mithören
     * - idle: nichts zu tun
     */
    public void cycle() {
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
            recDoDown();

            recordingStarted = true;
            upTime = count;
            this.value = value;
            this.count = 0;

        } else if (this.value == 0 && value != 0) {
            // handle DOWN -> UP
            recDoUp();

            downTime = count;
            this.value = value;
            this.count = 0;
            if (recordingStarted) {
                tapeSlot().write(upTime, downTime);
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
                Pair<Integer, Integer> v = tapeSlot().read();
                if (v == null) {
                    stop();
                    return;
                }
                countUpTime = v.getLeft();
                countDownTime = v.getRight();
                // first up!
                playDoUp();
            }
            return;
        }
        if (countUpTime > 0) {
            countUpTime--;
            if (countUpTime == 0) {
                playDoDown();
            }
            return;
        }
        countDownTime--;
        if (countDownTime == 0) {
            playDoUp();

            Pair<Integer, Integer> v = tapeSlot().read();
            if (v == null) {
                stop();
                return;
            }
            countUpTime = v.getLeft();
            countDownTime = v.getRight();
        }
    }

    private void playDoDown() {
        int l = vz.getVdcLatch();
        vz.writeByte(LATCH, ((l | INPUT_MASK) & (0xff - SOUND_MASK_2)) | SOUND_MASK_1);
    }

    private void playDoUp() {
        int l = vz.getVdcLatch();
        vz.writeByte(LATCH, (l & (0xff - INPUT_MASK) & (0xff - SOUND_MASK_1)) | SOUND_MASK_2);
    }

    private void recDoDown() {
        int l = vz.getVdcLatch();
        vz.writeByte(LATCH, (l & (0xff - OUTPUT_MASK) & (0xff - SOUND_MASK_2)) | SOUND_MASK_1);
    }

    private void recDoUp() {
        int l = vz.getVdcLatch();
        vz.writeByte(LATCH, ((l | OUTPUT_MASK) & (0xff - SOUND_MASK_1)) | SOUND_MASK_2);
    }
}
