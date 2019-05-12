package jemu;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.annotation.PostConstruct;
import javax.swing.JFrame;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jemu.config.JemuConfiguration;
import jemu.ui.JemuUi;
import jemu.util.assembler.z80.Constants;

@Component
public class Emulator {
	
	private JemuConfiguration config;
	private JemuUi jemuUi;
	
	@Autowired
	public Emulator(JemuConfiguration config, JemuUi jemuUi) {
		this.config = config;
		this.jemuUi = jemuUi;
	}

	@PostConstruct
	public void start() {	
		JFrame frame = new JFrame() {
			private static final long serialVersionUID = 1L;

			protected void processWindowEvent(WindowEvent e) {
				super.processWindowEvent(e);
				if (e.getID() == WindowEvent.WINDOW_CLOSING) {
					System.exit(0);
				}
			}

			public synchronized void setTitle(String title) {
				super.setTitle(title);
				enableEvents(AWTEvent.WINDOW_EVENT_MASK);
			}

		};
		frame.setTitle("JEMU");
		frame.getContentPane().add(jemuUi, BorderLayout.CENTER);
		// frame.setBackground(Color.black);
		if (config.getBoolean(Constants.FULLSCREEN)) {
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
			frame.setUndecorated(true);
			frame.setCursor(java.awt.Toolkit.getDefaultToolkit().createCustomCursor(
					new BufferedImage(1,1,BufferedImage.TYPE_4BYTE_ABGR),
					new java.awt.Point(0,0),
					"NOCURSOR"));
		} else {
			frame.setSize(config.getInt(Constants.SCREEN_WIDTH), config.getInt(Constants.SCREEN_HEIGHT));
		}
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
		jemuUi.init();
		jemuUi.start(config.getBoolean(Constants.FULLSCREEN));
		frame.setVisible(true);
		jemuUi.focusDisplay();
	}
}
