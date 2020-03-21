package jemu.ui;

import jemu.config.JemuConfiguration;
import jemu.core.device.Computer;
import jemu.core.device.ComputerDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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

	private final Computer computer;

	protected Display display;
	protected boolean started;
	protected Color background;
	protected ImageComponent keyboardImagePanel;
	protected JComboBox<ComputerDescriptor> computerSelectionBox;

	@Autowired
	public JemuUi(JemuConfiguration config, Computer computer) {
		this.computer = computer;
		this.display = new Display();
		started = false;
		enableEvents(AWTEvent.KEY_EVENT_MASK);
	}

	public void start() {
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
			initComputer();
			started = true;
		} catch (Exception e) {
			log.error("error initializing emulator", e);
		}
	}


	public void focusDisplay() {
		display.requestFocus();
	}

	public void run() {
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

	public Computer getComputer() {
		return computer;
	}

	public void softReset() {
		computer.softReset();
	}

	public void initComputer() throws Exception {
		setFullSize();
		computer.initialise();
		computer.start();
	}

	public void setFullSize() {
		display.setImageSize(computer.getDisplaySize(), Display.SCALE_1);
		computer.setDisplay(display);
	}

	public void focusLost(FocusEvent e) {
		computer.displayLostFocus();
	}

	public void focusGained(FocusEvent e) {
	}

	public void actionPerformed(ActionEvent e) {
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
	}
}