package jemu.ui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import jemu.config.JemuConfiguration;
import jemu.core.Util;
import jemu.core.device.Computer;
import jemu.core.device.ComputerDescriptor;
import jemu.core.device.FileDescriptor;
import jemu.util.assembler.z80.Constants;

/**
 * Title: JEMU Description: The Java Emulation Platform Copyright: Copyright (c)
 * 2002 Company:
 * 
 * @author
 * @version 1.0
 */

@org.springframework.stereotype.Component
public class JemuUi extends JPanel
		implements KeyListener, MouseListener, ItemListener, ActionListener, FocusListener, Runnable {

	private static final Logger log = LoggerFactory.getLogger(JemuUi.class);
	private static final long serialVersionUID = 1L;

	protected Computer computer = null;

	protected boolean isStandalone = false;
	protected Display display = new Display();
	protected Debugger debug = null;
	protected JButton bReset = new JButton("Reset");
	protected boolean started = false;
	protected boolean large = false;
	protected Thread focusThread = null;
	protected Color background;
	protected boolean gotGames = false;
	protected ImageComponent keyboardImagePanel;
	protected JComboBox<ComputerDescriptor> computerSelectionBox;
	protected boolean fullscreen;

	private JemuConfiguration config;

	public String getParameter(String key, String def) {
		return System.getProperty(key, def);
	}

	@Autowired
	public JemuUi(JemuConfiguration config) {
		this.config = config;
		this.isStandalone = true;
		enableEvents(AWTEvent.KEY_EVENT_MASK);
	}

	public void init() {
		requestFocus();
	}

	public void start(boolean fullscreen) {
		this.fullscreen = fullscreen;
		try {
			log.info("init()");
			removeAll();
			background = getBackground();
			setBackground(Color.black);
			setLayout(new BorderLayout());
			add(display, BorderLayout.CENTER);
			display.setDoubleBuffered(false);
			display.setBackground(Color.black);
			display.addKeyListener(this);
			display.addMouseListener(this);
			display.addFocusListener(this);
			boolean debug = Util.getBoolean(getParameter("DEBUG", "false"));
			boolean pause = Util.getBoolean(getParameter("PAUSE", "false"));
			large = Util.getBoolean(getParameter("LARGE", "false"));
			log.info("DEBUG=" + debug + ", PAUSE=" + pause + ", LARGE=" + large);
			if (computer == null) {
				setComputer(getParameter("COMPUTER", Computer.DEFAULT_COMPUTER), !(debug || pause));
			} else if (!(debug || pause)) {
				computer.start();
			}
			log.info("Computer Set to [{}]", computer.getName());

			if (!fullscreen) {
				boolean status = Util.getBoolean(getParameter("STATUS", "false"));
				JPanel topPanel = null;
				if (status) {
					topPanel = new JPanel();
					topPanel.setLayout(new FlowLayout());
				}
				boolean selector = Util.getBoolean(getParameter("SELECTOR", "true"));
				if (selector) {
					if (topPanel == null) {
						topPanel = new JPanel();
						topPanel.setLayout(new FlowLayout());
					}
					JLabel computerLabel = new JLabel("Computer:");
					computerLabel.setForeground(new Color(0, 0, 127));
					topPanel.add(computerLabel);
					computerSelectionBox = new JComboBox<>();
					for (int i = 0; i < Computer.COMPUTERS.length; i++) {
						ComputerDescriptor desc = Computer.COMPUTERS[i];
						if (desc.shown) {
							computerSelectionBox.addItem(desc);
							if (computer.getName().equalsIgnoreCase(desc.key))
								computerSelectionBox.setSelectedIndex(computerSelectionBox.getItemCount() - 1);
						}
					}
					topPanel.add(computerSelectionBox);
					computerSelectionBox.addItemListener(this);
					JLabel loadLabel = new JLabel("Program:");
					loadLabel.setForeground(new Color(0, 0, 127));
					topPanel.add(loadLabel);
					bReset.addActionListener(this);
					topPanel.add(bReset);
					JButton loadButton = new JButton("LOAD");
					loadButton.addActionListener(event -> {
						String filename = chooseLoadFile();

						boolean running = computer.isRunning();
						computer.stop();

						if (filename != null) {
							try {
								if (filename.toUpperCase().endsWith(".HEX")) {
									loadHexFile(filename);
								} else if (filename.toUpperCase().endsWith(".ASM")) {
									loadAsmFile(filename);
								} else {
									loadFile(filename);
								}
							} catch (Exception e) {
								log.error("Error loading file {}", filename, e);
								alertException(e);
							} finally {
								display.requestFocus();
								if (running) {
									computer.start();
								}
							}
						}
					});
					JButton saveButton = new JButton("SAVE");
					saveButton.addActionListener(event -> {
						String filename = chooseSaveFile();
						if (filename != null) {
							try {
								computer.saveFile(filename);
							} catch (Exception e) {
								log.info("Error saving file " + filename);
								e.printStackTrace();
							}
						}
					});
					topPanel.add(loadButton);
					topPanel.add(saveButton);
				}

				if (topPanel != null) {
					topPanel.setBackground(background);
					add(topPanel, BorderLayout.NORTH);
				}
				if (debug)
					showDebugger();
			}
			started = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String chooseSaveFile() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Save program");
		chooser.setApproveButtonText("SAVE");
		chooser.setCurrentDirectory(new File(config.get(Constants.LAST_WORKING_DIR)));
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			config.set(Constants.LAST_WORKING_DIR, chooser.getCurrentDirectory().getAbsolutePath());
			return file.getAbsolutePath();
		}
		return null;
	}

	private String chooseLoadFile() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Load program");
		chooser.setApproveButtonText("LOAD");
		chooser.setCurrentDirectory(new File(config.get(Constants.LAST_WORKING_DIR)));
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			config.set(Constants.LAST_WORKING_DIR, chooser.getCurrentDirectory().getAbsolutePath());
			return file.getAbsolutePath();
		}
		return null;
	}

	private void alertException(Exception e) {
		JDialog dialog = new JDialog();
		dialog.setTitle("Fehler");
		dialog.setLocationRelativeTo(this);
		dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		LayoutManager layout = new BorderLayout();
		dialog.setLayout(layout);

		JLabel label = new JLabel(e.getMessage().trim());
		label.setFont(new Font("sans-serif", Font.BOLD, 14));
		dialog.add(label, BorderLayout.NORTH);

		String trace = StringEscapeUtils.escapeHtml4(getShortTrace(e)).replaceAll("\n", "<br>");
		JLabel error = new JLabel("<html><div style=\"font-size:normal;\">" + trace + "</html>");
		error.setVerticalAlignment(SwingConstants.TOP);
		dialog.add(error, BorderLayout.CENTER);

		JButton closeButton = new JButton("CLOSE");
		closeButton.addActionListener(event -> {
			dialog.dispose();
		});
		dialog.add(closeButton, BorderLayout.SOUTH);

		dialog.getRootPane().setDefaultButton(closeButton);
		dialog.setPreferredSize(new Dimension(640, 400));
		dialog.pack();
		dialog.setVisible(true);
	}

	private String getShortTrace(Exception e) {
		StringWriter sw = new StringWriter();
		Throwable currentException = e.getCause();
		while (currentException != null) {
			sw.append("=> " + currentException.getMessage());
			currentException = currentException.getCause();
		}
		return sw.toString();
	}

	private void refreshKeyboardImage(boolean fullscreen) {
		String keyboardImage = fullscreen ? null : computer.getKeyboardImage();
		if (keyboardImagePanel != null) {
			remove(keyboardImagePanel);
			keyboardImagePanel = null;
		}
		if (keyboardImage != null) {
			keyboardImagePanel = new ImageComponent(keyboardImage, config.getInt(Constants.SCREEN_WIDTH));
			keyboardImagePanel.setAlignmentX(CENTER_ALIGNMENT);
			keyboardImagePanel.setAlignmentY(CENTER_ALIGNMENT);
			add(keyboardImagePanel, BorderLayout.SOUTH);
		}
	}

	public boolean isStarted() {
		return started;
	}

	public void waitStart() {
		try {
			while (!started)
				Thread.sleep(10);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void focusDisplay() {
		display.requestFocus();
		/*
		 * if (!display.isFocused()) (focusThread = new Thread(this)).start();
		 */
	}

	public void run() {
	}

	public void startComputer() {
		computer.start();
	}

	public void stop() {
		log.info("stop()");
		computer.stop();
	}

	public void destroy() {
	}

	public String getAppletInfo() {
		return "Applet Information";
	}

	public String[][] getParameterInfo() {
		return null;
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		computer.processKeyEvent(e);
	}

	public void keyReleased(KeyEvent e) {
		computer.processKeyEvent(e);
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2 && !fullscreen)
			if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
				showDebugger();
			}
	}

	public void mousePressed(MouseEvent e) {
		display.requestFocus();
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void showDebugger() {
		try {
			log.info("showDebugger");
			if (debug == null) {
				debug = (Debugger) Util.secureConstructor(Debugger.class, new Class[] {}, new Object[] {});
				debug.setBounds(0, 0, 500, 400);
				debug.setComputer(computer);
			}
			log.info("Showing Debugger");
			debug.setVisible(true);
			debug.toFront();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Computer getComputer() {
		return computer;
	}

	public String loadHexFile(String name) throws Exception {
		String result = null;
		boolean running = computer.isRunning();
		computer.stop();
		try {
			computer.loadHexFile(name);
			result = computer.getFileInfo(name);
		} finally {
			display.requestFocus();
			if (running) {
				computer.start();
			}
		}
		return result;
	}

	public String loadAsmFile(String name) throws Exception {
		String result = null;
		computer.loadAsmFile(name);
		result = computer.getFileInfo(name);
		return result;
	}

	public String loadFile(String name) {
		String result = null;
		try {
			boolean running = computer.isRunning();
			computer.stop();
			try {
				if (name.endsWith(".vz")) {
					computer.loadBinaryFile(name);
				} else {
					computer.loadSourceFile(name);
				}
				result = computer.getFileInfo(name);
			} finally {
				display.requestFocus();
				if (running) {
					computer.start();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public void loadHexFile(InputStream is) throws Exception {
		computer.loadHexFile(is);
	}

	public void loadAsmFile(InputStream is) throws Exception {
		computer.loadAsmFile(is);
	}

	public void loadBinaryFile(InputStream is) throws Exception {
		computer.loadBinaryFile(is);
	}

	public void loadSourceFile(InputStream is) throws Exception {
		computer.loadSourceFile(is);
	}

	public List<String> flushPrinter() {
		return computer.flushPrinter();
	}
	
	public void resetComputer() {
		computer.reset();
		computer.start();
	}

	public void setComputer(String name) throws Exception {
		setComputer(name, true);
	}

	public void setComputer(String name, boolean start) throws Exception {
		if (computer == null || !name.equalsIgnoreCase(computer.getName())) {
			Computer newComputer = Computer.createComputer(this, name);
			if (computer != null) {
				computer.dispose();
				computer = null;
				Runtime runtime = Runtime.getRuntime();
				runtime.gc();
				runtime.runFinalization();
				runtime.gc();
				log.info("Computer Disposed");
			}
			computer = newComputer;
			setFullSize(large);
			computer.initialise();
			refreshKeyboardImage(config.getBoolean(Constants.FULLSCREEN));
			if (debug != null)
				debug.setComputer(computer);
			if (start)
				computer.start();
		}
	}

	public void setFullSize(boolean value) {
		large = value;
		boolean running = computer.isRunning();
		computer.stop();
		computer.setLarge(large);
		display.setImageSize(computer.getDisplaySize(large), computer.getDisplayScale(large));
		computer.setDisplay(display);
		if (running)
			computer.start();
	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			Object item = computerSelectionBox.getSelectedItem();
			try {
				setComputer(((ComputerDescriptor) item).key);
				findWindow(this).pack();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public Window findWindow(Component comp) {
		while (comp != null)
			if (comp instanceof Window)
				return (Window) comp;
			else
				comp = comp.getParent();
		return null;
	}

	public Vector<FileDescriptor> getFiles() {
		Vector<FileDescriptor> files = computer == null ? new Vector<>() : computer.getFiles();
		Vector<FileDescriptor> result = files.size() == 0 ? files : new Vector<>(files.size() * 2);
		for (int i = 0; i < files.size(); i++) {
			FileDescriptor file = files.elementAt(i);
			result.add(file);
		}
		return result;
	}

	public Vector<ComputerDescriptor> getComputers() {
		int count = Computer.COMPUTERS.length;
		Vector<ComputerDescriptor> result = new Vector<>(count);
		for (int i = 0; i < count; i++) {
			ComputerDescriptor desc = Computer.COMPUTERS[i];
			if (desc.shown) {
				result.add(desc);
			}
		}
		return result;
	}

	public void focusLost(FocusEvent e) {
		computer.displayLostFocus();
	}

	public void focusGained(FocusEvent e) {
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == bReset)
			computer.reset();
	}

}