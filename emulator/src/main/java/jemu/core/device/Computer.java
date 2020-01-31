package jemu.core.device;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.zip.ZipInputStream;

import jemu.core.cpu.Processor;
import jemu.core.device.memory.Memory;
import jemu.ui.Display;
import jemu.util.diss.Disassembler;

/**
 * Title: JEMU Description: The Java Emulation Platform Copyright: Copyright (c)
 * 2002 Company:
 * 
 * @author
 * @version 1.0
 */

public abstract class Computer extends Device implements Runnable {

	// Entries are Name, Key, Class, Shown in list

	public static final ComputerDescriptor[] COMPUTERS = {
			new ComputerDescriptor("BBC-B", "Acorn BBC Model B", "jemu.system.bbc.BBC", true),
			new ComputerDescriptor("CPC464", "Amstrad CPC464", "jemu.system.cpc.CPC", true),
			new ComputerDescriptor("CPC664", "Amstrad CPC664", "jemu.system.cpc.CPC", false),
			new ComputerDescriptor("CPC6128", "Amstrad CPC6128", "jemu.system.cpc.CPC", false),
			new ComputerDescriptor("VZ200", "Dick Smith VZ-200", "jemu.system.vz.VZ", false),
			new ComputerDescriptor("VZ300", "Dick Smith VZ-300", "jemu.system.vz.VZ", true),
			new ComputerDescriptor("SPECTRUM48", "Sinclair ZX Spectrum 48K", "jemu.system.spectrum.Spectrum", true),
			new ComputerDescriptor("ZX80", "Sinclair ZX80", "jemu.system.zx.ZX", true),
			new ComputerDescriptor("ZX81", "Sinclair ZX81", "jemu.system.zx.ZX", true) };

	public static final String DEFAULT_COMPUTER = "VZ200";

	public static final int STOP = 0;
	public static final int STEP = 1;
	public static final int STEP_OVER = 2;
	public static final int RUN = 3;

	public static final int MAX_FRAME_SKIP = 20;
	public static final int MAX_FILE_SIZE = 1024 * 1024; // 1024K maximum

	protected Thread thread;
	protected boolean stopped;
	protected int action = STOP;
	protected boolean running;
	protected boolean waiting;
	protected long startTime;
	protected long startCycles;
	protected String name;
	protected String romPath;
	protected String filePath;
	protected Vector<FileDescriptor> files;
	protected Display display;
	protected int frameSkip;
	protected int runTo;
	protected int mode;

	// Listeners for stopped emulation
	protected Vector listeners = new Vector(1);

	public Computer(String name) {
		super("Computer: " + name);
		thread = new Thread(this);
		stopped = false;
		action = STOP;
		running = false;
		waiting = false;
		files = null;
		frameSkip = 0;
		runTo = -1;
		mode = STOP;
		this.name = name;
		thread.start();
	}

	protected void setBasePath(String path) {
		romPath = "system/" + path + "/rom/";
		filePath = "system/" + path + "/file/";
	}

	public void initialise() {
		reset();
	}

	public String getKeyboardImage() {
		return null;
	}
	
	public InputStream openFile(String name) throws Exception {
		System.out.println("File: " + name);
		InputStream result = null; 
		try {
			if (name.toLowerCase().endsWith(".zip")) {
				ZipInputStream str = new ZipInputStream(result);
				str.getNextEntry();
				result = str;
			} else {
				result = new FileInputStream(name);
			}
		} catch (Exception e) {
			e.printStackTrace(); 
		}
		return result;
	}

	protected int getWord(byte[] buffer, int offs) {
		return (buffer[offs] & 0xff) | ((buffer[offs + 1] << 8) & 0xff00);
	}

	protected int getWordBE(byte[] buffer, int offs) {
		return (buffer[offs + 1] & 0xff) | ((buffer[offs] << 8) & 0xff00);
	}

	protected int readStream(InputStream stream, byte[] buffer, int offs, int size) throws Exception {
		return readStream(stream, buffer, offs, size, true);
	}

	protected int readStream(InputStream stream, byte[] buffer, int offs, int size, boolean error) throws Exception {
		while (size > 0) {
			int read = stream.read(buffer, offs, size);
			if (read == -1) {
				if (error)
					throw new Exception("Unexpected end of stream");
				else
					break;
			} else {
				offs += read;
				size -= read;
			}
		}
		return offs;
	}

	public byte[] getFile(String name) {
		return getFile(name, MAX_FILE_SIZE, true);
	}

	public byte[] getFile(String name, int size) {
		return getFile(name, size, false);
	}

	public byte[] getFile(String name, int size, boolean crop) {
		byte[] buffer = new byte[size];
		int offs = 0;
		try {
			InputStream stream = null;
			try {
				stream = openFile(name);
				while (size > 0) {
					int read = stream.read(buffer, offs, size);
					if (read == -1)
						break;
					else {
						offs += read;
						size -= read;
					}
				}
			} finally {
				if (stream != null)
					stream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (crop && offs < buffer.length) {
			byte[] result = new byte[offs];
			System.arraycopy(buffer, 0, result, 0, offs);
			buffer = result;
		}
		return buffer;
	}

	public void setDisplay(Display value) {
		display = value;
		displaySet();
	}

	public Display getDisplay() {
		return display;
	}

	protected void displaySet() {
	}

	// For now, only supporting a single Processor
	public Disassembler getDisassembler() {
		return null;
	}

	public abstract Processor getProcessor();

	public abstract Memory getMemory();

	public abstract void processKeyEvent(KeyEvent e);

	public void loadHexFile(String name) throws Exception {
		loadHexFile(new FileInputStream(name));
	}

	public void loadHexFile(InputStream is) throws Exception {
	}

	public String loadAsmFile(String name, Boolean autorun) throws Exception {
		return loadAsmFile(new FileInputStream(name), autorun);
	}

	public String loadAsmFile(InputStream is, Boolean autorun) throws Exception {
		return "";
	}

	public String loadAsmZip(InputStream is, Boolean autorun) throws Exception {
		return "";
	}

	public void loadBinaryFile(String name) throws Exception {
		loadBinaryFile(new FileInputStream(name));
	}

	public void loadBinaryFile(InputStream is) throws Exception {
	}

	public void loadSourceFile(String name) throws Exception {
		loadBinaryFile(new FileInputStream(name));
	}

	public void loadSourceFile(InputStream is) throws Exception {
	}

	public void saveFile(String name) throws Exception {
	}

	public void saveFile(OutputStream os, String range, Boolean autorun) throws Exception {
	}

	public List<String> flushPrinter() {
		return Collections.emptyList();
	}

	public abstract Dimension getDisplaySize(boolean large);

	public void setLarge(boolean value) {
	}

	public Dimension getDisplayScale(boolean large) {
		return large ? Display.SCALE_2 : Display.SCALE_1;
	}

	public void start() {
		setAction(RUN);
	}

	public void stop() {
		setAction(STOP);
	}

	public void step() {
		setAction(STEP);
	}

	public void stepOver() {
		setAction(STEP_OVER);
	}

	public synchronized void setAction(int value) {
		if (running && value != RUN) {
			action = STOP;
			// System.out.println(this + " Stopping " + getProcessor());
			getProcessor().stop();
			display.setPainted(true);
			while (running) {
				try {
					// System.out.println("stopping...");
					Thread.sleep(200);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		// System.out.println("Entering synchronized");
		synchronized (thread) {
			action = value;
			thread.notify();
		}
	}

	public void dispose() {
		stopped = true;
		// System.out.println(this + " thread stopped: " + thread);
		stop();
		// System.out.println(this + " has stopped");
		try {
			thread.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		thread = null;
		display = null;
	}

	protected void emulate(int mode) {
		switch (mode) {
		case STEP:
			getProcessor().step();
			break;

		case STEP_OVER:
			getProcessor().stepOver();
			break;

		case RUN:
			if (runTo == -1)
				getProcessor().run();
			else
				getProcessor().runTo(runTo);
			break;
		}
	}

	public void addActionListener(ActionListener listener) {
		listeners.addElement(listener);
	}

	public void removeActionListener(ActionListener listener) {
		listeners.removeElement(listener);
	}

	protected void fireActionEvent() {
		ActionEvent e = new ActionEvent(this, 0, null);
		for (int i = 0; i < listeners.size(); i++)
			((ActionListener) listeners.elementAt(i)).actionPerformed(e);
	}

	public String getROMPath() {
		return romPath;
	}

	public String getFilePath() {
		return filePath;
	}

	public void softReset() {}

	public void reset() {
		boolean run = running;
		stop();
		getProcessor().reset();
		if (run)
			start();
	}

	public void run() {
		while (!stopped) {
			try {
				if (action == STOP) {
					synchronized (thread) {
						// System.out.println(this + " Waiting");
						thread.wait();
						// System.out.println(this + " Not Waiting");
					}
				}
				if (action != STOP) {
					try {
						// System.out.println(this + " Running");
						running = true;
						synchronized (thread) {
							mode = action;
							action = STOP;
						}
						startCycles = getProcessor().getCycles();
						startTime = System.currentTimeMillis();
						emulate(mode);
					} finally {
						running = false;
						// System.out.println(this + " Not running");
						fireActionEvent();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Vector<FileDescriptor> getFiles() {
		files = new Vector<>();
		SortedSet<FileDescriptor> names = new TreeSet<>();
		try (DirectoryStream<Path> dir = Files.newDirectoryStream(Paths.get(filePath))) {
			dir.forEach(p -> {
				String name = p.toFile().getName();
				if (Files.isRegularFile(p) && (name.toUpperCase().endsWith(".VZ") | name.toUpperCase().endsWith(".HEX"))) {
					names.add(new FileDescriptor(name, name, "none"));
				}
			});
		} catch (IOException e) {
			System.out.println("unable to read dir " + filePath);
			e.printStackTrace();
		}
		files.addAll(names);
		return files;
	}

	public String getFileInfo(String fileName) {
		String result = null;
		getFiles();
		for (int i = 0; i < files.size(); i++) {
			FileDescriptor file = (FileDescriptor) files.elementAt(i);
			if (file.filename.equalsIgnoreCase(fileName)) {
				result = file.instructions;
				break;
			}
		}
		return result;
	}

	public boolean isRunning() {
		return running;
	}

	protected void syncProcessor() {
		startTime += (((getProcessor().getCycles() - startCycles) * 2000 / getProcessor().getCyclesPerSecond()) + 1)
				/ 2;
		startCycles = getProcessor().getCycles();
		long time = System.currentTimeMillis();
		// System.out.print(" " + startTime);
		if (time > startTime) {
			if (frameSkip == MAX_FRAME_SKIP) {
				setFrameSkip(0);
				// System.out.println(" R: " + (time - startTime));
				startTime = time + 1;
			} else {
				// System.out.print(" S" + frameSkip);
				setFrameSkip(frameSkip + 1);
			}
		} else {
			try {
				setFrameSkip(0);
				while (System.currentTimeMillis() < startTime)
					;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}

	public void setFrameSkip(int value) {
		frameSkip = value;
	}

	public void displayLostFocus() {
	}

	public void updateDisplay(boolean wait) {
	}

	public String getName() {
		return name;
	}

	public void setRunToAddress(int value) {
		runTo = value;
	}

	public void clearRunToAddress() {
		runTo = -1;
	}

	public int getMode() {
		return mode;
	}

}