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

import jemu.config.Constants;
import jemu.config.JemuConfiguration;
import jemu.ui.JemuUi;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

@Component
public class Emulator {

    private JemuConfiguration config;
    private JemuUi jemuUi;

    @Autowired
    public Emulator(JemuConfiguration config, JemuUi jemuUi) {
        this.config = config;
        this.jemuUi = jemuUi;
    }

    public static class EmulatorFrame extends JFrame {

        private static final long serialVersionUID = 1L;

        protected void processWindowEvent(WindowEvent e) {
            super.processWindowEvent(e);
            if (e.getID() == WindowEvent.WINDOW_CLOSING) {
                System.exit(0);
            }
        }

        public synchronized void enableWindowEvents() {
            enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        }
    }

    @PostConstruct
    public void start() {
        EmulatorFrame frame = new EmulatorFrame();
        frame.setTitle("JEMU");
        frame.enableWindowEvents();
        frame.getContentPane().add(jemuUi, BorderLayout.CENTER);
        var fullscreen = config.getBoolean(Constants.FULLSCREEN, true);
        if (fullscreen) {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setUndecorated(true);
        } else {
            frame.setSize(config.getInt(Constants.SCREEN_WIDTH), config.getInt(Constants.SCREEN_HEIGHT));
        }
        frame.setCursor(java.awt.Toolkit.getDefaultToolkit()
                                        .createCustomCursor(new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR),
                                                            new java.awt.Point(0, 0), "NOCURSOR"));
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
        jemuUi.start();
        frame.setVisible(true);
        jemuUi.focusDisplay();
    }
}
